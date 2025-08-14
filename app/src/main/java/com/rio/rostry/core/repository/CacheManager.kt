package com.rio.rostry.core.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rio.rostry.ui.marketplace.models.CreateOrderRequest
import com.rio.rostry.ui.marketplace.models.MarketplaceOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache manager for offline-first data storage
 * Handles pending operations and data synchronization
 */
@Singleton
class CacheManager @Inject constructor(
    private val context: Context,
    private val gson: Gson
) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "rostry_cache", Context.MODE_PRIVATE
    )
    
    private val cacheDir = File(context.cacheDir, "rostry_data")
    
    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    /**
     * Cache timestamp management
     */
    fun updateCacheTimestamp(key: String) {
        prefs.edit()
            .putLong("${key}_timestamp", System.currentTimeMillis())
            .apply()
    }
    
    fun getCacheTimestamp(key: String): Long {
        return prefs.getLong("${key}_timestamp", 0)
    }
    
    fun isCacheStale(key: String, maxAgeHours: Int): Boolean {
        val timestamp = getCacheTimestamp(key)
        val maxAge = maxAgeHours * 60 * 60 * 1000L // Convert to milliseconds
        return (System.currentTimeMillis() - timestamp) > maxAge
    }
    
    /**
     * Pending orders management for offline scenarios
     */
    suspend fun queuePendingOrder(orderRequest: CreateOrderRequest) = withContext(Dispatchers.IO) {
        val pendingOrders = getPendingOrders().toMutableList()
        pendingOrders.add(orderRequest)
        
        val json = gson.toJson(pendingOrders)
        File(cacheDir, "pending_orders.json").writeText(json)
        
        // Update pending count for UI
        prefs.edit()
            .putInt("pending_orders_count", pendingOrders.size)
            .apply()
    }
    
    suspend fun getPendingOrders(): List<CreateOrderRequest> = withContext(Dispatchers.IO) {
        try {
            val file = File(cacheDir, "pending_orders.json")
            if (!file.exists()) return@withContext emptyList()
            
            val json = file.readText()
            val type = object : TypeToken<List<CreateOrderRequest>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun removePendingOrder(orderRequest: CreateOrderRequest) = withContext(Dispatchers.IO) {
        val pendingOrders = getPendingOrders().toMutableList()
        pendingOrders.removeAll { it.listingId == orderRequest.listingId }
        
        val json = gson.toJson(pendingOrders)
        File(cacheDir, "pending_orders.json").writeText(json)
        
        // Update pending count
        prefs.edit()
            .putInt("pending_orders_count", pendingOrders.size)
            .apply()
    }
    
    fun getPendingOrdersCount(): Int {
        return prefs.getInt("pending_orders_count", 0)
    }
    
    /**
     * Order caching for offline access
     */
    suspend fun cacheOrder(order: MarketplaceOrder) = withContext(Dispatchers.IO) {
        val userOrders = getCachedOrders(order.buyerId).toMutableList()
        
        // Remove existing order with same ID and add updated one
        userOrders.removeAll { it.id == order.id }
        userOrders.add(0, order) // Add to beginning for recent-first order
        
        // Keep only last 50 orders to manage storage
        if (userOrders.size > 50) {
            userOrders.subList(50, userOrders.size).clear()
        }
        
        cacheUserOrders(order.buyerId, userOrders)
    }
    
    suspend fun cacheUserOrders(userId: String, orders: List<MarketplaceOrder>) = withContext(Dispatchers.IO) {
        val json = gson.toJson(orders)
        File(cacheDir, "orders_$userId.json").writeText(json)
        updateCacheTimestamp("orders_$userId")
    }
    
    suspend fun getCachedOrders(userId: String): List<MarketplaceOrder> = withContext(Dispatchers.IO) {
        try {
            val file = File(cacheDir, "orders_$userId.json")
            if (!file.exists()) return@withContext emptyList()
            
            val json = file.readText()
            val type = object : TypeToken<List<MarketplaceOrder>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * User preferences and settings cache
     */
    fun cacheUserPreference(key: String, value: String) {
        prefs.edit().putString("user_pref_$key", value).apply()
    }
    
    fun getCachedUserPreference(key: String, defaultValue: String = ""): String {
        return prefs.getString("user_pref_$key", defaultValue) ?: defaultValue
    }
    
    fun cacheUserPreference(key: String, value: Boolean) {
        prefs.edit().putBoolean("user_pref_$key", value).apply()
    }
    
    fun getCachedUserPreference(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean("user_pref_$key", defaultValue)
    }
    
    /**
     * Image cache management for rural networks
     */
    suspend fun cacheImage(url: String, imageData: ByteArray) = withContext(Dispatchers.IO) {
        try {
            val fileName = url.hashCode().toString() + ".jpg"
            val file = File(cacheDir, "images/$fileName")
            file.parentFile?.mkdirs()
            file.writeBytes(imageData)
            
            // Store URL mapping
            prefs.edit()
                .putString("image_${url.hashCode()}", file.absolutePath)
                .apply()
        } catch (e: Exception) {
            // Ignore cache errors
        }
    }
    
    suspend fun getCachedImagePath(url: String): String? = withContext(Dispatchers.IO) {
        val path = prefs.getString("image_${url.hashCode()}", null)
        if (path != null && File(path).exists()) {
            path
        } else {
            null
        }
    }
    
    /**
     * Cache cleanup for storage management
     */
    suspend fun cleanupOldCache(maxAgeHours: Int = 168) = withContext(Dispatchers.IO) { // 1 week default
        try {
            val cutoffTime = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000L)
            
            cacheDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    file.delete()
                }
            }
            
            // Clean up old preference entries
            val editor = prefs.edit()
            prefs.all.keys.forEach { key ->
                if (key.endsWith("_timestamp")) {
                    val timestamp = prefs.getLong(key, 0)
                    if (timestamp < cutoffTime) {
                        editor.remove(key)
                    }
                }
            }
            editor.apply()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    /**
     * Get cache statistics for monitoring
     */
    suspend fun getCacheStats(): CacheStats = withContext(Dispatchers.IO) {
        val totalFiles = cacheDir.listFiles()?.size ?: 0
        val totalSize = cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        val pendingOperations = getPendingOrdersCount()
        
        CacheStats(
            totalFiles = totalFiles,
            totalSizeBytes = totalSize,
            pendingOperations = pendingOperations,
            lastCleanup = prefs.getLong("last_cleanup", 0)
        )
    }
    
    /**
     * Clear all cache data
     */
    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
        prefs.edit().clear().apply()
    }
}

/**
 * Cache statistics data class
 */
data class CacheStats(
    val totalFiles: Int,
    val totalSizeBytes: Long,
    val pendingOperations: Int,
    val lastCleanup: Long
)
