package com.hedvig.android.data.forever

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import giraffe.RedeemReferralCodeMutation
import giraffe.ReferralsQuery

interface ForeverRepository {
  suspend fun getReferralsData(): Either<ErrorMessage, ReferralsQuery.Data>
  suspend fun updateCode(newCode: String): Either<ForeverRepositoryImpl.ReferralError, String>
  suspend fun redeemReferralCode(campaignCode: CampaignCode): Either<ErrorMessage, RedeemReferralCodeMutation.Data?>
}
