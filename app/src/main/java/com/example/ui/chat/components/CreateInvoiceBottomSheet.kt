package com.example.ui.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.fluidAnimatedGradient
import com.example.utils.SecurityUtils
import com.example.utils.toPersianCurrency
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceBottomSheet(
    onDismiss: () -> Unit,
    onCreateInvoice: (Invoice) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var sizeOrColorText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var selectedShipping by remember { mutableStateOf(ShippingMethod.LOCAL_EXPRESS) }
    var shippingCostText by remember { mutableStateOf("35000") }
    var isHygieneProtected by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF0284C7))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("صدور فاکتور امن Orange IT", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان دقیق کالا") },
                placeholder = { Text("مثال: مانتو لینن بهاره، کت چرم مردانه") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = sizeOrColorText,
                onValueChange = { sizeOrColorText = it },
                label = { Text("سایز / رنگ انتخابی توافق شده (اختیاری)") },
                placeholder = { Text("مثال: سایز L - رنگ طوسی") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = priceText,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) priceText = it },
                label = { Text("مبلغ توافق‌شده کالا (تومان)") },
                placeholder = { Text("مثال: 850000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("شیوه ارسال:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShippingOptionChip(
                    title = "پیک فوری شهری",
                    icon = Icons.Default.LocalShipping,
                    selected = (selectedShipping == ShippingMethod.LOCAL_EXPRESS),
                    onClick = {
                        selectedShipping = ShippingMethod.LOCAL_EXPRESS
                        shippingCostText = "45000"
                    },
                    modifier = Modifier.weight(1f)
                )
                ShippingOptionChip(
                    title = "پست پیشتاز سراسری",
                    icon = Icons.Default.LocalShipping,
                    selected = (selectedShipping == ShippingMethod.POSTAL),
                    onClick = {
                        selectedShipping = ShippingMethod.POSTAL
                        shippingCostText = "35000"
                    },
                    modifier = Modifier.weight(1f)
                )
                ShippingOptionChip(
                    title = "تحویل در فروشگاه",
                    icon = Icons.Default.Storefront,
                    selected = (selectedShipping == ShippingMethod.IN_STORE_PICKUP),
                    onClick = {
                        selectedShipping = ShippingMethod.IN_STORE_PICKUP
                        shippingCostText = "0"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = shippingCostText,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) shippingCostText = it },
                label = { Text("هزینه ارسال (تومان)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Hygiene Protection Switch
            Surface(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isHygieneProtected) Color(0xFFFDA4AF) else MaterialTheme.colorScheme.outlineVariant),
                color = if (isHygieneProtected) Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .clickable { isHygieneProtected = !isHygieneProtected }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = if (isHygieneProtected) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("کالای بهداشتی / پلمپ شرکتی", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("در صورت باز شدن پلمپ، امکان مرجوعی سلیقه‌ای سلب می‌شود.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isHygieneProtected,
                        onCheckedChange = { isHygieneProtected = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFE11D48), checkedTrackColor = Color(0xFFFECDD3))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val priceVal = priceText.toLongOrNull() ?: 0L
            val shippingVal = shippingCostText.toLongOrNull() ?: 0L
            val totalVal = priceVal + shippingVal

            if (priceVal > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مجموع کل فاکتور:", fontWeight = FontWeight.Bold)
                    Text(totalVal.toPersianCurrency(), fontWeight = FontWeight.ExtraBold, color = Color(0xFF0284C7))
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Button(
                onClick = {
                    if (title.isNotBlank() && priceVal > 0) {
                        val invoiceId = UUID.randomUUID().toString()
                        val signature = SecurityUtils.generateInvoiceSignature(invoiceId, totalVal, isHygieneProtected)
                        val invoice = Invoice(
                            id = invoiceId,
                            items = listOf(
                                InvoiceItem(
                                    title = title,
                                    quantity = 1,
                                    unitPrice = priceVal,
                                    sizeOrColor = sizeOrColorText.ifBlank { null },
                                    isHygieneNonReturnable = isHygieneProtected
                                )
                            ),
                            shippingMethod = selectedShipping,
                            shippingCost = shippingVal,
                            totalAmount = totalVal,
                            status = InvoiceStatus.PENDING,
                            isHygieneProtected = isHygieneProtected,
                            autoReleaseHoursRemaining = 48,
                            digitalSignature = signature
                        )
                        onCreateInvoice(invoice)
                    }
                },
                enabled = title.isNotBlank() && priceVal > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تایید و ارسال فاکتور رسمی", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun ShippingOptionChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) Color(0xFF0284C7) else MaterialTheme.colorScheme.surfaceVariant
    val bgColor = if (selected) Color(0xFF0284C7).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .background(bgColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
