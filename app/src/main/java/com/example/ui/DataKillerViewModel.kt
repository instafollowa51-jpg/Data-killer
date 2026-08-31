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
import com.example.data.network.NetworkOptimizationTip
import com.example.data.network.NetworkStabilityAnalysis
import com.example.data.network.NetworkType
import com.example.engine.DataKillerEngine
import com.example.engine.EngineStats
import com.example.engine.StressMode
import com.example.service.DataKillerService
import com.example.util.FormatUtils
import com.example.util.NetworkRoutingMode
import com.example.util.SpeedUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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

    // 0 = Dashboard, 1 = Strength Analyzer, 2 = Telemetry, 3 = History
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Live Network Strength & Stability Analysis State
    private val _stabilityAnalysis = MutableStateFlow(NetworkStabilityAnalysis())
    val stabilityAnalysis: StateFlow<NetworkStabilityAnalysis> = _stabilityAnalysis.asStateFlow()

    private val _completedSessionResult = MutableStateFlow<StressSessionEntity?>(null)
    val completedSessionResult: StateFlow<StressSessionEntity?> = _completedSessionResult.asStateFlow()

    private val signalHistoryBuffer = mutableListOf<Float>()
    private val pingHistoryBuffer = mutableListOf<Float>()
    private var sessionStartTimeMillis = 0L

    init {
        // Collect stats to update foreground service notification smoothly
        viewModelScope.launch {
            engineStats.collect { stats ->
                if (stats.isRunning && !stats.isPaused) {
                    val speedStr = FormatUtils.formatSpeed(stats.currentTotalSpeedMbps, _speedUnit.value)
                    val burnedStr = FormatUtils.formatBytes(stats.totalBytesBurned)
                    DataKillerService.updateNotification(getApplication(), speedStr, burnedStr)
                }

                if ((stats.limitReached || stats.durationReached) && stats.isRunning) {
                    stopStressTest(
                        reason = if (stats.limitReached) "TARGET_DATA_QUOTA_REACHED" else "TARGET_DURATION_COMPLETED"
                    )
                }
            }
        }

        // Real-Time Network Strength, Stability & Diagnostics Continuous Engine
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                try {
                    updateNetworkStabilityMetrics()
                } catch (_: Exception) {}
                delay(1200L)
            }
        }
    }

    private suspend fun updateNetworkStabilityMetrics() {
        val netInfo = networkInfo.value
        val stats = engineStats.value

        var liveLatency = 0.0
        var jitter = 0.0
        var packetLoss = 0.0

        if (stats.isRunning && stats.currentPingMs > 0) {
            liveLatency = stats.currentPingMs
            jitter = stats.jitterMs
            packetLoss = stats.packetLossPercent
        } else if (netInfo.isConnected) {
            val pingResult = probeLatencyFast()
            liveLatency = pingResult
            jitter = if (pingHistoryBuffer.isNotEmpty()) {
                abs(liveLatency - pingHistoryBuffer.last())
            } else 1.5
            packetLoss = if (liveLatency >= 999.0) 100.0 else 0.0
        }

        val cleanLatency = if (liveLatency in 1.0..998.0) liveLatency else 22.0

        // Append to historical rolling buffers (up to 30 items)
        val rawSignal = (netInfo.signalPercent.toFloat()).coerceIn(10f, 100f)
        if (signalHistoryBuffer.size >= 30) signalHistoryBuffer.removeAt(0)
        signalHistoryBuffer.add(rawSignal)

        if (pingHistoryBuffer.size >= 30) pingHistoryBuffer.removeAt(0)
        pingHistoryBuffer.add(cleanLatency.toFloat())

        val avgPing = if (pingHistoryBuffer.isNotEmpty()) pingHistoryBuffer.average() else cleanLatency

        // Calculate comprehensive Stability & Perfection Index (0 - 100%)
        val signalFactor = (netInfo.signalPercent / 100.0).coerceIn(0.1, 1.0)
        val latencyFactor = (1.0 - (cleanLatency / 200.0)).coerceIn(0.1, 1.0)
        val jitterFactor = (1.0 - (jitter / 50.0)).coerceIn(0.1, 1.0)
        val packetLossFactor = (1.0 - (packetLoss / 100.0)).coerceIn(0.0, 1.0)

        val computedStabilityIndex = (
            (signalFactor * 0.35 + latencyFactor * 0.30 + jitterFactor * 0.25 + packetLossFactor * 0.10) * 100.0
        ).coerceIn(15.0, 99.8)

        val stabilityRating = when {
            computedStabilityIndex >= 92.0 -> "PERFECTION (MAX STABILITY)"
            computedStabilityIndex >= 82.0 -> "OPTIMAL CONNECTION"
            computedStabilityIndex >= 68.0 -> "GOOD STABILITY"
            computedStabilityIndex >= 50.0 -> "MODERATE JITTER"
            else -> "UNSTABLE / HIGH ATTENUATION"
        }

        val signalRating = when {
            netInfo.rssiDbm >= -50 -> "PERFECT (-${abs(netInfo.rssiDbm)} dBm)"
            netInfo.rssiDbm >= -65 -> "EXCELLENT (-${abs(netInfo.rssiDbm)} dBm)"
            netInfo.rssiDbm >= -75 -> "GOOD (-${abs(netInfo.rssiDbm)} dBm)"
            netInfo.rssiDbm >= -85 -> "FAIR (-${abs(netInfo.rssiDbm)} dBm)"
            else -> "POOR (-${abs(netInfo.rssiDbm)} dBm)"
        }

        // Theoretical maximum throughput estimation based on protocol & signal
        val maxPotential = when {
            netInfo.type == NetworkType.ETHERNET -> 1000.0
            netInfo.frequencyMhz > 5900 -> 1200.0 * (netInfo.signalPercent / 100.0)
            netInfo.frequencyMhz in 4900..5900 -> 866.0 * (netInfo.signalPercent / 100.0)
            netInfo.frequencyMhz in 2400..2499 -> 150.0 * (netInfo.signalPercent / 100.0)
            netInfo.type == NetworkType.CELLULAR -> 350.0 * (netInfo.signalPercent / 100.0)
            else -> 100.0
        }.coerceAtLeast(25.0)

        // Generate intelligent real-time optimization advice
        val recommendations = mutableListOf<NetworkOptimizationTip>()

        if (netInfo.type == NetworkType.WIFI) {
            if (netInfo.frequencyMhz in 2400..2499) {
                recommendations.add(
                    NetworkOptimizationTip(
                        title = "Upgrade to 5 GHz / 6 GHz Band",
                        description = "Connected to 2.4 GHz. Switching to 5GHz eliminates Bluetooth/microwave channel congestion and triples maximum throughput.",
                        impact = "+300% Speed Ceiling",
                        isWarning = true,
                        iconType = "BAND"
                    )
                )
            } else {
                recommendations.add(
                    NetworkOptimizationTip(
                        title = "High-Bandwidth 5 GHz Channel Active",
                        description = "Broad channel width active with minimal RF interference. Optimal for full gigabit stress testing.",
                        impact = "Maximum Bandwidth",
                        isWarning = false,
                        iconType = "SPEED"
                    )
                )
            }

            if (netInfo.rssiDbm < -72) {
                recommendations.add(
                    NetworkOptimizationTip(
                        title = "Reduce Physical Attenuation",
                        description = "Signal power is currently ${netInfo.rssiDbm} dBm. Move 2-3 meters closer to the Wi-Fi router to boost stability above 95%.",
                        impact = "+45 Mbps Stability",
                        isWarning = true,
                        iconType = "STABILITY"
                    )
                )
            } else {
                recommendations.add(
                    NetworkOptimizationTip(
                        title = "Clean Radio Frequency Signal",
                        description = "Signal strength is excellent (${netInfo.rssiDbm} dBm). Transceiver packet drops are near zero.",
                        impact = "Zero Packet Drop",
                        isWarning = false,
                        iconType = "STABILITY"
                    )
                )
            }
        } else if (netInfo.type == NetworkType.CELLULAR) {
            recommendations.add(
                NetworkOptimizationTip(
                    title = "Cellular Multi-Carrier Aggregation",
                    description = "For peak 5G/LTE performance, avoid physical metal enclosures and enable multi-threaded stress mode.",
                    impact = "Carrier Aggregated",
                    isWarning = false,
                    iconType = "SPEED"
                )
            )
        }

        if (jitter < 5.0 && cleanLatency < 30.0) {
            recommendations.add(
                NetworkOptimizationTip(
                    title = "Ultra-Low Jitter Verified",
                    description = "Latency jitter is only ${String.format("%.1f", jitter)} ms. Connection is in prime state for competitive low-latency gaming & high-speed transfers.",
                    impact = "Sub-5ms Variance",
                    isWarning = false,
                    iconType = "BUFFERBLOAT"
                )
            )
        } else if (jitter > 12.0) {
            recommendations.add(
                NetworkOptimizationTip(
                    title = "Bufferbloat & Jitter Fluctuation",
                    description = "Measured jitter of ${String.format("%.1f", jitter)} ms. Enable QoS on router or pause background downloads to smooth out latency spikes.",
                    impact = "Latency Spikes",
                    isWarning = true,
                    iconType = "BUFFERBLOAT"
                )
            )
        }

        val isReady4K = computedStabilityIndex >= 65.0 && maxPotential >= 35.0
        val isReadyGaming = cleanLatency <= 45.0 && jitter <= 8.0 && packetLoss == 0.0
        val isReadyCloud = netInfo.isConnected && computedStabilityIndex >= 70.0
        val isReadyGigabit = maxPotential >= 300.0 && computedStabilityIndex >= 85.0

        _stabilityAnalysis.value = NetworkStabilityAnalysis(
            signalDbm = netInfo.rssiDbm,
            signalPercent = netInfo.signalPercent,
            signalRating = signalRating,
            stabilityIndexPercent = computedStabilityIndex,
            stabilityRating = stabilityRating,
            currentLatencyMs = cleanLatency,
            avgLatencyMs = avgPing,
            jitterVarianceMs = jitter,
            packetLossPercent = packetLoss,
            bufferbloatGrade = if (cleanLatency < 25 && jitter < 4) "A+" else if (cleanLatency < 50) "A" else "B",
            maxSpeedPotentialMbps = maxPotential,
            channelCongestion = if (netInfo.frequencyMhz > 5000) "Low (Wide 80/160MHz)" else "Moderate (20MHz)",
            frequencyProtocol = "${netInfo.frequencyBand} • ${netInfo.standardProtocol}",
            readiness4kStreaming = isReady4K,
            readinessLowLatencyGaming = isReadyGaming,
            readinessCloudBackup = isReadyCloud,
            readinessGigabitStress = isReadyGigabit,
            recommendations = recommendations,
            signalHistory = signalHistoryBuffer.toList(),
            pingHistory = pingHistoryBuffer.toList(),
            isDeepTesting = _stabilityAnalysis.value.isDeepTesting
        )
    }

    private suspend fun probeLatencyFast(): Double = withContext(Dispatchers.IO) {
        val targets = listOf("1.1.1.1", "8.8.8.8")
        for (host in targets) {
            try {
                val start = System.nanoTime()
                val socket = Socket()
                socket.connect(InetSocketAddress(host, 53), 1200)
                val durationMs = (System.nanoTime() - start) / 1_000_000.0
                socket.close()
                if (durationMs > 0) return@withContext durationMs
            } catch (_: Exception) {}
        }
        25.0
    }

    fun triggerDeepStabilityTest() {
        viewModelScope.launch(Dispatchers.IO) {
            _stabilityAnalysis.value = _stabilityAnalysis.value.copy(isDeepTesting = true)
            val pings = mutableListOf<Double>()
            for (i in 1..6) {
                val ping = probeLatencyFast()
                pings.add(ping)
                delay(200L)
            }
            _stabilityAnalysis.value = _stabilityAnalysis.value.copy(isDeepTesting = false)
            updateNetworkStabilityMetrics()
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
