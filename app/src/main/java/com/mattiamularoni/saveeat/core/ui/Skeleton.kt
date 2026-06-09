package com.mattiamularoni.saveeat.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Brush con effetto shimmer, usato per gli stati di caricamento (skeleton screens).
 * Coerente con le linee guida del DESIGN.md.
 */
@Composable
fun shimmerBrush(): Brush {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surfaceContainerHighest
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(x - 300f, 0f),
        end = Offset(x, 0f)
    )
}

/** Singolo blocco scheletro con shimmer. */
@Composable
fun ShimmerBox(modifier: Modifier = Modifier, cornerRadius: Int = 12) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(shimmerBrush())
    )
}

/**
 * Skeleton generico e uniforme per gli stati di caricamento delle schermate:
 * un blocco grande (header/card) seguito da alcune righe.
 */
@Composable
fun SaveEatLoadingSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBox(Modifier.fillMaxWidth(0.5f).height(22.dp), cornerRadius = 8)
        ShimmerBox(Modifier.fillMaxWidth().height(150.dp), cornerRadius = 24)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ShimmerBox(Modifier.weight(1f).height(120.dp), cornerRadius = 16)
            ShimmerBox(Modifier.weight(1f).height(120.dp), cornerRadius = 16)
        }
        repeat(3) {
            ShimmerBox(Modifier.fillMaxWidth().height(56.dp), cornerRadius = 16)
        }
    }
}
