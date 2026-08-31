package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stress_sessions")
data class StressSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationSeconds: Long,
    val totalBytesDownloaded: Long,
    val totalBytesUploaded: Long,
    val peakSpeedMbps: Double,
    val avgSpeedMbps: Double,
    val avgPingMs: Double,
    val minPingMs: Double,
    val maxPingMs: Double,
    val jitterMs: Double,
    val packetLossPercent: Double = 0.0,
    val stabilityScorePercent: Double = 100.0,
    val networkType: String,
    val networkOperator: String,
    val testMode: String,
    val networkRoutingMode: String = "AUTO_ALL",
    val threadCount: Int,
    val targetLimitBytes: Long = 0L,
    val targetDurationSeconds: Long = 0L,
    val completedByLimit: Boolean = false,
    val completionReason: String = "MANUAL_STOP"
) {
    val totalBytesBurned: Long
        get() = totalBytesDownloaded + totalBytesUploaded
}

data class LifetimeStats(
    val totalSessions: Int,
    val totalBytesBurned: Long,
    val totalDurationSeconds: Long,
    val allTimePeakSpeedMbps: Double
)
