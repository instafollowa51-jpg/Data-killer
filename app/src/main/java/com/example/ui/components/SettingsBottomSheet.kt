package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.StressMode
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.LaserRose
import com.example.ui.theme.NeonLime
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.QuantumAmber
import com.example.ui.theme.TextDisabled
import com.example.ui.theme.TextMediumEmphasis
import com.example.util.FormatUtils
import com.example.util.NetworkRoutingMode
import com.example.util.SpeedUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.RestartAlt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    selectedMode: StressMode,
    routingMode: NetworkRoutingMode,
    concurrencyThreads: Int,
    targetLimitBytes: Long,
    targetDurationSeconds: Long,
    speedUnit: SpeedUnit,
    keepScreenOn: Boolean,
    isWifiAvailable: Boolean,
    isCellularAvailable: Boolean,
    liquidGlassConfig: LiquidGlassConfig,
    onModeSelected: (StressMode) -> Unit,
    onRoutingModeSelected: (NetworkRoutingMode) -> Unit,
    onConcurrencyChanged: (Int) -> Unit,
    onTargetLimitBytesChanged: (Long) -> Unit,
    onTargetDurationSecondsChanged: (Long) -> Unit,
    onSpeedUnitChanged: (SpeedUnit) -> Unit,
    onKeepScreenOnChanged: (Boolean) -> Unit,
    onBlurRadiusChanged: (Float) -> Unit,
    onVibrancyChanged: (Float) -> Unit,
    onLensRefractionAmountChanged: (Float) -> Unit,
    onLensRefractionHeightChanged: (Float) -> Unit,
    onDepthEffectChanged: (Float) -> Unit,
    onPresetSelected: (LiquidGlassConfig) -> Unit,
    onResetToStandardIos: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(ObsidianBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TEST CUSTOMIZATION",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Configure network routing, data goals & stream limits",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMediumEmphasis
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ObsidianSurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. NETWORK INTERFACE SELECTION (Wi-Fi, Cellular, Both)
            SectionHeader(
                icon = Icons.Default.Public,
                title = "NETWORK INTERFACE ROUTING",
                subtitle = "Choose which physical connection to stress"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NetworkRoutingCard(
                    title = "Auto / Multi-Path",
                    subtitle = "Stress all active interfaces simultaneously",
                    icon = Icons.Default.Public,
                    selected = routingMode == NetworkRoutingMode.AUTO_ALL,
                    accentColor = NeonLime,
                    onClick = { onRoutingModeSelected(NetworkRoutingMode.AUTO_ALL) }
                )
                NetworkRoutingCard(
                    title = "Wi-Fi Only",
                    subtitle = if (isWifiAvailable) "Forces data strictly through Wi-Fi" else "Wi-Fi currently disconnected",
                    icon = Icons.Default.Wifi,
                    selected = routingMode == NetworkRoutingMode.WIFI_ONLY,
                    accentColor = AndroidGreen,
                    isEnabled = isWifiAvailable,
                    onClick = { onRoutingModeSelected(NetworkRoutingMode.WIFI_ONLY) }
                )
                NetworkRoutingCard(
                    title = "Mobile Data / Cellular Only",
                    subtitle = if (isCellularAvailable) "Bypasses Wi-Fi and burns Cellular LTE/5G" else "Mobile data unavailable",
                    icon = Icons.Default.CellTower,
                    selected = routingMode == NetworkRoutingMode.CELLULAR_ONLY,
                    accentColor = HologramCyan,
                    isEnabled = isCellularAvailable,
                    onClick = { onRoutingModeSelected(NetworkRoutingMode.CELLULAR_ONLY) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. STRESS MODE (Download, Upload, Bi-Directional)
            SectionHeader(
                icon = Icons.Default.CompareArrows,
                title = "STRESS TEST DIRECTION",
                subtitle = "Select data flow pipeline"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectableOptionChip(
                    label = "Download",
                    icon = Icons.Default.Download,
                    selected = selectedMode == StressMode.DOWNLOAD,
                    accentColor = HologramCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeSelected(StressMode.DOWNLOAD) }
                )
                SelectableOptionChip(
                    label = "Upload",
                    icon = Icons.Default.Upload,
                    selected = selectedMode == StressMode.UPLOAD,
                    accentColor = QuantumAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeSelected(StressMode.UPLOAD) }
                )
                SelectableOptionChip(
                    label = "Dual Duplex",
                    icon = Icons.Default.CompareArrows,
                    selected = selectedMode == StressMode.BI_DIRECTIONAL,
                    accentColor = NeonLime,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeSelected(StressMode.BI_DIRECTIONAL) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. TARGET DURATION GOAL
            SectionHeader(
                icon = Icons.Default.Timer,
                title = "TARGET DURATION LIMIT",
                subtitle = "Auto-stops test after elapsed time"
            )
            Spacer(modifier = Modifier.height(8.dp))
            val durationOptions = listOf(
                Pair(0L, "Unlimited"),
                Pair(30L, "30s"),
                Pair(60L, "1 Min"),
                Pair(120L, "2 Min"),
                Pair(300L, "5 Min"),
                Pair(900L, "15 Min"),
                Pair(1800L, "30 Min"),
                Pair(3600L, "1 Hour")
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0..3) {
                        val (sec, label) = durationOptions[i]
                        GridChoicePill(
                            label = label,
                            selected = targetDurationSeconds == sec,
                            modifier = Modifier.weight(1f),
                            onClick = { onTargetDurationSecondsChanged(sec) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 4..7) {
                        val (sec, label) = durationOptions[i]
                        GridChoicePill(
                            label = label,
                            selected = targetDurationSeconds == sec,
                            modifier = Modifier.weight(1f),
                            onClick = { onTargetDurationSecondsChanged(sec) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. TARGET DATA CONSUMPTION GOAL
            SectionHeader(
                icon = Icons.Default.DataUsage,
                title = "DATA CONSUMPTION GOAL",
                subtitle = "Auto-stops when exact quota is burned"
            )
            Spacer(modifier = Modifier.height(8.dp))
            val quotaOptions = listOf(
                Pair(0L, "Unlimited"),
                Pair(250L * 1024 * 1024, "250 MB"),
                Pair(500L * 1024 * 1024, "500 MB"),
                Pair(1L * 1024 * 1024 * 1024, "1 GB"),
                Pair(2500L * 1024 * 1024, "2.5 GB"),
                Pair(5L * 1024 * 1024 * 1024, "5 GB"),
                Pair(10L * 1024 * 1024 * 1024, "10 GB"),
                Pair(25L * 1024 * 1024 * 1024, "25 GB")
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0..3) {
                        val (bytes, label) = quotaOptions[i]
                        GridChoicePill(
                            label = label,
                            selected = targetLimitBytes == bytes,
                            modifier = Modifier.weight(1f),
                            onClick = { onTargetLimitBytesChanged(bytes) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 4..7) {
                        val (bytes, label) = quotaOptions[i]
                        GridChoicePill(
                            label = label,
                            selected = targetLimitBytes == bytes,
                            modifier = Modifier.weight(1f),
                            onClick = { onTargetLimitBytesChanged(bytes) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. PARALLEL THREAD CONCURRENCY (1 to 32 streams)
            SectionHeader(
                icon = Icons.Default.Memory,
                title = "PARALLEL CONCURRENCY PIPELINES",
                subtitle = "Higher streams saturate multi-gigabit connections"
            )
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = AndroidGreen
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$concurrencyThreads Parallel Streams",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime
                        )
                        Text(
                            text = when {
                                concurrencyThreads <= 4 -> "Lightweight"
                                concurrencyThreads <= 12 -> "Balanced"
                                concurrencyThreads <= 20 -> "Heavy Stress"
                                else -> "MAX APOCALYPSE"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (concurrencyThreads > 20) LaserRose else AndroidGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = concurrencyThreads.toFloat(),
                        onValueChange = { onConcurrencyChanged(it.toInt()) },
                        valueRange = 1f..32f,
                        steps = 30,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonLime,
                            activeTrackColor = AndroidGreen,
                            inactiveTrackColor = ObsidianBorder
                        ),
                        modifier = Modifier.testTag("threads_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. SPEED UNIT & SCREEN TOGGLE
            SectionHeader(
                icon = Icons.Default.Speed,
                title = "DISPLAY & SYSTEM PREFERENCES",
                subtitle = "Units and screen wakelock behavior"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpeedUnit.values().forEach { unit ->
                    SelectableOptionChip(
                        label = unit.label,
                        icon = Icons.Default.Speed,
                        selected = speedUnit == unit,
                        accentColor = HologramCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { onSpeedUnitChanged(unit) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Keep Screen On Toggle
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = AndroidGreen
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Screen Awake",
                            tint = NeonLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Keep Screen Awake",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Prevents device sleeping during stress runs",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMediumEmphasis,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = onKeepScreenOnChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = NeonLime,
                            uncheckedThumbColor = TextMediumEmphasis,
                            uncheckedTrackColor = ObsidianBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 7. iOS LIQUID GLASS OPTICS & MATERIAL LAB
            SectionHeader(
                icon = Icons.Default.AutoAwesome,
                title = "iOS LIQUID GLASS OPTICS & SHADERS",
                subtitle = "Fine-tune blur radius, vibrancy, lens refraction & depth"
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Presets Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    LiquidGlassConfig.STANDARD_IOS,
                    LiquidGlassConfig.ULTRA_FROST_PRO,
                    LiquidGlassConfig.DEEP_OBSIDIAN,
                    LiquidGlassConfig.CLEAR_CRYSTAL
                )
                presets.forEach { preset ->
                    val isSelected = liquidGlassConfig.presetName == preset.presetName
                    GridChoicePill(
                        label = preset.presetName,
                        selected = isSelected,
                        modifier = Modifier.weight(1f),
                        onClick = { onPresetSelected(preset) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Optical Glass Sliders Container
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = HologramCyan,
                customConfig = liquidGlassConfig
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Live Optical Preview Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE MATERIAL: ${liquidGlassConfig.presetName.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = NeonLime
                        )

                        IconButton(
                            onClick = onResetToStandardIos,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ObsidianSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset to Standard iOS",
                                tint = HologramCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Slider 1: Blur Radius
                    GlassSliderRow(
                        icon = Icons.Default.BlurOn,
                        title = "Blur Radius",
                        valueLabel = "${liquidGlassConfig.blurRadiusDp.toInt()} dp (iOS Standard)",
                        value = liquidGlassConfig.blurRadiusDp,
                        valueRange = 4f..50f,
                        onValueChange = onBlurRadiusChanged
                    )

                    // Slider 2: Vibrancy (Optical Light Dispersion)
                    GlassSliderRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Liquid Vibrancy",
                        valueLabel = "${(liquidGlassConfig.vibrancy * 100).toInt()}% (iOS Saturation)",
                        value = liquidGlassConfig.vibrancy,
                        valueRange = 0.10f..1.0f,
                        onValueChange = onVibrancyChanged
                    )

                    // Slider 3: Lens Refraction Amount (Specular Rim)
                    GlassSliderRow(
                        icon = Icons.Default.Lens,
                        title = "Lens Refraction Amount",
                        valueLabel = "${(liquidGlassConfig.lensRefractionAmount * 100).toInt()}% (Specular Bevel)",
                        value = liquidGlassConfig.lensRefractionAmount,
                        valueRange = 0f..1.0f,
                        onValueChange = onLensRefractionAmountChanged
                    )

                    // Slider 4: Lens Refraction Height (Convex Curvature Highlight)
                    GlassSliderRow(
                        icon = Icons.Default.Lens,
                        title = "Lens Refraction Height",
                        valueLabel = "${(liquidGlassConfig.lensRefractionHeight * 100).toInt()}% (Convex Arc)",
                        value = liquidGlassConfig.lensRefractionHeight,
                        valueRange = 0f..1.0f,
                        onValueChange = onLensRefractionHeightChanged
                    )

                    // Slider 5: Depth Effect (Ambient Occlusion & Elevation)
                    GlassSliderRow(
                        icon = Icons.Default.Layers,
                        title = "Depth Effect",
                        valueLabel = "${liquidGlassConfig.depthEffectDp.toInt()} dp (Ambient Occlusion)",
                        value = liquidGlassConfig.depthEffectDp,
                        valueRange = 0f..30f,
                        onValueChange = onDepthEffectChanged
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassSliderRow(
    icon: ImageVector,
    title: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = HologramCyan,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = NeonLime,
                fontSize = 11.sp
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = HologramCyan,
                activeTrackColor = HologramCyan,
                inactiveTrackColor = ObsidianBorder
            )
        )
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AndroidGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextMediumEmphasis,
            fontSize = 11.5.sp
        )
    }
}

@Composable
private fun NetworkRoutingCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    accentColor: Color,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        accentColor = if (selected) accentColor else ObsidianBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (selected) accentColor.copy(alpha = 0.12f) else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (selected) accentColor.copy(alpha = 0.2f) else ObsidianBorder.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (!isEnabled) TextDisabled else if (selected) accentColor else TextMediumEmphasis,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (!isEnabled) TextDisabled else if (selected) Color.White else TextMediumEmphasis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (!isEnabled) TextDisabled else TextMediumEmphasis,
                    fontSize = 11.sp
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
        }
    }
}

@Composable
private fun SelectableOptionChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        accentColor = if (selected) accentColor else ObsidianBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (selected) accentColor.copy(alpha = 0.16f) else Color.Transparent)
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) accentColor else TextMediumEmphasis,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else TextMediumEmphasis,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun GridChoicePill(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) AndroidGreen.copy(alpha = 0.22f) else ObsidianDark)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) NeonLime else TextMediumEmphasis,
            fontSize = 11.5.sp
        )
    }
}
