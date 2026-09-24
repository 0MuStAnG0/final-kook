package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppState
import com.example.network.ApiResult
import com.example.network.UiHandshakeManager
import com.example.network.toComposeColor
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun UiHandshakeDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val handshakeManager = remember { UiHandshakeManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }
    var syncResultMsg by remember { mutableStateOf<String?>(null) }

    val config = AppState.uiConfig

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = FuchsiaAccent)
                Spacer(modifier = Modifier.width(10.dp))
                Text("هندشیک بصری با سرور (UI Handshake)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column {
                Text(
                    text = "ارسال مشخصات گرافیکی کلاینت و دریافت خودکار پالت رنگی، نام برند و بنرها از بک‌اند:",
                    color = SlateText,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Server Visual Status Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (AppState.isUiSyncedWithBackend) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (AppState.isUiSyncedWithBackend) EmeraldSuccess else AmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (AppState.isUiSyncedWithBackend) "وضعیت: متصل و همگام با بک‌اند" else "وضعیت: آماده برقراری ارتباط با سرور",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• نسخه پیکربندی سرور: ${config.configVersion}", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                        Text("• نام برند دریافتی: ${config.branding.appTitle}", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                        Text("• شعار پلتفرم: ${config.branding.brandTagline}", color = SlateMuted, style = MaterialTheme.typography.labelSmall)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("پالت رنگی ارسالی سرور:", color = IndigoPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ColorSwatch(config.theme.primaryHex.toComposeColor(IndigoPrimary), "اصلی")
                            ColorSwatch(config.theme.accentHex.toComposeColor(FuchsiaAccent), "اکسنت")
                            ColorSwatch(config.theme.emeraldSuccessHex.toComposeColor(EmeraldSuccess), "تایید")
                            ColorSwatch(config.theme.orangeItPurpleHex.toComposeColor(OrangeItPurple), "اورنج IT")
                        }
                    }
                }

                if (config.branding.announcementText != null && config.branding.isAnnouncementVisible) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = IndigoPrimary.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("📢 اعلان فعال بک‌اند:", color = IndigoPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text(config.branding.announcementText, color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (syncResultMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = syncResultMsg ?: "",
                        color = if (syncResultMsg?.contains("موفق") == true) EmeraldSuccess else RoseError,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSyncing = true
                    syncResultMsg = null
                    coroutineScope.launch {
                        when (val res = handshakeManager.performHandshake()) {
                            is ApiResult.Success -> {
                                isSyncing = false
                                syncResultMsg = "✅ هندشیک با موفقیت انجام شد و داده‌های ظاهری دریافت شدند."
                            }
                            is ApiResult.Error -> {
                                isSyncing = false
                                syncResultMsg = "❌ ${res.message}"
                            }
                            is ApiResult.NetworkError -> {
                                isSyncing = false
                                syncResultMsg = "❌ خطا در برقراری ارتباط با سرور."
                            }
                        }
                    }
                },
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = FuchsiaAccent)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("در حال هندشیک...", fontSize = 13.sp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("همگام‌سازی مجدد با سرور", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن", color = SlateMuted)
            }
        }
    )
}

@Composable
private fun ColorSwatch(color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = SlateMuted, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
    }
}
