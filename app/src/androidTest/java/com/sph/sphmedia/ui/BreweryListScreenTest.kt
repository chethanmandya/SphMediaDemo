package com.sph.sphmedia.ui


import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sph.sphmedia.AppNavHost
import com.sph.sphmedia.MainActivity
import com.sph.sphmedia.ui.brewery.BreweryDetailScreen
import com.sph.sphmedia.ui.brewery.BreweryListScreen
import com.sph.sphmedia.ui.brewery.BreweryListViewModel
import com.sphmedia.common.MainDestinations
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class BreweryListTests {


    @get:Rule(order = 1)
    var hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeTestRule = createAndroidComposeRule<MainActivity>()


    lateinit var navController: TestNavHostController

    @Before
    fun setup() {

        hiltTestRule.inject()

        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            BreweryListScreen(
                composeTestRule.activity.viewModels<BreweryListViewModel>().value
            ) {

            }
        }


    }

    @Test
    fun screen_displaysBreweryTabs_ignoreCase() {
        val tabLabels = listOf(
            "Micro",
            "Nano",
            "Regional",
            "Large",
            "Planning",
            "Bar",
            "Contract",
            "Proprietor",
            "Closed"
        )

        tabLabels.forEach { label ->
            // Check if at least one node matches the label, ignoring case
            composeTestRule.onAllNodesWithText(label, substring = true)
                .filter(hasText(label, ignoreCase = true))
                .assertAny(hasText(label, ignoreCase = true))
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun click_each_tab() {
        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            AppNavHost(navController = navController)
        }


        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("name_of_the_brewery_micro_2"), 3000)

        // Now that the node is found and visible, perform a click
        composeTestRule.onNode(hasTestTag("name_of_the_brewery_micro_2")).performClick()

        // Check if navigated to the detail screen by asserting an element that should exist there

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("name_of_the_brewery_detail"), 3000)

        // Optionally, assert the expected content in the detail screen
        composeTestRule.onNode(hasTestTag("name_of_the_brewery_detail")).assertExists()
    }


    @Test
    fun clickGridItem_navigateToDetail_verifyName() {
        composeTestRule.activity.setContent {
            BreweryListScreen(
                composeTestRule.activity.viewModels<BreweryListViewModel>().value
            ) {

            }
        }

        // Introduce a delay
        Thread.sleep(1500) // Wait for 1500 milliseconds

        // Fetch the unmerged node
        val node1 = composeTestRule.onNode(
            hasTestTag("name_of_the_brewery_micro_2") and SemanticsMatcher.keyIsDefined(
                SemanticsProperties.Text
            )
        ).fetchSemanticsNode()

        // Read the text value from the semantics node
        val expectedBreweryName = node1.config.getOrNull(SemanticsProperties.Text)

        // Introduce a delay
        Thread.sleep(500) // Wait for 500 milliseconds

        // Move on to detail screen
        composeTestRule.onNode(
            hasTestTag("name_of_the_brewery") // Replace with your actual tag
        ).performClick()

        // Introduce a delay
        Thread.sleep(500) // Wait for 500 milliseconds

        // Wait for navigation and verify name on detail screen
        val node2 = composeTestRule.onNode(
            hasTestTag("name_of_the_brewery") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)
        ).fetchSemanticsNode()

        // Introduce a delay
        Thread.sleep(500) // Wait for 500 milliseconds

        val actualBreweryName = node2.config.getOrNull(SemanticsProperties.Text)

        // Assert that the actual name matches the expected name
        assertEquals(expectedBreweryName, actualBreweryName)
    }
}




//@Module
//@InstallIn(SingletonComponent::class)
//object TestBreweryModule {
//
//    @Provides
//    fun provideTestBreweryRepository(): BreweryRepository {
//        // Return a mocked or fake BreweryRepository here
//        return FakeBreweryRepository()
//    }
//}

