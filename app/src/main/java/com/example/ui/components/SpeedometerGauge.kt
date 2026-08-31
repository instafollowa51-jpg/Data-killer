package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.CyberMint
import com.example.ui.theme.GaugeTrackColor
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.LaserRose
import com.example.ui.theme.NeonLime
import com.example.ui.theme.QuantumAmber
import com.example.ui.theme.TextMediumEmphasis
import com.example.util.FormatUtils
import com.example.util.SpeedUnit
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    currentSpeedMbps: Double,
    peakSpeedMbps: Double,
    isRunning: Boolean,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    // Dynamic max scale range (e.g. 50, 100, 250, 500, 1000, 2500, 5000 Mbps)
    val maxScale = when {
        peakSpeedMbps > 2500.0 || currentSpeedMbps > 2500.0 -> 5000.0
        peakSpeedMbps > 1000.0 || currentSpeedMbps > 1000.0 -> 2500.0
        peakSpeedMbps > 500.0 || currentSpeedMbps > 500.0 -> 1000.0
        peakSpeedMbps > 250.0 || currentSpeedMbps > 250.0 -> 500.0
        peakSpeedMbps > 100.0 || currentSpeedMbps > 100.0 -> 250.0
        peakSpeedMbps > 50.0 || currentSpeedMbps > 50.0 -> 100.0
        else -> 50.0
    }

    val fraction = (currentSpeedMbps / maxScale).toFloat().coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "gauge_needle"
    )

    val peakFraction = (peakSpeedMbps / maxScale).toFloat().coerceIn(0f, 1f)

    // Pulsing core animation for active stress testing
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val displaySpeed = FormatUtils.formatSpeedValue(currentSpeedMbps, speedUnit)

    Box(
        modifier = modifier
            .size(270.dp)
            .testTag("speedometer_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth * 2.2f
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)
            val startAngle = 135f
            val sweepAngle = 270f

            // 1. Background Track Arc
            drawArc(
                color = GaugeTrackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Active Value Arc with Android Cyber Green -> Cyan -> Lime Gradient
            val activeSweep = sweepAngle * animatedFraction
            if (activeSweep > 0f) {
                val gradientBrush = Brush.sweepGradient(
                    colors = listOf(
                        HologramCyan,
                        AndroidGreen,
                        NeonLime,
                        QuantumAmber,
                        LaserRose
                    ),
                    center = center
                )
                drawArc(
                    brush = gradientBrush,
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 3. Peak Indicator Tick
            if (peakFraction > 0.01f) {
                val peakAngleDeg = startAngle + sweepAngle * peakFraction
                val peakRad = Math.toRadians(peakAngleDeg.toDouble())
                val radius = diameter / 2
                val innerR = radius - strokeWidth * 0.9f
                val outerR = radius + strokeWidth * 0.9f
                val p1 = Offset(
                    center.x + innerR.toFloat() * cos(peakRad).toFloat(),
                    center.y + innerR.toFloat() * sin(peakRad).toFloat()
                )
                val p2 = Offset(
                    center.x + outerR.toFloat() * cos(peakRad).toFloat(),
                    center.y + outerR.toFloat() * sin(peakRad).toFloat()
                )
                drawLine(
                    color = QuantumAmber,
                    start = p1,
                    end = p2,
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 4. Radial Graduation Ticks
            val numTicks = 10
            for (i in 0..numTicks) {
                val tickFraction = i.toFloat() / numTicks
                val angleDeg = startAngle + sweepAngle * tickFraction
                val rad = Math.toRadians(angleDeg.toDouble())
                val radius = (diameter / 2) - strokeWidth * 1.25f
                val tickLen = if (i % 2 == 0) 8.dp.toPx() else 4.dp.toPx()
                val pStart = Offset(
                    center.x + (radius - tickLen) * cos(rad).toFloat(),
                    center.y + (radius - tickLen) * sin(rad).toFloat()
                )
                val pEnd = Offset(
                    center.x + radius * cos(rad).toFloat(),
                    center.y + radius * sin(rad).toFloat()
                )
                drawLine(
                    color = if (tickFraction <= animatedFraction) AndroidGreen.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.15f),
                    start = pStart,
                    end = pEnd,
                    strokeWidth = if (i % 2 == 0) 2.dp.toPx() else 1.2.dp.toPx()
                )
            }

            // 5. Active Cyber Core Glow
            if (isRunning) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AndroidGreen.copy(alpha = 0.22f * pulseGlow),
                            NeonLime.copy(alpha = 0.08f * pulseGlow),
                            Color.Transparent
                        ),
                        center = center,
                        radius = diameter * 0.45f
                    ),
                    center = center,
                    radius = diameter * 0.45f
                )
            }
        }

        // Center Speed Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRunning) "BURNING SPEED" else "STANDBY",
                style = MaterialTheme.typography.labelSmall,
                color = if (isRunning) NeonLime else TextMediumEmphasis,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = displaySpeed,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (isRunning) Color.White else TextMediumEmphasis,
                lineHeight = 46.sp,
                modifier = Modifier.testTag("gauge_speed_text")
            )
            Text(
                text = speedUnit.label,
                style = MaterialTheme.typography.titleMedium,
                color = AndroidGreen,
                fontWeight = FontWeight.Bold
            )
            if (peakSpeedMbps > 0.0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "TOP: ${FormatUtils.formatSpeed(peakSpeedMbps, speedUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = QuantumAmber,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
