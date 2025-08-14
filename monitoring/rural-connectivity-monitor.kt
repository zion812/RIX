/**
 * Rural Connectivity Monitor for RIO Platform
 * Specialized monitoring for rural network conditions and optimization
 */
package com.rio.rostry.monitoring

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.telephony.TelephonyManager
import androidx.work.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuralConnectivityMonitor @Inject constructor(
    private val context: Context,
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
    private val performance: FirebasePerformance,
    private val workManager: WorkManager
) {

    companion object {
        private const val TAG = "RuralConnectivityMonitor"
        private const val CONNECTIVITY_CHECK_INTERVAL = 30L // seconds
        private const val PERFORMANCE_SAMPLE_RATE = 0.1 // 10% sampling

        // Network quality thresholds for rural optimization
        private const val POOR_NETWORK_THRESHOLD = 100L // ms
        private const val MODERATE_NETWORK_THRESHOLD = 500L // ms
        private const val GOOD_NETWORK_THRESHOLD = 1000L // ms
    }

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private val _networkState = MutableStateFlow(NetworkState.UNKNOWN)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val _connectivityMetrics = MutableStateFlow(ConnectivityMetrics())
    val connectivityMetrics: StateFlow<ConnectivityMetrics> = _connectivityMetrics.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var performanceTrace: Trace? = null

    data class NetworkState(
        val isConnected: Boolean = false,
        val networkType: NetworkType = NetworkType.UNKNOWN,
        val quality: NetworkQuality = NetworkQuality.UNKNOWN,
        val signalStrength: Int = 0,
        val latency: Long = 0L,
        val bandwidth: Long = 0L,
        val isMetered: Boolean = true
    ) {
        companion object {
            val UNKNOWN = NetworkState()
        }
    }

    enum class NetworkType {
        WIFI, CELLULAR_2G, CELLULAR_3G, CELLULAR_4G, CELLULAR_5G, ETHERNET, UNKNOWN
    }

    enum class NetworkQuality {
        EXCELLENT, GOOD, MODERATE, POOR, VERY_POOR, UNKNOWN
    }

    data class ConnectivityMetrics(
        val totalConnectedTime: Long = 0L,
        val totalOfflineTime: Long = 0L,
        val networkSwitches: Int = 0,
        val averageLatency: Double = 0.0,
        val dataUsage: Long = 0L,
        val syncFailures: Int = 0,
        val lastSyncTime: Long = 0L
    )

    fun startMonitoring() {
        // Register network callback for real-time monitoring
        registerNetworkCallback()

        // Start periodic connectivity checks
        schedulePeriodicConnectivityCheck()

        // Initialize performance monitoring
        initializePerformanceMonitoring()

        // Set up crash reporting with rural context
        setupRuralCrashReporting()
    }

    fun stopMonitoring() {
        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        performanceTrace?.stop()
        workManager.cancelUniqueWork("connectivity_check")
    }

    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                updateNetworkState(network, true)
                logConnectivityEvent("network_available")
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                updateNetworkState(network, false)
                logConnectivityEvent("network_lost")
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, capabilities)
                updateNetworkCapabilities(network, capabilities)
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    private fun updateNetworkState(network: Network, isConnected: Boolean) {
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val networkType = determineNetworkType(capabilities)
        val isMetered = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == false

        val newState = _networkState.value.copy(
            isConnected = isConnected,
            networkType = networkType,
            isMetered = isMetered
        )

        _networkState.value = newState

        // Log network state change for analytics
        analytics.logEvent("network_state_changed", Bundle().apply {
            putString("network_type", networkType.name)
            putBoolean("is_connected", isConnected)
            putBoolean("is_metered", isMetered)
        })
    }

    private fun updateNetworkCapabilities(network: Network, capabilities: NetworkCapabilities) {
        val bandwidth = capabilities.linkDownstreamBandwidthKbps
        val signalStrength = getSignalStrength()

        // Measure network latency
        measureNetworkLatency { latency ->
            val quality = determineNetworkQuality(latency, bandwidth)

            val updatedState = _networkState.value.copy(
                quality = quality,
                signalStrength = signalStrength,
                latency = latency,
                bandwidth = bandwidth.toLong()
            )

            _networkState.value = updatedState

            // Log network quality metrics
            logNetworkQualityMetrics(quality, latency, bandwidth, signalStrength)
        }
    }

    private fun determineNetworkType(capabilities: NetworkCapabilities?): NetworkType {
        return when {
            capabilities == null -> NetworkType.UNKNOWN
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // Determine cellular generation based on network info
                when (telephonyManager.dataNetworkType) {
                    TelephonyManager.NETWORK_TYPE_GPRS,
                    TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_CDMA,
                    TelephonyManager.NETWORK_TYPE_1xRTT -> NetworkType.CELLULAR_2G

                    TelephonyManager.NETWORK_TYPE_UMTS,
                    TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A,
                    TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_EVDO_B,
                    TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP -> NetworkType.CELLULAR_3G

                    TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.CELLULAR_4G
                    TelephonyManager.NETWORK_TYPE_NR -> NetworkType.CELLULAR_5G
                    else -> NetworkType.UNKNOWN
                }
            }
            else -> NetworkType.UNKNOWN
        }
    }