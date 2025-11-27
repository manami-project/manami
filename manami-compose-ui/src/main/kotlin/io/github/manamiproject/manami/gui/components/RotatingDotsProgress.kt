package io.github.manamiproject.manami.gui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.theme.ThemeState

@Composable
fun RotatingDotsProgress(size: Dp = 64.dp, alpha: Float = 1.0f) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.size(size).alpha(alpha)) {
        val cx = size.toPx() / 2f
        val cy = size.toPx() / 2f
        val radius = size.toPx() * 0.35f
        val dotRadius = size.toPx() * 0.07f
        val angles = listOf(0f, 120f, 240f)

        for (i in angles.indices) {
            val angleRad = Math.toRadians((angles[i] + rotation).toDouble())
            val x = cx + (radius * kotlin.math.cos(angleRad)).toFloat()
            val y = cy + (radius * kotlin.math.sin(angleRad)).toFloat()

            drawCircle(
                color = ThemeState.instance.currentScheme.value.primary,
                radius = dotRadius,
                center = Offset(x, y),
                alpha = 1f - i * 0.25f
            )
        }
    }
}