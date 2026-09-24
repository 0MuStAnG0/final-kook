package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Post
import com.example.repository.NetworkPostRepository
import com.example.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel(
    application: Application,
    private val repository: PostRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, NetworkPostRepository(application))

    private val _posts = MutableStateFlow<List<Post>>(com.example.data.MockData.posts)
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isServerConnected = MutableStateFlow(false)
    val isServerConnected: StateFlow<Boolean> = _isServerConnected.asStateFlow()

    private val _connectionStatus = MutableStateFlow("آماده به کار (حالت پایدار)")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    init {
        loadPosts()
    }

    fun refreshPosts() {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            try {
                val fetchedPosts = repository.getPosts()
                if (fetchedPosts.isNotEmpty()) {
                    _posts.value = fetchedPosts
                }
                val connected = repository.isConnectedToLiveServer()
                _isServerConnected.value = connected
                _connectionStatus.value = if (connected) {
                    "متصل به سرور ابری Orange IT (داده‌های زنده)"
                } else {
                    "حالت آماده به کار (داده‌های محلی)"
                }
            } catch (e: Exception) {
                _isServerConnected.value = false
                _connectionStatus.value = "حالت آماده به کار (داده‌های محلی)"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

