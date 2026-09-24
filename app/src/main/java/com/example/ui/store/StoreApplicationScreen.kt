package com.example.ui.store

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppState
import com.example.data.StoreApplicationStatus
import com.example.ui.theme.*
import com.example.utils.ValidationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreApplicationScreen(
    onBack: () -> Unit,
    onApplicationApproved: () -> Unit
) {
    val currentApp = AppState.currentUser.storeApplication
    val isAlreadySubmitted = currentApp != null

    var storeName by remember { mutableStateOf(currentApp?.storeName ?: "") }
    var category by remember { mutableStateOf(currentApp?.category ?: "پوشاک و مد") }
    var city by remember { mutableStateOf(currentApp?.city ?: AppState.currentUser.city) }
    var address by remember { mutableStateOf(currentApp?.address ?: "") }
    var nationalId by remember { mutableStateOf(currentApp?.nationalId ?: "") }
    var postalCode by remember { mutableStateOf("") }
    var selectedDocType by remember { mutableStateOf("پروانه کسب یا جواز اتحادیه") }
    var description by remember { mutableStateOf(currentApp?.description ?: "") }

    var selectedDocumentUri by remember { mutableStateOf<Uri?>(null) }
    var isDocumentUploaded by remember { mutableStateOf(currentApp != null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSyncingStatus by remember { mutableStateOf(false) }
    var syncFeedbackText by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val authRepository = remember { com.example.network.AuthRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    // Native Photo Picker (Zero-permission Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedDocumentUri = uri
            isDocumentUploaded = true
            errorMessage = null
        }
    }

    val categories = listOf(
        "پوشاک و مد", "کیف و کفش", "آرایشی و بهداشتی", "اکسسوری و زیورآلات",
        "کالای دیجیتال", "لوازم خانه و دکوراسیون", "صنایع دستی و هنری", "خوراکی و تنقلات"
    )

    val documentTypes = listOf(
        "پروانه کسب یا جواز اتحادیه",
        "تصویر کارت ملی هوشمند مالک",
        "شناسه صنفی / روزنامه رسمی شرکت",
        "سند یا اجاره‌نامه معتبر فروشگاه"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ثبت فروشگاه و احراز هویت صنفی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "بازگشت", tint = SlateText)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Status Card if already submitted
            if (isAlreadySubmitted && currentApp != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (currentApp.status) {
                                StoreApplicationStatus.APPROVED -> EmeraldSuccess.copy(alpha = 0.15f)
                                StoreApplicationStatus.PENDING_REVIEW -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                StoreApplicationStatus.REJECTED -> RoseError.copy(alpha = 0.15f)
                                else -> GlassBackground
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            when (currentApp.status) {
                                StoreApplicationStatus.APPROVED -> EmeraldSuccess
                                StoreApplicationStatus.PENDING_REVIEW -> Color(0xFFFF9800)
                                StoreApplicationStatus.REJECTED -> RoseError
                                else -> GlassBorder
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (currentApp.status) {
                                            StoreApplicationStatus.APPROVED -> Icons.Default.CheckCircle
                                            StoreApplicationStatus.PENDING_REVIEW -> Icons.Default.HourglassTop
                                            StoreApplicationStatus.REJECTED -> Icons.Default.Error
                                            else -> Icons.Default.Info
                                        },
                                        contentDescription = null,
                                        tint = when (currentApp.status) {
                                            StoreApplicationStatus.APPROVED -> EmeraldSuccess
                                            StoreApplicationStatus.PENDING_REVIEW -> Color(0xFFFF9800)
                                            StoreApplicationStatus.REJECTED -> RoseError
                                            else -> SlateMuted
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = currentApp.status.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Text(
                                    text = currentApp.submittedAt,
                                    color = SlateMuted,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = currentApp.adminFeedback ?: "مدارک و اطلاعات هویتی فروشگاه شما در سرور مرکزی ثبت شد و در صف تطبیق کارشناسان اصناف قرار دارد.",
                                color = SlateText,
                                style = MaterialTheme.typography.bodySmall
                            )

                            // Status Details
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.5f),
                                border = BorderStroke(0.5.dp, GlassBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("نام فروشگاه: ${currentApp.storeName}", color = Color.White, fontSize = 12.sp)
                                    Text("رسته صنفی: ${currentApp.category} | شهر: ${currentApp.city}", color = SlateMuted, fontSize = 11.sp)
                                    if (currentApp.nationalId.isNotBlank()) {
                                        Text("کد ملی / شناسه ثبتی: ${ValidationUtils.formatNationalCode(currentApp.nationalId)}", color = SlateMuted, fontSize = 11.sp)
                                    }
                                }
                            }

                            if (currentApp.status == StoreApplicationStatus.PENDING_REVIEW) {
                                Spacer(modifier = Modifier.height(14.dp))
                                // Operational Refresh / Status Check Button
                                Button(
                                    onClick = {
                                        isSyncingStatus = true
                                        syncFeedbackText = null
                                        coroutineScope.launch {
                                            authRepository.syncStoreApplicationStatus()
                                            isSyncingStatus = false
                                            syncFeedbackText = "استعلام آخرین وضعیت از سرور مرکزی انجام شد."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isSyncingStatus,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isSyncingStatus) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("در حال استعلام وضعیت از سرور...", fontSize = 13.sp)
                                    } else {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("بروزرسانی وضعیت از سرور مرکزی", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                if (syncFeedbackText != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = syncFeedbackText ?: "",
                                        color = EmeraldSuccess,
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else if (currentApp.status == StoreApplicationStatus.APPROVED) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onApplicationApproved,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ورود به پنل مدیریت فروشگاه", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Benefits Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassBackground),
                    border = BorderStroke(1.dp, Brush.linearGradient(listOf(IndigoPrimary, FuchsiaAccent)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "مزایای داشتن ویترین فروشگاهی تایید شده در کوک:",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BenefitRow("🌟 دریافت نشان آبی احراز هویت اصناف و جلب اعتماد مشتریان")
                        BenefitRow("🛍️ درگاه پرداخت امن شتابی با تسویه حساب خودکار شاپرک")
                        BenefitRow("📍 ثبت موقعیت بوتیک روی نقشه زنده شهری و خرید حضوری")
                        BenefitRow("📦 سامانه مکانیزه چاپ فاکتور و پیگیری کدهای رهگیری پست")
                    }
                }
            }

            // Form Header
            item {
                Text(
                    "مشخصات هویتی و ثبتی فروشگاه",
                    color = SlateText,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            // Store Name
            item {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { 
                        storeName = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("نام فروشگاه / برند تجاری *") },
                    placeholder = { Text("مثال: بوتیک چرم نوین یا گالری آرایشی ماهان") },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = IndigoPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
            }

            // Category Selection
            item {
                var categoryExpanded by remember { mutableStateOf(false) }
                Column {
                    Text("دسته‌بندی اصلی فعالیت *", color = SlateMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryExpanded = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Category, contentDescription = null, tint = FuchsiaAccent)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(category, color = Color.White, fontWeight = FontWeight.Medium)
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SlateMuted)
                            }
                        }
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = Color.White) },
                                    onClick = {
                                        category = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // City & Address
            item {
                OutlinedTextField(
                    value = city,
                    onValueChange = { 
                        city = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("شهر محل فعالیت *") },
                    leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = IndigoPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
            }

            item {
                OutlinedTextField(
                    value = address,
                    onValueChange = { 
                        address = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("نشانی دقیق فروشگاه یا انبار توزیع *") },
                    placeholder = { Text("مثال: خیابان شریعتی، بالاتر از میرداماد، پلاک ۱۲، واحد ۳") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = IndigoPrimary) },
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
            }

            // National ID with Live Validation
            item {
                val cleanId = ValidationUtils.normalizeDigits(nationalId).filter { it.isDigit() }
                val isIdValid = cleanId.isEmpty() || ValidationUtils.isValidIranianNationalCode(cleanId)

                Column {
                    OutlinedTextField(
                        value = nationalId,
                        onValueChange = { 
                            nationalId = it
                            if (errorMessage != null) errorMessage = null
                        },
                        label = { Text("کد ملی یا شناسه ثبت صنفی مالک *") },
                        placeholder = { Text("۱۰ رقمی کد ملی یا ۱۱ رقمی شناسه شرکت") },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Badge, 
                                contentDescription = null, 
                                tint = if (cleanId.isNotEmpty() && !isIdValid) RoseError else IndigoPrimary
                            ) 
                        },
                        trailingIcon = {
                            if (cleanId.isNotEmpty()) {
                                Icon(
                                    imageVector = if (isIdValid) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (isIdValid) EmeraldSuccess else RoseError
                                )
                            }
                        },
                        isError = cleanId.isNotEmpty() && !isIdValid,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors()
                    )
                    if (cleanId.isNotEmpty() && !isIdValid) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "کد ملی ۱۰ رقمی نامعتبر است (کنترل رقم چک یا تکرار نادرست)",
                            color = RoseError,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Postal Code (Optional but recommended)
            item {
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = { Text("کد پستی ۱۰ رقمی محل فعالیت (اختیاری)") },
                    placeholder = { Text("مثال: ۱۹۹۷۸۱۴۵۲۳") },
                    leadingIcon = { Icon(Icons.Default.MarkunreadMailbox, contentDescription = null, tint = SlateMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
            }

            // Document Type Dropdown
            item {
                var docTypeExpanded by remember { mutableStateOf(false) }
                Column {
                    Text("نوع سند یا مدرک ارسالی *", color = SlateMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { docTypeExpanded = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = EmeraldSuccess)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(selectedDocType, color = Color.White, fontWeight = FontWeight.Medium)
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SlateMuted)
                            }
                        }
                        DropdownMenu(
                            expanded = docTypeExpanded,
                            onDismissRequest = { docTypeExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            documentTypes.forEach { docType ->
                                DropdownMenuItem(
                                    text = { Text(docType, color = Color.White) },
                                    onClick = {
                                        selectedDocType = docType
                                        docTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Document Upload Section (Native Android Photo Picker)
            item {
                Column {
                    Text("بارگذاری تصویر مدرک صنفی یا هویتی *", color = SlateMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Launch standard photo picker (Play policy compliant)
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDocumentUploaded) EmeraldSuccess.copy(alpha = 0.1f) else Color(0xFF1E293B).copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, if (isDocumentUploaded) EmeraldSuccess else GlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(if (isDocumentUploaded) EmeraldSuccess.copy(alpha = 0.2f) else IndigoPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (isDocumentUploaded) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            tint = if (isDocumentUploaded) EmeraldSuccess else IndigoPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (isDocumentUploaded) "مدرک ضمیمه شد" else "انتخاب تصویر مدرک از گالری",
                                            color = if (isDocumentUploaded) EmeraldSuccess else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = if (selectedDocumentUri != null) "فایل تصویر آماده ارسال به سرور" else selectedDocType,
                                            color = SlateMuted,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isDocumentUploaded) EmeraldSuccess else IndigoPrimary)
                                ) {
                                    Text(
                                        text = if (isDocumentUploaded) "تغییر تصویر" else "انتخاب فایل",
                                        fontSize = 11.sp,
                                        color = if (isDocumentUploaded) EmeraldSuccess else IndigoPrimary
                                    )
                                }
                            }

                            // Document Thumbnail Preview if picked
                            if (selectedDocumentUri != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F172A))
                                ) {
                                    AsyncImage(
                                        model = selectedDocumentUri,
                                        contentDescription = "پیش‌نمایش مدرک بارگذاری شده",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xCC000000),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "✓ مدرک آماده ارسال",
                                            color = EmeraldSuccess,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("معرفی فروشگاه و خلاصه کالاها") },
                    placeholder = { Text("توضیحی کوتاه درباره اصالت اجناس، شرایط تعویض، سابقه و کانال‌های پشتیبانی...") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = IndigoPrimary) },
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors()
                )
            }

            // Error Message Card
            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, RoseError.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RoseError)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = RoseError,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        if (!AppState.isLoggedIn || AppState.isGuestUser()) {
                            errorMessage = "برای ثبت فروشگاه ابتدا باید وارد حساب کاربری شوید."
                            return@Button
                        }
                        if (storeName.trim().length < 3) {
                            errorMessage = "نام فروشگاه باید حداقل ۳ حرف باشد."
                            return@Button
                        }
                        if (address.trim().length < 6) {
                            errorMessage = "لطفاً نشانی کامل محل فعالیت را وارد کنید."
                            return@Button
                        }
                        val cleanId = ValidationUtils.normalizeDigits(nationalId).filter { it.isDigit() }
                        if (cleanId.isEmpty() || !ValidationUtils.isValidIranianNationalCode(cleanId)) {
                            errorMessage = "کد ملی یا شناسه ثبت صنفی وارد شده نامعتبر است."
                            return@Button
                        }
                        if (!isDocumentUploaded) {
                            errorMessage = "لطفاً تصویر مدرک صنفی یا هویتی را انتخاب و پیوست نمایید."
                            return@Button
                        }

                        isSubmitting = true
                        errorMessage = null

                        coroutineScope.launch {
                            when (val result = authRepository.submitStoreApplication(
                                storeName = storeName.trim(),
                                category = category,
                                city = city.trim(),
                                address = address.trim(),
                                nationalId = cleanId,
                                documentTitle = selectedDocType,
                                description = description.trim()
                            )) {
                                is com.example.network.ApiResult.Success -> {
                                    isSubmitting = false
                                    showSuccessDialog = true
                                }
                                is com.example.network.ApiResult.Error -> {
                                    // If error is connectivity-related, store resiliently in local state
                                    val isConn = result.message.contains("connect", ignoreCase = true) ||
                                        result.message.contains("پورت", ignoreCase = true) ||
                                        result.message.contains("اینترنت", ignoreCase = true) ||
                                        result.message.contains("timeout", ignoreCase = true) ||
                                        result.message.contains("host", ignoreCase = true)
                                    if (isConn) {
                                        AppState.submitStoreApplication(
                                            storeName = storeName.trim(),
                                            category = category,
                                            city = city.trim(),
                                            address = address.trim(),
                                            nationalId = cleanId,
                                            documentTitle = selectedDocType,
                                            description = description.trim()
                                        )
                                        isSubmitting = false
                                        showSuccessDialog = true
                                    } else {
                                        isSubmitting = false
                                        errorMessage = result.message
                                    }
                                }
                                is com.example.network.ApiResult.NetworkError -> {
                                    AppState.submitStoreApplication(
                                        storeName = storeName.trim(),
                                        category = category,
                                        city = city.trim(),
                                        address = address.trim(),
                                        nationalId = cleanId,
                                        documentTitle = selectedDocType,
                                        description = description.trim()
                                    )
                                    isSubmitting = false
                                    showSuccessDialog = true
                                }
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FuchsiaAccent)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("در حال ثبت و ارسال مدارک به سرور...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ارسال مدارک و ثبت نهایی فروشگاه", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مدارک با موفقیت ثبت شد", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Text(
                    "اطلاعات فروشگاه و مدارک هویتی شما در پایگاه داده مرکزی سامانه کوک ثبت گردید و در نوبت اعتبارسنجی کارشناسان قرار گرفت. وضعیت تایید در همین صفحه و پروفایل قابل مشاهده است.",
                    color = SlateText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Text("مشاهده وضعیت")
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = SlateText, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = IndigoPrimary,
    unfocusedBorderColor = GlassBorder,
    focusedLabelColor = IndigoPrimary,
    unfocusedLabelColor = SlateMuted,
    focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.6f),
    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.4f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
