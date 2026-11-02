package com.example.data

enum class AccountType(val title: String) {
    REGULAR_USER("کاربر عادی (خریدار)"),
    STORE_OWNER("فروشگاه تایید شده (فروشنده)")
}

enum class StoreApplicationStatus(val title: String, val colorHex: Long) {
    NOT_SUBMITTED("ثبت‌نام نشده", 0xFF78909C),
    PENDING_REVIEW("در حال بررسی مدارک", 0xFFFF9800),
    APPROVED("تایید شده و فعال", 0xFF4CAF50),
    REJECTED("نیاز به اصلاح مدارک", 0xFFF44336)
}

data class StoreApplication(
    val id: String = "app_1",
    val storeName: String,
    val category: String,
    val city: String,
    val address: String,
    val nationalId: String,
    val documentTitle: String,
    val documentUri: String? = null,
    val description: String,
    val submittedAt: String,
    val status: StoreApplicationStatus = StoreApplicationStatus.PENDING_REVIEW,
    val adminFeedback: String? = null
)

data class StoreLocation(
    val latitude: Double,
    val longitude: Double,
    val province: String = "تهران",
    val city: String = "تهران",
    val neighborhood: String,
    val address: String,
    val postalCode: String? = null
)

data class User(
    val id: String,
    val username: String,
    val name: String,
    val avatarUrl: String,
    val bannerUrl: String? = null,
    val bio: String? = null,
    val gmail: String = "",
    val phone: String = "",
    val city: String = "تهران",
    val interests: String = "",
    val rating: Float = 0f,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isVerified: Boolean = false,
    val isLive: Boolean = false,
    val isSeller: Boolean = false,
    val accountType: AccountType = if (isSeller) AccountType.STORE_OWNER else AccountType.REGULAR_USER,
    val storeApplication: StoreApplication? = null,
    val location: StoreLocation? = null,
    val storeCategory: String = "پوشاک و مد",
    val openHours: String = "۱۰:۰۰ الی ۲۲:۰۰",
    val phoneOffice: String? = null
)

data class Product(
    val id: String,
    val title: String,
    val price: Long,
    val oldPrice: Long? = null,
    val inStock: Boolean = true,
    val imageUrl: String? = null,
    val category: String = "پوشاک و مد",
    val availableColors: List<String> = listOf("مشکی", "سرمه‌ای", "قهوه‌ای", "سفید"),
    val availableSizes: List<String> = listOf("M", "L", "XL", "Free Size"),
    val stockCount: Int = 12
)

data class Post(
    val id: String,
    val author: User,
    val description: String,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val savesCount: Int = 0,
    val product: Product?,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val audioTrack: String = "آهنگ اصلی - کاربر",
    val imageUrl: String,
    val videoUrl: String? = null
)

data class Wallet(
    val coins: Int = 0,
    val balance: Long = 0L
)

enum class OrderStatus(val title: String, val colorHex: Long) {
    PENDING(title = "در انتظار تایید فروشگاه", colorHex = 0xFFFF9800),
    PREPARING(title = "در حال آماده‌سازی و بسته‌بندی", colorHex = 0xFF2196F3),
    SHIPPED(title = "ارسال شده (کد رهگیری پست)", colorHex = 0xFF9C27B0),
    DELIVERED(title = "تحویل داده شده به مشتری", colorHex = 0xFF4CAF50),
    EXCHANGE_REQUESTED(title = "درخواست تعویض سایز", colorHex = 0xFFE65100),
    CANCELLED(title = "لغو شده", colorHex = 0xFFF44336)
}

data class CartItem(
    val product: Product,
    val storeName: String,
    var quantity: Int = 1,
    val isSurpriseGift: Boolean = false,
    val selectedColor: String? = null,
    val selectedSize: String? = null
)

data class WalletTransaction(
    val id: String,
    val title: String,
    val amount: Long,
    val isIncome: Boolean,
    val date: String,
    val status: String = "موفق",
    val trackingCode: String
)

data class StoreOrderItem(
    val product: Product,
    val quantity: Int,
    val selectedColor: String? = null,
    val selectedSize: String? = null,
    val unitPrice: Long
)

data class StoreOrder(
    val orderId: String,
    val customerName: String,
    val customerAvatar: String,
    val customerPhone: String,
    val customerCity: String,
    val customerAddress: String,
    val orderedTimeAgo: String,
    val items: List<StoreOrderItem>,
    val totalAmount: Long,
    val paymentMethod: String = "پرداخت آنلاین امن (شاپرک)",
    val status: OrderStatus,
    val trackingCode: String? = null,
    val storeName: String = "فروشگاه کوک",
    val isEscrowProtected: Boolean = true,
    val autoReleaseHoursRemaining: Int = 48,
    val isHygieneProtected: Boolean = false,
    val exchangeReason: String? = null,
    val postalCode: String = "۱۹۹۴۵۶۷۱۲۰",
    /** Server-side owner ids — used for scoped filtering (public GET /api/orders leaks all rows). */
    val customerId: String? = null,
    val storeId: String? = null
)
