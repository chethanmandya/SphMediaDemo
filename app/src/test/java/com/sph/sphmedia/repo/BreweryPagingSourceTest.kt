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
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
class BreweryPagingSourceTest {

    private val testScope = TestScope()
    private val testDispatcher = StandardTestDispatcher(testScope.testScheduler)
    private lateinit var breweryDao: BreweryDao
    private lateinit var breweryCacheDao: BreweryCacheDao
    private lateinit var breweryService: BreweryService
    private lateinit var database: AppDatabase
    private lateinit var mockWebServer: MockWebServer

    private val testBrewery = Brewery(
        id = "45b4f628-b1fb-4d61-baf9-29b557e987ad",
        name = "Brockopp Brewing",
        brewery_type = "nano",
        address_1 = "114 Main St E",
        address_2 = null,
        address_3 = null,
        city = "Valley City",
        state_province = "North Dakota",
        postal_code = "58072-3450",
        country = "United States",
        longitude = "-98.00272896",
        latitude = "46.92586281",
        phone = null,
        website_url = "https://www.facebook.com/BrockoppBrewing",
        state = "North Dakota",
        street = "114 Main St E"
    )

    private val testBreweryList = listOf(testBrewery)

    @Before
    fun setUp() {
        // Initialize MockWebServer
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Enqueue a mock response
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("[{\"id\":\"45b4f628-b1fb-4d61-baf9-29b557e987ad\",\"name\":\"Brockopp Brewing\",\"brewery_type\":\"nano\",\"address_1\":\"114 Main St E\",\"address_2\":null,\"address_3\":null,\"city\":\"Valley City\",\"state_province\":\"North Dakota\",\"postal_code\":\"58072-3450\",\"country\":\"United States\",\"longitude\":\"-98.00272896\",\"latitude\":\"46.92586281\",\"phone\":null,\"website_url\":\"https://www.facebook.com/BrockoppBrewing\",\"state\":\"North Dakota\",\"street\":\"114 Main St E\"}]")
        )

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
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // Shutdown MockWebServer
        mockWebServer.shutdown()
        // Close the database
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `when breweries are loaded successfully, returns success load result`() =
        testScope.runTest {
            // given

            val breweryPagingSource = BreweryPagingSource(
                breweryDao = breweryDao,
                breweryCacheDao = breweryCacheDao,
                apiService = breweryService,
                breweryType = "micro"
            )

            val params = PagingSource.LoadParams.Refresh(
                key = 0, // Start from the first page
                loadSize = 1, placeholdersEnabled = false
            )

            val expected = PagingSource.LoadResult.Page(
                data = testBreweryList,
                prevKey = -1,
                nextKey = 1 // Next page key will depend on your pagination logic
            )

            // when
            val actual = breweryPagingSource.load(params)

            // then
            assertEquals(expected, actual)
        }

    @Test
    fun `when breweries loading fails, returns error load result`() = testScope.runTest {
        // Setup MockWebServer to simulate an error response
        val breweryApiService: BreweryService = mock(BreweryService::class.java) // Mock before creating PagingSource

        `when`(
            breweryApiService.getBreweriesByType(anyString(), anyInt(), anyInt())
        ).thenAnswer {
            PagingSource.LoadResult.Error<Int, Brewery>(
                throwable = Throwable("HTTP 500 Server Error")
            )
        }

        // given
        val breweryPagingSource = BreweryPagingSource(
            breweryDao = breweryDao,
            breweryCacheDao = breweryCacheDao,
            apiService = breweryApiService,
            breweryType = "micro"
        )

        val params = PagingSource.LoadParams.Refresh(
            key = 0, // Start from the first page
            loadSize = 1, placeholdersEnabled = false
        )


        // Define the expected load result when an error occurs
        val expected = PagingSource.LoadResult.Error<Int, Brewery>(
            throwable = Throwable("HTTP 500 Server Error") // Adjust to match what your PagingSource throws
        )

        // when
        val actual = breweryPagingSource.load(params)
        // then
        assertTrue(actual is PagingSource.LoadResult.Error)
//        val errorResult = actual as PagingSource.LoadResult.Error
//        assertEquals(expected.throwable.message, errorResult.throwable.message)
    }

    @Test
    fun `when loading next page, returns success append load result`() = testScope.runTest {
        // given
        val breweryPagingSource = BreweryPagingSource(
            breweryDao = breweryDao,
            breweryCacheDao = breweryCacheDao,
            apiService = breweryService,
            breweryType = "micro"
        )
        val params = PagingSource.LoadParams.Append(
            key = 2, // Assuming we are loading the next page
            loadSize = 1, placeholdersEnabled = false
        )

        val expected = PagingSource.LoadResult.Page(
            data = testBreweryList, prevKey = 1, // The previous key based on your logic
            nextKey = 3 // Assuming there is another page
        )

        // when
        val actual = breweryPagingSource.load(params)

        // then
        assertEquals(expected, actual)
    }
}
