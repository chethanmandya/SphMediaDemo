package com.sph.sphmedia.ui


import androidx.activity.compose.setContent
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
import androidx.compose.ui.test.performScrollToIndex
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sph.sphmedia.AppNavHost
import com.sph.sphmedia.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
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

    }

    @Test
    fun check_for_tabs_are_displayed_ignoreCase() {

        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            AppNavHost(navController = navController)
        }

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
    fun click_to_navigate_detail_screen() {
        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            AppNavHost(navController = navController)
        }


        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("name_of_the_brewery_micro_2"), 10000)

        // Now that the node is found and visible, perform a click
        composeTestRule.onNode(hasTestTag("name_of_the_brewery_micro_2")).performClick()

        // Check if navigated to the detail screen by asserting an element that should exist there
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("name_of_the_brewery_detail"), 10000)

        // Optionally, assert the expected content in the detail screen
        composeTestRule.onNode(hasTestTag("name_of_the_brewery_detail")).assertExists()
    }


    @OptIn(ExperimentalTestApi::class)
    @Test
    fun click_lazy_gridItem_to_navigate_to_detail_screen_verifyName() {
        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            AppNavHost(navController = navController)
        }

        // Wait until the node is available
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("name_of_the_brewery_micro_3"), 10000)

        // Fetch the unmerged node once it is available
        val node1 = composeTestRule.onNode(
            hasTestTag("name_of_the_brewery_micro_3") and SemanticsMatcher.keyIsDefined(
                SemanticsProperties.Text
            )
        ).fetchSemanticsNode()

        // Read the text value from the semantics node
        val expectedBreweryName = node1.config.getOrNull(SemanticsProperties.Text)

        // Move on to detail screen
        composeTestRule.onNode(
            hasTestTag("name_of_the_brewery_micro_3") // You may want to click the same node or another
        ).performClick()

        // Wait for navigation and verify name on detail screen
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("name_of_the_brewery_detail"), 10000)

        //before fetching check the field is existing
        composeTestRule.onNode(hasTestTag("name_of_the_brewery_detail")).assertExists()


        // Fetch the node on the detail screen
        val node2 = composeTestRule.onNode(
            hasTestTag("name_of_the_brewery_detail")
        ).fetchSemanticsNode()


        // Read the actual name from the detail screen
        val actualBreweryName =
            node2.children.map { it.config.getOrNull(SemanticsProperties.Text)?.first()?.text }

        // Comparing the title
        assertEquals(expectedBreweryName?.first().toString(), actualBreweryName.first().toString())

    }


    @OptIn(ExperimentalTestApi::class)
    @Test
    fun scroll_lazy_grid_and_check_for_loading_progressBar() {
        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            AppNavHost(navController = navController)
        }

        // Wait until the LazyVerticalGrid is available
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("name_of_the_brewery_micro_3"), 10000)

        // Identify the LazyVerticalGrid node
        val gridNode = composeTestRule.onNode(hasTestTag("LazyVerticalGrid_micro"))

        // Set initial values for scrolling and checking
        var isProgressBarVisible = false
        var scrollIndex = 0

        // Loop to scroll and check for the progress bar
        while (!isProgressBarVisible) {
            // Scroll to the next index
            gridNode.performScrollToIndex(scrollIndex)

            // Check if the progress bar is now visible
            // Check if the progress bar is now visible
            isProgressBarVisible = try {
                composeTestRule.onNode(hasTestTag("circularProgressBar"))
                    .assertExists(errorMessageOnFail = "Progress bar not found.")
                true
            } catch (e: AssertionError) {
                false
            }

            // Increment the index for the next scroll attempt
            scrollIndex++
        }

        // Assert that the progress bar was found and is displayed
        assertTrue(
            "Circular progress bar was not displayed after scrolling.", isProgressBarVisible
        )
    }


}




