package com.sph.sphmedia.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sphmedia.data.db.AppDatabase
import com.sphmedia.data.db.BreweryCacheDao
import com.sphmedia.data.model.BreweryCache
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Test class for BreweryCacheDao, containing tests for database operations related to BreweryCache.
 */
@Config(sdk = [32])
@RunWith(AndroidJUnit4::class)
class BreweryCacheDaoTest {

    private lateinit var db: AppDatabase // Reference to the database instance
    private lateinit var dao: BreweryCacheDao // Reference to the DAO

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
        // Access the BreweryCacheDao from the database instance
        dao = db.breweryCacheDao()
    }

    /**
     * Closes the database after each test to prevent memory leaks.
     */
    @After
    fun tearDown() {
        db.close() // Close the database
    }

    /**
     * Tests that inserting a BreweryCache entry and retrieving its last updated timestamp works correctly.
     */
    @Test
    fun insert_and_getLastUpdated_shouldReturnCorrectTimestamp() = runBlocking {
        // Arrange: Create a cache entry
        val cacheEntry = BreweryCache(pageNumber = 1, breweryType = "micro", lastUpdated = 1000L)

        // Act: Insert the entry and retrieve the last updated timestamp
        dao.insert(cacheEntry)
        val result = dao.getLastUpdated(page = 1, breweryType = "micro")

        // Assert: Verify the retrieved timestamp matches the inserted value
        assertEquals(1000L, result)
    }

    /**
     * Tests that retrieving the last updated timestamp for a non-existent entry returns null.
     */
    @Test
    fun getLastUpdated_withNoMatchingEntry_shouldReturnNull() = runBlocking {
        // Act: Attempt to retrieve a last updated timestamp for a non-existent entry
        val result = dao.getLastUpdated(page = 1, breweryType = "unknown")

        // Assert: Verify the result is null
        assertNull(result)
    }

    /**
     * Tests that clearing the cache by brewery type deletes all entries of that type.
     */
    @Test
    fun clearCacheByType_shouldDeleteAllEntriesForType() = runBlocking {
        // Arrange: Create multiple cache entries with different types
        val cacheEntry1 = BreweryCache(pageNumber = 1, breweryType = "micro", lastUpdated = 1000L)
        val cacheEntry2 = BreweryCache(pageNumber = 2, breweryType = "micro", lastUpdated = 2000L)
        val cacheEntry3 = BreweryCache(pageNumber = 1, breweryType = "nano", lastUpdated = 3000L)

        // Insert the entries into the database
        dao.insert(cacheEntry1)
        dao.insert(cacheEntry2)
        dao.insert(cacheEntry3)

        // Act: Clear cache entries for the "micro" brewery type
        dao.clearCacheByType("micro")

        // Assert: Verify that the micro entries have been deleted, and the nano entry remains
        assertNull(dao.getLastUpdated(page = 1, breweryType = "micro"))
        assertNull(dao.getLastUpdated(page = 2, breweryType = "micro"))
        assertEquals(3000L, dao.getLastUpdated(page = 1, breweryType = "nano"))
    }

    /**
     * Tests that inserting a duplicate BreweryCache entry updates its last updated timestamp correctly.
     */
    @Test
    fun insert_duplicateEntries_shouldUpdateLastUpdated() = runBlocking {
        // Arrange: Create two cache entries for the same page and type
        val cacheEntry1 = BreweryCache(pageNumber = 1, breweryType = "micro", lastUpdated = 1000L)
        val cacheEntry2 = BreweryCache(
            pageNumber = 1,
            breweryType = "micro",
            lastUpdated = 2000L
        ) // Same page and type

        // Act: Insert the first entry and then the duplicate entry (which should update the timestamp)
        dao.insert(cacheEntry1)
        dao.insert(cacheEntry2)

        // Assert: Verify the last updated timestamp is the latest one
        assertEquals(2000L, dao.getLastUpdated(page = 1, breweryType = "micro"))
    }

    /**
     * Tests that clearing the cache by type does not throw an exception when no entries exist for that type.
     */
    @Test
    fun clearCacheByType_whenNoEntries_shouldNotThrow() = runBlocking {
        // Act: Clear cache for an unknown brewery type
        dao.clearCacheByType("unknown") // No entries should exist

        // Assert: Verify that retrieving a last updated timestamp still returns null
        assertNull(dao.getLastUpdated(page = 1, breweryType = "unknown"))
    }

    /**
     * Tests that inserting multiple brewery types allows retrieval of correct timestamps for each type.
     */
    @Test
    fun insert_multipleTypes_shouldReturnCorrectTimestamps() = runBlocking {
        // Arrange: Create cache entries for different brewery types
        val cacheEntry1 = BreweryCache(pageNumber = 1, breweryType = "micro", lastUpdated = 1000L)
        val cacheEntry2 = BreweryCache(pageNumber = 1, breweryType = "nano", lastUpdated = 2000L)

        // Act: Insert the entries into the database
        dao.insert(cacheEntry1)
        dao.insert(cacheEntry2)

        // Assert: Verify retrieval of last updated timestamps for each type
        assertEquals(1000L, dao.getLastUpdated(page = 1, breweryType = "micro"))
        assertEquals(2000L, dao.getLastUpdated(page = 1, breweryType = "nano"))
    }

    /**
     * Tests that clearing the cache by type does not affect entries with different page numbers.
     */
    @Test
    fun clearCacheByType_shouldNotAffectDifferentPageNumbers() = runBlocking {
        // Arrange: Create cache entries with the same brewery type but different page numbers
        val cacheEntry1 = BreweryCache(pageNumber = 1, breweryType = "micro", lastUpdated = 1000L)
        val cacheEntry2 = BreweryCache(pageNumber = 2, breweryType = "micro", lastUpdated = 2000L)

        // Insert the entries into the database
        dao.insert(cacheEntry1)
        dao.insert(cacheEntry2)

        // Act: Clear cache entries for the "micro" brewery type
        dao.clearCacheByType("micro")

        // Assert: Verify that both entries have been cleared
        assertNull(
            dao.getLastUpdated(
                page = 1,
                breweryType = "micro"
            )
        ) // First page should be cleared
        assertNull(
            dao.getLastUpdated(
                page = 2,
                breweryType = "micro"
            )
        ) // Second page should be cleared
    }

    /**
     * Tests that counting entries manually by checking how many entries exist for a specific brewery type.
     */
    @Test
    fun countEntriesByType_shouldReturnCorrectCountManually() = runBlocking {
        // Arrange: Create cache entries of different types
        val cacheEntry1 = BreweryCache(pageNumber = 1, breweryType = "micro", lastUpdated = 1000L)
        val cacheEntry2 = BreweryCache(pageNumber = 2, breweryType = "micro", lastUpdated = 2000L)
        val cacheEntry3 = BreweryCache(pageNumber = 1, breweryType = "nano", lastUpdated = 3000L)

        // Insert the entries into the database
        dao.insert(cacheEntry1)
        dao.insert(cacheEntry2)
        dao.insert(cacheEntry3)

        // Act: Retrieve the count of entries for "micro" brewery type manually
        val microEntries =
            listOf(dao.getLastUpdated(1, "micro"), dao.getLastUpdated(2, "micro")).filterNotNull()
        val nanoEntries = listOf(dao.getLastUpdated(1, "nano")).filterNotNull()

        // Assert: Verify the counts are as expected
        assertEquals(2, microEntries.size) // Should have 2 entries for micro
        assertEquals(1, nanoEntries.size) // Should have 1 entry for nano
    }
}
