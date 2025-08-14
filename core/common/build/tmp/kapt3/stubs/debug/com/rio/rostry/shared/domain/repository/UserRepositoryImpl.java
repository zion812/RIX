package com.rio.rostry.shared.domain.repository;

/**
 * ✅ Basic implementation of UserRepository
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\n\u0010\u0003\u001a\u0004\u0018\u00010\u0004H\u0016J\u0010\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006H\u0016J\b\u0010\b\u001a\u00020\tH\u0016\u00a8\u0006\n"}, d2 = {"Lcom/rio/rostry/shared/domain/repository/UserRepositoryImpl;", "Lcom/rio/rostry/shared/domain/repository/UserRepository;", "()V", "getCurrentUserId", "", "getCurrentUserTier", "Lkotlinx/coroutines/flow/Flow;", "Lcom/rio/rostry/core/common/model/UserTier;", "isUserAuthenticated", "", "common_debug"})
public final class UserRepositoryImpl implements com.rio.rostry.shared.domain.repository.UserRepository {
    
    public UserRepositoryImpl() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.rio.rostry.core.common.model.UserTier> getCurrentUserTier() {
        return null;
    }
    
    @java.lang.Override()
    public boolean isUserAuthenticated() {
        return false;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.String getCurrentUserId() {
        return null;
    }
}