package com.sph.sphmedia.repo

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sphmedia.data.api.BreweryService
import com.sphmedia.data.db.AppDatabase
import com.sphmedia.data.db.BreweryCacheDao
import com.sphmedia.data.db.BreweryDao
import com.sphmedia.data.model.Brewery
import com.sphmedia.domain.repository.BreweryPagingSource
import com.sphmedia.domain.repository.BreweryRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class BreweryRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var breweryDao: BreweryDao
    private lateinit var breweryCacheDao: BreweryCacheDao
    private lateinit var breweryService: BreweryService
    private lateinit var repository: BreweryRepositoryImpl
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        // Initialize MockWebServer
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Set up Room database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        breweryDao = database.breweryDao()
        breweryCacheDao = database.breweryCacheDao()

        // Set up Moshi for Retrofit
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        // Set up Retrofit for the API service
        breweryService = Retrofit.Builder().baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi)).build()
            .create(BreweryService::class.java)

        // Create the repository
        repository = BreweryRepositoryImpl(breweryService, breweryDao, breweryCacheDao)
    }

    @After
    fun tearDown() {
        // Shutdown MockWebServer
        mockWebServer.shutdown()
        // Close the database
        database.close()
    }

    @Test
    fun getBreweryById_fromApi_savesToLocal() = runBlocking {
        // Given
        val breweryId = "1"
        val expectedBrewery = Brewery(
            id = breweryId,
            name = "Brewery One",
            brewery_type = "micro",
            address_1 = "123 Brew St",
            address_2 = null,
            address_3 = null,
            city = "Brew City",
            state_province = "BC",
            postal_code = "12345",
            country = "Country",
            longitude = null,
            latitude = null,
            phone = "123-456-7890",
            website_url = "http://breweryone.com",
            state = "State",
            street = "123 Brew St"
        )

        // Mock the API response
        mockWebServer.enqueue(
            MockResponse().setBody(
                """{
                    "id": "$breweryId",
                    "name": "Brewery One",
                    "brewery_type": "micro",
                    "address_1": "123 Brew St",
                    "city": "Brew City",
                    "state_province": "BC",
                    "postal_code": "12345",
                    "country": "Country",
                    "phone": "123-456-7890",
                    "website_url": "http://breweryone.com",
                    "state": "State",
                    "street": "123 Brew St"
                }"""
            ).setResponseCode(200)
        )

        // When
        val brewery = repository.getBreweryById(breweryId)

        // Then
        assertEquals(expectedBrewery, brewery)
        val localBrewery = breweryDao.getBreweryById(breweryId)
        assertEquals(expectedBrewery, localBrewery)
    }

 /*   @Test
    fun getBreweriesStream_returnsPagingData() = runTest {
        // Given
        val breweryType = "micro"
        val breweries = listOf(
            Brewery(
                id = "1",
                name = "Brewery One",
                brewery_type = breweryType,
                address_1 = "123 Brew St",
                address_2 = null,
                address_3 = null,
                city = "City",
                state_province = "SP",
                postal_code = "12345",
                country = "Country",
                longitude = null,
                latitude = null,
                phone = null,
                website_url = null,
                state = "State",
                street = "Street"
            ), Brewery(
                id = "2",
                name = "Brewery Two",
                brewery_type = breweryType,
                address_1 = "456 Brew St",
                address_2 = null,
                address_3 = null,
                city = "City",
                state_province = "SP",
                postal_code = "54321",
                country = "Country",
                longitude = null,
                latitude = null,
                phone = null,
                website_url = null,
                state = "State",
                street = "Street"
            )
        )

        // Mock the API and local response
        breweries.forEach { breweryDao.insertBrewery(it) }


        val breweryCacheDao: BreweryCacheDao = mock(BreweryCacheDao::class.java)
        `when`(
            breweryCacheDao.getLastUpdated(anyInt(), anyString())
        ).then {
            0
        }

        val breweryPagingSource = BreweryPagingSource(
            breweryDao = breweryDao,
            breweryCacheDao = breweryCacheDao,
            apiService = breweryService,
            breweryType = "micro"
        )

        // Define parameters for refreshing/loading the initial page
        val params = PagingSource.LoadParams.Refresh(
            key = 0, // Start from the first page
            loadSize = 1, placeholdersEnabled = false
        )

        val actual = breweryPagingSource.load(params)


        // Then
        assertEquals(2, items?.size) // Check if we received two items
        if (items != null) {
            assertTrue(items.containsAll(breweries))
        } // Check if all breweries are included
    }*/
}
