package com.sph.sphmedia.ui.brewery

import androidx.paging.PagingData
import androidx.paging.map
import com.sph.sphmedia.toList
import com.sphmedia.data.model.Brewery
import com.sphmedia.domain.repository.BreweryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class BreweryListViewModelTest {

    private lateinit var viewModel: BreweryListViewModel
    private lateinit var breweryRepository: BreweryRepository
    private val testDispatcher = StandardTestDispatcher() // Dispatcher for testing
    private lateinit var testScope: TestScope // Scope for running test coroutines

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // Set the test dispatcher
        breweryRepository = mock(BreweryRepository::class.java) // Mock the BreweryRepository
        viewModel = BreweryListViewModel(breweryRepository) // Instantiate the ViewModel
        testScope = TestScope(testDispatcher)
    }

    @After
    fun tearDown() {
        // Clean up any resources if needed
        Dispatchers.resetMain() // Reset the dispatcher
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getOrCreatePager_createsNewPager() = runTest {
        // Given
        val breweryType = "micro"

        val breweryMock = Brewery(
            id = "1",
            name = "Brewery One",
            brewery_type = breweryType,
            address_1 = "123 Main St",
            address_2 = null,
            address_3 = null,
            city = "Brew City",
            state_province = "Brew State",
            postal_code = "12345",
            country = "USA",
            longitude = "-123.456",
            latitude = "12.345",
            phone = "123-456-7890",
            website_url = "http://breweryone.com",
            state = "CA",
            street = "Brewery St"
        )
        val pagingData: PagingData<Brewery> = PagingData.from(listOf(breweryMock))

        `when`(breweryRepository.getBreweriesStream(breweryType)).thenReturn(flowOf(pagingData))

        // When
        val resultList = mutableListOf<Brewery>()
        val result = viewModel.getOrCreatePager(breweryType)


        // Launch collectLatest in a separate Job
        val collectJob = launch {
            result.collectLatest { paging ->
                paging.map { brewery -> resultList.add(brewery) }
            }
        }

        val expectedList = pagingData.toList()
        advanceUntilIdle() // Process all pending coroutine tasks

        // Cancel the collectLatest job
        collectJob.cancel()


        // Then
        assertNotNull(result)
        assertEquals(resultList, expectedList) // Check collected PagingData
        verify(breweryRepository).getBreweriesStream(breweryType)
    }


    @Test
    fun getOrCreatePager_returnsCachedPager() = runTest(testDispatcher) {
        // Given
        val breweryType = "micro" // Define the brewery type
        // Create mock Brewery data for testing
        val breweryMock = Brewery(
            id = "1",
            name = "Brewery One",
            brewery_type = breweryType,
            address_1 = "123 Main St",
            address_2 = null,
            address_3 = null,
            city = "Brew City",
            state_province = "Brew State",
            postal_code = "12345",
            country = "USA",
            longitude = "-123.456",
            latitude = "12.345",
            phone = "123-456-7890",
            website_url = "http://breweryone.com",
            state = "CA",
            street = "Brewery St"
        )
        val pagingData = PagingData.from(listOf(breweryMock)) // Mock PagingData with one brewery

        // Mock the repository to return a Flow of PagingData
        `when`(breweryRepository.getBreweriesStream(breweryType)).thenReturn(flowOf(pagingData))

        // Call it once to create the pager
        viewModel.getOrCreatePager(breweryType)

        // When called again with the same breweryType
        val cachedResult = viewModel.getOrCreatePager(breweryType)

        // Then
        assertNotNull(cachedResult) // Ensure the cached result is not null
        assertEquals(
            pagingData.toList(), cachedResult.first().toList()
        ) // Verify the cached PagingData is correct
        // Verify that the repository was not called again
        verify(breweryRepository, times(1)).getBreweriesStream(breweryType)
    }

    @Test
    fun getOrCreatePager_differentTypes_returnSeparatePagers() = runTest(testDispatcher) {
        // Given
        val breweryType1 = "micro" // Define the first brewery type
        val breweryType2 = "brewpub" // Define the second brewery type

        // Create mock data for each type
        val breweryMock1 = Brewery(
            id = "1",
            name = "Brewery One",
            brewery_type = breweryType1,
            address_1 = "123 Main St",
            address_2 = null,
            address_3 = null,
            city = "Brew City",
            state_province = "Brew State",
            postal_code = "12345",
            country = "USA",
            longitude = "-123.456",
            latitude = "12.345",
            phone = "123-456-7890",
            website_url = "http://breweryone.com",
            state = "CA",
            street = "Brewery St"
        )
        val pagingData1 = PagingData.from(listOf(breweryMock1))

        val breweryMock2 = Brewery(
            id = "2",
            name = "Brewery Two",
            brewery_type = breweryType2,
            address_1 = "456 Elm St",
            address_2 = null,
            address_3 = null,
            city = "Brew Town",
            state_province = "Brew State",
            postal_code = "54321",
            country = "USA",
            longitude = "-124.567",
            latitude = "13.456",
            phone = "987-654-3210",
            website_url = "http://brewerytwo.com",
            state = "CA",
            street = "Brewery Ave"
        )
        val pagingData2 = PagingData.from(listOf(breweryMock2))

        // Mock the repository to return different PagingData for different types
        `when`(breweryRepository.getBreweriesStream(breweryType1)).thenReturn(flowOf(pagingData1))
        `when`(breweryRepository.getBreweriesStream(breweryType2)).thenReturn(flowOf(pagingData2))

        // When
        val result1 = viewModel.getOrCreatePager(breweryType1) // Get pager for the first type
        val result2 = viewModel.getOrCreatePager(breweryType2) // Get pager for the second type

        // Then
        assertNotNull(result1) // Ensure the result for type 1 is not null
        assertNotNull(result2) // Ensure the result for type 2 is not null
        assertEquals(pagingData1, result1.first()) // Verify the result for type 1
        assertEquals(pagingData2, result2.first()) // Verify the result for type 2

        // Verify that the repository was called for both types
        verify(breweryRepository).getBreweriesStream(breweryType1)
        verify(breweryRepository).getBreweriesStream(breweryType2)
    }
}
