package com.hedvig.app.feature.dashboard.ui.contractdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.app.feature.dashboard.data.DashboardRepository
import e
import kotlinx.coroutines.launch

abstract class ContractDetailViewModel : ViewModel() {
    abstract val data: LiveData<DashboardQuery.Contract>

    abstract fun loadContract(id: String)
}

class ContractDetailViewModelImpl(
    private val dashboardRepository: DashboardRepository
) : ContractDetailViewModel() {
    override val data = MutableLiveData<DashboardQuery.Contract>()

    override fun loadContract(id: String) {
        viewModelScope.launch {
            val response = runCatching {
                dashboardRepository
                    .dashboardAsync()
                    .await()
            }

            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }

            val contract = response
                .getOrNull()
                ?.data()
                ?.contracts
                ?.firstOrNull { it.id == id }

            data.postValue(contract)
        }
    }
}