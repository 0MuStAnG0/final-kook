package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.InvoiceEntity
import com.example.repository.CatalogRepository
import com.example.repository.ChatInboxItem
import com.example.repository.ChatRepository
import com.example.repository.MarketOrderRepository
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(onBack: () -> Unit, onNavigateToChat: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { MarketOrderRepository(context) }
    val chatRepository = remember { ChatRepository(context) }
    val catalog = remember { CatalogRepository(context) }
    val invoices by repository.allInvoices.collectAsState(initial = emptyList())
    var chats by remember { mutableStateOf<List<ChatInboxItem>>(emptyList()) }
    var storeContacts by remember { mutableStateOf<List<com.example.data.User>>(emptyList()) }

    LaunchedEffect(Unit) {
        val mine = chatRepository.getChats()
        chats = mine.ifEmpty {
            // Seeded demo threads (user not a participant) — show as discoverable only.
            chatRepository.getDiscoverableChats()
        }
        storeContacts = catalog.getStores().filter { it.id != com.example.data.AppState.currentUser.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پیام‌ها و مذاکرات", fontWeight = FontWeight.Bold) },
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (invoices.isNotEmpty()) {
                item {
                    Text(
                        "فاکتورها و سفارش‌های اخیر شما",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(invoices.take(3)) { inv ->
                    RecentInvoiceSummaryCard(inv, onClick = {
                        val target = chats.firstOrNull()?.peer?.id
                            ?: storeContacts.firstOrNull()?.id
                        if (target != null) onNavigateToChat(target)
                    })
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            if (chats.isNotEmpty()) {
                item {
                    Text(
                        "گفتگوهای سرور",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(chats) { chat ->
                    ChatItem(
                        user = chat.peer,
                        subtitle = chat.lastMessagePreview,
                        onClick = { onNavigateToChat(chat.peer.id) }
                    )
                }
            } else {
                item {
                    Text(
                        "هنوز گفتگویی ندارید. از لیست فروشگاه‌ها یک گفتگو را شروع کنید. اگر سرور ساخت چت جدید را پشتیبانی نکند، پیام‌ها فقط محلی ذخیره می‌شوند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            item {
                Text(
                    "فروشگاه‌های قابل گفتگو",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(storeContacts) { user ->
                ChatItem(user = user, onClick = { onNavigateToChat(user.id) })
            }
        }
    }
}

@Composable
fun RecentInvoiceSummaryCard(invoice: InvoiceEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = IndigoPrimary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("فاکتور ${invoice.id}", fontWeight = FontWeight.Bold)
                Text(
                    "${invoice.totalAmount} تومان · ${invoice.status}",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldSuccess
                )
            }
        }
    }
}

@Composable
fun ChatItem(
    user: com.example.data.User,
    subtitle: String = user.bio ?: user.storeCategory,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.avatarUrl,
            contentDescription = user.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
