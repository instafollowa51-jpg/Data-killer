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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.network.NetworkInfoState
import com.example.engine.EngineStats
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.CyberMint
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.LaserRose
import com.example.ui.theme.NeonLime
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.QuantumAmber
import com.example.ui.theme.TextMediumEmphasis
import com.example.util.FormatUtils
import com.example.util.SpeedUnit

@Composable
fun TelemetryTab(
    stats: EngineStats,
    networkInfo: NetworkInfoState,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    val rating = FormatUtils.getConnectionRating(
        peakMbps = stats.peakTotalSpeedMbps,
        stabilityScore = stats.stabilityScorePercent,
        packetLoss = stats.packetLossPercent
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Connection Quality Rating Hero Banner
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("telemetry_rating_card"),
            accentColor = AndroidGreen,
            isGlowActive = stats.isRunning
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Big Grade Badge (e.g. A+)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AndroidGreen, NeonLime)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rating.grade,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = rating.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = rating.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rating: ${rating.stabilityRating}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime
                    )
                }
            }
        }

        // 2. Stability & Packet Loss Diagnostics Card
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentColor = HologramCyan
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Stability",
                        tint = HologramCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONNECTION STABILITY & INTEGRITY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DiagMetricBox(
                        title = "SPEED CONSISTENCY",
                        value = FormatUtils.formatStabilityScore(stats.stabilityScorePercent),
                        subtitle = if (stats.stabilityScorePercent >= 85.0) "Uniform Flow" else "Fluctuating",
                        accentColor = NeonLime,
                        modifier = Modifier.weight(1f)
                    )
                    DiagMetricBox(
                        title = "PACKET LOSS",
                        value = FormatUtils.formatPacketLoss(stats.packetLossPercent),
                        subtitle = if (stats.packetLossPercent < 1.0) "Clean Stream" else "Degraded",
                        accentColor = if (stats.packetLossPercent > 3.0) LaserRose else HologramCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Detailed Latency & Jitter Matrix Card
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentColor = QuantumAmber
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NetworkPing,
                        contentDescription = "Ping Matrix",
                        tint = QuantumAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LATENCY & BUFFERBLOAT MATRIX",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DiagMetricBox(
                            title = "CURRENT PING",
                            value = FormatUtils.formatPing(stats.currentPingMs),
                            accentColor = QuantumAmber,
                            modifier = Modifier.weight(1f)
                        )
                        DiagMetricBox(
                            title = "AVG PING",
                            value = FormatUtils.formatPing(stats.avgPingMs),
                            accentColor = QuantumAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DiagMetricBox(
                            title = "MIN PING (BEST)",
                            value = FormatUtils.formatPing(stats.minPingMs),
                            accentColor = NeonLime,
                            modifier = Modifier.weight(1f)
                        )
                        DiagMetricBox(
                            title = "MAX PING (WORST)",
                            value = FormatUtils.formatPing(stats.maxPingMs),
                            accentColor = LaserRose,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DiagMetricBox(
                            title = "JITTER DEVIATION",
                            value = FormatUtils.formatPing(stats.jitterMs),
                            subtitle = if (stats.jitterMs < 5.0) "Minimal Jitter" else "High Variance",
                            accentColor = CyberMint,
                            modifier = Modifier.weight(1f)
                        )
                        DiagMetricBox(
                            title = "LOADED LATENCY Δ",
                            value = "+${FormatUtils.formatPing(stats.loadedLatencyDeltaMs)}",
                            subtitle = "Bufferbloat under load",
                            accentColor = QuantumAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Physical Network Hardware & Routing Info
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentColor = AndroidGreen
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lan,
                        contentDescription = "Hardware",
                        tint = AndroidGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACTIVE INTERFACE & ROUTING TELEMETRY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianDark)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryRow("Network Provider / SSID", networkInfo.operatorOrSsid)
                    TelemetryRow("Interface Type", networkInfo.typeName)
                    if (networkInfo.frequencyBand.isNotEmpty()) {
                        TelemetryRow("Radio Frequency", "${networkInfo.frequencyMhz} MHz (${networkInfo.frequencyBand})")
                    }
                    TelemetryRow("Theoretical Link Speed", "${networkInfo.linkSpeedMbps} Mbps")
                    TelemetryRow("Routing Mode", stats.routingMode.label)
                    TelemetryRow("Local Device IP", networkInfo.localIpAddress)
                    TelemetryRow("Active Parallel Streams", "${stats.activeThreads} Coroutine Pipelines")
                }
            }
        }
    }
}

@Composable
private fun DiagMetricBox(
    title: String,
    value: String,
    subtitle: String? = null,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianDark)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextMediumEmphasis,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = accentColor,
                fontSize = 16.sp
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMediumEmphasis,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextMediumEmphasis,
            fontSize = 11.5.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 11.5.sp
        )
    }
}
