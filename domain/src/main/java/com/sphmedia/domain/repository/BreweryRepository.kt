package com.sphmedia.domain.repository

import com.sphmedia.data.api.BreweryService
import com.sphmedia.data.db.BreweryCacheDao
import com.sphmedia.data.db.BreweryDao
import com.sphmedia.data.model.Brewery
import javax.inject.Inject

class BreweryRepository @Inject constructor(
    private val breweryDao: BreweryDao, // Room DAO
    private val breweryApi: BreweryService, // Retrofit API Service
    private val breweryCacheDao: BreweryCacheDao
) {


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

