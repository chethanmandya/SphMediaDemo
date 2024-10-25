package com.sph.sphmedia.ui.brewery


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sphmedia.data.model.Brewery
import com.sphmedia.domain.repository.BreweryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BreweryDetailViewModel @Inject constructor(
    private val breweryRepository: BreweryRepository
) : ViewModel() {

    private var _brewery = MutableLiveData<Brewery>()
    val brewery: LiveData<Brewery> = _brewery

    fun getBreweryById(breweryId: String) {
        viewModelScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            Timber.i("Throwable : ${throwable.message}")
        }) {
            _brewery.postValue(breweryRepository.getBreweryById(breweryId))
        }
    }
}
