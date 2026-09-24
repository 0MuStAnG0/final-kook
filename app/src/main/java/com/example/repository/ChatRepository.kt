package com.example.repository

import android.content.Context
import android.util.Log
import com.example.data.AppState
import com.example.data.ChatMessage
import com.example.data.MessageType
import com.example.data.User
import com.example.network.ApiClient
import com.example.network.ChatDto
import com.example.network.ChatMessageDto
import com.example.network.ChatParticipantDto
import com.example.network.SendChatMessageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ChatOpenResult {
    data class Ready(val chatId: String) : ChatOpenResult()
    /** Server has no create-chat API; conversation stays local-only. */
    data class LocalOnly(val reason: String) : ChatOpenResult()
}

class ChatRepository(private val context: Context) {
    private val api = ApiClient.getApiService(context)

    suspend fun getChats(): List<ChatInboxItem> = withContext(Dispatchers.IO) {
        try {
            val response = api.getChats()
            if (response.isSuccessful && response.body() != null) {
                val me = AppState.currentUser.id
                response.body()!!
                    .filter { involvesCurrentUser(it, me) }
                    .mapNotNull { mapChat(it) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.d(TAG, "getChats: ${e.message}")
            emptyList()
        }
    }

    /**
     * All inbox rows from the server (including seeded demos). Used only when
     * the signed-in user has no personal threads yet, as a discoverability aid.
     */
    suspend fun getDiscoverableChats(): List<ChatInboxItem> = withContext(Dispatchers.IO) {
        try {
            val response = api.getChats()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.mapNotNull { mapChat(it) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.d(TAG, "getDiscoverableChats: ${e.message}")
            emptyList()
        }
    }

    suspend fun getMessages(chatId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val response = api.getChatMessages(chatId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { mapMessage(it) }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.d(TAG, "getMessages: ${e.message}")
            emptyList()
        }
    }

    suspend fun sendMessage(chatId: String, text: String): ChatMessage? = withContext(Dispatchers.IO) {
        try {
            val response = api.sendChatMessage(chatId, SendChatMessageRequest(text = text))
            if (response.isSuccessful && response.body() != null) {
                mapMessage(response.body()!!)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d(TAG, "sendMessage: ${e.message}")
            null
        }
    }

    suspend fun findChatIdForUser(userId: String): String? {
        return getChats().find { it.peer.id == userId }?.chatId
            ?: getDiscoverableChats().find { it.peer.id == userId }?.chatId
    }

    /**
     * Resolve an existing thread with [peerUserId]. Live server has no
     * POST /api/chat create route (404); until that exists we open local-only.
     */
    suspend fun openOrCreateChat(peerUserId: String): ChatOpenResult {
        findChatIdForUser(peerUserId)?.let { return ChatOpenResult.Ready(it) }
        // Probe create variants — all currently 404 on production.
        val created = tryCreateChat(peerUserId)
        if (created != null) return ChatOpenResult.Ready(created)
        return ChatOpenResult.LocalOnly(
            "ساخت گفتگوی جدید روی سرور هنوز فعال نیست. پیام‌ها فقط روی این دستگاه نگه داشته می‌شوند."
        )
    }

    private suspend fun tryCreateChat(peerUserId: String): String? = withContext(Dispatchers.IO) {
        // Reserved for when backend adds POST /api/chat. Soft-fail for now.
        Log.d(TAG, "createChat unavailable for peer=$peerUserId")
        null
    }

    private fun involvesCurrentUser(dto: ChatDto, me: String): Boolean {
        if (me.isBlank() || me == "user_guest") return false
        if (dto.userA == me || dto.userB == me) return true
        return dto.participants.any { it.id == me }
    }

    private fun mapChat(dto: ChatDto): ChatInboxItem? {
        val me = AppState.currentUser.id
        val peerDto = dto.participants.firstOrNull { it.id != me && it.id.isNotBlank() }
            ?: dto.participants.firstOrNull { it.id == dto.userB || it.id == dto.userA }
            ?: dto.participants.firstOrNull()
            ?: return null
        return ChatInboxItem(
            chatId = dto.id,
            peer = mapParticipant(peerDto),
            lastMessagePreview = dto.lastMessage?.text.orEmpty()
        )
    }

    private fun mapParticipant(dto: ChatParticipantDto): User = User(
        id = dto.id,
        username = dto.username,
        name = dto.name,
        avatarUrl = dto.avatarUrl
            ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
        bannerUrl = dto.bannerUrl,
        bio = dto.bio,
        gmail = dto.gmail.orEmpty(),
        phone = dto.phone.orEmpty(),
        city = dto.city ?: "تهران",
        interests = dto.interests.orEmpty(),
        rating = dto.rating ?: 0f,
        followersCount = dto.followersCount ?: 0,
        followingCount = dto.followingCount ?: 0,
        isVerified = dto.isVerified,
        isLive = dto.isLive,
        isSeller = dto.isSeller,
        location = dto.location?.let {
            com.example.data.StoreLocation(
                latitude = it.latitude,
                longitude = it.longitude,
                province = it.province ?: "تهران",
                city = it.city ?: "تهران",
                neighborhood = it.neighborhood ?: "مرکز",
                address = it.address ?: "",
                postalCode = it.postalCode
            )
        },
        storeCategory = dto.storeCategory ?: "پوشاک و مد",
        openHours = dto.openHours ?: "۱۰:۰۰ الی ۲۲:۰۰",
        phoneOffice = dto.phoneOffice
    )

    private fun mapMessage(dto: ChatMessageDto): ChatMessage = ChatMessage(
        id = dto.id,
        senderId = dto.senderId,
        text = dto.text.orEmpty(),
        type = when (dto.type?.uppercase()) {
            "PRODUCT" -> MessageType.PRODUCT
            "INVOICE" -> MessageType.INVOICE
            "SECURE_PAYMENT" -> MessageType.SECURE_PAYMENT
            else -> MessageType.TEXT
        }
    )

    companion object {
        private const val TAG = "ChatRepository"
    }
}

data class ChatInboxItem(
    val chatId: String,
    val peer: User,
    val lastMessagePreview: String
)
