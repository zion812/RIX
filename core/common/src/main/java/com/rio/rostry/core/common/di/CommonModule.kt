package com.rio.rostry.core.common.di

import android.content.Context
import com.rio.rostry.core.common.utils.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module providing common dependencies across the application
 */
@Module
@InstallIn(SingletonComponent::class)
object CommonModule {

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return NetworkMonitorImpl(context)
    }

    @Provides
    @Singleton
    fun providePermissionManager(): PermissionManager {
        return PermissionManagerImpl()
    }

    @Provides
    @Singleton
    fun provideLoadingStateManager(): LoadingStateManager {
        return LoadingStateManagerImpl()
    }

    @Provides
    @Singleton
    fun provideErrorHandler(@ApplicationContext context: Context): ErrorHandler {
        return ErrorHandlerImpl(context)
    }

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoaderImpl(context)
    }

    @Provides
    @Singleton
    fun provideFileManager(@ApplicationContext context: Context): FileManager {
        return FileManagerImpl(context)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManagerImpl(context)
    }

    @Provides
    @Singleton
    fun provideLocalizationManager(@ApplicationContext context: Context): LocalizationManager {
        return LocalizationManagerImpl(context)
    }

    @Provides
    @Singleton
    fun provideAnalyticsManager(): AnalyticsManager {
        return AnalyticsManagerImpl()
    }

    @Provides
    @Singleton
    fun provideCacheManager(@ApplicationContext context: Context): CacheManager {
        return CacheManagerImpl(context)
    }

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @UnconfinedDispatcher
    fun provideUnconfinedDispatcher(): CoroutineDispatcher = Dispatchers.Unconfined
}

/**
 * Qualifier annotations for different coroutine dispatchers
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnconfinedDispatcher

/**
 * Network monitoring interface
 */
interface NetworkMonitor {
    val isConnected: kotlinx.coroutines.flow.StateFlow<Boolean>
    val networkType: kotlinx.coroutines.flow.StateFlow<com.rio.rostry.core.common.model.NetworkType>
    fun startMonitoring()
    fun stopMonitoring()
}

/**
 * Permission management interface
 */
interface PermissionManager {
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean
    fun requestPermissions(
        fragment: androidx.fragment.app.Fragment,
        permissions: Array<String>,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    )
    fun shouldShowRationale(fragment: androidx.fragment.app.Fragment, permission: String): Boolean
}

/**
 * Loading state management interface
 */
interface LoadingStateManager {
    fun showLoading(context: Context, message: String? = null)
    fun hideLoading()
    fun showError(context: Context, message: String)
    fun showMessage(view: android.view.View, message: String)
    fun showSuccessMessage(view: android.view.View, message: String)
    fun showErrorMessage(view: android.view.View, message: String)
    fun showOfflineMessage(context: Context)
    fun hideOfflineMessage()
    fun showTierUpgradePrompt(context: Context, requiredTier: com.rio.rostry.core.common.model.UserTier)
}

/**
 * Error handling interface
 */
interface ErrorHandler {
    fun getErrorMessage(throwable: Throwable): String
    fun handleError(throwable: Throwable)
    fun logError(throwable: Throwable, additionalInfo: Map<String, Any> = emptyMap())
}

/**
 * Image loading interface with network-aware caching
 */
interface ImageLoader {
    fun loadImage(
        imageView: android.widget.ImageView,
        url: String,
        placeholder: Int? = null,
        error: Int? = null
    )
    fun loadImageWithNetworkAwareness(
        imageView: android.widget.ImageView,
        url: String,
        networkType: com.rio.rostry.core.common.model.NetworkType,
        placeholder: Int? = null,
        error: Int? = null
    )
    fun preloadImage(url: String)
    fun clearCache()
}

/**
 * File management interface
 */
interface FileManager {
    suspend fun saveFile(data: ByteArray, fileName: String): String
    suspend fun readFile(filePath: String): ByteArray?
    suspend fun deleteFile(filePath: String): Boolean
    suspend fun getFileSize(filePath: String): Long
    suspend fun compressImage(imagePath: String, quality: Int = 80): String
}

/**
 * Preferences management interface
 */
interface PreferencesManager {
    suspend fun saveString(key: String, value: String)
    suspend fun getString(key: String, defaultValue: String = ""): String
    suspend fun saveBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    suspend fun saveInt(key: String, value: Int)
    suspend fun getInt(key: String, defaultValue: Int = 0): Int
    suspend fun clear()
}

/**
 * Localization management interface
 */
interface LocalizationManager {
    fun setLanguage(language: com.rio.rostry.core.common.model.Language)
    fun getCurrentLanguage(): com.rio.rostry.core.common.model.Language
    fun getString(resourceId: Int, vararg formatArgs: Any): String
    fun getLocalizedBreedName(breedKey: String): String
    fun getLocalizedRegionName(regionKey: String): String
}

/**
 * Analytics management interface
 */
interface AnalyticsManager {
    fun logEvent(eventName: String, parameters: Map<String, Any> = emptyMap())
    fun logUserAction(action: String, parameters: Map<String, Any> = emptyMap())
    fun logScreenView(screenName: String, screenClass: String)
    fun setUserProperty(name: String, value: String)
    fun setUserId(userId: String)
}

/**
 * Cache management interface
 */
interface CacheManager {
    suspend fun put(key: String, value: Any, ttlMillis: Long = 0)
    suspend fun <T> get(key: String, clazz: Class<T>): T?
    suspend fun remove(key: String)
    suspend fun clear()
    suspend fun size(): Long
}

/**
 * Constants for common use across the application
 */
object Constants {
    // Shared Preferences Keys
    const val PREF_USER_TIER = "user_tier"
    const val PREF_LANGUAGE = "language"
    const val PREF_FIRST_LAUNCH = "first_launch"
    const val PREF_OFFLINE_MODE = "offline_mode"
    
    // Cache Keys
    const val CACHE_USER_PROFILE = "user_profile"
    const val CACHE_FOWL_LIST = "fowl_list"
    const val CACHE_MARKETPLACE_LISTINGS = "marketplace_listings"
    
    // Network Timeouts
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val CACHE_TIMEOUT_MINUTES = 5L
    
    // Image Sizes
    const val IMAGE_SIZE_THUMBNAIL = 150
    const val IMAGE_SIZE_SMALL = 300
    const val IMAGE_SIZE_MEDIUM = 600
    const val IMAGE_SIZE_LARGE = 1200
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
    
    // Regional Settings
    const val DEFAULT_REGION = "andhra_pradesh"
    const val DEFAULT_LANGUAGE = "en"
    
    // Tier Limits
    const val GENERAL_USER_LISTING_LIMIT = 0
    const val FARMER_LISTING_LIMIT = 50
    const val ENTHUSIAST_LISTING_LIMIT = 200
    
    const val GENERAL_USER_MESSAGE_LIMIT = 50
    const val FARMER_MESSAGE_LIMIT = 200
    const val ENTHUSIAST_MESSAGE_LIMIT = 1000
}
