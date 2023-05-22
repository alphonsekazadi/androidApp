package com.hedvig.android.feature.travelcertificate.navigation

import com.hedvig.android.feature.travelcertificate.CoInsured
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface Destinations : Destination {
  @Serializable
  object GenerateTravelCertificate : Destinations
}

internal sealed interface GenerateTravelCertificateDestination : Destination {

  @Serializable
  data class TravelCertificateInput(
    val email: String?
  ) : GenerateTravelCertificateDestination

  @Serializable
  data class AddCoInsured(
    val coInsured: CoInsured?
  ) : GenerateTravelCertificateDestination
}
