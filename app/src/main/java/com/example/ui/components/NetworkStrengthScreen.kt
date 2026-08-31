package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.NetworkInfoState
import com.example.data.network.NetworkOptimizationTip
import com.example.data.network.NetworkStabilityAnalysis
import com.example.data.network.NetworkType
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.CyberMint
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.LaserRose
import com.example.ui.theme.NeonLime
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.QuantumAmber
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMediumEmphasis
import com.example.util.FormatUtils

@Composable
fun NetworkStrengthScreen(
    analysis: NetworkStabilityAnalysis,
    networkInfo: NetworkInfoState,
    onTriggerDeepTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO CONNECTION PERFECTION & STABILITY GAUGE
        PerfectionHeroGaugeCard(
            analysis = analysis,
            networkInfo = networkInfo,
            onTriggerDeepTest = onTriggerDeepTest
        )

        // 2. SIGNAL STRENGTH & HARDWARE RF CLARITY CARD
        SignalClarityCard(
            analysis = analysis,
            networkInfo = networkInfo
        )

        // 3. LIVE STABILITY & JITTER WAVEFORM OSCILLOSCOPE
        LiveStabilityOscilloscopeCard(
            analysis = analysis
        )

        // 4. MAX PERFORMANCE & ACTIVITY READINESS MATRIX
        ActivityReadinessCard(
            analysis = analysis
        )

        // 5. MAXIMUM STABILITY & SPEED OPTIMIZATION ADVISOR
        OptimizationAdvisorCard(
            tips = analysis.recommendations
        )

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun PerfectionHeroGaugeCard(
    analysis: NetworkStabilityAnalysis,
    networkInfo: NetworkInfoState,
    onTriggerDeepTest: () -> Unit
) {
    val animatedScore by animateFloatAsState(
        targetValue = analysis.stabilityIndexPercent.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "stability_gauge_score"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val deepTestInteraction = remember { MutableInteractionSource() }
    val isDeepPressed by deepTestInteraction.collectIsPressedAsState()
    val deepScale by animateFloatAsState(
        targetValue = if (isDeepPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "deep_scale"
    )

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("strength_hero_card"),
        accentColor = analysis.ratingColor,
        isGlowActive = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Header: Network SSID & Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(analysis.ratingColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (networkInfo.type == NetworkType.WIFI) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
                            contentDescription = "Signal Icon",
                            tint = analysis.ratingColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = networkInfo.operatorOrSsid.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${networkInfo.typeName} • ${networkInfo.frequencyBand.ifEmpty { "Active" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMediumEmphasis,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Deep Test Pulse Button
                Button(
                    onClick = onTriggerDeepTest,
                    enabled = !analysis.isDeepTesting,
                    interactionSource = deepTestInteraction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ObsidianSurfaceElevated.copy(alpha = 0.85f),
                        contentColor = NeonLime
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .scale(deepScale)
                        .border(1.dp, analysis.ratingColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .testTag("deep_stability_test_button")
                ) {
                    if (analysis.isDeepTesting) {
                        CircularProgressIndicator(
                            color = NeonLime,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Probe",
                            modifier = Modifier.size(16.dp),
                            tint = NeonLime
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DIAGNOSE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gauge Center Arc Meter (0 to 100% Stability Index)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = size.width - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Track Background Arc
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Glowing Sweep Arc
                    val activeSweep = (animatedScore / 100f) * 260f
                    val sweepBrush = Brush.sweepGradient(
                        0.0f to CyberMint,
                        0.4f to HologramCyan,
                        0.8f to NeonLime,
                        1.0f to AndroidGreen
                    )

                    drawArc(
                        brush = sweepBrush,
                        startAngle = 140f,
                        sweepAngle = activeSweep.coerceAtLeast(4f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner Numerical Perfection Readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PERFECTION INDEX",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = TextMediumEmphasis,
                        fontSize = 9.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format("%.1f%%", animatedScore),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = analysis.ratingColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(analysis.ratingColor.copy(alpha = 0.22f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = analysis.stabilityRating,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = analysis.ratingColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-metrics Row (Signal dBm, Latency, Jitter, Ceiling)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagMetricBox(
                    title = "SIGNAL POWER",
                    value = "${analysis.signalDbm} dBm",
                    subtitle = analysis.signalRating,
                    accentColor = analysis.signalColor,
                    modifier = Modifier.weight(1f)
                )
                DiagMetricBox(
                    title = "LIVE LATENCY",
                    value = FormatUtils.formatPing(analysis.currentLatencyMs),
                    subtitle = "Jitter ±${String.format("%.1f", analysis.jitterVarianceMs)}ms",
                    accentColor = HologramCyan,
                    modifier = Modifier.weight(1f)
                )
                DiagMetricBox(
                    title = "SPEED CEILING",
                    value = "~${analysis.maxSpeedPotentialMbps.toInt()} Mbps",
                    subtitle = "Max Capacity",
                    accentColor = NeonLime,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SignalClarityCard(
    analysis: NetworkStabilityAnalysis,
    networkInfo: NetworkInfoState
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("signal_clarity_card"),
        accentColor = CyberMint
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Waves,
                    contentDescription = "Signal Clarity",
                    tint = CyberMint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SIGNAL STRENGTH & RF AIRTIME CLARITY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar of Signal Strength Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Carrier Power Level",
                    fontSize = 12.sp,
                    color = TextMediumEmphasis,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${analysis.signalPercent}% (${analysis.signalDbm} dBm)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = analysis.signalColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Signal bar with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(analysis.signalPercent / 100f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    CyberMint,
                                    HologramCyan,
                                    NeonLime
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagMetricBox(
                    title = "PROTOCOL",
                    value = networkInfo.frequencyBand.ifEmpty { "5G / LTE" },
                    subtitle = networkInfo.standardProtocol.take(18),
                    accentColor = HologramCyan,
                    modifier = Modifier.weight(1f)
                )
                DiagMetricBox(
                    title = "CHANNEL",
                    value = if (networkInfo.channel > 0) "CH ${networkInfo.channel}" else "Cell RAT",
                    subtitle = analysis.channelCongestion,
                    accentColor = QuantumAmber,
                    modifier = Modifier.weight(1f)
                )
                DiagMetricBox(
                    title = "LINK SPEED",
                    value = if (networkInfo.linkSpeedMbps > 0) "${networkInfo.linkSpeedMbps} Mbps" else "Optimal",
                    subtitle = "PHY Layer",
                    accentColor = NeonLime,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LiveStabilityOscilloscopeCard(
    analysis: NetworkStabilityAnalysis
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stability_oscilloscope_card"),
        accentColor = HologramCyan
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Waveform",
                        tint = HologramCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE STABILITY & JITTER OSCILLOSCOPE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonLime.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LIVE PULSE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonLime
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Waveform
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianDark.copy(alpha = 0.9f))
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                val pingHistory = analysis.pingHistory
                val signalHistory = analysis.signalHistory

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid Lines
                    for (i in 1..3) {
                        val y = h * (i / 4f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    if (pingHistory.size >= 2) {
                        val stepX = w / (pingHistory.size - 1)
                        val maxPing = (pingHistory.maxOrNull() ?: 100f).coerceAtLeast(60f)

                        // 1. Draw Ping Jitter Path (Cyan Waveform)
                        val pingPath = Path()
                        pingHistory.forEachIndexed { index, ping ->
                            val x = index * stepX
                            val normalizedPing = (ping / maxPing).coerceIn(0.05f, 0.95f)
                            val y = h - (normalizedPing * h)
                            if (index == 0) pingPath.moveTo(x, y) else pingPath.lineTo(x, y)
                        }

                        drawPath(
                            path = pingPath,
                            color = HologramCyan,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw glow fill beneath ping path
                        val fillPath = Path().apply {
                            addPath(pingPath)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(HologramCyan.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                    }

                    if (signalHistory.size >= 2) {
                        val stepX = w / (signalHistory.size - 1)
                        // 2. Draw Signal Stability Path (NeonLime Line)
                        val signalPath = Path()
                        signalHistory.forEachIndexed { index, sig ->
                            val x = index * stepX
                            val normalizedSig = (sig / 100f).coerceIn(0.1f, 0.95f)
                            val y = h - (normalizedSig * h)
                            if (index == 0) signalPath.moveTo(x, y) else signalPath.lineTo(x, y)
                        }

                        drawPath(
                            path = signalPath,
                            color = NeonLime.copy(alpha = 0.85f),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Waveform Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(HologramCyan))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Latency Stability (ms)", fontSize = 11.sp, color = TextMediumEmphasis)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeonLime))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Carrier Signal (%)", fontSize = 11.sp, color = TextMediumEmphasis)
                }
            }
        }
    }
}

@Composable
private fun ActivityReadinessCard(
    analysis: NetworkStabilityAnalysis
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_readiness_card"),
        accentColor = AndroidGreen
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Readiness",
                    tint = AndroidGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MAXIMUM SPEED & WORKLOAD READINESS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReadinessTile(
                    title = "4K / 8K HDR Stream",
                    icon = Icons.Default.Tv,
                    isReady = analysis.readiness4kStreaming,
                    modifier = Modifier.weight(1f)
                )
                ReadinessTile(
                    title = "Pro Gaming Sync",
                    icon = Icons.Default.SportsEsports,
                    isReady = analysis.readinessLowLatencyGaming,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReadinessTile(
                    title = "Cloud High-Bandwidth",
                    icon = Icons.Default.CloudUpload,
                    isReady = analysis.readinessCloudBackup,
                    modifier = Modifier.weight(1f)
                )
                ReadinessTile(
                    title = "1 Gbps Stress Test",
                    icon = Icons.Default.FitnessCenter,
                    isReady = analysis.readinessGigabitStress,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ReadinessTile(
    title: String,
    icon: ImageVector,
    isReady: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isReady) NeonLime else QuantumAmber

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianDark.copy(alpha = 0.8f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isReady) "READY • MAXIMUM" else "SUB-OPTIMAL",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = color
                    )
                }
            }

            Icon(
                imageVector = if (isReady) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = "Status",
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun OptimizationAdvisorCard(
    tips: List<NetworkOptimizationTip>
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("optimization_advisor_card"),
        accentColor = NeonLime
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = "Advisor",
                    tint = NeonLime,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MAXIMUM SPEED & STABILITY ADVISOR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (tips.isEmpty()) {
                Text(
                    text = "Calculating real-time radio frequency and latency benchmarks...",
                    color = TextMediumEmphasis,
                    fontSize = 12.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    tips.forEach { tip ->
                        OptimizationTipItem(tip = tip)
                    }
                }
            }
        }
    }
}

@Composable
private fun OptimizationTipItem(
    tip: NetworkOptimizationTip
) {
    val accentColor = if (tip.isWarning) QuantumAmber else NeonLime

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianDark.copy(alpha = 0.85f))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tip.isWarning) Icons.Default.Warning else Icons.Default.Bolt,
                    contentDescription = "Tip Icon",
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tip.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tip.impact,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = tip.description,
                    fontSize = 11.5.sp,
                    color = TextMediumEmphasis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
