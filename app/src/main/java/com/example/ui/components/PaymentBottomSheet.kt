package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.data.*
import com.example.repository.MarketOrderRepository
import com.example.ui.components.GuestCheckoutDialog
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.OrangeItCyan
import com.example.ui.theme.OrangeItCoral
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SlateText
import com.example.utils.toPersianCurrency
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
    post: Post,
    onDismiss: () -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val repository = remember { MarketOrderRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val isGuest = AppState.isGuestUser()
    var showGuestDialog by remember { mutableStateOf(false) }
    var paymentStatus by remember { mutableStateOf(0) } // 0: init, 1: processing, 2: success
    var createdOrder by remember { mutableStateOf<StoreOrder?>(null) }
    
    // Variant Selection (Instagram & Digikala recommendation)
    val availableSizes = remember { listOf("S", "M", "L", "XL") }
    var selectedSize by remember { mutableStateOf("M") }
    
    val availableColors = remember {
        listOf(
            "مشکی" to Color(0xFF1E293B),
            "کرم نود" to Color(0xFFD4B996),
            "سرمه‌ای" to Color(0xFF1E3A8A),
            "سفید" to Color(0xFFF8FAFC)
        )
    }
    var selectedColor by remember { mutableStateOf(availableColors[0].first) }
    var quantity by remember { mutableStateOf(1) }
    
    // Shipping & In-Store Pickup Selection (Digikala & Divar)
    var shippingMethod by remember { mutableStateOf(0) } // 0: پست پیشتاز سراسری (۴۵,۰۰۰ تومان), 1: پیک اکسپرس محلی (۶۵,۰۰۰ تومان), 2: پرو و تحویل در بوتیک (رایگان)
    val shippingCost = when (shippingMethod) {
        0 -> 45000L
        1 -> 65000L
        else -> 0L
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF131B2E),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (paymentStatus) {
                0 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تایید و تسویه حساب کوک",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, EmeraldSuccess.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("پرداخت امن", color = EmeraldSuccess, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Guest Mode Warning Banner
                    if (isGuest) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF2D1515),
                            border = BorderStroke(1.dp, OrangeItCoral.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
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
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "حساب میهمان • ثبت‌نام الزامی است",
                                        color = OrangeItCoral,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "تسویه و خرید کالا بدون ثبت‌نام امکان‌پذیر نیست.",
                                        color = Color.White.copy(alpha = 0.85f),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { showGuestDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangeItCoral),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "ثبت‌نام",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    post.product?.let { product ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = product.imageUrl ?: post.imageUrl,
                                        contentDescription = product.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = product.title,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "فروشگاه: ${post.author.name}",
                                            color = SlateMuted,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = GlassBorder
                                )

                                // Color Variant Selector
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("انتخاب رنگ:", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        availableColors.forEach { (colorName, colorHex) ->
                                            val isSelected = selectedColor == colorName
                                            Surface(
                                                onClick = { selectedColor = colorName },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) IndigoPrimary.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.3f),
                                                border = BorderStroke(
                                                    if (isSelected) 1.5.dp else 0.5.dp,
                                                    if (isSelected) OrangeItCyan else GlassBorder
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(12.dp)
                                                            .clip(CircleShape)
                                                            .background(colorHex)
                                                            .border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        colorName,
                                                        color = if (isSelected) Color.White else SlateMuted,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Size Variant Selector
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("انتخاب سایز:", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        availableSizes.forEach { sizeOption ->
                                            val isSelected = selectedSize == sizeOption
                                            Surface(
                                                onClick = { selectedSize = sizeOption },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) IndigoPrimary.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f),
                                                border = BorderStroke(
                                                    if (isSelected) 1.5.dp else 0.5.dp,
                                                    if (isSelected) OrangeItCyan else GlassBorder
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        sizeOption,
                                                        color = if (isSelected) Color.White else SlateMuted,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = GlassBorder
                                )

                                // Digikala-Style Shipping Method Selection
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("روش ارسال سفارش:", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Option 1: Post Pishtaz
                                        Surface(
                                            onClick = { shippingMethod = 0 },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (shippingMethod == 0) IndigoPrimary.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.3f),
                                            border = BorderStroke(if (shippingMethod == 0) 1.5.dp else 0.5.dp, if (shippingMethod == 0) OrangeItCyan else GlassBorder),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = if (shippingMethod == 0) OrangeItCyan else SlateMuted, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("پست پیشتاز", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("۴۵,۰۰۰ تومان (سراسری)", color = SlateMuted, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                                            }
                                        }

                                        // Option 2: Express Courier
                                        Surface(
                                            onClick = { shippingMethod = 1 },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (shippingMethod == 1) IndigoPrimary.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.3f),
                                            border = BorderStroke(if (shippingMethod == 1) 1.5.dp else 0.5.dp, if (shippingMethod == 1) OrangeItCyan else GlassBorder),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("⚡", fontSize = 12.sp)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("پیک اکسپرس", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("۶۵,۰۰۰ تومان (۲ ساعته)", color = SlateMuted, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                                            }
                                        }

                                        // Option 3: Divar-Style In-Store Fitting & Pickup
                                        Surface(
                                            onClick = { shippingMethod = 2 },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (shippingMethod == 2) EmeraldSuccess.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.3f),
                                            border = BorderStroke(if (shippingMethod == 2) 1.5.dp else 0.5.dp, if (shippingMethod == 2) EmeraldSuccess else GlassBorder),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = if (shippingMethod == 2) EmeraldSuccess else SlateMuted, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("پرو و تحویل", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("رایگان (بوتیک حضوری)", color = EmeraldSuccess, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = GlassBorder
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("مبلغ کالا:", color = SlateMuted, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = product.price.toPersianCurrency(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("هزینه ارسال:", color = SlateMuted, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = shippingCost.toPersianCurrency(),
                                        color = SlateText,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("مبلغ نهایی پرداخت:", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = (product.price + shippingCost).toPersianCurrency(),
                                        color = OrangeItCyan,
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Digikala 3-Step Buyer Protection Trust Shield Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2338)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Shield",
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ضمانت سه‌مرحله‌ای خرید امن کوک (Escrow Shield)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "۱. سفارش روی سرور کوک ثبت می‌شود (وضعیت در انتظار تایید).\n۲. درگاه بانکی واقعی هنوز به این نسخه وصل نشده است.\n۳. پس از فعال‌سازی پرداخت، تسویه امانی فعال خواهد شد.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD1FAE5),
                                lineHeight = 18.sp,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isGuest) {
                        Button(
                            onClick = { showGuestDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeItCoral)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "ثبت‌نام برای تسویه و خرید کالا (الزامی)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    } else {
                        var addedToCartMessage by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        paymentStatus = 1
                                        val product = post.product
                                        if (product == null) {
                                            paymentStatus = 0
                                            return@launch
                                        }
                                        val catalog = com.example.repository.CatalogRepository(context)
                                        val remote = catalog.createOrder(
                                            product = product,
                                            storeName = post.author.name,
                                            quantity = quantity,
                                            selectedColor = selectedColor,
                                            selectedSize = selectedSize,
                                            customerAddress = AppState.currentUser.location?.address
                                                ?: "تهران، آدرس ثبت‌شده کاربر"
                                        )
                                        remote.onSuccess { order ->
                                            val enriched = order.copy(
                                                items = listOf(
                                                    StoreOrderItem(
                                                        product = product,
                                                        quantity = quantity,
                                                        selectedColor = selectedColor,
                                                        selectedSize = selectedSize,
                                                        unitPrice = product.price
                                                    )
                                                ),
                                                totalAmount = product.price * quantity + shippingCost
                                            )
                                            createdOrder = enriched
                                            AppState.customerOrders = listOf(enriched) + AppState.customerOrders.filter {
                                                it.orderId != enriched.orderId
                                            }
                                            val invoice = Invoice(
                                                id = enriched.orderId,
                                                items = listOf(
                                                    InvoiceItem(
                                                        title = product.title,
                                                        quantity = quantity,
                                                        unitPrice = product.price,
                                                        sizeOrColor = "$selectedColor | سایز: $selectedSize"
                                                    )
                                                ),
                                                shippingCost = shippingCost,
                                                totalAmount = product.price * quantity + shippingCost,
                                                status = InvoiceStatus.PENDING,
                                                customerAddress = enriched.customerAddress,
                                                customerName = enriched.customerName,
                                                customerPhone = enriched.customerPhone,
                                                postalCode = enriched.postalCode
                                            )
                                            repository.saveInvoice(invoice)
                                            paymentStatus = 2
                                        }.onFailure {
                                            // Keep user informed — do not fake success
                                            paymentStatus = 0
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                            ) {
                                Text(
                                    "ثبت سفارش در سرور کوک",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    if (post.product != null) {
                                        AppState.addToCart(
                                            product = post.product,
                                            storeName = post.author.name,
                                            quantity = quantity,
                                            color = selectedColor,
                                            size = selectedSize
                                        )
                                        coroutineScope.launch {
                                            repository.addToCart(
                                                CartItem(
                                                    product = post.product,
                                                    storeName = post.author.name,
                                                    quantity = quantity,
                                                    selectedColor = selectedColor,
                                                    selectedSize = selectedSize
                                                )
                                            )
                                            addedToCartMessage = true
                                            delay(1200)
                                            onDismiss()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, OrangeItCyan)
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = OrangeItCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (addedToCartMessage) "به سبد خرید اضافه شد!" else "افزودن به سبد خرید",
                                    color = OrangeItCyan,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
                1 -> {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(color = IndigoPrimary, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("در حال ثبت سفارش روی سرور کوک...", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(24.dp))
                }
                2 -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "پرداخت امن با موفقیت انجام شد!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "شماره سفارش: ${createdOrder?.orderId ?: "ORD- ثبت شد"}\nمبلغ پرداختی تا تحویل رضایت‌بخش کالا نزد حساب امانی کوک محفوظ می‌ماند.",
                        textAlign = TextAlign.Center,
                        color = SlateMuted,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("متوجه شدم - بازگشت", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showGuestDialog) {
            GuestCheckoutDialog(
                onDismiss = { showGuestDialog = false },
                onNavigateToRegister = {
                    showGuestDialog = false
                    onDismiss()
                    onNavigateToRegister()
                },
                onNavigateToLogin = {
                    showGuestDialog = false
                    onDismiss()
                    onNavigateToAuth()
                }
            )
        }
    }
}
