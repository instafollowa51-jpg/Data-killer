package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DataKillerViewModel
import com.example.ui.components.AmbientLiquidMeshBackground
import com.example.ui.components.DashboardScreen
import com.example.ui.components.HistoryTab
import com.example.ui.components.LocalLiquidGlassConfig
import com.example.ui.components.SessionSummaryDialog
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.components.TelemetryTab
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonLime
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMediumEmphasis

class MainActivity : ComponentActivity() {

    private val viewModel: DataKillerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val stats by viewModel.engineStats.collectAsStateWithLifecycle()
                val networkInfo by viewModel.networkInfo.collectAsStateWithLifecycle()
                val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
                val totalLifetimeBurned by viewModel.totalLifetimeBurned.collectAsStateWithLifecycle()
                val allTimePeakSpeed by viewModel.allTimePeakSpeed.collectAsStateWithLifecycle()
                val sessionCount by viewModel.sessionCount.collectAsStateWithLifecycle()

                val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
                val routingMode by viewModel.routingMode.collectAsStateWithLifecycle()
                val concurrencyThreads by viewModel.concurrencyThreads.collectAsStateWithLifecycle()
                val targetLimitBytes by viewModel.targetLimitBytes.collectAsStateWithLifecycle()
                val targetDurationSeconds by viewModel.targetDurationSeconds.collectAsStateWithLifecycle()
                val speedUnit by viewModel.speedUnit.collectAsStateWithLifecycle()
                val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
                val liquidGlassConfig by viewModel.liquidGlassConfig.collectAsStateWithLifecycle()
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val completedSession by viewModel.completedSessionResult.collectAsStateWithLifecycle()

                var showSettingsSheet by remember { mutableStateOf(false) }

                // Manage Keep Screen On flag
                LaunchedEffect(keepScreenOn, stats.isRunning) {
                    if (keepScreenOn && stats.isRunning) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else if (!keepScreenOn) {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                // Request Notification Permission on Android 13+
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { }
                )
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                CompositionLocalProvider(LocalLiquidGlassConfig provides liquidGlassConfig) {
                    AmbientLiquidMeshBackground(isRunning = stats.isRunning) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = Color.Transparent,
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Whatshot,
                                                contentDescription = null,
                                                tint = NeonLime,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "DATA KILLER",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 19.sp,
                                                letterSpacing = 1.4.sp,
                                                color = Color.White
                                            )
                                        }
                                    },
                                    actions = {
                                        IconButton(
                                            onClick = { showSettingsSheet = true },
                                            modifier = Modifier.testTag("open_settings_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = "Test Customization",
                                                tint = AndroidGreen
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color.Transparent
                                    )
                                )
                            },
                            bottomBar = {
                                NavigationBar(
                                    containerColor = ObsidianSurfaceElevated.copy(alpha = 0.92f),
                                    modifier = Modifier
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .testTag("main_navigation_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == 0,
                                        onClick = { viewModel.setCurrentTab(0) },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == 0) Icons.Filled.Speed else Icons.Outlined.Speed,
                                                contentDescription = "Dashboard"
                                            )
                                        },
                                        label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = NeonLime,
                                            selectedTextColor = NeonLime,
                                            unselectedIconColor = TextMediumEmphasis,
                                            unselectedTextColor = TextMediumEmphasis,
                                            indicatorColor = AndroidGreen.copy(alpha = 0.22f)
                                        ),
                                        modifier = Modifier.testTag("nav_item_dashboard")
                                    )

                                    NavigationBarItem(
                                        selected = currentTab == 1,
                                        onClick = { viewModel.setCurrentTab(1) },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == 1) Icons.Filled.NetworkCheck else Icons.Outlined.NetworkCheck,
                                                contentDescription = "Telemetry"
                                            )
                                        },
                                        label = { Text("Telemetry", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = HologramCyan,
                                            selectedTextColor = HologramCyan,
                                            unselectedIconColor = TextMediumEmphasis,
                                            unselectedTextColor = TextMediumEmphasis,
                                            indicatorColor = HologramCyan.copy(alpha = 0.22f)
                                        ),
                                        modifier = Modifier.testTag("nav_item_telemetry")
                                    )

                                    NavigationBarItem(
                                        selected = currentTab == 2,
                                        onClick = { viewModel.setCurrentTab(2) },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == 2) Icons.Filled.History else Icons.Outlined.History,
                                                contentDescription = "History"
                                            )
                                        },
                                        label = { Text("History", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = AndroidGreen,
                                            selectedTextColor = AndroidGreen,
                                            unselectedIconColor = TextMediumEmphasis,
                                            unselectedTextColor = TextMediumEmphasis,
                                            indicatorColor = AndroidGreen.copy(alpha = 0.22f)
                                        ),
                                        modifier = Modifier.testTag("nav_item_history")
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                AnimatedContent(
                                    targetState = currentTab,
                                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                                    label = "tab_transition"
                                ) { tab ->
                                    when (tab) {
                                        0 -> DashboardScreen(
                                            stats = stats,
                                            networkInfo = networkInfo,
                                            routingMode = routingMode,
                                            targetLimitBytes = targetLimitBytes,
                                            targetDurationSeconds = targetDurationSeconds,
                                            speedUnit = speedUnit,
                                            onStart = { viewModel.startStressTest() },
                                            onPause = { viewModel.pauseStressTest() },
                                            onResume = { viewModel.resumeStressTest() },
                                            onStop = { viewModel.stopStressTest() },
                                            onOpenSettings = { showSettingsSheet = true },
                                            onSetQuickPreset = { bytes, seconds ->
                                                viewModel.setTargetLimitBytes(bytes)
                                                viewModel.setTargetDurationSeconds(seconds)
                                            }
                                        )
                                        1 -> TelemetryTab(
                                            stats = stats,
                                            networkInfo = networkInfo,
                                            speedUnit = speedUnit
                                        )
                                        2 -> HistoryTab(
                                            sessions = allSessions,
                                            totalLifetimeBurned = totalLifetimeBurned ?: 0L,
                                            allTimePeakSpeed = allTimePeakSpeed ?: 0.0,
                                            sessionCount = sessionCount,
                                            speedUnit = speedUnit,
                                            onDeleteSession = { session -> viewModel.deleteSession(session) },
                                            onClearHistory = { viewModel.clearHistory() }
                                        )
                                    }
                                }
                            }

                            // Settings Bottom Sheet with iOS Liquid Glass Customizer
                            if (showSettingsSheet) {
                                SettingsBottomSheet(
                                    selectedMode = selectedMode,
                                    routingMode = routingMode,
                                    concurrencyThreads = concurrencyThreads,
                                    targetLimitBytes = targetLimitBytes,
                                    targetDurationSeconds = targetDurationSeconds,
                                    speedUnit = speedUnit,
                                    keepScreenOn = keepScreenOn,
                                    isWifiAvailable = networkInfo.isWifiAvailable,
                                    isCellularAvailable = networkInfo.isCellularAvailable,
                                    liquidGlassConfig = liquidGlassConfig,
                                    onModeSelected = { viewModel.setSelectedMode(it) },
                                    onRoutingModeSelected = { viewModel.setRoutingMode(it) },
                                    onConcurrencyChanged = { viewModel.setConcurrencyThreads(it) },
                                    onTargetLimitBytesChanged = { viewModel.setTargetLimitBytes(it) },
                                    onTargetDurationSecondsChanged = { viewModel.setTargetDurationSeconds(it) },
                                    onSpeedUnitChanged = { viewModel.setSpeedUnit(it) },
                                    onKeepScreenOnChanged = { viewModel.setKeepScreenOn(it) },
                                    onBlurRadiusChanged = { viewModel.setBlurRadius(it) },
                                    onVibrancyChanged = { viewModel.setVibrancy(it) },
                                    onLensRefractionAmountChanged = { viewModel.setLensRefractionAmount(it) },
                                    onLensRefractionHeightChanged = { viewModel.setLensRefractionHeight(it) },
                                    onDepthEffectChanged = { viewModel.setDepthEffect(it) },
                                    onPresetSelected = { viewModel.setLiquidGlassPreset(it) },
                                    onResetToStandardIos = { viewModel.resetToStandardIosGlass() },
                                    onDismiss = { showSettingsSheet = false }
                                )
                            }

                            // Session Summary Report Dialog
                            completedSession?.let { session ->
                                SessionSummaryDialog(
                                    session = session,
                                    speedUnit = speedUnit,
                                    onDismiss = { viewModel.dismissSessionSummary() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
