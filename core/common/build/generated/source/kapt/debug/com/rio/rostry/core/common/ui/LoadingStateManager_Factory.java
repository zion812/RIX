package com.rio.rostry.core.common.ui;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class LoadingStateManager_Factory implements Factory<LoadingStateManager> {
  @Override
  public LoadingStateManager get() {
    return newInstance();
  }

  public static LoadingStateManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LoadingStateManager newInstance() {
    return new LoadingStateManager();
  }

  private static final class InstanceHolder {
    private static final LoadingStateManager_Factory INSTANCE = new LoadingStateManager_Factory();
  }
}
