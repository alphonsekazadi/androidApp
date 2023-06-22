package com.hedvig.android.feature.claimtriaging.claimentrypoints

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.SerifBookSmall
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.core.ui.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.data.claimtriaging.EntryPoint
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.EntryPointOption
import com.hedvig.android.feature.claimtriaging.OptionChipsFlowRow
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import slimber.log.e

@Composable
internal fun ClaimEntryPointsDestination(
  viewModel: ClaimEntryPointsViewModel,
  onEntryPointSubmit: (EntryPointId, ImmutableList<EntryPointOption>) -> Unit,
  startClaimFlow: (EntryPointId) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ClaimEntryPointsScreen(
    uiState = uiState,
    loadEntryPoints = viewModel::loadEntryPoints,
    onSelectEntryPoint = viewModel::onSelectEntryPoint,
    onContinue = {
      uiState.selectedEntryPoint?.let { entryPoint ->
        val entryPointOptions = entryPoint.entryPointOptions
        if (entryPointOptions?.isNullOrEmpty() == true) {
          startClaimFlow(entryPoint.id)
        } else {
          onEntryPointSubmit(entryPoint.id, entryPointOptions)
        }
      }
    },
    navigateUp = navigateUp,
  )
}

@Composable
private fun ClaimEntryPointsScreen(
  uiState: ClaimEntryPointsUiState,
  loadEntryPoints: () -> Unit,
  onSelectEntryPoint: (EntryPoint) -> Unit,
  onContinue: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigTheme(useNewColorScheme = true) {
    Box(modifier = Modifier.fillMaxSize(), propagateMinConstraints = true) {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        Spacer(Modifier.height(16.dp))
        Text(
          text = stringResource(R.string.CLAIMS_TRIAGING_WHAT_HAPPENED_TITLE),
          style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = SerifBookSmall,
            lineBreak = LineBreak.Heading,
          ),
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        if (uiState.errorMessage != null) {
          LaunchedEffect(Unit) { e { "ClaimEntryPointsScreen: errorMessage${uiState.errorMessage}" } }
          GenericErrorScreen(
            description = uiState.errorMessage,
            onRetryButtonClick = loadEntryPoints,
            modifier = Modifier.padding(16.dp),
          )
        } else {
          Spacer(Modifier.weight(1f))
          OptionChipsFlowRow(
            items = uiState.entryPoints,
            itemDisplayName = EntryPoint::displayName,
            selectedItem = uiState.selectedEntryPoint,
            onItemClick = { entryPoint -> onSelectEntryPoint(entryPoint) },
            modifier = Modifier.padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(8.dp))
          HedvigContainedButton(
            text = stringResource(R.string.claims_continue_button),
            onClick = onContinue,
            contentPadding = PaddingValues(16.dp),
            enabled = uiState.canContinue,
            modifier = Modifier.padding(horizontal = 16.dp),
          )
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
      HedvigFullScreenCenterAlignedProgress(show = uiState.isLoading)
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimEntryPointsScreen() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      val entryPoints = remember {
        List(12) {
          val displayName = buildString { repeat((4..14).random()) { append(('a'..'z').random()) } }
          EntryPoint(EntryPointId(it.toString()), displayName, persistentListOf())
        }.toImmutableList()
      }
      ClaimEntryPointsScreen(
        uiState = ClaimEntryPointsUiState(
          entryPoints = entryPoints,
          selectedEntryPoint = entryPoints[3],
          errorMessage = null,
          isLoading = false,
        ),
        loadEntryPoints = {},
        onSelectEntryPoint = {},
        onContinue = {},
        navigateUp = {},
      )
    }
  }
}
