package com.example.ui.wallet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.repository.MarketOrderRepository
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.components.GuestCheckoutDialog
import com.example.ui.theme.*
import com.example.utils.toPersianCurrency
import com.example.utils.toPersianNumber

data class SurpriseRecommendation(
    val product: Product,
    val storeName: String,
    val matchScore: Int,
    val reason: String,
    val affinityTags: List<String>,
    val discountPercent: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateToRegister: () -> Unit = onNavigateToAuth
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { MarketOrderRepository(context) }
    val roomCartItems by repository.allCartItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val roomBookings by repository.allTryOnBookings.collectAsStateWithLifecycle(initialValue = emptyList())

    val currentMode = AppState.currentUserMode
    // Sync Room cart & bookings with AppState without wiping in-memory items before hydration
    LaunchedEffect(roomCartItems) {
        if (roomCartItems.isNotEmpty() || AppState.cartItems.isEmpty()) {
            AppState.cartItems = roomCartItems
        }
    }
    LaunchedEffect(roomBookings) {
        if (roomBookings.isNotEmpty() && AppState.customerBookings.isEmpty()) {
            AppState.customerBookings = roomBookings
        }
    }
    val cartItems = if (roomCartItems.isNotEmpty()) roomCartItems else AppState.cartItems
    var storeOrders by remember { mutableStateOf<List<StoreOrder>>(emptyList()) }
    val catalog = remember { com.example.repository.CatalogRepository(context) }

    LaunchedEffect(currentMode, AppState.isLoggedIn) {
        val remoteOrders = if (currentMode == UserMode.STORE) {
            catalog.getStoreOrders()
        } else {
            catalog.getOrdersForCurrentUser()
        }
        if (remoteOrders.isNotEmpty()) {
            val localOnly = AppState.customerOrders.filter { local ->
                remoteOrders.none { it.orderId == local.orderId }
            }
            storeOrders = if (currentMode == UserMode.STORE) remoteOrders else (localOnly + remoteOrders)
            if (currentMode != UserMode.STORE) {
                AppState.customerOrders = storeOrders
            }
        } else {
            storeOrders = if (currentMode == UserMode.STORE) {
                com.example.data.MockData.sampleStoreOrders.toList()
            } else {
                AppState.customerOrders
            }
        }
    }
    
    var customerSelectedTab by remember { mutableStateOf(0) }
    var storeSelectedTab by remember { mutableStateOf(0) }
    var showSurpriseDialog by remember { mutableStateOf(false) }
    var showGuestCheckoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title & Icon based on Customer vs Store
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentMode == UserMode.STORE) Icons.Default.Assignment else Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = if (currentMode == UserMode.STORE) FuchsiaAccent else IndigoPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentMode == UserMode.STORE) "سفارشات فروشگاه" else "سبد خرید من",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = SlateText
                            )
                        }

                        if (currentMode == UserMode.STORE) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FuchsiaAccent.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, FuchsiaAccent.copy(alpha = 0.3f)),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "پنل فروشگاه",
                                    color = FuchsiaAccent,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = SlateText
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (currentMode == UserMode.CUSTOMER) {
                // Customer Tabs: 0: Cart, 1: Orders, 2: Wallet & Coins
                TabRow(
                    selectedTabIndex = customerSelectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = IndigoPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[customerSelectedTab]),
                            color = IndigoPrimary
                        )
                    },
                    divider = { HorizontalDivider(color = GlassBorder) }
                ) {
                    Tab(
                        selected = customerSelectedTab == 0,
                        onClick = { customerSelectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("سبد خرید", fontWeight = FontWeight.Bold, color = if (customerSelectedTab == 0) IndigoPrimary else SlateMuted)
                                if (cartItems.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(IndigoPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(cartItems.size.toPersianNumber(), color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = customerSelectedTab == 1,
                        onClick = { customerSelectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("سفارشات من", fontWeight = FontWeight.Bold, color = if (customerSelectedTab == 1) IndigoPrimary else SlateMuted)
                                if (AppState.customerOrders.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldSuccess),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(AppState.customerOrders.size.toPersianNumber(), color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = customerSelectedTab == 2,
                        onClick = { customerSelectedTab = 2 },
                        text = {
                            Text("کیف پول و امتیازات", fontWeight = FontWeight.Bold, color = if (customerSelectedTab == 2) IndigoPrimary else SlateMuted)
                        }
                    )
                    Tab(
                        selected = customerSelectedTab == 3,
                        onClick = { customerSelectedTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("پرو و خدمات حضوری", fontWeight = FontWeight.Bold, color = if (customerSelectedTab == 3) IndigoPrimary else SlateMuted)
                                if (AppState.customerBookings.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(FuchsiaAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(AppState.customerBookings.size.toPersianNumber(), color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    )
                }

                when (customerSelectedTab) {
                    0 -> CustomerCartContent(
                        cartItems = cartItems,
                        onUpdateQuantity = { item, newQty ->
                            AppState.updateCartQuantity(item.product.id, newQty)
                            coroutineScope.launch {
                                repository.updateCartQuantity(item.product.id, newQty)
                            }
                        },
                        onRemoveItem = { item ->
                            AppState.removeFromCart(item.product.id)
                            coroutineScope.launch {
                                repository.removeFromCart(item.product.id)
                            }
                        },
                        onOpenSurpriseMe = { showSurpriseDialog = true },
                        onCheckoutOrder = {
                            if (AppState.isGuestUser()) {
                                showGuestCheckoutDialog = true
                                null
                            } else {
                                val order = AppState.checkoutCart()
                                if (order != null) {
                                    coroutineScope.launch {
                                        repository.clearCart()
                                    }
                                    customerSelectedTab = 1
                                }
                                order
                            }
                        },
                        onRequireRegistration = {
                            showGuestCheckoutDialog = true
                        }
                    )
                    1 -> CustomerOrdersSection(
                        orders = AppState.customerOrders,
                        onNavigateToChat = onNavigateToChat
                    )
                    2 -> CustomerWalletAndCoinsSection()
                    3 -> CustomerTryOnSection(
                        bookings = AppState.customerBookings,
                        onNavigateToChat = onNavigateToChat
                    )
                }
            } else {
                // ==================== حالت فروشگاه (سفارشات و اقلام فروش‌رفته) ====================
                StoreOrdersContent(
                    orders = storeOrders,
                    selectedSubTab = storeSelectedTab,
                    onSubTabChange = { storeSelectedTab = it },
                    onUpdateOrderStatus = { orderId, newStatus, trackingCode ->
                        storeOrders = storeOrders.map { order ->
                            if (order.orderId == orderId) {
                                order.copy(
                                    status = newStatus,
                                    trackingCode = trackingCode ?: order.trackingCode
                                )
                            } else order
                        }
                        coroutineScope.launch {
                            catalog.updateOrderStatus(orderId, newStatus, trackingCode)
                        }
                    },
                    onNavigateToChat = onNavigateToChat
                )
            }
        }

        if (showSurpriseDialog) {
            SurpriseMeDialog(
                onDismiss = { showSurpriseDialog = false },
                onAddToCart = { surpriseProduct, storeName ->
                    AppState.addToCart(surpriseProduct, storeName, 1)
                    showSurpriseDialog = false
                }
            )
        }

        if (showGuestCheckoutDialog) {
            GuestCheckoutDialog(
                onDismiss = { showGuestCheckoutDialog = false },
                onNavigateToRegister = {
                    showGuestCheckoutDialog = false
                    onNavigateToRegister()
                },
                onNavigateToLogin = {
                    showGuestCheckoutDialog = false
                    onNavigateToAuth()
                }
            )
        }
    }
}

// =========================================================================================
// بخش مشتری: سبد خرید و امکانات کامل خریدار
// =========================================================================================
@Composable
fun CustomerCartContent(
    cartItems: List<CartItem>,
    onUpdateQuantity: (CartItem, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    onOpenSurpriseMe: () -> Unit,
    onCheckoutOrder: () -> StoreOrder?,
    onRequireRegistration: () -> Unit = {}
) {
    val totalAmount = cartItems.sumOf { it.product.price * it.quantity }
    var placedOrder by remember { mutableStateOf<StoreOrder?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val isGuest = AppState.isGuestUser()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // "Surprise Me" Option at the Top
        item {
            SurpriseMeGlowingCard(onClick = onOpenSurpriseMe)
        }

        // Guest Mode Registration Reminder Banner
        if (isGuest) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF2D1515),
                    border = BorderStroke(1.2.dp, OrangeItCoral.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(OrangeItCoral.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = OrangeItCoral,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "حساب میهمان • ثبت‌نام الزامی است",
                                color = OrangeItCoral,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "تسویه حساب و خرید بدون ثبت‌نام امکان‌پذیر نیست. برای ثبت نشانی تحویل و صدور فاکتور، ثبت‌نام کنید.",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onRequireRegistration,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeItCoral),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "ثبت‌نام",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Cart items list or empty state
        if (cartItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GlassBackground),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("سبد خرید شما خالی است", color = SlateText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("از بخش ویترین و بازارگردی کالاها را مشاهده کنید و یا دکمه «منو سورپرایزم کن» را بزنید!", color = SlateMuted, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "کالاهای انتخابی شما (${cartItems.size.toPersianNumber()} کالا)",
                        color = SlateText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${cartItems.sumOf { it.quantity }.toPersianNumber()} عدد",
                        color = IndigoPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(cartItems) { item ->
                CartItemCard(
                    item = item,
                    onIncrease = { onUpdateQuantity(item, item.quantity + 1) },
                    onDecrease = { onUpdateQuantity(item, item.quantity - 1) },
                    onRemove = { onRemoveItem(item) }
                )
            }

            // Escrow Guarantee Badge
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldSuccess.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("خرید امن با حساب امانی کوک", color = EmeraldSuccess, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text("مبلغ پرداختی شما نزد صندوق امانی شاپرک کوک محفوظ می‌ماند و تنها پس از دریافت و تایید کالا به فروشگاه واریز می‌شود.", color = SlateMuted, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }

            // Checkout Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GlassBackground),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("خلاصه و پرداخت سفارش", color = SlateText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("مجموع اقلام:", color = SlateMuted)
                            Text(totalAmount.toPersianCurrency(), color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("هزینه ارسال پستی:", color = SlateMuted)
                            Text("رایگان (تخفیف ویژه)", color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = GlassBorder)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("مبلغ نهایی قابل پرداخت:", color = SlateText, fontWeight = FontWeight.Bold)
                            Text(totalAmount.toPersianCurrency(), color = FuchsiaAccent, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isGuest) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = OrangeItCoral.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, OrangeItCoral.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = OrangeItCoral,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "امکان تسویه خرید بدون ثبت‌نام میسر نیست.",
                                        color = OrangeItCoral,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = onRequireRegistration,
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeItCoral),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ثبت‌نام برای تسویه و پرداخت (الزامی)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = {
                                    val order = onCheckoutOrder()
                                    if (order != null) {
                                        placedOrder = order
                                        showSuccessDialog = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تکمیل خرید و پرداخت آنی (شاپرک)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(60.dp)) }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("ثبت موفق سفارش 🎉", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "سفارش شما با موفقیت پرداخت شد و برای فروشنده ارسال گردید.",
                        color = SlateMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "کد رهگیری سفارش: ${placedOrder?.orderId ?: "ORD-1092"}",
                        color = IndigoPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "می‌توانید وضعیت بسته را از تب «سفارشات من» پیگیری کنید.",
                        color = SlateMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("مشاهده در سفارشات من", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// =========================================================================================
// بخش پیگیری سفارشات مشتری (Customer Orders & Tracking)
// =========================================================================================
@Composable
fun CustomerOrdersSection(
    orders: List<StoreOrder>,
    onNavigateToChat: (String) -> Unit
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("هنوز سفارشی ثبت نکرده‌اید", color = SlateText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("محصولات دلخواه خود را از بخش ویترین به سبد خرید اضافه کنید و با تضمین امن کوک سفارش دهید.", color = SlateMuted, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    "تاریخچه سفارشات و رهگیری مرسولات (${orders.size.toPersianNumber()})",
                    color = SlateText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassBackground),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(order.orderId, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(order.orderedTimeAgo, color = SlateMuted, style = MaterialTheme.typography.bodySmall)
                            }
                            // Status Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (order.status) {
                                    OrderStatus.DELIVERED -> EmeraldSuccess.copy(alpha = 0.2f)
                                    OrderStatus.SHIPPED -> IndigoPrimary.copy(alpha = 0.2f)
                                    OrderStatus.PREPARING -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                    else -> SlateMuted.copy(alpha = 0.2f)
                                },
                                border = BorderStroke(1.dp, when (order.status) {
                                    OrderStatus.DELIVERED -> EmeraldSuccess.copy(alpha = 0.5f)
                                    OrderStatus.SHIPPED -> IndigoPrimary.copy(alpha = 0.5f)
                                    OrderStatus.PREPARING -> Color(0xFFF59E0B).copy(alpha = 0.5f)
                                    else -> SlateMuted.copy(alpha = 0.5f)
                                })
                            ) {
                                Text(
                                    text = when (order.status) {
                                        OrderStatus.DELIVERED -> "تحویل شده ✓"
                                        OrderStatus.SHIPPED -> "در حال حمل پستی 🚚"
                                        OrderStatus.PREPARING -> "آماده‌سازی سفارش"
                                        OrderStatus.PENDING -> "در انتظار بررسی فروشگاه"
                                        OrderStatus.EXCHANGE_REQUESTED -> "درخواست تعویض سایز 🔄"
                                        OrderStatus.CANCELLED -> "لغو شده"
                                    },
                                    color = when (order.status) {
                                        OrderStatus.DELIVERED -> EmeraldSuccess
                                        OrderStatus.SHIPPED -> IndigoPrimary
                                        OrderStatus.PREPARING -> Color(0xFFF59E0B)
                                        else -> SlateText
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = GlassBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Items
                        order.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.product.imageUrl ?: "https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=100&q=80",
                                    contentDescription = item.product.title,
                                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product.title, color = SlateText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("فروشگاه: ${order.storeName}", color = SlateMuted, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                                }
                                Text("${item.quantity.toPersianNumber()} × ${(item.product.price * item.quantity).toPersianCurrency()}", color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (!order.trackingCode.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("کد مرسوله پست پیشتاز:", color = SlateMuted, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(order.trackingCode ?: "", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "سامانه رهگیری مرسولات شرکت ملی پست (tracking.post.ir)",
                                            color = OrangeItCyan,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 11.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = EmeraldSuccess.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                "تحویل بیمه شده ✓",
                                                color = EmeraldSuccess,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("مبلغ پرداختی: ${order.totalAmount.toPersianCurrency()}", color = FuchsiaAccent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            OutlinedButton(
                                onClick = { onNavigateToChat("seller") },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("گفتگو با فروشنده", color = IndigoPrimary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

// =========================================================================================
// بخش کیف پول و امتیازات مشتری (Customer Wallet, Coins & Escrow Balance)
// =========================================================================================
@Composable
fun CustomerWalletAndCoinsSection() {
    var showTopUpDialog by remember { mutableStateOf(false) }
    var showBuyCoinsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF312E81), Color(0xFF1E1B4B))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("موجودی ریالی", color = SlateMuted, style = MaterialTheme.typography.bodyMedium)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = EmeraldSuccess.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
                            ) {
                                Text("فعال و متصل به شاپرک", color = EmeraldSuccess, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            AppState.customerWalletBalance.toPersianCurrency(),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showTopUpDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("افزایش موجودی ریالی", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Kook Coins Card (Live stream gifting & loyalty)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(28.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("سکه‌های طلایی (اعتبار هدیه)", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("${AppState.kookCoins.toPersianNumber()} سکه در دسترس", color = Color(0xFFFBBF24), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Button(
                        onClick = { showBuyCoinsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("خرید بسته سکه", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Escrow Explanation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = OrangeItCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("امنیت امانی کوک و Orange IT (Escrow)", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "در بازار کوک تمامی خریدها از طریق درگاه پرداخت امن شاپرک و با پشتوانه امانی کوک انجام می‌شوند. پول شما تا ۳ روز پس از تحویل کالا و رضایت کامل شما از محصول، در صندوق امانت حفظ می‌شود و در صورت هرگونه مغایرت، بلافاصله استرداد خواهد شد.",
                        color = SlateMuted,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Transaction History Header
        item {
            Text("گردش حساب و تراکنش‌های اخیر", color = SlateText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (AppState.transactions.isEmpty()) {
            item {
                Text("تراکنشی ثبت نشده است.", color = SlateMuted, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            items(AppState.transactions) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (tx.amount >= 0) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (tx.amount >= 0) EmeraldSuccess else FuchsiaAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(tx.title, color = SlateText, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("${tx.date} • کد رهگیری: ${tx.trackingCode}", color = SlateMuted, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                            }
                        }
                        Text(
                            text = (if (tx.amount > 0) "+" else "") + tx.amount.toPersianCurrency(),
                            color = if (tx.amount >= 0) EmeraldSuccess else Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(60.dp)) }
    }

    if (showTopUpDialog) {
        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("افزایش موجودی ریالی", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "درگاه بانکی واقعی هنوز به سرور متصل نیست. مبلغ انتخابی فقط به صورت محلی روی این دستگاه ثبت می‌شود و از API کیف‌پول سرور (فعلاً 403 برای USER) شارژ نمی‌شود.",
                        color = SlateMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    listOf(200000L, 500000L, 1000000L, 2000000L).forEach { amount ->
                        Button(
                            onClick = {
                                AppState.addWalletFunds(amount)
                                showTopUpDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GlassBackground),
                            border = BorderStroke(1.dp, IndigoPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(amount.toPersianCurrency(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTopUpDialog = false }) {
                    Text("انصراف", color = SlateMuted)
                }
            }
        )
    }

    if (showBuyCoinsDialog) {
        AlertDialog(
            onDismissRequest = { showBuyCoinsDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("خرید سکه‌های طلایی", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("سکه‌های طلایی برای اهدای گل، لایک ویژه و دونیت در لایو استریم‌ها استفاده می‌شوند:", color = SlateMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(
                        Triple(100, 50000L, "بسته پایه"),
                        Triple(300, 120000L, "بسته محبوب (۲۰٪ تخفیف)"),
                        Triple(1000, 350000L, "بسته ویژه استریمرها (۳۰٪ تخفیف)")
                    ).forEach { (coins, price, label) ->
                        Button(
                            onClick = {
                                AppState.kookCoins += coins
                                AppState.transactions = listOf(
                                    WalletTransaction(
                                        id = "TX-COIN-${System.currentTimeMillis()}",
                                        title = "خرید $coins سکه طلایی ($label)",
                                        amount = -price,
                                        isIncome = false,
                                        date = "لحظاتی پیش",
                                        status = "موفق",
                                        trackingCode = "SHP-${(100000..999999).random()}"
                                    )
                                ) + AppState.transactions
                                showBuyCoinsDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706).copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("$coins سکه • $label", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                                Text(price.toPersianCurrency(), color = Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBuyCoinsDialog = false }) {
                    Text("بستن", color = SlateMuted)
                }
            }
        )
    }
}

// =========================================================================================
// بخش فروشگاه: مدیریت سفارشات، اقلام فروش‌رفته، وضعیت سفارش و کیف پول
// =========================================================================================
@Composable
fun StoreOrdersContent(
    orders: List<StoreOrder>,
    selectedSubTab: Int,
    onSubTabChange: (Int) -> Unit,
    onUpdateOrderStatus: (String, OrderStatus, String?) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Sub tabs for store: 1. سفارشات دریافتی 2. اقلام فروش‌رفته و انبار 3. کیف پول و تسویه
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = FuchsiaAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = FuchsiaAccent
                )
            },
            divider = { HorizontalDivider(color = GlassBorder) }
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { onSubTabChange(0) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("سفارشات مشتریان", fontWeight = FontWeight.Bold, color = if (selectedSubTab == 0) FuchsiaAccent else SlateMuted)
                        Spacer(modifier = Modifier.width(4.dp))
                        val pendingCount = orders.count { it.status == OrderStatus.PENDING || it.status == OrderStatus.PREPARING }
                        if (pendingCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(FuchsiaAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(pendingCount.toPersianNumber(), color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { onSubTabChange(1) },
                text = { Text("اقلام فروش‌رفته و انبار", fontWeight = FontWeight.Bold, color = if (selectedSubTab == 1) FuchsiaAccent else SlateMuted) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { onSubTabChange(2) },
                text = { Text("کیف پول و تسویه", fontWeight = FontWeight.Bold, color = if (selectedSubTab == 2) FuchsiaAccent else SlateMuted) }
            )
            Tab(
                selected = selectedSubTab == 3,
                onClick = { onSubTabChange(3) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("اشتراک Pro", fontWeight = FontWeight.Bold, color = if (selectedSubTab == 3) Color(0xFFFBBF24) else SlateMuted)
                    }
                }
            )
        }

        when (selectedSubTab) {
            0 -> StoreOrdersListSection(orders = orders, onUpdateOrderStatus = onUpdateOrderStatus, onNavigateToChat = onNavigateToChat)
            1 -> StoreSoldItemsInventorySection(orders = orders)
            2 -> SellerWalletAndRevenueSection()
            3 -> StoreProSubscriptionSection()
        }
    }
}

@Composable
fun StoreOrdersListSection(
    orders: List<StoreOrder>,
    onUpdateOrderStatus: (String, OrderStatus, String?) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    var selectedFilterStatus by remember { mutableStateOf<OrderStatus?>(null) }
    var showTrackingDialogForOrder by remember { mutableStateOf<StoreOrder?>(null) }
    var showInvoiceDialogForOrder by remember { mutableStateOf<StoreOrder?>(null) }

    val filteredOrders = remember(orders, selectedFilterStatus) {
        if (selectedFilterStatus == null) orders else orders.filter { it.status == selectedFilterStatus }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Filter Chips for Orders Status
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilterStatus == null,
                        onClick = { selectedFilterStatus = null },
                        label = { Text("همه سفارشات (${orders.size.toPersianNumber()})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FuchsiaAccent,
                            selectedLabelColor = Color.White,
                            containerColor = GlassBackground,
                            labelColor = SlateText
                        )
                    )
                }
                items(OrderStatus.values()) { status ->
                    val count = orders.count { it.status == status }
                    FilterChip(
                        selected = selectedFilterStatus == status,
                        onClick = { selectedFilterStatus = if (selectedFilterStatus == status) null else status },
                        label = { Text("${status.title} (${count.toPersianNumber()})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(status.colorHex),
                            selectedLabelColor = Color.White,
                            containerColor = GlassBackground,
                            labelColor = SlateText
                        )
                    )
                }
            }
        }

        // Summary banner for store orders
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("سفارشات جدید", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(orders.count { it.status == OrderStatus.PENDING }.toPersianNumber(), color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = GlassBorder)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("در حال بسته‌بندی", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(orders.count { it.status == OrderStatus.PREPARING }.toPersianNumber(), color = Color(0xFF2196F3), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = GlassBorder)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ارسال شده", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(orders.count { it.status == OrderStatus.SHIPPED }.toPersianNumber(), color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("سفارشی در این وضعیت یافت نشد", color = SlateText, fontWeight = FontWeight.Bold)
                    if (orders.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "فقط سفارش‌های متعلق به فروشگاه شما نمایش داده می‌شود. فهرست عمومی سرور دیگر در اپ ذخیره یا نشان داده نمی‌شود.",
                            color = SlateMuted,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredOrders) { order ->
                StoreOrderCard(
                    order = order,
                    onStatusChange = { newStatus ->
                        if (newStatus == OrderStatus.SHIPPED) {
                            showTrackingDialogForOrder = order
                        } else {
                            onUpdateOrderStatus(order.orderId, newStatus, null)
                        }
                    },
                    onOpenInvoice = { showInvoiceDialogForOrder = order },
                    onChatClick = { onNavigateToChat("u1") }
                )
            }
        }
    }

    // Dialog to enter tracking code for shipping
    showTrackingDialogForOrder?.let { order ->
        var trackingInput by remember { mutableStateOf(order.trackingCode ?: "۱۹۸۲۷۳۶۴۵۰") }
        AlertDialog(
            onDismissRequest = { showTrackingDialogForOrder = null },
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("ثبت کد رهگیری پستی و ارسال 📦", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("سفارش ${order.orderId} برای مشتری «${order.customerName}» ارسال می‌شود.", color = SlateMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = trackingInput,
                        onValueChange = { trackingInput = it },
                        label = { Text("کد رهگیری مرسوله پستی / تیپاکس") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FuchsiaAccent,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = SlateText,
                            unfocusedTextColor = SlateText
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateOrderStatus(order.orderId, OrderStatus.SHIPPED, trackingInput)
                        showTrackingDialogForOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FuchsiaAccent)
                ) {
                    Text("تایید و تغییر به ارسال شده", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTrackingDialogForOrder = null }) {
                    Text("انصراف", color = SlateMuted)
                }
            }
        )
    }

    // Invoice & Shipping Label Dialog
    showInvoiceDialogForOrder?.let { order ->
        OrderInvoiceDialog(
            order = order,
            onDismiss = { showInvoiceDialogForOrder = null }
        )
    }
}

@Composable
fun StoreOrderCard(
    order: StoreOrder,
    onStatusChange: (OrderStatus) -> Unit,
    onOpenInvoice: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Order ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = FuchsiaAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(order.orderId, color = SlateText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("• ${order.orderedTimeAgo}", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(order.status.colorHex).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(order.status.colorHex).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = order.status.title,
                        color = Color(order.status.colorHex),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer Info Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = order.customerAvatar,
                    contentDescription = order.customerName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(order.customerName, color = SlateText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(order.customerPhone.toPersianNumber(), color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("مقصد: ${order.customerAddress}", color = SlateMuted, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                IconButton(
                    onClick = onChatClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(IndigoPrimary.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat", tint = IndigoPrimary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ordered Items List (اقلامی که مشتری سفارش داده است)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.product.imageUrl,
                            contentDescription = item.product.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.product.title, color = SlateText, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row {
                                item.selectedColor?.let { color ->
                                    Text("رنگ: $color", color = SlateMuted, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                item.selectedSize?.let { size ->
                                    Text("سایز: $size", color = SlateMuted, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("${item.quantity.toPersianNumber()} عدد", color = SlateText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text(item.unitPrice.toPersianCurrency(), color = FuchsiaAccent, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = GlassBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Total price and tracking info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("جمع کل فاکتور:", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                    Text(order.totalAmount.toPersianCurrency(), color = EmeraldSuccess, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                order.trackingCode?.let { track ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCode, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("کد پستی: ${track.toPersianNumber()}", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons for Store Owner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Secondary Action: Invoice
                OutlinedButton(
                    onClick = onOpenInvoice,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = SlateText, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("فاکتور و برچسب", color = SlateText, style = MaterialTheme.typography.labelSmall)
                }

                // Primary Stage Progression Action
                when (order.status) {
                    OrderStatus.PENDING -> {
                        Button(
                            onClick = { onStatusChange(OrderStatus.PREPARING) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.4f).height(40.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تایید و بسته‌بندی", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    OrderStatus.PREPARING -> {
                        Button(
                            onClick = { onStatusChange(OrderStatus.SHIPPED) },
                            colors = ButtonDefaults.buttonColors(containerColor = FuchsiaAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.4f).height(40.dp)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ثبت ارسال پستی", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    OrderStatus.SHIPPED -> {
                        Button(
                            onClick = { onStatusChange(OrderStatus.DELIVERED) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.4f).height(40.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تایید تحویل به مشتری", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    OrderStatus.DELIVERED -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1.4f).height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("سفارش با موفقیت تحویل شد ✅", color = EmeraldSuccess, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    OrderStatus.EXCHANGE_REQUESTED -> {
                        Button(
                            onClick = { onStatusChange(OrderStatus.SHIPPED) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.4f).height(40.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تایید تعویض و ارسال مجدد", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    OrderStatus.CANCELLED -> {
                        Text("سفارش لغو شده", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// =========================================================================================
// بخش گزارش اقلام فروش رفته و موجودی انبار فروشگاه
// =========================================================================================
@Composable
fun StoreSoldItemsInventorySection(orders: List<StoreOrder>) {
    val allSoldItems = remember(orders) {
        orders.flatMap { it.items }
            .groupBy { it.product.title }
            .map { (title, items) ->
                val totalQty = items.sumOf { it.quantity }
                val totalRevenue = items.sumOf { it.unitPrice * it.quantity }
                val sampleProduct = items.first().product
                Triple(sampleProduct, totalQty, totalRevenue)
            }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text("گزارش اقلام فروش‌رفته فروشگاه شما", color = SlateText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(allSoldItems) { (product, totalSold, totalRevenue) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.title, color = SlateText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("تعداد فروش رفته:", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${totalSold.toPersianNumber()} عدد", color = EmeraldSuccess, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("مجموع فروش: ${totalRevenue.toPersianCurrency()}", color = FuchsiaAccent, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                    }

                    // Stock status
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("موجودی انبار", color = SlateMuted, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp))
                            Text("۱۴ عدد".toPersianNumber(), color = SlateText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================================
// بخش کیف پول و تسویه حساب فروشگاه
// =========================================================================================
@Composable
fun SellerWalletAndRevenueSection() {
    var showWithdrawDialog by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Balance Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = IndigoPrimary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("موجودی قابل تسویه فروشگاه شما", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(18500000L.toPersianCurrency(), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showWithdrawDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = IndigoPrimary),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("درخواست تسویه آنی به شماره شبا", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Monthly Stats
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = FuchsiaAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("عملکرد فروش این ماه فروشگاه", color = SlateText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تعداد سفارشات موفق:", color = SlateMuted)
                        Text("۴۲ سفارش".toPersianNumber(), color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("کل فروش ناخالص ماه:", color = SlateMuted)
                        Text(24800000L.toPersianCurrency(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("کارمزد پلتفرم کوک (۱٪):", color = SlateMuted)
                        Text(248000L.toPersianCurrency(), color = FuchsiaAccent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("درخواست تسویه آنی پایا/ساتنا 🏦", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("مبلغ ۱۸,۵۰۰,۰۰۰ تومان به حساب شبای زیر واریز خواهد شد:", color = SlateMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("بانک ملت - به نام بوتیک کوک استایل", color = SlateText, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("IR640120000000001234567890", color = IndigoPrimary, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showWithdrawDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("تایید انتقال وجه", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("انصراف", color = SlateMuted)
                }
            }
        )
    }
}

// =========================================================================================
// بخش اشتراک ویژه فروشگاه (Store Pro Subscription & Monetization)
// =========================================================================================
@Composable
fun StoreProSubscriptionSection() {
    var isSubscribed by remember { mutableStateOf(false) }
    var showSubscribeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF78350F), Color(0xFF1E1B4B))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("پلن طلایی فروشگاه Pro", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSubscribed) EmeraldSuccess.copy(alpha = 0.25f) else Color(0xFFF59E0B).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, if (isSubscribed) EmeraldSuccess else Color(0xFFF59E0B))
                            ) {
                                Text(
                                    if (isSubscribed) "اشتراک فعال است ✓" else "طرح ویژه فروشندگان",
                                    color = if (isSubscribed) EmeraldSuccess else Color(0xFFFBBF24),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "با فعال‌سازی اشتراک Pro، فروشگاه خود را به عنوان فروشنده برگزیده و مطمئن به بیش از ۵۰,۰۰۰ خریدار فعال کوک معرفی کنید.",
                            color = SlateText,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { showSubscribeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isSubscribed) "مدیریت اشتراک طلایی" else "ارتقا به فروشگاه طلایی Pro (ماهیانه ۱۹۹ هزار تومان)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text("مزایای انحصاری فروشندگان طلایی کوک:", color = SlateText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        val perks = listOf(
            Triple(Icons.Default.Percent, "کارمزد پلتفرم صفر درصد (۰٪)", "هیچ کارمزدی از فروش شما کسر نمی‌شود و سود ۱۰۰٪ به کیف پول شما واریز می‌گردد."),
            Triple(Icons.Default.LocationOn, "سنجاق طلایی در نقشه محلی", "فروشگاه شما در نقشه محله و سرچ اطراف با نشان طلایی و اولویت اول نمایش داده می‌شود."),
            Triple(Icons.Default.LiveTv, "لایو استریم نامحدود بدون سقف", "برگزاری لایو فروش با ظرفیت نامحدود بینندگان و امکان قرار دادن بنر اختصاصی تخفیف."),
            Triple(Icons.Default.Verified, "تیک آبی تایید اصالت فروشگاه", "افزایش ۴ برابری اعتماد خریداران و افزایش نرخ تبدیل بازدید به خرید قطعی.")
        )

        items(perks) { (icon, title, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(desc, color = SlateMuted, style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(60.dp)) }
    }

    if (showSubscribeDialog) {
        AlertDialog(
            onDismissRequest = { showSubscribeDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("ارتقا به فروشگاه Pro 🌟", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "پلن ۳۰ روزه فروشگاه طلایی کوک با مبلغ ۱۹۹,۰۰۰ تومان از کیف پول یا درگاه مستقیم بانکی فعال می‌شود. آیا مایل به فعال‌سازی هستید؟",
                    color = SlateMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSubscribed = true
                        showSubscribeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("پرداخت و فعال‌سازی آنی", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubscribeDialog = false }) {
                    Text("انصراف", color = SlateMuted)
                }
            }
        )
    }
}

// =========================================================================================
// دیالوگ فاکتور و برچسب پستی فروشگاه
// =========================================================================================
@Composable
fun OrderInvoiceDialog(order: StoreOrder, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("فاکتور رسمی و برچسب مرسوله", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("فرستنده: بوتیک کوک استایل", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("شماره: ${order.orderId}", color = Color.DarkGray, fontSize = 11.sp)
                    }
                    HorizontalDivider(color = Color.LightGray, modifier = Modifier.padding(vertical = 6.dp))
                    Text("گیرنده: ${order.customerName} - ${order.customerPhone.toPersianNumber()}", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("نشانی: ${order.customerAddress}", color = Color.DarkGray, fontSize = 11.sp)
                    HorizontalDivider(color = Color.LightGray, modifier = Modifier.padding(vertical = 6.dp))
                    
                    order.items.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.product.title} (تعداد: ${item.quantity.toPersianNumber()})", color = Color.Black, fontSize = 11.sp)
                            Text(item.unitPrice.toPersianCurrency(), color = Color.Black, fontSize = 11.sp)
                        }
                    }
                    HorizontalDivider(color = Color.Black, modifier = Modifier.padding(vertical = 6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مبلغ کل پرداخت‌شده:", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(order.totalAmount.toPersianCurrency(), color = Color(0xFFE91E63), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("چاپ برچسب پستی", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =========================================================================================
// کارت درخشان و انیمیشنی «منو سورپرایزم کن!»
// =========================================================================================
@Composable
fun SurpriseMeGlowingCard(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "surprise_animation")
    
    // Rotating dice angle
    val diceRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dice_rotation"
    )

    // Shimmer reflection sweep position
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -400f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    // Pulsing scale for CTA button
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val cardBorderBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFF007F), // Vivid Pink
                Color(0xFF7C4DFF), // Violet
                Color(0xFFFF9100)  // Neon Orange
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .clickable { onClick() }
                .border(
                    width = 1.5.dp,
                    brush = cardBorderBrush,
                    shape = RoundedCornerShape(22.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140D24)),
            shape = RoundedCornerShape(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF22113D),
                                Color(0xFF10081E)
                            )
                        )
                    )
                    .drawWithContent {
                        drawContent()
                        val gleamWidth = 140f
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.55f),
                                    Color.White.copy(alpha = 0.35f),
                                    Color.Transparent
                                ),
                                start = Offset(shimmerOffset, 0f),
                                end = Offset(shimmerOffset + gleamWidth, size.height)
                            )
                        )
                    }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0xFFFF007F),
                                            Color(0xFF7C4DFF),
                                            Color(0xFFFFEA00)
                                        )
                                    )
                                )
                                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "🎲",
                                fontSize = 28.sp,
                                modifier = Modifier.rotate(diceRotation)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                "منو سورپرایزم کن!",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                "طبق رفتار و سلیقه شما",
                                color = Color(0xFFD6CEEE),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .scale(pulseScale)
                            .height(42.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFFF007F),
                                            Color(0xFF7C4DFF),
                                            Color(0xFFFF9100)
                                        )
                                    ),
                                    RoundedCornerShape(14.dp)
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "بزن بریم ⚡",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (item.isSurpriseGift) FuchsiaAccent.copy(alpha = 0.6f) else GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (item.isSurpriseGift) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(FuchsiaAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("✨ انتخاب شده با الگوریتم اختصاصی Orange IT", color = FuchsiaAccent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = item.product.imageUrl,
                    contentDescription = item.product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.product.title,
                        color = SlateText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "فروشگاه: ${item.storeName}",
                        color = SlateMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        item.product.price.toPersianCurrency(),
                        color = FuchsiaAccent,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black
                    )
                }

                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                        Icon(
                            if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = if (item.quantity == 1) Color(0xFFFF5252) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        item.quantity.toString().toPersianNumber(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SurpriseMeDialog(
    onDismiss: () -> Unit,
    onAddToCart: (Product, String) -> Unit
) {
    val recommendations = remember {
        listOf(
            SurpriseRecommendation(
                product = Product(
                    id = "prod_surp1",
                    title = "کتونی نایک ایر جردن ۱ شیکاگو",
                    price = 2450000,
                    oldPrice = 3500000,
                    imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1080&q=80"
                ),
                storeName = "دنیای کتونی",
                matchScore = 98,
                reason = "بر اساس لایک‌های مکرر شما روی ویدیوهای استریت‌استایل و جستجوی کتونی‌های ترند",
                affinityTags = listOf("#کتونی_اورجینال", "#استایل_خیابانی", "#تخفیف_ویژه"),
                discountPercent = 30
            ),
            SurpriseRecommendation(
                product = Product(
                    id = "prod_surp2",
                    title = "کت جین اورسایز وینتیج زاپ‌دار",
                    price = 980000,
                    oldPrice = 1400000,
                    imageUrl = "https://images.unsplash.com/photo-1516257984-b1b4d707412e?auto=format&fit=crop&w=1080&q=80"
                ),
                storeName = "خانه جین",
                matchScore = 95,
                reason = "بر اساس کامنت‌های تحسین‌آمیز شما درباره کالکشن‌های پاییزی و ذخیره استایل‌های کلاسیک",
                affinityTags = listOf("#کت_جین", "#وینتیج", "#تخفیف_سورپرایز"),
                discountPercent = 30
            ),
            SurpriseRecommendation(
                product = Product(
                    id = "prod_surp3",
                    title = "ساعت هوشمند الترا X با بند میلانس",
                    price = 1450000,
                    oldPrice = 2100000,
                    imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=1080&q=80"
                ),
                storeName = "ساعت لوکس",
                matchScore = 92,
                reason = "بر اساس مشاهده کامل لایو خرید و تعامل بالا با کالاهای مدرن",
                affinityTags = listOf("#اکسسوری", "#کالای_دیجیتال", "#پیشنهاد_OrangeIT"),
                discountPercent = 31
            )
        )
    }

    var currentIndex by remember { mutableStateOf(0) }
    val currentRecommendation = recommendations[currentIndex]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(FuchsiaAccent, IndigoPrimary)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎲", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("پیشنهاد ویژه Orange IT برای شما", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(EmeraldSuccess.copy(alpha = 0.2f), IndigoPrimary.copy(alpha = 0.2f))
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, EmeraldSuccess.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "سامانه پیشنهاد هوشمند Orange IT: ${currentRecommendation.matchScore.toString().toPersianNumber()}٪ تطابق با سلیقه شما",
                            color = EmeraldSuccess,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = currentRecommendation.product.imageUrl,
                        contentDescription = currentRecommendation.product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(FuchsiaAccent, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${currentRecommendation.discountPercent.toString().toPersianNumber()}٪ تخفیف سورپرایز",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    currentRecommendation.product.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "از فروشگاه: ${currentRecommendation.storeName}",
                    color = SlateMuted,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    currentRecommendation.product.oldPrice?.let { orig ->
                        Text(
                            orig.toPersianCurrency(),
                            color = SlateMuted,
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        currentRecommendation.product.price.toPersianCurrency(),
                        color = FuchsiaAccent,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GlassBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("🎯 چرا این کالا برای شما مناسب است؟", color = SlateText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentRecommendation.reason, color = SlateMuted, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            currentRecommendation.affinityTags.forEach { tag ->
                                Text(tag, color = IndigoPrimary, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onAddToCart(currentRecommendation.product, currentRecommendation.storeName) },
                    colors = ButtonDefaults.buttonColors(containerColor = FuchsiaAccent),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("افزودن به سبد خرید با تخفیف سورپرایز ⚡", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        currentIndex = (currentIndex + 1) % recommendations.size
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateText)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("یک پیشنهاد سورپرایز دیگه بده! 🎲", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// =========================================================================================
// بخش خدمات و نوبت‌های پرو حضوری اختصاصی خریدار (Customer Try-On & VIP Store Services)
// =========================================================================================
@Composable
fun CustomerTryOnSection(
    bookings: List<TryOnBooking>,
    onNavigateToChat: (String) -> Unit
) {
    var showBookDialog by remember { mutableStateOf(false) }
    var selectedStoreForBook by remember { mutableStateOf("بوتیک کوک استایل") }
    var productForBook by remember { mutableStateOf("هودی کلاه‌دار اورسایز") }
    var dateForBook by remember { mutableStateOf("فردا عصر ساعت ۱۸:۰۰") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. VIP In-Store Try-on Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(FuchsiaAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Checkroom, contentDescription = null, tint = FuchsiaAccent, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                "خدمات پرو و خرید حضوری اختصاصی",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "رزرو اتاق پرو در بوتیک، تست سایز و پرداخت پس از اطمینان",
                                color = SlateMuted,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f).padding(end = 6.dp)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تضمین اصالت کالا", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f).padding(start = 6.dp)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = FuchsiaAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("پرداخت امن شاپرک", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // 2. New Reservation Action Button
        item {
            Button(
                onClick = { showBookDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = FuchsiaAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("رزرو نوبت تست و پرو جدید در بوتیک", fontWeight = FontWeight.Bold)
            }
        }

        // 3. Bookings List
        if (bookings.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("هنوز نوبت پرو رزرو نشده است", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("می‌توانید برای هر محصولی از بوتیک‌های روی نقشه نوبت اختصاصی ثبت کنید.", color = SlateMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(bookings) { booking ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Store Info Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AsyncImage(
                                    model = booking.storeAvatar,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(36.dp).clip(CircleShape).border(1.dp, GlassBorder, CircleShape)
                                )
                                Column {
                                    Text(booking.storeName, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("فروشگاه معتبر کوک", color = EmeraldSuccess, fontSize = 10.sp)
                                    }
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, EmeraldSuccess)
                            ) {
                                Text(booking.status, color = EmeraldSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = GlassBorder)

                        // Product and Timing Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = booking.productImage,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(booking.productTitle, color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = FuchsiaAccent, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(booking.scheduledDate, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = FuchsiaAccent, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(booking.scheduledTime, color = SlateMuted, fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Reservation Code & Address
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("کد رزرو اختصاصی:", color = SlateMuted, fontSize = 10.sp)
                                    }
                                    Text(booking.bookingCode, color = IndigoPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(booking.storeAddress, color = SlateMuted, fontSize = 10.sp, lineHeight = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onNavigateToChat(booking.storeName) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("گفتگو با بوتیک", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    AppState.customerBookings = AppState.customerBookings.filter { it.id != booking.id }
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                            ) {
                                Text("لغو نوبت", fontSize = 11.sp, color = SlateMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    // Reservation Dialog
    if (showBookDialog) {
        AlertDialog(
            onDismissRequest = { showBookDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Checkroom, contentDescription = null, tint = FuchsiaAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("رزرو اختصاصی نوبت پرو حضوری", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("اطلاعات رزرو تست لباس و سایز در بوتیک:", color = SlateMuted, fontSize = 11.sp)

                    OutlinedTextField(
                        value = selectedStoreForBook,
                        onValueChange = { selectedStoreForBook = it },
                        label = { Text("نام فروشگاه / بوتیک") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = productForBook,
                        onValueChange = { productForBook = it },
                        label = { Text("نام محصول یا مدل") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = dateForBook,
                        onValueChange = { dateForBook = it },
                        label = { Text("تاریخ و ساعت ترجیحی شما") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AppState.bookTryOn(
                            storeName = selectedStoreForBook,
                            storeAvatar = "https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?auto=format&fit=crop&w=200&q=80",
                            productTitle = productForBook,
                            productImage = "https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=600&q=80",
                            date = dateForBook,
                            time = "ساعت هماهنگ‌شده",
                            address = "تهران، سعادت‌آباد، مجتمع تجاری سروستان، واحد ۱۲"
                        )
                        showBookDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FuchsiaAccent)
                ) {
                    Text("ثبت نوبت پرو", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookDialog = false }) {
                    Text("انصراف", color = SlateMuted)
                }
            }
        )
    }
}
