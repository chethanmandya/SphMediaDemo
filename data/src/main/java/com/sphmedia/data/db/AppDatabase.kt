package com.sphmedia.data.db


import androidx.room.Database
import androidx.room.RoomDatabase
import com.sphmedia.data.model.Brewery
import com.sphmedia.data.model.BreweryCache


@Database(
    entities = [Brewery::class, BreweryCache::class], version = 1, exportSchema = true
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun BreweryDao(): BreweryDao
    abstract fun BreweryCacheDao(): BreweryCacheDao
}


