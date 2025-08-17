package com.rio.rostry.core.network.di;

import com.google.firebase.storage.FirebaseStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class NetworkModule_ProvideFirebaseStorageFactory implements Factory<FirebaseStorage> {
  @Override
  public FirebaseStorage get() {
    return provideFirebaseStorage();
  }

  public static NetworkModule_ProvideFirebaseStorageFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FirebaseStorage provideFirebaseStorage() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideFirebaseStorage());
  }

  private static final class InstanceHolder {
    private static final NetworkModule_ProvideFirebaseStorageFactory INSTANCE = new NetworkModule_ProvideFirebaseStorageFactory();
  }
}
