package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    secondary = FuchsiaAccent,
    onSecondary = Color.White,
    tertiary = BluePrimary,
    background = LightBackground,
    onBackground = DarkText,
    surface = LightSurface,
    onSurface = DarkText,
    surfaceVariant = Color(0xFFDFE6E9),
    onSurfaceVariant = MutedDarkText
)

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    secondary = FuchsiaAccent,
    onSecondary = Color.White,
    tertiary = BluePrimary,
    background = DarkBackground,
    onBackground = LightText,
    surface = DarkSurface,
    onSurface = LightText,
    surfaceVariant = Color(0xFF2D3436),
    onSurfaceVariant = MutedLightText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Fluid animated gradient modifier (Orange IT dynamic signature gradient)
fun Modifier.fluidAnimatedGradient(shape: androidx.compose.ui.graphics.Shape = androidx.compose.ui.graphics.RectangleShape): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "fluid_gradient")
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_x"
    )
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_y"
    )

    this.background(
        brush = Brush.linearGradient(
            colors = listOf(OrangeItPurple, OrangeItCyan, OrangeItCoral, OrangeItOrange, IndigoPrimary),
            start = Offset(offsetX, offsetY),
            end = Offset(offsetX + 500f, offsetY + 500f)
        ),
        shape = shape
    )
}
