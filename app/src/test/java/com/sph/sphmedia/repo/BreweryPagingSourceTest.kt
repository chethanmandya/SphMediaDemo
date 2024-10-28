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
import com.sphmedia.data.model.BreweryCache
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
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.notNull
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class BreweryPagingSourceTest {

    // Test coroutine scope and dispatcher
    private val testScope = TestScope()
    private val testDispatcher = StandardTestDispatcher(testScope.testScheduler)

    // Late-initialized database and DAO components
    private lateinit var breweryDao: BreweryDao
    private lateinit var breweryCacheDao: BreweryCacheDao
    private lateinit var breweryService: BreweryService
    private lateinit var database: AppDatabase
    private lateinit var mockWebServer: MockWebServer

    // Mock brewery data used for testing
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

    // List of breweries for load results
    private val testBreweryList = listOf(testBrewery)

    @Before
    fun setUp() {
        // Initialize MockWebServer to mock HTTP requests
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Enqueue a mock HTTP response
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                "[{\"id\":\"45b4f628-b1fb-4d61-baf9-29b557e987ad\"," +
                        "\"name\":\"Brockopp Brewing\",\"brewery_type\":\"nano\"," +
                        "\"address_1\":\"114 Main St E\",\"city\":\"Valley City\"," +
                        "\"state_province\":\"North Dakota\",\"postal_code\":\"58072-3450\"," +
                        "\"country\":\"United States\",\"longitude\":\"-98.00272896\"," +
                        "\"latitude\":\"46.92586281\"," +
                        "\"website_url\":\"https://www.facebook.com/BrockoppBrewing\"," +
                        "\"state\":\"North Dakota\",\"street\":\"114 Main St E\"}]"
            )
        )

        // Set up Room database in memory for testing DAOs
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()

        breweryDao = database.breweryDao()
        breweryCacheDao = database.breweryCacheDao()

        // Set up Moshi with JSON adapter for Kotlin to parse responses
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        // Set up Retrofit for API service with MockWebServer base URL
        breweryService = Retrofit.Builder().baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi)).build()
            .create(BreweryService::class.java)

        // Set main dispatcher to test dispatcher
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // Shut down MockWebServer to release resources
        mockWebServer.shutdown()
        // Close the in-memory database
        database.close()
        // Reset the main dispatcher to the original
        Dispatchers.resetMain()
    }

    @Test
    fun `when breweries are loaded successfully, returns success load result`() = testScope.runTest {
        // Create an instance of BreweryPagingSource for pagination
        val breweryPagingSource = BreweryPagingSource(
            breweryDao = breweryDao,
            breweryCacheDao = breweryCacheDao,
            apiService = breweryService,
            breweryType = "micro"
        )

        // Define loading parameters for the test, setting page key to 0 and load size to 1
        val params = PagingSource.LoadParams.Refresh(
            key = 0, // Start from the first page
            loadSize = 1, placeholdersEnabled = false
        )

        // Expected load result with data containing the test brewery list
        val expected = PagingSource.LoadResult.Page(
            data = testBreweryList,
            prevKey = -1,
            nextKey = 1 // Key for the next page, depending on pagination logic
        )

        // Load the data using the paging source and check result
        val actual = breweryPagingSource.load(params)
        assertEquals(expected, actual)
    }

    @Test
    fun `when breweries loading fails, returns error load result`() = testScope.runTest {
        // Mock API service to simulate a network failure or error response
        val breweryApiService: BreweryService = mock(BreweryService::class.java)
        `when`(
            breweryApiService.getBreweriesByType(anyString(), anyInt(), anyInt())
        ).thenAnswer {
            PagingSource.LoadResult.Error<Int, Brewery>(
                throwable = Throwable("HTTP 500 Server Error")
            )
        }

        // Initialize BreweryPagingSource with mocked API service
        val breweryPagingSource = BreweryPagingSource(
            breweryDao = breweryDao,
            breweryCacheDao = breweryCacheDao,
            apiService = breweryApiService,
            breweryType = "micro"
        )

        // Define parameters for refreshing/loading the initial page
        val params = PagingSource.LoadParams.Refresh(
            key = 0, // Start from the first page
            loadSize = 1, placeholdersEnabled = false
        )

        // Expected error load result when an error occurs
        val expected = PagingSource.LoadResult.Error<Int, Brewery>(
            throwable = Throwable("HTTP 500 Server Error")
        )

        // Attempt to load the data and assert the result is an error
        val actual = breweryPagingSource.load(params)
        assertTrue(actual is PagingSource.LoadResult.Error)
    }

    @Test
    fun `when loading next page, returns success append load result`() = testScope.runTest {
        // Initialize BreweryPagingSource for testing page append behavior
        val breweryPagingSource = BreweryPagingSource(
            breweryDao = breweryDao,
            breweryCacheDao = breweryCacheDao,
            apiService = breweryService,
            breweryType = "micro"
        )

        // Define loading parameters for appending the next page
        val params = PagingSource.LoadParams.Append(
            key = 2, // Key for the next page
            loadSize = 1, placeholdersEnabled = false
        )

        // Expected page load result with test brewery list
        val expected = PagingSource.LoadResult.Page(
            data = testBreweryList,
            prevKey = 1, // Previous page key
            nextKey = 3 // Next page key for further pagination
        )

        // Load the next page and verify the result matches expectations
        val actual = breweryPagingSource.load(params)
        assertEquals(expected, actual)
    }



    /**
     * Test 1: Verifies that when the cache is expired, data is fetched from the network.
     */
    @Test
    fun `when cache is expired, data is fetched from network`() = runTest {
        val page = 1
        val breweryType = "micro"
        val currentTime = System.currentTimeMillis()
        val expiredTime = currentTime - TimeUnit.MINUTES.toMillis(10) // Simulate cache expiration

        val testBreweryList = listOf(testBrewery)

        val breweryCacheDao: BreweryCacheDao = mock(BreweryCacheDao::class.java)
        val breweryDao: BreweryDao = mock(BreweryDao::class.java)

        // Mock an expired cache scenario
        `when`(breweryCacheDao.getLastUpdated(anyInt(), anyString())).thenReturn(expiredTime)
        // Mock empty database response to indicate no valid cached data
        `when`(breweryDao.getBreweriesPage(anyString(), anyInt(), anyInt())).thenReturn(emptyList())

        // Initialize BreweryPagingSource for testing page append behavior
        val breweryPagingSource = BreweryPagingSource(
            breweryDao = breweryDao,
            breweryCacheDao = breweryCacheDao,
            apiService = breweryService,
            breweryType = "micro"
        )
        // Load the data from the PagingSource, below this code simulate breweryPagingSource, above two `when` condition execute during this time
        val params = PagingSource.LoadParams.Refresh(key = page, loadSize = 20, placeholdersEnabled = false)
        val result = breweryPagingSource.load(params)

        // Verify that data was fetched from network and stored in the database, since it is network call due expireTime, we are verifying cache is inserted
        val cacheCaptor = argumentCaptor<BreweryCache>()
        verify(breweryCacheDao).insert(cacheCaptor.capture())
        // verifying network response is inserted
        verify(breweryDao).insertAll(testBreweryList)

        // Check that the result matches expected values
        val expected = PagingSource.LoadResult.Page(
            data = testBreweryList,
            prevKey = null,
            nextKey = page + 1
        )
        assertEquals(expected, result)
    }

    /**
     * Test 2: Verifies that when the cache is valid, data is retrieved from the database without a network call.
     */
    @Test
    fun `when cache is valid, data is fetched from database`() = runTest {
        val page = 1
        val breweryType = "micro"
        val currentTime = System.currentTimeMillis()

        val testBreweryList = listOf(testBrewery)


        val breweryCacheDao: BreweryCacheDao = mock(BreweryCacheDao::class.java)
        val breweryDao: BreweryDao = mock(BreweryDao::class.java)

        // Simulate a valid cache that is not expired
        `when`(breweryCacheDao.getLastUpdated(page, breweryType)).thenReturn(currentTime)
        // Return a non-empty list from the database as a valid brewery response
        `when`(breweryDao.getBreweriesPage(anyString(), anyInt(), anyInt())).thenReturn(testBreweryList)

        // Initialize BreweryPagingSource for testing page append behavior
        val breweryPagingSource = BreweryPagingSource(
            breweryDao = breweryDao,
            breweryCacheDao = breweryCacheDao,
            apiService = breweryService,
            breweryType = "micro"
        )

        // Load the data from the PagingSource,below this code simulate breweryPagingSource, above two `when` condition execute during this time
        val params = PagingSource.LoadParams.Refresh(key = page, loadSize = 1, placeholdersEnabled = false)
        val result = breweryPagingSource.load(params)

        // Verify that no network call was made and data was fetched from the database
        val cacheCaptor = argumentCaptor<BreweryCache>()
        //cacheCaptor.capture() ignore what value inserted, only look for whether it is inserted or not
        verify(breweryCacheDao, never()).insert(cacheCaptor.capture())
        verify(breweryDao, never()).insertAll(anyList())

        // Check that the result matches expected values
        val expected = PagingSource.LoadResult.Page(
            data = testBreweryList,
            prevKey = null,
            nextKey = page + 1
        )
        assertEquals(expected, result)
    }

}
