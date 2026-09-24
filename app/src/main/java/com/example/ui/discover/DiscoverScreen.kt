package com.example.ui.discover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import coil.compose.AsyncImage
import com.example.data.DivarTaxonomy
import com.example.data.AppState
import com.example.data.MockData
import com.example.data.Post
import com.example.data.User
import com.example.ui.feed.LiveShoppingOverlay
import com.example.ui.theme.*
import com.example.utils.toPersianCurrency
import com.example.utils.toPersianNumber
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(onNavigateToStore: (String) -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLiveUser by remember { mutableStateOf<User?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var filterState by remember { mutableStateOf(DivarFilterState()) }
    
    // Nearby stores based on selected province/cities
    val context = LocalContext.current
    val catalog = remember(context) { com.example.repository.CatalogRepository(context) }
    var liveStores by remember { mutableStateOf<List<User>>(emptyList()) }
    var livePosts by remember { mutableStateOf<List<Post>>(emptyList()) }

    LaunchedEffect(Unit) {
        liveStores = catalog.getStores().filter { it.id != AppState.currentUser.id }
        livePosts = catalog.getPosts()
    }

    val nearbyStores = remember(filterState.selectedCities, filterState.selectedProvince, liveStores) {
        liveStores.shuffled().take(3).ifEmpty {
            MockData.users.filter { it.id != "u_me" }.shuffled().take(3)
        }
    }
    
    // Recommended stores
    val recommendedStores = remember(liveStores) {
        liveStores.ifEmpty { MockData.users.filter { it.id != "u_me" } }
    }

    // Advanced search algorithm & filtering on Posts/Products
    val filteredPosts = remember(searchQuery, filterState, livePosts) {
        val source = livePosts.ifEmpty { MockData.posts }
        source.filter { post ->
            // Search query matching (title, description, tags, author)
            val matchesQuery = if (searchQuery.isBlank()) true else {
                post.description.contains(searchQuery, ignoreCase = true) ||
                (post.product?.title?.contains(searchQuery, ignoreCase = true) == true) ||
                post.author.name.contains(searchQuery, ignoreCase = true) ||
                post.author.username.contains(searchQuery, ignoreCase = true)
            }

            // Main Category filter
            val matchesMainCat = if (filterState.selectedMainCategory == null) true else {
                when (filterState.selectedMainCategory) {
                    "لوازم شخصی" -> post.description.contains("زنانه") || post.description.contains("پوشاک") || post.description.contains("ساعت") || post.description.contains("اکسسوری") || post.description.contains("کفش") || post.description.contains("کتونی") || post.description.contains("آرایشی")
                    "کالای دیجیتال" -> post.description.contains("موبایل") || post.description.contains("گوشی") || post.description.contains("اپل") || post.description.contains("سامسونگ") || post.description.contains("لپ‌تاپ") || post.description.contains("هدفون")
                    "وسایل نقلیه" -> post.description.contains("خودرو") || post.description.contains("موتور") || post.description.contains("ماشین") || post.description.contains("لاستیک")
                    "خانه و آشپزخانه" -> post.description.contains("خانه") || post.description.contains("مبل") || post.description.contains("فرش") || post.description.contains("آشپزخانه") || post.description.contains("لوستر")
                    "سرگرمی و فراغت" -> post.description.contains("بازی") || post.description.contains("کتاب") || post.description.contains("ورزش") || post.description.contains("موسیقی")
                    "تجهیزات و صنعتی" -> post.description.contains("ابزار") || post.description.contains("صنعتی") || post.description.contains("دریل")
                    "خدمات" -> post.description.contains("خدمات") || post.description.contains("تعمیر") || post.description.contains("آموزش")
                    else -> true
                }
            }

            // Subcategory filter
            val matchesSubCat = if (filterState.selectedSubCategories.isEmpty()) true else {
                filterState.selectedSubCategories.any { sub ->
                    val cleanSub = sub.replace("همه زیردسته‌های", "").trim()
                    cleanSub.isEmpty() || post.description.contains(cleanSub, ignoreCase = true) || (post.product?.title?.contains(cleanSub, ignoreCase = true) == true)
                }
            }

            // Min & Max Price filter
            val price = post.product?.price ?: 0L
            val matchesMinPrice = filterState.minPrice?.let { price >= it } ?: true
            val matchesMaxPrice = filterState.maxPrice?.let { price <= it } ?: true

            // Discount filter
            val matchesDiscount = if (filterState.onlyDiscounted) {
                post.product?.oldPrice != null && (post.product.oldPrice > (post.product.price))
            } else true

            // In Stock filter
            val matchesStock = if (filterState.onlyInStock) {
                post.product?.inStock != false
            } else true

            // Live/Video filter
            val matchesLiveOrVideo = if (filterState.onlyLiveOrVideo) {
                post.author.isLive || post.videoUrl != null
            } else true

            matchesQuery && matchesMainCat && matchesSubCat && matchesMinPrice && matchesMaxPrice && matchesDiscount && matchesStock && matchesLiveOrVideo
        }.let { list ->
            // Sorting algorithm (الگوریتم مرتب‌سازی پیشرفته دیوار)
            when (filterState.sortBy) {
                SortOption.NEWEST -> list
                SortOption.POPULAR -> list.sortedByDescending { it.likesCount + it.commentsCount * 5 }
                SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.product?.price ?: 0L }
                SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.product?.price ?: 0L }
                SortOption.DISCOUNT_PERCENT -> list.sortedByDescending {
                    val orig = it.product?.oldPrice ?: 0L
                    val curr = it.product?.price ?: 0L
                    if (orig > 0) ((orig - curr).toDouble() / orig) else 0.0
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search bar input
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("جستجو در کالاها و آگهی‌ها...", color = SlateMuted, style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SlateMuted) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = SlateMuted, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPrimary,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = SlateText,
                                unfocusedTextColor = SlateText,
                                cursorColor = IndigoPrimary,
                                focusedContainerColor = GlassBackground,
                                unfocusedContainerColor = GlassBackground
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Divar Advanced Filter Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (filterState.activeFiltersCount > 0) IndigoPrimary else GlassBackground,
                            border = BorderStroke(1.dp, if (filterState.activeFiltersCount > 0) IndigoPrimary else GlassBorder),
                            modifier = Modifier
                                .height(48.dp)
                                .clickable { showFilterSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filter",
                                    tint = if (filterState.activeFiltersCount > 0) Color.White else SlateText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (filterState.activeFiltersCount > 0) "فیلتر (${filterState.activeFiltersCount.toPersianNumber()})" else "فیلترها",
                                    color = if (filterState.activeFiltersCount > 0) Color.White else SlateText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Stories in Bazargardi
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    SectionHeader(title = "استوری‌های فروشگاه‌ها و لایو خرید", icon = Icons.Default.LocalFireDepartment, color = FuchsiaAccent)
                    DiscoverStoriesRow(
                        users = liveStores,
                        onStoryClicked = { user ->
                            if (user.isLive) {
                                selectedLiveUser = user
                            } else {
                                onNavigateToStore(user.id)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Active Location & Category Indicator (نوار وضعیت فیلتر شهر و دسته‌بندی)
                item {
                    // Premium Fashion & Footwear Brand Showcase Carousel
                    BrandShowcaseBannerCarousel(onNavigateToStore = onNavigateToStore)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Location pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = IndigoPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { showFilterSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                val locationLabel = if (filterState.selectedCities.isEmpty()) {
                                    filterState.selectedProvince
                                } else if (filterState.selectedCities.size == 1) {
                                    "${filterState.selectedProvince} (${filterState.selectedCities.first()})"
                                } else {
                                    "${filterState.selectedProvince} (${filterState.selectedCities.size.toPersianNumber()} شهر)"
                                }
                                Text(locationLabel, color = IndigoPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Category pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = FuchsiaAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, FuchsiaAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { showFilterSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Category, contentDescription = null, tint = FuchsiaAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                val catLabel = filterState.selectedMainCategory ?: "همه دسته‌ها"
                                Text(catLabel, color = FuchsiaAccent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Quick Category Filters (دسته‌بندی‌های اصلی دیوار)
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val isAllSelected = filterState.selectedMainCategory == null && !filterState.onlyDiscounted
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isAllSelected) IndigoPrimary else GlassBackground,
                                border = BorderStroke(1.dp, if (isAllSelected) IndigoPrimary else GlassBorder),
                                modifier = Modifier.clickable {
                                    filterState = filterState.copy(selectedMainCategory = null, selectedSubCategories = emptySet(), onlyDiscounted = false)
                                }
                            ) {
                                Text(
                                    text = "✨ همه دسته‌ها",
                                    color = if (isAllSelected) Color.White else SlateText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }

                        item {
                            val isDiscountSelected = filterState.onlyDiscounted
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isDiscountSelected) Color(0xFFE50914) else GlassBackground,
                                border = BorderStroke(1.dp, if (isDiscountSelected) Color(0xFFE50914) else GlassBorder),
                                modifier = Modifier.clickable {
                                    filterState = filterState.copy(onlyDiscounted = !filterState.onlyDiscounted)
                                }
                            ) {
                                Text(
                                    text = "🔥 حراجی‌ها",
                                    color = if (isDiscountSelected) Color.White else SlateText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isDiscountSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }

                        items(DivarTaxonomy.categories) { cat ->
                            val isSelected = filterState.selectedMainCategory == cat.name
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) IndigoPrimary else GlassBackground,
                                border = BorderStroke(1.dp, if (isSelected) IndigoPrimary else GlassBorder),
                                modifier = Modifier.clickable {
                                    filterState = if (filterState.selectedMainCategory == cat.name) {
                                        filterState.copy(selectedMainCategory = null, selectedSubCategories = emptySet())
                                    } else {
                                        filterState.copy(selectedMainCategory = cat.name, selectedSubCategories = emptySet())
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cat.icon, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat.name,
                                        color = if (isSelected) Color.White else SlateText,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // If filters or search is applied, show search results grid
                if (searchQuery.isNotBlank() || filterState.activeFiltersCount > 0) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "نتایج جستجو و فیلتر (${filteredPosts.size.toPersianNumber()} مورد)",
                                color = SlateText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "مرتب‌سازی: ${filterState.sortBy.title}",
                                color = IndigoPrimary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    if (filteredPosts.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔍", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("موردی با این مشخصات یافت نشد!", color = SlateText, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("فیلترها را تغییر دهید یا کلمه دیگری را جستجو کنید.", color = SlateMuted, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        searchQuery = ""
                                        filterState = DivarFilterState()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                                ) {
                                    Text("حذف فیلترها")
                                }
                            }
                        }
                    } else {
                        // Display search result cards (Divar Style)
                        items(filteredPosts) { post ->
                            DivarResultCard(
                                post = post,
                                onStoreClick = { onNavigateToStore(post.author.id) }
                            )
                        }
                    }
                } else {
                    // Default Discover Content
                    // Nearby Stores Section in Province/Cities
                    item {
                        val cityTitle = if (filterState.selectedCities.isNotEmpty()) filterState.selectedCities.first() else filterState.selectedProvince
                        SectionHeader(title = "فروشگاه‌های اطراف شما در $cityTitle", icon = Icons.Default.LocationOn, color = EmeraldSuccess)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(nearbyStores) { store ->
                                NearbyStoreItem(store = store, onClick = { onNavigateToStore(store.id) })
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    // Hot Posts Section
                    item {
                        SectionHeader(title = "پست‌های داغ منطقه (محبوب‌ترین‌ها)", icon = Icons.Default.LocalFireDepartment, color = FuchsiaAccent)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredPosts) { post ->
                                HotPostItem(post = post)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    // Recommended / Liked Stores Section
                    item {
                        SectionHeader(title = "پیشنهاد برای شما (بر اساس لایک‌ها)", icon = Icons.Default.ThumbUp, color = FuchsiaAccent)
                    }
                    
                    items(recommendedStores) { store ->
                        RecommendedStoreCard(store = store, onClick = { onNavigateToStore(store.id) })
                    }
                }
            }
            
            selectedLiveUser?.let { user ->
                LiveShoppingOverlay(user = user, onClose = { selectedLiveUser = null })
            }

            // Divar Filter Bottom Sheet
            if (showFilterSheet) {
                DivarFilterBottomSheet(
                    filterState = filterState,
                    onFilterChange = { filterState = it },
                    onDismiss = { showFilterSheet = false }
                )
            }
        }
    }
}

@Composable
fun DivarResultCard(post: Post, onStoreClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onStoreClick() },
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
            // Post/Product Image
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (post.product?.oldPrice != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(FuchsiaAccent, RoundedCornerShape(bottomEnd = 8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("حراج", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details (Divar style)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.product?.title ?: post.description,
                    color = SlateText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = post.author.name, color = SlateMuted, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(6.dp))

                post.product?.let { prod ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = prod.price.toPersianCurrency(),
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        prod.oldPrice?.let { old ->
                            Text(
                                text = old.toPersianCurrency(),
                                color = SlateMuted,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoverStoriesRow(
    onStoryClicked: (User) -> Unit,
    modifier: Modifier = Modifier,
    users: List<User> = MockData.users
) {
    val storyUsers = users.ifEmpty { MockData.users }
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DiscoverAddStoryItem(user = storyUsers.first())
        }
        items(storyUsers.drop(1).take(12)) { user ->
            DiscoverStoryItem(user = user, onClick = { onStoryClicked(user) })
        }
    }
}

@Composable
fun DiscoverAddStoryItem(user: User) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Box(modifier = Modifier.size(64.dp).clickable { /* Open camera/gallery */ }) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = "My Story",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
            ) {
                 Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(IndigoPrimary), contentAlignment = Alignment.Center) {
                     Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(12.dp))
                 }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("استوری من", color = SlateText, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun DiscoverStoryItem(user: User, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp).clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(2.dp, Brush.linearGradient(listOf(FuchsiaAccent, IndigoPrimary)), CircleShape)
                .padding(4.dp)
        ) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
            if (user.isLive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 6.dp)
                        .background(FuchsiaAccent, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(user.name, color = SlateText, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, color = SlateText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NearbyStoreItem(store: User, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(GlassBackground)
                .padding(2.dp)
        ) {
            AsyncImage(
                model = store.avatarUrl,
                contentDescription = store.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
            // Online/Distance indicator
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(EmeraldSuccess)
                    .padding(2.dp)
            ) {
                 Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.White))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = store.name,
            color = SlateText,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "۲.۵ کیلومتر",
            color = SlateMuted,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun RecommendedStoreCard(store: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column {
            // Store Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                AsyncImage(
                    model = store.bannerUrl ?: store.avatarUrl,
                    contentDescription = "Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, GlassBackground),
                                startY = 50f
                            )
                        )
                )
            }
            
            // Store Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                AsyncImage(
                    model = store.avatarUrl,
                    contentDescription = store.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f).padding(bottom = 4.dp)) {
                    Text(text = store.name, color = SlateText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = store.rating.toString().toPersianNumber(), color = SlateMuted, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "• ${store.followersCount.toPersianNumber()} دنبال‌کننده", color = SlateMuted, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            
            // Bio & Action
            Column(modifier = Modifier.padding(horizontal = 16.dp).offset(y = (-16).dp)) {
                store.bio?.let { bio ->
                    Text(text = bio, color = SlateText, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary.copy(alpha=0.2f), contentColor = IndigoPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("مشاهده فروشگاه", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HotPostItem(post: Post) {
    Box(
        modifier = Modifier
            .width(130.dp)
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AsyncImage(
            model = post.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Gradient overlay
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                startY = 150f
            )
        ))
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(text = post.description, color = Color.White, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = FuchsiaAccent, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = post.likesCount.toString().toPersianNumber(), color = SlateMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

data class BrandShowcase(
    val id: String,
    val brandName: String,
    val tagline: String,
    val badge: String,
    val bannerUrl: String,
    val discountText: String,
    val bgGradient: List<Color>
)

@Composable
fun BrandShowcaseBannerCarousel(onNavigateToStore: (String) -> Unit) {
    val showcases = remember {
        listOf(
            BrandShowcase(
                id = "brand_novin_leather",
                brandName = "نوین چرم | Novin Leather",
                tagline = "جشنواره فروش مستقیم کاپشن، کیف و کفش چرم طبیعی با ضمانت ۲ ساله",
                badge = "برند رسمی و تاییدشده",
                bannerUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=800&q=80",
                discountText = "تا ۳۵٪ تخفیف افتتاحیه ویترین",
                bgGradient = listOf(Color(0xFF831843), Color(0xFF1E1B4B))
            ),
            BrandShowcase(
                id = "brand_hakopian",
                brandName = "هاکوپیان | HAKOPIAN",
                tagline = "کالکشن کت و شلوارهای پشمی ایتالیایی و سفارش شخصی‌دوزی با نوبت پرو حضوری",
                badge = "اصالت پارچه ۱۰۰٪ پشم",
                bannerUrl = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80",
                discountText = "ارسال اکسپرس و کاور اختصاصی",
                bgGradient = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
            ),
            BrandShowcase(
                id = "brand_dorian_leather",
                brandName = "چرم درسا | Dorsa Group",
                tagline = "کلکسیون لوکس کیف‌های دستی و مجلسی دیبا؛ طراحی ماندگار با یراق‌آلات طلایی",
                badge = "ویترین برندهای مجلل",
                bannerUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?auto=format&fit=crop&w=800&q=80",
                discountText = "هدایای ویژه اعضای کوک",
                bgGradient = listOf(Color(0xFF701A75), Color(0xFF18181B))
            ),
            BrandShowcase(
                id = "brand_maral_leather",
                brandName = "مارال چرم | Maral Leather",
                tagline = "بوت‌ها و نیم‌بوت‌های زمستانه نبوک و چرم طبیعی با زیره تخصصی ضد لغزش",
                badge = "پرفروش‌ترین کفش فصل",
                bannerUrl = "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?auto=format&fit=crop&w=800&q=80",
                discountText = "تخفیف ویژه کالکشن زمستان",
                bgGradient = listOf(Color(0xFF451A03), Color(0xFF1E293B))
            ),
            BrandShowcase(
                id = "brand_bodyspinner_1",
                brandName = "بادی اسپینر | Body Spinner",
                tagline = "کاپشن‌های پافر اورسایز ضدآب و هودی‌های استریت‌استایل برای جوانان شیک‌پوش",
                badge = "استایل خیابانی مدرن",
                bannerUrl = "https://images.unsplash.com/photo-1544441893-675973e31985?auto=format&fit=crop&w=800&q=80",
                discountText = "تخفیف پاییزی تا ۴۰٪",
                bgGradient = listOf(Color(0xFF1E3A8A), Color(0xFF0284C7))
            )
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ویترین برندهای معتبر پوشاک و چرم",
                    color = SlateText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "مشاهده شعب",
                color = IndigoPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(showcases) { item ->
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .height(150.dp)
                        .clickable { onNavigateToStore(item.id) },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background Image with dark gradient overlay
                        AsyncImage(
                            model = item.bannerUrl,
                            contentDescription = item.brandName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            item.bgGradient[0].copy(alpha = 0.92f),
                                            item.bgGradient[1].copy(alpha = 0.75f),
                                            Color.Transparent
                                        ),
                                        startX = 0f,
                                        endX = 700f
                                    )
                                )
                        )

                        // Content Inside Card
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(item.badge, color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldSuccess
                                ) {
                                    Text(
                                        text = item.discountText,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = item.brandName,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.tagline,
                                    color = Color.White.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = "ورود به غرفه برند",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.ArrowBack, // In RTL arrow points back/left
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

