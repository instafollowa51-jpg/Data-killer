package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.GlassSpecularShine
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.LaserRose
import com.example.ui.theme.NeonLime
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMediumEmphasis

data class NavTileItem(
    val index: Int,
    val title: String,
    val subtitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val accentColor: Color,
    val testTag: String
)

/**
 * Pure Transparent Multi-Tile Liquid Glass Floating Navigation Hotbar
 * with fluid spring transitions, interactive bouncy tiles, lens refraction and specular caustics.
 */
@Composable
fun LiquidGlassMultiTileNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val glassConfig = LocalLiquidGlassConfig.current

    val navItems = listOf(
        NavTileItem(
            index = 0,
            title = "Dashboard",
            subtitle = "Speed Test",
            selectedIcon = Icons.Filled.Speed,
            unselectedIcon = Icons.Outlined.Speed,
            accentColor = NeonLime,
            testTag = "nav_item_dashboard"
        ),
        NavTileItem(
            index = 1,
            title = "Telemetry",
            subtitle = "Live Ping",
            selectedIcon = Icons.Filled.NetworkCheck,
            unselectedIcon = Icons.Outlined.NetworkCheck,
            accentColor = HologramCyan,
            testTag = "nav_item_telemetry"
        ),
        NavTileItem(
            index = 2,
            title = "History",
            subtitle = "Saved Logs",
            selectedIcon = Icons.Filled.History,
            unselectedIcon = Icons.Outlined.History,
            accentColor = AndroidGreen,
            testTag = "nav_item_history"
        )
    )

    val refractionFactor = glassConfig.lensRefractionAmount.coerceIn(0f, 1f)
    val vibrancyFactor = glassConfig.vibrancy.coerceIn(0f, 1f)

    // Outer Floating Glass Shell (Pure Translucent Crystal)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            // Multi-pass Ambient Shadow & Vibrancy Back-Glow
            .drawBehind {
                val shadowDepth = with(density) { 14.dp.toPx() }
                // Deep Ambient Occlusion Drop Shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.45f),
                    topLeft = Offset(0f, shadowDepth * 0.35f),
                    size = size,
                    cornerRadius = CornerRadius(with(density) { 26.dp.toPx() })
                )
                // Translucent Liquid Ambient Back-Glow
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonLime.copy(alpha = 0.08f * vibrancyFactor),
                            HologramCyan.copy(alpha = 0.09f * vibrancyFactor),
                            AndroidGreen.copy(alpha = 0.08f * vibrancyFactor)
                        )
                    ),
                    topLeft = Offset(-4f, -4f),
                    size = Size(size.width + 8f, size.height + 8f),
                    cornerRadius = CornerRadius(with(density) { 28.dp.toPx() })
                )
            }
            .clip(RoundedCornerShape(26.dp))
            // Pure Translucent Glass Substrate (Highly transparent)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianSurfaceElevated.copy(alpha = 0.45f),
                        ObsidianDark.copy(alpha = 0.55f)
                    )
                )
            )
            // Convex Lens Refraction Sheen
            .drawWithContent {
                drawContent()
                val highlightHeight = size.height * 0.40f
                val cornerRadiusVal = with(density) { 26.dp.toPx() }

                // Top Specular Reflection Arc
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.28f * refractionFactor),
                            HologramCyan.copy(alpha = 0.06f * vibrancyFactor),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = highlightHeight
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, highlightHeight),
                    cornerRadius = CornerRadius(cornerRadiusVal, cornerRadiusVal)
                )

                // Top Edge Hairline Specular Bevel
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.65f * refractionFactor),
                            GlassSpecularShine.copy(alpha = 0.45f * refractionFactor),
                            NeonLime.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, with(density) { 1.5.dp.toPx() }),
                    cornerRadius = CornerRadius(cornerRadiusVal, cornerRadiusVal),
                    style = Stroke(width = with(density) { 1.dp.toPx() })
                )
            }
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f * refractionFactor),
                        GlassSpecularShine.copy(alpha = 0.30f),
                        ObsidianBorder.copy(alpha = 0.30f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(6.dp)
            .testTag("liquid_glass_bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = selectedTab == item.index
                LiquidNavTile(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onTabSelected(item.index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual Bouncy Liquid Glass Tile
 */
@Composable
private fun LiquidNavTile(
    item: NavTileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Bouncy Physics Scale Animation
    val targetScale = when {
        isPressed -> 0.90f
        isSelected -> 1.04f
        else -> 1.0f
    }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_tile_scale_${item.index}"
    )

    // Bouncy Elevation Offset
    val targetOffsetY = if (isSelected) (-2).dp else 0.dp
    val animatedOffsetY by animateDpAsState(
        targetValue = targetOffsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_tile_offset_${item.index}"
    )

    // Smooth Color Transitions
    val tileBackgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.30f else 0.08f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "tile_alpha_${item.index}"
    )

    val activeGlowColor by animateColorAsState(
        targetValue = if (isSelected) item.accentColor else Color.Transparent,
        animationSpec = tween(300),
        label = "tile_glow_${item.index}"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) item.accentColor else TextMediumEmphasis,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "tile_icon_color_${item.index}"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextMediumEmphasis,
        animationSpec = tween(250),
        label = "tile_text_color_${item.index}"
    )

    Box(
        modifier = modifier
            .offset(y = animatedOffsetY)
            .scale(animatedScale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        item.accentColor.copy(alpha = tileBackgroundAlpha * 0.9f),
                        ObsidianSurfaceElevated.copy(alpha = tileBackgroundAlpha),
                        ObsidianDark.copy(alpha = if (isSelected) 0.35f else 0.05f)
                    )
                )
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                item.accentColor.copy(alpha = 0.85f),
                                Color.White.copy(alpha = 0.60f),
                                item.accentColor.copy(alpha = 0.30f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                } else {
                    Modifier.border(
                        width = 0.8.dp,
                        color = Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            )
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag(item.testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with Glow Dot
            Box(
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        item.accentColor.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
                Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Tile Title
            Text(
                text = item.title,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                color = textColor,
                maxLines = 1
            )

            // Dynamic Subtitle Pill / Indicator
            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(18.dp)
                        .clip(CircleShape)
                        .background(item.accentColor)
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 9.sp,
                    color = TextMediumEmphasis.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
        }
    }
}
