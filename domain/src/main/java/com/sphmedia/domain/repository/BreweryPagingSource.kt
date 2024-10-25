package com.sphmedia.domain.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sphmedia.data.api.BreweryService
import com.sphmedia.data.db.BreweryCacheDao
import com.sphmedia.data.db.BreweryDao
import com.sphmedia.data.model.Brewery
import com.sphmedia.data.model.BreweryCache
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BreweryPagingSource(
    private val breweryDao: BreweryDao,
    private val breweryCacheDao: BreweryCacheDao,
    private val apiService: BreweryService,
    private val breweryType: String, // Pass the type here
) : PagingSource<Int, Brewery>() {

    private val PAGE_EXPIRY_TIME = TimeUnit.MINUTES.toMillis(5)

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Brewery> {
        val page = params.key ?: 1 // Start from page 1 if null
        val currentTime = System.currentTimeMillis()

        return try {
            // Get the last updated time for the current page from the database
            val lastUpdatedTime = breweryCacheDao.getLastUpdated(page, breweryType) ?: 0L
            val isPageExpired = (currentTime - lastUpdatedTime) > PAGE_EXPIRY_TIME

            if (isPageExpired) {
                // Fetch fresh data from the API
                val response = apiService.getBreweriesByType(breweryType, params.loadSize, page)

                // Stop fetching when no more pages exist (response is empty)
                val endOfPaginationReached = response.isEmpty()

                // Update lastUpdated time in the cache table for the current page
                breweryCacheDao.insert(
                    BreweryCache(
                        pageNumber = page,
                        breweryType = breweryType,
                        lastUpdated = currentTime
                    )
                )

                // Insert the fetched data into the database
                breweryDao.insertAll(response)

                // Return the paginated result
                LoadResult.Page(
                    data = response,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (endOfPaginationReached) null else page + 1
                )
            } else {
                Timber.v("Retrieving from DB : $breweryType, ${params.loadSize}, ${(page - 1) * params.loadSize} ")
                // Load data from the local Room database if the page is not expired
                val breweries = breweryDao.getBreweriesPage(
                    breweryType,
                    params.loadSize,
                    (page - 1) * params.loadSize
                )
                val endOfPaginationReached = breweries.isEmpty()

                // Return paginated data from the database
                LoadResult.Page(
                    data = breweries,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (endOfPaginationReached) null else page + 1
                )
            }
        } catch (e: Exception) {
            // Handle exceptions
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Brewery>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val page = state.closestPageToPosition(anchorPosition)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }
    }
}
