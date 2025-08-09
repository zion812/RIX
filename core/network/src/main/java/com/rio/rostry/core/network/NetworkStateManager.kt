package com.rio.rostry.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.telephony.TelephonyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network state manager for monitoring connectivity and quality
 * Optimized for rural India's network conditions
 */
@Singleton
class NetworkStateManager @Inject constructor(
    private val context: Context
) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _networkType = MutableStateFlow(NetworkType.UNKNOWN)
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()
    
    private val _connectionQuality = MutableStateFlow(ConnectionQuality.UNKNOWN)
    val connectionQuality: StateFlow<ConnectionQuality> = _connectionQuality.asStateFlow()
    
    private val _bandwidthEstimate = MutableStateFlow(0L)
    val bandwidthEstimate: StateFlow<Long> = _bandwidthEstimate.asStateFlow()
    
    private val _isMetered = MutableStateFlow(true)
    val isMetered: StateFlow<Boolean> = _isMetered.asStateFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
            updateNetworkInfo()
        }
        
        override fun onLost(network: Network) {
            _isConnected.value = false
            _networkType.value = NetworkType.UNKNOWN
            _connectionQuality.value = ConnectionQuality.UNKNOWN
            _bandwidthEstimate.value = 0L
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateNetworkInfo(networkCapabilities)
        }
    }
    
    init {
        startNetworkMonitoring()
        updateNetworkInfo()
    }
    
    /**
     * Start monitoring network changes
     */
    private fun startNetworkMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    /**
     * Update network information
     */
    private fun updateNetworkInfo(networkCapabilities: NetworkCapabilities? = null) {
        val capabilities = networkCapabilities ?: connectivityManager.getNetworkCapabilities(
            connectivityManager.activeNetwork
        )
        
        if (capabilities != null) {
            _networkType.value = determineNetworkType(capabilities)
            _connectionQuality.value = determineConnectionQuality(capabilities)
            _bandwidthEstimate.value = estimateBandwidth(capabilities)
            _isMetered.value = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        }
    }
    
    /**
     * Determine network type from capabilities
     */
    private fun determineNetworkType(capabilities: NetworkCapabilities): NetworkType {
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                when (telephonyManager.dataNetworkType) {
                    TelephonyManager.NETWORK_TYPE_LTE,
                    TelephonyManager.NETWORK_TYPE_NR -> NetworkType.CELLULAR_4G
                    TelephonyManager.NETWORK_TYPE_UMTS,
                    TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSPA -> NetworkType.CELLULAR_3G
                    TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_GPRS -> NetworkType.CELLULAR_2G
                    else -> NetworkType.CELLULAR_4G
                }
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
        }
    }
    
    /**
     * Determine connection quality based on bandwidth and latency
     */
    private fun determineConnectionQuality(capabilities: NetworkCapabilities): ConnectionQuality {
        val downstreamBandwidth = capabilities.linkDownstreamBandwidthKbps
        val upstreamBandwidth = capabilities.linkUpstreamBandwidthKbps
        
        return when {
            downstreamBandwidth >= 10000 && upstreamBandwidth >= 1000 -> ConnectionQuality.EXCELLENT
            downstreamBandwidth >= 5000 && upstreamBandwidth >= 500 -> ConnectionQuality.GOOD
            downstreamBandwidth >= 1000 && upstreamBandwidth >= 100 -> ConnectionQuality.FAIR
            downstreamBandwidth >= 100 && upstreamBandwidth >= 50 -> ConnectionQuality.POOR
            else -> ConnectionQuality.VERY_POOR
        }
    }
    
    /**
     * Estimate bandwidth in kbps
     */
    private fun estimateBandwidth(capabilities: NetworkCapabilities): Long {
        return capabilities.linkDownstreamBandwidthKbps.toLong()
    }
    
    /**
     * Get sync strategy based on current network conditions
     */
    fun getSyncStrategy(): SyncStrategy {
        return when {
            !_isConnected.value -> SyncStrategy.OFFLINE_ONLY
            _networkType.value == NetworkType.WIFI -> SyncStrategy.AGGRESSIVE
            _connectionQuality.value == ConnectionQuality.EXCELLENT -> SyncStrategy.AGGRESSIVE
            _connectionQuality.value in listOf(ConnectionQuality.GOOD, ConnectionQuality.FAIR) -> SyncStrategy.CONSERVATIVE
            _connectionQuality.value == ConnectionQuality.POOR -> SyncStrategy.CRITICAL_ONLY
            else -> SyncStrategy.MINIMAL
        }
    }
    
    /**
     * Get optimal batch size for sync operations
     */
    fun getOptimalBatchSize(): Int {
        return when (_connectionQuality.value) {
            ConnectionQuality.EXCELLENT -> 100
            ConnectionQuality.GOOD -> 50
            ConnectionQuality.FAIR -> 25
            ConnectionQuality.POOR -> 10
            ConnectionQuality.VERY_POOR -> 5
            ConnectionQuality.UNKNOWN -> 25
        }
    }
    
    /**
     * Get request timeout based on network quality
     */
    fun getRequestTimeout(): Long {
        return when (_connectionQuality.value) {
            ConnectionQuality.EXCELLENT -> 10_000L // 10 seconds
            ConnectionQuality.GOOD -> 15_000L // 15 seconds
            ConnectionQuality.FAIR -> 30_000L // 30 seconds
            ConnectionQuality.POOR -> 60_000L // 1 minute
            ConnectionQuality.VERY_POOR -> 120_000L // 2 minutes
            ConnectionQuality.UNKNOWN -> 30_000L // 30 seconds
        }
    }
    
    /**
     * Should use data compression
     */
    fun shouldUseCompression(): Boolean {
        return _isMetered.value || _connectionQuality.value in listOf(
            ConnectionQuality.POOR,
            ConnectionQuality.VERY_POOR
        )
    }
    
    /**
     * Should defer non-critical operations
     */
    fun shouldDeferNonCritical(): Boolean {
        return _connectionQuality.value in listOf(
            ConnectionQuality.POOR,
            ConnectionQuality.VERY_POOR
        ) || (_isMetered.value && _networkType.value != NetworkType.WIFI)
    }
    
    /**
     * Get optimal image quality for current network
     */
    fun getOptimalImageQuality(): ImageQuality {
        return when (_connectionQuality.value) {
            ConnectionQuality.EXCELLENT -> ImageQuality.HIGH
            ConnectionQuality.GOOD -> ImageQuality.MEDIUM
            ConnectionQuality.FAIR -> ImageQuality.LOW
            ConnectionQuality.POOR -> ImageQuality.VERY_LOW
            ConnectionQuality.VERY_POOR -> ImageQuality.THUMBNAIL
            ConnectionQuality.UNKNOWN -> ImageQuality.LOW
        }
    }
    
    /**
     * Stop network monitoring
     */
    fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

/**
 * Network types
 */
enum class NetworkType {
    WIFI,
    CELLULAR_2G,
    CELLULAR_3G,
    CELLULAR_4G,
    CELLULAR_5G,
    ETHERNET,
    UNKNOWN
}

/**
 * Connection quality levels
 */
enum class ConnectionQuality {
    EXCELLENT,  // >10 Mbps down, >1 Mbps up
    GOOD,       // >5 Mbps down, >500 Kbps up
    FAIR,       // >1 Mbps down, >100 Kbps up
    POOR,       // >100 Kbps down, >50 Kbps up
    VERY_POOR,  // <100 Kbps down
    UNKNOWN
}

/**
 * Sync strategies based on network conditions
 */
enum class SyncStrategy {
    AGGRESSIVE,     // Full sync, all data types
    CONSERVATIVE,   // Selective sync, compressed data
    CRITICAL_ONLY,  // Only critical data (transfers, payments)
    MINIMAL,        // Only essential updates
    OFFLINE_ONLY    // No network operations
}

/**
 * Image quality levels for network optimization
 */
enum class ImageQuality {
    HIGH,       // Original quality
    MEDIUM,     // 70% quality
    LOW,        // 50% quality
    VERY_LOW,   // 30% quality
    THUMBNAIL   // Small thumbnail only
}

/**
 * Network optimization configuration
 */
data class NetworkOptimizationConfig(
    val syncStrategy: SyncStrategy,
    val batchSize: Int,
    val requestTimeout: Long,
    val useCompression: Boolean,
    val imageQuality: ImageQuality,
    val deferNonCritical: Boolean
) {
    companion object {
        fun fromNetworkState(networkStateManager: NetworkStateManager): NetworkOptimizationConfig {
            return NetworkOptimizationConfig(
                syncStrategy = networkStateManager.getSyncStrategy(),
                batchSize = networkStateManager.getOptimalBatchSize(),
                requestTimeout = networkStateManager.getRequestTimeout(),
                useCompression = networkStateManager.shouldUseCompression(),
                imageQuality = networkStateManager.getOptimalImageQuality(),
                deferNonCritical = networkStateManager.shouldDeferNonCritical()
            )
        }
    }
}
