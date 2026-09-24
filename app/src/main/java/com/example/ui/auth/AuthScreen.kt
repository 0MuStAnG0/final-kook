package com.example.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.ApiResult
import com.example.network.AuthRepository
import com.example.network.ServerConfigManager
import com.example.ui.theme.*
import com.example.utils.ValidationUtils
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    initialTab: Int = 0,
    onAuthSuccess: () -> Unit,
    onSkipOrClose: (() -> Unit)? = null
) {
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) } // 0: Login, 1: Register
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    // Register Form Fields
    var regFullName by remember { mutableStateOf("") }
    var regGmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regCity by remember { mutableStateOf("تهران") }
    var regInterests by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regErrorMessage by remember { mutableStateOf<String?>(null) }

    // Login Form Fields
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    val iranianCities = listOf(
        "تهران", "مشهد", "اصفهان", "شیراز", "تبریز", "کرج", "اهواز", 
        "قم", "رشت", "کرمانشاه", "همدان", "یزد", "ارومیه", "قزوین", "کیش"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Server Config & Close/Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onSkipOrClose != null) {
                    IconButton(onClick = onSkipOrClose) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = SlateMuted)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // Security & Privacy Shield Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "کانال ارتباطی امن (SSL)",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // App Logo & Branding Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF9800), Color(0xFFE65100))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🍊", fontSize = 34.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "کوک | Kook Market",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "احراز هویت زنده و تبادل داده صرفاً با تایید مستقیم سرور بک‌اند (JWT/Postgres)",
                color = SlateMuted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Glassmorphic Auth Container Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.85f)),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Tab Selector: Login vs Register
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF0F172A).copy(alpha = 0.6f),
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = if (selectedTab == 0) IndigoPrimary else FuchsiaAccent,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier.clip(RoundedCornerShape(14.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { 
                                selectedTab = 0 
                                loginErrorMessage = null
                            },
                            text = {
                                Text(
                                    "ورود به حساب",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) Color.White else SlateMuted
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { 
                                selectedTab = 1 
                                regErrorMessage = null
                            },
                            text = {
                                Text(
                                    "ثبت‌نام جدید",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) Color.White else SlateMuted
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedTab == 0) {
                        // ==================== LOGIN FORM ====================
                        Text(
                            text = "ورود با استعلام از سرور بک‌اند",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "احراز هویت مبتنی بر توکن‌های JWT و بررسی دیتابیس",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Identifier (Gmail or Phone)
                        OutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = { loginIdentifier = it },
                            label = { Text("آدرس جیمیل یا شماره موبایل") },
                            placeholder = { Text("مثال: user@gmail.com یا ۰۹۱۲۳۴۵۶۷۸۹") },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = IndigoPrimary) },
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("کلمه عبور") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = SlateMuted
                                    )
                                }
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )

                        if (loginErrorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, RoseError.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RoseError, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = loginErrorMessage ?: "",
                                        color = RoseError,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val cleanId = ValidationUtils.normalizeDigits(loginIdentifier).trim()
                                if (cleanId.isBlank()) {
                                    loginErrorMessage = "لطفاً ایمیل یا شماره همراه خود را وارد کنید."
                                    return@Button
                                }
                                if (loginPassword.isBlank()) {
                                    loginErrorMessage = "لطفاً کلمه عبور را وارد کنید."
                                    return@Button
                                }

                                isLoading = true
                                loginErrorMessage = null

                                coroutineScope.launch {
                                    when (val result = authRepository.login(cleanId, loginPassword)) {
                                        is ApiResult.Success -> {
                                            isLoading = false
                                            onAuthSuccess()
                                        }
                                        is ApiResult.Error -> {
                                            isLoading = false
                                            loginErrorMessage = result.message
                                        }
                                        is ApiResult.NetworkError -> {
                                            isLoading = false
                                            loginErrorMessage = "خطا در برقراری ارتباط با سرور."
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("در حال استعلام از سرور...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.LockOpen, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ورود به حساب کاربری", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        if (onSkipOrClose != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            TextButton(
                                onClick = onSkipOrClose,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "ورود به عنوان مهمان و مرور بازار",
                                    color = SlateMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = SlateMuted, modifier = Modifier.size(16.dp))
                            }
                        }

                    } else {
                        // ==================== REGISTER FORM ====================
                        Text(
                            text = "ثبت‌نام زنده در سرور Orange IT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ثبت و اعتبارسنجی مستقیم رکورد کاربر در پایگاه داده مرکزی",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Full Name
                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("نام و نام خانوادگی *") },
                            placeholder = { Text("مثال: سحر رادمان") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = IndigoPrimary) },
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Gmail Address (Required)
                        OutlinedTextField(
                            value = regGmail,
                            onValueChange = { regGmail = it },
                            label = { Text("آدرس جیمیل (Gmail) *") },
                            placeholder = { Text("مثال: yourname@gmail.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = IndigoPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mobile Phone Number (Required)
                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text("شماره تلفن همراه *") },
                            placeholder = { Text("مثال: ۰۹۱۲۳۴۵۶۷۸۹") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = IndigoPrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // City of Residence
                        Text(
                            text = "شهر محل سکونت *",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        var cityMenuExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isLoading) { cityMenuExpanded = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.5f)),
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
                                        Icon(Icons.Default.LocationCity, contentDescription = null, tint = IndigoPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(regCity, color = Color.White, fontWeight = FontWeight.Medium)
                                    }
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SlateMuted)
                                }
                            }
                            DropdownMenu(
                                expanded = cityMenuExpanded,
                                onDismissRequest = { cityMenuExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                iranianCities.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city, color = Color.White) },
                                        onClick = {
                                            regCity = city
                                            cityMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tastes & Interests
                        OutlinedTextField(
                            value = regInterests,
                            onValueChange = { regInterests = it },
                            label = { Text("علایق و سبک مورد علاقه شما *") },
                            placeholder = { Text("مثال: پوشاک کژوال، لباس سنتی، اکسسوری نقره، لوازم آرایشی ارگانیک...") },
                            leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = FuchsiaAccent) },
                            singleLine = false,
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("تعیین کلمه عبور *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = SlateMuted
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = customTextFieldColors()
                        )

                        if (regErrorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, RoseError.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RoseError, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = regErrorMessage ?: "",
                                        color = RoseError,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Register Button
                        Button(
                            onClick = {
                                val cleanName = regFullName.trim()
                                val cleanEmail = regGmail.trim()
                                val cleanPhone = ValidationUtils.normalizePhoneNumber(regPhone)
                                val cleanInterests = regInterests.trim()

                                if (cleanName.length < 3) {
                                    regErrorMessage = "لطفاً نام و نام خانوادگی خود را کامل وارد کنید (حداقل ۳ حرف)."
                                    return@Button
                                }
                                if (!ValidationUtils.isValidEmail(cleanEmail)) {
                                    regErrorMessage = "لطفاً یک آدرس ایمیل یا جیمیل معتبر وارد نمایید."
                                    return@Button
                                }
                                if (!ValidationUtils.isValidIranianPhone(cleanPhone)) {
                                    regErrorMessage = "شماره همراه وارد شده نامعتبر است. فرمت معتبر: ۰۹۱۲۳۴۵۶۷۸۹"
                                    return@Button
                                }
                                if (cleanInterests.isBlank()) {
                                    regErrorMessage = "لطفاً متنی کوتاه درباره علایق و سبک‌های مدنظر خود بنویسید."
                                    return@Button
                                }
                                if (regPassword.length < 6) {
                                    regErrorMessage = "کلمه عبور جهت امنیت حساب کاربری باید حداقل ۶ کاراکتر باشد."
                                    return@Button
                                }

                                isLoading = true
                                regErrorMessage = null

                                coroutineScope.launch {
                                    when (val result = authRepository.register(
                                        fullName = cleanName,
                                        gmail = cleanEmail,
                                        phone = cleanPhone,
                                        city = regCity,
                                        interests = cleanInterests,
                                        password = regPassword
                                    )) {
                                        is ApiResult.Success -> {
                                            isLoading = false
                                            onAuthSuccess()
                                        }
                                        is ApiResult.Error -> {
                                            isLoading = false
                                            regErrorMessage = result.message
                                        }
                                        is ApiResult.NetworkError -> {
                                            isLoading = false
                                            regErrorMessage = "خطا در برقراری ارتباط با سرور بک‌اند."
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("در حال ثبت کاربر در دیتابیس سرور...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ثبت قطعی در سرور و ورود", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = IndigoPrimary,
    unfocusedBorderColor = GlassBorder,
    focusedLabelColor = IndigoPrimary,
    unfocusedLabelColor = SlateMuted,
    focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.6f),
    unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.4f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
