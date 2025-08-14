package com.rio.rostry.core.common.compression;

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
public final class DataCompressionManager_Factory implements Factory<DataCompressionManager> {
  @Override
  public DataCompressionManager get() {
    return newInstance();
  }

  public static DataCompressionManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DataCompressionManager newInstance() {
    return new DataCompressionManager();
  }

  private static final class InstanceHolder {
    private static final DataCompressionManager_Factory INSTANCE = new DataCompressionManager_Factory();
  }
}
