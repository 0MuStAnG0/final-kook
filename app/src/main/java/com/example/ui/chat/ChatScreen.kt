package com.example.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.chat.components.*
import com.example.ui.theme.*
import com.example.utils.toPersianCurrency
import com.example.utils.toPersianNumber
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: String,
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit = {},
    onNavigateToRegister: () -> Unit = onNavigateToAuth,
    viewModel: ChatViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val catalog = remember { com.example.repository.CatalogRepository(context) }
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(userId) {
        user = catalog.findUser(userId) ?: MockData.users.find { it.id == userId }
        viewModel.loadConversation(userId)
    }

    if (user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = IndigoPrimary)
        }
        return
    }
    val peer = user!!
    
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val connectionHint by viewModel.connectionHint.collectAsStateWithLifecycle()
    val isServerSynced by viewModel.isServerSynced.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val currentUserId = AppState.currentUser.id

    var myProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    LaunchedEffect(currentUserId) {
        myProducts = catalog.getPosts()
            .filter { it.author.id == currentUserId }
            .mapNotNull { it.product }
            .ifEmpty { catalog.getProducts() }
            .ifEmpty { MockData.profilePosts.mapNotNull { it.product } }
    }
    
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showProductPicker by remember { mutableStateOf(false) }
    var showInvoiceDialog by remember { mutableStateOf(false) }
    var quickPayInvoiceMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var attachTrackingInvoiceMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var requestExchangeInvoiceMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var packingSlipInvoiceMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var trackingStatusInvoiceMessage by remember { mutableStateOf<ChatMessage?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = peer.avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(peer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (isServerSynced) "متصل به سرور چت" else "حالت محلی (بدون ساخت چت سرور)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isServerSynced) EmeraldSuccess else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(currentUserId, inputText)
                        inputText = ""
                    }
                },
                onAttachClick = { showAttachmentMenu = true }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            connectionHint?.let { hint ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                MessageBubble(
                    message = msg, 
                    isMe = (msg.senderId == currentUserId || msg.senderId == AppState.currentUser.id), 
                    onAcceptPayment = {
                        viewModel.updatePaymentState(msg.id, SecurePaymentState.PAID_AWAITING_DELIVERY)
                    }, 
                    onConfirmDelivery = {
                        viewModel.updatePaymentState(msg.id, SecurePaymentState.DELIVERED_FUNDS_RELEASED)
                    },
                    onQuickPayInvoice = {
                        quickPayInvoiceMessage = msg
                    },
                    onRejectInvoice = {
                        msg.invoice?.let { inv ->
                            viewModel.updateInvoiceStatus(inv.id, InvoiceStatus.CANCELLED)
                        }
                    },
                    onAttachTracking = {
                        attachTrackingInvoiceMessage = msg
                    },
                    onRequestExchange = {
                        requestExchangeInvoiceMessage = msg
                    },
                    onConfirmInvoiceDelivery = {
                        msg.invoice?.let { inv ->
                            viewModel.updateInvoiceStatus(inv.id, InvoiceStatus.COMPLETED)
                        }
                    },
                    onApproveExchange = {
                        msg.invoice?.let { inv ->
                            viewModel.updateInvoiceStatus(inv.id, InvoiceStatus.SHIPPED)
                        }
                    },
                    onViewPackingSlip = {
                        packingSlipInvoiceMessage = msg
                    },
                    onTrackShipment = {
                        trackingStatusInvoiceMessage = msg
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        }
        
        if (showAttachmentMenu) {
            ModalBottomSheet(onDismissRequest = { showAttachmentMenu = false }) {
                Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                    Text("پیوست‌ها", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        AttachmentOption(
                            icon = Icons.Default.ShoppingBag,
                            title = "ارسال محصول",
                            color = IndigoPrimary,
                            onClick = {
                                showAttachmentMenu = false
                                showProductPicker = true
                            }
                        )
                        
                        AttachmentOption(
                            icon = Icons.Default.ReceiptLong,
                            title = "صدور فاکتور",
                            color = Color(0xFF0284C7),
                            onClick = {
                                showAttachmentMenu = false
                                showInvoiceDialog = true
                            }
                        )

                        AttachmentOption(
                            icon = Icons.Default.Security,
                            title = "خرید امن",
                            color = EmeraldSuccess,
                            onClick = {
                                showAttachmentMenu = false
                                viewModel.sendSecurePaymentProposal(currentUserId, 500000)
                            }
                        )
                    }
                }
            }
        }
        
        if (showProductPicker) {
            ModalBottomSheet(onDismissRequest = { showProductPicker = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("انتخاب محصول", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    if (myProducts.isEmpty()) {
                        Text("محصولی برای ارسال یافت نشد", modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                            items(myProducts) { product ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showProductPicker = false
                                            viewModel.sendProduct(currentUserId, product)
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(product.title, fontWeight = FontWeight.Bold)
                                        Text(product.price.toPersianCurrency(), color = FuchsiaAccent, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showInvoiceDialog) {
            CreateInvoiceBottomSheet(
                onDismiss = { showInvoiceDialog = false },
                onCreateInvoice = { invoice ->
                    showInvoiceDialog = false
                    viewModel.createInvoice(currentUserId, invoice)
                }
            )
        }

        quickPayInvoiceMessage?.let { targetMsg ->
            targetMsg.invoice?.let { inv ->
                QuickInvoicePaymentBottomSheet(
                    invoice = inv,
                    onDismiss = { quickPayInvoiceMessage = null },
                    onPaymentSuccess = {
                        quickPayInvoiceMessage = null
                        viewModel.updateInvoiceStatus(inv.id, InvoiceStatus.PAID)
                    },
                    onNavigateToRegister = onNavigateToRegister,
                    onNavigateToAuth = onNavigateToAuth
                )
            }
        }

        attachTrackingInvoiceMessage?.let { targetMsg ->
            targetMsg.invoice?.let { inv ->
                AttachTrackingBottomSheet(
                    invoice = inv,
                    onDismiss = { attachTrackingInvoiceMessage = null },
                    onSubmitTracking = { trackingCode, courierNote ->
                        attachTrackingInvoiceMessage = null
                        viewModel.attachTracking(inv.id, trackingCode, courierNote)
                    }
                )
            }
        }

        requestExchangeInvoiceMessage?.let { targetMsg ->
            targetMsg.invoice?.let { inv ->
                SizeExchangeRequestBottomSheet(
                    invoice = inv,
                    onDismiss = { requestExchangeInvoiceMessage = null },
                    onSubmitExchange = { details ->
                        requestExchangeInvoiceMessage = null
                        viewModel.requestExchange(inv.id, details)
                    }
                )
            }
        }

        packingSlipInvoiceMessage?.let { targetMsg ->
            targetMsg.invoice?.let { inv ->
                PostalPackingSlipBottomSheet(
                    invoice = inv,
                    onDismiss = { packingSlipInvoiceMessage = null }
                )
            }
        }

        trackingStatusInvoiceMessage?.let { targetMsg ->
            targetMsg.invoice?.let { inv ->
                TrackingStatusBottomSheet(
                    invoice = inv,
                    onDismiss = { trackingStatusInvoiceMessage = null }
                )
            }
        }
    }
}

@Composable
fun AttachmentOption(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.labelMedium)
    }
}
