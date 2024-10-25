package com.sphmedia.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sphmedia.data.model.BreweryCache

@Dao
interface BreweryCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: BreweryCache)

    @Query("SELECT lastUpdated FROM brewery_cache WHERE pageNumber = :page AND breweryType = :breweryType")
    suspend fun getLastUpdated(page: Int, breweryType: String): Long?

    @Query("DELETE FROM brewery_cache WHERE breweryType = :breweryType")
    suspend fun clearCacheByType(breweryType: String)
}
