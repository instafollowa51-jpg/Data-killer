package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.network.NetworkInfoState
import com.example.data.network.NetworkType
import com.example.engine.DataUsagePoint
import com.example.engine.EngineStats
import com.example.ui.components.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NetworkRoutingMode
import com.example.util.SpeedUnit
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        DashboardScreen(
          stats = EngineStats(
            isRunning = true,
            currentTotalSpeedMbps = 345.8,
            peakTotalSpeedMbps = 520.4,
            currentDownloadSpeedMbps = 310.2,
            currentUploadSpeedMbps = 35.6,
            totalBytesBurned = 3_450_000_000L,
            totalBytesDownloaded = 3_100_000_000L,
            totalBytesUploaded = 350_000_000L,
            currentPingMs = 18.0,
            jitterMs = 2.4,
            packetLossPercent = 0.0,
            stabilityScorePercent = 94.5,
            elapsedSeconds = 125L,
            speedHistory = listOf(120f, 180f, 290f, 345f, 410f, 520f, 480f, 345.8f),
            dataUsageHistory = listOf(
              DataUsagePoint(10L, 500_000_000L, 450_000_000L, 50_000_000L, 120.0),
              DataUsagePoint(30L, 1_200_000_000L, 1_100_000_000L, 100_000_000L, 240.0),
              DataUsagePoint(60L, 2_100_000_000L, 1_900_000_000L, 200_000_000L, 310.0),
              DataUsagePoint(125L, 3_450_000_000L, 3_100_000_000L, 350_000_000L, 345.8)
            )
          ),
          networkInfo = NetworkInfoState(
            isConnected = true,
            isWifiAvailable = true,
            isCellularAvailable = true,
            type = NetworkType.WIFI,
            typeName = "Wi-Fi",
            operatorOrSsid = "HyperFiber_5G",
            frequencyMhz = 5240,
            linkSpeedMbps = 866
          ),
          routingMode = NetworkRoutingMode.AUTO_ALL,
          targetLimitBytes = 5L * 1024 * 1024 * 1024,
          targetDurationSeconds = 300L,
          speedUnit = SpeedUnit.MBPS,
          onStart = {},
          onPause = {},
          onResume = {},
          onStop = {},
          onOpenSettings = {},
          onSetQuickPreset = { _, _ -> }
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
