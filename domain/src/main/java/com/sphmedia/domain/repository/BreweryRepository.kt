package com.sphmedia.domain.repository

import androidx.paging.PagingData
import com.sphmedia.data.model.Brewery
import kotlinx.coroutines.flow.Flow

interface BreweryRepository {
    fun getBreweriesStream(type: String): Flow<PagingData<Brewery>>
    suspend fun getBreweryById(breweryId: String): Brewery?
}