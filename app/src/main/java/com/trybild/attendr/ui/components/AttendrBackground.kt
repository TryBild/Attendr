package com.trybild.attendr.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import com.trybild.attendr.ui.theme.*

@Composable
fun AttendrBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val inf = rememberInfiniteTransition(label = "bg")

    val orb1x by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse), "o1x"
    )
    val orb1y by inf.animateFloat(
        0f, 0.6f,
        infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse), "o1y"
    )
    val orb2x by inf.animateFloat(
        1f, 0.2f,
        infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Reverse), "o2x"
    )
    val orb2y by inf.animateFloat(
        0.3f, 0.9f,
        infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Reverse), "o2y"
    )
    val orb3x by inf.animateFloat(
        0.3f, 0.8f,
        infiniteRepeatable(tween(22000, easing = LinearEasing), RepeatMode.Reverse), "o3x"
    )
    val pulse by inf.animateFloat(
        0.9f, 1.1f,
        infiniteRepeatable(tween(5000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "pulse"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Base gradient — top light blue → bottom white
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(BgGradientTop, BgGradientMid, BgGradientBottom),
                    startY = 0f, endY = size.height
                )
            )

            val w = size.width
            val h = size.height

            // Orb 1 — large navy orb drifting top-left
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrbBlue, Color.Transparent),
                    center = Offset(orb1x * w * 0.6f, orb1y * h * 0.5f),
                    radius = w * 0.6f * pulse
                ),
                radius = w * 0.6f * pulse,
                center = Offset(orb1x * w * 0.6f, orb1y * h * 0.5f)
            )

            // Orb 2 — indigo orb bottom-right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrbIndigo, Color.Transparent),
                    center = Offset(w * 0.4f + orb2x * w * 0.4f, h * 0.5f + orb2y * h * 0.3f),
                    radius = w * 0.5f
                ),
                radius = w * 0.5f,
                center = Offset(w * 0.4f + orb2x * w * 0.4f, h * 0.5f + orb2y * h * 0.3f)
            )

            // Orb 3 — sky blue small accent
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(OrbSky, Color.Transparent),
                    center = Offset(w * 0.6f + orb3x * w * 0.3f, h * 0.15f),
                    radius = w * 0.3f * pulse
                ),
                radius = w * 0.3f * pulse,
                center = Offset(w * 0.6f + orb3x * w * 0.3f, h * 0.15f)
            )
        }

        content()
    }
}
