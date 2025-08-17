package com.rio.rostry.core.payment.ui.viewmodels;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\b\u0010\u0010\u001a\u00020\u0011H\u0002J\u0013\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\u0002\u0010\u0015J\u0006\u0010\u0016\u001a\u00020\u0011R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u0017"}, d2 = {"Lcom/rio/rostry/core/payment/ui/viewmodels/PaymentViewModel;", "Landroidx/lifecycle/ViewModel;", "getCoinBalanceUseCase", "Lcom/rio/rostry/core/payment/domain/usecases/GetCoinBalanceUseCase;", "getTransactionHistoryUseCase", "Lcom/rio/rostry/core/payment/domain/usecases/GetTransactionHistoryUseCase;", "purchaseCoinsUseCase", "Lcom/rio/rostry/core/payment/domain/usecases/PurchaseCoinsUseCase;", "(Lcom/rio/rostry/core/payment/domain/usecases/GetCoinBalanceUseCase;Lcom/rio/rostry/core/payment/domain/usecases/GetTransactionHistoryUseCase;Lcom/rio/rostry/core/payment/domain/usecases/PurchaseCoinsUseCase;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/rio/rostry/core/payment/ui/viewmodels/PaymentUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "loadInitialData", "", "purchaseCoins", "coinPackage", "error/NonExistentClass", "(Lerror/NonExistentClass;)V", "resetPurchaseState", "payment_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class PaymentViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.rio.rostry.core.payment.domain.usecases.GetCoinBalanceUseCase getCoinBalanceUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.rio.rostry.core.payment.domain.usecases.GetTransactionHistoryUseCase getTransactionHistoryUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.rio.rostry.core.payment.domain.usecases.PurchaseCoinsUseCase purchaseCoinsUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.rio.rostry.core.payment.ui.viewmodels.PaymentUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.rio.rostry.core.payment.ui.viewmodels.PaymentUiState> uiState = null;
    
    @javax.inject.Inject()
    public PaymentViewModel(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.payment.domain.usecases.GetCoinBalanceUseCase getCoinBalanceUseCase, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.payment.domain.usecases.GetTransactionHistoryUseCase getTransactionHistoryUseCase, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.payment.domain.usecases.PurchaseCoinsUseCase purchaseCoinsUseCase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.rio.rostry.core.payment.ui.viewmodels.PaymentUiState> getUiState() {
        return null;
    }
    
    private final void loadInitialData() {
    }
    
    public final void purchaseCoins(@org.jetbrains.annotations.NotNull()
    error.NonExistentClass coinPackage) {
    }
    
    public final void resetPurchaseState() {
    }
}