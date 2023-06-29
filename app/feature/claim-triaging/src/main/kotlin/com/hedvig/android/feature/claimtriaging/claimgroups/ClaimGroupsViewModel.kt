package com.hedvig.android.feature.claimtriaging.claimgroups

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.android.i
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimtriaging.ClaimGroup
import com.hedvig.android.feature.claimtriaging.GetEntryPointGroupsUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClaimGroupsViewModel(
  private val getEntryPointGroupsUseCase: GetEntryPointGroupsUseCase,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(ClaimGroupsUiState())
  val uiState = _uiState.asStateFlow()

  init {
    loadClaimGroups()
  }

  fun loadClaimGroups() {
    _uiState.update { it.copy(chipLoadingErrorMessage = null, isLoading = true) }
    viewModelScope.launch {
      getEntryPointGroupsUseCase.invoke().fold(
        ifLeft = { errorMessage ->
          i(errorMessage.throwable) { "ClaimGroupsViewModel failed to load entry groups" }
          _uiState.update {
            it.copy(
              chipLoadingErrorMessage = errorMessage.message,
              isLoading = false,
            )
          }
        },
        ifRight = { claimGroups ->
          _uiState.update {
            it.copy(
              claimGroups = claimGroups,
              selectedClaimGroup = null,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun onSelectClaimGroup(claimGroup: ClaimGroup) {
    _uiState.update { it.copy(selectedClaimGroup = claimGroup) }
  }

  fun startClaimFlow() {
    if (_uiState.value.isLoading) return
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      claimFlowRepository.startClaimFlow(null, null).fold(
        ifLeft = { errorMessage ->
          _uiState.update { it.copy(isLoading = false, startClaimErrorMessage = errorMessage.message) }
        },
        ifRight = { claimFlowStep ->
          _uiState.update { it.copy(isLoading = false, nextStep = claimFlowStep) }
        },
      )
    }
  }

  fun showedStartClaimError() {
    _uiState.update { it.copy(startClaimErrorMessage = null) }
  }

  fun handledNextStepNavigation() {
    _uiState.update { it.copy(nextStep = null) }
  }
}

@Immutable
internal data class ClaimGroupsUiState(
  val claimGroups: ImmutableList<ClaimGroup> = persistentListOf(),
  val selectedClaimGroup: ClaimGroup? = null,
  val chipLoadingErrorMessage: String? = null,
  val startClaimErrorMessage: String? = null,
  val isLoading: Boolean = true,
  val nextStep: ClaimFlowStep? = null,
) {
  val canContinue: Boolean
    get() = selectedClaimGroup != null &&
      isLoading == false &&
      nextStep == null &&
      chipLoadingErrorMessage == null &&
      startClaimErrorMessage == null
}
