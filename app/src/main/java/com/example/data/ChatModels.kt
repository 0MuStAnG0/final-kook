package com.example.data

enum class MessageType {
    TEXT, PRODUCT, SECURE_PAYMENT, INVOICE
}

enum class SecurePaymentState {
    PROPOSED, AWAITING_PAYMENT, PAID_AWAITING_DELIVERY, DELIVERED_FUNDS_RELEASED, CANCELLED
}

enum class InvoiceStatus {
    PENDING, PAID, SHIPPED, EXCHANGE_REQUESTED, COMPLETED, CANCELLED
}

enum class ShippingMethod {
    LOCAL_EXPRESS, POSTAL, IN_STORE_PICKUP
}

data class InvoiceItem(
    val title: String,
    val quantity: Int = 1,
    val unitPrice: Long,
    val isHygieneNonReturnable: Boolean = false,
    val sizeOrColor: String? = null
)

data class Invoice(
    val id: String,
    val items: List<InvoiceItem>,
    val shippingMethod: ShippingMethod? = null,
    val shippingCost: Long = 0L,
    val totalAmount: Long,
    val status: InvoiceStatus = InvoiceStatus.PENDING,
    val trackingCode: String? = null,
    val courierNote: String? = null,
    val isHygieneProtected: Boolean = false,
    val autoReleaseHoursRemaining: Int = 48,
    val exchangeRequestReason: String? = null,
    val customerAddress: String = AppState.currentUser.location?.address ?: "تهران، میدان تجریش، پلاک ۴۲",
    val customerName: String = AppState.currentUser.name,
    val customerPhone: String = AppState.currentUser.phone.ifBlank { "۰۹۱۲۳۴۵۶۷۸۹" },
    val postalCode: String = AppState.currentUser.location?.postalCode ?: "۱۹۹۴۵۶۷۱۲۰",
    val digitalSignature: String? = null,
    val unboxingVideoRecorded: Boolean = false
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String = "",
    val type: MessageType = MessageType.TEXT,
    val product: Product? = null,
    val securePaymentState: SecurePaymentState? = null,
    val securePaymentAmount: Long? = null,
    val invoice: Invoice? = null,
    val timestamp: String = "10:30"
)

data class Chat(
    val id: String,
    val otherUser: User,
    val messages: List<ChatMessage>
)
