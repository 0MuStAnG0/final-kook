package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun GuestCheckoutDialog(
    onDismiss: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .shadow(24.dp, RoundedCornerShape(26.dp), spotColor = IndigoPrimary),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
            border = BorderStroke(1.2.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Glowing Icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    OrangeItCoral.copy(alpha = 0.35f),
                                    FuchsiaAccent.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            BorderStroke(2.dp, Brush.linearGradient(listOf(OrangeItCoral, FuchsiaAccent))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                // Status Badge
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = OrangeItCoral
                        )
                        Text(
                            text = "کاربر میهمان • ثبت‌نام الزامی است",
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangeItCoral,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Title & Subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ثبت‌نام برای تسویه سفارش الزامی است",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "امکان تسویه حساب و نهایی‌سازی سفارش برای کاربران میهمان میسر نیست. جهت ثبت نشانی پستی، صدور فاکتور رسمی و ضمانت امانی شاپرک، لطفاً ابتدا ثبت‌نام کنید.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }

                // Benefits / Requirements List
                Surface(
                    color = GlassBackground,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CheckoutRequirementRow(
                            icon = Icons.Default.LocalShipping,
                            iconTint = IndigoPrimary,
                            title = "ثبت دقیق نشانی و کد پستی",
                            desc = "جهت ارسال مرسوله توسط پست پیشتاز و پیک"
                        )
                        HorizontalDivider(color = GlassBorder.copy(alpha = 0.5f))
                        CheckoutRequirementRow(
                            icon = Icons.Default.Security,
                            iconTint = EmeraldSuccess,
                            title = "پرداخت امن در صندوق امانی شاپرک",
                            desc = "حفظ وجه تا زمان تایید تحویل و رضایت خریدار"
                        )
                        HorizontalDivider(color = GlassBorder.copy(alpha = 0.5f))
                        CheckoutRequirementRow(
                            icon = Icons.Default.ReceiptLong,
                            iconTint = FuchsiaAccent,
                            title = "صدور فاکتور رسمی و کد رهگیری",
                            desc = "امکان پیگیری ۲۴ ساعته مرسوله و پشتیبانی"
                        )
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Register Button (Primary CTA)
                    Button(
                        onClick = {
                            onDismiss()
                            onNavigateToRegister()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = IndigoPrimary),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(IndigoPrimary, FuchsiaAccent))),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "ثبت‌نام سریع در کوک (رایگان)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Login Button (Secondary CTA)
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onNavigateToLogin()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GlassBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF1E293B).copy(alpha = 0.6f),
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "قبلاً ثبت‌نام کرده‌اید؟ ورود به حساب",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Dismiss Button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "انصراف و ادامه مشاهده",
                            color = SlateMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckoutRequirementRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.15f))
                .border(BorderStroke(1.dp, iconTint.copy(alpha = 0.3f)), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = SlateMuted,
                fontSize = 11.sp
            )
        }
    }
}
