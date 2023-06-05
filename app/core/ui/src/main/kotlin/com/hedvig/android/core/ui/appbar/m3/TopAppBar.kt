package com.hedvig.android.core.ui.appbar.m3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Just the layout and placing of the top app bar, without the background, drag handling and so on. Can be used to
 * simply place the actions in the right spot, without interfering in other ways like swallowing the touch events on it.
 */
@Composable
fun TopAppBarLayoutForActions(
  modifier: Modifier = Modifier,
  actions: @Composable RowScope.() -> Unit = {},
) {
  Row(
    horizontalArrangement = Arrangement.End,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
      .height(64.dp)
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    actions()
  }
}

@Composable
fun TopAppBarWithBack(
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
  colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.background,
    scrolledContainerColor = MaterialTheme.colorScheme.surface,
  ),
  scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
) {
  TopAppBar(
    title = title,
    onClick = onClick,
    actionType = TopAppBarActionType.BACK,
    windowInsets = windowInsets,
    colors = colors,
    scrollBehavior = scrollBehavior,
    modifier = modifier,
  )
}

@Suppress("unused")
@Composable
fun TopAppBarWithClose(
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
  colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.background,
    scrolledContainerColor = MaterialTheme.colorScheme.surface,
  ),
  scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
) {
  TopAppBar(
    title = title,
    onClick = onClick,
    actionType = TopAppBarActionType.CLOSE,
    windowInsets = windowInsets,
    colors = colors,
    scrollBehavior = scrollBehavior,
    modifier = modifier,
  )
}

enum class TopAppBarActionType {
  BACK, CLOSE
}

@Composable
inline fun TopAppBar(
  title: String,
  crossinline onClick: () -> Unit,
  actionType: TopAppBarActionType,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
  colors: TopAppBarColors,
  scrollBehavior: TopAppBarScrollBehavior,
  modifier: Modifier = Modifier,
) {
  TopAppBar(
    modifier = modifier,
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
      )
    },
    navigationIcon = {
      IconButton(
        onClick = { onClick() },
        content = {
          Icon(
            imageVector = when (actionType) {
              TopAppBarActionType.BACK -> Icons.Filled.ArrowBack
              TopAppBarActionType.CLOSE -> Icons.Filled.Close
            },
            contentDescription = null,
          )
        },
      )
    },
    windowInsets = windowInsets,
    colors = colors,
    scrollBehavior = scrollBehavior,
  )
}
