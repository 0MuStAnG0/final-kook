package com.example.ui.map.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.data.User
import com.example.ui.theme.*
import kotlin.math.roundToInt

// Custom downward-pointing triangle shape for pin stem
val PinPointerShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width / 2f, size.height)
    close()
}

@Composable
fun StoreMapPin(
    store: User,
    isSelected: Boolean,
    pulseScale: Float,
    screenX: Float,
    screenY: Float,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        label = "pinScale"
    )

    // Calculate dynamic anchor offset so the pin tip rests exactly at (screenX, screenY)
    val pinHeightDp = if (isSelected) 54.dp else 44.dp
    val pinHeightPx = with(density) { pinHeightDp.toPx() }

    Box(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    val x = (screenX - placeable.width / 2f).roundToInt()
                    val y = (screenY - placeable.height).roundToInt()
                    placeable.place(x, y)
                }
            }
            .zIndex(if (isSelected) 10f else if (store.isLive) 5f else 2f),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Ground shadow & Live pulsing radar under the pin tip
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 4.dp)
        ) {
            if (store.isLive) {
                Box(
                    modifier = Modifier
                        .size(32.dp * pulseScale)
                        .clip(CircleShape)
                        .background(FuchsiaAccent.copy(alpha = 0.35f))
                        .align(Alignment.Center)
                )
            }
            Box(
                modifier = Modifier
                    .size(if (isSelected) 18.dp else 12.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .align(Alignment.Center)
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (store.isLive) FuchsiaAccent else if (isSelected) IndigoPrimary else Color.White)
                    .align(Alignment.Center)
            )
        }

        // Main Pin Column (Card Bubble + Stem)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scaleAnim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            // Pin Content Pill / Bubble
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) {
                    Color(0xFF1E1B4B) // Deep vibrant indigo
                } else {
                    Color(0xFF0F172A).copy(alpha = 0.94f) // Slate dark
                },
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    brush = if (store.isLive) {
                        Brush.linearGradient(listOf(FuchsiaAccent, Color(0xFFFF007A)))
                    } else if (isSelected) {
                        Brush.linearGradient(listOf(Color.White, IndigoPrimary))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                    }
                ),
                shadowElevation = if (isSelected) 12.dp else 6.dp,
                modifier = Modifier.shadow(
                    elevation = if (isSelected) 12.dp else 6.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = if (store.isLive) FuchsiaAccent else if (isSelected) IndigoPrimary else Color.Black
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Store Thumbnail or Live Badge
                    Box(modifier = Modifier.size(24.dp)) {
                        AsyncImage(
                            model = store.avatarUrl,
                            contentDescription = store.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(
                                    1.dp,
                                    if (store.isLive) FuchsiaAccent else Color.White.copy(alpha = 0.3f),
                                    CircleShape
                                )
                        )
                        if (store.isLive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(FuchsiaAccent)
                                    .border(1.dp, Color.White, CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }

                    // Store Title and Rating
                    Column(
                        modifier = Modifier.widthIn(max = 70.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = store.name,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (store.isLive) {
                                Text(
                                    text = "LIVE",
                                    color = FuchsiaAccent,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Black
                                )
                            } else {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "${store.rating}",
                                    color = Color(0xFFFFB300),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Downward Pointer Arrow / Stem
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 7.dp)
                    .clip(PinPointerShape)
                    .background(
                        if (isSelected) Color(0xFF1E1B4B)
                        else Color(0xFF0F172A).copy(alpha = 0.94f)
                    )
            )
        }
    }
}
