package com.example.ui.map

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.example.data.MockData
import com.example.data.User
import com.example.ui.map.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToStore: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("همه") }
    var showOnlyLive by remember { mutableStateOf(false) }
    var showOnlyNearby by remember { mutableStateOf(false) }

    // Real geographic center coordinates for central/north Tehran
    val userCenterLat = 35.7650
    val userCenterLng = 51.3900

    // User simulated GPS center (Saadat Abad / Vanak corridor)
    val userLat = 35.7720
    val userLng = 51.3950

    // Map viewport transform states
    var mapOffsetX by remember { mutableStateOf(0f) }
    var mapOffsetY by remember { mutableStateOf(-30f) }
    var mapZoom by remember { mutableStateOf(1.0f) }

    // Active selected store
    val context = LocalContext.current
    val catalog = remember(context) { com.example.repository.CatalogRepository(context) }
    var allStores by remember { mutableStateOf(MockData.users.filter { it.location != null }) }
    LaunchedEffect(Unit) {
        val remote = catalog.getStores().filter { it.location != null }
        if (remote.isNotEmpty()) allStores = remote
    }
    var selectedStore by remember { mutableStateOf<User?>(null) }
    LaunchedEffect(allStores) {
        if (selectedStore == null) {
            selectedStore = allStores.firstOrNull { it.isLive } ?: allStores.firstOrNull()
        }
    }
    var showRouteModal by remember { mutableStateOf(false) }

    // Pulsing animation for live stores & user location
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha"
    )

    // Filtered stores based on search, category, live, and neighborhood
    val filteredStores = remember(searchQuery, selectedCategory, showOnlyLive, showOnlyNearby, allStores) {
        allStores.filter { store ->
            val matchQuery = searchQuery.isEmpty() ||
                store.name.contains(searchQuery, ignoreCase = true) ||
                store.location?.neighborhood?.contains(searchQuery, ignoreCase = true) == true ||
                store.location?.address?.contains(searchQuery, ignoreCase = true) == true

            val matchCategory = when (selectedCategory) {
                "همه" -> true
                "پوشاک" -> store.storeCategory.contains("پوشاک") || store.storeCategory.contains("شلوار") || store.storeCategory.contains("لباس")
                "کتونی" -> store.storeCategory.contains("کتونی") || store.storeCategory.contains("کفش")
                "ساعت" -> store.storeCategory.contains("ساعت") || store.storeCategory.contains("اکسسوری")
                "آرایشی" -> store.storeCategory.contains("آرایشی")
                else -> true
            }

            val matchLive = !showOnlyLive || store.isLive
            val matchNearby = !showOnlyNearby || (store.location?.neighborhood in listOf("سعادت‌آباد", "جردن / ولیعصر", "شهرک غرب"))

            matchQuery && matchCategory && matchLive && matchNearby
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(userCenterLat, userCenterLng))
        }
    }

    // Helper to center the map on a given coordinate
    fun centerOnLocation(targetLat: Double, targetLng: Double) {
        mapView.controller.animateTo(GeoPoint(targetLat, targetLng))
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        // 1. Dynamic Vector Map Layer & Interactive Store Pins
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                map.overlays.clear()
                
                val userMarker = Marker(map).apply {
                    position = GeoPoint(userLat, userLng)
                    title = "موقعیت من"
                    infoWindow = null
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                map.overlays.add(userMarker)
                
                filteredStores.forEach { store ->
                    store.location?.let { loc ->
                        val marker = Marker(map).apply {
                            position = GeoPoint(loc.latitude, loc.longitude)
                            title = store.name
                            infoWindow = null
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                selectedStore = store
                                centerOnLocation(loc.latitude, loc.longitude)
                                true
                            }
                        }
                        map.overlays.add(marker)
                    }
                }
                map.invalidate()
            },
            onRelease = { map ->
                map.onPause()
                map.onDetach()
            }
        )

        // 2. Top Header & Search Bar with Category Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Header Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.92f),
                border = BorderStroke(1.dp, GlassBorder),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, shape = RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .fluidAnimatedGradient(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    "نقشه فروشگاه‌های کوک",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "نمایش ${filteredStores.size} فروشگاه تایید شده",
                                    color = EmeraldSuccess,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Verified Store Guarantee Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IndigoPrimary.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "تایید لوکیشن",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Input Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "جستجوی فروشگاه یا محله (سعادت‌آباد، تجریش، جردن)...",
                                color = SlateMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = IndigoPrimary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "پاک کردن",
                                        tint = SlateMuted
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF090D16).copy(alpha = 0.85f),
                            unfocusedContainerColor = Color(0xFF090D16).copy(alpha = 0.85f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Live Stream Filter
                item {
                    FilterChip(
                        selected = showOnlyLive,
                        onClick = { showOnlyLive = !showOnlyLive },
                        label = {
                            Text(
                                "🔴 در حال لایو",
                                fontWeight = if (showOnlyLive) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FuchsiaAccent,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.85f),
                            labelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = showOnlyLive,
                            borderColor = if (showOnlyLive) FuchsiaAccent else GlassBorder
                        )
                    )
                }

                // Nearby Filter
                item {
                    FilterChip(
                        selected = showOnlyNearby,
                        onClick = { showOnlyNearby = !showOnlyNearby },
                        label = { Text("📍 نزدیک من") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldSuccess,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.85f),
                            labelColor = SlateText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = showOnlyNearby,
                            borderColor = if (showOnlyNearby) EmeraldSuccess else GlassBorder
                        )
                    )
                }

                // Categories
                val categories = listOf(
                    "همه" to Icons.Default.Storefront,
                    "پوشاک" to Icons.Default.Checkroom,
                    "کتونی" to Icons.Default.ShoppingBag,
                    "ساعت" to Icons.Default.Watch,
                    "آرایشی" to Icons.Default.Brush
                )

                items(categories) { (cat, icon) ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        leadingIcon = {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.85f),
                            labelColor = SlateText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) IndigoPrimary else GlassBorder
                        )
                    )
                }
            }
        }

        // 3. Floating Action Buttons (Re-center, Fit All, Zoom In/Out)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Re-center on My Location
            FloatingActionButton(
                onClick = {
                    centerOnLocation(userLat, userLng)
                    mapView.controller.setZoom(16.0)
                },
                modifier = Modifier.size(44.dp),
                containerColor = Color(0xFF1E293B).copy(alpha = 0.92f),
                contentColor = EmeraldSuccess,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "موقعیت من",
                    modifier = Modifier.size(22.dp)
                )
            }

            // Fit All Stores
            FloatingActionButton(
                onClick = {
                    centerOnLocation(userCenterLat, userCenterLng)
                    mapView.controller.setZoom(14.0)
                },
                modifier = Modifier.size(44.dp),
                containerColor = Color(0xFF1E293B).copy(alpha = 0.92f),
                contentColor = IndigoPrimary,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.CropFree,
                    contentDescription = "نمایش همه فروشگاه‌ها",
                    modifier = Modifier.size(22.dp)
                )
            }

            // Zoom In
            FloatingActionButton(
                onClick = { mapView.controller.zoomIn() },
                modifier = Modifier.size(44.dp),
                containerColor = Color(0xFF1E293B).copy(alpha = 0.92f),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "بزرگنمایی",
                    modifier = Modifier.size(22.dp)
                )
            }

            // Zoom Out
            FloatingActionButton(
                onClick = { mapView.controller.zoomOut() },
                modifier = Modifier.size(44.dp),
                containerColor = Color(0xFF1E293B).copy(alpha = 0.92f),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "کوچکنمایی",
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 4. Bottom Area: Store Preview Card or Quick Carousel (Raised to avoid floating bottom navigation bar)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Quick-access Carousel when no store is opened or as a companion
            AnimatedVisibility(
                visible = selectedStore == null && filteredStores.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                StoreMapCarousel(
                    stores = filteredStores,
                    selectedStoreId = selectedStore?.id,
                    onSelectStore = { store ->
                        selectedStore = store
                        store.location?.let { centerOnLocation(it.latitude, it.longitude) }
                    }
                )
            }

            // Selected Store Detail Preview Card
            AnimatedVisibility(
                visible = selectedStore != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                selectedStore?.let { store ->
                    StoreMapPreviewCard(
                        store = store,
                        userLat = userLat,
                        userLng = userLng,
                        onNavigateToStore = { onNavigateToStore(store.id) },
                        onNavigateRoute = { 
                            val loc = store.location
                            if (loc != null) {
                                val uri = android.net.Uri.parse("google.navigation:q=${loc.latitude},${loc.longitude}")
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val webUri = android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${loc.latitude},${loc.longitude}")
                                    val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, webUri)
                                    context.startActivity(webIntent)
                                }
                            }
                        },
                        onClose = { selectedStore = null }
                    )
                }
            }
        }

        // 5. Navigation Route Modal Dialog
        if (showRouteModal && selectedStore != null) {
            RouteDetailsDialog(
                store = selectedStore!!,
                userLat = userLat,
                userLng = userLng,
                onDismiss = { showRouteModal = false },
                onOpenStore = {
                    showRouteModal = false
                    onNavigateToStore(selectedStore!!.id)
                }
            )
        }
    }
}
