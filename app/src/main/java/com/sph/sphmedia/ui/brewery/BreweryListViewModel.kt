package com.sph.sphmedia.ui.brewery

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.sphmedia.data.model.Brewery
import com.sphmedia.domain.repository.BreweryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class BreweryListViewModel @Inject constructor(
    private val breweryRepository: BreweryRepository
) : ViewModel() {

    // Function to get paginated breweries as Flow
    fun getBreweriesStream(breweryType: String): Flow<PagingData<Brewery>> {
        return breweryRepository.getBreweriesStream(breweryType)
    }

    private val scrollPositionMap = mutableMapOf<String, Int>()

    fun setCurrentScrollPosition(breweryType: String, position: Int) {
        if(position != 0) {
            scrollPositionMap[breweryType] = position
        }
    }

    fun getCurrentScrollPosition(breweryType: String): Int {
        return scrollPositionMap[breweryType] ?: 0
    }
}