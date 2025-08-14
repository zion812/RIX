package com.rio.rostry.core.common.exceptions;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u001a\n\u0000\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0003\n\u0000\u001a\n\u0010\u0000\u001a\u00020\u0001*\u00020\u0002\u001a\n\u0010\u0003\u001a\u00020\u0001*\u00020\u0002\u001a\n\u0010\u0004\u001a\u00020\u0005*\u00020\u0002\u001a\n\u0010\u0006\u001a\u00020\u0002*\u00020\u0007\u00a8\u0006\b"}, d2 = {"getCategory", "", "Lcom/rio/rostry/core/common/exceptions/SyncException;", "getUserMessage", "isRetryable", "", "toSyncException", "", "common_debug"})
public final class SyncExceptionsKt {
    
    /**
     * Check if exception is retryable
     */
    public static final boolean isRetryable(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.common.exceptions.SyncException $this$isRetryable) {
        return false;
    }
    
    /**
     * Get user-friendly error message
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String getUserMessage(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.common.exceptions.SyncException $this$getUserMessage) {
        return null;
    }
    
    /**
     * Get error category for analytics
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String getCategory(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.common.exceptions.SyncException $this$getCategory) {
        return null;
    }
    
    /**
     * Convert throwable to appropriate SyncException
     */
    @org.jetbrains.annotations.NotNull()
    public static final com.rio.rostry.core.common.exceptions.SyncException toSyncException(@org.jetbrains.annotations.NotNull()
    java.lang.Throwable $this$toSyncException) {
        return null;
    }
}