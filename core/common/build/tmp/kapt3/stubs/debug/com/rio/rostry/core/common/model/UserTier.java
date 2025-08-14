package com.rio.rostry.core.common.model;

/**
 * User tier enumeration for permission-based access control
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\n\b\u0086\u0081\u0002\u0018\u0000 \u000e2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001\u000eB\u0017\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nj\u0002\b\u000bj\u0002\b\fj\u0002\b\r\u00a8\u0006\u000f"}, d2 = {"Lcom/rio/rostry/core/common/model/UserTier;", "", "displayName", "", "level", "", "(Ljava/lang/String;ILjava/lang/String;I)V", "getDisplayName", "()Ljava/lang/String;", "getLevel", "()I", "GENERAL", "FARMER", "ENTHUSIAST", "Companion", "common_debug"})
public enum UserTier {
    /*public static final*/ GENERAL /* = new GENERAL(null, 0) */,
    /*public static final*/ FARMER /* = new FARMER(null, 0) */,
    /*public static final*/ ENTHUSIAST /* = new ENTHUSIAST(null, 0) */;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String displayName = null;
    private final int level = 0;
    @org.jetbrains.annotations.NotNull()
    public static final com.rio.rostry.core.common.model.UserTier.Companion Companion = null;
    
    UserTier(java.lang.String displayName, int level) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDisplayName() {
        return null;
    }
    
    public final int getLevel() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.rio.rostry.core.common.model.UserTier> getEntries() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u0004\u0018\u00010\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/model/UserTier$Companion;", "", "()V", "fromString", "Lcom/rio/rostry/core/common/model/UserTier;", "value", "", "common_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.rio.rostry.core.common.model.UserTier fromString(@org.jetbrains.annotations.Nullable()
        java.lang.String value) {
            return null;
        }
    }
}