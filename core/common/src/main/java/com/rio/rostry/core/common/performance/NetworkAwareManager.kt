package com.rio.rostry.core.common.performance

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.telephony.TelephonyManager
import android.util.Log
import com.rio.rostry.core.common.network.NetworkType
import com.rio.rostry.core.common.network.ConnectionQuality
import com.rio.rostry.core.common.network.SyncStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network-aware manager for optimizing performance based on connection quality
 */
@Singleton
class NetworkAwareManager @Inject constructor(
    private val context: Context
) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private val _networkType = MutableStateFlow(NetworkType.OTHER)
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionQuality = MutableStateFlow(ConnectionQuality.UNKNOWN)
    val connectionQuality: StateFlow<ConnectionQuality> = _connectionQuality.asStateFlow()

    private val _bandwidthEstimate = MutableStateFlow(0L)
    val bandwidthEstimate: StateFlow<Long> = _bandwidthEstimate.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
            updateNetworkInfo()
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
            _networkType.value = NetworkType.OTHER
            _connectionQuality.value = ConnectionQuality.UNKNOWN
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
        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            try {
                connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            } catch (ex: SecurityException) {
                Log.e(
                    "NetworkAwareManager",
                    "registerNetworkCallback requires ACCESS_NETWORK_STATE. Fallback to offline.",
                    ex
                )
                _isConnected.value = false
            }
        } else {
            Log.w(
                "NetworkAwareManager",
                "Missing ACCESS_NETWORK_STATE permission. Offline behavior enforced."
            )
            _isConnected.value = false
        }
    }

    /**
     * Update network information
     */
    private fun updateNetworkInfo(networkCapabilities: NetworkCapabilities? = null) {
        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            val capabilities = try {
                networkCapabilities ?: connectivityManager.getNetworkCapabilities(
                    connectivityManager.activeNetwork
                )
            } catch (ex: SecurityException) {
                Log.e(
                    "NetworkAwareManager",
                    "getNetworkCapabilities requires ACCESS_NETWORK_STATE.",
                    ex
                )
                null
            }
            if (capabilities != null) {
                _networkType.value = determineNetworkType(capabilities)
                _connectionQuality.value = determineConnectionQuality(capabilities)
                _bandwidthEstimate.value = estimateBandwidth(capabilities)
            }
        } else {
            Log.w(
                "NetworkAwareManager",
                "Missing ACCESS_NETWORK_STATE permission. Network info unavailable."
            )
            _networkType.value = NetworkType.OTHER
            _connectionQuality.value = ConnectionQuality.UNKNOWN
        }
    }

    /**
     * Determine network type from capabilities
     */
    private fun determineNetworkType(capabilities: NetworkCapabilities): NetworkType {
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            else -> NetworkType.OTHER
        }
    }

    /**
     * Determine connection quality
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
     * Get optimal image quality based on network
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
     * Get optimal page size for pagination
     */
    fun getOptimalPageSize(): Int {
        return when (_connectionQuality.value) {
            ConnectionQuality.EXCELLENT -> 50
            ConnectionQuality.GOOD -> 30
            ConnectionQuality.FAIR -> 20
            ConnectionQuality.POOR -> 10
            ConnectionQuality.VERY_POOR -> 5
            ConnectionQuality.UNKNOWN -> 20
        }
    }

    /**
     * Get optimal cache size
     */
    fun getOptimalCacheSize(): Long {
        return when (_connectionQuality.value) {
            ConnectionQuality.EXCELLENT -> 200 * 1024 * 1024L // 200MB
            ConnectionQuality.GOOD -> 100 * 1024 * 1024L // 100MB
            ConnectionQuality.FAIR -> 50 * 1024 * 1024L // 50MB
            ConnectionQuality.POOR -> 25 * 1024 * 1024L // 25MB
            ConnectionQuality.VERY_POOR -> 10 * 1024 * 1024L // 10MB
            ConnectionQuality.UNKNOWN -> 50 * 1024 * 1024L // 50MB
        }
    }

    /**
     * Should preload data based on network
     */
    fun shouldPreloadData(): Boolean {
        return _networkType.value == NetworkType.WIFI || 
               _connectionQuality.value in listOf(ConnectionQuality.EXCELLENT, ConnectionQuality.GOOD)
    }

    /**
     * Should use data compression
     */
    fun shouldUseCompression(): Boolean {
        return _networkType.value != NetworkType.WIFI || 
               _connectionQuality.value in listOf(ConnectionQuality.POOR, ConnectionQuality.VERY_POOR)
    }

    /**
     * Get sync strategy based on network
     */
    fun getSyncStrategy(): SyncStrategy {
        return when {
            _networkType.value == NetworkType.WIFI -> SyncStrategy.AGGRESSIVE
            _connectionQuality.value == ConnectionQuality.EXCELLENT -> SyncStrategy.AGGRESSIVE
            _connectionQuality.value in listOf(
                ConnectionQuality.GOOD,
                ConnectionQuality.FAIR
            ) -> SyncStrategy.CONSERVATIVE

            else -> SyncStrategy.MINIMAL
        }
    }

    /**
     * Get request timeout based on network
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
     * Should enable real-time features
     */
    fun shouldEnableRealTimeFeatures(): Boolean {
        return _connectionQuality.value in listOf(
            ConnectionQuality.EXCELLENT,
            ConnectionQuality.GOOD,
            ConnectionQuality.FAIR
        )
    }

    /**
     * Get optimal video quality
     */
    fun getOptimalVideoQuality(): VideoQuality {
        return when (_connectionQuality.value) {
            ConnectionQuality.EXCELLENT -> VideoQuality.HD
            ConnectionQuality.GOOD -> VideoQuality.MEDIUM
            ConnectionQuality.FAIR -> VideoQuality.LOW
            ConnectionQuality.POOR -> VideoQuality.VERY_LOW
            ConnectionQuality.VERY_POOR -> VideoQuality.AUDIO_ONLY
            ConnectionQuality.UNKNOWN -> VideoQuality.LOW
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
 * Connection quality levels
 */
enum class ConnectionQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    VERY_POOR,
    UNKNOWN
}

/**
 * Image quality levels
 */
enum class ImageQuality {
    HIGH,
    MEDIUM,
    LOW,
    VERY_LOW,
    THUMBNAIL
}

/**
 * Video quality levels
 */
enum class VideoQuality {
    HD,
    MEDIUM,
    LOW,
    VERY_LOW,
    AUDIO_ONLY
}

/**
 * Sync strategies
 */
enum class SyncStrategy {
    AGGRESSIVE,
    CONSERVATIVE,
    MINIMAL,
    MANUAL
}

/**
 * Network optimization configuration
 */
data class NetworkOptimizationConfig(
    val imageQuality: ImageQuality,
    val videoQuality: VideoQuality,
    val pageSize: Int,
    val cacheSize: Long,
    val useCompression: Boolean,
    val preloadData: Boolean,
    val syncStrategy: SyncStrategy,
    val requestTimeout: Long,
    val enableRealTime: Boolean
) {
    companion object {
        fun fromNetworkManager(networkManager: NetworkAwareManager): NetworkOptimizationConfig {
            return NetworkOptimizationConfig(
                imageQuality = networkManager.getOptimalImageQuality(),
                videoQuality = networkManager.getOptimalVideoQuality(),
                pageSize = networkManager.getOptimalPageSize(),
                cacheSize = networkManager.getOptimalCacheSize(),
                useCompression = networkManager.shouldUseCompression(),
                preloadData = networkManager.shouldPreloadData(),
                syncStrategy = networkManager.getSyncStrategy(),
                requestTimeout = networkManager.getRequestTimeout(),
                enableRealTime = networkManager.shouldEnableRealTimeFeatures()
            )
        }
    }
}
