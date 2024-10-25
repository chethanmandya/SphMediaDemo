package com.sph.sphmedia.ui.brewery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sphmedia.data.model.Brewery
import com.sphmedia.domain.repository.BreweryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

@HiltViewModel
class BreweryNanoViewModel @Inject constructor(
    private val breweryRepository: BreweryRepository
) : ViewModel() {

    // Function to get paginated breweries as Flow
    fun getBreweriesStream(breweryType: String): Flow<PagingData<Brewery>> {
        return breweryRepository.getBreweriesStream(breweryType)
    }
}