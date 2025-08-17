package com.rio.rostry.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.collection.LruCache
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Manager for caching thumbnails to reduce data usage and improve loading times
 * for proof media in timeline views
 */
class ThumbnailCacheManager private constructor(context: Context) {
    
    private val cacheDir: File = File(context.cacheDir, "thumbnails").apply {
        if (!exists()) mkdirs()
    }
    
    // In-memory LRU cache for bitmaps
    private val memoryCache = LruCache<String, Bitmap>(CacheConfig.getMemoryCacheSize(context)) {
        it.byteCount
    }
    
    // Disk cache size
    private val diskCacheSize = CacheConfig.getDiskCacheSize(context)
    
    companion object {
        @Volatile
        private var INSTANCE: ThumbnailCacheManager? = null
        
        fun getInstance(context: Context): ThumbnailCacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThumbnailCacheManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        private fun getCacheSize(): Int {
            val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
            return maxMemory / 8 // Use 1/8th of available memory for cache
        }
    }
    
    /**
     * Get thumbnail from cache or create it if not exists
     */
    fun getThumbnail(filePath: String, maxWidth: Int = 120, maxHeight: Int = 120): Bitmap? {
        val key = generateKey(filePath, maxWidth, maxHeight)
        
        // Check memory cache first
        memoryCache.get(key)?.let { return it }
        
        // Check disk cache
        val cachedFile = File(cacheDir, key)
        if (cachedFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(cachedFile.absolutePath)
            bitmap?.let { memoryCache.put(key, it) }
            return bitmap
        }
        
        // Create thumbnail and cache it
        return createAndCacheThumbnail(filePath, key, maxWidth, maxHeight)
    }
    
    /**
     * Create thumbnail and cache it both in memory and disk
     */
    private fun createAndCacheThumbnail(filePath: String, key: String, maxWidth: Int, maxHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        
        BitmapFactory.decodeFile(filePath, options)
        
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false
        
        val bitmap = BitmapFactory.decodeFile(filePath, options) ?: return null
        
        // Cache in memory
        memoryCache.put(key, bitmap)
        
        // Cache on disk
        val cachedFile = File(cacheDir, key)
        try {
            FileOutputStream(cachedFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            }
        } catch (e: Exception) {
            // Failed to cache to disk, but we can still use the in-memory version
        }
        
        return bitmap
    }
    
    /**
     * Calculate the largest inSampleSize value that is a power of 2 and keeps both
     * height and width larger than the requested height and width
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Generate a unique key for the thumbnail based on file path and dimensions
     */
    private fun generateKey(filePath: String, maxWidth: Int, maxHeight: Int): String {
        val input = "$filePath-$maxWidth-$maxHeight"
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Clear all cached thumbnails
     */
    fun clearCache() {
        memoryCache.evictAll()
        
        cacheDir.listFiles()?.forEach { file ->
            file.delete()
        }
    }
    
    /**
     * Remove a specific thumbnail from cache
     */
    fun removeThumbnail(filePath: String, maxWidth: Int = 120, maxHeight: Int = 120) {
        val key = generateKey(filePath, maxWidth, maxHeight)
        
        // Remove from memory cache
        memoryCache.remove(key)
        
        // Remove from disk cache
        val cachedFile = File(cacheDir, key)
        if (cachedFile.exists()) {
            cachedFile.delete()
        }
    }
    
    /**
     * Get current memory cache size
     */
    fun getMemoryCacheSize(): Int {
        return memoryCache.maxSize()
    }
    
    /**
     * Get current disk cache usage
     */
    fun getDiskCacheUsage(): Long {
        return cacheDir.walk().sumOf { it.length() }
    }
    
    /**
     * Get disk cache limit
     */
    fun getDiskCacheLimit(): Long {
        return diskCacheSize
    }
}