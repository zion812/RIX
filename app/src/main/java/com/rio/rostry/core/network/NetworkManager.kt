package com.rio.rostry.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network manager for monitoring connectivity and network quality
 * Optimized for rural network conditions
 */
@Singleton
class NetworkManager @Inject constructor(
    private val context: Context
) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _networkQuality = MutableStateFlow(NetworkQuality.UNKNOWN)
    val networkQuality: StateFlow<NetworkQuality> = _networkQuality.asStateFlow()
    
    private val _isMetered = MutableStateFlow(false)
    val isMetered: StateFlow<Boolean> = _isMetered.asStateFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
            updateNetworkQuality(network)
        }
        
        override fun onLost(network: Network) {
            _isConnected.value = false
            _networkQuality.value = NetworkQuality.UNKNOWN
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateNetworkCapabilities(networkCapabilities)
            updateNetworkQuality(network)
        }
    }
    
    init {
        registerNetworkCallback()
        updateCurrentNetworkStatus()
    }
    
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    private fun updateCurrentNetworkStatus() {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        _isConnected.value = activeNetwork != null && networkCapabilities != null
        
        if (networkCapabilities != null) {
            updateNetworkCapabilities(networkCapabilities)
        }
    }
    
    private fun updateNetworkCapabilities(capabilities: NetworkCapabilities) {
        _isMetered.value = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        
        // Determine network type for rural optimization
        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                _networkQuality.value = NetworkQuality.GOOD
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // Check cellular generation for rural areas
                when {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) -> {
                        _networkQuality.value = NetworkQuality.GOOD
                    }
                    else -> {
                        // Assume slower cellular in rural areas
                        _networkQuality.value = NetworkQuality.POOR
                    }
                }
            }
            else -> {
                _networkQuality.value = NetworkQuality.POOR
            }
        }
    }
    
    private fun updateNetworkQuality(network: Network) {
        // Additional quality checks can be implemented here
        // For now, use basic transport type detection
    }
    
    /**
     * Check if network is connected
     */
    fun isConnected(): Boolean {
        return _isConnected.value
    }
    
    /**
     * Check if network is suitable for large operations
     */
    fun isGoodQuality(): Boolean {
        return _networkQuality.value == NetworkQuality.GOOD
    }
    
    /**
     * Check if network is metered (data charges apply)
     */
    fun isMetered(): Boolean {
        return _isMetered.value
    }
    
    /**
     * Get recommended image quality based on network
     */
    fun getRecommendedImageQuality(): ImageQuality {
        return when (_networkQuality.value) {
            NetworkQuality.GOOD -> ImageQuality.HIGH
            NetworkQuality.MODERATE -> ImageQuality.MEDIUM
            NetworkQuality.POOR -> ImageQuality.LOW
            NetworkQuality.UNKNOWN -> ImageQuality.LOW
        }
    }
    
    /**
     * Get recommended batch size for API calls
     */
    fun getRecommendedBatchSize(): Int {
        return when (_networkQuality.value) {
            NetworkQuality.GOOD -> 20
            NetworkQuality.MODERATE -> 10
            NetworkQuality.POOR -> 5
            NetworkQuality.UNKNOWN -> 5
        }
    }
    
    /**
     * Check if should sync now based on network conditions
     */
    fun shouldSyncNow(): Boolean {
        return isConnected() && (!isMetered() || isGoodQuality())
    }
    
    /**
     * Get network status summary for debugging
     */
    fun getNetworkStatus(): NetworkStatus {
        return NetworkStatus(
            isConnected = _isConnected.value,
            quality = _networkQuality.value,
            isMetered = _isMetered.value,
            timestamp = System.currentTimeMillis()
        )
    }
    
    fun cleanup() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

/**
 * Network quality levels for rural optimization
 */
enum class NetworkQuality {
    UNKNOWN,
    POOR,      // 2G or very slow connection
    MODERATE,  // 3G or slow 4G
    GOOD       // Fast 4G, 5G, or WiFi
}

/**
 * Image quality levels based on network
 */
enum class ImageQuality {
    LOW,       // Highly compressed, small size
    MEDIUM,    // Moderate compression
    HIGH       // Full quality
}

/**
 * Network status data class
 */
data class NetworkStatus(
    val isConnected: Boolean,
    val quality: NetworkQuality,
    val isMetered: Boolean,
    val timestamp: Long
)
