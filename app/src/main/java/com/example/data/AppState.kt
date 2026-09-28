package com.example.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class UserMode(val title: String, val tabLabel: String) {
    CUSTOMER(title = "حالت خریدار", tabLabel = "سبد خرید"),
    STORE(title = "حالت فروشگاه", tabLabel = "سفارشات")
}

data class TryOnBooking(
    val id: String,
    val storeName: String,
    val storeAvatar: String,
    val productTitle: String,
    val productImage: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val bookingCode: String,
    val storeAddress: String,
    val status: String
)

object AppState {
    // Demo / Test User Profiles
    val defaultCustomerUser = User(
        id = "user_me_01",
        username = "sahar_rad",
        name = "سحر رادمان",
        gmail = "sahar.radman@gmail.com",
        phone = "۰۹۱۲۳۴۵۶۷۸۹",
        city = "تهران",
        interests = "علاقه‌مند به استایل کژوال، مد پایدار، اکسسوری‌های مینیمال، تکنولوژی و کتاب",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
        bannerUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=1080&q=80",
        bio = "طراح گرافیک و شیفته مد روز ✨ | ساکن تهران | دوستدار استایل‌های ساده و باکیفیت",
        rating = 5.0f,
        followersCount = 1420,
        followingCount = 88,
        isVerified = true,
        isSeller = false,
        accountType = AccountType.REGULAR_USER,
        storeApplication = null
    )

    val defaultStoreUser = User(
        id = "u_me",
        username = "kook_style",
        name = "بوتیک کوک استایل",
        gmail = "store@kook.orangeit.site",
        phone = "۰۲۱-۲۲۰۸۹۳۴۱",
        city = "تهران",
        avatarUrl = "https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?auto=format&fit=crop&w=400&q=80",
        bannerUrl = "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?auto=format&fit=crop&w=1080&q=80",
        bio = "جدیدترین کالکشن‌های پوشاک و استایل خیابانی 👗🧥 | تایید شده رسمی اصناف Orange IT | ارسال فوری 🚚",
        rating = 4.9f,
        followersCount = 45800,
        followingCount = 120,
        isVerified = true,
        isSeller = true,
        accountType = AccountType.STORE_OWNER,
        storeCategory = "پوشاک و مد",
        location = StoreLocation(
            latitude = 35.7830,
            longitude = 51.3750,
            province = "تهران",
            city = "تهران",
            neighborhood = "سعادت‌آباد",
            address = "تهران، سعادت‌آباد، میدان کاج، مجتمع تجاری سروستان، طبقه همکف، واحد ۱۲",
            postalCode = "۱۹۹۷۸۱۴۵۲۳"
        ),
        openHours = "۱۰:۰۰ الی ۲۲:۳۰",
        phoneOffice = "۰۲۱-۲۲۰۸۹۳۴۱",
        storeApplication = StoreApplication(
            id = "app_approved_01",
            storeName = "بوتیک کوک استایل",
            category = "پوشاک و مد",
            city = "تهران",
            address = "تهران، سعادت‌آباد، میدان کاج، پاساژ سروستان",
            nationalId = "۰۰۱۸۲۷۳۶۴۵",
            documentTitle = "پروانه کسب معتبر اتحادیه پوشاک",
            description = "فروشگاه فعال دارای نماد اعتماد و تاییدیه رسمی اصناف",
            submittedAt = "تایید شده",
            status = StoreApplicationStatus.APPROVED,
            adminFeedback = "فروشگاه با موفقیت تایید گردید."
        )
    )

    val guestUser = User(
        id = "user_guest",
        username = "guest_user",
        name = "کاربر مهمان",
        gmail = "",
        phone = "",
        city = "تهران",
        interests = "برای تنظیم سلیقه و علاقه‌مندی‌ها وارد حساب خود شوید",
        avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=400&q=80",
        bannerUrl = null,
        bio = "جهت خرید، ثبت سفارش و راه‌اندازی ویترین بوتیک وارد حساب کاربری شوید.",
        rating = 5.0f,
        followersCount = 0,
        followingCount = 0,
        isVerified = false,
        isSeller = false,
        accountType = AccountType.REGULAR_USER,
        storeApplication = null
    )

    // Current active role/mode of the app navigation: automatically derived from user account
    val currentUserMode: UserMode
        get() = if (currentUser.isSeller || currentUser.accountType == AccountType.STORE_OWNER) UserMode.STORE else UserMode.CUSTOMER

    // User authentication state (starts clean, ready for login)
    var isLoggedIn by mutableStateOf(false)

    // Current active user profile
    var currentUser by mutableStateOf(guestUser)

    // Customer liked / favorite categories for personalized store showcase
    var customerLikedCategories by mutableStateOf(
        setOf("پوشاک و مد", "کیف و کفش", "چرم طبیعی", "استریت‌استایل")
    )

    fun toggleLikedCategory(category: String) {
        customerLikedCategories = if (customerLikedCategories.contains(category)) {
            customerLikedCategories - category
        } else {
            customerLikedCategories + category
        }
    }

    fun isCategoryLiked(category: String): Boolean {
        return customerLikedCategories.contains(category)
    }

    // Dynamic UI Handshake & Remote Theme Configuration from Server
    var uiConfig by mutableStateOf(com.example.network.UiHandshakeResponse())
    var isUiSyncedWithBackend by mutableStateOf(false)
    var uiSyncStatusText by mutableStateOf("در حال همگام‌سازی بصری با سرور...")

    fun register(
        fullName: String,
        gmail: String,
        phone: String,
        city: String,
        interests: String,
        password: String
    ) {
        val username = gmail.substringBefore("@").replace(".", "_")
        currentUser = User(
            id = "user_${System.currentTimeMillis()}",
            username = username,
            name = fullName,
            gmail = gmail,
            phone = phone,
            city = city,
            interests = interests,
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=1080&q=80",
            bio = "کاربر تازه کوک ✨ | ساکن $city",
            rating = 5.0f,
            followersCount = 0,
            followingCount = 12,
            isVerified = true,
            isSeller = false,
            accountType = AccountType.REGULAR_USER,
            storeApplication = null
        )
        isLoggedIn = true
    }

    fun login(identifier: String) {
        isLoggedIn = true
        // If logging in as an existing store owner or regular user
        if (identifier.contains("kook", ignoreCase = true) || identifier.contains("shop", ignoreCase = true) || identifier.contains("store", ignoreCase = true)) {
            currentUser = currentUser.copy(
                isSeller = true,
                accountType = AccountType.STORE_OWNER,
                name = "بوتیک کوک استایل",
                storeCategory = "پوشاک و مد"
            )
        } else {
            isLoggedIn = true
        }
    }

    fun submitStoreApplication(
        storeName: String,
        category: String,
        city: String,
        address: String,
        nationalId: String,
        documentTitle: String,
        description: String
    ) {
        val app = StoreApplication(
            id = "app_${System.currentTimeMillis()}",
            storeName = storeName,
            category = category,
            city = city,
            address = address,
            nationalId = nationalId,
            documentTitle = documentTitle,
            description = description,
            submittedAt = "امروز - ساعت " + java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            status = StoreApplicationStatus.PENDING_REVIEW,
            adminFeedback = "مدارک صنفی و هویتی شما با موفقیت ثبت شد و در صف بررسی واحد تایید صلاحیت فروشندگان کوک قرار دارد."
        )
        currentUser = currentUser.copy(
            storeApplication = app
        )
    }

    fun approveStoreApplication() {
        val currentApp = currentUser.storeApplication
        if (currentApp != null) {
            val approvedApp = currentApp.copy(
                status = StoreApplicationStatus.APPROVED,
                adminFeedback = "مدارک شما تایید شد. دسترسی به پنل فروشندگان فعال گردید."
            )
            currentUser = currentUser.copy(
                isSeller = true,
                accountType = AccountType.STORE_OWNER,
                name = currentApp.storeName,
                storeCategory = currentApp.category,
                storeApplication = approvedApp,
                location = StoreLocation(
                    latitude = 35.7830,
                    longitude = 51.3750,
                    province = "تهران",
                    city = currentApp.city,
                    neighborhood = "مرکز شهر",
                    address = currentApp.address
                )
            )
        }
    }

    fun logout() {
        isLoggedIn = false
        currentUser = guestUser
        cartItems = emptyList()
        customerOrders = emptyList()
        customerBookings = emptyList()
        transactions = emptyList()
        customerWalletBalance = 0L
        kookCoins = 0
    }

    fun updateProfile(name: String, username: String, bio: String, city: String, interests: String) {
        currentUser = currentUser.copy(
            name = name,
            username = username,
            bio = bio,
            city = city,
            interests = interests
        )
    }

    // ==========================================
    // E-Commerce, Cart & Orders State (Clean State)
    // ==========================================
    var cartItems by mutableStateOf<List<CartItem>>(emptyList())
    var customerOrders by mutableStateOf<List<StoreOrder>>(emptyList())
    var customerBookings by mutableStateOf<List<TryOnBooking>>(emptyList())

    // Production Mode & Live Server State
    var isProductionMode by mutableStateOf(true)
    var isServerOnline by mutableStateOf(true)
    var serverStatusText by mutableStateOf("ارتباط امن با هسته مرکزی برقرار است")
    var isDemoDataLoaded by mutableStateOf(false)
    var lastMockDataReloadTime by mutableStateOf("همگام با سرور امن ابری")

    suspend fun testServerConnection(context: android.content.Context): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val api = com.example.network.ApiClient.getApiService(context)
                val health = api.checkHealth()
                val ok = health.isSuccessful
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    isServerOnline = ok
                    serverStatusText = if (ok) {
                        "پایگاه داده زنده و ارتباط امن فعال است"
                    } else {
                        "سرور پاسخ نامعتبر داد (${health.code()})"
                    }
                }
                ok
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    isServerOnline = false
                    serverStatusText = "عدم اتصال به سرور: ${e.javaClass.simpleName}"
                }
                false
            }
        }
    }

    fun reloadAllMockData() {
        cartItems = listOf(
            CartItem(
                product = Product("prod1", "هودی دورس لش کلاه‌دار", 750000, 950000, imageUrl = "https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=1080&q=80"),
                storeName = "تهران استایل",
                quantity = 1,
                selectedColor = "مشکی مات",
                selectedSize = "L"
            ),
            CartItem(
                product = Product("prod2", "شلوار کارگو کتان ترک", 890000, imageUrl = "https://images.unsplash.com/photo-1529139574466-a303027c1d8b?auto=format&fit=crop&w=1080&q=80"),
                storeName = "استایل خیابانی",
                quantity = 2,
                selectedColor = "سبز ارتشی",
                selectedSize = "32"
            )
        )

        customerOrders = listOf(
            StoreOrder(
                orderId = "ORD-۱۰۲۳",
                customerName = currentUser.name,
                customerAvatar = currentUser.avatarUrl,
                customerPhone = currentUser.phone.ifEmpty { "۰۹۱۲۳۴۵۶۷۸۹" },
                customerCity = currentUser.city,
                customerAddress = "تهران، خیابان ولیعصر، کوچه نسترن، پلاک ۲۴، واحد ۳",
                orderedTimeAgo = "دیروز ساعت ۱۶:۲۰",
                items = listOf(
                    StoreOrderItem(
                        product = Product("prod3", "کت جین اورسایز زنانه/مردانه", 1250000, imageUrl = "https://images.unsplash.com/photo-1516257984-b1b4d707412e?auto=format&fit=crop&w=1080&q=80"),
                        quantity = 1,
                        selectedColor = "آبی یخی وینتیج",
                        selectedSize = "Free Size",
                        unitPrice = 1250000
                    )
                ),
                totalAmount = 1250000,
                status = OrderStatus.SHIPPED,
                trackingCode = "۱۹۸۲۷۳۶۴۵۰۱۹۲۸۳۷۴۶",
                storeName = "خانه جین",
                isEscrowProtected = true
            ),
            StoreOrder(
                orderId = "ORD-۱۰۲۴",
                customerName = currentUser.name,
                customerAvatar = currentUser.avatarUrl,
                customerPhone = currentUser.phone.ifEmpty { "۰۹۱۲۳۴۵۶۷۸۹" },
                customerCity = currentUser.city,
                customerAddress = "تهران، شهرک غرب، خیابان مهستان، پلاک ۵",
                orderedTimeAgo = "۲ روز پیش",
                items = listOf(
                    StoreOrderItem(
                        product = Product("prod5", "کتونی اسپرت سفید نایک", 2500000, imageUrl = "https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?auto=format&fit=crop&w=1080&q=80"),
                        quantity = 1,
                        selectedColor = "تمام سفید",
                        selectedSize = "43",
                        unitPrice = 2500000
                    )
                ),
                totalAmount = 2500000,
                status = OrderStatus.DELIVERED,
                trackingCode = "۸۳۷۴۶۵۰۱۹۲۸۳۷۴۶۵۱۲",
                storeName = "بوتیک کوک استایل",
                isEscrowProtected = true
            )
        )

        customerBookings = listOf(
            TryOnBooking(
                id = "TRY-8921",
                storeName = "بوتیک کوک استایل",
                storeAvatar = "https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?auto=format&fit=crop&w=200&q=80",
                productTitle = "هودی دورس لش کلاه‌دار",
                productImage = "https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=600&q=80",
                scheduledDate = "فردا چهارشنبه",
                scheduledTime = "ساعت ۱۷:۳۰ تا ۱۸:۳۰",
                bookingCode = "KOOK-PRO-9821",
                storeAddress = "تهران، سعادت‌آباد، میدان کاج، مجتمع سروستان، واحد ۱۲",
                status = "تایید شده (اتاق پرو رزرو شد)"
            ),
            TryOnBooking(
                id = "TRY-8740",
                storeName = "تهران استایل",
                storeAvatar = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=200&q=80",
                productTitle = "شلوار کارگو کتان ترک",
                productImage = "https://images.unsplash.com/photo-1529139574466-a303027c1d8b?auto=format&fit=crop&w=600&q=80",
                scheduledDate = "جمعه این هفته",
                scheduledTime = "ساعت ۱۹:۰۰ تا ۲۰:۰۰",
                bookingCode = "KOOK-PRO-7640",
                storeAddress = "تهران، خیابان ولیعصر، بالاتر از ظفر، برج پایتخت",
                status = "رزرو فعال"
            )
        )

        customerWalletBalance = 3500000L
        kookCoins = 480
        isDemoDataLoaded = true
        lastMockDataReloadTime = "هم‌اکنون بارگذاری شد"
    }

    fun bookTryOn(storeName: String, storeAvatar: String, productTitle: String, productImage: String, date: String, time: String, address: String): TryOnBooking {
        val newBooking = TryOnBooking(
            id = "TRY-" + (1000..9999).random(),
            storeName = storeName,
            storeAvatar = storeAvatar,
            productTitle = productTitle,
            productImage = productImage,
            scheduledDate = date,
            scheduledTime = time,
            bookingCode = "KOOK-PRO-" + (1000..9999).random(),
            storeAddress = address,
            status = "تایید شده (نوبت شما ثبت شد)"
        )
        customerBookings = listOf(newBooking) + customerBookings
        return newBooking
    }

    // Customer Wallet & Coins (Monetization & Gamification) - Starts clean
    var customerWalletBalance by mutableStateOf(0L)
    var kookCoins by mutableStateOf(0)

    var transactions by mutableStateOf<List<WalletTransaction>>(emptyList())

    fun addToCart(product: Product, storeName: String, quantity: Int = 1, color: String? = null, size: String? = null) {
        val existing = cartItems.find { it.product.id == product.id }
        cartItems = if (existing != null) {
            cartItems.map { if (it.product.id == product.id) it.copy(quantity = it.quantity + quantity) else it }
        } else {
            cartItems + CartItem(
                product = product,
                storeName = storeName,
                quantity = quantity,
                selectedColor = color,
                selectedSize = size
            )
        }
    }

    fun updateCartQuantity(productId: String, newQty: Int) {
        cartItems = if (newQty <= 0) {
            cartItems.filter { it.product.id != productId }
        } else {
            cartItems.map { if (it.product.id == productId) it.copy(quantity = newQty) else it }
        }
    }

    fun removeFromCart(productId: String) {
        cartItems = cartItems.filter { it.product.id != productId }
    }

    fun isGuestUser(): Boolean {
        return !isLoggedIn || currentUser.id == "user_guest"
    }

    fun checkoutCart(): StoreOrder? {
        if (isGuestUser()) return null
        if (cartItems.isEmpty()) return null
        val total = cartItems.sumOf { it.product.price * it.quantity }
        val orderItems = cartItems.map {
            StoreOrderItem(
                product = it.product,
                quantity = it.quantity,
                selectedColor = it.selectedColor,
                selectedSize = it.selectedSize,
                unitPrice = it.product.price
            )
        }
        val orderId = "ORD-" + (1000..9999).random()
        val newOrder = StoreOrder(
            orderId = orderId,
            customerName = currentUser.name,
            customerAvatar = currentUser.avatarUrl,
            customerPhone = currentUser.phone.ifEmpty { "۰۹۱۲۳۴۵۶۷۸۹" },
            customerCity = currentUser.city,
            customerAddress = "تهران، میدان تجریش، پلاک ۴۲",
            orderedTimeAgo = "هم‌اکنون",
            items = orderItems,
            totalAmount = total,
            status = OrderStatus.PENDING,
            storeName = cartItems.firstOrNull()?.storeName ?: "فروشگاه کوک",
            isEscrowProtected = true
        )

        // Add to customer orders
        customerOrders = listOf(newOrder) + customerOrders
        // Add to store orders in MockData
        MockData.addStoreOrder(newOrder)

        // Record transaction
        transactions = listOf(
            WalletTransaction(
                id = "TX-" + (100..999).random(),
                title = "پرداخت سفارش $orderId (پرداخت امن)",
                amount = total,
                isIncome = false,
                date = "هم‌اکنون",
                status = "تایید شاپرک - در حساب امانی",
                trackingCode = "ESC-" + (100000..999999).random()
            )
        ) + transactions

        // Clear cart
        cartItems = emptyList()
        return newOrder
    }

    fun buyDirectly(product: Product, storeName: String): StoreOrder? {
        if (isGuestUser()) return null
        val orderId = "ORD-" + (1000..9999).random()
        val newOrder = StoreOrder(
            orderId = orderId,
            customerName = currentUser.name,
            customerAvatar = currentUser.avatarUrl,
            customerPhone = currentUser.phone.ifEmpty { "۰۹۱۲۳۴۵۶۷۸۹" },
            customerCity = currentUser.city,
            customerAddress = "تهران، میدان تجریش، پلاک ۴۲",
            orderedTimeAgo = "هم‌اکنون",
            items = listOf(
                StoreOrderItem(
                    product = product,
                    quantity = 1,
                    unitPrice = product.price
                )
            ),
            totalAmount = product.price,
            status = OrderStatus.PENDING,
            storeName = storeName,
            isEscrowProtected = true
        )
        customerOrders = listOf(newOrder) + customerOrders
        MockData.addStoreOrder(newOrder)

        transactions = listOf(
            WalletTransaction(
                id = "TX-" + (100..999).random(),
                title = "خرید مستقیم کالا (${product.title})",
                amount = product.price,
                isIncome = false,
                date = "هم‌اکنون",
                status = "پرداخت امن شاپرک",
                trackingCode = "ESC-" + (100000..999999).random()
            )
        ) + transactions

        return newOrder
    }

    fun publishProductPost(
        title: String,
        price: Long,
        category: String,
        description: String,
        imageUrl: String?,
        location: StoreLocation
    ): Post {
        val prodId = "prod_" + System.currentTimeMillis()
        val finalImage = imageUrl ?: "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=1080&q=80"
        val newProduct = Product(
            id = prodId,
            title = title,
            price = price,
            inStock = true,
            imageUrl = finalImage
        )

        val updatedUser = currentUser.copy(
            isSeller = true,
            accountType = AccountType.STORE_OWNER,
            location = location,
            storeCategory = category
        )
        currentUser = updatedUser

        val newPost = Post(
            id = "p_" + System.currentTimeMillis(),
            author = updatedUser,
            description = "$title | $description #$category #کوک_مارکت",
            likesCount = 1,
            commentsCount = 0,
            sharesCount = 0,
            savesCount = 0,
            product = newProduct,
            imageUrl = finalImage,
            videoUrl = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_1MB.mp4"
        )

        MockData.addPost(newPost)
        return newPost
    }

    fun sendLiveGift(coinCost: Int): Boolean {
        if (kookCoins >= coinCost) {
            kookCoins -= coinCost
            return true
        }
        return false
    }

    fun addWalletFunds(amount: Long) {
        customerWalletBalance += amount
        transactions = listOf(
            WalletTransaction(
                id = "TX-" + (100..999).random(),
                title = "شارژ کیف پول کاربری",
                amount = amount,
                isIncome = true,
                date = "هم‌اکنون",
                status = "موفق",
                trackingCode = "DEP-" + (100000..999999).random()
            )
        ) + transactions
    }
}
