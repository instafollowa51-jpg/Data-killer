package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.NetworkInfoState
import com.example.engine.EngineStats
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.CyberMint
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.QuantumAmber
import com.example.ui.theme.TextMediumEmphasis
import com.example.util.NetworkRoutingMode
import com.example.util.SpeedUnit

@Composable
fun DashboardScreen(
    stats: EngineStats,
    networkInfo: NetworkInfoState,
    routingMode: NetworkRoutingMode,
    targetLimitBytes: Long,
    targetDurationSeconds: Long,
    speedUnit: SpeedUnit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onOpenSettings: () -> Unit,
    onSetQuickPreset: (targetBytes: Long, targetSeconds: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // 1. Network Status Badge
            NetworkStatusBadge(
                networkInfo = networkInfo,
                routingMode = routingMode
            )
        }

        item {
            // 2. Speedometer Gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                SpeedometerGauge(
                    currentSpeedMbps = stats.currentTotalSpeedMbps,
                    peakSpeedMbps = stats.peakTotalSpeedMbps,
                    isRunning = stats.isRunning,
                    speedUnit = speedUnit
                )
            }
        }

        item {
            // 3. Main Action Controls
            ControlActionButtons(
                isRunning = stats.isRunning,
                isPaused = stats.isPaused,
                onStart = onStart,
                onPause = onPause,
                onResume = onResume,
                onStop = onStop,
                onOpenSettings = onOpenSettings
            )
        }

        if (!stats.isRunning) {
            item {
                // 4. Quick Test Presets
                QuickPresetsRow(
                    targetLimitBytes = targetLimitBytes,
                    targetDurationSeconds = targetDurationSeconds,
                    onSetQuickPreset = onSetQuickPreset,
                    onOpenSettings = onOpenSettings
                )
            }
        }

        item {
            // 5. Total Data Burned Hero Card
            MainBurnedHeroCard(
                totalBytesBurned = stats.totalBytesBurned,
                targetLimitBytes = stats.targetLimitBytes,
                elapsedSeconds = stats.elapsedSeconds,
                targetDurationSeconds = stats.targetDurationSeconds,
                remainingDurationSeconds = stats.remainingDurationSeconds,
                avgSpeedMbps = stats.avgSpeedMbps,
                speedUnit = speedUnit,
                isRunning = stats.isRunning
            )
        }

        item {
            // 6. Real-Time Data Usage Monitor Graph & Bandwidth Waveform
            LiveSpeedGraph(
                speedHistory = stats.speedHistory,
                dataUsageHistory = stats.dataUsageHistory,
                currentSpeedMbps = stats.currentTotalSpeedMbps,
                totalBytesBurned = stats.totalBytesBurned,
                totalBytesDownloaded = stats.totalBytesDownloaded,
                totalBytesUploaded = stats.totalBytesUploaded,
                elapsedSeconds = stats.elapsedSeconds,
                speedUnit = speedUnit,
                isRunning = stats.isRunning
            )
        }

        item {
            // 7. Live Connection Metrics Grid (Down, Up, Ping, Jitter, Loss, Stability)
            LiveMetricsGrid(
                downSpeedMbps = stats.currentDownloadSpeedMbps,
                upSpeedMbps = stats.currentUploadSpeedMbps,
                pingMs = stats.currentPingMs,
                jitterMs = stats.jitterMs,
                packetLossPercent = stats.packetLossPercent,
                stabilityScorePercent = stats.stabilityScorePercent,
                speedUnit = speedUnit
            )
        }

        item {
            // 8. iOS Liquid Glass Material Badge & Customizer CTA
            val glassConfig = LocalLiquidGlassConfig.current
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSettings),
                accentColor = HologramCyan
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(HologramCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Glass Optics",
                                tint = HologramCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "iOS Liquid Glass: ${glassConfig.presetName}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${glassConfig.blurRadiusDp.toInt()}dp Blur • ${(glassConfig.vibrancy * 100).toInt()}% Vibrancy • ${(glassConfig.lensRefractionAmount * 100).toInt()}% Refraction • ${glassConfig.depthEffectDp.toInt()}dp Depth",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMediumEmphasis,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        text = "Customize",
                        style = MaterialTheme.typography.labelSmall,
                        color = HologramCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickPresetsRow(
    targetLimitBytes: Long,
    targetDurationSeconds: Long,
    onSetQuickPreset: (targetBytes: Long, targetSeconds: Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUICK TEST PRESETS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMediumEmphasis
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenSettings)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Customize",
                    tint = AndroidGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Custom",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AndroidGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetChip(
                icon = Icons.Default.LocalFireDepartment,
                title = "Unlimited",
                subtitle = "Infinite Max",
                selected = targetLimitBytes == 0L && targetDurationSeconds == 0L,
                color = NeonLime,
                modifier = Modifier.weight(1f),
                onClick = { onSetQuickPreset(0L, 0L) }
            )
            PresetChip(
                icon = Icons.Default.Timer,
                title = "1-Min Burn",
                subtitle = "60s Test",
                selected = targetDurationSeconds == 60L && targetLimitBytes == 0L,
                color = QuantumAmber,
                modifier = Modifier.weight(1f),
                onClick = { onSetQuickPreset(0L, 60L) }
            )
            PresetChip(
                icon = Icons.Default.DataUsage,
                title = "1 GB Quota",
                subtitle = "1024 MB",
                selected = targetLimitBytes == 1L * 1024 * 1024 * 1024,
                color = HologramCyan,
                modifier = Modifier.weight(1f),
                onClick = { onSetQuickPreset(1L * 1024 * 1024 * 1024, 0L) }
            )
            PresetChip(
                icon = Icons.Default.Bolt,
                title = "5 GB Quota",
                subtitle = "Heavy Burn",
                selected = targetLimitBytes == 5L * 1024 * 1024 * 1024,
                color = AndroidGreen,
                modifier = Modifier.weight(1f),
                onClick = { onSetQuickPreset(5L * 1024 * 1024 * 1024, 0L) }
            )
        }
    }
}

@Composable
private fun PresetChip(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.90f
            selected -> 1.03f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "chip_scale"
    )

    LiquidGlassCard(
        modifier = modifier
            .scale(animatedScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        accentColor = if (selected) color else Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (selected) color.copy(alpha = 0.18f) else Color.Transparent)
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) color else TextMediumEmphasis,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else TextMediumEmphasis,
                fontSize = 10.5.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) color else TextMediumEmphasis.copy(alpha = 0.7f),
                fontSize = 9.sp
            )
        }
    }
}
