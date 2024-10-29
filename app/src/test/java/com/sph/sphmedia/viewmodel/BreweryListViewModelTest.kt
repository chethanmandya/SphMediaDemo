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



 /*   @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createNewPagerForBreweryType_andVerifyPagingData() = runTest {
        // Given: Define a brewery type for testing
        val breweryType = "micro"

        // Mock data for a brewery of type "micro"
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

        // Stub the repository to return paging data flow for the specified brewery type
        `when`(breweryRepository.getBreweriesStream(breweryType)).thenReturn(flowOf(pagingData))

        // When: Call getOrCreatePager to create or retrieve a pager for the specified brewery type
        val resultPager = viewModel.getOrCreatePager(breweryType)

        // Prepare a list to hold the collected items
        val collectedItems = mutableListOf<Brewery>()

        // Collect items from PagingData
        val job = launch {
            resultPager.collectLatest { pagingData ->
                pagingData.collect { brewery ->
                    collectedItems.add(brewery)
                }
            }
        }

        advanceUntilIdle() // Process all pending coroutine tasks

        // Then: Verify that the collected data matches the expected mock data
        assertNotNull(resultPager)
        assertEquals(listOf(breweryMock), collectedItems) // Check that the collected items match the expected mock list

        // Confirm that the repository was called with the specified brewery type
        verify(breweryRepository).getBreweriesStream(breweryType)

        // Cancel the collection job to avoid test leaks
        job.cancel()
    }*/


    @Test
    fun createSeparatePagersForDifferentBreweryTypes() = runTest(testDispatcher) {
        // Given: Define two different brewery types to test
        val breweryTypeMicro = "micro"
        val breweryTypeBrewpub = "brewpub"

        // Create mock brewery data for each type
        val breweryMockMicro = Brewery(
            id = "1",
            name = "Brewery One",
            brewery_type = breweryTypeMicro,
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
        val pagingDataMicro = PagingData.from(listOf(breweryMockMicro))

        val breweryMockBrewpub = Brewery(
            id = "2",
            name = "Brewery Two",
            brewery_type = breweryTypeBrewpub,
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
        val pagingDataBrewpub = PagingData.from(listOf(breweryMockBrewpub))

        // Mock the repository to return different PagingData flows for each brewery type
        `when`(breweryRepository.getBreweriesStream(breweryTypeMicro)).thenReturn(flowOf(pagingDataMicro))
        `when`(breweryRepository.getBreweriesStream(breweryTypeBrewpub)).thenReturn(flowOf(pagingDataBrewpub))

        // When: Call getOrCreatePager to create or retrieve pagers for each type
        val pagerForMicro = viewModel.getOrCreatePager(breweryTypeMicro)
        val pagerForBrewpub = viewModel.getOrCreatePager(breweryTypeBrewpub)

        // Then: Verify both pagers are not null, meaning they were created successfully
        assertNotNull(pagerForMicro) // Pager for "micro" type should not be null
        assertNotNull(pagerForBrewpub) // Pager for "brewpub" type should not be null

        // Verify that the repository was called with each brewery type, confirming it fetched data for both types
        verify(breweryRepository).getBreweriesStream(breweryTypeMicro)
        verify(breweryRepository).getBreweriesStream(breweryTypeBrewpub)
    }

}
