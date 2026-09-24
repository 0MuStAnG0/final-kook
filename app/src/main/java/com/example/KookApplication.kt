package com.example

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import org.osmdroid.config.Configuration

class KookApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        try {
            Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            Configuration.getInstance().userAgentValue = packageName
        } catch (_: Throwable) {
            // Graceful fallback for devices with restricted storage or config access
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .allowHardware(false)
            .crossfade(true)
            .build()
    }
}
