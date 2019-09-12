package com.hedvig.app

import android.content.Intent
import android.net.Uri
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.offer.OfferActivity
import com.hedvig.app.feature.profile.ui.payment.TrustlyActivity
import com.hedvig.app.feature.referrals.ReferralsReceiverActivity
import com.hedvig.app.service.LoginStatus
import com.hedvig.app.service.LoginStatusService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import timber.log.Timber

class SplashActivity : BaseActivity() {
    private val loggedInService: LoginStatusService by inject()

    override fun onStart() {
        super.onStart()

        disposables += loggedInService
            .getLoginStatus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ navigateToActivity(it) }, { Timber.e(it) })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleFirebaseDynamicLink(intent, null)
    }

    private fun handleFirebaseDynamicLink(intent: Intent, loginStatus: LoginStatus?) {
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
            .addOnSuccessListener { pendingDynamicLinkData ->
                if (pendingDynamicLinkData != null && pendingDynamicLinkData.link != null) {
                    val link = pendingDynamicLinkData.link
                    when (link?.pathSegments?.get(0)) {
                        "referrals" -> handleReferralsDeepLink(link, loginStatus)
                        "direct-debit" -> handleDirectDebitDeepLink(loginStatus)
                        else -> startDefaultActivity(loginStatus)
                    }
                } else {
                    startDefaultActivity(loginStatus)
                }
            }.addOnFailureListener {
                startDefaultActivity(loginStatus)
            }
    }

    private fun handleDirectDebitDeepLink(loginStatus: LoginStatus?) {
        if (loginStatus != LoginStatus.LOGGED_IN) {
            startDefaultActivity(loginStatus)
            return
        }

        startActivities(
            arrayOf(
                Intent(this, LoggedInActivity::class.java),
                Intent(this, TrustlyActivity::class.java)
            )
        )
    }

    private fun handleReferralsDeepLink(link: Uri, loginStatus: LoginStatus?) {
        if (loginStatus != LoginStatus.ONBOARDING) {
            startDefaultActivity(loginStatus)
            return
        }
        link.getQueryParameter("code")?.let { referralCode ->
            startActivity(
                ReferralsReceiverActivity.newInstance(
                    this,
                    referralCode,
                    "10"
                )
            ) //Fixme "10" should not be hard coded

        } ?: startDefaultActivity(loginStatus)
    }

    private fun startDefaultActivity(loginStatus: LoginStatus?) {
        when (loginStatus) {
            LoginStatus.ONBOARDING -> startActivity(Intent(this, MarketingActivity::class.java))
            LoginStatus.IN_OFFER -> startActivity(Intent(this, OfferActivity::class.java))
            LoginStatus.LOGGED_IN -> startActivity(Intent(this, LoggedInActivity::class.java))
            LoginStatus.LOGGED_IN_TERMINATED -> startActivity(
                Intent(
                    this,
                    LoggedInTerminatedActivity::class.java
                )
            )
            else -> {
                disposables += loggedInService
                    .getLoginStatus()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ lst ->
                        startDefaultActivity(lst)
                    }, { Timber.e(it) })
            }
        }
    }

    private fun navigateToActivity(loginStatus: LoginStatus) = when (loginStatus) {
        LoginStatus.ONBOARDING, LoginStatus.LOGGED_IN -> {
            handleFirebaseDynamicLink(intent, loginStatus)
        }
        else -> startDefaultActivity(loginStatus)
    }
}
