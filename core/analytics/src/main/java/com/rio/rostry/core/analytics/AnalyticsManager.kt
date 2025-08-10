package com.rio.rostry.core.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.rio.rostry.core.common.model.UserTier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ Comprehensive analytics and monitoring system
 * Tracks user behavior, performance, and errors for rural optimization
 */
@Singleton
class AnalyticsManager @Inject constructor(
    private val context: Context,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
    private val performance: FirebasePerformance
) {
    
    private val activeTraces = ConcurrentHashMap<String, Trace>()
    private val userProperties = mutableMapOf<String, String>()
    private val sessionStartTime = System.currentTimeMillis()
    
    // ✅ Performance monitoring
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()
    
    init {
        initializeAnalytics()
        startPerformanceMonitoring()
    }
    
    /**
     * ✅ Initialize analytics with user context
     */
    private fun initializeAnalytics() {
        // Enable analytics collection
        firebaseAnalytics.setAnalyticsCollectionEnabled(true)
        
        // Set default user properties
        setUserProperty("platform", "android")
        setUserProperty("app_version", getAppVersion())
        setUserProperty("device_type", getDeviceType())
        setUserProperty("network_type", getNetworkType())
    }
    
    /**
     * ✅ Set user properties for segmentation
     */
    fun setUserProperties(
        userId: String,
        userTier: UserTier,
        location: String? = null,
        language: String? = null
    ) {
        firebaseAnalytics.setUserId(userId)
        
        setUserProperty("user_tier", userTier.name.lowercase())
        location?.let { setUserProperty("location", it) }
        language?.let { setUserProperty("language", it) }
        
        // Crashlytics user identification
        crashlytics.setUserId(userId)
        crashlytics.setCustomKey("user_tier", userTier.name)
        location?.let { crashlytics.setCustomKey("location", it) }
    }
    
    /**
     * ✅ Track user events with rural context
     */
    fun trackEvent(
        eventName: String,
        parameters: Map<String, Any> = emptyMap(),
        isRuralContext: Boolean = false
    ) {
        val enhancedParams = parameters.toMutableMap().apply {
            put("timestamp", System.currentTimeMillis())
            put("session_duration", System.currentTimeMillis() - sessionStartTime)
            put("is_rural_context", isRuralContext)
            put("network_quality", getCurrentNetworkQuality())
        }
        
        // Firebase Analytics
        firebaseAnalytics.logEvent(eventName, enhancedParams.toBundle())
        
        // Custom logging for rural analytics
        if (isRuralContext) {
            logRuralEvent(eventName, enhancedParams)
        }
    }
    
    /**
     * ✅ Track fowl management events
     */
    fun trackFowlEvent(
        action: FowlAction,
        fowlId: String,
        breed: String? = null,
        generation: Int? = null
    ) {
        trackEvent("fowl_${action.name.lowercase()}", mapOf(
            "fowl_id" to fowlId,
            "breed" to (breed ?: "unknown"),
            "generation" to (generation ?: -1),
            "action_type" to "fowl_management"
        ), isRuralContext = true)
    }
    
    /**
     * ✅ Track marketplace events
     */
    fun trackMarketplaceEvent(
        action: MarketplaceAction,
        listingId: String? = null,
        priceInCoins: Int? = null,
        category: String? = null
    ) {
        trackEvent("marketplace_${action.name.lowercase()}", mapOf(
            "listing_id" to (listingId ?: ""),
            "price_coins" to (priceInCoins ?: 0),
            "category" to (category ?: "unknown"),
            "action_type" to "marketplace"
        ), isRuralContext = true)
    }
    
    /**
     * ✅ Track transfer events
     */
    fun trackTransferEvent(
        action: TransferAction,
        transferId: String,
        fowlId: String,
        fromUserId: String,
        toUserId: String
    ) {
        trackEvent("transfer_${action.name.lowercase()}", mapOf(
            "transfer_id" to transferId,
            "fowl_id" to fowlId,
            "from_user_tier" to getUserTier(fromUserId),
            "to_user_tier" to getUserTier(toUserId),
            "action_type" to "transfer"
        ), isRuralContext = true)
    }
    
    /**
     * ✅ Track payment events
     */
    fun trackPaymentEvent(
        action: PaymentAction,
        amount: Double,
        currency: String = "INR",
        paymentMethod: String,
        success: Boolean
    ) {
        trackEvent("payment_${action.name.lowercase()}", mapOf(
            "amount" to amount,
            "currency" to currency,
            "payment_method" to paymentMethod,
            "success" to success,
            "action_type" to "payment"
        ))
    }
    
    /**
     * ✅ Start performance trace
     */
    fun startTrace(traceName: String): String {
        val trace = performance.newTrace(traceName)
        trace.start()
        
        val traceId = "${traceName}_${System.currentTimeMillis()}"
        activeTraces[traceId] = trace
        
        return traceId
    }
    
    /**
     * ✅ Stop performance trace
     */
    fun stopTrace(traceId: String, attributes: Map<String, String> = emptyMap()) {
        activeTraces.remove(traceId)?.let { trace ->
            attributes.forEach { (key, value) ->
                trace.putAttribute(key, value)
            }
            trace.stop()
        }
    }
    
    /**
     * ✅ Track screen views
     */
    fun trackScreenView(
        screenName: String,
        screenClass: String,
        previousScreen: String? = null
    ) {
        trackEvent("screen_view", mapOf(
            "screen_name" to screenName,
            "screen_class" to screenClass,
            "previous_screen" to (previousScreen ?: "unknown")
        ))
    }
    
    /**
     * ✅ Track errors and crashes
     */
    fun trackError(
        error: Throwable,
        context: String,
        isFatal: Boolean = false,
        additionalData: Map<String, String> = emptyMap()
    ) {
        // Set additional context
        additionalData.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value)
        }
        crashlytics.setCustomKey("error_context", context)
        crashlytics.setCustomKey("is_fatal", isFatal)
        
        if (isFatal) {
            crashlytics.recordException(error)
        } else {
            crashlytics.log("Non-fatal error in $context: ${error.message}")
        }
        
        // Track as analytics event
        trackEvent("error_occurred", mapOf(
            "error_type" to error.javaClass.simpleName,
            "error_message" to (error.message ?: "unknown"),
            "error_context" to context,
            "is_fatal" to isFatal
        ))
    }
    
    /**
     * ✅ Track user engagement metrics
     */
    fun trackEngagement(
        feature: String,
        duration: Long,
        interactionCount: Int,
        completionRate: Float
    ) {
        trackEvent("user_engagement", mapOf(
            "feature" to feature,
            "duration_ms" to duration,
            "interaction_count" to interactionCount,
            "completion_rate" to completionRate
        ))
    }
    
    /**
     * ✅ Track offline usage patterns
     */
    fun trackOfflineUsage(
        feature: String,
        duration: Long,
        actionsPerformed: Int,
        dataSize: Long
    ) {
        trackEvent("offline_usage", mapOf(
            "feature" to feature,
            "duration_ms" to duration,
            "actions_performed" to actionsPerformed,
            "data_size_bytes" to dataSize,
            "usage_type" to "offline"
        ), isRuralContext = true)
    }
    
    /**
     * ✅ Start performance monitoring
     */
    private fun startPerformanceMonitoring() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                updatePerformanceMetrics()
                delay(30000) // Update every 30 seconds
            }
        }
    }
    
    /**
     * ✅ Update performance metrics
     */
    private fun updatePerformanceMetrics() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val memoryUsage = (usedMemory.toFloat() / maxMemory) * 100
        
        _performanceMetrics.value = PerformanceMetrics(
            memoryUsagePercent = memoryUsage,
            activeTraces = activeTraces.size,
            sessionDuration = System.currentTimeMillis() - sessionStartTime,
            networkQuality = getCurrentNetworkQuality()
        )
    }
    
    private fun setUserProperty(key: String, value: String) {
        userProperties[key] = value
        firebaseAnalytics.setUserProperty(key, value)
    }
    
    private fun logRuralEvent(eventName: String, parameters: Map<String, Any>) {
        // Custom logging for rural-specific analytics
        android.util.Log.d("RuralAnalytics", "Event: $eventName, Params: $parameters")
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun getDeviceType(): String {
        return if (context.resources.configuration.smallestScreenWidthDp >= 600) {
            "tablet"
        } else {
            "phone"
        }
    }
    
    private fun getNetworkType(): String {
        // Implementation to detect network type
        return "unknown"
    }
    
    private fun getCurrentNetworkQuality(): String {
        // Implementation to assess network quality
        return "unknown"
    }
    
    private fun getUserTier(userId: String): String {
        // Implementation to get user tier
        return "unknown"
    }
    
    private fun Map<String, Any>.toBundle(): android.os.Bundle {
        val bundle = android.os.Bundle()
        forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        return bundle
    }
}

/**
 * ✅ Event action enums
 */
enum class FowlAction {
    CREATED, UPDATED, DELETED, VIEWED, PHOTOGRAPHED, QR_GENERATED
}

enum class MarketplaceAction {
    LISTING_CREATED, LISTING_VIEWED, PURCHASE_INITIATED, PURCHASE_COMPLETED, SEARCH_PERFORMED
}

enum class TransferAction {
    INITIATED, APPROVED, REJECTED, COMPLETED, CANCELLED
}

enum class PaymentAction {
    INITIATED, COMPLETED, FAILED, REFUNDED
}

/**
 * ✅ Performance metrics data class
 */
data class PerformanceMetrics(
    val memoryUsagePercent: Float = 0f,
    val activeTraces: Int = 0,
    val sessionDuration: Long = 0L,
    val networkQuality: String = "unknown"
)
