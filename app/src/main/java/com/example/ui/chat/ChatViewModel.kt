package com.example.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.local.InvoiceEntity
import com.example.repository.ChatOpenResult
import com.example.repository.ChatRepository
import com.example.repository.MarketOrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MarketOrderRepository(application.applicationContext)
    private val chatRepository = ChatRepository(application.applicationContext)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _connectionHint = MutableStateFlow<String?>(null)
    val connectionHint: StateFlow<String?> = _connectionHint.asStateFlow()

    private val _isServerSynced = MutableStateFlow(false)
    val isServerSynced: StateFlow<Boolean> = _isServerSynced.asStateFlow()

    private var activeChatId: String? = null
    private var localOnly: Boolean = false

    init {
        viewModelScope.launch {
            repository.allInvoices.collect { invoiceEntities: List<InvoiceEntity> ->
                if (invoiceEntities.isNotEmpty()) {
                    _messages.update { currentList ->
                        currentList.map { msg ->
                            if (msg.type == MessageType.INVOICE && msg.invoice != null) {
                                val updated = invoiceEntities.find { it.id == msg.invoice.id }
                                if (updated != null) {
                                    msg.copy(
                                        invoice = msg.invoice.copy(
                                            status = updated.status,
                                            trackingCode = updated.trackingCode,
                                            courierNote = updated.courierNote,
                                            exchangeRequestReason = updated.exchangeRequestReason,
                                            digitalSignature = updated.digitalSignature
                                        )
                                    )
                                } else msg
                            } else msg
                        }
                    }
                }
            }
        }
    }

    fun loadConversation(peerUserId: String) {
        viewModelScope.launch {
            _connectionHint.value = null
            _isServerSynced.value = false
            activeChatId = null
            localOnly = false
            _messages.value = emptyList()

            when (val open = chatRepository.openOrCreateChat(peerUserId)) {
                is ChatOpenResult.Ready -> {
                    activeChatId = open.chatId
                    localOnly = false
                    _isServerSynced.value = true
                    _connectionHint.value = null
                    val remote = chatRepository.getMessages(open.chatId)
                    if (remote.isNotEmpty()) {
                        _messages.value = remote
                    }
                }
                is ChatOpenResult.LocalOnly -> {
                    activeChatId = null
                    localOnly = true
                    _isServerSynced.value = false
                    _connectionHint.value = open.reason
                }
            }
        }
    }

    fun sendMessage(senderId: String, text: String) {
        val localMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            text = text
        )
        _messages.update { it + localMsg }
        val chatId = activeChatId
        if (chatId == null || localOnly) {
            // Keep local bubble; server create-chat is missing.
            return
        }
        viewModelScope.launch {
            val remote = chatRepository.sendMessage(chatId, text)
            if (remote != null) {
                _messages.update { list ->
                    list.map { if (it.id == localMsg.id) remote else it }
                }
            } else {
                _connectionHint.value =
                    "ارسال به سرور ناموفق بود؛ پیام روی دستگاه ذخیره شد."
            }
        }
    }

    fun sendProduct(senderId: String, product: Product) {
        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            text = "معرفی محصول: ${product.title} (${product.price} تومان)",
            type = MessageType.PRODUCT,
            product = product
        )
        _messages.update { it + newMsg }
        val chatId = activeChatId ?: return
        if (!localOnly) {
            viewModelScope.launch {
                chatRepository.sendMessage(
                    chatId = chatId,
                    text = newMsg.text,
                    type = "PRODUCT",
                    productId = product.id,
                    amount = product.price
                )
            }
        }
    }

    fun createInvoice(senderId: String, invoice: Invoice) {
        viewModelScope.launch {
            repository.saveInvoice(invoice)
            val newMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = senderId,
                text = "فاکتور رسمی #${invoice.id} به مبلغ ${invoice.totalAmount} تومان",
                type = MessageType.INVOICE,
                invoice = invoice
            )
            _messages.update { it + newMsg }
            val chatId = activeChatId
            if (chatId != null && !localOnly) {
                chatRepository.sendMessage(
                    chatId = chatId,
                    text = newMsg.text,
                    type = "INVOICE",
                    invoiceId = invoice.id,
                    amount = invoice.totalAmount,
                    status = invoice.status.name
                )
            }
        }
    }

    fun updateInvoiceStatus(invoiceId: String, status: InvoiceStatus) {
        viewModelScope.launch {
            repository.updateInvoiceStatus(invoiceId, status)
            _messages.update { current ->
                current.map { msg ->
                    if (msg.invoice?.id == invoiceId) {
                        msg.copy(invoice = msg.invoice.copy(status = status))
                    } else msg
                }
            }
            val chatId = activeChatId
            if (chatId != null && !localOnly) {
                chatRepository.sendMessage(
                    chatId = chatId,
                    text = "به‌روزرسانی وضعیت فاکتور #$invoiceId: ${status.name}",
                    type = "INVOICE",
                    invoiceId = invoiceId,
                    status = status.name
                )
            }
        }
    }

    fun attachTracking(invoiceId: String, trackingCode: String, courierNote: String?) {
        viewModelScope.launch {
            repository.attachTracking(invoiceId, trackingCode, courierNote)
            _messages.update { current ->
                current.map { msg ->
                    if (msg.invoice?.id == invoiceId) {
                        msg.copy(invoice = msg.invoice.copy(status = InvoiceStatus.SHIPPED, trackingCode = trackingCode, courierNote = courierNote))
                    } else msg
                }
            }
            val chatId = activeChatId
            if (chatId != null && !localOnly) {
                chatRepository.sendMessage(
                    chatId = chatId,
                    text = "کد رهگیری مرسوله فاکتور #$invoiceId: $trackingCode",
                    type = "INVOICE",
                    invoiceId = invoiceId,
                    status = InvoiceStatus.SHIPPED.name
                )
            }
        }
    }

    fun requestExchange(invoiceId: String, reason: String) {
        viewModelScope.launch {
            repository.requestExchange(invoiceId, reason)
            _messages.update { current ->
                current.map { msg ->
                    if (msg.invoice?.id == invoiceId) {
                        msg.copy(invoice = msg.invoice.copy(status = InvoiceStatus.EXCHANGE_REQUESTED, exchangeRequestReason = reason))
                    } else msg
                }
            }
            val chatId = activeChatId
            if (chatId != null && !localOnly) {
                chatRepository.sendMessage(
                    chatId = chatId,
                    text = "درخواست تعویض برای فاکتور #$invoiceId: $reason",
                    type = "INVOICE",
                    invoiceId = invoiceId,
                    status = InvoiceStatus.EXCHANGE_REQUESTED.name
                )
            }
        }
    }

    fun sendSecurePaymentProposal(senderId: String, amount: Long) {
        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            text = "پیشنهاد پرداخت امن امانی به مبلغ $amount تومان",
            type = MessageType.SECURE_PAYMENT,
            securePaymentState = SecurePaymentState.PROPOSED,
            securePaymentAmount = amount
        )
        _messages.update { it + newMsg }
        val chatId = activeChatId ?: return
        if (!localOnly) {
            viewModelScope.launch {
                chatRepository.sendMessage(
                    chatId = chatId,
                    text = newMsg.text,
                    type = "SECURE_PAYMENT",
                    amount = amount,
                    status = SecurePaymentState.PROPOSED.name
                )
            }
        }
    }

    fun updatePaymentState(messageId: String, state: SecurePaymentState) {
        _messages.update { current ->
            current.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(securePaymentState = state)
                } else msg
            }
        }
        val chatId = activeChatId ?: return
        if (!localOnly) {
            viewModelScope.launch {
                chatRepository.sendMessage(
                    chatId = chatId,
                    text = "وضعیت پرداخت امن به ${state.name} تغییر یافت",
                    type = "SECURE_PAYMENT",
                    status = state.name
                )
            }
        }
    }
}
