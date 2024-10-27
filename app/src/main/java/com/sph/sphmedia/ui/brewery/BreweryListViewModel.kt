package com.sph.sphmedia.ui.brewery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sphmedia.data.model.Brewery
import com.sphmedia.domain.repository.BreweryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class BreweryListViewModel @Inject constructor(
    private val breweryRepository: BreweryRepository
) : ViewModel() {


    /**
     *  Why we used a MutableMap: See the discussion in the thread below:
     *  https://issuetracker.google.com/issues/177245496
     *
     *  When navigating to the detail screen and returning to the list view, the scroll position should remain at the point where it was left.
     *  Although `rememberLazyPageState` is available to preserve the scroll position in a LazyColumn, it currently has issues in scenarios like configuration changes and navigating back.
     *  `rememberLazyPageState` resets to the first item following configuration changes or navigation.
     *
     *  To address this, it is suggested to cache the data in the ViewModel scope.
     *  Since our use case involves different category types, I manage separate `PagingData` entries within a Map, each cached in `viewModelScope`.
     */

    // Map to hold a Pager for each brewery type
    private val pagerMap = mutableMapOf<String, Flow<PagingData<Brewery>>>()


    fun getOrCreatePager(type: String): Flow<PagingData<Brewery>> {
        return pagerMap.getOrPut(type) {
            Pager(config = PagingConfig(pageSize = 20),
                pagingSourceFactory = { breweryRepository.getBreweriesStream(type) }).flow.cachedIn(
                viewModelScope
            )
        }
    }


}
