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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.fluidAnimatedGradient
import com.example.utils.toPersianCurrency
import com.example.utils.toPersianNumber

@Composable
fun InvoiceMessageBubble(
    invoice: Invoice,
    isMe: Boolean,
    onPayClick: () -> Unit,
    onRejectClick: () -> Unit,
    onAttachTrackingClick: () -> Unit,
    onRequestExchangeClick: () -> Unit,
    onConfirmDeliveryClick: () -> Unit,
    onApproveExchangeClick: () -> Unit,
    onViewPackingSlipClick: () -> Unit,
    onTrackShipmentClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var isExpanded by remember { mutableStateOf(false) }

    val statusColor = when (invoice.status) {
        InvoiceStatus.PAID -> EmeraldSuccess
        InvoiceStatus.SHIPPED -> Color(0xFF8B5CF6)
        InvoiceStatus.EXCHANGE_REQUESTED -> Color(0xFFEA580C)
        InvoiceStatus.COMPLETED -> EmeraldSuccess
        InvoiceStatus.CANCELLED -> Color(0xFFEF4444)
        InvoiceStatus.PENDING -> Color(0xFF0284C7)
    }

    val statusText = when (invoice.status) {
        InvoiceStatus.PAID -> "پرداخت شده (امانت در صندوق امن)"
        InvoiceStatus.SHIPPED -> "تحویل پست شده 🚚"
        InvoiceStatus.EXCHANGE_REQUESTED -> "درخواست تعویض سایز 🔄"
        InvoiceStatus.COMPLETED -> "تکمیل و تسویه شده ✓"
        InvoiceStatus.CANCELLED -> "لغو شده"
        InvoiceStatus.PENDING -> "در انتظار پرداخت"
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .padding(14.dp)
            .width(300.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "فاکتور خرید مستقیم",
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "فاکتور رسمی Orange IT",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Cryptographic signature badge
        invoice.digitalSignature?.let { sig ->
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("امضای دیجیتال شاپرک: $sig", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Hygiene / Non-returnable shield
        if (invoice.isHygieneProtected) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF1F2),
                border = BorderStroke(1.dp, Color(0xFFFDA4AF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "کالای بهداشتی / پلمپ (غیرقابل مرجوعی سلیقه‌ای)",
                        color = Color(0xFFBE123C),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(8.dp))

        // Items preview (First item + expand toggle if multiple)
        invoice.items.forEach { item ->
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.title} × ${item.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = (item.unitPrice * item.quantity).toPersianCurrency(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                item.sizeOrColor?.let { spec ->
                    Text(
                        text = "مشخصه انتخابی: $spec",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Delivery option
        invoice.shippingMethod?.let { shipping ->
            val shippingTitle = when (shipping) {
                ShippingMethod.LOCAL_EXPRESS -> "پیک فوری شهری"
                ShippingMethod.POSTAL -> "پست پیشتاز سراسری"
                ShippingMethod.IN_STORE_PICKUP -> "تحویل حضوری در فروشگاه"
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("هزینه ارسال ($shippingTitle):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = if (invoice.shippingCost == 0L) "رایگان" else invoice.shippingCost.toPersianCurrency(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(8.dp))

        // Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("مبلغ نهایی فاکتور:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(
                text = invoice.totalAmount.toPersianCurrency(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0284C7)
            )
        }

        // Auto-release liquidity assurance banner
        if (invoice.status == InvoiceStatus.PAID || invoice.status == InvoiceStatus.SHIPPED) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldSuccess.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "تضمین نقدینگی فروشگاه (آزادسازی خودکار)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "وجه پس از تایید تحویل یا تا ${invoice.autoReleaseHoursRemaining} ساعت آینده خودکار واریز می‌گردد.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Postal Tracking Info Card
        if (invoice.trackingCode != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "کد رهگیری: ${invoice.trackingCode}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        IconButton(
                            onClick = { clipboardManager.setText(AnnotatedString(invoice.trackingCode)) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "کپی کد", modifier = Modifier.size(14.dp), tint = Color(0xFF8B5CF6))
                        }
                    }
                    invoice.courierNote?.let { note ->
                        Text(text = "توضیح: $note", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FilledTonalButton(
                        onClick = onTrackShipmentClick,
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("رهگیری آنلاین وضعیت مرسوله", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Size Exchange Notice
        if (invoice.status == InvoiceStatus.EXCHANGE_REQUESTED) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF7ED),
                border = BorderStroke(1.dp, Color(0xFFFDBA74)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "درخواست تعویض سایز ثبت شده",
                            color = Color(0xFFEA580C),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    invoice.exchangeRequestReason?.let { reason ->
                        Text(
                            text = "درخواست مشتری: $reason",
                            color = Color(0xFF9A3412),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Text(
                        text = "مبلغ در صندوق امن محفوظ است تا پس از تحویل سایز جدید تسویه شود.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onApproveExchangeClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(34.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تایید تعویض و ارسال سایز جدید", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Postal & Packing Slip Button
        if (invoice.status != InvoiceStatus.PENDING && invoice.status != InvoiceStatus.CANCELLED) {
            OutlinedButton(
                onClick = onViewPackingSlipClick,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("مشاهده بارنامه پستی و انبار", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Actions based on state
        when (invoice.status) {
            InvoiceStatus.PENDING -> {
                if (isMe) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "فاکتور ارسال شد. در انتظار پرداخت خریدار...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRejectClick,
                            modifier = Modifier.weight(0.35f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("رد", style = MaterialTheme.typography.labelMedium)
                        }
                        Button(
                            onClick = onPayClick,
                            modifier = Modifier
                                .weight(0.65f)
                                .fluidAnimatedGradient(shape = RoundedCornerShape(8.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("پرداخت سریع", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            InvoiceStatus.PAID -> {
                if (isMe) {
                    Button(
                        onClick = onAttachTrackingClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت ارسال / کد رهگیری 🚚", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldSuccess.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "پرداخت در صندوق امن شاپرک تایید شد",
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            InvoiceStatus.SHIPPED -> {
                if (isMe) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مرسوله در راه است (تسویه خودکار فعال)",
                            color = Color(0xFF8B5CF6),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRequestExchangeClick,
                            modifier = Modifier.weight(0.45f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("تعویض سایز 🔄", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onConfirmDeliveryClick,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(0.55f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("تایید تحویل ✓", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            InvoiceStatus.COMPLETED -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldSuccess.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "سفارش تحویل و تسویه شد ✅",
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            InvoiceStatus.EXCHANGE_REQUESTED -> {
                // Notice is already shown above
            }
            InvoiceStatus.CANCELLED -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEF4444).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "این فاکتور لغو شد",
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
