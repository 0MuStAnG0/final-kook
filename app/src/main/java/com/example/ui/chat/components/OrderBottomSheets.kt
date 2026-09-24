package com.example.ui.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Invoice
import com.example.ui.theme.EmeraldSuccess
import com.example.utils.SecurityUtils
import com.example.utils.toPersianCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachTrackingBottomSheet(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onSubmitTracking: (trackingCode: String, courierNote: String?) -> Unit
) {
    var trackingCode by remember { mutableStateOf("") }
    var courierNote by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF8B5CF6))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ثبت ارسال مرسوله پستی / پیک", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = trackingCode,
                onValueChange = { trackingCode = it },
                label = { Text("کد رهگیری ۲۴ رقمی پست یا شماره بارنامه") },
                placeholder = { Text("مثال: 14030920194820198200192") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = courierNote,
                onValueChange = { courierNote = it },
                label = { Text("توضیحات و هماهنگی ارسال (اختیاری)") },
                placeholder = { Text("مثال: تحویل پست پیشتاز گردید...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF8B5CF6).copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "با ثبت کد رهگیری، خریدار از ارسال مطلع شده و تایمر آزادسازی خودکار فعال می‌گردد.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6D28D9)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (trackingCode.isNotBlank()) {
                        onSubmitTracking(trackingCode.trim(), courierNote.trim().ifBlank { null })
                    }
                },
                enabled = trackingCode.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ثبت و اطلاع‌رسانی ارسال به خریدار", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizeExchangeRequestBottomSheet(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onSubmitExchange: (reason: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("سایز کوچک است") }
    var requestedSize by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    val reasons = listOf("سایز کوچک است", "سایز بزرگ است", "تفاوت رنگ با سفارش", "نیاز به مشخصه دیگر")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFEA580C))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("درخواست تعویض سایز / مشخصه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step-by-step exchange lifecycle guide
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("مراحل تعویض بدون ریسک:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("۱. تایید تعویض توسط فروشنده\n۲. ارسال کالای فعلی توسط خریدار به آدرس فروشگاه\n۳. ارسال سایز جدید توسط فروشگاه و آزادسازی وجه صندوق امن", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (invoice.isHygieneProtected) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF1F2),
                    border = BorderStroke(1.dp, Color(0xFFFDA4AF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "هشدار: این کالا دارای برچسب بهداشتی و پلمپ شرکتی است؛ تعویض فقط در صورت باز نشدن پلمپ امکان‌پذیر است.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9F1239)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text("دلیل درخواست تعویض:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                reasons.chunked(2).forEach { rowReasons ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowReasons.forEach { reason ->
                            FilterChip(
                                selected = (selectedReason == reason),
                                onClick = { selectedReason = reason },
                                label = { Text(reason, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = requestedSize,
                onValueChange = { requestedSize = it },
                label = { Text("سایز / رنگ مورد نیاز جدید") },
                placeholder = { Text("مثال: سایز XL، رنگ سرمه‌ای") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = additionalNotes,
                onValueChange = { additionalNotes = it },
                label = { Text("پیام برای فروشنده") },
                placeholder = { Text("هماهنگی زمان ارسال مرسوله بازگشتی...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFF7ED),
                border = BorderStroke(1.dp, Color(0xFFFDBA74)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "صندوق امن وجه را تا تحویل کالای جایگزین مسدود نگه می‌دارد.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9A3412)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val fullDetails = "$selectedReason (سایز درخواستی: ${requestedSize.ifBlank { "طبق هماهنگی" }}). ${additionalNotes.trim()}"
                    onSubmitExchange(fullDetails)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ثبت و ارسال درخواست تعویض", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostalPackingSlipBottomSheet(
    invoice: Invoice,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var isPrinted by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بارنامه پستی و حواله انبار", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Printable Label Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with logo and barcode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("پست پیشتاز جمهوری اسلامی ایران", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text("حواله ارسال مستقیم فروشگاهی", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.QrCode, contentDescription = "بارکد", modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    // Sender & Receiver details with Privacy protection
                    Text("فرستنده: فروشگاه رسمی (بازار تهران)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Text("آدرس: تهران، بازار بزرگ، سرای امید، پلاک ۲۴ | تلفن: ۰۲۱-۵۵۶۶۷۷۸۸", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("گیرنده: ${invoice.customerName} (مشتری تایید شده)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Text("آدرس: ${invoice.customerAddress} | کدپستی: ${SecurityUtils.maskPostalCode(invoice.postalCode)} | همراه: ${SecurityUtils.maskPhoneNumber(invoice.customerPhone)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    // Manifest
                    Text("محتویات مرسوله:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    invoice.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• ${item.title} ${item.sizeOrColor?.let { "($it)" } ?: ""} × ${item.quantity}", style = MaterialTheme.typography.labelSmall)
                            Text((item.unitPrice * item.quantity).toPersianCurrency(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Barcode banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("||| | |||| || ||||| |||| | ||| ||||| |||", letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                            Text("کد مرسوله: ${invoice.trackingCode ?: "IR-POST-8942180"}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString("بارنامه: ${invoice.trackingCode ?: "IR-POST-8942180"}\nگیرنده: ${invoice.customerName} - ${invoice.customerAddress}"))
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("کپی مشخصات")
                }

                Button(
                    onClick = { isPrinted = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPrinted) "چاپ شد ✓" else "چاپ برچسب پستی")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingStatusBottomSheet(
    invoice: Invoice,
    onDismiss: () -> Unit
) {
    val steps = listOf(
        Triple("ثبت مرسوله در باجه پستی مبدا", "تهران، منطقه پستی ۱۱", "تکمیل شد"),
        Triple("ارسال از مرکز مبادلات پستی", "هاب مرکزی پست چهارراه لشگر", "تکمیل شد"),
        Triple("ورود به مرکز تجزیه و مبادلات مقصد", "مشهد، مرکز مکانیزه پست", "تکمیل شد"),
        Triple("تحویل به موزع (نامه‌رسان)", "منطقه پستی سجاد مشهد", "در حال توزیع"),
        Triple("تحویل نهایی به گیرنده", "درب منزل خریدار", "در انتظار")
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF8B5CF6))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("رهگیری آنلاین مرسوله پستی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "بستن")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("شماره بارنامه پستی:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(invoice.trackingCode ?: "140309201948201", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("در حال توزیع", color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("مراحل ارسال:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            steps.forEachIndexed { index, (title, loc, status) ->
                val isDone = status == "تکمیل شد"
                val isCurrent = status == "در حال توزیع"
                val iconColor = when {
                    isDone -> EmeraldSuccess
                    isCurrent -> Color(0xFF8B5CF6)
                    else -> MaterialTheme.colorScheme.outlineVariant
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(iconColor.copy(alpha = 0.15f))
                                .border(1.5.dp, iconColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
                            } else if (isCurrent) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(iconColor))
                            }
                        }
                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(36.dp)
                                    .background(if (isDone) EmeraldSuccess else MaterialTheme.colorScheme.outlineVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.padding(bottom = if (index < steps.size - 1) 16.dp else 0.dp)) {
                        Text(title, fontWeight = if (isCurrent || isDone) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.bodySmall)
                        Text(loc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
