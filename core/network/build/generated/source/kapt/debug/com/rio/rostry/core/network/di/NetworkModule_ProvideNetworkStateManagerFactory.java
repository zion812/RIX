package com.rio.rostry.core.network.di;

import android.content.Context;
import com.rio.rostry.core.network.NetworkStateManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class NetworkModule_ProvideNetworkStateManagerFactory implements Factory<NetworkStateManager> {
  private final Provider<Context> contextProvider;

  public NetworkModule_ProvideNetworkStateManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NetworkStateManager get() {
    return provideNetworkStateManager(contextProvider.get());
  }

  public static NetworkModule_ProvideNetworkStateManagerFactory create(
      Provider<Context> contextProvider) {
    return new NetworkModule_ProvideNetworkStateManagerFactory(contextProvider);
  }

  public static NetworkStateManager provideNetworkStateManager(Context context) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideNetworkStateManager(context));
  }
}
