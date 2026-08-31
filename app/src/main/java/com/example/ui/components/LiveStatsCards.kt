package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.CyberMint
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.LaserRose
import com.example.ui.theme.NeonLime
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.QuantumAmber
import com.example.ui.theme.TextMediumEmphasis
import com.example.util.FormatUtils
import com.example.util.SpeedUnit

@Composable
fun MainBurnedHeroCard(
    totalBytesBurned: Long,
    targetLimitBytes: Long,
    elapsedSeconds: Long,
    targetDurationSeconds: Long,
    remainingDurationSeconds: Long,
    avgSpeedMbps: Double,
    speedUnit: SpeedUnit,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val (numStr, unitStr) = FormatUtils.formatBytesNumberOnly(totalBytesBurned)

    // Data quota progress
    val dataProgress = if (targetLimitBytes > 0) {
        (totalBytesBurned.toFloat() / targetLimitBytes.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedDataProgress by animateFloatAsState(
        targetValue = dataProgress,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "data_progress"
    )

    // Duration progress
    val durationProgress = if (targetDurationSeconds > 0) {
        (elapsedSeconds.toFloat() / targetDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedDurationProgress by animateFloatAsState(
        targetValue = durationProgress,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "duration_progress"
    )

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_burned_card"),
        accentColor = AndroidGreen,
        isGlowActive = isRunning,
        glowIntensity = 0.5f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AndroidGreen.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Data Burned",
                            tint = NeonLime,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "TOTAL DATA BURNED",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = Color.White
                    )
                }

                // Elapsed or Countdown Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianDark)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = if (targetDurationSeconds > 0) QuantumAmber else HologramCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (targetDurationSeconds > 0) {
                            "REMAIN: ${FormatUtils.formatDuration(remainingDurationSeconds)}"
                        } else {
                            FormatUtils.formatDuration(elapsedSeconds)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Numbers
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = numStr,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = NeonLime,
                    lineHeight = 48.sp,
                    modifier = Modifier.testTag("hero_burned_value")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = unitStr,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AndroidGreen,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Target Quota Progress Bar
            if (targetLimitBytes > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target Quota: ${FormatUtils.formatBytes(targetLimitBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis
                        )
                        Text(
                            text = "${(dataProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { animatedDataProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NeonLime,
                        trackColor = ObsidianDark
                    )
                }
            }

            // Target Duration Progress Bar
            if (targetDurationSeconds > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target Duration: ${FormatUtils.formatDuration(targetDurationSeconds)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis
                        )
                        Text(
                            text = "${(durationProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = QuantumAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { animatedDurationProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = QuantumAmber,
                        trackColor = ObsidianDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub info footer: Average Speed & Burn Pace
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianDark.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AVERAGE THROUGHPUT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMediumEmphasis,
                    fontSize = 11.sp
                )
                Text(
                    text = FormatUtils.formatSpeed(avgSpeedMbps, speedUnit),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = HologramCyan
                )
            }
        }
    }
}

@Composable
fun LiveMetricsGrid(
    downSpeedMbps: Double,
    upSpeedMbps: Double,
    pingMs: Double,
    jitterMs: Double,
    packetLossPercent: Double,
    stabilityScorePercent: Double,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Download & Upload Speeds
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                icon = Icons.Default.ArrowDownward,
                iconColor = HologramCyan,
                title = "DOWNLOAD",
                value = FormatUtils.formatSpeed(downSpeedMbps, speedUnit),
                modifier = Modifier.weight(1f),
                testTag = "metric_download"
            )
            MetricCard(
                icon = Icons.Default.ArrowUpward,
                iconColor = AndroidGreen,
                title = "UPLOAD",
                value = FormatUtils.formatSpeed(upSpeedMbps, speedUnit),
                modifier = Modifier.weight(1f),
                testTag = "metric_upload"
            )
        }

        // Row 2: Ping & Jitter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                icon = Icons.Default.NetworkPing,
                iconColor = QuantumAmber,
                title = "LATENCY PING",
                value = FormatUtils.formatPing(pingMs),
                modifier = Modifier.weight(1f),
                testTag = "metric_ping"
            )
            MetricCard(
                icon = Icons.Default.Waves,
                iconColor = CyberMint,
                title = "JITTER",
                value = "${FormatUtils.formatPing(jitterMs)}",
                modifier = Modifier.weight(1f),
                testTag = "metric_jitter"
            )
        }

        // Row 3: Packet Loss & Stability Score
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                icon = Icons.Default.Security,
                iconColor = if (packetLossPercent > 3.0) LaserRose else NeonLime,
                title = "PACKET LOSS",
                value = FormatUtils.formatPacketLoss(packetLossPercent),
                subtitle = if (packetLossPercent < 1.0) "Optimal" else "Degraded",
                modifier = Modifier.weight(1f),
                testTag = "metric_packet_loss"
            )
            MetricCard(
                icon = Icons.Default.Speed,
                iconColor = if (stabilityScorePercent >= 80.0) NeonLime else QuantumAmber,
                title = "SPEED STABILITY",
                value = FormatUtils.formatStabilityScore(stabilityScorePercent),
                subtitle = if (stabilityScorePercent >= 80.0) "Rock Solid" else "Variable",
                modifier = Modifier.weight(1f),
                testTag = "metric_stability"
            )
        }
    }
}

@Composable
fun MetricCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    LiquidGlassCard(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        accentColor = iconColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMediumEmphasis,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 16.sp
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = iconColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
