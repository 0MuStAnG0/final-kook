package com.example.ui.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*
import com.example.utils.toPersianCurrency

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    onAcceptPayment: () -> Unit,
    onConfirmDelivery: () -> Unit,
    onQuickPayInvoice: () -> Unit,
    onRejectInvoice: () -> Unit,
    onAttachTracking: () -> Unit = {},
    onRequestExchange: () -> Unit = {},
    onConfirmInvoiceDelivery: () -> Unit = {},
    onApproveExchange: () -> Unit = {},
    onViewPackingSlip: () -> Unit = {},
    onTrackShipment: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.Start else Arrangement.End
    ) {
        val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 0.dp else 16.dp,
                        bottomEnd = if (isMe) 16.dp else 0.dp
                    )
                )
                .background(bubbleColor)
                .padding(if (message.type == MessageType.TEXT) 12.dp else 0.dp)
        ) {
            when (message.type) {
                MessageType.TEXT -> {
                    Text(message.text, color = textColor)
                }
                MessageType.PRODUCT -> {
                    message.product?.let { product ->
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = product.imageUrl ?: "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?auto=format&fit=crop&w=600&q=80",
                                contentDescription = product.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(product.title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(product.price.toPersianCurrency(), color = FuchsiaAccent, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        }
                    }
                }
                MessageType.SECURE_PAYMENT -> {
                    SecurePaymentCard(message, isMe, onAcceptPayment, onConfirmDelivery)
                }
                MessageType.INVOICE -> {
                    message.invoice?.let { inv ->
                        InvoiceMessageBubble(
                            invoice = inv,
                            isMe = isMe,
                            onPayClick = onQuickPayInvoice,
                            onRejectClick = onRejectInvoice,
                            onAttachTrackingClick = onAttachTracking,
                            onRequestExchangeClick = onRequestExchange,
                            onConfirmDeliveryClick = onConfirmInvoiceDelivery,
                            onApproveExchangeClick = onApproveExchange,
                            onViewPackingSlipClick = onViewPackingSlip,
                            onTrackShipmentClick = onTrackShipment
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecurePaymentCard(
    message: ChatMessage,
    isMe: Boolean,
    onAcceptPayment: () -> Unit,
    onConfirmDelivery: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, EmeraldSuccess.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .padding(16.dp)
            .width(280.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = "خرید امن", tint = EmeraldSuccess)
            Spacer(modifier = Modifier.width(8.dp))
            Text("پیشنهاد خرید امن", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = EmeraldSuccess)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("مبلغ:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            message.securePaymentAmount?.toPersianCurrency() ?: "",
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (message.securePaymentState) {
            SecurePaymentState.PROPOSED -> {
                if (isMe) {
                    Text("منتظر تایید خریدار...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                } else {
                    Button(
                        onClick = onAcceptPayment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fluidAnimatedGradient(shape = RoundedCornerShape(8.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("پرداخت و واریز به صندوق امن", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
            SecurePaymentState.PAID_AWAITING_DELIVERY -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IndigoPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("پرداخت شد. در انتظار تحویل کالا.", color = IndigoPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                if (!isMe) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onConfirmDelivery,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("تایید دریافت کالا", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
            SecurePaymentState.DELIVERED_FUNDS_RELEASED -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تحویل تایید شد. پول به فروشنده واریز شد.", color = EmeraldSuccess, style = MaterialTheme.typography.labelSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
            SecurePaymentState.CANCELLED -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("معامله امن لغو شد.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
            SecurePaymentState.AWAITING_PAYMENT, null -> {
                Text("در انتظار پرداخت...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAttachClick) {
                Icon(Icons.Default.Add, contentDescription = "Attach", tint = MaterialTheme.colorScheme.primary)
            }

            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                placeholder = { Text("پیام شما...") },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
