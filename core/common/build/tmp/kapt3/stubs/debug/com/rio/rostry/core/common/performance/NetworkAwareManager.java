package com.rio.rostry.core.common.performance;

/**
 * Network-aware manager for optimizing performance based on connection quality
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u001d\u001a\u00020\t2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u0010\u0010 \u001a\u00020\r2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u0010\u0010!\u001a\u00020\u00072\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u0006\u0010\"\u001a\u00020\u0007J\u0006\u0010#\u001a\u00020$J\u0006\u0010%\u001a\u00020&J\u0006\u0010\'\u001a\u00020(J\u0006\u0010)\u001a\u00020\u0007J\u0006\u0010*\u001a\u00020+J\u0006\u0010,\u001a\u00020\u000bJ\u0006\u0010-\u001a\u00020\u000bJ\u0006\u0010.\u001a\u00020\u000bJ\b\u0010/\u001a\u000200H\u0002J\u0006\u00101\u001a\u000200J\u0014\u00102\u001a\u0002002\n\b\u0002\u00103\u001a\u0004\u0018\u00010\u001fH\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\t0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u000b0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0011R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0011R\u000e\u0010\u001b\u001a\u00020\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00064"}, d2 = {"Lcom/rio/rostry/core/common/performance/NetworkAwareManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "_bandwidthEstimate", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_connectionQuality", "Lcom/rio/rostry/core/common/network/ConnectionQuality;", "_isConnected", "", "_networkType", "Lcom/rio/rostry/core/common/network/NetworkType;", "bandwidthEstimate", "Lkotlinx/coroutines/flow/StateFlow;", "getBandwidthEstimate", "()Lkotlinx/coroutines/flow/StateFlow;", "connectionQuality", "getConnectionQuality", "connectivityManager", "Landroid/net/ConnectivityManager;", "isConnected", "networkCallback", "Landroid/net/ConnectivityManager$NetworkCallback;", "networkType", "getNetworkType", "telephonyManager", "Landroid/telephony/TelephonyManager;", "determineConnectionQuality", "capabilities", "Landroid/net/NetworkCapabilities;", "determineNetworkType", "estimateBandwidth", "getOptimalCacheSize", "getOptimalImageQuality", "Lcom/rio/rostry/core/common/performance/ImageQuality;", "getOptimalPageSize", "", "getOptimalVideoQuality", "Lcom/rio/rostry/core/common/performance/VideoQuality;", "getRequestTimeout", "getSyncStrategy", "Lcom/rio/rostry/core/common/network/SyncStrategy;", "shouldEnableRealTimeFeatures", "shouldPreloadData", "shouldUseCompression", "startNetworkMonitoring", "", "stopMonitoring", "updateNetworkInfo", "networkCapabilities", "common_debug"})
public final class NetworkAwareManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final android.net.ConnectivityManager connectivityManager = null;
    @org.jetbrains.annotations.NotNull()
    private final android.telephony.TelephonyManager telephonyManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.rio.rostry.core.common.network.NetworkType> _networkType = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.rio.rostry.core.common.network.NetworkType> networkType = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isConnected = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isConnected = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.rio.rostry.core.common.network.ConnectionQuality> _connectionQuality = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.rio.rostry.core.common.network.ConnectionQuality> connectionQuality = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Long> _bandwidthEstimate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Long> bandwidthEstimate = null;
    @org.jetbrains.annotations.NotNull()
    private final android.net.ConnectivityManager.NetworkCallback networkCallback = null;
    
    @javax.inject.Inject()
    public NetworkAwareManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.rio.rostry.core.common.network.NetworkType> getNetworkType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isConnected() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.rio.rostry.core.common.network.ConnectionQuality> getConnectionQuality() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Long> getBandwidthEstimate() {
        return null;
    }
    
    /**
     * Start monitoring network changes
     */
    private final void startNetworkMonitoring() {
    }
    
    /**
     * Update network information
     */
    private final void updateNetworkInfo(android.net.NetworkCapabilities networkCapabilities) {
    }
    
    /**
     * Determine network type from capabilities
     */
    private final com.rio.rostry.core.common.network.NetworkType determineNetworkType(android.net.NetworkCapabilities capabilities) {
        return null;
    }
    
    /**
     * Determine connection quality
     */
    private final com.rio.rostry.core.common.network.ConnectionQuality determineConnectionQuality(android.net.NetworkCapabilities capabilities) {
        return null;
    }
    
    /**
     * Estimate bandwidth in kbps
     */
    private final long estimateBandwidth(android.net.NetworkCapabilities capabilities) {
        return 0L;
    }
    
    /**
     * Get optimal image quality based on network
     */
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.common.performance.ImageQuality getOptimalImageQuality() {
        return null;
    }
    
    /**
     * Get optimal page size for pagination
     */
    public final int getOptimalPageSize() {
        return 0;
    }
    
    /**
     * Get optimal cache size
     */
    public final long getOptimalCacheSize() {
        return 0L;
    }
    
    /**
     * Should preload data based on network
     */
    public final boolean shouldPreloadData() {
        return false;
    }
    
    /**
     * Should use data compression
     */
    public final boolean shouldUseCompression() {
        return false;
    }
    
    /**
     * Get sync strategy based on network
     */
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.common.network.SyncStrategy getSyncStrategy() {
        return null;
    }
    
    /**
     * Get request timeout based on network
     */
    public final long getRequestTimeout() {
        return 0L;
    }
    
    /**
     * Should enable real-time features
     */
    public final boolean shouldEnableRealTimeFeatures() {
        return false;
    }
    
    /**
     * Get optimal video quality
     */
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.common.performance.VideoQuality getOptimalVideoQuality() {
        return null;
    }
    
    /**
     * Stop network monitoring
     */
    public final void stopMonitoring() {
    }
}