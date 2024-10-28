package com.sph.sphmedia.di

import com.sphmedia.data.api.BreweryService
import com.sphmedia.data.db.BreweryCacheDao
import com.sphmedia.data.db.BreweryDao
import com.sphmedia.domain.repository.BreweryRepository
import com.sphmedia.domain.repository.BreweryRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun provideRepository(
        breweryApi: BreweryService,
        breweryDao: BreweryDao,
        breweryCacheDao: BreweryCacheDao
    ): BreweryRepository = BreweryRepositoryImpl(breweryApi, breweryDao, breweryCacheDao)
}