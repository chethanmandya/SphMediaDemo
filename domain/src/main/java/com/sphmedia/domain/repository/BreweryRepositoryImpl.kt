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


class BreweryRepositoryImpl @Inject constructor(
    private val breweryApi: BreweryService, // Retrofit API Service
    private val breweryDao: BreweryDao, private val breweryCacheDao: BreweryCacheDao
) : BreweryRepository {


    override fun getBreweriesStream(type: String): Flow<PagingData<Brewery>> {
        return Pager(config = PagingConfig(pageSize = 20), pagingSourceFactory = {
            BreweryPagingSource(
                breweryDao = breweryDao,
                breweryCacheDao = breweryCacheDao,
                apiService = breweryApi,
                breweryType = type
            )
        }).flow
    }


    // Fetch brewery details by ID from local database or remote API
    override suspend fun getBreweryById(breweryId: String): Brewery? {
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
