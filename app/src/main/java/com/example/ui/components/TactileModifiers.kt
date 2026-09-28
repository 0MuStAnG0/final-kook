package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adds subtle scale compression and dynamic shadow elevation changes when store cards are pressed/tapped,
 * providing responsive physical tactile feedback.
 */
@Composable
fun Modifier.tactileStoreCard(
    enabled: Boolean = true,
    pressedScale: Float = 0.955f,
    defaultElevation: Dp = 4.dp,
    pressedElevation: Dp = 16.dp,
    shape: Shape = RoundedCornerShape(20.dp),
    spotColor: Color = Color(0xFF0284C7),
    ambientColor: Color = Color.Black.copy(alpha = 0.45f),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tactileCardScale"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) pressedElevation else defaultElevation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tactileCardShadow"
    )

    return this
        .shadow(
            elevation = shadowElevation,
            shape = shape,
            clip = false,
            spotColor = spotColor.copy(alpha = if (isPressed) 0.65f else 0.25f),
            ambientColor = ambientColor
        )
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(color = Color.White.copy(alpha = 0.25f)),
            enabled = enabled,
            onClick = onClick
        )
}
