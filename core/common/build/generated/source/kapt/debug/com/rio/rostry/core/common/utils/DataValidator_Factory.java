package com.rio.rostry.core.common.utils;

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
public final class DataValidator_Factory implements Factory<DataValidator> {
  @Override
  public DataValidator get() {
    return newInstance();
  }

  public static DataValidator_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DataValidator newInstance() {
    return new DataValidator();
  }

  private static final class InstanceHolder {
    private static final DataValidator_Factory INSTANCE = new DataValidator_Factory();
  }
}
