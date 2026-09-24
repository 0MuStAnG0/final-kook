package com.example.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

// Data Transfer Objects (DTOs)
data class LoginRequest(
    val identifier: String = "", // Gmail or Phone
    val password: String = ""
)

data class RegisterRequest(
    val fullName: String,
    val gmail: String,
    val phone: String,
    val city: String,
    val interests: String,
    val password: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val fullName: String,
    val role: String, // "USER" or "STORE"
    val message: String? = null
)

data class StoreApplicationRequest(
    val storeName: String,
    val category: String,
    val city: String,
    val address: String,
    val nationalId: String,
    val documentTitle: String,
    val documentUrl: String? = null,
    val description: String
)

data class StoreApplicationResponse(
    val applicationId: String,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val message: String,
    val submittedAt: String,
    val adminFeedback: String? = null
)

data class UserProfileDto(
    val id: String,
    val fullName: String,
    val username: String,
    val gmail: String,
    val phone: String,
    val city: String,
    val interests: String,
    val role: String,
    val avatarUrl: String?,
    val storeStatus: String? = null
)

data class ProductDto(
    val id: String = "",
    val title: String = "",
    val price: Long = 0L,
    val oldPrice: Long? = null,
    val inStock: Boolean = true,
    val imageUrl: String? = null
)

data class PostAuthorDto(
    val id: String = "",
    val username: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val bannerUrl: String? = null,
    val bio: String? = null,
    val isVerified: Boolean = false,
    val isSeller: Boolean = false,
    val city: String = "تهران",
    val storeCategory: String? = null
)

data class PostDto(
    val id: String = "",
    val author: PostAuthorDto? = null,
    val description: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val savesCount: Int = 0,
    val product: ProductDto? = null,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val audioTrack: String? = "آهنگ اصلی - کوک",
    val imageUrl: String = "",
    val videoUrl: String? = null,
    val createdAt: String? = null
)

data class CreatePostRequest(
    val title: String,
    val description: String,
    val price: Long,
    val category: String,
    val imageUrl: String?,
    val videoUrl: String?,
    val city: String,
    val neighborhood: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

data class OrderDto(
    val orderId: String = "",
    @com.google.gson.annotations.SerializedName("customer_id")
    val customerId: String? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerCity: String = "",
    val customerAddress: String = "",
    @com.google.gson.annotations.SerializedName("store_id")
    val storeId: String? = null,
    val totalAmount: Long = 0L,
    val status: String = "PENDING",
    val trackingCode: String? = null,
    val storeName: String = "",
    val orderedTimeAgo: String = "هم‌اکنون",
    val isEscrowProtected: Boolean = true,
    val items: List<OrderItemDto>? = null
)

data class OrderItemDto(
    @com.google.gson.annotations.SerializedName("product_id")
    val productId: String? = null,
    val title: String? = null,
    val quantity: Int = 1,
    @com.google.gson.annotations.SerializedName("unit_price")
    val unitPrice: Long? = null,
    @com.google.gson.annotations.SerializedName("selected_color")
    val selectedColor: String? = null,
    @com.google.gson.annotations.SerializedName("selected_size")
    val selectedSize: String? = null,
    @com.google.gson.annotations.SerializedName("image_url")
    val imageUrl: String? = null
)

data class CreateOrderRequest(
    val productId: String,
    val quantity: Int = 1,
    val customerName: String,
    val customerPhone: String,
    val customerCity: String,
    val customerAddress: String,
    val totalAmount: Long,
    val storeName: String
)

// Android Kotlin: Retrofit API Service
interface OrangeItApiService {
    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body req: RefreshRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<UserProfileDto>

    @POST("api/store/apply")
    suspend fun applyForStore(@Body req: StoreApplicationRequest): Response<StoreApplicationResponse>

    @GET("api/store/status")
    suspend fun getStoreApplicationStatus(): Response<StoreApplicationResponse>

    @GET("api/users")
    suspend fun getUsers(@Header("Authorization") bearerToken: String): Response<List<UserProfileDto>>

    // Live Server Endpoints for Production
    @GET("api/posts")
    suspend fun getPosts(): Response<List<PostDto>>

    @POST("api/posts")
    suspend fun createPost(@Body req: CreatePostRequest): Response<PostDto>

    @GET("api/products")
    suspend fun getProducts(): Response<List<ProductDto>>

    @GET("api/stores")
    suspend fun getStores(): Response<List<UserProfileDto>>

    @GET("api/orders")
    suspend fun getOrders(): Response<List<OrderDto>>

    @POST("api/orders")
    suspend fun createOrder(@Body req: CreateOrderRequest): Response<OrderDto>

    @GET("api/health")
    suspend fun checkHealth(): Response<Map<String, Any>>

    // UI Handshake & Remote Visual Configuration Endpoints
    @POST("api/ui/handshake")
    suspend fun performUiHandshake(@Body req: UiHandshakeRequest): Response<UiHandshakeResponse>

    @GET("api/ui/config")
    suspend fun getUiConfig(): Response<UiHandshakeResponse>

    @GET("api/chat")
    suspend fun getChats(): Response<List<ChatDto>>

    @GET("api/chat/{chatId}/messages")
    suspend fun getChatMessages(@retrofit2.http.Path("chatId") chatId: String): Response<List<ChatMessageDto>>

    @POST("api/chat/{chatId}/messages")
    suspend fun sendChatMessage(
        @retrofit2.http.Path("chatId") chatId: String,
        @Body req: SendChatMessageRequest
    ): Response<ChatMessageDto>
}

data class SendChatMessageRequest(
    val text: String,
    val type: String = "TEXT"
)

data class ChatLastMessageDto(
    val text: String? = null,
    val type: String? = null
)

data class ChatLocationDto(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val province: String? = null,
    val city: String? = null,
    val neighborhood: String? = null,
    val address: String? = null,
    val postalCode: String? = null
)

data class ChatParticipantDto(
    val id: String = "",
    val username: String = "",
    val name: String = "",
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val bio: String? = null,
    val gmail: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val interests: String? = null,
    val rating: Float? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null,
    val isVerified: Boolean = false,
    val isLive: Boolean = false,
    val isSeller: Boolean = false,
    val storeCategory: String? = null,
    val openHours: String? = null,
    val phoneOffice: String? = null,
    val location: ChatLocationDto? = null
)

data class ChatDto(
    val id: String = "",
    val userA: String? = null,
    val userB: String? = null,
    val participants: List<ChatParticipantDto> = emptyList(),
    val lastMessage: ChatLastMessageDto? = null
)

data class ChatMessageDto(
    val id: String = "",
    @com.google.gson.annotations.SerializedName("chat_id") val chatId: String? = null,
    @com.google.gson.annotations.SerializedName("sender_id") val senderId: String = "",
    val text: String? = null,
    val type: String? = "TEXT"
)
