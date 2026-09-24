package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.InvoiceStatus
import com.example.data.ShippingMethod

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val itemsJson: String, // Serialized List<InvoiceItem>
    val shippingMethod: ShippingMethod? = null,
    val shippingCost: Long = 0L,
    val totalAmount: Long,
    val status: InvoiceStatus = InvoiceStatus.PENDING,
    val trackingCode: String? = null,
    val courierNote: String? = null,
    val isHygieneProtected: Boolean = false,
    val autoReleaseHoursRemaining: Int = 48,
    val exchangeRequestReason: String? = null,
    val customerAddress: String = "تهران، میدان ونک، خ خدامی، پلاک ۲۴، واحد ۳",
    val customerName: String = "مریم احمدی",
    val customerPhone: String = "۰۹۱۲۹۸۷۶۵۴۳",
    val postalCode: String = "۱۹۹۴۵۶۷۱۲۰",
    val digitalSignature: String? = null, // Cryptographic verification token
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_addresses")
data class SavedAddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, // منزل، محل کار، ...
    val recipientName: String,
    val phone: String,
    val province: String,
    val city: String,
    val fullAddress: String,
    val postalCode: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "audit_events")
data class AuditEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String, // INVOICE, PAYMENT, EXCHANGE
    val entityId: String,
    val eventType: String, // CREATED, PAID, SHIPPED, EXCHANGE_REQUESTED, COMPLETED
    val performedBy: String, // BUYER, SELLER, SYSTEM
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val productTitle: String,
    val productPrice: Long,
    val originalPrice: Long? = null,
    val productImage: String,
    val storeName: String,
    val quantity: Int = 1,
    val selectedColor: String? = null,
    val selectedSize: String? = null,
    val isSurpriseGift: Boolean = false,
    val addedAtTimestamp: Long = System.currentTimeMillis()
)

