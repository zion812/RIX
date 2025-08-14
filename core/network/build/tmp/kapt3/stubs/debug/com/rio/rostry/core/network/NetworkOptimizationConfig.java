package com.rio.rostry.core.network;

/**
 * Network optimization configuration
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0018\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\b\u0018\u0000 %2\u00020\u0001:\u0001%B5\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\t\u00a2\u0006\u0002\u0010\rJ\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\tH\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u000bH\u00c6\u0003J\t\u0010\u001e\u001a\u00020\tH\u00c6\u0003JE\u0010\u001f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\tH\u00c6\u0001J\u0013\u0010 \u001a\u00020\t2\b\u0010!\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\"\u001a\u00020\u0005H\u00d6\u0001J\t\u0010#\u001a\u00020$H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\f\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0011\u00a8\u0006&"}, d2 = {"Lcom/rio/rostry/core/network/NetworkOptimizationConfig;", "", "syncStrategy", "Lcom/rio/rostry/core/network/SyncStrategy;", "batchSize", "", "requestTimeout", "", "useCompression", "", "imageQuality", "Lcom/rio/rostry/core/network/ImageQuality;", "deferNonCritical", "(Lcom/rio/rostry/core/network/SyncStrategy;IJZLcom/rio/rostry/core/network/ImageQuality;Z)V", "getBatchSize", "()I", "getDeferNonCritical", "()Z", "getImageQuality", "()Lcom/rio/rostry/core/network/ImageQuality;", "getRequestTimeout", "()J", "getSyncStrategy", "()Lcom/rio/rostry/core/network/SyncStrategy;", "getUseCompression", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "other", "hashCode", "toString", "", "Companion", "network_debug"})
public final class NetworkOptimizationConfig {
    @org.jetbrains.annotations.NotNull()
    private final com.rio.rostry.core.network.SyncStrategy syncStrategy = null;
    private final int batchSize = 0;
    private final long requestTimeout = 0L;
    private final boolean useCompression = false;
    @org.jetbrains.annotations.NotNull()
    private final com.rio.rostry.core.network.ImageQuality imageQuality = null;
    private final boolean deferNonCritical = false;
    @org.jetbrains.annotations.NotNull()
    public static final com.rio.rostry.core.network.NetworkOptimizationConfig.Companion Companion = null;
    
    public NetworkOptimizationConfig(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.network.SyncStrategy syncStrategy, int batchSize, long requestTimeout, boolean useCompression, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.network.ImageQuality imageQuality, boolean deferNonCritical) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.network.SyncStrategy getSyncStrategy() {
        return null;
    }
    
    public final int getBatchSize() {
        return 0;
    }
    
    public final long getRequestTimeout() {
        return 0L;
    }
    
    public final boolean getUseCompression() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.network.ImageQuality getImageQuality() {
        return null;
    }
    
    public final boolean getDeferNonCritical() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.network.SyncStrategy component1() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final long component3() {
        return 0L;
    }
    
    public final boolean component4() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.network.ImageQuality component5() {
        return null;
    }
    
    public final boolean component6() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.network.NetworkOptimizationConfig copy(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.network.SyncStrategy syncStrategy, int batchSize, long requestTimeout, boolean useCompression, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.network.ImageQuality imageQuality, boolean deferNonCritical) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/network/NetworkOptimizationConfig$Companion;", "", "()V", "fromNetworkState", "Lcom/rio/rostry/core/network/NetworkOptimizationConfig;", "networkStateManager", "Lcom/rio/rostry/core/network/NetworkStateManager;", "network_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.rio.rostry.core.network.NetworkOptimizationConfig fromNetworkState(@org.jetbrains.annotations.NotNull()
        com.rio.rostry.core.network.NetworkStateManager networkStateManager) {
            return null;
        }
    }
}