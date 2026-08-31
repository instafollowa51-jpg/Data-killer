package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SpeedUnit(val label: String) {
    MBPS("Mbps"),
    MB_PER_SEC("MB/s"),
    GBPS("Gbps")
}

enum class NetworkRoutingMode(val label: String, val description: String) {
    AUTO_ALL("Auto / Multi-Path", "Uses system default or combined network routing"),
    WIFI_ONLY("Wi-Fi Only", "Forces all stress traffic exclusively through Wi-Fi"),
    CELLULAR_ONLY("Mobile Data Only", "Forces stress traffic through Cellular LTE/5G network")
}

data class ConnectionRating(
    val grade: String,
    val title: String,
    val description: String,
    val stabilityRating: String
)

object FormatUtils {
    private val df2 = DecimalFormat("#,##0.00")
    private val df1 = DecimalFormat("#,##0.0")
    private val df0 = DecimalFormat("#,##0")

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "${df2.format(kb)} KB"
        val mb = kb / 1024.0
        if (mb < 1024) return "${df2.format(mb)} MB"
        val gb = mb / 1024.0
        if (gb < 1024) return "${df2.format(gb)} GB"
        val tb = gb / 1024.0
        return "${df2.format(tb)} TB"
    }

    fun formatBytesToMbOrGb(bytes: Long): Pair<Double, String> {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return if (gb >= 1.0) {
            Pair(gb, "GB")
        } else {
            val mb = bytes / (1024.0 * 1024.0)
            Pair(mb, "MB")
        }
    }

    fun formatBytesNumberOnly(bytes: Long): Pair<String, String> {
        if (bytes < 1024) return Pair(bytes.toString(), "B")
        val kb = bytes / 1024.0
        if (kb < 1024) return Pair(df2.format(kb), "KB")
        val mb = kb / 1024.0
        if (mb < 1024) return Pair(df2.format(mb), "MB")
        val gb = mb / 1024.0
        if (gb < 1024) return Pair(df2.format(gb), "GB")
        val tb = gb / 1024.0
        return Pair(df2.format(tb), "TB")
    }

    fun formatSpeed(speedMbps: Double, unit: SpeedUnit = SpeedUnit.MBPS): String {
        return when (unit) {
            SpeedUnit.MBPS -> "${df1.format(speedMbps)} Mbps"
            SpeedUnit.MB_PER_SEC -> "${df1.format(speedMbps / 8.0)} MB/s"
            SpeedUnit.GBPS -> "${df2.format(speedMbps / 1000.0)} Gbps"
        }
    }

    fun formatSpeedValue(speedMbps: Double, unit: SpeedUnit = SpeedUnit.MBPS): String {
        return when (unit) {
            SpeedUnit.MBPS -> df1.format(speedMbps)
            SpeedUnit.MB_PER_SEC -> df1.format(speedMbps / 8.0)
            SpeedUnit.GBPS -> df2.format(speedMbps / 1000.0)
        }
    }

    fun formatPing(ms: Double): String {
        if (ms <= 0) return "-- ms"
        return "${df0.format(ms)} ms"
    }

    fun formatPacketLoss(lossPercent: Double): String {
        return "${df1.format(lossPercent)}%"
    }

    fun formatStabilityScore(scorePercent: Double): String {
        val clamped = scorePercent.coerceIn(0.0, 100.0)
        return "${df1.format(clamped)}%"
    }

    fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    fun getConnectionRating(
        peakMbps: Double,
        stabilityScore: Double,
        packetLoss: Double
    ): ConnectionRating {
        val grade = when {
            peakMbps >= 500.0 && stabilityScore >= 85.0 && packetLoss < 1.0 -> "A+"
            peakMbps >= 200.0 && stabilityScore >= 75.0 && packetLoss < 2.0 -> "A"
            peakMbps >= 75.0 && stabilityScore >= 65.0 && packetLoss < 4.0 -> "B"
            peakMbps >= 25.0 -> "C"
            else -> "D"
        }

        val (title, desc) = when {
            peakMbps >= 1000.0 -> Pair("GIGABIT TITAN", "Ultra-low latency multi-gigabit throughput performance.")
            peakMbps >= 500.0 -> Pair("HYPER BROADBAND", "Exceptional high-capacity pipeline with low jitter.")
            peakMbps >= 200.0 -> Pair("ULTRA BROADBAND", "Flawless multi-stream data saturation capability.")
            peakMbps >= 100.0 -> Pair("HIGH PERFORMANCE", "Robust and fast connection suitable for continuous load.")
            peakMbps >= 40.0 -> Pair("SOLID BROADBAND", "Reliable 4G/5G cellular or standard Wi-Fi link.")
            else -> Pair("CONSTRAINED", "Limited throughput bandwidth or high bottlenecking.")
        }

        val stabilityRating = when {
            stabilityScore >= 90.0 -> "Flawless Stability"
            stabilityScore >= 75.0 -> "High Consistency"
            stabilityScore >= 50.0 -> "Moderate Jitter"
            else -> "Fluctuating Throughput"
        }

        return ConnectionRating(grade, title, desc, stabilityRating)
    }
}
