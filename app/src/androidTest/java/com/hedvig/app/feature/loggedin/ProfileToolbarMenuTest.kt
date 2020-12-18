package com.hedvig.app.feature.loggedin

import androidx.test.espresso.intent.rule.IntentsTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.TriggerClaimChatMutation
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ProfileToolbarMenuTest : TestCase() {
    @get:Rule
    val activityRule = IntentsTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        TriggerClaimChatMutation.QUERY_DOCUMENT to apolloResponse {
            success(TriggerClaimChatMutation.Data(true))
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldOpenChatWhenClickingToolbarActionOnProfileTab() = run {
        activityRule.launchActivity(LoggedInActivity.newInstance(context()))

        onScreen<LoggedInScreen> {
            chat { stub() }
            root { isVisible() }
            bottomTabs { setSelectedItem(R.id.profile) }
            openChat {
                isVisible()
                click()
            }
            chat { intended() }
        }
    }
}
