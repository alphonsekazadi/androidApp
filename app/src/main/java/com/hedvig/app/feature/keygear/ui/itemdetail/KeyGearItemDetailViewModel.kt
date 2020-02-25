package com.hedvig.app.feature.keygear.ui.itemdetail

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import com.hedvig.app.util.LiveEvent
import kotlinx.coroutines.launch

abstract class KeyGearItemDetailViewModel : ViewModel() {
    abstract val data: LiveData<KeyGearItemQuery.KeyGearItem>

    abstract val isUploading: LiveEvent<Boolean>
    abstract val isDeleted: LiveEvent<Boolean>

    abstract fun loadItem(id: String)
    abstract fun uploadReceipt(uri: Uri)
    abstract fun updateItemName(newName: String)
    abstract fun deleteItem()
}

class KeyGearItemDetailViewModelImpl(
    private val repository: KeyGearItemsRepository
) : KeyGearItemDetailViewModel() {

    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override val isUploading = LiveEvent<Boolean>()
    override val isDeleted = LiveEvent<Boolean>()

    override fun loadItem(id: String) {
        viewModelScope.launch {
            for (response in repository.keyGearItem(id)) {
                data.postValue(response.data()?.keyGearItem)
            }
        }
    }

    override fun uploadReceipt(uri: Uri) {
        viewModelScope.launch {
            isUploading.value = true
            val id = data.value?.fragments?.keyGearItemFragment?.id ?: return@launch
            repository.uploadReceipt(id, uri)
        }
    }

    override fun updateItemName(newName: String) {
        viewModelScope.launch {
            val id = data.value?.fragments?.keyGearItemFragment?.id ?: return@launch
            repository.updateItemName(id, newName)
        }
    }

    override fun deleteItem() {
        viewModelScope.launch {
            val id = data.value?.fragments?.keyGearItemFragment?.id ?: return@launch
            repository.deleteItem(id)
            isDeleted.postValue(true)
        }
    }
}
