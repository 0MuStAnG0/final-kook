package com.example.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import com.example.utils.toPersianCurrency
import com.example.utils.toPersianNumber

@Composable
fun ProfileScreen(
    userId: String? = null,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToStoreApplication: () -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToStore: (String) -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateToRegister: () -> Unit = onNavigateToAuth
) {
    val isMe = userId == null || userId == AppState.currentUser.id || userId == "u_me" || userId == "user_me_01" || userId == "user_guest"
    val isGuest = !AppState.isLoggedIn || AppState.currentUser.id == "user_guest"

    // A guest user should not have a profile; when entering profile, ask them to register
    if (isMe && isGuest) {
        GuestProfilePrompt(
            onNavigateToRegister = onNavigateToRegister,
            onNavigateToLogin = onNavigateToAuth,
            onNavigateToSettings = onNavigateToSettings
        )
        return
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val catalog = remember(context) { com.example.repository.CatalogRepository(context) }
    var remoteUser by remember { mutableStateOf<User?>(null) }
    var posts by remember { mutableStateOf<List<Post>>(MockData.profilePosts.toList()) }
    var followedStores by remember { mutableStateOf<List<User>>(MockData.users.filter { it.isSeller }) }

    LaunchedEffect(userId, AppState.isLoggedIn) {
        if (!isMe && userId != null) {
            remoteUser = catalog.findUser(userId)
        }
        val allPosts = catalog.getPosts()
        posts = if (isMe) {
            allPosts.filter { it.author.id == AppState.currentUser.id }.ifEmpty { MockData.profilePosts }
        } else {
            allPosts.filter { it.author.id == userId }.ifEmpty { MockData.profilePosts }
        }
        followedStores = catalog.getStores().filter { it.isSeller }.ifEmpty { MockData.users.filter { it.isSeller } }
    }

    val user = if (isMe) AppState.currentUser else (remoteUser ?: MockData.users.find { it.id == userId } ?: MockData.currentUser)
    val savedPosts = posts
    var selectedTab by remember { mutableStateOf(0) }
    
    val isSellerMode = if (isMe) (AppState.currentUser.isSeller || AppState.currentUser.accountType == AccountType.STORE_OWNER) else (user.isSeller || user.location != null || user.rating > 0f)
    var storeSearchQuery by remember { mutableStateOf("") }

    val filteredFollowedStores = remember(storeSearchQuery, followedStores) {
        if (storeSearchQuery.isEmpty()) {
            followedStores
        } else {
            followedStores.filter { store ->
                store.name.contains(storeSearchQuery, ignoreCase = true) ||
                store.username.contains(storeSearchQuery, ignoreCase = true) ||
                store.storeCategory.contains(storeSearchQuery, ignoreCase = true)
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 90.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item(span = { GridItemSpan(3) }) {
            Column {
                ProfileHeader(
                    user = user,
                    posts = posts,
                    isSellerMode = isSellerMode,
                    isMe = isMe,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToStoreApplication = onNavigateToStoreApplication,
                    onNavigateToChat = onNavigateToChat,
                    onNavigateToAuth = onNavigateToAuth
                )

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = SlateText,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = if (isSellerMode) FuchsiaAccent else IndigoPrimary
                        )
                    },
                    divider = { HorizontalDivider(color = GlassBorder) }
                ) {
                    if (isSellerMode) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("ویترین محصولات", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("ویدیوها و تیزرها", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                    } else {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("ویدیوهای من", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("فروشگاه‌های محبوب", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("نشان‌شده‌ها", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }

        if (isSellerMode) {
            val displayPosts = if (selectedTab == 1) posts.filter { it.videoUrl != null } else posts
            items(displayPosts) { post ->
                ProfileGridItem(post, isStoreView = selectedTab == 0, isSellerMode = true)
            }
        } else {
            when (selectedTab) {
                0 -> {
                    items(posts) { post ->
                        ProfileGridItem(post, isStoreView = false, isSellerMode = false)
                    }
                }
                1 -> {
                    item(span = { GridItemSpan(3) }) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                            OutlinedTextField(
                                value = storeSearchQuery,
                                onValueChange = { storeSearchQuery = it },
                                placeholder = { Text("جستجوی در فروشگاه‌های محبوب...", color = SlateMuted, style = MaterialTheme.typography.bodyMedium) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = IndigoPrimary) },
                                trailingIcon = {
                                    if (storeSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { storeSearchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = SlateMuted)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoPrimary,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                                    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )

                            if (filteredFollowedStores.isEmpty()) {
                                Spacer(modifier = Modifier.height(32.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("فروشگاهی پیدا نشد", color = SlateMuted, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }

                    items(filteredFollowedStores, span = { GridItemSpan(3) }) { store ->
                        FavoriteStoreItem(
                            store = store,
                            onClick = { onNavigateToStore(store.id) }
                        )
                    }
                }
                2 -> {
                    items(savedPosts) { post ->
                        ProfileGridItem(post, isStoreView = true, isSellerMode = false)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    user: User,
    posts: List<Post>,
    isSellerMode: Boolean,
    isMe: Boolean,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToStoreApplication: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToAuth: () -> Unit = {}
) {
    var showRatingDetails by remember { mutableStateOf(false) }
    var show360Panorama by remember { mutableStateOf(false) }
    val dynamicRating = remember(posts) { calculateStoreRating(posts) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            AsyncImage(
                model = user.bannerUrl ?: user.avatarUrl,
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha=0.5f), Color.Transparent, MaterialTheme.colorScheme.background),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            // Settings Icon
            if (isMe) {
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "تنظیمات", tint = Color.White)
                }
            }
        }
        
        // Avatar and Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(92.dp)) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                )
                if (isSellerMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(FuchsiaAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                if (user.isVerified || isSellerMode) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.CheckCircle, contentDescription = "تایید شده", tint = if (isSellerMode) FuchsiaAccent else IndigoPrimary, modifier = Modifier.size(20.dp))
                }
            }
            Text(text = "@${user.username}", style = MaterialTheme.typography.bodyMedium, color = SlateMuted)
            
            // Location & Interests Chips
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(user.city, color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                if (isSellerMode) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FuchsiaAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, FuchsiaAccent.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = FuchsiaAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(user.storeCategory, color = FuchsiaAccent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tastes / Interests card for Regular User
            if (!isSellerMode && user.interests.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = FuchsiaAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("علایق و سلیقه ثبت‌شده:", color = SlateMuted, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                            Text(user.interests, color = SlateText, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            user.bio?.let { bio ->
                Text(text = bio, style = MaterialTheme.typography.bodySmall, color = SlateText, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem("دنبال‌کننده", user.followersCount.toPersianNumber())
                HorizontalDivider(modifier = Modifier.height(20.dp).width(1.dp), color = GlassBorder)
                if (isSellerMode) {
                    StatItem("فروش موفق", "۱,۲۴۰")
                    HorizontalDivider(modifier = Modifier.height(20.dp).width(1.dp), color = GlassBorder)
                    StatItem("امتیاز", "${dynamicRating.toString().toPersianNumber()} ⭐️", modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { showRatingDetails = true })
                } else {
                    StatItem("دنبال‌شده‌ها", user.followingCount.toPersianNumber())
                    HorizontalDivider(modifier = Modifier.height(20.dp).width(1.dp), color = GlassBorder)
                    StatItem("نشان‌شده‌ها", "۴")
                }
            }

            if (showRatingDetails) {
                RatingDetailsDialog(
                    rating = dynamicRating,
                    posts = posts,
                    onDismiss = { showRatingDetails = false }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== STORE REGISTRATION CTA FOR REGULAR USER ====================
            if (isMe && !isSellerMode) {
                val app = AppState.currentUser.storeApplication
                if (app == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToStoreApplication() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassBackground),
                        border = BorderStroke(1.dp, Brush.linearGradient(listOf(IndigoPrimary, FuchsiaAccent)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(IndigoPrimary, FuchsiaAccent))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("ثبت فروشگاه و شروع فروش در کوک", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("ارسال مدارک، دریافت تیک تایید و ویترین اختصاصی", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = FuchsiaAccent)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToStoreApplication() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (app.status == StoreApplicationStatus.APPROVED) EmeraldSuccess.copy(alpha = 0.15f) else Color(0xFFFF9800).copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, if (app.status == StoreApplicationStatus.APPROVED) EmeraldSuccess else Color(0xFFFF9800))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (app.status == StoreApplicationStatus.APPROVED) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = if (app.status == StoreApplicationStatus.APPROVED) EmeraldSuccess else Color(0xFFFF9800)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (app.status == StoreApplicationStatus.APPROVED) "فروشگاه شما تایید شد ✓" else "مدارک فروشگاه در حال بررسی است",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text("جهت مشاهده وضعیت و فعال‌سازی ضربه بزنید", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SlateMuted)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Actions Buttons
            if (isSellerMode) {
                Button(
                    onClick = { show360Panorama = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FuchsiaAccent.copy(alpha = 0.15f),
                        contentColor = FuchsiaAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, FuchsiaAccent)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = "360 View", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تور ۳۶۰ درجه", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isMe) {
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Follow", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("دنبال کردن", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onNavigateToChat(user.id) },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassBackground, contentColor = SlateText),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Message", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ارسال پیام", fontWeight = FontWeight.Bold)
                    }
                } else if (isSellerMode) {
                    Button(
                        onClick = onNavigateToEditProfile,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassBackground, contentColor = SlateText),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ویرایش ویترین", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { /* Boost */ },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FuchsiaAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = "Ads", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تبلیغ کالا", fontWeight = FontWeight.Bold)
                    }
                } else if (!AppState.isLoggedIn) {
                    Button(
                        onClick = onNavigateToAuth,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ورود به حساب کاربری / ثبت‌نام", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onNavigateToEditProfile,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassBackground, contentColor = SlateText),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ویرایش مشخصات", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            if (show360Panorama) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { show360Panorama = false },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        val scrollState = rememberScrollState()
                        
                        // Fake a wide panorama image using horizontal scroll
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?q=80&w=2500&auto=format&fit=crop",
                            contentDescription = "Panorama 360",
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(scrollState)
                        )
                        
                        // Overlay instructions
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(32.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "برای مشاهده اطراف، تصویر را به چپ و راست بکشید",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Close button
                        IconButton(
                            onClick = { show360Panorama = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.padding(horizontal = 8.dp)) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = SlateMuted)
    }
}

@Composable
fun ProfileGridItem(post: Post, isStoreView: Boolean, isSellerMode: Boolean) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Color(0xFF1E293B))
    ) {
        AsyncImage(
            model = post.imageUrl,
            contentDescription = post.description,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        if (isStoreView && post.product != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 100f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
            ) {
                Text(
                    text = post.product.title,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = post.product.price.toPersianCurrency(),
                    color = EmeraldSuccess,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FavoriteStoreItem(store: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = store.avatarUrl,
                contentDescription = store.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(1.dp, FuchsiaAccent, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(store.name, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    if (store.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FuchsiaAccent, modifier = Modifier.size(16.dp))
                    }
                }
                Text(store.storeCategory + " • " + store.city, color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                Text("⭐ ${store.rating.toString().toPersianNumber()} رضایت خریداران", color = Color(0xFFFFB300), style = MaterialTheme.typography.labelSmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SlateMuted)
        }
    }
}

private fun calculateStoreRating(posts: List<Post>): Float = 4.9f

@Composable
fun RatingDetailsDialog(rating: Float, posts: List<Post>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("امتیاز و رضایت خریداران", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Text("امتیاز میانگین: $rating از ۵", color = Color(0xFFFFB300), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("محاسبه شده بر اساس ۱,۲۴۰ نظر ثبت‌شده پس از تحویل کالا با ضمانت Orange IT", color = SlateMuted, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("بستن")
                }
            }
        }
    }
}
