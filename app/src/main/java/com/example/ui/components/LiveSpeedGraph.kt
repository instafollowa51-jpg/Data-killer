package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DataUsagePoint
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.QuantumAmber
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMediumEmphasis
import com.example.util.FormatUtils
import com.example.util.SpeedUnit
import java.text.DecimalFormat
import kotlin.math.max

enum class GraphDisplayMode {
    DATA_CONSUMED,
    SPEED_WAVEFORM
}

@Composable
fun LiveSpeedGraph(
    speedHistory: List<Float>,
    dataUsageHistory: List<DataUsagePoint>,
    currentSpeedMbps: Double,
    totalBytesBurned: Long,
    totalBytesDownloaded: Long,
    totalBytesUploaded: Long,
    elapsedSeconds: Long,
    speedUnit: SpeedUnit,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    var displayMode by remember { mutableStateOf(GraphDisplayMode.DATA_CONSUMED) }

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_graph_card"),
        accentColor = if (displayMode == GraphDisplayMode.DATA_CONSUMED) AndroidGreen else HologramCyan,
        isGlowActive = isRunning,
        glowIntensity = 0.4f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Mode Selector Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isRunning) NeonLime else TextDisabled)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (displayMode == GraphDisplayMode.DATA_CONSUMED) "DATA USAGE MONITOR" else "LIVE BANDWIDTH WAVE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }

                // Glass Pill Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ObsidianDark)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Data Consumed Button
                    GraphModePill(
                        selected = displayMode == GraphDisplayMode.DATA_CONSUMED,
                        icon = Icons.Default.DataUsage,
                        label = "Data (MB)",
                        activeColor = AndroidGreen,
                        onClick = { displayMode = GraphDisplayMode.DATA_CONSUMED }
                    )

                    // Speed Waveform Button
                    GraphModePill(
                        selected = displayMode == GraphDisplayMode.SPEED_WAVEFORM,
                        icon = Icons.Default.ShowChart,
                        label = "Speed",
                        activeColor = HologramCyan,
                        onClick = { displayMode = GraphDisplayMode.SPEED_WAVEFORM }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-metrics / Legend Bar
            AnimatedContent(
                targetState = displayMode,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                label = "graph_subbar"
            ) { mode ->
                if (mode == GraphDisplayMode.DATA_CONSUMED) {
                    val (num, unit) = FormatUtils.formatBytesToMbOrGb(totalBytesBurned)
                    val burnRateMBs = (currentSpeedMbps / 8.0)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem(color = AndroidGreen, label = "Total Burned: ${FormatUtils.formatBytes(totalBytesBurned)}")
                            LegendItem(color = HologramCyan, label = "Down: ${FormatUtils.formatBytes(totalBytesDownloaded)}")
                            if (totalBytesUploaded > 0) {
                                LegendItem(color = QuantumAmber, label = "Up: ${FormatUtils.formatBytes(totalBytesUploaded)}")
                            }
                        }
                        Text(
                            text = "Rate: ${DecimalFormat("#,##0.0").format(burnRateMBs)} MB/s",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem(color = HologramCyan, label = "Throughput: ${FormatUtils.formatSpeed(currentSpeedMbps, speedUnit)}")
                            LegendItem(color = AndroidGreen, label = "Active Streams")
                        }
                        Text(
                            text = "T+ ${FormatUtils.formatDuration(elapsedSeconds)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextMediumEmphasis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Graph Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianDark)
            ) {
                if (displayMode == GraphDisplayMode.DATA_CONSUMED) {
                    DataConsumedOverTimeCanvas(
                        dataUsageHistory = dataUsageHistory,
                        isRunning = isRunning
                    )
                } else {
                    SpeedWaveformCanvas(
                        speedHistory = speedHistory,
                        speedUnit = speedUnit,
                        isRunning = isRunning
                    )
                }
            }
        }
    }
}

@Composable
private fun GraphModePill(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    activeColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) activeColor.copy(alpha = 0.22f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) activeColor else TextMediumEmphasis,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) activeColor else TextMediumEmphasis
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMediumEmphasis,
            fontSize = 11.sp
        )
    }
}

/**
 * Real-time Data Consumed Over Time Canvas with labeled X (Time) and Y (MB/GB) Axes
 */
@Composable
private fun DataConsumedOverTimeCanvas(
    dataUsageHistory: List<DataUsagePoint>,
    isRunning: Boolean
) {
    val df = DecimalFormat("#,##0.#")

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 12.dp, top = 12.dp, bottom = 20.dp)
    ) {
        val width = size.width
        val height = size.height

        // Calculate max bytes in history for dynamic Y scale
        val maxBytesInHistory = dataUsageHistory.maxOfOrNull { it.totalBytes } ?: 0L
        val maxMb = max(10.0, maxBytesInHistory / (1024.0 * 1024.0) * 1.15)
        val isGbScale = maxMb >= 1024.0
        val scaleUnit = if (isGbScale) "GB" else "MB"
        val scaleDivisor = if (isGbScale) (1024.0 * 1024.0 * 1024.0) else (1024.0 * 1024.0)
        val maxYValue = if (isGbScale) maxMb / 1024.0 else maxMb

        // 1. Draw Horizontal Grid Lines and Y-Axis Labels
        val yGridCount = 3
        for (i in 0..yGridCount) {
            val fraction = i.toFloat() / yGridCount
            val yPos = height - (fraction * height)
            val gridVal = fraction * maxYValue

            drawLine(
                color = ObsidianBorder.copy(alpha = 0.6f),
                start = Offset(0f, yPos),
                end = Offset(width, yPos),
                strokeWidth = 1.dp.toPx()
            )

            // Y-axis text label using native Android Canvas
            val labelText = "${df.format(gridVal)} $scaleUnit"
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(140, 148, 163, 184)
                textSize = 22f
                typeface = android.graphics.Typeface.MONOSPACE
            }
            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                8f,
                yPos - 4f,
                paint
            )
        }

        // 2. Draw X-Axis Time Labels
        if (dataUsageHistory.isNotEmpty()) {
            val maxTime = dataUsageHistory.lastOrNull()?.timestampSec ?: 0L
            val xLabelCount = 4
            for (i in 0..xLabelCount) {
                val fraction = i.toFloat() / xLabelCount
                val xPos = fraction * width
                val timeValSec = (fraction * max(30L, maxTime)).toLong()
                val timeLabel = "${timeValSec}s"

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(140, 148, 163, 184)
                    textSize = 20f
                    typeface = android.graphics.Typeface.MONOSPACE
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    timeLabel,
                    xPos.coerceIn(20f, width - 20f),
                    height + 16f,
                    paint
                )
            }
        }

        // 3. Draw Spline Curve of Data Consumed Over Time
        if (dataUsageHistory.size >= 2) {
            val points = dataUsageHistory.mapIndexed { index, point ->
                val x = (index.toFloat() / (dataUsageHistory.size - 1)) * width
                val yVal = (point.totalBytes.toDouble() / scaleDivisor)
                val yFraction = (yVal / maxYValue).toFloat().coerceIn(0f, 1f)
                val y = height - (yFraction * height)
                Offset(x, y)
            }

            // Build smooth Bezier path
            val strokePath = Path()
            strokePath.moveTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2, p0.y)
                val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2, p1.y)
                strokePath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
            }

            // Area Gradient Fill
            val fillPath = Path().apply {
                addPath(strokePath)
                lineTo(points.last().x, height)
                lineTo(points.first().x, height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AndroidGreen.copy(alpha = 0.35f),
                        NeonLime.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Stroke line
            drawPath(
                path = strokePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        HologramCyan,
                        AndroidGreen,
                        NeonLime
                    )
                ),
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Live glowing head indicator dot
            val lastPoint = points.last()
            drawCircle(
                color = AndroidGreen.copy(alpha = 0.4f),
                radius = 7.dp.toPx(),
                center = lastPoint
            )
            drawCircle(
                color = NeonLime,
                radius = 3.5.dp.toPx(),
                center = lastPoint
            )
        } else if (!isRunning) {
            // Idle placeholder line
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(120, 148, 163, 184)
                textSize = 26f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawContext.canvas.nativeCanvas.drawText(
                "Start stress test to monitor real-time data consumption curve",
                width / 2,
                height / 2,
                paint
            )
        }
    }
}

/**
 * Real-time Bandwidth Waveform Canvas
 */
@Composable
private fun SpeedWaveformCanvas(
    speedHistory: List<Float>,
    speedUnit: SpeedUnit,
    isRunning: Boolean
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        val width = size.width
        val height = size.height

        val maxVal = max(50f, (speedHistory.maxOrNull() ?: 10f) * 1.2f)

        // Draw gridlines
        val gridLines = 3
        for (i in 0..gridLines) {
            val y = height * (i.toFloat() / gridLines)
            drawLine(
                color = ObsidianBorder.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (speedHistory.size >= 2) {
            val points = speedHistory.mapIndexed { index, value ->
                val x = (index.toFloat() / (speedHistory.size - 1)) * width
                val fraction = (value / maxVal).coerceIn(0f, 1f)
                val y = height - (fraction * height)
                Offset(x, y)
            }

            val strokePath = Path()
            strokePath.moveTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val cx = (p0.x + p1.x) / 2
                strokePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }

            val fillPath = Path().apply {
                addPath(strokePath)
                lineTo(points.last().x, height)
                lineTo(points.first().x, height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HologramCyan.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            drawPath(
                path = strokePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        HologramCyan,
                        AndroidGreen,
                        QuantumAmber
                    )
                ),
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
