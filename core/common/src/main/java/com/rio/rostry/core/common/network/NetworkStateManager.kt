package com.rio.rostry.core.common.network

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
 * Manages network connectivity state for offline-first functionality
 */
@Singleton
class NetworkStateManager @Inject constructor(
    private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _networkType = MutableStateFlow(NetworkType.NONE)
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()
    
    private val _connectionQuality = MutableStateFlow(ConnectionQuality.UNKNOWN)
    val connectionQuality: StateFlow<ConnectionQuality> = _connectionQuality.asStateFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
            updateNetworkInfo()
        }
        
        override fun onLost(network: Network) {
            _isConnected.value = false
            _networkType.value = NetworkType.NONE
            _connectionQuality.value = ConnectionQuality.UNKNOWN
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateNetworkInfo(networkCapabilities)
        }
    }
    
    init {
        registerNetworkCallback()
        updateNetworkInfo()
    }
    
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    private fun updateNetworkInfo(networkCapabilities: NetworkCapabilities? = null) {
        val capabilities = networkCapabilities ?: connectivityManager.getNetworkCapabilities(
            connectivityManager.activeNetwork
        )
        
        if (capabilities != null) {
            _networkType.value = when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                else -> NetworkType.OTHER
            }
            
            _connectionQuality.value = determineConnectionQuality(capabilities)
        }
    }
    
    private fun determineConnectionQuality(capabilities: NetworkCapabilities): ConnectionQuality {
        val downstreamBandwidth = capabilities.linkDownstreamBandwidthKbps
        val upstreamBandwidth = capabilities.linkUpstreamBandwidthKbps
        
        return when {
            downstreamBandwidth >= 5000 && upstreamBandwidth >= 1000 -> ConnectionQuality.EXCELLENT
            downstreamBandwidth >= 1500 && upstreamBandwidth >= 500 -> ConnectionQuality.GOOD
            downstreamBandwidth >= 500 && upstreamBandwidth >= 200 -> ConnectionQuality.FAIR
            downstreamBandwidth >= 100 && upstreamBandwidth >= 50 -> ConnectionQuality.POOR
            else -> ConnectionQuality.VERY_POOR
        }
    }
    
    /**
     * Check if network is suitable for large data operations
     */
    fun isNetworkSuitableForLargeOperations(): Boolean {
        return isConnected.value && (
            networkType.value == NetworkType.WIFI ||
            connectionQuality.value in listOf(ConnectionQuality.EXCELLENT, ConnectionQuality.GOOD)
        )
    }
    
    /**
     * Check if network is suitable for media uploads
     */
    fun isNetworkSuitableForMedia(): Boolean {
        return isConnected.value && (
            networkType.value == NetworkType.WIFI ||
            connectionQuality.value != ConnectionQuality.VERY_POOR
        )
    }
    
    /**
     * Get recommended sync strategy based on network conditions
     */
    fun getRecommendedSyncStrategy(): SyncStrategy {
        return when {
            !isConnected.value -> SyncStrategy.OFFLINE_ONLY
            networkType.value == NetworkType.WIFI -> SyncStrategy.AGGRESSIVE
            connectionQuality.value == ConnectionQuality.EXCELLENT -> SyncStrategy.AGGRESSIVE
            connectionQuality.value in listOf(ConnectionQuality.GOOD, ConnectionQuality.FAIR) -> SyncStrategy.CONSERVATIVE
            else -> SyncStrategy.MINIMAL
        }
    }
    
    fun cleanup() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

enum class NetworkType {
    NONE,
    WIFI,
    CELLULAR,
    ETHERNET,
    OTHER
}

enum class ConnectionQuality {
    UNKNOWN,
    VERY_POOR,  // < 100 Kbps down, < 50 Kbps up
    POOR,       // 100-500 Kbps down, 50-200 Kbps up
    FAIR,       // 500-1500 Kbps down, 200-500 Kbps up
    GOOD,       // 1500-5000 Kbps down, 500-1000 Kbps up
    EXCELLENT   // > 5000 Kbps down, > 1000 Kbps up
}

enum class SyncStrategy {
    OFFLINE_ONLY,    // No sync, work offline only
    MINIMAL,         // Sync only critical data
    CONSERVATIVE,    // Sync important data, compress media
    AGGRESSIVE       // Sync all data, full quality media
}