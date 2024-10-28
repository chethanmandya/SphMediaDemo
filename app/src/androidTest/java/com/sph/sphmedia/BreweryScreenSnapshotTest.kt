package com.sph.sphmedia

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.paging.PagingData
import com.sph.sphmedia.ui.brewery.BreweryDetailScreen
import com.sph.sphmedia.ui.brewery.BreweryListScreen
import com.sph.sphmedia.ui.brewery.BreweryListViewModel
import com.sphmedia.data.model.Brewery
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(JUnit4::class)
class BreweryScreenSnapshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testBreweryListScreenSnapshot() {
        composeTestRule.setContent {
            BreweryListScreenSnapshotPreview()
        }
        composeTestRule.onNodeWithText("Brockopp Brewing")
            .assertExists() // Modify based on your mock data
        // Use `assertScreenshot` if using a library for capturing snapshots
    }

    @Test
    fun testBreweryDetailScreenSnapshot() {
        composeTestRule.setContent {
            BreweryDetailScreenSnapshotPreview()
        }
        composeTestRule.onNodeWithText("Brewery Detail").assertExists() // Modify as needed
        // Use `assertScreenshot` if using a library for capturing snapshots
    }

    @Composable
    @Preview(showBackground = true)
    fun BreweryListScreenSnapshotPreview() {
        BreweryListScreen(
            navController = mockNavController(), viewModel = mockBreweryListViewModel()
        )
    }

    @Composable
    @Preview(showBackground = true)
    fun BreweryDetailScreenSnapshotPreview() {
        BreweryDetailScreen(
            navController = mockNavController(),
            breweryId = "45b4f628-b1fb-4d61-baf9-29b557e987ad" // Sample breweryId
        )
    }

    private fun mockNavController(): NavController {
        return mock() // Returns a mock NavController
    }

    private fun mockBreweryListViewModel(): BreweryListViewModel {
        val viewModel = mock<BreweryListViewModel>()
        val breweryList = listOf(
            Brewery(
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
            // Add more mock data as needed
        )
        whenever(viewModel.getOrCreatePager("micro")).thenReturn(flowOf(PagingData.from(breweryList))) // Mocking the method call
        return viewModel
    }
}
