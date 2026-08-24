package com.example.paranoia.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.paranoia.data.CoinSide
import com.example.paranoia.ui.theme.AccentCyan
import com.example.paranoia.ui.theme.AccentFuchsia
import com.example.paranoia.ui.theme.CoinGoldEnd
import com.example.paranoia.ui.theme.CoinGoldStart
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun InteractiveCoin(
    isFlipping: Boolean,
    targetResult: CoinSide?,
    onFlipComplete: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 180
) {
    val rotationY = remember { Animatable(0f) }
    val heightOffset = remember { Animatable(0f) }

    // Breathing glow animation while idle
    val infiniteTransition = rememberInfiniteTransition(label = "coin_idle")
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LaunchedEffect(isFlipping) {
        if (isFlipping) {
            // Launch coin toss physics: flips 6 full spins (2160 deg) plus extra half turn if TAILS
            val targetDegrees = 360f * 6f + if (targetResult == CoinSide.TAILS) 180f else 0f
            
            // Simultaneous flip height arc and rapid Y rotation
            rotationY.snapTo(0f)
            heightOffset.animateTo(
                targetValue = -60f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
            heightOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(isFlipping) {
        if (isFlipping) {
            val targetDegrees = 360f * 6f + if (targetResult == CoinSide.TAILS) 180f else 0f
            rotationY.animateTo(
                targetValue = targetDegrees,
                animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing)
            )
            onFlipComplete()
        }
    }

    val currentRotY = rotationY.value % 360f
    val isShowingFront = currentRotY in 0f..90f || currentRotY in 270f..360f

    Box(
        modifier = modifier
            .size(size.dp)
            .graphicsLayer {
                this.rotationY = currentRotY
                this.translationY = heightOffset.value
                this.scaleX = if (!isFlipping) idlePulse else 1f
                this.scaleY = if (!isFlipping) idlePulse else 1f
                cameraDistance = 16f * density
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2f - 8f

            if (isShowingFront) {
                // Front: Heads / Coin Face (Gold / Cyan Glow)
                drawCoinFace(
                    center = center,
                    radius = radius,
                    isHeads = true,
                    isFlipping = isFlipping,
                    targetResult = targetResult
                )
            } else {
                // Back: Tails / Secret Mystery Face (Gold / Fuchsia Glow)
                drawCoinFace(
                    center = center,
                    radius = radius,
                    isHeads = false,
                    isFlipping = isFlipping,
                    targetResult = targetResult
                )
            }
        }
    }
}

private fun DrawScope.drawCoinFace(
    center: Offset,
    radius: Float,
    isHeads: Boolean,
    isFlipping: Boolean,
    targetResult: CoinSide?
) {
    // Drop shadow
    drawCircle(
        color = Color(0x66000000),
        radius = radius + 6f,
        center = center.copy(y = center.y + 6f)
    )

    // Coin outer rim
    val rimBrush = if (isFlipping || targetResult == null) {
        Brush.linearGradient(
            colors = listOf(CoinGoldStart, CoinGoldEnd, Color(0xFF78350F), CoinGoldStart),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x + radius, center.y + radius)
        )
    } else if (targetResult == CoinSide.HEADS) {
        Brush.linearGradient(
            colors = listOf(AccentCyan, Color(0xFF0284C7), Color(0xFF0F172A), AccentCyan),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x + radius, center.y + radius)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(AccentFuchsia, Color(0xFF9D174D), Color(0xFF1E1B4B), AccentFuchsia),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x + radius, center.y + radius)
        )
    }

    drawCircle(
        brush = rimBrush,
        radius = radius,
        center = center
    )

    // Inner face ring
    val innerRadius = radius * 0.85f
    val innerBrush = if (isFlipping || targetResult == null) {
        Brush.radialGradient(
            colors = listOf(Color(0xFFFDE68A), Color(0xFFD97706), Color(0xFF451A03)),
            center = center,
            radius = innerRadius
        )
    } else if (targetResult == CoinSide.HEADS) {
        Brush.radialGradient(
            colors = listOf(Color(0xFF67E8F9), AccentCyan, Color(0xFF083344)),
            center = center,
            radius = innerRadius
        )
    } else {
        Brush.radialGradient(
            colors = listOf(Color(0xFFFDA4AF), AccentFuchsia, Color(0xFF3B0764)),
            center = center,
            radius = innerRadius
        )
    }

    drawCircle(
        brush = innerBrush,
        radius = innerRadius,
        center = center
    )

    // Inner milled ring line
    drawCircle(
        color = Color(0x60FFFFFF),
        radius = innerRadius * 0.92f,
        center = center,
        style = Stroke(width = 3f)
    )

    // Center icon/symbol
    if (isHeads) {
        // Draw Eye icon / 'H' for Heads
        drawEyeSymbol(center = center, size = innerRadius * 0.75f)
    } else {
        // Draw Paranoia Mystery Glyph 'M' or '?'
        drawMysterySymbol(center = center, size = innerRadius * 0.75f)
    }
}

private fun DrawScope.drawEyeSymbol(center: Offset, size: Float) {
    val eyeWidth = size * 1.3f
    val eyeHeight = size * 0.75f

    // Eye outline path
    val path = Path().apply {
        moveTo(center.x - eyeWidth / 2, center.y)
        quadraticTo(
            center.x, center.y - eyeHeight,
            center.x + eyeWidth / 2, center.y
        )
        quadraticTo(
            center.x, center.y + eyeHeight,
            center.x - eyeWidth / 2, center.y
        )
        close()
    }

    drawPath(
        path = path,
        color = Color.White,
        style = Stroke(width = 6f, cap = StrokeCap.Round)
    )

    // Pupil
    drawCircle(
        color = Color.White,
        radius = eyeHeight * 0.35f,
        center = center
    )

    drawCircle(
        color = Color(0xFF083344),
        radius = eyeHeight * 0.18f,
        center = center
    )
}

private fun DrawScope.drawMysterySymbol(center: Offset, size: Float) {
    // Draw stylized '?' and padlock/secret mask
    val path = Path().apply {
        // Draw sleek M / Mystery crest
        moveTo(center.x - size * 0.45f, center.y + size * 0.4f)
        lineTo(center.x - size * 0.45f, center.y - size * 0.35f)
        lineTo(center.x, center.y + size * 0.1f)
        lineTo(center.x + size * 0.45f, center.y - size * 0.35f)
        lineTo(center.x + size * 0.45f, center.y + size * 0.4f)
    }

    drawPath(
        path = path,
        color = Color.White,
        style = Stroke(width = 7f, cap = StrokeCap.Round)
    )

    // Question dot
    drawCircle(
        color = Color.White,
        radius = size * 0.08f,
        center = Offset(center.x, center.y + size * 0.42f)
    )
}
