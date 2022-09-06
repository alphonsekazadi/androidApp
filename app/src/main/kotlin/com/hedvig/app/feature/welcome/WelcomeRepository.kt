package com.hedvig.app.feature.welcome

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.WelcomeQuery
import com.hedvig.app.util.LocaleManager

class WelcomeRepository(
  private val apolloClient: ApolloClient,
  private val localeManager: LocaleManager,
) {
  suspend fun fetchWelcomeScreens() = apolloClient
    .query(WelcomeQuery(localeManager.defaultLocale()))
    .execute()
}