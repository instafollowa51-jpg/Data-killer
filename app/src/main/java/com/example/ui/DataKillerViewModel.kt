package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.DataKillerDatabase
import com.example.data.db.SessionRepository
import com.example.data.db.StressSessionEntity
import com.example.data.network.NetworkInfoState
import com.example.data.network.NetworkMonitor
import com.example.engine.DataKillerEngine
import com.example.engine.EngineStats
import com.example.engine.StressMode
import com.example.service.DataKillerService
import com.example.util.FormatUtils
import com.example.util.NetworkRoutingMode
import com.example.util.SpeedUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DataKillerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DataKillerDatabase.getDatabase(application)
    val repository = SessionRepository(database.stressSessionDao())
    val networkMonitor = NetworkMonitor(application)
    val engine = DataKillerEngine(viewModelScope)

    val engineStats: StateFlow<EngineStats> = engine.stats

    val networkInfo: StateFlow<NetworkInfoState> = networkMonitor.observeNetwork()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = networkMonitor.getCurrentNetworkInfo()
        )

    val allSessions: StateFlow<List<StressSessionEntity>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalLifetimeBurned: StateFlow<Long?> = repository.totalBytesBurned
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val allTimePeakSpeed: StateFlow<Double?> = repository.allTimePeakSpeed
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val sessionCount: StateFlow<Int> = repository.sessionCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // User Test Configuration
    private val _selectedMode = MutableStateFlow(StressMode.DOWNLOAD)
    val selectedMode: StateFlow<StressMode> = _selectedMode.asStateFlow()

    private val _routingMode = MutableStateFlow(NetworkRoutingMode.AUTO_ALL)
    val routingMode: StateFlow<NetworkRoutingMode> = _routingMode.asStateFlow()

    private val _concurrencyThreads = MutableStateFlow(8)
    val concurrencyThreads: StateFlow<Int> = _concurrencyThreads.asStateFlow()

    private val _targetLimitBytes = MutableStateFlow(0L) // 0 = Unlimited
    val targetLimitBytes: StateFlow<Long> = _targetLimitBytes.asStateFlow()

    private val _targetDurationSeconds = MutableStateFlow(0L) // 0 = Unlimited
    val targetDurationSeconds: StateFlow<Long> = _targetDurationSeconds.asStateFlow()

    private val _speedUnit = MutableStateFlow(SpeedUnit.MBPS)
    val speedUnit: StateFlow<SpeedUnit> = _speedUnit.asStateFlow()

    private val _keepScreenOn = MutableStateFlow(true)
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    // iOS Liquid Glass Optics & Materials State
    private val _liquidGlassConfig = MutableStateFlow(com.example.ui.components.LiquidGlassConfig.STANDARD_IOS)
    val liquidGlassConfig: StateFlow<com.example.ui.components.LiquidGlassConfig> = _liquidGlassConfig.asStateFlow()

    // 0 = Dashboard, 1 = Real-Time Usage Monitor, 2 = Telemetry, 3 = History
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _completedSessionResult = MutableStateFlow<StressSessionEntity?>(null)
    val completedSessionResult: StateFlow<StressSessionEntity?> = _completedSessionResult.asStateFlow()

    private var sessionStartTimeMillis = 0L

    init {
        // Collect stats to update foreground service notification
        viewModelScope.launch {
            engineStats.collect { stats ->
                if (stats.isRunning && !stats.isPaused) {
                    val speedStr = FormatUtils.formatSpeed(stats.currentTotalSpeedMbps, _speedUnit.value)
                    val burnedStr = FormatUtils.formatBytes(stats.totalBytesBurned)

                    val intent = Intent(getApplication(), DataKillerService::class.java).apply {
                        putExtra(DataKillerService.EXTRA_SPEED, speedStr)
                        putExtra(DataKillerService.EXTRA_BURNED, burnedStr)
                    }
                    try {
                        getApplication<Application>().startService(intent)
                    } catch (_: Exception) {}
                }

                if ((stats.limitReached || stats.durationReached) && stats.isRunning) {
                    stopStressTest(
                        reason = if (stats.limitReached) "TARGET_DATA_QUOTA_REACHED" else "TARGET_DURATION_COMPLETED"
                    )
                }
            }
        }
    }

    fun startStressTest() {
        sessionStartTimeMillis = System.currentTimeMillis()
        val targetNetwork = networkMonitor.getNetworkForMode(_routingMode.value)

        engine.start(
            mode = _selectedMode.value,
            concurrency = _concurrencyThreads.value,
            targetLimitBytes = _targetLimitBytes.value,
            targetDurationSeconds = _targetDurationSeconds.value,
            routingMode = _routingMode.value,
            targetNetwork = targetNetwork
        )

        try {
            DataKillerService.startService(getApplication())
        } catch (_: Exception) {}
    }

    fun pauseStressTest() {
        engine.pause()
    }

    fun resumeStressTest() {
        engine.resume()
    }

    fun stopStressTest(reason: String = "MANUAL_STOP") {
        val finalStats = engine.stop(reason)
        val endTimeMillis = System.currentTimeMillis()

        try {
            DataKillerService.stopService(getApplication())
        } catch (_: Exception) {}

        if (finalStats.totalBytesBurned > 0 || finalStats.elapsedSeconds > 0) {
            val netInfo = networkInfo.value
            val session = StressSessionEntity(
                startTimeMillis = sessionStartTimeMillis,
                endTimeMillis = endTimeMillis,
                durationSeconds = finalStats.elapsedSeconds,
                totalBytesDownloaded = finalStats.totalBytesDownloaded,
                totalBytesUploaded = finalStats.totalBytesUploaded,
                peakSpeedMbps = finalStats.peakTotalSpeedMbps,
                avgSpeedMbps = finalStats.avgSpeedMbps,
                avgPingMs = finalStats.avgPingMs,
                minPingMs = finalStats.minPingMs,
                maxPingMs = finalStats.maxPingMs,
                jitterMs = finalStats.jitterMs,
                packetLossPercent = finalStats.packetLossPercent,
                stabilityScorePercent = finalStats.stabilityScorePercent,
                networkType = "${netInfo.typeName} ${netInfo.frequencyBand}".trim(),
                networkOperator = netInfo.operatorOrSsid,
                testMode = _selectedMode.value.name,
                networkRoutingMode = _routingMode.value.name,
                threadCount = _concurrencyThreads.value,
                targetLimitBytes = _targetLimitBytes.value,
                targetDurationSeconds = _targetDurationSeconds.value,
                completedByLimit = (reason != "MANUAL_STOP"),
                completionReason = reason
            )

            viewModelScope.launch {
                val id = repository.saveSession(session)
                _completedSessionResult.value = session.copy(id = id)
            }
        }
    }

    fun dismissSessionSummary() {
        _completedSessionResult.value = null
    }

    fun deleteSession(session: StressSessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun setSelectedMode(mode: StressMode) {
        _selectedMode.value = mode
    }

    fun setRoutingMode(mode: NetworkRoutingMode) {
        _routingMode.value = mode
    }

    fun setConcurrencyThreads(threads: Int) {
        _concurrencyThreads.value = threads
    }

    fun setTargetLimitBytes(bytes: Long) {
        _targetLimitBytes.value = bytes
    }

    fun setTargetDurationSeconds(seconds: Long) {
        _targetDurationSeconds.value = seconds
    }

    fun setSpeedUnit(unit: SpeedUnit) {
        _speedUnit.value = unit
    }

    fun setKeepScreenOn(keep: Boolean) {
        _keepScreenOn.value = keep
    }

    fun setCurrentTab(tab: Int) {
        _currentTab.value = tab
    }

    fun setLiquidGlassPreset(preset: com.example.ui.components.LiquidGlassConfig) {
        _liquidGlassConfig.value = preset
    }

    fun setBlurRadius(dp: Float) {
        _liquidGlassConfig.value = _liquidGlassConfig.value.copy(
            blurRadiusDp = dp,
            presetName = "Custom"
        )
    }

    fun setVibrancy(vibrancy: Float) {
        _liquidGlassConfig.value = _liquidGlassConfig.value.copy(
            vibrancy = vibrancy,
            presetName = "Custom"
        )
    }

    fun setLensRefractionAmount(refraction: Float) {
        _liquidGlassConfig.value = _liquidGlassConfig.value.copy(
            lensRefractionAmount = refraction,
            presetName = "Custom"
        )
    }

    fun setLensRefractionHeight(height: Float) {
        _liquidGlassConfig.value = _liquidGlassConfig.value.copy(
            lensRefractionHeight = height,
            presetName = "Custom"
        )
    }

    fun setDepthEffect(dp: Float) {
        _liquidGlassConfig.value = _liquidGlassConfig.value.copy(
            depthEffectDp = dp,
            presetName = "Custom"
        )
    }

    fun resetToStandardIosGlass() {
        _liquidGlassConfig.value = com.example.ui.components.LiquidGlassConfig.STANDARD_IOS
    }
}
