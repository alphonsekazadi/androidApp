package com.hedvig.app.feature.zignsec.usecase

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.NorwegianBankIdAuthMutation
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class StartNorwegianAuthUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(nationalIdentityNumber: String): SimpleSignStartAuthResult =
    when (val response = apolloClient.mutation(NorwegianBankIdAuthMutation(nationalIdentityNumber)).safeQuery()) {
      is QueryResult.Error -> SimpleSignStartAuthResult.Error
      is QueryResult.Success -> {
        val redirectUrl = response.data.norwegianBankIdAuth.redirectUrl
        SimpleSignStartAuthResult.Success(redirectUrl)
      }
    }
}