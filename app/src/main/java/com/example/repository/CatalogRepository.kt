package com.example.repository

import android.content.Context
import android.util.Log
import com.example.data.AccountType
import com.example.data.AppState
import com.example.data.CartItem
import com.example.data.MockData
import com.example.data.OrderStatus
import com.example.data.Post
import com.example.data.Product
import com.example.data.StoreLocation
import com.example.data.StoreOrder
import com.example.data.StoreOrderItem
import com.example.data.TryOnBooking
import com.example.data.User
import com.example.network.ApiClient
import com.example.network.CreateOrderRequest
import com.example.network.OrderDto
import com.example.network.OrderItemDto
import com.example.network.ProductDto
import com.example.network.TryOnBookingRequest
import com.example.network.UpdateOrderStatusRequest
import com.example.network.UserProfileDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class CatalogRepository(private val context: Context) {
    private val api = ApiClient.getApiService(context)

    suspend fun getStores(): List<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.getStores()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                response.body()!!.map { mapStore(it) }
            } else {
                MockData.users.filter { it.isSeller || it.location != null }
            }
        } catch (e: Exception) {
            Log.d(TAG, "getStores fallback: ${e.message}")
            MockData.users.filter { it.isSeller || it.location != null }
        }
    }

    suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProducts()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                response.body()!!.map { mapProduct(it) }
            } else {
                MockData.posts.mapNotNull { it.product }
            }
        } catch (e: Exception) {
            Log.d(TAG, "getProducts fallback: ${e.message}")
            MockData.posts.mapNotNull { it.product }
        }
    }

    suspend fun getPosts(): List<Post> = NetworkPostRepository(context).getPosts()

    suspend fun findUser(userId: String): User? {
        if (userId == AppState.currentUser.id) return AppState.currentUser
        getStores().find { it.id == userId }?.let { return it }
        getPosts().map { it.author }.distinctBy { it.id }.find { it.id == userId }?.let { return it }
        return MockData.users.find { it.id == userId }
    }

    /**
     * Fetches orders and keeps only rows owned by the signed-in customer.
     * The live server still exposes GET /api/orders publicly with full PII;
     * we never surface other customers' rows in the UI.
     */
    suspend fun getOrdersForCurrentUser(): List<StoreOrder> = withContext(Dispatchers.IO) {
        val local = AppState.customerOrders
        try {
            val response = api.getOrders()
            if (!response.isSuccessful || response.body() == null) {
                return@withContext local
            }
            val me = AppState.currentUser
            val myDigits = normalizePhone(me.phone)
            val mine = response.body()!!
                .asSequence()
                .filter { dto -> isOwnedByCustomer(dto, me.id, me.name, myDigits) }
                .map { mapOrder(it, redactForeignPii = false) }
                .toList()
            // Prefer scoped server rows; fall back to locally created orders only.
            if (mine.isNotEmpty()) mine else local
        } catch (e: Exception) {
            Log.d(TAG, "getOrdersForCurrentUser: ${e.message}")
            local
        }
    }

    /**
     * Store-panel orders: only rows for this store. Never return the public
     * dump of every customer's PII.
     */
    suspend fun getStoreOrders(): List<StoreOrder> = withContext(Dispatchers.IO) {
        try {
            val response = api.getOrders()
            if (!response.isSuccessful || response.body() == null) {
                return@withContext emptyList()
            }
            val me = AppState.currentUser
            response.body()!!
                .asSequence()
                .filter { dto -> isOwnedByStore(dto, me.id, me.name) }
                .map { mapOrder(it, redactForeignPii = false) }
                .toList()
        } catch (e: Exception) {
            Log.d(TAG, "getStoreOrders: ${e.message}")
            emptyList()
        }
    }

    suspend fun createOrder(
        product: Product,
        storeName: String,
        quantity: Int = 1,
        selectedColor: String? = null,
        selectedSize: String? = null,
        customerName: String = AppState.currentUser.name,
        customerPhone: String = AppState.currentUser.phone.ifBlank { "09120000000" },
        customerCity: String = AppState.currentUser.city.ifBlank { "تهران" },
        customerAddress: String = AppState.currentUser.location?.address ?: "تهران"
    ): Result<StoreOrder> = withContext(Dispatchers.IO) {
        try {
            val normalizedPhone = normalizePhone(customerPhone).ifBlank { "09120000000" }
            val lineItem = OrderItemDto(
                productId = product.id,
                title = product.title,
                quantity = quantity,
                unitPrice = product.price,
                selectedColor = selectedColor,
                selectedSize = selectedSize,
                imageUrl = product.imageUrl
            )
            val req = CreateOrderRequest(
                productId = product.id,
                quantity = quantity,
                customerName = customerName,
                customerPhone = normalizedPhone,
                customerCity = customerCity,
                customerAddress = customerAddress,
                totalAmount = product.price * quantity,
                storeName = storeName,
                selectedColor = selectedColor,
                selectedSize = selectedSize,
                items = listOf(lineItem)
            )
            val response = api.createOrder(req)
            if (response.isSuccessful && response.body() != null) {
                val mapped = mapOrder(response.body()!!, redactForeignPii = false)
                    .copy(customerId = response.body()!!.customerId ?: AppState.currentUser.id)
                Result.success(mapped)
            } else {
                Result.failure(IllegalStateException("ثبت سفارش ناموفق (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCartOrder(
        cartItems: List<CartItem>,
        customerName: String = AppState.currentUser.name,
        customerPhone: String = AppState.currentUser.phone.ifBlank { "09120000000" },
        customerCity: String = AppState.currentUser.city.ifBlank { "تهران" },
        customerAddress: String = AppState.currentUser.location?.address ?: "تهران، میدان تجریش، پلاک ۴۲"
    ): Result<StoreOrder> = withContext(Dispatchers.IO) {
        if (cartItems.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("سبد خرید خالی است"))
        }
        try {
            val first = cartItems.first()
            val total = cartItems.sumOf { it.product.price * it.quantity }
            val normalizedPhone = normalizePhone(customerPhone).ifBlank { "09120000000" }
            val lineItems = cartItems.map {
                OrderItemDto(
                    productId = it.product.id,
                    title = it.product.title,
                    quantity = it.quantity,
                    unitPrice = it.product.price,
                    selectedColor = it.selectedColor,
                    selectedSize = it.selectedSize,
                    imageUrl = it.product.imageUrl
                )
            }
            val req = CreateOrderRequest(
                productId = first.product.id,
                quantity = cartItems.sumOf { it.quantity },
                customerName = customerName,
                customerPhone = normalizedPhone,
                customerCity = customerCity,
                customerAddress = customerAddress,
                totalAmount = total,
                storeName = first.storeName,
                selectedColor = first.selectedColor,
                selectedSize = first.selectedSize,
                items = lineItems
            )
            val response = api.createOrder(req)
            if (response.isSuccessful && response.body() != null) {
                val mapped = mapOrder(response.body()!!, redactForeignPii = false).copy(
                    items = cartItems.map {
                        StoreOrderItem(
                            product = it.product,
                            quantity = it.quantity,
                            selectedColor = it.selectedColor,
                            selectedSize = it.selectedSize,
                            unitPrice = it.product.price
                        )
                    },
                    totalAmount = total,
                    customerId = response.body()!!.customerId ?: AppState.currentUser.id
                )
                Result.success(mapped)
            } else {
                Result.failure(IllegalStateException("ثبت سفارش سبد خرید ناموفق (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        trackingCode: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.updateOrderStatus(
                orderId = orderId,
                req = UpdateOrderStatusRequest(status = newStatus.name, trackingCode = trackingCode)
            )
            response.isSuccessful
        } catch (e: Exception) {
            Log.d(TAG, "updateOrderStatus fallback: ${e.message}")
            false
        }
    }

    suspend fun syncTryOnBooking(booking: TryOnBooking): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.createTryOnBooking(
                TryOnBookingRequest(
                    id = booking.id,
                    storeName = booking.storeName,
                    productTitle = booking.productTitle,
                    scheduledDate = booking.scheduledDate,
                    scheduledTime = booking.scheduledTime,
                    bookingCode = booking.bookingCode,
                    storeAddress = booking.storeAddress
                )
            )
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    private fun mapStore(dto: UserProfileDto): User {
        val seed = MockData.users.find { it.id == dto.id || it.username == dto.username }
        val isSeller = dto.role.equals("STORE", ignoreCase = true) ||
            !dto.storeStatus.isNullOrBlank() ||
            seed?.isSeller == true
        return User(
            id = dto.id,
            username = dto.username.ifBlank { dto.fullName },
            name = dto.fullName.ifBlank { dto.username },
            avatarUrl = dto.avatarUrl
                ?: seed?.avatarUrl
                ?: "https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?auto=format&fit=crop&w=200&q=80",
            bannerUrl = seed?.bannerUrl,
            bio = seed?.bio ?: "فروشگاه متصل به سرور کوک",
            gmail = dto.gmail,
            phone = dto.phone,
            city = dto.city.ifBlank { "تهران" },
            interests = dto.interests,
            rating = seed?.rating ?: 4.8f,
            followersCount = seed?.followersCount ?: 1000,
            followingCount = seed?.followingCount ?: 50,
            isVerified = true,
            isLive = seed?.isLive == true,
            isSeller = isSeller,
            accountType = if (isSeller) AccountType.STORE_OWNER else AccountType.REGULAR_USER,
            location = seed?.location ?: syntheticLocation(dto.id, dto.city),
            storeCategory = seed?.storeCategory ?: "پوشاک و مد",
            openHours = seed?.openHours ?: "۱۰:۰۰ الی ۲۲:۰۰",
            phoneOffice = dto.phone.ifBlank { null } ?: seed?.phoneOffice
        )
    }

    private fun mapProduct(dto: ProductDto): Product = Product(
        id = dto.id.ifBlank { "prod_${System.currentTimeMillis()}" },
        title = dto.title.ifBlank { "محصول" },
        price = dto.price,
        oldPrice = dto.oldPrice,
        inStock = dto.inStock,
        imageUrl = dto.imageUrl
    )

    private fun mapOrder(dto: OrderDto, redactForeignPii: Boolean): StoreOrder {
        val status = runCatching { OrderStatus.valueOf(dto.status.uppercase()) }
            .getOrDefault(OrderStatus.PENDING)
        val name = if (redactForeignPii) "مشتری" else dto.customerName
        val phone = if (redactForeignPii) "—" else dto.customerPhone
        val address = if (redactForeignPii) "—" else dto.customerAddress
        val items = dto.items?.map { line ->
            StoreOrderItem(
                product = Product(
                    id = line.productId ?: "line_${dto.orderId}",
                    title = line.title ?: "سفارش ${dto.storeName}",
                    price = line.unitPrice ?: dto.totalAmount,
                    imageUrl = line.imageUrl
                ),
                quantity = line.quantity.coerceAtLeast(1),
                selectedColor = line.selectedColor,
                selectedSize = line.selectedSize,
                unitPrice = line.unitPrice ?: dto.totalAmount
            )
        }.orEmpty().ifEmpty {
            listOf(
                StoreOrderItem(
                    product = Product(
                        id = "line_${dto.orderId}",
                        title = "سفارش ${dto.storeName}",
                        price = dto.totalAmount,
                        imageUrl = null
                    ),
                    quantity = 1,
                    unitPrice = dto.totalAmount
                )
            )
        }
        return StoreOrder(
            orderId = dto.orderId.ifBlank { "ORD-${System.currentTimeMillis()}" },
            customerName = name,
            customerAvatar = AppState.currentUser.avatarUrl,
            customerPhone = phone,
            customerCity = dto.customerCity,
            customerAddress = address,
            orderedTimeAgo = dto.orderedTimeAgo.ifBlank { "اخیراً" },
            items = items,
            totalAmount = dto.totalAmount,
            status = status,
            trackingCode = dto.trackingCode,
            storeName = dto.storeName.ifBlank { "فروشگاه کوک" },
            isEscrowProtected = dto.isEscrowProtected,
            customerId = dto.customerId,
            storeId = dto.storeId
        )
    }

    companion object {
        private const val TAG = "CatalogRepository"

        fun normalizePhone(raw: String): String =
            com.example.utils.ValidationUtils.normalizeDigits(raw).filter { it.isDigit() }

        fun isOwnedByCustomer(
            dto: OrderDto,
            userId: String,
            userName: String,
            phoneDigits: String
        ): Boolean {
            if (userId.isBlank() || userId == "user_guest") return false
            val cid = dto.customerId?.trim().orEmpty()
            if (cid.isNotEmpty()) {
                return cid == userId
            }
            // Only fall back to verified normalized phone number (never match by display name to prevent PII leak)
            val orderPhone = normalizePhone(dto.customerPhone)
            if (phoneDigits.length >= 10 && orderPhone == phoneDigits) return true
            return false
        }

        fun isOwnedByStore(dto: OrderDto, storeUserId: String, storeName: String): Boolean {
            val sid = dto.storeId?.trim().orEmpty()
            if (sid.isNotEmpty() && sid == storeUserId) return true
            if (sid.isEmpty() && storeName.isNotBlank() &&
                dto.storeName.equals(storeName, ignoreCase = true)
            ) {
                return true
            }
            return false
        }

        fun syntheticLocation(id: String, city: String): StoreLocation {
            val hash = abs(id.hashCode())
            val lat = 35.70 + ((hash % 1700) / 10000.0)
            val lng = 51.30 + (((hash / 17) % 2000) / 10000.0)
            return StoreLocation(
                latitude = lat,
                longitude = lng,
                province = "تهران",
                city = city.ifBlank { "تهران" },
                neighborhood = "مرکز شهر",
                address = "$city — موقعیت تقریبی فروشگاه"
            )
        }
    }
}
