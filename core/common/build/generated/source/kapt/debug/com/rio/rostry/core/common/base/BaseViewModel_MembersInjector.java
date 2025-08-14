package com.rio.rostry.core.common.base;

import com.rio.rostry.core.common.utils.ErrorHandler;
import com.rio.rostry.shared.domain.repository.UserRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class BaseViewModel_MembersInjector implements MembersInjector<BaseViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<ErrorHandler> errorHandlerProvider;

  public BaseViewModel_MembersInjector(Provider<UserRepository> userRepositoryProvider,
      Provider<ErrorHandler> errorHandlerProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.errorHandlerProvider = errorHandlerProvider;
  }

  public static MembersInjector<BaseViewModel> create(
      Provider<UserRepository> userRepositoryProvider,
      Provider<ErrorHandler> errorHandlerProvider) {
    return new BaseViewModel_MembersInjector(userRepositoryProvider, errorHandlerProvider);
  }

  @Override
  public void injectMembers(BaseViewModel instance) {
    injectUserRepository(instance, userRepositoryProvider.get());
    injectErrorHandler(instance, errorHandlerProvider.get());
  }

  @InjectedFieldSignature("com.rio.rostry.core.common.base.BaseViewModel.userRepository")
  public static void injectUserRepository(BaseViewModel instance, UserRepository userRepository) {
    instance.userRepository = userRepository;
  }

  @InjectedFieldSignature("com.rio.rostry.core.common.base.BaseViewModel.errorHandler")
  public static void injectErrorHandler(BaseViewModel instance, ErrorHandler errorHandler) {
    instance.errorHandler = errorHandler;
  }
}
