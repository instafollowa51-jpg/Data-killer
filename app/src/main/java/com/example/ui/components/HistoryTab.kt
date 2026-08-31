package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.StressSessionEntity
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.LaserRose
import com.example.ui.theme.NeonLime
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.QuantumAmber
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMediumEmphasis
import com.example.util.FormatUtils
import com.example.util.SpeedUnit

@Composable
fun HistoryTab(
    sessions: List<StressSessionEntity>,
    totalLifetimeBurned: Long,
    allTimePeakSpeed: Double,
    sessionCount: Int,
    speedUnit: SpeedUnit,
    onDeleteSession: (StressSessionEntity) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Session History?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all saved stress test metrics and lifetime logs.", color = TextMediumEmphasis) },
            containerColor = ObsidianSurfaceElevated,
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LaserRose)
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextMediumEmphasis)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Lifetime Stats Banner
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lifetime_stats_card"),
                accentColor = AndroidGreen
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Lifetime Burned",
                                tint = NeonLime,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LIFETIME DATA CONSUMPTION",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.White
                            )
                        }

                        if (sessions.isNotEmpty()) {
                            IconButton(
                                onClick = { showClearDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear History",
                                    tint = TextMediumEmphasis,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LifetimeStatBox(
                            title = "TOTAL BURNED",
                            value = FormatUtils.formatBytes(totalLifetimeBurned),
                            accentColor = NeonLime,
                            modifier = Modifier.weight(1.3f)
                        )
                        LifetimeStatBox(
                            title = "RECORD PEAK",
                            value = FormatUtils.formatSpeed(allTimePeakSpeed, speedUnit),
                            accentColor = HologramCyan,
                            modifier = Modifier.weight(1.3f)
                        )
                        LifetimeStatBox(
                            title = "RUNS",
                            value = "$sessionCount",
                            accentColor = QuantumAmber,
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                }
            }
        }

        if (sessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No sessions",
                            tint = TextDisabled,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No stress sessions recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMediumEmphasis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Launch a test to record bandwidth metrics",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDisabled
                        )
                    }
                }
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                SessionHistoryCard(
                    session = session,
                    speedUnit = speedUnit,
                    onDelete = { onDeleteSession(session) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LifetimeStatBox(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianDark)
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextMediumEmphasis,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = accentColor,
                fontSize = 13.5.sp
            )
        }
    }
}

@Composable
private fun SessionHistoryCard(
    session: StressSessionEntity,
    speedUnit: SpeedUnit,
    onDelete: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        accentColor = AndroidGreen
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Date & Mode Tag + Delete Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = FormatUtils.formatDate(session.startTimeMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis
                    )
                    Text(
                        text = "${session.networkType} • ${session.networkOperator}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AndroidGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = session.testMode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime,
                            fontSize = 10.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Session",
                            tint = TextDisabled,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianDark)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DATA BURNED",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis,
                        fontSize = 10.sp
                    )
                    Text(
                        text = FormatUtils.formatBytes(session.totalBytesBurned),
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = NeonLime
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PEAK SPEED",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis,
                        fontSize = 10.sp
                    )
                    Text(
                        text = FormatUtils.formatSpeed(session.peakSpeedMbps, speedUnit),
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = HologramCyan
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DURATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMediumEmphasis,
                        fontSize = 10.sp
                    )
                    Text(
                        text = FormatUtils.formatDuration(session.durationSeconds),
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Sub metric tags: Stability, Ping, Jitter
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Stability: ${FormatUtils.formatStabilityScore(session.stabilityScorePercent)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMediumEmphasis,
                    fontSize = 11.sp
                )
                Text(text = "•", color = TextDisabled, fontSize = 11.sp)
                Text(
                    text = "Avg Ping: ${FormatUtils.formatPing(session.avgPingMs)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMediumEmphasis,
                    fontSize = 11.sp
                )
                Text(text = "•", color = TextDisabled, fontSize = 11.sp)
                Text(
                    text = "Loss: ${FormatUtils.formatPacketLoss(session.packetLossPercent)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMediumEmphasis,
                    fontSize = 11.sp
                )
            }
        }
    }
}
