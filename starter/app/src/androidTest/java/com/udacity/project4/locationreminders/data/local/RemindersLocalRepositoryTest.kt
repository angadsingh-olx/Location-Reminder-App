package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    private val testData = ReminderDTO(
        "Title",
        "Description",
        "location",
        0.0, 0.0
    )

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
        .allowMainThreadQueries()
        .build()
        remindersRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @Test
    fun testInsertReminder() = runBlocking {
        remindersRepository.saveReminder(testData)

        val result = remindersRepository.getReminders()
        Assert.assertTrue(result is Result.Success)

        result as Result.Success

        Assert.assertEquals("No items are inserted in the database", 1, result.data.size)
        Assert.assertTrue("The list doesn't contain the inserted item", result.data.contains(testData))
    }

    @Test
    fun testReminderById() = runBlocking {
        remindersRepository.saveReminder(testData)

        val result = remindersRepository.getReminder(testData.id)
        Assert.assertTrue(result is Result.Success)

        result as Result.Success

        val savedData = result.data
        Assert.assertEquals("Titles are not equal", testData.title, savedData.title)
        Assert.assertEquals("Descriptions are not equal", testData.description, savedData.description)
        Assert.assertEquals("Locations are not equal", testData.location, savedData.location)
        Assert.assertEquals("Latitudes are not equal", testData.latitude, savedData.latitude)
        Assert.assertEquals("Longitudes are not equal", testData.longitude, savedData.longitude)
    }

    @Test
    fun testErrorReminderById() = runBlocking {
        val result = remindersRepository.getReminder(testData.id)
        Assert.assertTrue(result is Result.Error)

        result as Result.Error

        Assert.assertEquals("Error message are not equal", "Reminder not found!", result.message)
    }

    @Test
    fun testDeletionAll() = runBlocking {
        remindersRepository.saveReminder(testData)
        remindersRepository.deleteAllReminders()

        val result = remindersRepository.getReminders()
        Assert.assertTrue(result is Result.Success)

        result as Result.Success

        Assert.assertTrue("There are no reminders in the database", result.data.isEmpty())
    }

    @After
    fun clearDatabase() {
        database.clearAllTables()
        database.close()
    }
}