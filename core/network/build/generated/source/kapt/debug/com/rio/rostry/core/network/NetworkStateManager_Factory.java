package com.rio.rostry.core.network;

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
public final class NetworkStateManager_Factory implements Factory<NetworkStateManager> {
  private final Provider<Context> contextProvider;

  public NetworkStateManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NetworkStateManager get() {
    return newInstance(contextProvider.get());
  }

  public static NetworkStateManager_Factory create(Provider<Context> contextProvider) {
    return new NetworkStateManager_Factory(contextProvider);
  }

  public static NetworkStateManager newInstance(Context context) {
    return new NetworkStateManager(context);
  }
}
