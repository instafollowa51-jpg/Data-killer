package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.FormatUtils
import com.example.util.SpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DATA KILLER", appName)
  }

  @Test
  fun `format bytes formatting test`() {
    assertEquals("500 B", FormatUtils.formatBytes(500L))
    assertEquals("1.00 MB", FormatUtils.formatBytes(1024L * 1024L))
    assertEquals("2.50 GB", FormatUtils.formatBytes((2.5 * 1024L * 1024L * 1024L).toLong()))
  }

  @Test
  fun `speed formatting test`() {
    assertEquals("150.0 Mbps", FormatUtils.formatSpeed(150.0, SpeedUnit.MBPS))
    assertEquals("18.8 MB/s", FormatUtils.formatSpeed(150.0, SpeedUnit.MB_PER_SEC))
  }

  @Test
  fun `duration formatting test`() {
    assertEquals("01:30", FormatUtils.formatDuration(90L))
    assertEquals("01:00:00", FormatUtils.formatDuration(3600L))
  }

  @Test
  fun `room database initialization and insert test`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.db.DataKillerDatabase.getDatabase(context)
    val dao = db.stressSessionDao()

    val session = com.example.data.db.StressSessionEntity(
      startTimeMillis = System.currentTimeMillis() - 60000,
      endTimeMillis = System.currentTimeMillis(),
      durationSeconds = 60L,
      totalBytesDownloaded = 500_000_000L,
      totalBytesUploaded = 50_000_000L,
      peakSpeedMbps = 350.0,
      avgSpeedMbps = 280.0,
      avgPingMs = 15.0,
      minPingMs = 10.0,
      maxPingMs = 25.0,
      jitterMs = 2.0,
      packetLossPercent = 0.0,
      stabilityScorePercent = 95.0,
      networkType = "Wi-Fi",
      networkOperator = "HyperFiber",
      testMode = "DOWNLOAD",
      threadCount = 8
    )

    val id = dao.insertSession(session)
    assertTrue(id > 0)

    val retrieved = dao.getSessionById(id)
    org.junit.Assert.assertNotNull(retrieved)
    assertEquals(550_000_000L, retrieved?.totalBytesBurned)
  }
}
