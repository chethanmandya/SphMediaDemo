package com.sphmedia.data.db

import androidx.paging.PagingSource
import androidx.room.*
import com.sphmedia.data.model.Brewery

@Dao
interface BreweryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(breweries: List<Brewery>)

    @Query("SELECT * FROM breweries WHERE brewery_type = :breweryType ORDER BY id LIMIT :pageSize OFFSET :offset")
    suspend fun getBreweriesPage(breweryType: String, pageSize: Int, offset: Int): List<Brewery>

    @Query("SELECT * FROM breweries WHERE id = :breweryId")
    suspend fun getBreweryById(breweryId: String): Brewery?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrewery(brewery: Brewery)
}
