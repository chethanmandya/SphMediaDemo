package com.sph.sphmedia.viewmodel

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.sph.sphmedia.ui.brewery.BreweryListViewModel
import com.sphmedia.data.model.Brewery
import com.sphmedia.domain.repository.BreweryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
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
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class
BreweryListViewModelTest {

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

    private val noopListUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Brewery>() {
        override fun areItemsTheSame(oldItem: Brewery, newItem: Brewery): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Note that in kotlin, == checking on data classes compares all contents, but in Java,
         * typically you'll implement Object#equals, and use it to compare object contents.
         */
        override fun areContentsTheSame(oldItem: Brewery, newItem: Brewery): Boolean {
            return oldItem.name == newItem.name
        }
    }


    /**
     * This test case will validate - getOrCreatePager on BreweryListViewModel
     *
     * Test for below key aspects :
     * - The correct PagingData is provided when getOrCreatePager is called with a specified breweryType.
     * - The AsyncPagingDataDiffer properly processes and captures the items in the PagingData as expected.
     * - The data output (in differ.snapshot().items) matches the mock data input, ensuring data consistency.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun verifyPagerCreationAndDataRetrieval() = runTest {
        // Given: Define a brewery type to use for the test
        val breweryType = "micro"

        // Mock data representing a single brewery of the specified type "micro"
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

        // Create a PagingData instance from the mock data
        val pagingData: PagingData<Brewery> = PagingData.from(listOf(breweryMock))

        // Stub the repository method to return a flow of PagingData when called with breweryType
        `when`(breweryRepository.getBreweriesStream(breweryType)).thenReturn(flowOf(pagingData))

        // Create an AsyncPagingDataDiffer to help collect PagingData items
        val differ = AsyncPagingDataDiffer(
            diffCallback = diffCallback,
            updateCallback = noopListUpdateCallback,
            mainDispatcher = testDispatcher, // Set to test dispatcher for main thread
            workerDispatcher = testDispatcher, // Set to test dispatcher for background work
        )

        // Launch a job to collect paging data using the view model's pager function
        val job = launch {
            // Collects paging data for the specified brewery type and submits it to the differ
            viewModel.getOrCreatePager(breweryType).collectLatest { pagingData ->
                differ.submitData(pagingData)
            }
        }

        // Wait until all currently queued tasks are complete, simulating initial load completion
        advanceUntilIdle()

        // Verify that the items collected by the differ match the expected list of mock items
        assertEquals(
            listOf(breweryMock), differ.snapshot().items
        )

        // Manually cancel the job to release resources as collectLatest is an infinite stream
        job.cancel()
    }


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
        `when`(breweryRepository.getBreweriesStream(breweryTypeMicro)).thenReturn(
            flowOf(
                pagingDataMicro
            )
        )
        `when`(breweryRepository.getBreweriesStream(breweryTypeBrewpub)).thenReturn(
            flowOf(
                pagingDataBrewpub
            )
        )

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
