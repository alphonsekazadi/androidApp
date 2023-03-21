package com.hedvig.android.feature.terminateinsurance.step.terminationsuccess

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoScreen
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun TerminationSuccessDestination(
  terminationDate: LocalDate,
  surveyUrl: String,
  windowSizeClass: WindowSizeClass,
  navigateBack: () -> Unit,
) {
  val uriHandler = LocalUriHandler.current
  TerminationSuccessScreen(
    terminationDate = terminationDate,
    windowSizeClass = windowSizeClass,
    onOpenSurvey = { uriHandler.openUri(surveyUrl) },
    navigateBack = navigateBack,
  )
}

@Composable
private fun TerminationSuccessScreen(
  terminationDate: LocalDate,
  windowSizeClass: WindowSizeClass,
  onOpenSurvey: () -> Unit,
  navigateBack: () -> Unit,
) {
  TerminationInfoScreen(
    windowSizeClass = windowSizeClass,
    navigateBack = navigateBack,
    title = "",
    headerText = stringResource(R.string.TERMINATION_SUCCESSFUL_TITLE),
    bodyText = stringResource(
      R.string.TERMINATION_SUCCESSFUL_TEXT,
      terminationDate,
      "Hedvig",
    ),
    bottomContent = {
      Column {
        LargeOutlinedTextButton(
          text = stringResource(R.string.general_done_button),
          onClick = navigateBack,
        )
        Spacer(Modifier.height(16.dp))
        LargeContainedTextButton(
          text = stringResource(R.string.TERMINATION_OPEN_SURVEY_LABEL),
          onClick = onOpenSurvey,
        )
      }
    },
    icon = Icons.Outlined.CheckCircle,
  )
}

@HedvigPreview
@Composable
private fun PreviewTerminationSuccessScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationSuccessScreen(
        LocalDate(2021, 12, 21),
        WindowSizeClass.calculateForPreview(),
        {},
        {},
      )
    }
  }
}
