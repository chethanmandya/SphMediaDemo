package com.sphmedia.data.api

import com.sphmedia.data.model.Brewery
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface BreweryService {
    @GET("v1/breweries")
    suspend fun getBreweriesByType(
        @Query("by_type") type: String, // Filter by type
        @Query("per_page") perPage: Int, // Number of results per page
        @Query("page") page: Int // Pagination page number
    ): List<Brewery>


    @GET("v1/breweries/{id}")
    suspend fun getBreweryById(@Path("id") breweryId: String): Brewery?


}

