package com.sph.sphmedia.di

import android.app.Application
import androidx.room.Room
import com.sphmedia.data.db.AppDatabase
import com.sphmedia.data.db.BreweryCacheDao
import com.sphmedia.data.db.BreweryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideDb(app: Application): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "SPHMedia.db")
            .fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideBreweryDao(db: AppDatabase): BreweryDao {
        return db.breweryDao()
    }

    @Singleton
    @Provides
    fun provideBreweryCacheDao(db: AppDatabase): BreweryCacheDao {
        return db.breweryCacheDao()
    }
}