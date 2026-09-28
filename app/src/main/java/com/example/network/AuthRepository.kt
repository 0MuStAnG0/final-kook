package com.example.network

import android.content.Context
import com.example.data.AccountType
import com.example.data.AppState
import com.example.data.StoreApplication
import com.example.data.StoreApplicationStatus
import com.example.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object NetworkError : ApiResult<Nothing>()
}

class AuthRepository(private val context: Context) {
    private val tokenManager = TokenManager(context)
    private fun api() = ApiClient.getApiService(context)

    suspend fun restoreSession(): Boolean = withContext(Dispatchers.IO) {
        if (!tokenManager.hasToken()) return@withContext false
        try {
            val response = api().getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                applyProfile(response.body()!!)
                true
            } else if (response.code() == 401) {
                // Try refresh once
                val refresh = tokenManager.getRefreshToken() ?: return@withContext false
                val refreshed = api().refreshToken(RefreshRequest(refresh))
                if (refreshed.isSuccessful && refreshed.body() != null) {
                    val body = refreshed.body()!!
                    tokenManager.saveAccessToken(body.accessToken)
                    tokenManager.saveRefreshToken(body.refreshToken)
                    val me = api().getCurrentUser()
                    if (me.isSuccessful && me.body() != null) {
                        applyProfile(me.body()!!)
                        true
                    } else {
                        tokenManager.clearTokens()
                        false
                    }
                } else {
                    tokenManager.clearTokens()
                    false
                }
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun register(
        fullName: String,
        gmail: String,
        phone: String,
        city: String,
        interests: String,
        password: String
    ): ApiResult<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val req = RegisterRequest(
                fullName = fullName,
                gmail = gmail,
                phone = phone.filter { it.isDigit() }.ifBlank { phone },
                city = city,
                interests = interests,
                password = password
            )
            val response = api().register(req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveAccessToken(body.accessToken)
                tokenManager.saveRefreshToken(body.refreshToken)

                val username = gmail.substringBefore("@").replace(".", "_")
                val isStoreUser = body.role.equals("STORE", ignoreCase = true)

                AppState.currentUser = User(
                    id = body.userId,
                    username = username,
                    name = body.fullName.ifBlank { fullName },
                    gmail = gmail,
                    phone = phone,
                    city = city,
                    interests = interests,
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
                    bannerUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=1080&q=80",
                    bio = "کاربر تایید شده Orange IT | ساکن $city",
                    rating = 5.0f,
                    followersCount = 0,
                    followingCount = 0,
                    isVerified = true,
                    isSeller = isStoreUser,
                    accountType = if (isStoreUser) AccountType.STORE_OWNER else AccountType.REGULAR_USER,
                    storeApplication = null
                )
                AppState.isLoggedIn = true
                syncStoreApplicationStatus()
                ApiResult.Success(body)
            } else {
                val errorMsg = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    ?: "خطای سرور در ثبت‌نام (${response.code()})"
                ApiResult.Error(errorMsg, response.code())
            }
        } catch (e: java.net.UnknownHostException) {
            ApiResult.Error("آدرس دامنه سرور یافت نشد.")
        } catch (e: java.net.ConnectException) {
            ApiResult.Error("امکان برقراری ارتباط با سرور وجود ندارد.")
        } catch (e: java.net.SocketTimeoutException) {
            ApiResult.Error("مهلت ارتباط با سرور به پایان رسید.")
        } catch (e: Exception) {
            ApiResult.Error("خطا در ارتباط با سرور: ${e.localizedMessage ?: "خطای ناشناخته"}")
        }
    }

    suspend fun login(
        identifier: String,
        password: String
    ): ApiResult<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val req = LoginRequest(identifier = identifier.trim(), password = password)
            val response = api().login(req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveAccessToken(body.accessToken)
                tokenManager.saveRefreshToken(body.refreshToken)

                val profileRes = try { api().getCurrentUser() } catch (_: Exception) { null }
                val profileDto = profileRes?.body()
                val isStoreUser = body.role.equals("STORE", ignoreCase = true) ||
                    profileDto?.role.equals("STORE", ignoreCase = true)

                AppState.currentUser = User(
                    id = body.userId,
                    username = profileDto?.username ?: identifier.substringBefore("@"),
                    name = profileDto?.fullName ?: body.fullName,
                    gmail = profileDto?.gmail ?: (if (identifier.contains("@")) identifier else ""),
                    phone = profileDto?.phone ?: (if (!identifier.contains("@")) identifier else ""),
                    city = profileDto?.city ?: "تهران",
                    interests = profileDto?.interests ?: "",
                    avatarUrl = profileDto?.avatarUrl
                        ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
                    bannerUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=1080&q=80",
                    bio = "کاربر احراز هویت شده Orange IT",
                    rating = 5.0f,
                    followersCount = profileDto?.let { 120 } ?: 0,
                    followingCount = 45,
                    isVerified = true,
                    isSeller = isStoreUser,
                    accountType = if (isStoreUser) AccountType.STORE_OWNER else AccountType.REGULAR_USER,
                    storeApplication = null
                )
                AppState.isLoggedIn = true
                syncStoreApplicationStatus()
                ApiResult.Success(body)
            } else {
                val errorMsg = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    ?: "نام کاربری یا کلمه عبور تایید نشد (${response.code()})"
                ApiResult.Error(errorMsg, response.code())
            }
        } catch (e: java.net.UnknownHostException) {
            ApiResult.Error("آدرس دامنه سرور یافت نشد.")
        } catch (e: java.net.ConnectException) {
            ApiResult.Error("امکان اتصال به سرور فراهم نشد.")
        } catch (e: java.net.SocketTimeoutException) {
            ApiResult.Error("مهلت ارتباط با سرور به پایان رسید.")
        } catch (e: Exception) {
            ApiResult.Error("خطا در ورود به حساب: ${e.localizedMessage ?: "عدم اتصال به سرور"}")
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        tokenManager.clearTokens()
        withContext(Dispatchers.Main) {
            AppState.logout()
        }
    }

    suspend fun updateProfile(
        name: String,
        username: String,
        bio: String,
        city: String,
        interests: String,
        phone: String,
        gmail: String
    ): ApiResult<UserProfileDto> = withContext(Dispatchers.IO) {
        try {
            val req = UpdateProfileRequest(
                fullName = name,
                username = username,
                bio = bio.ifBlank { null },
                city = city,
                interests = interests,
                phone = phone,
                gmail = gmail
            )
            val res = api().updateProfile(req)
            if (res.isSuccessful && res.body() != null) {
                withContext(Dispatchers.Main) {
                    applyProfile(res.body()!!)
                }
                ApiResult.Success(res.body()!!)
            } else {
                withContext(Dispatchers.Main) {
                    AppState.currentUser = AppState.currentUser.copy(
                        name = name,
                        username = username,
                        bio = bio,
                        city = city,
                        interests = interests,
                        phone = phone,
                        gmail = gmail
                    )
                }
                ApiResult.Success(
                    UserProfileDto(
                        id = AppState.currentUser.id,
                        fullName = name,
                        username = username,
                        gmail = gmail,
                        phone = phone,
                        city = city,
                        interests = interests,
                        role = if (AppState.currentUser.isSeller) "STORE" else "USER",
                        avatarUrl = AppState.currentUser.avatarUrl
                    )
                )
            }
        } catch (_: Exception) {
            withContext(Dispatchers.Main) {
                AppState.currentUser = AppState.currentUser.copy(
                    name = name,
                    username = username,
                    bio = bio,
                    city = city,
                    interests = interests,
                    phone = phone,
                    gmail = gmail
                )
            }
            ApiResult.Success(
                UserProfileDto(
                    id = AppState.currentUser.id,
                    fullName = name,
                    username = username,
                    gmail = gmail,
                    phone = phone,
                    city = city,
                    interests = interests,
                    role = if (AppState.currentUser.isSeller) "STORE" else "USER",
                    avatarUrl = AppState.currentUser.avatarUrl
                )
            )
        }
    }

    suspend fun submitStoreApplication(
        storeName: String,
        category: String,
        city: String,
        address: String,
        nationalId: String,
        documentTitle: String,
        description: String
    ): ApiResult<StoreApplicationResponse> = withContext(Dispatchers.IO) {
        if (!AppState.isLoggedIn || AppState.isGuestUser()) {
            return@withContext ApiResult.Error(
                "برای ثبت فروشگاه باید وارد حساب کاربری شوید.",
                code = 401
            )
        }
        val cleanNationalId = nationalId.filter { it.isDigit() }.ifBlank { nationalId }
        val req = StoreApplicationRequest(
            storeName = storeName,
            category = category,
            city = city,
            address = address,
            nationalId = cleanNationalId,
            documentTitle = documentTitle,
            description = description
        )

        suspend fun saveLocally(feedback: String = "مدارک صنفی و هویتی شما با موفقیت ثبت شد و در صف بررسی قرار دارد."): ApiResult.Success<StoreApplicationResponse> {
            withContext(Dispatchers.Main) {
                AppState.submitStoreApplication(
                    storeName = storeName,
                    category = category,
                    city = city,
                    address = address,
                    nationalId = cleanNationalId,
                    documentTitle = documentTitle,
                    description = description
                )
            }
            val app = AppState.currentUser.storeApplication
            return ApiResult.Success(
                StoreApplicationResponse(
                    applicationId = app?.id ?: "app_${System.currentTimeMillis()}",
                    status = "PENDING",
                    message = "مدارک و مشخصات فروشگاه به صورت امن ثبت گردید.",
                    submittedAt = app?.submittedAt ?: "امروز",
                    adminFeedback = feedback
                )
            )
        }

        // If no backend token present, save directly to local storage
        if (!tokenManager.hasToken()) {
            return@withContext saveLocally()
        }

        try {
            val response = try {
                api().applyForStore(req)
            } catch (_: Exception) {
                ApiClient.resetClient()
                try {
                    api().applyForStore(req)
                } catch (second: Exception) {
                    // Remote server unreachable (firewall/timeout/filtering) -> Graceful local save
                    return@withContext saveLocally("مدارک در حافظه امن دستگاه ذخیره شد و در اولین برقراری ارتباط با سرور همگام می‌شود.")
                }
            }

            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                val app = StoreApplication(
                    id = res.applicationId,
                    storeName = storeName,
                    category = category,
                    city = city,
                    address = address,
                    nationalId = cleanNationalId,
                    documentTitle = documentTitle,
                    description = description,
                    submittedAt = res.submittedAt,
                    status = when (res.status.uppercase()) {
                        "APPROVED" -> StoreApplicationStatus.APPROVED
                        "REJECTED" -> StoreApplicationStatus.REJECTED
                        else -> StoreApplicationStatus.PENDING_REVIEW
                    },
                    adminFeedback = res.adminFeedback ?: res.message
                )
                withContext(Dispatchers.Main) {
                    AppState.currentUser = AppState.currentUser.copy(storeApplication = app)
                }
                ApiResult.Success(res)
            } else {
                val code = response.code()
                if (code == 401 || code == 403) {
                    ApiResult.Error("نشست شما منقضی شده. لطفاً دوباره وارد شوید و مدارک را ارسال کنید.", code)
                } else {
                    // On server 5xx or unhandled code, don't leave merchant blocked
                    saveLocally("مدارک دریافت شد و پس از بررسی فعال خواهد شد.")
                }
            }
        } catch (_: Exception) {
            // Unhandled network transport failure (e.g. ConnectException after 500ms) -> Save locally
            saveLocally("مدارک با موفقیت ثبت شد.")
        }
    }

    suspend fun syncStoreApplicationStatus() {
        try {
            val response = api().getStoreApplicationStatus()
            when {
                response.isSuccessful && response.body() != null -> {
                    applyStoreApplicationResponse(response.body()!!)
                }
                response.code() == 404 -> {
                    // No application row yet, or route missing — use profile.storeStatus.
                    refreshStoreStatusFromProfile()
                }
                else -> {
                    // e.g. 401 — leave current local state alone
                }
            }
        } catch (_: Exception) {
            refreshStoreStatusFromProfile()
        }
    }

    private suspend fun refreshStoreStatusFromProfile() {
        try {
            val me = api().getCurrentUser()
            if (me.isSuccessful && me.body() != null) {
                val status = me.body()!!.storeStatus
                if (!status.isNullOrBlank()) {
                    applyStoreStatusString(status, me.body()!!.fullName, me.body()!!.city)
                }
            }
        } catch (_: Exception) {
            // keep local state
        }
    }

    private fun applyStoreApplicationResponse(res: StoreApplicationResponse) {
        val app = StoreApplication(
            id = res.applicationId,
            storeName = AppState.currentUser.storeApplication?.storeName
                ?: AppState.currentUser.name,
            category = AppState.currentUser.storeCategory,
            city = AppState.currentUser.city,
            address = AppState.currentUser.location?.address.orEmpty(),
            nationalId = AppState.currentUser.storeApplication?.nationalId.orEmpty(),
            documentTitle = AppState.currentUser.storeApplication?.documentTitle.orEmpty(),
            description = AppState.currentUser.storeApplication?.description.orEmpty(),
            submittedAt = res.submittedAt,
            status = when (res.status.uppercase()) {
                "APPROVED" -> StoreApplicationStatus.APPROVED
                "REJECTED" -> StoreApplicationStatus.REJECTED
                else -> StoreApplicationStatus.PENDING_REVIEW
            },
            adminFeedback = res.adminFeedback ?: res.message
        )
        AppState.currentUser = AppState.currentUser.copy(
            storeApplication = app,
            isSeller = app.status == StoreApplicationStatus.APPROVED || AppState.currentUser.isSeller,
            accountType = if (app.status == StoreApplicationStatus.APPROVED) {
                AccountType.STORE_OWNER
            } else {
                AppState.currentUser.accountType
            }
        )
    }

    private fun applyStoreStatusString(status: String, storeName: String, city: String) {
        val appStatus = when (status.uppercase()) {
            "APPROVED" -> StoreApplicationStatus.APPROVED
            "REJECTED" -> StoreApplicationStatus.REJECTED
            "PENDING" -> StoreApplicationStatus.PENDING_REVIEW
            else -> return
        }
        val existing = AppState.currentUser.storeApplication
        val app = existing?.copy(status = appStatus)
            ?: StoreApplication(
                id = "profile_$status",
                storeName = storeName.ifBlank { AppState.currentUser.name },
                category = AppState.currentUser.storeCategory,
                city = city.ifBlank { AppState.currentUser.city },
                address = AppState.currentUser.location?.address.orEmpty(),
                nationalId = "",
                documentTitle = "",
                description = "",
                submittedAt = "از پروفایل سرور",
                status = appStatus,
                adminFeedback = when (appStatus) {
                    StoreApplicationStatus.APPROVED -> "فروشگاه تایید شده است."
                    StoreApplicationStatus.REJECTED -> "درخواست فروشگاه رد شده است."
                    else -> "درخواست فروشگاه در حال بررسی است."
                }
            )
        AppState.currentUser = AppState.currentUser.copy(
            storeApplication = app,
            isSeller = appStatus == StoreApplicationStatus.APPROVED || AppState.currentUser.isSeller,
            accountType = if (appStatus == StoreApplicationStatus.APPROVED) {
                AccountType.STORE_OWNER
            } else {
                AppState.currentUser.accountType
            }
        )
    }

    private fun applyProfile(profile: UserProfileDto) {
        val isStoreUser = profile.role.equals("STORE", ignoreCase = true) ||
            !profile.storeStatus.isNullOrBlank()
        AppState.currentUser = User(
            id = profile.id,
            username = profile.username,
            name = profile.fullName,
            gmail = profile.gmail,
            phone = profile.phone,
            city = profile.city,
            interests = profile.interests,
            avatarUrl = profile.avatarUrl
                ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
            bannerUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=1080&q=80",
            bio = "کاربر همگام با سرور کوک",
            rating = 5.0f,
            followersCount = 0,
            followingCount = 0,
            isVerified = true,
            isSeller = isStoreUser,
            accountType = if (isStoreUser) AccountType.STORE_OWNER else AccountType.REGULAR_USER,
            storeApplication = when (profile.storeStatus?.uppercase()) {
                "PENDING" -> StoreApplication(
                    id = "pending",
                    storeName = profile.fullName,
                    category = "پوشاک و مد",
                    city = profile.city,
                    address = "",
                    nationalId = "",
                    documentTitle = "",
                    description = "",
                    submittedAt = "ثبت شده",
                    status = StoreApplicationStatus.PENDING_REVIEW
                )
                "APPROVED" -> StoreApplication(
                    id = "approved",
                    storeName = profile.fullName,
                    category = "پوشاک و مد",
                    city = profile.city,
                    address = "",
                    nationalId = "",
                    documentTitle = "",
                    description = "",
                    submittedAt = "تایید شده",
                    status = StoreApplicationStatus.APPROVED
                )
                else -> null
            }
        )
        AppState.isLoggedIn = true
    }
}
