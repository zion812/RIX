package com.rio.rostry.core.media

import android.content.Context
import android.os.Build
import android.app.ActivityManager
import android.content.pm.PackageManager

/**
 * Configuration class for cache management based on device capabilities
 */
object CacheConfig {
    
    /**
     * Get memory cache size based on device class
     */
    fun getMemoryCacheSize(context: Context): Int {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        
        return when (getDeviceClass(context)) {
            DeviceClass.HIGH_END -> maxMemory / 4 // Use 1/4th of available memory
            DeviceClass.MID_END -> maxMemory / 6  // Use 1/6th of available memory
            DeviceClass.LOW_END -> maxMemory / 8  // Use 1/8th of available memory
        }
    }
    
    /**
     * Get disk cache size based on device class
     */
    fun getDiskCacheSize(context: Context): Long {
        return when (getDeviceClass(context)) {
            DeviceClass.HIGH_END -> 50L * 1024 * 1024 // 50MB
            DeviceClass.MID_END -> 30L * 1024 * 1024  // 30MB
            DeviceClass.LOW_END -> 15L * 1024 * 1024  // 15MB
        }
    }
    
    /**
     * Determine device class based on hardware specifications
     */
    private fun getDeviceClass(context: Context): DeviceClass {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val totalRam = memoryInfo.totalMem
        
        // Check if device has low RAM (typically <= 1GB)
        if (activityManager.isLowRamDevice) {
            return DeviceClass.LOW_END
        }
        
        // Check RAM size
        val ramClass = when {
            totalRam <= 1.5 * 1024 * 1024 * 1024 -> DeviceClass.LOW_END // <= 1.5GB
            totalRam <= 3 * 1024 * 1024 * 1024 -> DeviceClass.MID_END   // <= 3GB
            else -> DeviceConfig.HIGH_END
        }
        
        // Check SDK version
        val sdkClass = when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> DeviceClass.LOW_END // < Android 6.0
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> DeviceClass.MID_END // < Android 8.0
            else -> DeviceClass.HIGH_END
        }
        
        // Return the lowest class to ensure performance on constrained devices
        return when {
            ramClass == DeviceClass.LOW_END || sdkClass == DeviceClass.LOW_END -> DeviceClass.LOW_END
            ramClass == DeviceClass.MID_END || sdkClass == DeviceClass.MID_END -> DeviceClass.MID_END
            else -> DeviceClass.HIGH_END
        }
    }
    
    /**
     * Check if the device supports thumbnails
     */
    fun isThumbnailSupported(context: Context): Boolean {
        return try {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        } catch (e: Exception) {
            true // Assume supported if we can't determine
        }
    }
}

/**
 * Enum representing device classes for cache configuration
 */
enum class DeviceClass {
    LOW_END,
    MID_END,
    HIGH_END
}

/**
 * Object to hold device configuration information
 */
object DeviceConfig {
    const val HIGH_END = DeviceClass.HIGH_END
    const val MID_END = DeviceClass.MID_END
    const val LOW_END = DeviceClass.LOW_END
}