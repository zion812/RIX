package com.rio.rostry.core.common.model;

/**
 * Network connection state
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0003\u0003\u0004\u0005B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0003\u0006\u0007\b\u00a8\u0006\t"}, d2 = {"Lcom/rio/rostry/core/common/model/NetworkState;", "", "()V", "Connected", "Disconnected", "Limited", "Lcom/rio/rostry/core/common/model/NetworkState$Connected;", "Lcom/rio/rostry/core/common/model/NetworkState$Disconnected;", "Lcom/rio/rostry/core/common/model/NetworkState$Limited;", "common_debug"})
public abstract class NetworkState {
    
    private NetworkState() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rio/rostry/core/common/model/NetworkState$Connected;", "Lcom/rio/rostry/core/common/model/NetworkState;", "()V", "common_debug"})
    public static final class Connected extends com.rio.rostry.core.common.model.NetworkState {
        @org.jetbrains.annotations.NotNull()
        public static final com.rio.rostry.core.common.model.NetworkState.Connected INSTANCE = null;
        
        private Connected() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/rio/rostry/core/common/model/NetworkState$Disconnected;", "Lcom/rio/rostry/core/common/model/NetworkState;", "()V", "common_debug"})
    public static final class Disconnected extends com.rio.rostry.core.common.model.NetworkState {
        @org.jetbrains.annotations.NotNull()
        public static final com.rio.rostry.core.common.model.NetworkState.Disconnected INSTANCE = null;
        
        private Disconnected() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/rio/rostry/core/common/model/NetworkState$Limited;", "Lcom/rio/rostry/core/common/model/NetworkState;", "type", "Lcom/rio/rostry/core/common/model/NetworkType;", "(Lcom/rio/rostry/core/common/model/NetworkType;)V", "getType", "()Lcom/rio/rostry/core/common/model/NetworkType;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "common_debug"})
    public static final class Limited extends com.rio.rostry.core.common.model.NetworkState {
        @org.jetbrains.annotations.NotNull()
        private final com.rio.rostry.core.common.model.NetworkType type = null;
        
        public Limited(@org.jetbrains.annotations.NotNull()
        com.rio.rostry.core.common.model.NetworkType type) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.rio.rostry.core.common.model.NetworkType getType() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.rio.rostry.core.common.model.NetworkType component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.rio.rostry.core.common.model.NetworkState.Limited copy(@org.jetbrains.annotations.NotNull()
        com.rio.rostry.core.common.model.NetworkType type) {
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
    }
}