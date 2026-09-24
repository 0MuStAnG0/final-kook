package com.example.ui.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen() {
    var title by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var autoSlug by remember { mutableStateOf(true) }
    
    var excerpt by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    
    var category by remember { mutableStateOf("بدون دسته‌بندی") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    
    var price by remember { mutableStateOf("") }
    var discountedPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    
    var brand by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var gtin by remember { mutableStateOf("") }

    // Mandatory Store Location Fields
    val currentUserLoc = com.example.data.AppState.currentUser.location ?: com.example.data.MockData.currentUser.location
    var province by remember { mutableStateOf(currentUserLoc?.province ?: "تهران") }
    var city by remember { mutableStateOf(currentUserLoc?.city ?: "تهران") }
    var neighborhood by remember { mutableStateOf(currentUserLoc?.neighborhood ?: "سعادت‌آباد") }
    var address by remember { mutableStateOf(currentUserLoc?.address ?: "تهران، سعادت‌آباد، میدان کاج، مجتمع تجاری سروستان") }
    var postalCode by remember { mutableStateOf(currentUserLoc?.postalCode ?: "۱۹۹۷۸۱۴۵۲۳") }
    var locationVerifiedOnMap by remember { mutableStateOf(true) }
    var showLocationSuccessSnackbar by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var publishedPostTitle by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Auto-update slug if autoSlug is true
    LaunchedEffect(title) {
        if (autoSlug && title.isNotEmpty()) {
            slug = title.replace(" ", "-").take(40)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("افزودن محصول سئو شده", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            
            // 1. Title and SEO Preview
            item {
                SectionHeader("۱. اطلاعات پایه و سئو (SEO)")
                Spacer(modifier = Modifier.height(12.dp))
                
                val titleColor = if (title.length > 60) FuchsiaAccent else if (title.length > 40) EmeraldSuccess else SlateText
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        if (autoSlug) slug = it.replace(" ", "-").take(40)
                    },
                    label = { Text("عنوان سئو شده محصول", color = SlateMuted) },
                    supportingText = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("شامل کلمه کلیدی اصلی باشد", color = SlateMuted)
                            Text("${title.length} / 60", color = titleColor, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            
            // SEO Preview Snippet
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF202124)), // Google dark mode color roughly
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("پیش‌نمایش در گوگل (SERP)", color = SlateMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color.White)) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = Color.Gray, modifier = Modifier.align(Alignment.Center).size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("kook.ir", color = Color(0xFFDADCE0), style = MaterialTheme.typography.bodySmall)
                                Text("https://kook.ir/product/${if (slug.isEmpty()) "your-product" else slug}", color = Color(0xFF8AB4F8), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (title.isEmpty()) "عنوان محصول شما در اینجا نمایش داده می‌شود" else title,
                            color = Color(0xFF8AB4F8),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (excerpt.isEmpty()) "توضیحات متای محصول که کاربر را ترغیب به کلیک و خرید می‌کند. ۱۲۰ تا ۱۶۰ کاراکتر پیشنهاد می‌شود." else excerpt,
                            color = Color(0xFFBDC1C6),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            item {
                OutlinedTextField(
                    value = slug,
                    onValueChange = { 
                        slug = it
                        autoSlug = false
                    },
                    label = { Text("نامک (URL Slug)", color = SlateMuted) },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = SlateMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // 2. Descriptions
            item {
                SectionHeader("۲. توضیحات و محتوا")
                Spacer(modifier = Modifier.height(12.dp))
                
                val excerptColor = if (excerpt.length in 120..160) EmeraldSuccess else SlateText
                OutlinedTextField(
                    value = excerpt,
                    onValueChange = { excerpt = it },
                    label = { Text("توضیحات کوتاه (Meta Description)", color = SlateMuted) },
                    supportingText = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("نمایش در بالای صفحه و نتایج گوگل", color = SlateMuted)
                            Text("${excerpt.length} / 160", color = excerptColor)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            item {
                // Fake Rich Text Editor
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GlassBackground)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = Color.White, modifier = Modifier.size(20.dp).clickable{})
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = Color.White, modifier = Modifier.size(20.dp).clickable{})
                        Icon(Icons.Default.FormatListBulleted, contentDescription = "List", tint = Color.White, modifier = Modifier.size(20.dp).clickable{})
                        Icon(Icons.Default.TableChart, contentDescription = "Table", tint = Color.White, modifier = Modifier.size(20.dp).clickable{})
                        Spacer(modifier = Modifier.weight(1f))
                        Text("توضیحات اصلی", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                    }
                    TextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        placeholder = { Text("توضیحات کامل، ویژگی‌ها و مزایای محصول...", color = SlateMuted) }
                    )
                }
            }

            // 3. Images and Alt Text
            item {
                SectionHeader("۳. تصاویر بهینه (Image SEO)")
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(120.dp).aspectRatio(9f / 16f)
                                    .background(GlassBackground, RoundedCornerShape(12.dp))
                                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                    .clickable { },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("افزودن تصویر", color = SlateText, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    
                    // Mock existing image with ALT field
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(150.dp)) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=300&q=80",
                                contentDescription = "Watch",
                                modifier = Modifier.width(120.dp).aspectRatio(9f / 16f).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = "ساعت هوشمند", // Mock value
                                onValueChange = {},
                                label = { Text("متن جایگزین (Alt)", color = FuchsiaAccent) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                textStyle = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldSuccess,
                                    unfocusedBorderColor = GlassBorder,
                                    unfocusedLabelColor = SlateMuted,
                                    focusedLabelColor = EmeraldSuccess
                                )
                            )
                        }
                    }
                }
            }

            // 4. Categories & Dynamic Specifications
            item {
                SectionHeader("۴. دسته‌بندی و مشخصات (Schema Markup)")
                Spacer(modifier = Modifier.height(12.dp))
                
                val categories = listOf("بدون دسته‌بندی", "موبایل و تبلت", "پوشاک", "لوازم خانگی")
                
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("دسته‌بندی محصول", color = SlateMuted) },
                        modifier = Modifier.fillMaxWidth().clickable { showCategoryDropdown = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledBorderColor = GlassBorder,
                            disabledLabelColor = SlateMuted
                        ),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SlateMuted) }
                    )
                    
                    Box(modifier = Modifier.matchParentSize().clickable { showCategoryDropdown = true })
                    
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.background).border(1.dp, GlassBorder)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = Color.White) },
                                onClick = {
                                    category = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Dynamic Fields based on Category
            item {
                AnimatedVisibility(visible = category == "موبایل و تبلت") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MockDropdownField("حافظه داخلی", listOf("128 GB", "256 GB", "512 GB"), modifier = Modifier.weight(1f))
                            MockDropdownField("مقدار RAM", listOf("4 GB", "8 GB", "12 GB"), modifier = Modifier.weight(1f))
                        }
                        OutlinedTextField(
                            value = "", onValueChange = {}, label = { Text("ظرفیت باتری (mAh)", color = SlateMuted) }, modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = GlassBorder)
                        )
                    }
                }
                AnimatedVisibility(visible = category == "پوشاک") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MockDropdownField("جنس لباس", listOf("نخ پنبه", "پلی‌استر", "جین", "چرم"), modifier = Modifier.weight(1f))
                            MockDropdownField("سایزبندی", listOf("S, M, L", "فری‌سایز", "36 تا 44"), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            
            // Common Schema Fields
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MockDropdownField("برند (Brand)", listOf("متفرقه", "سامسونگ", "اپل", "نایک"), modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("کد انبار (SKU)", color = SlateMuted) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }
            
            item {
                OutlinedTextField(
                    value = gtin,
                    onValueChange = { gtin = it },
                    label = { Text("بارکد جهانی (GTIN/UPC)", color = SlateMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    supportingText = { Text("شناسایی دقیق کالا برای Google Shopping", color = EmeraldSuccess) }
                )
            }

            // 5. Pricing & Inventory
            item {
                SectionHeader("۵. قیمت و موجودی")
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("قیمت اصلی (تومان)", color = SlateMuted) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IndigoPrimary,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = discountedPrice,
                        onValueChange = { discountedPrice = it },
                        label = { Text("قیمت با تخفیف", color = FuchsiaAccent) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FuchsiaAccent,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            
            item {
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("موجودی در انبار", color = SlateMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 6. Mandatory Store Location on Map
            item {
                SectionHeader("۶. موقعیت مکانی و آدرس فروشگاه (اجباری) 📍")
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (locationVerifiedOnMap) EmeraldSuccess else FuchsiaAccent)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.PinDrop, contentDescription = null, tint = EmeraldSuccess)
                                Text("تایید لوکیشن روی نقشه خریداران", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                            
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (locationVerifiedOnMap) EmeraldSuccess.copy(alpha = 0.2f) else FuchsiaAccent.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, if (locationVerifiedOnMap) EmeraldSuccess else FuchsiaAccent)
                            ) {
                                Text(
                                    text = if (locationVerifiedOnMap) "الزامی و تایید شده ✓" else "ثبت لوکیشن اجباری است",
                                    color = if (locationVerifiedOnMap) EmeraldSuccess else FuchsiaAccent,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "برای نمایش محصولات و فروشگاه شما در تب «نقشه» برای مشتریان، موقعیت مکانی دقیق فروشگاه اجباری است.",
                            color = SlateMuted,
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Interactive Map Mini Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                .clickable { locationVerifiedOnMap = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = FuchsiaAccent, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("پین موقعیت فروشگاه: $neighborhood", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("مختصات: ۳۵.۷۸۳۰ شمالی، ۵۱.۳۷۵۰ شرقی", color = EmeraldSuccess, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = province,
                                onValueChange = { province = it },
                                label = { Text("استان", color = SlateMuted) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoPrimary,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("شهر", color = SlateMuted) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoPrimary,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = neighborhood,
                            onValueChange = { neighborhood = it },
                            label = { Text("محله / منطقه", color = SlateMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPrimary,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("آدرس دقیق فروشگاه (خیابان، پلاک، واحد)", color = SlateMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPrimary,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            // Generate Schema & Publish Button
            item {
                Button(
                    onClick = {
                        val finalTitle = title.ifBlank { "کالای جدید فروشگاه کوک" }
                        publishedPostTitle = finalTitle
                        val parsedPrice = price.replace(",", "").replace("،", "").trim().toLongOrNull() ?: 450000L
                        val newLoc = com.example.data.StoreLocation(
                            latitude = 35.7796,
                            longitude = 51.3753,
                            province = province,
                            city = city,
                            neighborhood = neighborhood,
                            address = address,
                            postalCode = postalCode
                        )
                        com.example.data.AppState.publishProductPost(
                            title = finalTitle,
                            price = parsedPrice,
                            category = category.ifBlank { "مد و پوشاک" },
                            description = excerpt.ifBlank { "کالای ترند و باکیفیت ارائه شده در بوتیک کوک استایل" },
                            imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80",
                            location = newLoc
                        )
                        coroutineScope.launch {
                            com.example.repository.NetworkPostRepository(context).createPost(
                                title = finalTitle,
                                price = parsedPrice,
                                category = category.ifBlank { "مد و پوشاک" },
                                description = excerpt.ifBlank { "کالای ترند و باکیفیت ارائه شده در بوتیک کوک استایل" },
                                imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80",
                                location = newLoc
                            )
                        }
                        showSuccessDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("انتشار کالا و ثبت موقعیت در نقشه کوک", fontWeight = FontWeight.Bold)
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showSuccessDialog = false
                    title = ""
                    price = ""
                    excerpt = ""
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            showSuccessDialog = false
                            title = ""
                            price = ""
                            excerpt = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("متوجه شدم - مشاهده در ویترین")
                    }
                },
                title = { Text("انتشار با موفقیت انجام شد! 🎉", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "محصول «$publishedPostTitle» با موفقیت در فید و اکسپلور کوک ثبت شد و لوکیشن فروشگاه شما در محله $neighborhood روی نقشه مشتریان فعال شد.",
                        color = SlateText
                    )
                },
                containerColor = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color.White,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockDropdownField(label: String, options: List<String>, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("") }
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = SlateMuted) },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.White,
                disabledBorderColor = GlassBorder,
                disabledLabelColor = SlateMuted
            ),
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SlateMuted) }
        )
        
        Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.background).border(1.dp, GlassBorder)
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt, color = Color.White) },
                    onClick = {
                        selected = opt
                        expanded = false
                    }
                )
            }
        }
    }
}
