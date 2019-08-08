package com.hedvig.app.feature.referrals

import android.os.Bundle
import android.view.View
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.BaseTabFragment
import com.hedvig.app.feature.loggedin.ui.BaseTabViewModel
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.ui.decoration.BottomPaddingItemDecoration
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.safeLet
import kotlinx.android.synthetic.main.fragment_new_referral.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class ReferralsFragment : BaseTabFragment() {
    private val profileViewModel: ProfileViewModel by sharedViewModel()

    private val tabViewModel: BaseTabViewModel by sharedViewModel()

    override val layout = R.layout.fragment_new_referral

    override fun onResume() {
        tabViewModel.removeReferralNotification()
        (invites.adapter as? InvitesAdapter)?.startTankAnimation()
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        invites.addItemDecoration(
            BottomPaddingItemDecoration(
                resources.getDimensionPixelSize(R.dimen.referral_extra_bottom_space)
            )
        )

        profileViewModel.data.observe(this) { data ->
            safeLet(
                data?.insurance?.cost?.fragments?.costFragment?.monthlyGross?.amount?.toBigDecimal()?.toInt(),
                data?.referralInformation
            ) { monthlyCost, referralCampaign ->
                bindData(monthlyCost, referralCampaign)
            } ?: Timber.e("No data")
        }
    }

    private fun bindData(monthlyCost: Int, data: ProfileQuery.ReferralInformation) {
        invites.adapter = InvitesAdapter(monthlyCost, data)
    }
}
