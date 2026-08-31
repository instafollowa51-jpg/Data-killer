package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AndroidGreen
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.LaserRose
import com.example.ui.theme.NeonLime
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.QuantumAmber

@Composable
fun ControlActionButtons(
    isRunning: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!isRunning) {
            // Main Big START STRESS TEST Button with Bouncy Spring Animation
            val startInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            val isStartPressed by startInteraction.collectIsPressedAsState()
            val startScale by animateFloatAsState(
                targetValue = if (isStartPressed) 0.93f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "start_button_scale"
            )

            Button(
                onClick = onStart,
                interactionSource = startInteraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .scale(startScale)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("start_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AndroidGreen,
                                    NeonLime,
                                    HologramCyan
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Start",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "KILL BANDWIDTH NOW",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        } else {
            // Active Run Controls: Pause / Resume + Terminate Stop with Bouncy Animation
            val pauseInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            val isPausePressed by pauseInteraction.collectIsPressedAsState()
            val pauseScale by animateFloatAsState(
                targetValue = if (isPausePressed) 0.92f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "pause_scale"
            )

            val stopInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            val isStopPressed by stopInteraction.collectIsPressedAsState()
            val stopScale by animateFloatAsState(
                targetValue = if (isStopPressed) 0.92f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "stop_scale"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pause / Resume Button
                Button(
                    onClick = if (isPaused) onResume else onPause,
                    interactionSource = pauseInteraction,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .scale(pauseScale)
                        .clip(RoundedCornerShape(18.dp))
                        .testTag("pause_resume_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) NeonLime else QuantumAmber
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPaused) "RESUME" else "PAUSE",
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            fontSize = 15.sp
                        )
                    }
                }

                // STOP Button
                Button(
                    onClick = onStop,
                    interactionSource = stopInteraction,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .scale(stopScale)
                        .clip(RoundedCornerShape(18.dp))
                        .testTag("stop_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LaserRose
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STOP TEST",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
