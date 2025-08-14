package com.rio.rostry.core.network;

/**
 * Sync strategies based on network conditions
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/rio/rostry/core/network/SyncStrategy;", "", "(Ljava/lang/String;I)V", "AGGRESSIVE", "CONSERVATIVE", "CRITICAL_ONLY", "MINIMAL", "OFFLINE_ONLY", "network_debug"})
public enum SyncStrategy {
    /*public static final*/ AGGRESSIVE /* = new AGGRESSIVE() */,
    /*public static final*/ CONSERVATIVE /* = new CONSERVATIVE() */,
    /*public static final*/ CRITICAL_ONLY /* = new CRITICAL_ONLY() */,
    /*public static final*/ MINIMAL /* = new MINIMAL() */,
    /*public static final*/ OFFLINE_ONLY /* = new OFFLINE_ONLY() */;
    
    SyncStrategy() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.rio.rostry.core.network.SyncStrategy> getEntries() {
        return null;
    }
}