package com.hedvig.app.service

import android.content.Context
import android.content.Intent
import com.hedvig.android.app.navigation.TopLevelDestination
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.marketing.MarketingActivity
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.app.feature.referrals.ReferralsReceiverActivity

suspend inline fun DynamicLink.startActivity(
  context: Context,
  marketManager: MarketManager,
  featureManager: FeatureManager,
  onDefault: () -> Unit,
) {
  when (this) {
    DynamicLink.DirectDebit -> {
      val market = marketManager.market ?: return onDefault()
      context.startActivities(
        arrayOf(
          Intent(context, LoggedInActivity::class.java),
          connectPayinIntent(
            context,
            featureManager.getPaymentType(),
            market,
            false,
          ),
        ),
      )
    }
    DynamicLink.Forever -> context.startActivity(
      LoggedInActivity.newInstance(
        context,
        initialTab = TopLevelDestination.REFERRALS,
      ),
    )
    DynamicLink.Insurance -> context.startActivity(
      LoggedInActivity.newInstance(
        context,
        initialTab = TopLevelDestination.INSURANCE,
      ),
    )
    is DynamicLink.Referrals -> {
      when (marketManager.market) {
        Market.SE -> {
          context.startActivity(
            ReferralsReceiverActivity.newInstance(
              context,
              code,
              incentive,
            ),
            null,
          )
        }
        else -> context.startActivity(MarketingActivity.newInstance(context))
      }
    }
    DynamicLink.None -> onDefault()
    DynamicLink.Unknown -> onDefault()
  }
}
