package com.hedvig.app.feature.insurance.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.animation.animateContentHeight
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.plus
import com.hedvig.android.core.ui.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.feature.insurance.ui.bindTo
import com.hedvig.app.feature.insurance.ui.detail.coverage.ContractCoverage
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageTab
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageViewModel
import com.hedvig.app.feature.insurance.ui.detail.coverage.InsurableLimitsBottomSheet
import com.hedvig.app.feature.insurance.ui.detail.documents.DocumentsTab
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoTab
import hedvig.resources.R
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ContractDetailActivity : AppCompatActivity() {
  private val contractId: String
    get() = intent.getStringExtra(ID) ?: error("Programmer error: ID not provided to ${this.javaClass.name}")
  private val viewModel: ContractDetailViewModel by viewModel { parametersOf(contractId) }
  private val coverageViewModel: CoverageViewModel by viewModel { parametersOf(contractId) }
  private val imageLoader: ImageLoader by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent {
      HedvigTheme(useNewColorScheme = true) {
        Surface(
          color = MaterialTheme.colorScheme.background,
          modifier = Modifier.fillMaxSize(),
        ) {
          ContractDetailDestination(
            viewModel = viewModel,
            coverageViewModel = coverageViewModel,
            imageLoader = imageLoader,
            navigateUp = onBackPressedDispatcher::onBackPressed,
            onInsurableLimitClick = { insurableLimit: ContractCoverage.InsurableLimit ->
              InsurableLimitsBottomSheet
                .newInstance(insurableLimit.label, insurableLimit.description)
                .show(supportFragmentManager, InsurableLimitsBottomSheet.TAG)
            },
          )
        }
      }
    }
  }

  companion object {
    private const val ID = "ID"
    fun newInstance(context: Context, id: String) =
      Intent(context, ContractDetailActivity::class.java).apply {
        putExtra(ID, id)
      }
  }
}

@Composable
internal fun ContractDetailDestination(
  viewModel: ContractDetailViewModel,
  coverageViewModel: CoverageViewModel,
  imageLoader: ImageLoader,
  navigateUp: () -> Unit,
  onInsurableLimitClick: (ContractCoverage.InsurableLimit) -> Unit,
) {
  val uiState: ContractDetailViewModel.ViewState by viewModel.viewState.collectAsStateWithLifecycle()
  ContractDetailScreen(
    uiState = uiState,
    imageLoader = imageLoader,
    retry = viewModel::retryLoadingContract,
    navigateUp = navigateUp,
    tab1 = { YourInfoTab(viewModel) },
    tab2 = { CoverageTab(coverageViewModel, onInsurableLimitClick) },
    tab3 = { DocumentsTab(viewModel) },
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContractDetailScreen(
  uiState: ContractDetailViewModel.ViewState,
  imageLoader: ImageLoader,
  retry: () -> Unit,
  navigateUp: () -> Unit,
  tab1: @Composable () -> Unit,
  tab2: @Composable () -> Unit,
  tab3: @Composable () -> Unit,
) {
  Column {
    TopAppBarWithBack(
      title = "",
      onClick = navigateUp,
    )
    val pagerState = rememberPagerState()
    Box(Modifier.weight(1f)) {
      when (uiState) {
        ContractDetailViewModel.ViewState.Error -> {
          HedvigErrorSection(retry = retry)
        }
        ContractDetailViewModel.ViewState.Loading -> {}
        is ContractDetailViewModel.ViewState.Success -> {
          LazyColumn(
            contentPadding = WindowInsets
              .safeDrawing
              .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
              .asPaddingValues()
              .plus(PaddingValues(top = 16.dp)),
            modifier = Modifier.consumeWindowInsets(
              WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
          ) {
            item {
              AndroidViewBinding(
                factory = InsuranceContractCardBinding::inflate,
              ) {
                val contract = uiState.state.contractCardViewState
                contract.bindTo(this, imageLoader)
              }
            }
            item { Spacer(Modifier.height(16.dp)) }
            stickyHeader { PagerSelector(pagerState) }
            item {
              HorizontalPager(
                pageCount = 3,
                state = pagerState,
                key = { it },
                verticalAlignment = Alignment.Top,
                modifier = Modifier.animateContentHeight(spring(stiffness = Spring.StiffnessLow)),
              ) { pageIndex ->
                when (pageIndex) {
                  0 -> tab1()
                  1 -> tab2()
                  2 -> tab3()
                  else -> {}
                }
              }
            }
          }
        }
      }
      HedvigFullScreenCenterAlignedProgress(show = uiState is ContractDetailViewModel.ViewState.Loading)
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagerSelector(pagerState: PagerState) {
  LocalConfiguration.current
  val resources = LocalContext.current.resources
  val couroutineScope = rememberCoroutineScope()
  TabRow(
    selectedTabIndex = pagerState.currentPage,
    containerColor = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier.fillMaxWidth(),
  ) {
    remember {
      listOf(
        resources.getString(R.string.insurance_details_view_tab_1_title),
        resources.getString(R.string.insurance_details_view_tab_2_title),
        resources.getString(R.string.insurance_details_view_tab_3_title),
      )
    }.mapIndexed { index, tabTitle ->
      Tab(
        selected = pagerState.currentPage == index,
        onClick = {
          couroutineScope.launch {
            pagerState.animateScrollToPage(index)
          }
        },
        text = {
          Text(text = tabTitle, style = MaterialTheme.typography.bodyMedium)
        },
      )
    }
  }
}
