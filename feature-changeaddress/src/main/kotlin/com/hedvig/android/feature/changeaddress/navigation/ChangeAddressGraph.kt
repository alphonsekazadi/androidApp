package com.hedvig.android.feature.changeaddress.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.feature.changeaddress.navigation.ChangeAddressResultDestination
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressEnterNewDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressOfferDestination
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressSelectHousingTypeDestination
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.changeAddressGraph(
  windowSizeClass: WindowSizeClass,
  density: Density,
  navController: NavHostController,
  openChat: () -> Unit,
  navigateUp: () -> Boolean,
  finish: () -> Unit,
) {
  animatedNavigation<Destinations.ChangeAddress>(
    startDestination = createRoutePattern<ChangeAddressDestination.SelectHousingType>(),
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    animatedComposable<ChangeAddressDestination.SelectHousingType> {
      val viewModel = navGraphScopedViewModel(navController, it)
      ChangeAddressSelectHousingTypeDestination(
        viewModel = viewModel,
        navigateUp = { navigateUp() },
        onHousingTypeSubmitted = {
          navController.navigate(ChangeAddressDestination.EnterNewAddress)
        },
      )
    }

    animatedComposable<ChangeAddressDestination.EnterNewAddress> {
      val viewModel = navGraphScopedViewModel(navController, it)
      ChangeAddressEnterNewDestination(
        viewModel = viewModel,
        navigateBack = { navController.navigateUp() },
        onQuotesReceived = {
          navController.navigate(ChangeAddressDestination.OfferDestination)
        },
      )
    }

    animatedComposable<ChangeAddressDestination.OfferDestination> {
      val viewModel = navGraphScopedViewModel(navController, it)
      BackHandler {
        viewModel.onQuotesCleared()
        navController.navigateUp()
      }
      ChangeAddressOfferDestination(
        viewModel = viewModel,
        openChat = openChat,
        navigateBack = {
          viewModel.onQuotesCleared()
          navController.navigateUp()
        },
        onChangeAddressResult = { navController.navigate(ChangeAddressDestination.AddressResult) },
      )
    }

    animatedComposable<ChangeAddressDestination.AddressResult> {
      BackHandler {
        finish()
      }
      ChangeAddressResultDestination {
        finish()
      }
    }
  }
}

@Composable
private fun navGraphScopedViewModel(
  navController: NavHostController,
  backStackEntry: NavBackStackEntry,
): ChangeAddressViewModel {
  val parentEntry = remember(navController, backStackEntry) {
    navController.getBackStackEntry(createRoutePattern<Destinations.ChangeAddress>())
  }
  return koinViewModel(viewModelStoreOwner = parentEntry)
}
