package com.example.repository

import android.content.Context
import android.util.Log
import com.example.data.*
import com.example.network.ApiClient
import com.example.network.CreatePostRequest
import com.example.network.PostDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

interface PostRepository {
    suspend fun getPosts(): List<Post>
    suspend fun createPost(
        title: String,
        price: Long,
        category: String,
        description: String,
        imageUrl: String?,
        location: StoreLocation
    ): Post?
    suspend fun toggleLike(postId: String): Boolean = false
    suspend fun toggleSave(postId: String): Boolean = false
    fun isConnectedToLiveServer(): Boolean
}

class NetworkPostRepository(private val context: Context) : PostRepository {
    private val apiService = ApiClient.getApiService(context)
    private var isLiveConnected = false

    override fun isConnectedToLiveServer(): Boolean = isLiveConnected

    override suspend fun toggleLike(postId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            apiService.toggleLikePost(postId).isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun toggleSave(postId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            apiService.toggleSavePost(postId).isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getPosts(): List<Post> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPosts()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val serverPosts = response.body()!!.map { dto -> mapDtoToPost(dto) }
                isLiveConnected = true
                Log.d("PostRepository", "Fetched ${serverPosts.size} posts directly from live server!")
                // Prepend any locally added posts that might not be on server yet
                val localAdded = MockData.posts.filter { local -> serverPosts.none { it.id == local.id } }
                localAdded + serverPosts
            } else {
                Log.w("PostRepository", "Server returned ${response.code()}, falling back to cached/default posts.")
                isLiveConnected = false
                MockData.posts
            }
        } catch (e: Exception) {
            Log.d("PostRepository", "Server not reachable (${e.javaClass.simpleName}). Using store cache.")
            isLiveConnected = false
            MockData.posts
        }
    }

    override suspend fun createPost(
        title: String,
        price: Long,
        category: String,
        description: String,
        imageUrl: String?,
        location: StoreLocation
    ): Post? = withContext(Dispatchers.IO) {
        try {
            val req = CreatePostRequest(
                title = title,
                description = description,
                price = price,
                category = category,
                imageUrl = imageUrl,
                videoUrl = null,
                city = location.city,
                neighborhood = location.neighborhood,
                address = location.address,
                latitude = location.latitude,
                longitude = location.longitude
            )
            val res = apiService.createPost(req)
            if (res.isSuccessful && res.body() != null) {
                isLiveConnected = true
                mapDtoToPost(res.body()!!)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d("PostRepository", "Could not send post to remote server: ${e.message}")
            null
        }
    }

    private fun mapDtoToPost(dto: PostDto): Post {
        val authorDto = dto.author
        val authorUser = User(
            id = authorDto?.id ?: "user_${dto.id}",
            username = authorDto?.username ?: "kook_creator",
            name = authorDto?.name ?: "فروشگاه تایید شده کوک",
            avatarUrl = authorDto?.avatarUrl?.ifBlank { null }
                ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
            bannerUrl = authorDto?.bannerUrl,
            bio = authorDto?.bio ?: "فروشگاه رسمی متصل به سرور کوک",
            isVerified = authorDto?.isVerified ?: true,
            isSeller = authorDto?.isSeller ?: true,
            city = authorDto?.city ?: "تهران",
            storeCategory = authorDto?.storeCategory ?: "پوشاک و مد"
        )

        val prod = dto.product?.let { p ->
            Product(
                id = p.id.ifBlank { "prod_${dto.id}" },
                title = p.title.ifBlank { "محصول بدون عنوان" },
                price = p.price,
                oldPrice = p.oldPrice,
                inStock = p.inStock,
                imageUrl = p.imageUrl ?: dto.imageUrl
            )
        }

        return Post(
            id = dto.id.ifBlank { "post_${System.currentTimeMillis()}" },
            author = authorUser,
            description = dto.description,
            likesCount = dto.likesCount,
            commentsCount = dto.commentsCount,
            sharesCount = dto.sharesCount,
            savesCount = dto.savesCount,
            product = prod,
            isLiked = dto.isLiked,
            isSaved = dto.isSaved,
            audioTrack = dto.audioTrack ?: "آهنگ رسمی کوک",
            imageUrl = dto.imageUrl.ifBlank { "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=1080&q=80" },
            videoUrl = com.example.utils.PlayerManager.resolvePlayableVideoUrl(dto.videoUrl, dto.id)
        )
    }
}

class MockPostRepository : PostRepository {
    override fun isConnectedToLiveServer(): Boolean = false
    override suspend fun getPosts(): List<Post> {
        delay(300)
        return MockData.posts
    }
    override suspend fun createPost(
        title: String,
        price: Long,
        category: String,
        description: String,
        imageUrl: String?,
        location: StoreLocation
    ): Post? = null
}

