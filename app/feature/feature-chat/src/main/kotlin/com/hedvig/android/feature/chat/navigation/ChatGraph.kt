package com.hedvig.android.feature.chat.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.feature.chat.ChatViewModelNew
import com.hedvig.android.feature.chat.ui.ChatDestination
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.chatGraph(hedvigDeepLinkContainer: HedvigDeepLinkContainer, navigator: Navigator) {
  composable<AppDestination.Chat>(
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.chat },
    ),
    enterTransition = { slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up) },
    exitTransition = { slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down) },
  ) {
    val viewModel: ChatViewModelNew = koinViewModel()
    ChatDestination(viewModel = viewModel, navigator::navigateUp)
  }
}
