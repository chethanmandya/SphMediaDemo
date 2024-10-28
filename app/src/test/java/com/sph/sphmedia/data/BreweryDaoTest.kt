package com.sph.sphmedia.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sphmedia.data.db.AppDatabase
import com.sphmedia.data.db.BreweryDao
import com.sphmedia.data.model.Brewery
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Test class for BreweryDao, containing tests for database operations related to Brewery.
 */
@Config(sdk = [32])
@RunWith(AndroidJUnit4::class)
class BreweryDaoTest {

    private lateinit var db: AppDatabase // Reference to the database instance
    private lateinit var dao: BreweryDao // Reference to the DAO

    /**
     * Sets up the in-memory database and DAO before each test.
     */
    @Before
    fun setUp() {
        // Create an in-memory Room database for testing purposes
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java // Specify your database class
        ).build()
        // Access the BreweryDao from the database instance
        dao = db.breweryDao()
    }

    /**
     * Closes the database after each test to prevent memory leaks.
     */
    @After
    fun tearDown() {
        db.close() // Close the database
    }

    /**
     * Tests inserting a list of breweries and retrieving them by type with pagination.
     */
    @Test
    fun insertAll_and_getBreweriesPage_shouldReturnInsertedBreweries() = runBlocking {
        // Arrange: Create a list of breweries
        val breweries = listOf(
            Brewery(
                id = "1",
                name = "Brewery One",
                brewery_type = "micro",
                address_1 = "123 Main St",
                address_2 = null,
                address_3 = null,
                city = "City A",
                state_province = "State A",
                postal_code = "12345",
                country = "Country A",
                longitude = "12.34",
                latitude = "56.78",
                phone = "123-456-7890",
                website_url = "http://breweryone.com",
                state = "State A",
                street = "Main St"
            ),
            Brewery(
                id = "2",
                name = "Brewery Two",
                brewery_type = "micro",
                address_1 = "456 Elm St",
                address_2 = null,
                address_3 = null,
                city = "City B",
                state_province = "State B",
                postal_code = "23456",
                country = "Country B",
                longitude = "23.45",
                latitude = "67.89",
                phone = "234-567-8901",
                website_url = "http://brewerytwo.com",
                state = "State B",
                street = "Elm St"
            ),
            Brewery(
                id = "3",
                name = "Brewery Three",
                brewery_type = "nano",
                address_1 = "789 Oak St",
                address_2 = null,
                address_3 = null,
                city = "City C",
                state_province = "State C",
                postal_code = "34567",
                country = "Country C",
                longitude = "34.56",
                latitude = "78.90",
                phone = "345-678-9012",
                website_url = "http://brewerythree.com",
                state = "State C",
                street = "Oak St"
            )
        )

        // Act: Insert the breweries into the database
        dao.insertAll(breweries)

        // Retrieve breweries with pagination (pageSize = 2, offset = 0)
        val result = dao.getBreweriesPage("micro", pageSize = 2, offset = 0)

        // Assert: Verify the retrieved breweries match the inserted ones
        assertEquals(2, result.size) // Should return 2 breweries
        assertEquals("Brewery One", result[0].name) // First brewery
        assertEquals("Brewery Two", result[1].name) // Second brewery
    }

    /**
     * Tests retrieving a single brewery by its ID.
     */
    @Test
    fun insertBrewery_and_getBreweryById_shouldReturnCorrectBrewery() = runBlocking {
        // Arrange: Create a brewery instance
        val brewery = Brewery(
            id = "1",
            name = "Brewery One",
            brewery_type = "micro",
            address_1 = "123 Main St",
            address_2 = null,
            address_3 = null,
            city = "City A",
            state_province = "State A",
            postal_code = "12345",
            country = "Country A",
            longitude = "12.34",
            latitude = "56.78",
            phone = "123-456-7890",
            website_url = "http://breweryone.com",
            state = "State A",
            street = "Main St"
        )

        // Act: Insert the brewery into the database
        dao.insertBrewery(brewery)

        // Retrieve the brewery by ID
        val result = dao.getBreweryById("1")

        // Assert: Verify the retrieved brewery matches the inserted one
        assertEquals("Brewery One", result?.name)
    }

    /**
     * Tests that getting a brewery by a non-existent ID returns null.
     */
    @Test
    fun getBreweryById_withNonExistentId_shouldReturnNull() = runBlocking {
        // Act: Attempt to retrieve a brewery by a non-existent ID
        val result = dao.getBreweryById("unknown")

        // Assert: Verify the result is null
        assertNull(result)
    }

    /**
     * Tests inserting a duplicate brewery and ensuring it updates the existing entry.
     */
    @Test
    fun insert_duplicateBrewery_shouldUpdateExistingEntry() = runBlocking {
        // Arrange: Create a brewery instance
        val brewery = Brewery(
            id = "1",
            name = "Brewery One",
            brewery_type = "micro",
            address_1 = "123 Main St",
            address_2 = null,
            address_3 = null,
            city = "City A",
            state_province = "State A",
            postal_code = "12345",
            country = "Country A",
            longitude = "12.34",
            latitude = "56.78",
            phone = "123-456-7890",
            website_url = "http://breweryone.com",
            state = "State A",
            street = "Main St"
        )

        // Act: Insert the brewery into the database
        dao.insertBrewery(brewery)

        // Insert a duplicate brewery with an updated name
        val updatedBrewery = Brewery(
            id = "1",
            name = "Updated Brewery One",
            brewery_type = "micro",
            address_1 = "123 Main St",
            address_2 = null,
            address_3 = null,
            city = "City A",
            state_province = "State A",
            postal_code = "12345",
            country = "Country A",
            longitude = "12.34",
            latitude = "56.78",
            phone = "123-456-7890",
            website_url = "http://breweryone.com",
            state = "State A",
            street = "Main St"
        )
        dao.insertBrewery(updatedBrewery)

        // Retrieve the brewery by ID
        val result = dao.getBreweryById("1")

        // Assert: Verify the name has been updated
        assertEquals("Updated Brewery One", result?.name)
    }

    /**
     * Tests that retrieving breweries of a different type returns an empty list.
     */
    @Test
    fun getBreweriesPage_withDifferentType_shouldReturnEmptyList() = runBlocking {
        // Arrange: Create a list of breweries of one type
        val breweries = listOf(
            Brewery(
                id = "1",
                name = "Brewery One",
                brewery_type = "micro",
                address_1 = "123 Main St",
                address_2 = null,
                address_3 = null,
                city = "City A",
                state_province = "State A",
                postal_code = "12345",
                country = "Country A",
                longitude = "12.34",
                latitude = "56.78",
                phone = "123-456-7890",
                website_url = "http://breweryone.com",
                state = "State A",
                street = "Main St"
            ),
            Brewery(
                id = "2",
                name = "Brewery Two",
                brewery_type = "micro",
                address_1 = "456 Elm St",
                address_2 = null,
                address_3 = null,
                city = "City B",
                state_province = "State B",
                postal_code = "23456",
                country = "Country B",
                longitude = "23.45",
                latitude = "67.89",
                phone = "234-567-8901",
                website_url = "http://brewerytwo.com",
                state = "State B",
                street = "Elm St"
            )
        )
        // Act: Insert the breweries into the database
        dao.insertAll(breweries)

        // Retrieve breweries of a different type
        val result = dao.getBreweriesPage("brewpub", pageSize = 2, offset = 0)

        // Assert: Verify the retrieved list is empty
        assertEquals(0, result.size)
    }
}
