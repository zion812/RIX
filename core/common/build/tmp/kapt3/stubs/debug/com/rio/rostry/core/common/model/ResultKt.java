package com.rio.rostry.core.common.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000(\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u001aE\u0010\u0000\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u00012!\u0010\u0003\u001a\u001d\u0012\u0013\u0012\u00110\u0005\u00a2\u0006\f\b\u0006\u0012\b\b\u0007\u0012\u0004\b\b(\b\u0012\u0004\u0012\u00020\t0\u0004H\u0086\b\u00f8\u0001\u0000\u001a0\u0010\n\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u00012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\t0\u000bH\u0086\b\u00f8\u0001\u0000\u001aE\u0010\f\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u00012!\u0010\u0003\u001a\u001d\u0012\u0013\u0012\u0011H\u0002\u00a2\u0006\f\b\u0006\u0012\b\b\u0007\u0012\u0004\b\b(\r\u0012\u0004\u0012\u00020\t0\u0004H\u0086\b\u00f8\u0001\u0000\u0082\u0002\u0007\n\u0005\b\u009920\u0001\u00a8\u0006\u000e"}, d2 = {"onError", "Lcom/rio/rostry/core/common/model/Result;", "T", "action", "Lkotlin/Function1;", "", "Lkotlin/ParameterName;", "name", "exception", "", "onLoading", "Lkotlin/Function0;", "onSuccess", "value", "common_debug"})
public final class ResultKt {
    
    /**
     * ✅ Extension functions for Result
     */
    @org.jetbrains.annotations.NotNull()
    public static final <T extends java.lang.Object>com.rio.rostry.core.common.model.Result<T> onSuccess(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.common.model.Result<? extends T> $this$onSuccess, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super T, kotlin.Unit> action) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final <T extends java.lang.Object>com.rio.rostry.core.common.model.Result<T> onError(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.common.model.Result<? extends T> $this$onError, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Throwable, kotlin.Unit> action) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final <T extends java.lang.Object>com.rio.rostry.core.common.model.Result<T> onLoading(@org.jetbrains.annotations.NotNull()
    com.rio.rostry.core.common.model.Result<? extends T> $this$onLoading, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> action) {
        return null;
    }
}