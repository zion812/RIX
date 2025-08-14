package com.rio.rostry.core.common.exceptions;

/**
 * Base sync exception
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00060\u0001j\u0002`\u0002:\n\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011B\u001b\b\u0004\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\u0007\u0082\u0001\n\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u00a8\u0006\u001c"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException;", "Ljava/lang/Exception;", "Lkotlin/Exception;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "AuthError", "ConflictError", "DataProcessingError", "DatabaseError", "MediaError", "NetworkError", "RegionalError", "SyncOperationError", "UnknownError", "ValidationError", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError;", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError;", "Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError;", "Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError;", "Lcom/rio/rostry/core/common/exceptions/SyncException$UnknownError;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError;", "common_debug"})
public abstract class SyncException extends java.lang.Exception {
    
    private SyncException(java.lang.String message, java.lang.Throwable cause) {
        super();
    }
    
    /**
     * Authentication and authorization errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0006\u0007\b\t\n\u000b\fB\u001b\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u0082\u0001\u0006\r\u000e\u000f\u0010\u0011\u0012\u00a8\u0006\u0013"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError;", "Lcom/rio/rostry/core/common/exceptions/SyncException;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "AccountSuspended", "InsufficientPermissions", "InvalidCredentials", "NotAuthenticated", "TierRestriction", "TokenExpired", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError$AccountSuspended;", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError$InsufficientPermissions;", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError$InvalidCredentials;", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError$NotAuthenticated;", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError$TierRestriction;", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError$TokenExpired;", "common_debug"})
    public static abstract class AuthError extends com.rio.rostry.core.common.exceptions.SyncException {
        
        private AuthError(java.lang.String message, java.lang.Throwable cause) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError$AccountSuspended;", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError;", "reason", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class AccountSuspended extends com.rio.rostry.core.common.exceptions.SyncException.AuthError {
            
            public AccountSuspended(@org.jetbrains.annotations.NotNull()
            java.lang.String reason) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError$InsufficientPermissions;", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError;", "action", "", "resource", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class InsufficientPermissions extends com.rio.rostry.core.common.exceptions.SyncException.AuthError {
            
            public InsufficientPermissions(@org.jetbrains.annotations.NotNull()
            java.lang.String action, @org.jetbrains.annotations.NotNull()
            java.lang.String resource) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError$InvalidCredentials;", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError;", "message", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class InvalidCredentials extends com.rio.rostry.core.common.exceptions.SyncException.AuthError {
            
            public InvalidCredentials(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            public InvalidCredentials() {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError$NotAuthenticated;", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError;", "message", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class NotAuthenticated extends com.rio.rostry.core.common.exceptions.SyncException.AuthError {
            
            public NotAuthenticated(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            public NotAuthenticated() {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError$TierRestriction;", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError;", "requiredTier", "", "currentTier", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class TierRestriction extends com.rio.rostry.core.common.exceptions.SyncException.AuthError {
            
            public TierRestriction(@org.jetbrains.annotations.NotNull()
            java.lang.String requiredTier, @org.jetbrains.annotations.NotNull()
            java.lang.String currentTier) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError$TokenExpired;", "Lcom/rio/rostry/core/common/exceptions/SyncException$AuthError;", "message", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class TokenExpired extends com.rio.rostry.core.common.exceptions.SyncException.AuthError {
            
            public TokenExpired(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            public TokenExpired() {
            }
        }
    }
    
    /**
     * Conflict-related errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0005\u0007\b\t\n\u000bB\u001b\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u0082\u0001\u0005\f\r\u000e\u000f\u0010\u00a8\u0006\u0011"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError;", "Lcom/rio/rostry/core/common/exceptions/SyncException;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "ConcurrentModification", "DeletionConflict", "OwnershipConflict", "UnresolvableConflict", "VersionConflict", "Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError$ConcurrentModification;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError$DeletionConflict;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError$OwnershipConflict;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError$UnresolvableConflict;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError$VersionConflict;", "common_debug"})
    public static abstract class ConflictError extends com.rio.rostry.core.common.exceptions.SyncException {
        
        private ConflictError(java.lang.String message, java.lang.Throwable cause) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError$ConcurrentModification;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError;", "entityId", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class ConcurrentModification extends com.rio.rostry.core.common.exceptions.SyncException.ConflictError {
            
            public ConcurrentModification(@org.jetbrains.annotations.NotNull()
            java.lang.String entityId) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError$DeletionConflict;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError;", "entityId", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class DeletionConflict extends com.rio.rostry.core.common.exceptions.SyncException.ConflictError {
            
            public DeletionConflict(@org.jetbrains.annotations.NotNull()
            java.lang.String entityId) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError$OwnershipConflict;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError;", "entityId", "", "currentOwner", "claimedOwner", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class OwnershipConflict extends com.rio.rostry.core.common.exceptions.SyncException.ConflictError {
            
            public OwnershipConflict(@org.jetbrains.annotations.NotNull()
            java.lang.String entityId, @org.jetbrains.annotations.NotNull()
            java.lang.String currentOwner, @org.jetbrains.annotations.NotNull()
            java.lang.String claimedOwner) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError$UnresolvableConflict;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError;", "entityId", "", "reason", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class UnresolvableConflict extends com.rio.rostry.core.common.exceptions.SyncException.ConflictError {
            
            public UnresolvableConflict(@org.jetbrains.annotations.NotNull()
            java.lang.String entityId, @org.jetbrains.annotations.NotNull()
            java.lang.String reason) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0007\u00a8\u0006\b"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError$VersionConflict;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ConflictError;", "entityId", "", "localVersion", "", "serverVersion", "(Ljava/lang/String;JJ)V", "common_debug"})
        public static final class VersionConflict extends com.rio.rostry.core.common.exceptions.SyncException.ConflictError {
            
            public VersionConflict(@org.jetbrains.annotations.NotNull()
            java.lang.String entityId, long localVersion, long serverVersion) {
            }
        }
    }
    
    /**
     * Data compression and serialization errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0006\u0007\b\t\n\u000b\fB\u001b\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u0082\u0001\u0006\r\u000e\u000f\u0010\u0011\u0012\u00a8\u0006\u0013"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError;", "Lcom/rio/rostry/core/common/exceptions/SyncException;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "CompressionFailed", "DecompressionFailed", "DecryptionFailed", "DeserializationFailed", "EncryptionFailed", "SerializationFailed", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError$CompressionFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError$DecompressionFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError$DecryptionFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError$DeserializationFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError$EncryptionFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError$SerializationFailed;", "common_debug"})
    public static abstract class DataProcessingError extends com.rio.rostry.core.common.exceptions.SyncException {
        
        private DataProcessingError(java.lang.String message, java.lang.Throwable cause) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError$CompressionFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
        public static final class CompressionFailed extends com.rio.rostry.core.common.exceptions.SyncException.DataProcessingError {
            
            public CompressionFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String message, @org.jetbrains.annotations.Nullable()
            java.lang.Throwable cause) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError$DecompressionFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
        public static final class DecompressionFailed extends com.rio.rostry.core.common.exceptions.SyncException.DataProcessingError {
            
            public DecompressionFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String message, @org.jetbrains.annotations.Nullable()
            java.lang.Throwable cause) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError$DecryptionFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
        public static final class DecryptionFailed extends com.rio.rostry.core.common.exceptions.SyncException.DataProcessingError {
            
            public DecryptionFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String message, @org.jetbrains.annotations.Nullable()
            java.lang.Throwable cause) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError$DeserializationFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
        public static final class DeserializationFailed extends com.rio.rostry.core.common.exceptions.SyncException.DataProcessingError {
            
            public DeserializationFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String message, @org.jetbrains.annotations.Nullable()
            java.lang.Throwable cause) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError$EncryptionFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
        public static final class EncryptionFailed extends com.rio.rostry.core.common.exceptions.SyncException.DataProcessingError {
            
            public EncryptionFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String message, @org.jetbrains.annotations.Nullable()
            java.lang.Throwable cause) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError$SerializationFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DataProcessingError;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
        public static final class SerializationFailed extends com.rio.rostry.core.common.exceptions.SyncException.DataProcessingError {
            
            public SerializationFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String message, @org.jetbrains.annotations.Nullable()
            java.lang.Throwable cause) {
            }
        }
    }
    
    /**
     * Database-related errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0006\u0007\b\t\n\u000b\fB\u001b\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u0082\u0001\u0006\r\u000e\u000f\u0010\u0011\u0012\u00a8\u0006\u0013"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError;", "Lcom/rio/rostry/core/common/exceptions/SyncException;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "ConnectionFailed", "ConstraintViolation", "DataCorruption", "MigrationFailed", "QueryFailed", "StorageFull", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError$ConnectionFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError$ConstraintViolation;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError$DataCorruption;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError$MigrationFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError$QueryFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError$StorageFull;", "common_debug"})
    public static abstract class DatabaseError extends com.rio.rostry.core.common.exceptions.SyncException {
        
        private DatabaseError(java.lang.String message, java.lang.Throwable cause) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError$ConnectionFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
        public static final class ConnectionFailed extends com.rio.rostry.core.common.exceptions.SyncException.DatabaseError {
            
            public ConnectionFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String message, @org.jetbrains.annotations.Nullable()
            java.lang.Throwable cause) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError$ConstraintViolation;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
        public static final class ConstraintViolation extends com.rio.rostry.core.common.exceptions.SyncException.DatabaseError {
            
            public ConstraintViolation(@org.jetbrains.annotations.NotNull()
            java.lang.String message, @org.jetbrains.annotations.Nullable()
            java.lang.Throwable cause) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError$DataCorruption;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
        public static final class DataCorruption extends com.rio.rostry.core.common.exceptions.SyncException.DatabaseError {
            
            public DataCorruption(@org.jetbrains.annotations.NotNull()
            java.lang.String message, @org.jetbrains.annotations.Nullable()
            java.lang.Throwable cause) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError$MigrationFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
        public static final class MigrationFailed extends com.rio.rostry.core.common.exceptions.SyncException.DatabaseError {
            
            public MigrationFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String message, @org.jetbrains.annotations.Nullable()
            java.lang.Throwable cause) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError$QueryFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
        public static final class QueryFailed extends com.rio.rostry.core.common.exceptions.SyncException.DatabaseError {
            
            public QueryFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String message, @org.jetbrains.annotations.Nullable()
            java.lang.Throwable cause) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError$StorageFull;", "Lcom/rio/rostry/core/common/exceptions/SyncException$DatabaseError;", "message", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class StorageFull extends com.rio.rostry.core.common.exceptions.SyncException.DatabaseError {
            
            public StorageFull(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            public StorageFull() {
            }
        }
    }
    
    /**
     * Media and file handling errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0006\u0007\b\t\n\u000b\fB\u001b\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u0082\u0001\u0006\r\u000e\u000f\u0010\u0011\u0012\u00a8\u0006\u0013"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError;", "Lcom/rio/rostry/core/common/exceptions/SyncException;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "CompressionFailed", "DownloadFailed", "FileNotFound", "FileTooLarge", "UnsupportedFormat", "UploadFailed", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError$CompressionFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError$DownloadFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError$FileNotFound;", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError$FileTooLarge;", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError$UnsupportedFormat;", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError$UploadFailed;", "common_debug"})
    public static abstract class MediaError extends com.rio.rostry.core.common.exceptions.SyncException {
        
        private MediaError(java.lang.String message, java.lang.Throwable cause) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError$CompressionFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError;", "fileName", "", "reason", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class CompressionFailed extends com.rio.rostry.core.common.exceptions.SyncException.MediaError {
            
            public CompressionFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String fileName, @org.jetbrains.annotations.NotNull()
            java.lang.String reason) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError$DownloadFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError;", "url", "", "reason", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class DownloadFailed extends com.rio.rostry.core.common.exceptions.SyncException.MediaError {
            
            public DownloadFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String url, @org.jetbrains.annotations.NotNull()
            java.lang.String reason) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError$FileNotFound;", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError;", "filePath", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class FileNotFound extends com.rio.rostry.core.common.exceptions.SyncException.MediaError {
            
            public FileNotFound(@org.jetbrains.annotations.NotNull()
            java.lang.String filePath) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0007\u00a8\u0006\b"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError$FileTooLarge;", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError;", "fileName", "", "size", "", "maxSize", "(Ljava/lang/String;JJ)V", "common_debug"})
        public static final class FileTooLarge extends com.rio.rostry.core.common.exceptions.SyncException.MediaError {
            
            public FileTooLarge(@org.jetbrains.annotations.NotNull()
            java.lang.String fileName, long size, long maxSize) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError$UnsupportedFormat;", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError;", "fileName", "", "mimeType", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class UnsupportedFormat extends com.rio.rostry.core.common.exceptions.SyncException.MediaError {
            
            public UnsupportedFormat(@org.jetbrains.annotations.NotNull()
            java.lang.String fileName, @org.jetbrains.annotations.NotNull()
            java.lang.String mimeType) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError$UploadFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$MediaError;", "fileName", "", "reason", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class UploadFailed extends com.rio.rostry.core.common.exceptions.SyncException.MediaError {
            
            public UploadFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String fileName, @org.jetbrains.annotations.NotNull()
            java.lang.String reason) {
            }
        }
    }
    
    /**
     * Network-related errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\b\u0007\b\t\n\u000b\f\r\u000eB\u001b\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u0082\u0001\b\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u00a8\u0006\u0017"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError;", "Lcom/rio/rostry/core/common/exceptions/SyncException;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "BadRequest", "Forbidden", "NoConnection", "NotFound", "RateLimited", "ServerError", "Timeout", "Unauthorized", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$BadRequest;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$Forbidden;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$NoConnection;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$NotFound;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$RateLimited;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$ServerError;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$Timeout;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$Unauthorized;", "common_debug"})
    public static abstract class NetworkError extends com.rio.rostry.core.common.exceptions.SyncException {
        
        private NetworkError(java.lang.String message, java.lang.Throwable cause) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$BadRequest;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError;", "message", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class BadRequest extends com.rio.rostry.core.common.exceptions.SyncException.NetworkError {
            
            public BadRequest(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$Forbidden;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError;", "message", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class Forbidden extends com.rio.rostry.core.common.exceptions.SyncException.NetworkError {
            
            public Forbidden(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            public Forbidden() {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$NoConnection;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError;", "message", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class NoConnection extends com.rio.rostry.core.common.exceptions.SyncException.NetworkError {
            
            public NoConnection(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            public NoConnection() {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$NotFound;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError;", "message", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class NotFound extends com.rio.rostry.core.common.exceptions.SyncException.NetworkError {
            
            public NotFound(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            public NotFound() {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$RateLimited;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError;", "message", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class RateLimited extends com.rio.rostry.core.common.exceptions.SyncException.NetworkError {
            
            public RateLimited(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            public RateLimited() {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$ServerError;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
        public static final class ServerError extends com.rio.rostry.core.common.exceptions.SyncException.NetworkError {
            
            public ServerError(@org.jetbrains.annotations.NotNull()
            java.lang.String message, @org.jetbrains.annotations.Nullable()
            java.lang.Throwable cause) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$Timeout;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError;", "message", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class Timeout extends com.rio.rostry.core.common.exceptions.SyncException.NetworkError {
            
            public Timeout(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            public Timeout() {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError$Unauthorized;", "Lcom/rio/rostry/core/common/exceptions/SyncException$NetworkError;", "message", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class Unauthorized extends com.rio.rostry.core.common.exceptions.SyncException.NetworkError {
            
            public Unauthorized(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            public Unauthorized() {
            }
        }
    }
    
    /**
     * Regional and localization errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0004\u0007\b\t\nB\u001b\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u0082\u0001\u0004\u000b\f\r\u000e\u00a8\u0006\u000f"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError;", "Lcom/rio/rostry/core/common/exceptions/SyncException;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "InvalidLocation", "LocalizationFailed", "RegionMismatch", "UnsupportedRegion", "Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError$InvalidLocation;", "Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError$LocalizationFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError$RegionMismatch;", "Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError$UnsupportedRegion;", "common_debug"})
    public static abstract class RegionalError extends com.rio.rostry.core.common.exceptions.SyncException {
        
        private RegionalError(java.lang.String message, java.lang.Throwable cause) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError$InvalidLocation;", "Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError;", "latitude", "", "longitude", "(DD)V", "common_debug"})
        public static final class InvalidLocation extends com.rio.rostry.core.common.exceptions.SyncException.RegionalError {
            
            public InvalidLocation(double latitude, double longitude) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError$LocalizationFailed;", "Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError;", "language", "", "key", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class LocalizationFailed extends com.rio.rostry.core.common.exceptions.SyncException.RegionalError {
            
            public LocalizationFailed(@org.jetbrains.annotations.NotNull()
            java.lang.String language, @org.jetbrains.annotations.NotNull()
            java.lang.String key) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError$RegionMismatch;", "Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError;", "expectedRegion", "", "actualRegion", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class RegionMismatch extends com.rio.rostry.core.common.exceptions.SyncException.RegionalError {
            
            public RegionMismatch(@org.jetbrains.annotations.NotNull()
            java.lang.String expectedRegion, @org.jetbrains.annotations.NotNull()
            java.lang.String actualRegion) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError$UnsupportedRegion;", "Lcom/rio/rostry/core/common/exceptions/SyncException$RegionalError;", "region", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class UnsupportedRegion extends com.rio.rostry.core.common.exceptions.SyncException.RegionalError {
            
            public UnsupportedRegion(@org.jetbrains.annotations.NotNull()
            java.lang.String region) {
            }
        }
    }
    
    /**
     * Sync operation errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0005\u0007\b\t\n\u000bB\u001b\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u0082\u0001\u0005\f\r\u000e\u000f\u0010\u00a8\u0006\u0011"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError;", "Lcom/rio/rostry/core/common/exceptions/SyncException;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "AlreadyInProgress", "DependencyNotMet", "InvalidOperation", "MaxRetriesExceeded", "QueueFull", "Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError$AlreadyInProgress;", "Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError$DependencyNotMet;", "Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError$InvalidOperation;", "Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError$MaxRetriesExceeded;", "Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError$QueueFull;", "common_debug"})
    public static abstract class SyncOperationError extends com.rio.rostry.core.common.exceptions.SyncException {
        
        private SyncOperationError(java.lang.String message, java.lang.Throwable cause) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError$AlreadyInProgress;", "Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError;", "operation", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class AlreadyInProgress extends com.rio.rostry.core.common.exceptions.SyncException.SyncOperationError {
            
            public AlreadyInProgress(@org.jetbrains.annotations.NotNull()
            java.lang.String operation) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError$DependencyNotMet;", "Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError;", "actionId", "", "dependencyId", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class DependencyNotMet extends com.rio.rostry.core.common.exceptions.SyncException.SyncOperationError {
            
            public DependencyNotMet(@org.jetbrains.annotations.NotNull()
            java.lang.String actionId, @org.jetbrains.annotations.NotNull()
            java.lang.String dependencyId) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError$InvalidOperation;", "Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError;", "operation", "", "reason", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class InvalidOperation extends com.rio.rostry.core.common.exceptions.SyncException.SyncOperationError {
            
            public InvalidOperation(@org.jetbrains.annotations.NotNull()
            java.lang.String operation, @org.jetbrains.annotations.NotNull()
            java.lang.String reason) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError$MaxRetriesExceeded;", "Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError;", "actionId", "", "retryCount", "", "(Ljava/lang/String;I)V", "common_debug"})
        public static final class MaxRetriesExceeded extends com.rio.rostry.core.common.exceptions.SyncException.SyncOperationError {
            
            public MaxRetriesExceeded(@org.jetbrains.annotations.NotNull()
            java.lang.String actionId, int retryCount) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError$QueueFull;", "Lcom/rio/rostry/core/common/exceptions/SyncException$SyncOperationError;", "message", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class QueueFull extends com.rio.rostry.core.common.exceptions.SyncException.SyncOperationError {
            
            public QueueFull(@org.jetbrains.annotations.NotNull()
            java.lang.String message) {
            }
            
            public QueueFull() {
            }
        }
    }
    
    /**
     * Unknown or unexpected errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0019\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$UnknownError;", "Lcom/rio/rostry/core/common/exceptions/SyncException;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "common_debug"})
    public static final class UnknownError extends com.rio.rostry.core.common.exceptions.SyncException {
        
        public UnknownError(@org.jetbrains.annotations.NotNull()
        java.lang.String message, @org.jetbrains.annotations.Nullable()
        java.lang.Throwable cause) {
        }
    }
    
    /**
     * Data validation errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0005\u0007\b\t\n\u000bB\u001b\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006\u0082\u0001\u0005\f\r\u000e\u000f\u0010\u00a8\u0006\u0011"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError;", "Lcom/rio/rostry/core/common/exceptions/SyncException;", "message", "", "cause", "", "(Ljava/lang/String;Ljava/lang/Throwable;)V", "BusinessRuleViolation", "InvalidData", "InvalidFormat", "MissingRequiredField", "ValueOutOfRange", "Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError$BusinessRuleViolation;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError$InvalidData;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError$InvalidFormat;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError$MissingRequiredField;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError$ValueOutOfRange;", "common_debug"})
    public static abstract class ValidationError extends com.rio.rostry.core.common.exceptions.SyncException {
        
        private ValidationError(java.lang.String message, java.lang.Throwable cause) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError$BusinessRuleViolation;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError;", "rule", "", "details", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class BusinessRuleViolation extends com.rio.rostry.core.common.exceptions.SyncException.ValidationError {
            
            public BusinessRuleViolation(@org.jetbrains.annotations.NotNull()
            java.lang.String rule, @org.jetbrains.annotations.NotNull()
            java.lang.String details) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError$InvalidData;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError;", "field", "", "value", "reason", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class InvalidData extends com.rio.rostry.core.common.exceptions.SyncException.ValidationError {
            
            public InvalidData(@org.jetbrains.annotations.NotNull()
            java.lang.String field, @org.jetbrains.annotations.Nullable()
            java.lang.String value, @org.jetbrains.annotations.NotNull()
            java.lang.String reason) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError$InvalidFormat;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError;", "field", "", "expectedFormat", "(Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class InvalidFormat extends com.rio.rostry.core.common.exceptions.SyncException.ValidationError {
            
            public InvalidFormat(@org.jetbrains.annotations.NotNull()
            java.lang.String field, @org.jetbrains.annotations.NotNull()
            java.lang.String expectedFormat) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError$MissingRequiredField;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError;", "field", "", "(Ljava/lang/String;)V", "common_debug"})
        public static final class MissingRequiredField extends com.rio.rostry.core.common.exceptions.SyncException.ValidationError {
            
            public MissingRequiredField(@org.jetbrains.annotations.NotNull()
            java.lang.String field) {
            }
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\u0018\u00002\u00020\u0001B)\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u0007\u00a8\u0006\b"}, d2 = {"Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError$ValueOutOfRange;", "Lcom/rio/rostry/core/common/exceptions/SyncException$ValidationError;", "field", "", "value", "min", "max", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "common_debug"})
        public static final class ValueOutOfRange extends com.rio.rostry.core.common.exceptions.SyncException.ValidationError {
            
            public ValueOutOfRange(@org.jetbrains.annotations.NotNull()
            java.lang.String field, @org.jetbrains.annotations.NotNull()
            java.lang.String value, @org.jetbrains.annotations.Nullable()
            java.lang.String min, @org.jetbrains.annotations.Nullable()
            java.lang.String max) {
            }
        }
    }
}