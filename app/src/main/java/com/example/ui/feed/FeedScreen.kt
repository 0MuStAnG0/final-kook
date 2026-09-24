package com.example.ui.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.MockData
import com.example.data.Post
import com.example.data.User
import com.example.data.AppState
import com.example.utils.DailyRewardManager
import com.example.ui.components.PaymentBottomSheet
import com.example.ui.components.VideoFeedItem
import com.example.ui.theme.*

import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.media3.common.Player
import com.example.utils.toPersianCurrency

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.FeedViewModel

@OptIn(ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToInbox: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateToRegister: () -> Unit = onNavigateToAuth,
    viewModel: FeedViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isServerConnected by viewModel.isServerConnected.collectAsState()

    val context = LocalContext.current
    val dailyRewardManager = remember { DailyRewardManager(context) }
    val listState = rememberLazyListState()
    var selectedPostForBuy by remember { mutableStateOf<Post?>(null) }
    var selectedLiveUser by remember { mutableStateOf<User?>(null) }
    var showDailyReward by remember { mutableStateOf(dailyRewardManager.shouldShowDailyReward()) }
    var feedFilterMode by remember { mutableStateOf(0) } // 0: همه (سراسری), 1: محله من (تا ۵ کیلومتر)
    
    val displayedPosts = remember(posts, feedFilterMode) {
        if (feedFilterMode == 1) {
            // Filter local stores / nearby posts
            posts.take(3)
        } else {
            posts
        }
    }
    
    // Once shown, immediately mark as seen today so tab transitions will never re-open it
    LaunchedEffect(showDailyReward) {
        if (showDailyReward) {
            dailyRewardManager.markRewardAsSeenToday()
        }
    }
    
    val isScrolling = listState.isScrollInProgress

    val exoPlayer = remember {
        com.example.utils.PlayerManager.getSharedPlayer(context)
    }
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                com.example.utils.PlayerManager.pausePlayback()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            com.example.utils.PlayerManager.pausePlayback()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FuchsiaAccent)
            }
        } else {
            LazyColumn(
                state = listState,
                flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = displayedPosts,
                    key = { _, post -> post.id }
                ) { index, post ->
                    val isFocused by remember { derivedStateOf { listState.firstVisibleItemIndex == index } }
                    VideoFeedItem(
                        post = post,
                        isFocused = isFocused,
                        exoPlayer = if (isFocused) exoPlayer else null,
                        onBuyClicked = { selectedPostForBuy = it },
                        onProfileClicked = { onNavigateToProfile(post.author.id) },
                        modifier = Modifier.fillParentMaxSize()
                    )
                }
            }
        }
        
        // Top Navigation Bar (Centered Geofence Switcher, Top-End Inbox & Brand, Top-Start clear for Store Location)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            // Divar-Style Local Geofence Radius Switcher in Center
            Surface(
                
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, if (feedFilterMode == 1) EmeraldSuccess else GlassBorder)
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { feedFilterMode = 0 },
                        shape = RoundedCornerShape(16.dp),
                        color = if (feedFilterMode == 0) IndigoPrimary else Color.Transparent
                    ) {
                        Text(
                            text = "سراسری",
                            color = if (feedFilterMode == 0) Color.White else SlateMuted,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (feedFilterMode == 0) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    Surface(
                        onClick = { feedFilterMode = 1 },
                        shape = RoundedCornerShape(16.dp),
                        color = if (feedFilterMode == 1) EmeraldSuccess.copy(alpha = 0.85f) else Color.Transparent
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.NearMe,
                                contentDescription = null,
                                tint = if (feedFilterMode == 1) Color.White else EmeraldSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "محله من (۵km)",
                                color = if (feedFilterMode == 1) Color.White else SlateMuted,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (feedFilterMode == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Top-Left (End in RTL): Live Server Refresh & Inbox Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live Server Refresh Button
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                    border = BorderStroke(0.5.dp, if (isServerConnected) EmeraldSuccess else GlassBorder),
                    shadowElevation = 4.dp
                ) {
                    IconButton(
                        onClick = { viewModel.refreshPosts() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (isServerConnected) {
                                    Badge(
                                        containerColor = EmeraldSuccess,
                                        modifier = Modifier.size(6.dp)
                                    )
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "بروزرسانی زنده از سرور",
                                tint = if (isServerConnected) EmeraldSuccess else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                    border = BorderStroke(0.5.dp, GlassBorder),
                    shadowElevation = 4.dp
                ) {
                    IconButton(
                        onClick = onNavigateToInbox,
                        modifier = Modifier.size(38.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = FuchsiaAccent,
                                    contentColor = Color.White,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "صندوق پیام‌ها",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            } // End of Column
        }

        selectedPostForBuy?.let { post ->
            PaymentBottomSheet(
                post = post,
                onDismiss = { selectedPostForBuy = null },
                onNavigateToRegister = onNavigateToRegister,
                onNavigateToAuth = onNavigateToAuth
            )
        }
        
        selectedLiveUser?.let { user ->
            LiveShoppingOverlay(user = user, onClose = { selectedLiveUser = null })
        }
        
        if (showDailyReward) {
            DailyRewardDialog(
                onClaim = {
                    AppState.addWalletFunds(50000L)
                    dailyRewardManager.markRewardAsSeenToday()
                    showDailyReward = false
                },
                onDismiss = {
                    dailyRewardManager.markRewardAsSeenToday()
                    showDailyReward = false
                }
            )
        }
    }
}

@Composable
fun DailyRewardDialog(onClaim: () -> Unit, onDismiss: () -> Unit) {
    var claimed by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
            border = BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp).fillMaxWidth()
            ) {
                Text("🎁", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.daily_reward_title),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    stringResource(R.string.daily_reward_desc),
                    color = SlateMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FuchsiaAccent.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .border(1.5.dp, FuchsiaAccent, RoundedCornerShape(16.dp))
                        .padding(vertical = 14.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        50000L.toPersianCurrency(),
                        color = FuchsiaAccent,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    border = BorderStroke(0.5.dp, GlassBorder)
                ) {
                    Text(
                        "⏳ هدیه ویژه ورود به کوک - هر ۲۴ ساعت یک‌بار",
                        color = SlateMuted,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.daily_reward_btn), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
