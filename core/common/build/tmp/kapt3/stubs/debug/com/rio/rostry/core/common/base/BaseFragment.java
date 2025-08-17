package com.rio.rostry.core.common.base;

/**
 * Base fragment class with common functionality
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\b&\u0018\u0000*\b\b\u0000\u0010\u0001*\u00020\u0002*\b\b\u0001\u0010\u0003*\u00020\u00042\u00020\u0005B\u0005\u00a2\u0006\u0002\u0010\u0006J\u001f\u0010\u000f\u001a\u00028\u00002\u0006\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H&\u00a2\u0006\u0002\u0010\u0014J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018H\u0014J\b\u0010\u0019\u001a\u00020\u0016H\u0014J\u0016\u0010\u001a\u001a\u00020\u00162\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00160\u001cH\u0004J\b\u0010\u001d\u001a\u00020\u0016H&J&\u0010\u001e\u001a\u0004\u0018\u00010\u001f2\u0006\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u00132\b\u0010 \u001a\u0004\u0018\u00010!H\u0016J\b\u0010\"\u001a\u00020\u0016H\u0016J\u001a\u0010#\u001a\u00020\u00162\u0006\u0010$\u001a\u00020\u001f2\b\u0010 \u001a\u0004\u0018\u00010!H\u0016J\b\u0010%\u001a\u00020\u0016H&J\u0010\u0010&\u001a\u00020\u00162\u0006\u0010\'\u001a\u00020(H\u0004J\b\u0010)\u001a\u00020\u0016H\u0014J\u0010\u0010*\u001a\u00020\u00162\u0006\u0010\'\u001a\u00020(H\u0004JA\u0010+\u001a\u00020\u0016\"\u0004\b\u0002\u0010,*\b\u0012\u0004\u0012\u0002H,0-2\"\u0010\u001b\u001a\u001e\b\u0001\u0012\u0004\u0012\u0002H,\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160/\u0012\u0006\u0012\u0004\u0018\u0001000.H\u0004\u00a2\u0006\u0002\u00101R\u0012\u0010\u0007\u001a\u0004\u0018\u00018\u0000X\u0082\u000e\u00a2\u0006\u0004\n\u0002\u0010\bR\u0014\u0010\t\u001a\u00028\u00008DX\u0084\u0004\u00a2\u0006\u0006\u001a\u0004\b\n\u0010\u000bR\u0012\u0010\f\u001a\u00028\u0001X\u00a4\u0004\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\u000e\u00a8\u00062"}, d2 = {"Lcom/rio/rostry/core/common/base/BaseFragment;", "VB", "Landroidx/viewbinding/ViewBinding;", "VM", "Landroidx/lifecycle/ViewModel;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Landroidx/viewbinding/ViewBinding;", "binding", "getBinding", "()Landroidx/viewbinding/ViewBinding;", "viewModel", "getViewModel", "()Landroidx/lifecycle/ViewModel;", "getViewBinding", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;)Landroidx/viewbinding/ViewBinding;", "handleError", "", "exception", "", "hideLoading", "navigateSafely", "action", "Lkotlin/Function0;", "observeViewModel", "onCreateView", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "setupUI", "showErrorMessage", "message", "", "showLoading", "showSuccessMessage", "collectInLifecycle", "T", "Lkotlinx/coroutines/flow/Flow;", "Lkotlin/Function2;", "Lkotlin/coroutines/Continuation;", "", "(Lkotlinx/coroutines/flow/Flow;Lkotlin/jvm/functions/Function2;)V", "common_debug"})
public abstract class BaseFragment<VB extends androidx.viewbinding.ViewBinding, VM extends androidx.lifecycle.ViewModel> extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private VB _binding;
    
    public BaseFragment() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final VB getBinding() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected abstract VM getViewModel();
    
    @org.jetbrains.annotations.NotNull()
    public abstract VB getViewBinding(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container);
    
    public abstract void setupUI();
    
    public abstract void observeViewModel();
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    protected final void showErrorMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    protected final void showSuccessMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    protected void showLoading() {
    }
    
    protected void hideLoading() {
    }
    
    protected void handleError(@org.jetbrains.annotations.NotNull()
    java.lang.Throwable exception) {
    }
    
    /**
     * Helper function to collect flows safely
     */
    protected final <T extends java.lang.Object>void collectInLifecycle(@org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.flow.Flow<? extends T> $this$collectInLifecycle, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super T, ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> action) {
    }
    
    /**
     * Navigate safely with error handling
     */
    protected final void navigateSafely(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> action) {
    }
}