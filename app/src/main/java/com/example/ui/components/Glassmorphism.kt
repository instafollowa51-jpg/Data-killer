package com.example.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassSpecularShine
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.ObsidianDeepVoid
import com.example.ui.theme.ObsidianSurfaceElevated

/**
 * Configuration data model for iOS-standard Liquid Glass material physics.
 */
data class LiquidGlassConfig(
    val blurRadiusDp: Float = 25f,          // iOS Standard Gaussian Blur Radius (dp)
    val vibrancy: Float = 0.85f,            // iOS Optical Material Vibrancy & Color Saturation (0.0 to 1.0)
    val lensRefractionAmount: Float = 0.70f, // Optical Specular Bevel Refraction Strength (0.0 to 1.0)
    val lensRefractionHeight: Float = 0.45f, // Convex Top Lens Curvature Highlight Height (0.0 to 1.0)
    val depthEffectDp: Float = 16f,         // Multi-layered Ambient Occlusion & Elevation Depth (dp)
    val presetName: String = "Standard iOS"
) {
    companion object {
        val STANDARD_IOS = LiquidGlassConfig(
            blurRadiusDp = 25f,
            vibrancy = 0.85f,
            lensRefractionAmount = 0.70f,
            lensRefractionHeight = 0.45f,
            depthEffectDp = 16f,
            presetName = "Standard iOS"
        )
        val ULTRA_FROST_PRO = LiquidGlassConfig(
            blurRadiusDp = 38f,
            vibrancy = 0.95f,
            lensRefractionAmount = 0.85f,
            lensRefractionHeight = 0.60f,
            depthEffectDp = 22f,
            presetName = "Ultra Frost Pro"
        )
        val DEEP_OBSIDIAN = LiquidGlassConfig(
            blurRadiusDp = 16f,
            vibrancy = 0.60f,
            lensRefractionAmount = 0.45f,
            lensRefractionHeight = 0.30f,
            depthEffectDp = 12f,
            presetName = "Deep Obsidian"
        )
        val CLEAR_CRYSTAL = LiquidGlassConfig(
            blurRadiusDp = 10f,
            vibrancy = 0.75f,
            lensRefractionAmount = 0.95f,
            lensRefractionHeight = 0.75f,
            depthEffectDp = 8f,
            presetName = "Clear Optical Crystal"
        )
    }
}

val LocalLiquidGlassConfig = compositionLocalOf { LiquidGlassConfig.STANDARD_IOS }

/**
 * iOS-Grade Liquid Glass Container with dynamic blur radius, optical vibrancy,
 * lens refraction caustics, top lens height reflection, and multi-layered depth shadows.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    accentColor: Color = AndroidGreen,
    isGlowActive: Boolean = false,
    glowIntensity: Float = 0.5f,
    customConfig: LiquidGlassConfig? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val config = customConfig ?: LocalLiquidGlassConfig.current
    val density = LocalDensity.current

    val vibrancyFactor = config.vibrancy.coerceIn(0f, 1f)
    val refractionFactor = config.lensRefractionAmount.coerceIn(0f, 1f)
    val refractionHeightFactor = config.lensRefractionHeight.coerceIn(0f, 1f)
    val blurFactor = (config.blurRadiusDp / 50f).coerceIn(0f, 1f)
    val depthShadowPx = with(density) { config.depthEffectDp.dp.toPx() }

    // Multi-stop Acrylic Vibrancy Substrate with Optical Light Dispersion
    val baseSurfaceAlpha = (0.82f - blurFactor * 0.18f + (1f - vibrancyFactor) * 0.10f).coerceIn(0.55f, 0.95f)
    val midSurfaceAlpha = (0.60f + vibrancyFactor * 0.18f).coerceIn(0.40f, 0.88f)
    val bottomSurfaceAlpha = (0.88f - blurFactor * 0.12f).coerceIn(0.65f, 0.96f)

    val glassBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            ObsidianSurfaceElevated.copy(alpha = baseSurfaceAlpha),
            accentColor.copy(alpha = 0.09f * vibrancyFactor),
            ObsidianSurfaceElevated.copy(alpha = midSurfaceAlpha),
            ObsidianDark.copy(alpha = bottomSurfaceAlpha)
        )
    )

    // Optical Specular Bevel Border (Refraction Amount)
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = (0.45f * refractionFactor + if (isGlowActive) 0.35f * glowIntensity else 0.05f).coerceIn(0f, 0.9f)),
            accentColor.copy(alpha = if (isGlowActive) 0.40f * glowIntensity else 0.28f * refractionFactor),
            GlassSpecularShine.copy(alpha = 0.35f * refractionFactor),
            ObsidianBorder.copy(alpha = 0.30f)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = modifier
            // 1. Layer 1: Ambient Occlusion & Multi-layered Depth Shadow
            .drawBehind {
                if (depthShadowPx > 0f) {
                    val shadowAlpha = (0.35f * (config.depthEffectDp / 16f)).coerceIn(0.08f, 0.65f)
                    val glowAlpha = if (isGlowActive) 0.20f * glowIntensity else 0.04f * vibrancyFactor

                    // Deep ambient drop shadow
                    drawRoundRect(
                        color = Color.Black.copy(alpha = shadowAlpha),
                        topLeft = Offset(0f, depthShadowPx * 0.30f),
                        size = size,
                        cornerRadius = CornerRadius(with(density) { 22.dp.toPx() })
                    )

                    // Optical vibrancy ambient colored back-glow
                    if (glowAlpha > 0.01f) {
                        drawRoundRect(
                            color = accentColor.copy(alpha = glowAlpha),
                            topLeft = Offset(-depthShadowPx * 0.08f, -depthShadowPx * 0.08f),
                            size = Size(size.width + depthShadowPx * 0.16f, size.height + depthShadowPx * 0.16f),
                            cornerRadius = CornerRadius(with(density) { 24.dp.toPx() })
                        )
                    }
                }
            }
            .clip(shape)
            // 2. Liquid Glass Acrylic Substrate
            .background(glassBackgroundBrush)
            // 3. Optical Lens Refraction Height: Convex Top Specular Curvature Highlight
            .drawWithContent {
                drawContent()
                if (refractionHeightFactor > 0.02f) {
                    val highlightHeight = size.height * (0.32f * refractionHeightFactor)
                    val cornerRadiusVal = with(density) { 22.dp.toPx() }

                    // Convex light sheen
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.20f * refractionFactor),
                                accentColor.copy(alpha = 0.06f * vibrancyFactor),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = highlightHeight
                        ),
                        topLeft = Offset.Zero,
                        size = Size(size.width, highlightHeight),
                        cornerRadius = CornerRadius(cornerRadiusVal, cornerRadiusVal)
                    )

                    // Top-edge crisp hairline refraction
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.65f * refractionFactor),
                                GlassSpecularShine.copy(alpha = 0.45f * refractionFactor),
                                accentColor.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, with(density) { 1.2.dp.toPx() }),
                        cornerRadius = CornerRadius(cornerRadiusVal, cornerRadiusVal),
                        style = Stroke(width = with(density) { 1.dp.toPx() })
                    )
                }
            }
            .border(1.2.dp, borderBrush, shape)
    ) {
        content()
    }
}

/**
 * Animated Ambient Background with floating cyber-green and cyan liquid orbs
 * creating an immersive glassmorphism backdrop.
 */
@Composable
fun AmbientLiquidMeshBackground(
    isRunning: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_mesh")
    val animProgress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mesh_orb1"
    )
    val animProgress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mesh_orb2"
    )

    val speedMultiplier = if (isRunning) 1.5f else 1.0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDeepVoid)
    ) {
        // Floating cyber aurora orbs canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Orb 1: Android Green Top-Right Orb
            val orb1X = w * (0.65f + 0.25f * kotlin.math.sin(animProgress1 * Math.PI.toFloat() * 2f))
            val orb1Y = h * (0.15f + 0.15f * kotlin.math.cos(animProgress1 * Math.PI.toFloat() * 2f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AndroidGreen.copy(alpha = if (isRunning) 0.22f else 0.10f),
                        NeonLime.copy(alpha = if (isRunning) 0.12f else 0.04f),
                        Color.Transparent
                    ),
                    center = Offset(orb1X, orb1Y),
                    radius = w * 0.80f
                ),
                center = Offset(orb1X, orb1Y),
                radius = w * 0.80f
            )

            // Orb 2: Holographic Cyan Bottom-Left Orb
            val orb2X = w * (0.35f + 0.25f * kotlin.math.cos(animProgress2 * Math.PI.toFloat() * 2f))
            val orb2Y = h * (0.70f + 0.18f * kotlin.math.sin(animProgress2 * Math.PI.toFloat() * 2f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        HologramCyan.copy(alpha = if (isRunning) 0.18f else 0.08f),
                        Color(0xFF003840).copy(alpha = if (isRunning) 0.12f else 0.04f),
                        Color.Transparent
                    ),
                    center = Offset(orb2X, orb2Y),
                    radius = w * 0.90f
                ),
                center = Offset(orb2X, orb2Y),
                radius = w * 0.90f
            )
        }

        content()
    }
}

