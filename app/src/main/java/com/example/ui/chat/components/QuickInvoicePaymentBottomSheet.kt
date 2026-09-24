package com.example.ui.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppState
import com.example.data.Invoice
import com.example.ui.components.GuestCheckoutDialog
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.OrangeItCoral
import com.example.ui.theme.fluidAnimatedGradient
import com.example.utils.toPersianCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickInvoicePaymentBottomSheet(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onPaymentSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {}
) {
    var selectedWallet by remember { mutableStateOf(false) } // false = Direct Gateway, true = Escrow Wallet
    var isProcessing by remember { mutableStateOf(false) }
    val isGuest = AppState.isGuestUser()
    var showGuestDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("پرداخت سریع با صندوق امن شاپرک", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "انصراف")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guest Mode Warning Banner
            if (isGuest) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2D1515),
                    border = BorderStroke(1.dp, OrangeItCoral.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = OrangeItCoral, modifier = Modifier.size(20.dp))
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
                                "امکان تسویه خرید و پرداخت فاکتور بدون ثبت‌نام میسر نیست.",
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
                            Text("ثبت‌نام", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    invoice.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.title, fontWeight = FontWeight.Medium)
                            Text((item.unitPrice * item.quantity).toPersianCurrency())
                        }
                    }
                    if (invoice.shippingCost > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("هزینه ارسال", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(invoice.shippingCost.toPersianCurrency(), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مبلغ پرداختی نهایی:", fontWeight = FontWeight.Bold)
                        Text(invoice.totalAmount.toPersianCurrency(), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge, color = EmeraldSuccess)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Options
            Text("شیوه پرداخت امن:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Option 1: Gateway
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        if (!selectedWallet) EmeraldSuccess else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .background(
                        if (!selectedWallet) EmeraldSuccess.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { selectedWallet = false }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !selectedWallet,
                    onClick = { selectedWallet = false },
                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldSuccess)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("درگاه مستقیم بانکی (شاپرک)", fontWeight = FontWeight.Bold)
                    Text("واریز به امانت در صندوق امن تا زمان تحویل کالا", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Option 2: Internal Wallet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        if (selectedWallet) EmeraldSuccess else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .background(
                        if (selectedWallet) EmeraldSuccess.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { selectedWallet = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedWallet,
                    onClick = { selectedWallet = true },
                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldSuccess)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("کیف پول حساب کاربری", fontWeight = FontWeight.Bold)
                    Text("موجودی فعال صندوق: ۱,۲۰۰,۰۰۰ تومان", style = MaterialTheme.typography.labelSmall, color = EmeraldSuccess)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security reassurance note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldSuccess.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "وجه شما تا زمان تحویل فیزیکی کالا نزد صندوق امن تضمین می‌شود.",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldSuccess
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            if (isGuest) {
                Button(
                    onClick = { showGuestDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeItCoral),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "ثبت‌نام برای پرداخت فاکتور (الزامی)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            } else {
                Button(
                    onClick = {
                        isProcessing = true
                        onPaymentSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .fluidAnimatedGradient(shape = RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.PriceCheck, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تایید و پرداخت آنی به امانت صندوق", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
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
