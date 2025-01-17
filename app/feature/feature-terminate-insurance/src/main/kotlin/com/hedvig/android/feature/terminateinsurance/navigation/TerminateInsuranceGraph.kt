package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.termination.data.TerminatableInsurance
import com.hedvig.android.feature.terminateinsurance.data.toTerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateDestination
import com.hedvig.android.feature.terminateinsurance.step.choose.ChooseInsuranceToTerminateViewModel
import com.hedvig.android.feature.terminateinsurance.step.deletion.InsuranceDeletionDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationdate.TerminationDateViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationfailure.TerminationFailureDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationConfirmationDestination
import com.hedvig.android.feature.terminateinsurance.step.terminationreview.TerminationConfirmationViewModel
import com.hedvig.android.feature.terminateinsurance.step.terminationsuccess.TerminationSuccessDestination
import com.hedvig.android.feature.terminateinsurance.step.unknown.UnknownScreenDestination
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popUpTo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.terminateInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  navigator: Navigator,
  navController: NavController,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  openChat: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
  openPlayStore: () -> Unit,
  navigateToInsurances: (NavOptions) -> Unit,
  closeTerminationFlow: () -> Unit,
) {
  composable<TerminateInsuranceDestination.TerminationSuccess> { backStackEntry ->
    TerminationSuccessDestination(
      terminationDate = terminationDate,
      onSurveyClicked = { openUrl(surveyUrl) },
      onDone = {
        if (!navController.popBackStack()) {
          // In the deep link situation, we want to navigate to Insurances when we're successfully done with this flow
          navigateToInsurances(
            navOptions {
              popUpTo<TerminateInsuranceDestination.TerminationSuccess> { inclusive = true }
            },
          )
        }
      },
    )
  }
  composable<TerminateInsuranceDestination.TerminationFailure> { backStackEntry ->
    TerminationFailureDestination(
      windowSizeClass = windowSizeClass,
      errorMessage = ErrorMessage(message),
      openChat = { openChat(backStackEntry) },
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
  composable<TerminateInsuranceDestination.UnknownScreen> {
    UnknownScreenDestination(
      windowSizeClass = windowSizeClass,
      openPlayStore = openPlayStore,
      navigateUp = navigator::navigateUp,
      navigateBack = navigator::popBackStack,
    )
  }
  navigation<TerminateInsuranceGraphDestination>(
    startDestination = createRoutePattern<TerminateInsuranceDestination.StartStep>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.terminateInsurance },
    ),
  ) {
    composable<TerminateInsuranceDestination.StartStep> { backStackEntry ->
      val terminateInsuranceGraphDestination = navController
        .getRouteFromBackStack<TerminateInsuranceGraphDestination>(backStackEntry)
      val viewModel: ChooseInsuranceToTerminateViewModel = koinViewModel {
        parametersOf(terminateInsuranceGraphDestination.insuranceId)
      }
      ChooseInsuranceToTerminateDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        openChat = { openChat(backStackEntry) },
        closeTerminationFlow = closeTerminationFlow,
        navigateToNextStep = { step, insuranceForCancellation: TerminatableInsurance ->
          navigator.navigateToTerminateFlowDestination(
            destination = step.toTerminateInsuranceDestination(
              insuranceForCancellation.displayName,
              insuranceForCancellation.contractExposure,
              insuranceForCancellation.activateFrom,
              insuranceForCancellation.contractGroup,
              /**
               * Another possible solution will be just to make an inner graph to go from Start,
               * and keep all these arguments common for all the destination inside that inner graph.
               * To not to drag these three args around from destination to destination
               */
            ),
          )
        },
      )
    }

    composable<TerminateInsuranceDestination.TerminationDate> {
      val viewModel: TerminationDateViewModel = koinViewModel {
        parametersOf(TerminationDataParameters(minDate, maxDate, insuranceDisplayName, exposureName))
      }
      TerminationDateDestination(
        viewModel = viewModel,
        onContinue = { localDate ->
          navController.navigate(
            TerminateInsuranceDestination.TerminationConfirmation(
              terminationType = TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Termination(
                localDate,
              ),
              parameters = TerminationConfirmationParameters(
                insuranceDisplayName = insuranceDisplayName,
                exposureName = exposureName,
                activeFrom = activeFrom,
                contractGroup = contractGroup,
              ),
            ),
          )
        },
        navigateUp = navigator::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
      )
    }

    composable<TerminateInsuranceDestination.TerminationConfirmation> { backStackEntry ->
      val viewModel: TerminationConfirmationViewModel = koinViewModel {
        parametersOf(
          terminationType,
        )
      }
      TerminationConfirmationDestination(
        viewModel = viewModel,
        onContinue = viewModel::submitContractTermination,
        navigateToNextStep = { terminationStep ->
          viewModel.handledNextStepNavigation()
          navigator.navigateToTerminateFlowDestination(
            destination = terminationStep.toTerminateInsuranceDestination(
              insuranceDisplayName = parameters.insuranceDisplayName,
              exposureName = parameters.exposureName,
              activeFrom = parameters.activeFrom,
              contractGroup = parameters.contractGroup,
            ),
          )
        },
        navigateUp = navigator::navigateUp,
      )
    }

    composable<TerminateInsuranceDestination.InsuranceDeletion> {
      InsuranceDeletionDestination(
        displayName = insuranceDisplayName,
        exposureName = exposureName,
        onContinue = {
          navController.navigate(
            TerminateInsuranceDestination.TerminationConfirmation(
              terminationType = TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion,
              parameters = TerminationConfirmationParameters(
                insuranceDisplayName = insuranceDisplayName,
                exposureName = exposureName,
                activeFrom = activeFrom,
                contractGroup = contractGroup,
              ),
            ),
          )
        },
        navigateUp = navigator::navigateUp,
        closeTerminationFlow = closeTerminationFlow,
      )
    }
  }
}

/**
 * If we're going to a terminal destination, pop the termination flow backstack completely before going there.
 */
private fun <T : TerminateInsuranceDestination> Navigator.navigateToTerminateFlowDestination(destination: T) {
  val navOptions = navOptions {
    when (destination) {
      is TerminateInsuranceDestination.TerminationSuccess,
      is TerminateInsuranceDestination.TerminationFailure,
      is TerminateInsuranceDestination.UnknownScreen,
      -> {
        popUpTo<TerminateInsuranceGraphDestination> {
          inclusive = true
        }
      }

      else -> {}
    }
  }
  navigateUnsafe(destination, navOptions)
}
