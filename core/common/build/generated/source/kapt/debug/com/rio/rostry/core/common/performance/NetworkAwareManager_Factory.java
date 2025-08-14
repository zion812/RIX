package com.rio.rostry.core.common.performance;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class NetworkAwareManager_Factory implements Factory<NetworkAwareManager> {
  private final Provider<Context> contextProvider;

  public NetworkAwareManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NetworkAwareManager get() {
    return newInstance(contextProvider.get());
  }

  public static NetworkAwareManager_Factory create(Provider<Context> contextProvider) {
    return new NetworkAwareManager_Factory(contextProvider);
  }

  public static NetworkAwareManager newInstance(Context context) {
    return new NetworkAwareManager(context);
  }
}
