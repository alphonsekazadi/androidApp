package com.hedvig.android.auth.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.auth.AndroidAccessTokenProvider
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.AuthTokenServiceImpl
import com.hedvig.android.auth.event.AuthEventBroadcaster
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.auth.interceptor.AuthTokenRefreshingInterceptor
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.ioDispatcherQualifier
import com.hedvig.authlib.AuthRepository
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val authModule = module {
  single<AccessTokenProvider> { AndroidAccessTokenProvider(get()) }
  single<AuthTokenRefreshingInterceptor> { AuthTokenRefreshingInterceptor(get()) }
  single<AuthTokenService> {
    AuthTokenServiceImpl(
      get<AuthTokenStorage>(),
      get<AuthRepository>(),
      get<AuthEventBroadcaster>(),
      get<ApplicationScope>(),
    )
  }
  single<AuthTokenStorage> { AuthTokenStorage(get<DataStore<Preferences>>()) }
  single<AuthEventBroadcaster> {
    AuthEventBroadcaster(
      authEventListeners = getAll<AuthEventListener>().toSet(),
      applicationScope = get(),
      coroutineContext = get(ioDispatcherQualifier),
    )
  }
}
