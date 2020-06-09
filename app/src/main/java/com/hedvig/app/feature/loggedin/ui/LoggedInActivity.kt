package com.hedvig.app.feature.loggedin.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.view.isEmpty
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.firebase.iid.FirebaseInstanceId
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.Feature
import com.hedvig.app.BaseActivity
import com.hedvig.app.BuildConfig
import com.hedvig.app.LoggedInTerminatedActivity
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.dashboard.ui.DashboardViewModel
import com.hedvig.app.feature.profile.service.ProfileTracker
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.referrals.ui.ReferralsInformationActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.welcome.WelcomeDialog
import com.hedvig.app.feature.welcome.WelcomeViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewDialog
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.isDebug
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.toWebLocaleTag
import com.hedvig.app.util.extensions.monthlyCostDeductionIncentive
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.showShareSheet
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.safeLet
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import kotlinx.android.synthetic.main.activity_logged_in.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class LoggedInActivity : BaseActivity(R.layout.activity_logged_in) {
    private val claimsViewModel: ClaimsViewModel by viewModel()
    private val tabViewModel: BaseTabViewModel by viewModel()
    private val whatsNewViewModel: WhatsNewViewModel by viewModel()
    private val profileViewModel: ProfileViewModel by viewModel()
    private val welcomeViewModel: WelcomeViewModel by viewModel()
    private val dashboardViewModel: DashboardViewModel by viewModel()

    private val loggedInViewModel: LoggedInViewModel by viewModel()

    private val profileTracker: ProfileTracker by inject()
    private val loggedInTracker: LoggedInTracker by inject()

    private var lastLoggedInTab = LoggedInTabs.DASHBOARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loggedInRoot.setEdgeToEdgeSystemUiFlags(true)
        hedvigToolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }

        bottomTabs.doOnApplyWindowInsets { view, insets, initialState ->
            view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
        }

        setSupportActionBar(hedvigToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        loggedInViewModel.scroll.observe(this) { elevation ->
            elevation?.let { hedvigToolbar.elevation = it }
        }

        tabContentContainer.adapter = TabPagerAdapter(supportFragmentManager)
        bottomTabs.setOnNavigationItemSelectedListener { menuItem ->
            val id = LoggedInTabs.fromId(menuItem.itemId)
            if (id == null) {
                e { "Programmer error: Invalid menu item chosen" }
                return@setOnNavigationItemSelectedListener false
            }
            tabContentContainer.setCurrentItem(id.ordinal, false)
            setupToolBar(id)
            setupFloatingButton(id)
            true
        }

        if (intent.getBooleanExtra(EXTRA_IS_FROM_REFERRALS_NOTIFICATION, false)) {
            bottomTabs.selectedItemId = R.id.referrals
            intent.removeExtra(EXTRA_IS_FROM_REFERRALS_NOTIFICATION)
        }

        if (intent.getBooleanExtra(EXTRA_IS_FROM_ONBOARDING, false)) {
            welcomeViewModel.fetch()
            welcomeViewModel.data.observe(lifecycleOwner = this) { data ->
                if (data != null) {
                    WelcomeDialog.newInstance(data).show(supportFragmentManager, WelcomeDialog.TAG)
                    loggedInRoot.postDelayed({
                        loggedInRoot.show()
                    }, resources.getInteger(R.integer.slide_in_animation_duration).toLong())
                }
            }
            intent.removeExtra(EXTRA_IS_FROM_ONBOARDING)
        }

        bindData()
        setupToolBar(LoggedInTabs.fromId(bottomTabs.selectedItemId))
    }

    private fun setupFloatingButton(id: LoggedInTabs) = when (id) {
        LoggedInTabs.REFERRALS -> referralButton.show()
        else -> referralButton.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (LoggedInTabs.fromId(bottomTabs.selectedItemId)) {
            LoggedInTabs.DASHBOARD,
            LoggedInTabs.KEY_GEAR,
            LoggedInTabs.CLAIMS -> {
                menuInflater.inflate(R.menu.base_tab_menu, menu)
            }
            LoggedInTabs.PROFILE -> {
                menuInflater.inflate(R.menu.profile_settings_menu, menu)
            }
            LoggedInTabs.REFERRALS -> {
                menuInflater.inflate(R.menu.referral_more_info_menu, menu)
            }
            else -> {
                menuInflater.inflate(R.menu.base_tab_menu, menu)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (LoggedInTabs.fromId(bottomTabs.selectedItemId)) {
            LoggedInTabs.DASHBOARD,
            LoggedInTabs.KEY_GEAR,
            LoggedInTabs.CLAIMS -> {
                claimsViewModel.triggerFreeTextChat {
                    startClosableChat()
                }
            }
            LoggedInTabs.PROFILE -> {
                startActivity(SettingsActivity.newInstance(this))
            }
            LoggedInTabs.REFERRALS -> {
                startActivity(
                    ReferralsInformationActivity.newInstance(
                        this,
                        "https://www.hedvig.com/invites/terms" // TODO: Replace with a link provided by content-service
                    )
                )
            }
        }
        return true
    }

    private fun bindData() {
        var badge: View? = null

        tabViewModel.tabNotification.observe(lifecycleOwner = this) { tab ->
            if (tab == null) {
                badge?.findViewById<ImageView>(R.id.notificationIcon)?.remove()
            } else {
                when (tab) {
                    TabNotification.REFERRALS -> {
                        val position = when (bottomTabs.menu.size()) {
                            4 -> 3
                            5 -> 4
                            else -> 3
                        }
                        val itemView =
                            (bottomTabs.getChildAt(0) as BottomNavigationMenuView).getChildAt(
                                position
                            ) as BottomNavigationItemView

                        badge = layoutInflater.inflate(
                            R.layout.bottom_navigation_notification,
                            itemView,
                            true
                        )
                    }
                }
            }
        }

        whatsNewViewModel.news.observe(lifecycleOwner = this) { data ->
            data?.let {
                if (data.news.isNotEmpty()) {
                    // Yep, this is actually happening
                    GlobalScope.launch(Dispatchers.IO) {
                        FirebaseInstanceId.getInstance().deleteInstanceId()
                    }
                    WhatsNewDialog.newInstance(data.news)
                        .show(supportFragmentManager, WhatsNewDialog.TAG)
                }
            }
        }

        loggedInViewModel.data.observe(this) { features ->
            features?.let { f ->
                if (bottomTabs.menu.isEmpty()) {
                    val keyGearEnabled = isDebug() || f.contains(Feature.KEYGEAR)
                    val referralsEnabled = isDebug() || f.contains(Feature.REFERRALS)

                    val menuId = when {
                        keyGearEnabled && referralsEnabled -> R.menu.logged_in_menu_key_gear
                        referralsEnabled -> R.menu.logged_in_menu
                        !keyGearEnabled && !referralsEnabled -> R.menu.logged_in_menu_no_referrals
                        else -> R.menu.logged_in_menu
                    }
                    bottomTabs.inflateMenu(menuId)
                    val initialTab = intent.extras?.getSerializable(INITIAL_TAB) as? LoggedInTabs
                        ?: LoggedInTabs.DASHBOARD
                    bottomTabs.selectedItemId = initialTab.id()
                    setupToolBar(LoggedInTabs.fromId(bottomTabs.selectedItemId))
                    loggedInRoot.show()
                }
            }
        }

        profileViewModel.data.observe(lifecycleOwner = this) { data ->
            safeLet(
                data?.referralInformation?.campaign?.monthlyCostDeductionIncentive()?.amount?.amount?.toBigDecimal()
                    ?.toDouble(),
                data?.referralInformation?.campaign?.code
            ) { incentive, code -> bindReferralsButton(incentive, code) }

            data?.member?.id?.let { id ->
                loggedInTracker.setMemberId(id)
            }

        }
        whatsNewViewModel.fetchNews()

        dashboardViewModel.data.observe(lifecycleOwner = this) { data ->
            data?.first?.let { d ->
                if (isTerminated(d.contracts)) {
                    startActivity(LoggedInTerminatedActivity.newInstance(this))
                }
            }
        }
    }

    private fun bindReferralsButton(incentive: Double, code: String) {
        referralButton.setHapticClickListener {
            profileTracker.clickReferral(incentive.toInt())
            showShareSheet(R.string.REFERRALS_SHARE_SHEET_TITLE) { intent ->
                intent.apply {
                    putExtra(
                        Intent.EXTRA_TEXT,
                        resources.getString(
                            R.string.REFERRAL_SMS_MESSAGE,
                            incentive.toBigDecimal().toInt().toString(),
                            "${BuildConfig.WEB_BASE_URL}${defaultLocale(this@LoggedInActivity).toWebLocaleTag()}/forever/${code}"
                        )
                    )
                    type = "text/plain"
                }
            }
        }
    }

    private fun setupToolBar(id: LoggedInTabs?) {
        if (lastLoggedInTab != id) {
            hedvigToolbar.elevation = 0f
            invalidateOptionsMenu()
        }
        if (id != null) {
            lastLoggedInTab = id
        }
    }

    companion object {
        private const val INITIAL_TAB = "INITIAL_TAB"
        fun newInstance(
            context: Context,
            withoutHistory: Boolean = false,
            initialTab: LoggedInTabs = LoggedInTabs.DASHBOARD
        ) =
            Intent(context, LoggedInActivity::class.java).apply {
                if (withoutHistory) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                putExtra(INITIAL_TAB, initialTab)
            }

        fun isTerminated(contracts: List<DashboardQuery.Contract>) =
            contracts.isNotEmpty() && contracts.all { it.status.fragments.contractStatusFragment.asTerminatedStatus != null }

        const val EXTRA_IS_FROM_REFERRALS_NOTIFICATION = "extra_is_from_referrals_notification"
        const val EXTRA_IS_FROM_ONBOARDING = "extra_is_from_onboarding"
    }
}
