package com.hedvig.app.authenticate

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.logger.logcat
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.util.apollo.reconnectSubscriptions
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch

internal class LogoutUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val userRepository: UserRepository,
  private val authTokenService: AuthTokenService,
  private val chatEventStore: ChatEventStore,
  private val hAnalytics: HAnalytics,
  private val applicationScope: ApplicationScope,
  private val demoManager: DemoManager,
) : LogoutUseCase {
  override fun invoke() {
    logcat { "Logout usecase called" }
    applicationScope.launch { hAnalytics.loggedOut() }
    applicationScope.launch { userRepository.logout() }
    applicationScope.launch { authTokenService.logoutAndInvalidateTokens() }
    applicationScope.launch { apolloClient.apolloStore.clearAll() }
    applicationScope.launch { chatEventStore.resetChatClosedCounter() }
    applicationScope.launch { apolloClient.reconnectSubscriptions() }
    applicationScope.launch { demoManager.setDemoMode(false) }
  }
}
