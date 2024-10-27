package com.sphmedia.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sphmedia.data.api.BreweryService
import com.sphmedia.data.db.BreweryCacheDao
import com.sphmedia.data.db.BreweryDao
import com.sphmedia.data.model.Brewery
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BreweryRepository @Inject constructor(
    private val breweryDao: BreweryDao, // Room DAO
    private val breweryApi: BreweryService, // Retrofit API Service
    private val breweryCacheDao: BreweryCacheDao
) {
//    fun getBreweries(page: Int, perPage: Int): Flow<Resource<List<Brewery>>> {
//        return object : NetworkBoundResource<List<Brewery>, List<Brewery>>() {
//
//            // Load data from Room
//            override fun loadFromDb(): Flow<List<Brewery>> {
//                return breweryDao.getBreweryPage(page, perPage)
//            }
//
//            // Fetch data from API for the current page
//            override suspend fun fetchFromNetwork(): List<Brewery> {
//                return apiService.getBreweriesByType("micro", perPage, page)
//            }
//
//            // Save API response to the Room database
//            override suspend fun saveNetworkResult(item: List<Brewery>) {
//                breweryDao.insertAll(item)
//            }
//
//            // Optionally, decide whether to fetch new data
//            override fun shouldFetch(data: List<Brewery>?): Boolean {
//                return data.isNullOrEmpty() || data.size < perPage
//            }
//
//        }.asFlow()
//    }
//} ̰


    // Modify this method to accept the page number
    fun getBreweriesStream(breweryType: String) = BreweryPagingSource(
        breweryDao = breweryDao,
        breweryCacheDao = breweryCacheDao,
        apiService = breweryApi,
        breweryType = breweryType
    )


    // Fetch brewery details by ID from local database or remote API
    suspend fun getBreweryById(breweryId: String): Brewery? {
        // First, try to get the brewery from the local database
        val breweryFromDb = breweryDao.getBreweryById(breweryId)
        return breweryFromDb ?: run {
            // If not found in the database, fetch from remote API
            val breweryFromApi = breweryApi.getBreweryById(breweryId)
            // You might want to save the fetched brewery to the database for future access
            if (breweryFromApi != null) {
                breweryDao.insertBrewery(breweryFromApi)
            }
            breweryFromApi
        }
    }
}

