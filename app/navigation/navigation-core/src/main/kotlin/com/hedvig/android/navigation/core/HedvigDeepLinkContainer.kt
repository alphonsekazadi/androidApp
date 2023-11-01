package com.hedvig.android.navigation.core

import com.hedvig.android.core.buildconstants.HedvigBuildConstants

interface HedvigDeepLinkContainer {
  val home: String // Home destination, the start destination of the app
  val insurances: String // The insurances destination, which also shows cross sells
  val forever: String // The forever/referrals destination, showing the existing discount and the unique code
  val profile: String // The profile screen, which acts as a gateway to several app settings
  val eurobonus: String // The destination allowing to edit your current Eurobonus (SAS) number
  val chat: String // Hedvig Chat
  val connectPayment: String // Screen where the member can connect their payment method to Hedvig to pay for insurance
  val directDebit: String // Same as connectPayment but to support an old link to it
}

internal class HedvigDeepLinkContainerImpl(
  hedvigBuildConstants: HedvigBuildConstants,
) : HedvigDeepLinkContainer {
  private val baseDeepLinkDomain = "https://${hedvigBuildConstants.deepLinkHost}"

  // Home does not have some special text, acts as the fallback to all unknown deep links
  override val home: String = baseDeepLinkDomain
  override val insurances: String = "$baseDeepLinkDomain/insurances"
  override val forever: String = "$baseDeepLinkDomain/forever"
  override val profile: String = "$baseDeepLinkDomain/profile"
  override val eurobonus: String = "$baseDeepLinkDomain/eurobonus"
  override val chat: String = "$baseDeepLinkDomain/chat"
  override val connectPayment: String = "$baseDeepLinkDomain/connect-payment"
  override val directDebit: String = "$baseDeepLinkDomain/direct-debit"
}
