package com.sph.sphmedia.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.byjus.common.network.NetworkManager
import com.sph.sphmedia.BuildConfig.APPLICATION_BASE_URL
import com.sphmedia.data.api.BreweryService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Singleton
    @Provides
    fun providesContext(application: Application): Context = application.applicationContext


    @Singleton
    @Provides
    fun provideGithubService(okHttpClient: OkHttpClient): BreweryService {
        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient).baseUrl(APPLICATION_BASE_URL).build()
        return retrofit.create(BreweryService::class.java)
    }


    /**
     * OkHttpClient
     * */

    @Provides
    @Singleton
    internal fun provideOkHttpClient(
        networkManager: NetworkManager
    ): OkHttpClient = networkManager.getClient()


    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }


}
