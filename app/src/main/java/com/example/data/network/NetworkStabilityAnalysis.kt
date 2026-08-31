package com.example.data.network

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.CyberMint
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.LaserRose
import com.example.ui.theme.NeonLime
import com.example.ui.theme.QuantumAmber

/**
 * Real-time comprehensive connection stability and signal metrics
 */
data class NetworkStabilityAnalysis(
    val signalDbm: Int = -55,
    val signalPercent: Int = 88,
    val signalRating: String = "EXCELLENT",
    val stabilityIndexPercent: Double = 94.5,
    val stabilityRating: String = "OPTIMAL STABILITY",
    val currentLatencyMs: Double = 18.0,
    val avgLatencyMs: Double = 21.5,
    val jitterVarianceMs: Double = 2.1,
    val packetLossPercent: Double = 0.0,
    val bufferbloatGrade: String = "A+",
    val maxSpeedPotentialMbps: Double = 850.0,
    val channelCongestion: String = "Low (Clear Airtime)",
    val frequencyProtocol: String = "5 GHz (Wi-Fi 6 / 802.11ax)",
    val readiness4kStreaming: Boolean = true,
    val readinessLowLatencyGaming: Boolean = true,
    val readinessCloudBackup: Boolean = true,
    val readinessGigabitStress: Boolean = true,
    val recommendations: List<NetworkOptimizationTip> = emptyList(),
    val signalHistory: List<Float> = emptyList(),
    val pingHistory: List<Float> = emptyList(),
    val isDeepTesting: Boolean = false
) {
    val ratingColor: Color
        get() = when {
            stabilityIndexPercent >= 90.0 -> NeonLime
            stabilityIndexPercent >= 75.0 -> AndroidGreen
            stabilityIndexPercent >= 60.0 -> QuantumAmber
            else -> LaserRose
        }

    val signalColor: Color
        get() = when {
            signalPercent >= 80 -> NeonLime
            signalPercent >= 60 -> CyberMint
            signalPercent >= 40 -> QuantumAmber
            else -> LaserRose
        }
}

data class NetworkOptimizationTip(
    val title: String,
    val description: String,
    val impact: String, // e.g. "+40% Speed", "Ultra Low Jitter", "Optimal"
    val isWarning: Boolean = false,
    val iconType: String = "SPEED" // "SPEED", "STABILITY", "BAND", "BUFFERBLOAT"
)
