package com.hedvig.android.feature.odyssey.step.singleitem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import arrow.core.nonEmptyListOf
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.FormRowCard
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCardButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.ItemBrand
import com.hedvig.android.data.claimflow.ItemModel
import com.hedvig.android.data.claimflow.ItemProblem
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
import com.hedvig.android.feature.odyssey.ui.DatePickerUiState
import com.hedvig.android.feature.odyssey.ui.DatePickerWithDialog
import com.hedvig.android.feature.odyssey.ui.MonetaryAmountInput
import com.hedvig.android.feature.odyssey.ui.MultiSelectDialog
import com.hedvig.android.feature.odyssey.ui.SingleSelectDialog
import octopus.type.CurrencyCode

@Composable
internal fun SingleItemDestination(
  viewModel: SingleItemViewModel,
  windowSizeClass: WindowSizeClass,
  imageLoader: ImageLoader,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val nextStep = uiState.nextStep
  LaunchedEffect(nextStep) {
    if (nextStep != null) {
      navigateToNextStep(nextStep)
    }
  }
  SingleItemScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    imageLoader = imageLoader,
    submitSelections = viewModel::submitSelections,
    selectBrand = viewModel::selectBrand,
    selectModel = viewModel::selectModel,
    selectProblem = viewModel::selectProblem,
    showedError = viewModel::showedError,
    navigateUp = navigateUp,
  )
}

@Composable
private fun SingleItemScreen(
  uiState: SingleItemUiState,
  windowSizeClass: WindowSizeClass,
  imageLoader: ImageLoader,
  submitSelections: () -> Unit,
  selectBrand: (ItemBrand) -> Unit,
  selectModel: (ItemModel) -> Unit,
  selectProblem: (ItemProblem) -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    topAppBarText = stringResource(hedvig.resources.R.string.claims_item_screen_title),
    isLoading = uiState.isLoading,
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.hasError,
      showedError = showedError,
    ),
    modifier = Modifier.clearFocusOnTap(),
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(hedvig.resources.R.string.CLAIMS_SINGLE_ITEM_DETAILS),
      style = MaterialTheme.typography.headlineMedium,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(30.dp))
    Spacer(Modifier.weight(1f))

    uiState.itemBrandsUiState.asContent()?.let { itemBrandsUiState ->
      Spacer(Modifier.height(2.dp))
      Brands(itemBrandsUiState, uiState.canSubmit, selectBrand, imageLoader, sideSpacingModifier.fillMaxWidth())
      Spacer(Modifier.height(2.dp))
    }
    val itemModelsUiStateContent = uiState.itemModelsUiState.asContent()
    AnimatedVisibility(
      visible = itemModelsUiStateContent != null,
      enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
      exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
    ) {
      Column {
        Spacer(Modifier.height(2.dp))
        Models(
          uiState = itemModelsUiStateContent,
          enabled = uiState.canSubmit,
          selectModel = selectModel,
          imageLoader = imageLoader,
          modifier = sideSpacingModifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(2.dp))
      }
    }
    Spacer(Modifier.height(2.dp))
    DateOfPurchase(uiState.datePickerUiState, uiState.canSubmit, sideSpacingModifier.fillMaxWidth())
    Spacer(Modifier.height(4.dp))
    PriceOfPurchase(
      uiState = uiState.purchasePriceUiState,
      canInteract = uiState.canSubmit,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(2.dp))
    uiState.itemProblemsUiState.asContent()?.let { itemProblemsUiState ->
      Spacer(Modifier.height(2.dp))
      ItemProblems(
        uiState = itemProblemsUiState,
        enabled = uiState.canSubmit,
        selectProblem = selectProblem,
        imageLoader = imageLoader,
        modifier = sideSpacingModifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(2.dp))
    }
    Spacer(Modifier.height(14.dp))
    VectorInfoCard(
      stringResource(hedvig.resources.R.string.CLAIMS_SINGLE_ITEM_NOTICE_LABEL),
      sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      onClick = submitSelections,
      enabled = uiState.canSubmit,
      text = stringResource(hedvig.resources.R.string.general_continue_button),
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Composable
private fun Models(
  uiState: ItemModelsUiState.Content?,
  enabled: Boolean,
  selectModel: (ItemModel) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  LocalConfiguration.current
  val resources = LocalContext.current.resources
  var showDialog by rememberSaveable { mutableStateOf(false) }
  if (showDialog && uiState != null) {
    SingleSelectDialog(
      title = stringResource(hedvig.resources.R.string.claims_item_screen_model_button),
      optionsList = uiState.availableItemModels,
      onSelected = selectModel,
      getDisplayText = { it.displayName(resources) },
      getImageUrl = { it.asKnown()?.imageUrl },
      getId = { it.asKnown()?.itemModelId ?: "id" },
      imageLoader = imageLoader,
    ) {
      showDialog = false
    }
  }

  HedvigCardButton(
    onClick = { showDialog = true },
    hintText = stringResource(hedvig.resources.R.string.claims_item_screen_model_button),
    inputText = uiState?.selectedItemModel?.displayName(resources),
    modifier = modifier,
    enabled = enabled,
  )
}

@Composable
private fun Brands(
  uiState: ItemBrandsUiState.Content,
  enabled: Boolean,
  selectBrand: (ItemBrand) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier,
) {
  LocalConfiguration.current
  val resources = LocalContext.current.resources
  var showDialog by rememberSaveable { mutableStateOf(false) }
  if (showDialog) {
    SingleSelectDialog(
      title = stringResource(hedvig.resources.R.string.SINGLE_ITEM_INFO_BRAND),
      optionsList = uiState.availableItemBrands,
      onSelected = selectBrand,
      getDisplayText = { it.displayName(resources) },
      getImageUrl = { null },
      getId = { it.asKnown()?.itemBrandId ?: "id" },
      imageLoader = imageLoader,
    ) {
      showDialog = false
    }
  }

  HedvigCardButton(
    onClick = { showDialog = true },
    hintText = stringResource(hedvig.resources.R.string.SINGLE_ITEM_INFO_BRAND),
    inputText = uiState.selectedItemBrand?.displayName(resources),
    modifier = modifier,
    enabled = enabled,
  )
}

@Composable
private fun DateOfPurchase(
  uiState: DatePickerUiState,
  canInteract: Boolean,
  modifier: Modifier,
) {
  DatePickerWithDialog(
    uiState = uiState,
    canInteract = canInteract,
    startText = stringResource(hedvig.resources.R.string.claims_item_screen_date_of_purchase_button),
    modifier = modifier,
  )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PriceOfPurchase(
  uiState: PurchasePriceUiState,
  canInteract: Boolean,
  modifier: Modifier = Modifier,
) {
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  FormRowCard(
    modifier = modifier,
    enabled = canInteract,
    onClick = {
      focusRequester.requestFocus()
      keyboardController?.show()
    },
  ) {
    Text(stringResource(hedvig.resources.R.string.claims_item_screen_purchase_price_button))
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.width(8.dp))
    CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = ContentAlpha.medium)) {
      MonetaryAmountInput(
        value = uiState.uiMoney.amount?.toString() ?: "",
        canInteract = canInteract,
        onInput = { uiState.updateAmount(it) },
        currency = uiState.uiMoney.currencyCode.rawValue,
        maximumFractionDigits = 0,
        focusRequester = focusRequester,
      )
    }
  }
}

@Composable
private fun ItemProblems(
  uiState: ItemProblemsUiState.Content,
  enabled: Boolean,
  selectProblem: (ItemProblem) -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier,
) {
  var showDialog: Boolean by rememberSaveable { mutableStateOf(false) }
  if (showDialog) {
    MultiSelectDialog(
      title = stringResource(hedvig.resources.R.string.claims_item_screen_type_of_damage_button),
      optionsList = uiState.availableItemProblems,
      onSelected = selectProblem,
      getDisplayText = { it.displayName },
      getIsSelected = { uiState.selectedItemProblems.contains(it) },
      getImageUrl = { null },
      getId = { it.itemProblemId },
      imageLoader = imageLoader,
    ) {
      showDialog = false
    }
  }

  HedvigCardButton(
    onClick = { showDialog = true },
    hintText = stringResource(hedvig.resources.R.string.claims_item_screen_type_of_damage_button),
    inputText = when {
      uiState.selectedItemProblems.isEmpty() -> null
      uiState.selectedItemProblems.size == 1 -> uiState.selectedItemProblems.first().displayName
      else -> stringResource(hedvig.resources.R.string.OFFER_START_DATE_MULTIPLE)
    },
    modifier = modifier,
    enabled = enabled,
  )
}

@HedvigPreview
@Composable
private fun PreviewSingleItemScreen(
  @PreviewParameter(IsLoadingPreviewProvider::class) isLoading: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      SingleItemScreen(
        SingleItemUiState(
          datePickerUiState = remember { DatePickerUiState(null) },
          purchasePriceUiState = PurchasePriceUiState(299.90, CurrencyCode.SEK),
          itemBrandsUiState = ItemBrandsUiState.Content(
            nonEmptyListOf(ItemBrand.Known("Item Brand", "", "")),
            ItemBrand.Known("Item Brand #1", "", ""),
          ),
          itemModelsUiState = ItemModelsUiState.Content(
            nonEmptyListOf(ItemModel.Known("Item Model", null, "", "", "")),
            ItemModel.Known("Item Model #2", null, "", "", ""),
          ),
          itemProblemsUiState = ItemProblemsUiState.Content(
            nonEmptyListOf(ItemProblem("Item Problem", "")),
            listOf(ItemProblem("Item Problem #3", "")),
          ),
          isLoading = isLoading,
          hasError = false,
          nextStep = null,
        ),
        WindowSizeClass.calculateForPreview(),
        rememberPreviewImageLoader(),
        {}, {}, {}, {}, {}, {},
      )
    }
  }
}

private class IsLoadingPreviewProvider : CollectionPreviewParameterProvider<Boolean>(listOf(false, true))
