package com.hedvig.android.feature.travelcertificate.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.error.ErrorDialog
import com.hedvig.android.core.ui.infocard.DrawableInfoCard
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import hedvig.resources.R

@Composable
fun TravelCertificateInformation(
  infoSections: List<TravelCertificateResult.TraverlCertificateData.InfoSection>?,
  isLoading: Boolean,
  errorMessage: String?,
  onErrorDialogDismissed: () -> Unit,
  onContinue: () -> Unit,
  navigateBack: () -> Unit,
) {
  if (errorMessage != null) {
    ErrorDialog(
      title = stringResource(id = R.string.general_error),
      message = errorMessage,
      onDismiss = onErrorDialogDismissed,
    )
  }

  if (isLoading) {
    Box(modifier = Modifier.fillMaxSize()) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
  } else {
    HedvigScaffold(
      navigateUp = {
        navigateBack()
      },
      modifier = Modifier.clearFocusOnTap(),
    ) {
      Spacer(modifier = Modifier.padding(top = 56.dp))
      Text(
        text = stringResource(id = R.string.travel_certificate_description),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(modifier = Modifier.padding(top = 48.dp))
      infoSections?.map {
        DrawableInfoCard(
          title = it.title,
          text = it.body,
          icon = painterResource(com.hedvig.android.core.designsystem.R.drawable.ic_info_transparent),
          iconColor = MaterialTheme.colorScheme.primary,
          colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))
      }
      Spacer(modifier = Modifier.weight(1f))
      LargeContainedButton(
        onClick = onContinue,
        shape = MaterialTheme.shapes.squircle,
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        Text(stringResource(R.string.travel_certificate_get_travel_certificate_button))
      }
      Spacer(Modifier.height(32.dp))
    }
  }
}

@HedvigPreview
@Composable
fun PreviewTravelCertificateInformation() {
  HedvigTheme {
    TravelCertificateInformation(
      infoSections = listOf(
        TravelCertificateResult.TraverlCertificateData.InfoSection(
          title = "Test1",
          body = "Body1"
        ),
        TravelCertificateResult.TraverlCertificateData.InfoSection(
          title = "Test2",
          body = "Body2"
        )
      ),
      isLoading = false,
      errorMessage = null,
      onErrorDialogDismissed = {},
      onContinue = {},
      navigateBack = {},
    )
  }
}