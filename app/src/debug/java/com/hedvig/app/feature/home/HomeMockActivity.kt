package com.hedvig.app.feature.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityGenericDevelopmentBinding
import com.hedvig.app.feature.home.ui.HomeViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.referrals.MockLoggedInViewModel
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.homeModule
import com.hedvig.app.loggedInModule
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE_IN_FUTURE
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE
import com.hedvig.app.testdata.feature.home.HOME_DATA_PENDING
import com.hedvig.app.testdata.feature.home.HOME_DATA_TERMINATED
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class HomeMockActivity : AppCompatActivity(R.layout.activity_generic_development) {
    private val binding by viewBinding(ActivityGenericDevelopmentBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        unloadKoinModules(
            listOf(
                loggedInModule,
                homeModule
            )
        )

        loadKoinModules(MOCK_MODULE)

        binding.root.adapter = genericDevelopmentAdapter {
            header("Tab Screen")
            clickableItem("Pending") {
                MockHomeViewModel.apply {
                    mockData = HOME_DATA_PENDING
                    shouldError = false
                }
                startActivity(LoggedInActivity.newInstance(this@HomeMockActivity))
            }
            clickableItem("Active in Future") {
                MockHomeViewModel.apply {
                    mockData = HOME_DATA_ACTIVE_IN_FUTURE
                    shouldError = false
                }
                startActivity(LoggedInActivity.newInstance(this@HomeMockActivity))
            }
            clickableItem("Active in Future + Active in Future and Terminated in Future") {
                MockHomeViewModel.apply {
                    mockData = HOME_DATA_ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE
                    shouldError = false
                }
                startActivity(LoggedInActivity.newInstance(this@HomeMockActivity))
            }
            clickableItem("Active") {
                MockHomeViewModel.apply {
                    mockData = HOME_DATA_ACTIVE
                    shouldError = false
                }
                startActivity(LoggedInActivity.newInstance(this@HomeMockActivity))
            }
            clickableItem("Terminated") {
                MockHomeViewModel.apply {
                    mockData = HOME_DATA_TERMINATED
                    shouldError = false
                }
                startActivity(LoggedInActivity.newInstance(this@HomeMockActivity))
            }
            clickableItem("Error") {
                MockHomeViewModel.apply {
                    mockData = HOME_DATA_PENDING
                    shouldError = true
                }
                startActivity(LoggedInActivity.newInstance(this@HomeMockActivity))
            }
        }
    }

    companion object {
        private val MOCK_MODULE = module {
            viewModel<LoggedInViewModel> { MockLoggedInViewModel() }
            viewModel<HomeViewModel> { MockHomeViewModel() }
        }
    }
}
