package com.rio.rostry.core.common.base;

/**
 * Base ViewModel class providing common functionality for all ViewModels in the RIO app.
 * Includes error handling, loading states, user tier management, and coroutine utilities.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u009a\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0003\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010$\n\u0002\b\u000b\b&\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002JV\u0010 \u001a\b\u0012\u0004\u0012\u0002H!0\u0013\"\u0004\b\u0000\u0010\"\"\u0004\b\u0001\u0010#\"\u0004\b\u0002\u0010!2\f\u0010$\u001a\b\u0012\u0004\u0012\u0002H\"0\u00132\f\u0010%\u001a\b\u0012\u0004\u0012\u0002H#0\u00132\u0018\u0010&\u001a\u0014\u0012\u0004\u0012\u0002H\"\u0012\u0004\u0012\u0002H#\u0012\u0004\u0012\u0002H!0\'H\u0004J5\u0010(\u001a\u00020)2\b\b\u0002\u0010*\u001a\u00020\u00052\u001c\u0010+\u001a\u0018\b\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u00020)0-\u0012\u0006\u0012\u0004\u0018\u00010.0,H\u0004\u00a2\u0006\u0002\u0010/Jy\u00100\u001a\u00020)\"\u0004\b\u0000\u001012(\u0010+\u001a$\b\u0001\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H103020-\u0012\u0006\u0012\u0004\u0018\u00010.0,2\u0014\b\u0002\u00104\u001a\u000e\u0012\u0004\u0012\u0002H1\u0012\u0004\u0012\u00020)0,2\u0014\b\u0002\u00105\u001a\u000e\u0012\u0004\u0012\u000206\u0012\u0004\u0012\u00020)0,2\u000e\b\u0002\u00107\u001a\b\u0012\u0004\u0012\u00020)08H\u0004\u00a2\u0006\u0002\u00109J?\u0010:\u001a\u00020)2\b\b\u0002\u0010;\u001a\u00020<2\b\b\u0002\u0010=\u001a\u00020>2\u001c\u0010+\u001a\u0018\b\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u00020)0-\u0012\u0006\u0012\u0004\u0018\u00010.0,H\u0004\u00a2\u0006\u0002\u0010?JC\u0010@\u001a\u00020)2\u0006\u0010A\u001a\u00020\u001e2\u001c\u0010+\u001a\u0018\b\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u00020)0-\u0012\u0006\u0012\u0004\u0018\u00010.0,2\u000e\b\u0002\u0010B\u001a\b\u0012\u0004\u0012\u00020)08H\u0004\u00a2\u0006\u0002\u0010CJ\n\u0010D\u001a\u0004\u0018\u00010EH\u0004J\u0010\u0010F\u001a\u00020)2\u0006\u0010G\u001a\u000206H\u0014J\u0010\u0010H\u001a\u00020\u00052\u0006\u0010A\u001a\u00020\u001eH\u0004J\b\u0010I\u001a\u00020\u0005H\u0004J&\u0010J\u001a\u00020)2\u0006\u0010+\u001a\u00020E2\u0014\b\u0002\u0010K\u001a\u000e\u0012\u0004\u0012\u00020E\u0012\u0004\u0012\u00020.0LH\u0004J\b\u0010M\u001a\u00020)H\u0016J\b\u0010N\u001a\u00020)H\u0004J\b\u0010O\u001a\u00020)H\u0004J\u000e\u0010P\u001a\u00020)2\u0006\u0010\u0012\u001a\u00020\u0005J\b\u0010Q\u001a\u00020)H\u0004JB\u0010R\u001a\u00020)\"\u0004\b\u0000\u00101*\b\u0012\u0004\u0012\u0002H1022\u0012\u0010S\u001a\u000e\u0012\u0004\u0012\u0002H1\u0012\u0004\u0012\u00020)0,2\u0014\b\u0002\u00105\u001a\u000e\u0012\u0004\u0012\u000206\u0012\u0004\u0012\u00020)0,H\u0004J(\u0010T\u001a\b\u0012\u0004\u0012\u0002H102\"\u0004\b\u0000\u00101*\b\u0012\u0004\u0012\u0002H1022\b\b\u0002\u0010U\u001a\u00020>H\u0004J8\u0010V\u001a\b\u0012\u0004\u0012\u0002H!0\u0013\"\u0004\b\u0000\u00101\"\u0004\b\u0001\u0010!*\b\u0012\u0004\u0012\u0002H10\u00132\u0012\u0010&\u001a\u000e\u0012\u0004\u0012\u0002H1\u0012\u0004\u0012\u0002H!0,H\u0004R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00070\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\u00020\tX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u001e\u0010\f\u001a\u00020\r8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00050\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0014R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00070\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0014R\u001e\u0010\u0017\u001a\u00020\u00188\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR\u0019\u0010\u001d\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001e0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0014\u00a8\u0006W"}, d2 = {"Lcom/rio/rostry/core/common/base/BaseViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "_isOffline", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_uiState", "Lcom/rio/rostry/core/common/model/state/UiState;", "coroutineExceptionHandler", "Lkotlinx/coroutines/CoroutineExceptionHandler;", "getCoroutineExceptionHandler", "()Lkotlinx/coroutines/CoroutineExceptionHandler;", "errorHandler", "Lcom/rio/rostry/core/common/utils/ErrorHandler;", "getErrorHandler", "()Lcom/rio/rostry/core/common/utils/ErrorHandler;", "setErrorHandler", "(Lcom/rio/rostry/core/common/utils/ErrorHandler;)V", "isOffline", "Lkotlinx/coroutines/flow/StateFlow;", "()Lkotlinx/coroutines/flow/StateFlow;", "uiState", "getUiState", "userRepository", "Lcom/rio/rostry/shared/domain/repository/UserRepository;", "getUserRepository", "()Lcom/rio/rostry/shared/domain/repository/UserRepository;", "setUserRepository", "(Lcom/rio/rostry/shared/domain/repository/UserRepository;)V", "userTier", "Lcom/rio/rostry/core/common/model/UserTier;", "getUserTier", "combineStates", "R", "T1", "T2", "flow1", "flow2", "transform", "Lkotlin/Function2;", "executeWithLoading", "", "showLoading", "action", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "", "(ZLkotlin/jvm/functions/Function1;)V", "executeWithResult", "T", "Lkotlinx/coroutines/flow/Flow;", "Lcom/rio/rostry/core/common/model/Result;", "onSuccess", "onError", "", "onLoading", "Lkotlin/Function0;", "(Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;)V", "executeWithRetry", "maxRetries", "", "delayMillis", "", "(IJLkotlin/jvm/functions/Function1;)V", "executeWithTierCheck", "requiredTier", "onInsufficientTier", "(Lcom/rio/rostry/core/common/model/UserTier;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;)V", "getCurrentUserId", "", "handleError", "exception", "hasRequiredTier", "isUserAuthenticated", "logUserAction", "parameters", "", "refreshData", "setEmptyState", "setLoadingState", "setOfflineState", "setSuccessState", "collectSafely", "onEach", "debounceSearch", "timeoutMillis", "mapState", "common_debug"})
public abstract class BaseViewModel extends androidx.lifecycle.ViewModel {
    @javax.inject.Inject()
    public com.rio.rostry.shared.domain.repository.UserRepository userRepository;
    @javax.inject.Inject()
    public com.rio.rostry.core.common.utils.ErrorHandler errorHandler;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.rio.rostry.core.common.model.state.UiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.rio.rostry.core.common.model.state.UiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.rio.rostry.core.common.model.UserTier> userTier = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isOffline = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isOffline = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineExceptionHandler coroutineExceptionHandler = null;
    
    public BaseViewModel() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.shared.domain.repository.UserRepository getUserRepository() {
        return null;
    }
    
    public final void setUserRepository(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.shared.domain.repository.UserRepository p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.rio.rostry.core.common.utils.ErrorHandler getErrorHandler() {
        return null;
    }
    
    public final void setErrorHandler(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.common.utils.ErrorHandler p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.rio.rostry.core.common.model.state.UiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.rio.rostry.core.common.model.UserTier> getUserTier() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isOffline() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final kotlinx.coroutines.CoroutineExceptionHandler getCoroutineExceptionHandler() {
        return null;
    }
    
    /**
     * Execute a suspend function with proper error handling and loading states
     */
    protected final void executeWithLoading(boolean showLoading, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> action) {
    }
    
    /**
     * Execute a suspend function that returns a Result and handle it appropriately
     */
    protected final <T extends java.lang.Object>void executeWithResult(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<? extends com.rio.rostry.core.common.model.Result<? extends T>>>, ? extends java.lang.Object> action, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super T, kotlin.Unit> onSuccess, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Throwable, kotlin.Unit> onError, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onLoading) {
    }
    
    /**
     * Handle errors with proper categorization and user-friendly messages
     */
    protected void handleError(@org.jetbrains.annotations.NotNull()
    java.lang.Throwable exception) {
    }
    
    /**
     * Set UI state to success
     */
    protected final void setSuccessState() {
    }
    
    /**
     * Set UI state to empty
     */
    protected final void setEmptyState() {
    }
    
    /**
     * Set UI state to loading
     */
    protected final void setLoadingState() {
    }
    
    /**
     * Check if user has required tier permissions
     */
    protected final boolean hasRequiredTier(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.common.model.UserTier requiredTier) {
        return false;
    }
    
    /**
     * Execute action only if user has required tier
     */
    protected final void executeWithTierCheck(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.common.model.UserTier requiredTier, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> action, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onInsufficientTier) {
    }
    
    /**
     * Refresh data - override in child ViewModels
     */
    public void refreshData() {
    }
    
    /**
     * Handle offline state changes
     */
    public final void setOfflineState(boolean isOffline) {
    }
    
    /**
     * Utility function to safely collect Flow with error handling
     */
    protected final <T extends java.lang.Object>void collectSafely(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.Flow<? extends T> $this$collectSafely, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super T, kotlin.Unit> onEach, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Throwable, kotlin.Unit> onError) {
    }
    
    /**
     * Utility function to combine multiple StateFlows
     */
    @org.jetbrains.annotations.NotNull()
    protected final <T1 extends java.lang.Object, T2 extends java.lang.Object, R extends java.lang.Object>kotlinx.coroutines.flow.StateFlow<R> combineStates(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.StateFlow<? extends T1> flow1, @org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.StateFlow<? extends T2> flow2, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super T1, ? super T2, ? extends R> transform) {
        return null;
    }
    
    /**
     * Utility function to map StateFlow with error handling
     */
    @org.jetbrains.annotations.NotNull()
    protected final <T extends java.lang.Object, R extends java.lang.Object>kotlinx.coroutines.flow.StateFlow<R> mapState(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.StateFlow<? extends T> $this$mapState, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super T, ? extends R> transform) {
        return null;
    }
    
    /**
     * Debounce user input for search functionality
     */
    @org.jetbrains.annotations.NotNull()
    protected final <T extends java.lang.Object>kotlinx.coroutines.flow.Flow<T> debounceSearch(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.Flow<? extends T> $this$debounceSearch, long timeoutMillis) {
        return null;
    }
    
    /**
     * Execute action with retry logic
     */
    protected final void executeWithRetry(int maxRetries, long delayMillis, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> action) {
    }
    
    /**
     * Log user action for analytics
     */
    protected final void logUserAction(@org.jetbrains.annotations.NotNull()
    java.lang.String action, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, ? extends java.lang.Object> parameters) {
    }
    
    /**
     * Check if user is authenticated
     */
    protected final boolean isUserAuthenticated() {
        return false;
    }
    
    /**
     * Get current user ID
     */
    @org.jetbrains.annotations.Nullable()
    protected final java.lang.String getCurrentUserId() {
        return null;
    }
}