package com.example.ui.map.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.User
import com.example.ui.components.tactileStoreCard
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun StoreMapPreviewCard(
    store: User,
    userLat: Double,
    userLng: Double,
    onNavigateToStore: () -> Unit,
    onNavigateRoute: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loc = store.location
    val distanceKm = remember(store.id, userLat, userLng) {
        val latDiff = abs(userLat - (loc?.latitude ?: userLat))
        val lngDiff = abs(userLng - (loc?.longitude ?: userLng))
        val dist = sqrt(latDiff.pow(2) + lngDiff.pow(2)) * 111.0
        String.format("%.1f", max(0.4, dist))
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        border = BorderStroke(1.dp, GlassBorder),
        shadowElevation = 24.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row with Close button & Live Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (store.isLive) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FuchsiaAccent,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Text(
                                    "پخش زنده فعال",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Text(
                            text = store.storeCategory,
                            color = IndigoPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = "$distanceKm کیلومتر فاصله",
                        color = EmeraldSuccess,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = SlateMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Store Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Store Avatar
                Box(modifier = Modifier.size(60.dp)) {
                    AsyncImage(
                        model = store.avatarUrl,
                        contentDescription = store.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                2.dp,
                                if (store.isLive) FuchsiaAccent else IndigoPrimary,
                                RoundedCornerShape(16.dp)
                            )
                    )
                    if (store.isVerified) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(18.dp)
                                .background(Color(0xFF0F172A), CircleShape)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = store.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "★ ${store.rating}",
                            color = Color(0xFFFFB300),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("•", color = SlateMuted)
                        Text(
                            loc?.neighborhood ?: "تهران",
                            color = SlateText,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text("•", color = SlateMuted)
                        Text(
                            store.openHours,
                            color = SlateMuted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Verified Address Line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = FuchsiaAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = loc?.address ?: "آدرس تایید شده فروشگاه",
                            color = SlateMuted,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: View Profile Store & Route Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Route Navigation Button
                Button(
                    onClick = onNavigateRoute,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Icon(
                        Icons.Default.Directions,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "مسیریابی 🚗",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // View Store Showcase
                Button(
                    onClick = onNavigateToStore,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Icon(
                        Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "ورود به ویترین فروشگاه",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Horizontal Quick-Access Store Carousel
@Composable
fun StoreMapCarousel(
    stores: List<User>,
    selectedStoreId: String?,
    onSelectStore: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(stores, key = { it.id }) { store ->
            val isSelected = store.id == selectedStoreId
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Color(0xFF1E1B4B) else Color(0xFF0F172A).copy(alpha = 0.92f),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (store.isLive) FuchsiaAccent else if (isSelected) Color.White else GlassBorder
                ),
                modifier = Modifier
                    .width(180.dp)
                    .tactileStoreCard(
                        pressedScale = 0.945f,
                        defaultElevation = if (isSelected) 8.dp else 3.dp,
                        pressedElevation = 14.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = if (store.isLive) FuchsiaAccent else IndigoPrimary,
                        onClick = { onSelectStore(store) }
                    )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(40.dp)) {
                        AsyncImage(
                            model = store.avatarUrl,
                            contentDescription = store.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    if (store.isLive) FuchsiaAccent else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                        )
                        if (store.isLive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(FuchsiaAccent)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = store.name,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = store.location?.neighborhood ?: store.storeCategory,
                            color = SlateMuted,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "${store.rating}",
                                color = Color(0xFFFFB300),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Navigation & Turn-by-Turn Route Dialog
@Composable
fun RouteDetailsDialog(
    store: User,
    userLat: Double,
    userLng: Double,
    onDismiss: () -> Unit,
    onOpenStore: () -> Unit
) {
    val loc = store.location
    val context = LocalContext.current

    val distanceKm = remember(store.id, userLat, userLng) {
        val latDiff = abs(userLat - (loc?.latitude ?: userLat))
        val lngDiff = abs(userLng - (loc?.longitude ?: userLng))
        val dist = sqrt(latDiff.pow(2) + lngDiff.pow(2)) * 111.0
        String.format("%.1f", max(0.4, dist))
    }

    val estimatedMinutes = remember(distanceKm) {
        val km = distanceKm.toDoubleOrNull() ?: 3.0
        val mins = (km * 3.2).roundToInt().coerceIn(5, 45)
        "$mins دقیقه (ترافیک عادی)"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
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
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("شروع مسیریابی 🚗")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Text("بستن", color = SlateText)
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Navigation,
                    contentDescription = null,
                    tint = EmeraldSuccess
                )
                Text(
                    "مسیریابی مستقیم Orange IT به ${store.name}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "مدت زمان تخمینی با خودرو:",
                                color = SlateMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                estimatedMinutes,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        HorizontalDivider(color = GlassBorder, thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "فاصله مسافتی:",
                                color = SlateMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "$distanceKm کیلومتر",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        HorizontalDivider(color = GlassBorder, thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "ساعت کاری فروشگاه:",
                                color = SlateMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                store.openHours,
                                color = Color(0xFFFFB300),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Exact Store Address
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "آدرس رسمی و تایید شده:",
                        color = SlateMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        loc?.address ?: "تهران، آدرس ثبت شده در کوک",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Phone & Contact
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${store.phone}")
                            }
                            context.startActivity(intent)
                        }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            store.phone,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        "تماس مستقیم 📞",
                        color = IndigoPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}
