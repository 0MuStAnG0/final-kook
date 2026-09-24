package com.example.utils

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

/**
 * Singleton PlayerManager to ensure optimal resource sharing,
 * avoid Android hardware codec exhaustion and prevent 'Failed to query component interface' errors.
 */
object PlayerManager {
    @Volatile
    private var sharedPlayer: ExoPlayer? = null

    @OptIn(UnstableApi::class)
    @Synchronized
    fun getSharedPlayer(context: Context): ExoPlayer {
        val existing = sharedPlayer
        if (existing != null) return existing

        val appContext = context.applicationContext

        val renderersFactory = DefaultRenderersFactory(appContext)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            .setEnableDecoderFallback(true)

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(10000)
            .setReadTimeoutMs(10000)

        val dataSourceFactory = DefaultDataSource.Factory(appContext, httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
            .setUsage(androidx.media3.common.C.USAGE_MEDIA)
            .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        val player = ExoPlayer.Builder(appContext, renderersFactory)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
            }
        sharedPlayer = player
        return player
    }

    @Synchronized
    fun pausePlayback() {
        sharedPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    @Synchronized
    fun releasePlayer() {
        sharedPlayer?.release()
        sharedPlayer = null
    }
}
