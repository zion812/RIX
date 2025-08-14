package com.rio.rostry.core.common.ui;

/**
 * ✅ Multi-step operation helper
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\u0007\n\u0002\b\u0003\u0018\u00002\u00020\u0001B#\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007\u00a2\u0006\u0002\u0010\bJ\u0012\u0010\u000b\u001a\u00020\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0003J\u000e\u0010\u000e\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u0003J\u0010\u0010\u0010\u001a\u00020\f2\b\b\u0002\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u0013\u001a\u00020\f2\u0006\u0010\u0014\u001a\u00020\u0003R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/rio/rostry/core/common/ui/MultiStepOperation;", "", "operationId", "", "loadingStateManager", "Lcom/rio/rostry/core/common/ui/LoadingStateManager;", "steps", "", "(Ljava/lang/String;Lcom/rio/rostry/core/common/ui/LoadingStateManager;Ljava/util/List;)V", "currentStep", "", "complete", "", "successMessage", "fail", "errorMessage", "nextStep", "stepProgress", "", "start", "title", "common_debug"})
public final class MultiStepOperation {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String operationId = null;
    @org.jetbrains.annotations.NotNull()
    private final com.rio.rostry.core.common.ui.LoadingStateManager loadingStateManager = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> steps = null;
    private int currentStep = 0;
    
    public MultiStepOperation(@org.jetbrains.annotations.NotNull()
    java.lang.String operationId, @org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.common.ui.LoadingStateManager loadingStateManager, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> steps) {
        super();
    }
    
    public final void start(@org.jetbrains.annotations.NotNull()
    java.lang.String title) {
    }
    
    public final void nextStep(float stepProgress) {
    }
    
    public final void complete(@org.jetbrains.annotations.Nullable()
    java.lang.String successMessage) {
    }
    
    public final void fail(@org.jetbrains.annotations.NotNull()
    java.lang.String errorMessage) {
    }
}