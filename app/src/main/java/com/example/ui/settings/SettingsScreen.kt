package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AccountType
import com.example.data.AppState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit = {},
    onNavigateToStoreApplication: () -> Unit = {}
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showUiHandshakeDialog by remember { mutableStateOf(false) }
    var isCheckingServer by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val user = AppState.currentUser
    val isStore = user.accountType == AccountType.STORE_OWNER || user.isSeller

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات و حساب کاربری", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back", tint = SlateText)
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
        ) {
            // User Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassBackground),
                    border = BorderStroke(1.dp, if (isStore) FuchsiaAccent.copy(alpha = 0.5f) else IndigoPrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = user.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(2.dp, if (isStore) FuchsiaAccent else IndigoPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.name, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isStore) FuchsiaAccent.copy(alpha = 0.2f) else IndigoPrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        if (isStore) "فروشگاه تایید شده" else "کاربر عادی (خریدار)",
                                        color = if (isStore) FuchsiaAccent else IndigoPrimary,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(user.gmail.ifEmpty { "user@kook.ir" }, color = SlateMuted, style = MaterialTheme.typography.bodySmall)
                            Text(user.phone.ifEmpty { "۰۹۱۲۳۴۵۶۷۸۹" }, color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Production Live Server & Real Database Engine
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(22.dp))
                                Text(
                                    "سرور اصلی و پایگاه داده رسمی",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (AppState.isServerOnline) EmeraldSuccess.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, if (AppState.isServerOnline) EmeraldSuccess else Color.Red)
                            ) {
                                Text(
                                    if (AppState.isServerOnline) "متصل به سرور ✅" else "آفلاین / کش محلی",
                                    color = if (AppState.isServerOnline) EmeraldSuccess else Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "سامانه با کانال رمزنگاری‌شده سرور مرکزی همگام بوده و تبادل اطلاعات، ورود امن، و سفارشات تحت پروتکل امن SSL/TLS انجام می‌پذیرد.",
                            color = SlateMuted,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(0.5.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("لایه رمزنگاری شبکه:", color = SlateMuted, fontSize = 10.sp)
                                        Text("کانال امن HTTPS و توکن Keystore", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = AppState.serverStatusText,
                                    color = if (AppState.isServerOnline) EmeraldSuccess else SlateMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.widthIn(max = 160.dp),
                                    maxLines = 2
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isCheckingServer = true
                                    coroutineScope.launch {
                                        AppState.testServerConnection(context)
                                        isCheckingServer = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isCheckingServer,
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                if (isCheckingServer) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.NetworkPing, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تست پینگ سرور", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        AppState.testServerConnection(context)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("بررسی اتصال سرور", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Store Application Shortcut if regular user
            if (!isStore) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onNavigateToStoreApplication() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
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
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = FuchsiaAccent)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("ارتقا به حساب فروشگاه", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("ارسال مدارک صنفی و فعال‌سازی ویترین اختصاصی", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = FuchsiaAccent)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            // Orange IT Company Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { showAboutDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassBackground),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFFF9800), Color(0xFFE65100)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🍊", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("پلتفرم جامع کوک‌استایل", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("نسخه رسمی ۱.۰ | لایه امنیتی و رمزنگاری پیشرفته", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                        }
                        Icon(Icons.Default.Info, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            item { SettingsSectionTitle("حساب و امنیت") }
            item { SettingsItem(icon = Icons.Default.Shield, title = "امنیت شبکه و داده‌ها", subtitle = "ارتباط رمزنگاری‌شده HTTPS و توکن‌های محافظت‌شده در KeyStore") }
            item { SettingsItem(icon = Icons.Default.Person, title = "اطلاعات حساب کاربری", subtitle = "ایمیل، شماره تماس، سلیقه و علایق") }
            item { SettingsItem(icon = Icons.Default.Security, title = "امنیت ورود و نشست‌ها", subtitle = "رمزگذاری کلید سخت‌افزاری Keystore") }
            item { SettingsItem(icon = Icons.Default.SwitchAccount, title = "ورود با حسابی دیگر / ثبت‌نام جدید", subtitle = "تغییر کاربر یا ایجاد حساب تازه", onClick = onNavigateToAuth) }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SettingsSectionTitle("تنظیمات عمومی و ظاهر") }
            item { SettingsItem(icon = Icons.Default.Palette, title = "هندشیک و همگام‌سازی بصری (UI Handshake)", subtitle = "بازخوانی پالت رنگی و بنرها از سرور", onClick = { showUiHandshakeDialog = true }) }
            item { SettingsItem(icon = Icons.Default.Notifications, title = "اعلان‌های سفارشات و پیام‌ها") }
            item { SettingsItem(icon = Icons.Default.Language, title = "زبان برنامه", subtitle = "فارسی (پیش‌فرض)") }
            item { SettingsItem(icon = Icons.Default.DarkMode, title = "پوسته ظاهری", subtitle = "تاریک شیشه‌ای (Dark Glass)") }

            item { Spacer(modifier = Modifier.height(20.dp)) }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { showLogoutDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, RoseError.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = RoseError)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "خروج از حساب کاربری",
                            color = RoseError,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "تمام حق اثر متعلق به Orange IT است",
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "All Rights Reserved © 2026 Orange IT • www.orangeit.site",
                        color = SlateMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                containerColor = Color(0xFF1E293B),
                shape = RoundedCornerShape(20.dp),
                title = { Text("خروج از حساب", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("آیا مطمئن هستید که می‌خواهید از حساب خود خارج شوید؟", color = SlateText) },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            AppState.logout()
                            coroutineScope.launch {
                                com.example.network.AuthRepository(context).logout()
                            }
                            onNavigateToAuth()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                    ) {
                        Text("خروج قطعی", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("انصراف", color = SlateMuted)
                    }
                }
            )
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                containerColor = Color(0xFF1E293B),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🍊", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("درباره Orange IT", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            "پلتفرم فروشگاهی و خرید زنده کوک (kook) تماماً توسط Orange IT طراحی، توسعه و پشتیبانی شده است. تمام حق اثر، کپی‌رایت، کدهای نرم‌افزاری و علائم تجاری این سامانه منحصراً متعلق به شرکت Orange IT می‌باشد.",
                            color = SlateText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GlassBackground),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🌐 وب‌سایت رسمی:", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                                Text("www.orangeit.site", color = IndigoPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("🔒 پورتال امنیتی و خدمات:", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                                Text("www.kook.orangeit.site", color = FuchsiaAccent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("⚖️ حق مالکیت و کپی‌رایت:", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                                Text("All Rights Reserved © 2026 Orange IT", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("بستن", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (showUiHandshakeDialog) {
            UiHandshakeDialog(
                onDismiss = { showUiHandshakeDialog = false }
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = SlateMuted,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = SlateText, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = SlateText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = SlateMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Arrow", tint = SlateMuted, modifier = Modifier.size(18.dp))
    }
}
