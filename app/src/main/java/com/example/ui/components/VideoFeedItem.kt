package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import coil.compose.AsyncImage
import com.example.data.Post
import com.example.ui.theme.*
import com.example.utils.toPersianCurrency
import com.example.utils.toPersianNumber

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.graphicsLayer

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.view.TextureView

data class EmojiAnimation(val id: Long, val x: Float, val y: Float, val text: String)

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoFeedItem(
    post: Post,
    onBuyClicked: (Post) -> Unit,
    modifier: Modifier = Modifier,
    isFocused: Boolean = true,
    exoPlayer: androidx.media3.exoplayer.ExoPlayer? = null,
    onProfileClicked: () -> Unit = {}
) {
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likesCount by remember { mutableStateOf(post.likesCount) }
    var isSaved by remember { mutableStateOf(post.isSaved) }
    var savesCount by remember { mutableStateOf(post.savesCount) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    
    var flyingEmojis by remember { mutableStateOf(listOf<EmojiAnimation>()) }

    fun extractEmoji(description: String): String {
        if (description.contains("#کتونی") || description.contains("#کفش")) return "👟"
        if (description.contains("#استایل") || description.contains("#هودی") || description.contains("#لباس")) return "👕"
        if (description.contains("#کارگو")) return "👖"
        if (description.contains("#آتش") || description.contains("ترند")) return "🔥"
        return "❤️"
    }

    Box(modifier = modifier
        .fillMaxSize()
        .background(Color.Black)
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { offset ->
                    if (!isLiked) {
                        isLiked = true
                        likesCount++
                    }
                    val emojiText = extractEmoji(post.description)
                    val newEmoji = EmojiAnimation(System.currentTimeMillis(), offset.x, offset.y, emojiText)
                    flyingEmojis = flyingEmojis + newEmoji
                }
            )
        }
    ) {

        var isPlaying by remember { mutableStateOf(false) }
        var hasPlaybackError by remember(post.videoUrl) { mutableStateOf(false) }
        
        LaunchedEffect(isFocused) {
            if (isFocused) {
                kotlinx.coroutines.delay(400) // Debounce for fast scrolling
                isPlaying = true
            } else {
                isPlaying = false
            }
        }

        if (post.videoUrl != null && exoPlayer != null) {
            DisposableEffect(exoPlayer, post.videoUrl) {
                val listener = object : androidx.media3.common.Player.Listener {
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        android.util.Log.w("VideoFeedItem", "Playback error for ${post.videoUrl}: ${error.message}")
                        hasPlaybackError = true
                        try {
                            exoPlayer.playWhenReady = false
                            exoPlayer.stop()
                            exoPlayer.clearMediaItems()
                        } catch (_: Throwable) {}
                    }
                }
                exoPlayer.addListener(listener)
                onDispose {
                    exoPlayer.removeListener(listener)
                    if (exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString() == post.videoUrl) {
                        try {
                            exoPlayer.playWhenReady = false
                            exoPlayer.pause()
                            exoPlayer.stop()
                            exoPlayer.clearMediaItems()
                        } catch (_: Throwable) {}
                    }
                }
            }

            LaunchedEffect(isPlaying, hasPlaybackError) {
                if (isPlaying && !hasPlaybackError) {
                    try {
                        val currentUri = exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString()
                        if (currentUri != post.videoUrl) {
                            val mediaItem = androidx.media3.common.MediaItem.fromUri(post.videoUrl)
                            exoPlayer.setMediaItem(mediaItem)
                            exoPlayer.prepare()
                        }
                        exoPlayer.playWhenReady = true
                    } catch (e: Throwable) {
                        hasPlaybackError = true
                    }
                } else {
                    if (exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString() == post.videoUrl) {
                        try {
                            exoPlayer.playWhenReady = false
                            exoPlayer.pause()
                            exoPlayer.stop()
                            exoPlayer.clearMediaItems()
                        } catch (_: Throwable) {}
                    }
                }
            }
            
            if (isPlaying && !hasPlaybackError) {
                AndroidView(
                    factory = { ctx ->
                        TextureView(ctx).apply {
                            try {
                                exoPlayer.setVideoTextureView(this)
                            } catch (_: Throwable) {}
                        }
                    },
                    update = { textureView ->
                        try {
                            exoPlayer.setVideoTextureView(textureView)
                        } catch (_: Throwable) {}
                    },
                    onRelease = { textureView ->
                        try {
                            exoPlayer.clearVideoTextureView(textureView)
                        } catch (_: Throwable) {}
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Video Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Post Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Flying Emojis
        flyingEmojis.forEach { emoji ->
            key(emoji.id) {
                FlyingEmoji(emoji, onAnimationFinished = {
                    flyingEmojis = flyingEmojis.filter { it.id != emoji.id }
                })
            }
        }

        // Gradient overlays for text readability (top and bottom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Top-Right Store Location Badge (Moved down to avoid Top Nav Bar overlap)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xDD0B0F19),
            border = BorderStroke(1.dp, GlassBorder),
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 8.dp)
                .clickable { onProfileClicked() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "موقعیت فروشگاه",
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val storeCity = post.author.location?.let { "${it.city}، ${it.neighborhood}" }
                    ?: "${post.author.city}، جردن"
                Text(
                    text = storeCity,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldSuccess.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "۷۵۰m",
                        color = EmeraldSuccess,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Right side Action Sidebar (Left side in RTL, strictly isolated with no overlap)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Profile Avatar with Store Follow + badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onProfileClicked() }
            ) {
                AsyncImage(
                    model = post.author.avatarUrl,
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = 6.dp)
                        .clip(CircleShape)
                        .background(IndigoPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "+",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(2.dp))

            // Like Action
            ActionIconButton(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "پسندیدن",
                tint = if (isLiked) TikTokPink else Color.White,
                label = likesCount.toPersianNumber(),
                onClick = {
                    isLiked = !isLiked
                    if (isLiked) likesCount++ else likesCount--
                }
            )

            // Comments Action
            ActionIconButton(
                icon = Icons.Default.ChatBubbleOutline,
                contentDescription = "نظرات",
                tint = Color.White,
                label = post.commentsCount.toPersianNumber(),
                onClick = { showCommentsSheet = true }
            )

            // Share Action
            ActionIconButton(
                icon = Icons.Default.Share,
                contentDescription = "اشتراک‌گذاری",
                tint = GoldCoin,
                label = "اشتراک",
                onClick = { showShareSheet = true }
            )
            
            // Bookmark / Save Action
            ActionIconButton(
                icon = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "ذخیره",
                tint = if (isSaved) EmeraldSuccess else Color.White,
                label = savesCount.toPersianNumber(),
                onClick = {
                    isSaved = !isSaved
                    if (isSaved) savesCount++ else savesCount--
                }
            )
        }

        // Bottom Content Column (Right side in RTL, constrained with end=72dp to avoid Action Sidebar)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, end = 72.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Store / Author name with verified badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.clickable { onProfileClicked() }
            ) {
                Text(
                    text = post.author.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (post.author.isVerified) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "تایید شده",
                        tint = OrangeItCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "@${post.author.username}",
                    color = SlateMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Description
            Text(
                text = post.description,
                color = Color(0xFFE2E8F0),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            // Showcase Tags Row (پرو حضوری، ارسال فوری، ضمانت اصالت)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xDD0F172A),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "پرو حضوری",
                            color = Color(0xFFD1FAE5),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldSuccess.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "ارسال فوری",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = BorderStroke(0.5.dp, GlassBorder)
                ) {
                    Text(
                        text = "ضمانت اصالت",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp
                    )
                }
            }

            // Integrated Product Buy Card
            post.product?.let { product ->
                Surface(
                    onClick = { onBuyClicked(post) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xEE131B2E),
                    border = BorderStroke(1.dp, GlassBorder),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            AsyncImage(
                                model = product.imageUrl ?: post.imageUrl,
                                contentDescription = product.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = product.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = product.price.toPersianCurrency(),
                                    color = OrangeItCyan,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IndigoPrimary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "خرید فوری",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // Audio Track
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "آهنگ",
                    tint = OrangeItPurple,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = post.audioTrack,
                    color = Color(0xFFCBD5E1),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
    
    // Comments Modal Bottom Sheet
    if (showCommentsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommentsSheet = false },
            containerColor = Color(0xFF131B2E),
            scrimColor = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(SlateMuted.copy(alpha = 0.4f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "نظرات کاربران (${post.commentsCount.toPersianNumber()})",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showCommentsSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = SlateMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val mockComments = listOf(
                    Triple("سارا احمدی", "قیمت این کار چقدره و سایزبندی داره؟", "۲ ساعت پیش"),
                    Triple("رضا کمالی", "کیفیت جنس فوق‌العاده‌ست، پیشنهاد می‌کنم 👌", "۵ ساعت پیش"),
                    Triple("مهسا کریمی", "ارسال به شهرستان چند روز طول می‌کشه؟", "دیروز"),
                    Triple("امیر حسینی", "خیلی قشنگه، ممنون از فروشگاه کوک ❤️", "۲ روز پیش")
                )

                mockComments.forEach { (name, text, time) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(0.5.dp, GlassBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(IndigoPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    name.take(1),
                                    color = IndigoPrimary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        name,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        time,
                                        color = SlateMuted,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text,
                                    color = Color(0xFFCBD5E1),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Share Modal Bottom Sheet
    if (showShareSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShareSheet = false },
            containerColor = Color(0xFF131B2E),
            scrimColor = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(SlateMuted.copy(alpha = 0.4f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    "اشتراک‌گذاری در کوک",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val shareOptions = listOf(
                        Triple(Icons.Default.Link, "کپی لینک", IndigoPrimary),
                        Triple(Icons.Default.Send, "ارسال دایرکت", OrangeItCyan),
                        Triple(Icons.Default.Group, "خرید گروهی", FuchsiaAccent),
                        Triple(Icons.Default.MoreHoriz, "سایر موارد", SlateMuted)
                    )
                    shareOptions.forEach { (icon, text, tintColor) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showShareSheet = false }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(tintColor.copy(alpha = 0.15f))
                                    .border(1.dp, tintColor.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = text, tint = tintColor, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .border(0.5.dp, GlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun FlyingEmoji(emoji: EmojiAnimation, onAnimationFinished: () -> Unit) {
    val offsetY = remember { Animatable(emoji.y) }
    val alpha = remember { Animatable(1f) }
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        launch {
            offsetY.animateTo(emoji.y - 300f, animationSpec = tween(1000))
        }
        launch {
            alpha.animateTo(0f, animationSpec = tween(1000, delayMillis = 200))
        }
        launch {
            scale.animateTo(1.5f, animationSpec = tween(1000))
        }
        delay(1200)
        onAnimationFinished()
    }

    Text(
        text = emoji.text,
        fontSize = 48.sp,
        modifier = Modifier
            .offset(x = (emoji.x / 2.5).dp, y = (offsetY.value / 2.5).dp)
            .graphicsLayer(
                alpha = alpha.value,
                scaleX = scale.value,
                scaleY = scale.value
            )
    )
}

/**
 * Instagram-Shopping Style Product Hotspot Pin:
 * Pulsing interactive tag pinned directly over the media content.
 */
@Composable
fun ProductHotspotTag(
    product: com.example.data.Product,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hotspotPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xCC0F172A),
        border = BorderStroke(1.dp, OrangeItCyan.copy(alpha = 0.7f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing dot
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale, alpha = pulseAlpha)
                        .clip(CircleShape)
                        .background(FuchsiaAccent)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = product.title,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = product.price.toPersianCurrency(),
                    color = EmeraldSuccess,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
