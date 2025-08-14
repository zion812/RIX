package com.rio.rostry.shared.domain.repository;

/**
 * ✅ Simple user repository interface
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\bf\u0018\u00002\u00020\u0001J\n\u0010\u0002\u001a\u0004\u0018\u00010\u0003H&J\u0010\u0010\u0004\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00060\u0005H&J\b\u0010\u0007\u001a\u00020\bH&\u00a8\u0006\t"}, d2 = {"Lcom/rio/rostry/shared/domain/repository/UserRepository;", "", "getCurrentUserId", "", "getCurrentUserTier", "Lkotlinx/coroutines/flow/Flow;", "Lcom/rio/rostry/core/common/model/UserTier;", "isUserAuthenticated", "", "common_debug"})
public abstract interface UserRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.rio.rostry.core.common.model.UserTier> getCurrentUserTier();
    
    public abstract boolean isUserAuthenticated();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.String getCurrentUserId();
}