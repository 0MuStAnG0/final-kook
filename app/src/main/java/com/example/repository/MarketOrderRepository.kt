package com.example.repository

import android.content.Context
import com.example.data.*
import com.example.data.local.*
import com.example.utils.SecurityUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MarketOrderRepository(context: Context) {
    private val db = KookDatabase.getDatabase(context)
    private val invoiceDao = db.invoiceDao()
    private val addressDao = db.savedAddressDao()
    private val auditDao = db.auditEventDao()
    private val gson = Gson()

    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()

    suspend fun saveInvoice(invoice: Invoice) {
        val signature = SecurityUtils.generateInvoiceSignature(
            invoice.id,
            invoice.totalAmount,
            invoice.isHygieneProtected
        )
        val entity = InvoiceEntity(
            id = invoice.id,
            itemsJson = gson.toJson(invoice.items),
            shippingMethod = invoice.shippingMethod,
            shippingCost = invoice.shippingCost,
            totalAmount = invoice.totalAmount,
            status = invoice.status,
            trackingCode = invoice.trackingCode,
            courierNote = invoice.courierNote,
            isHygieneProtected = invoice.isHygieneProtected,
            autoReleaseHoursRemaining = invoice.autoReleaseHoursRemaining,
            exchangeRequestReason = invoice.exchangeRequestReason,
            customerAddress = invoice.customerAddress,
            customerName = invoice.customerName,
            customerPhone = invoice.customerPhone,
            postalCode = invoice.postalCode,
            digitalSignature = signature
        )
        invoiceDao.insertInvoice(entity)
        auditDao.logEvent(
            AuditEventEntity(
                entityType = "INVOICE",
                entityId = invoice.id,
                eventType = "CREATED",
                performedBy = "SELLER",
                details = "فاکتور به مبلغ ${invoice.totalAmount} با امضای امنیتی $signature صادر شد"
            )
        )
    }

    suspend fun updateInvoiceStatus(invoiceId: String, newStatus: InvoiceStatus, actor: String = "USER", note: String = "") {
        invoiceDao.updateStatus(invoiceId, newStatus)
        auditDao.logEvent(
            AuditEventEntity(
                entityType = "INVOICE",
                entityId = invoiceId,
                eventType = newStatus.name,
                performedBy = actor,
                details = "تغییر وضعیت به ${newStatus.name}. $note"
            )
        )
    }

    suspend fun attachTracking(invoiceId: String, trackingCode: String, note: String?) {
        invoiceDao.updateTracking(invoiceId, trackingCode, note, InvoiceStatus.SHIPPED)
        auditDao.logEvent(
            AuditEventEntity(
                entityType = "INVOICE",
                entityId = invoiceId,
                eventType = "SHIPPED",
                performedBy = "SELLER",
                details = "کد رهگیری پستی: $trackingCode ثبت گردید"
            )
        )
    }

    suspend fun requestExchange(invoiceId: String, reason: String) {
        invoiceDao.updateStatus(invoiceId, InvoiceStatus.EXCHANGE_REQUESTED)
        auditDao.logEvent(
            AuditEventEntity(
                entityType = "INVOICE",
                entityId = invoiceId,
                eventType = "EXCHANGE_REQUESTED",
                performedBy = "BUYER",
                details = "درخواست تعویض ثبت شد: $reason"
            )
        )
    }

    val allSavedAddresses: Flow<List<SavedAddressEntity>> = addressDao.getAllAddresses()

    suspend fun addAddress(address: SavedAddressEntity) {
        addressDao.insertAddress(address)
    }

    private val cartDao = db.cartDao()

    val allCartItems: Flow<List<CartItem>> = cartDao.getAllCartItems().map { list ->
        list.map { entity ->
            CartItem(
                product = Product(
                    id = entity.productId,
                    title = entity.productTitle,
                    price = entity.productPrice,
                    oldPrice = entity.originalPrice,
                    imageUrl = entity.productImage
                ),
                storeName = entity.storeName,
                quantity = entity.quantity,
                selectedColor = entity.selectedColor,
                selectedSize = entity.selectedSize,
                isSurpriseGift = entity.isSurpriseGift
            )
        }
    }

    suspend fun addToCart(cartItem: CartItem) {
        val entity = CartItemEntity(
            productId = cartItem.product.id,
            productTitle = cartItem.product.title,
            productPrice = cartItem.product.price,
            originalPrice = cartItem.product.oldPrice,
            productImage = cartItem.product.imageUrl ?: "",
            storeName = cartItem.storeName,
            quantity = cartItem.quantity,
            selectedColor = cartItem.selectedColor,
            selectedSize = cartItem.selectedSize,
            isSurpriseGift = cartItem.isSurpriseGift
        )
        cartDao.insertCartItem(entity)
    }

    suspend fun updateCartQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(productId)
        } else {
            cartDao.updateQuantity(productId, quantity)
        }
    }

    suspend fun removeFromCart(productId: String) {
        cartDao.deleteCartItem(productId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }
}
