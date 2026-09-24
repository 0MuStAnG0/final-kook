package com.example.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.theme.*

@Composable
fun GuestProfilePrompt(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "حساب کاربری",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SlateText
            )
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "تنظیمات",
                    tint = SlateMuted
                )
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Glowing Hero Icon
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                IndigoPrimary.copy(alpha = 0.35f),
                                FuchsiaAccent.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        BorderStroke(2.dp, Brush.linearGradient(listOf(IndigoPrimary, FuchsiaAccent))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                    tint = Color.White
                )
            }

            // Status Badge
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = OrangeItCoral
                    )
                    Text(
                        text = "کاربر مهمان • نیاز به ثبت‌نام",
                        style = MaterialTheme.typography.labelMedium,
                        color = OrangeItCoral,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Headline & Explanation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "برای داشتن پروفایل، لطفاً ثبت‌نام کنید",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = SlateText,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "کاربران میهمان دارای پروفایل کاربری نیستند. با ثبت‌نام رایگان در کوک، صاحب پروفایل اختصاصی شوید، سفارش‌هایتان را مدیریت کنید و با فروشگاه‌ها مستقیماً گفتگو نمایید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            // Benefits List Card
            Surface(
                color = GlassBackground,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "با ثبت‌نام در کوک چه امکاناتی دریافت می‌کنید؟",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateText
                    )

                    BenefitRow(
                        icon = Icons.Default.ShoppingBag,
                        iconTint = EmeraldSuccess,
                        title = "ثبت و پیگیری سفارش‌ها",
                        subtitle = "دسترسی به سبد خرید، صدور فاکتور رسمی و ترکینگ ارسال پستی"
                    )

                    HorizontalDivider(color = GlassBorder.copy(alpha = 0.5f))

                    BenefitRow(
                        icon = Icons.Default.Storefront,
                        iconTint = OrangeItOrange,
                        title = "پروفایل اختصاصی و راه‌اندازی ویترین",
                        subtitle = "تنظیم سلیقه استایل، عکس و امکان ارتقای حساب به فروشنده برای فروش لباس"
                    )

                    HorizontalDivider(color = GlassBorder.copy(alpha = 0.5f))

                    BenefitRow(
                        icon = Icons.Default.Forum,
                        iconTint = OrangeItCyan,
                        title = "گفتگوی آنلاین با بوتیک‌ها",
                        subtitle = "ارسال پیام به فروشندگان برای استعلام سایز، موجودی و تخفیف"
                    )

                    HorizontalDivider(color = GlassBorder.copy(alpha = 0.5f))

                    BenefitRow(
                        icon = Icons.Default.Favorite,
                        iconTint = FuchsiaAccent,
                        title = "نشان‌کردن ویدیوها و محصولات",
                        subtitle = "ذخیره آیتم‌های محبوب، لیست علاقه‌مندی‌ها و دریافت کدهای تخفیف روزانه"
                    )
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary CTA: Register
                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = IndigoPrimary),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(IndigoPrimary, FuchsiaAccent))
                            ),
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
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "ثبت‌نام در کوک (سریع و رایگان)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                // Secondary CTA: Login
                OutlinedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF131B2E).copy(alpha = 0.6f),
                        contentColor = SlateText
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
                            text = "قبلاً ثبت‌نام کرده‌اید؟ ورود به حساب کاربری",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Trust & Security Footer
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = EmeraldSuccess
                )
                Text(
                    text = "حفاظت از اطلاعات با رمزنگاری Keystore و پروتکل امن SSL",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun BenefitRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.15f))
                .border(BorderStroke(1.dp, iconTint.copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SlateText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SlateMuted,
                lineHeight = 18.sp
            )
        }
    }
}
