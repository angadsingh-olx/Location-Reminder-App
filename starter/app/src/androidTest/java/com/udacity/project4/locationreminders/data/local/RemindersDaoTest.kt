package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDao: RemindersDao
    private lateinit var database: RemindersDatabase

    private val testData = ReminderDTO(
        "Title",
        "Description",
        "location",
        0.0, 0.0
    )

    @Before
    fun createDatabase() {
        database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java, "locationReminders.db"
        )
        .allowMainThreadQueries()
        .build()
        remindersDao = database.reminderDao()
    }

    @Test
    fun testInsertion() = runBlockingTest{
        val sizeBeforeInsertion = remindersDao.getReminders().size
        remindersDao.saveReminder(testData)

        val dataAfterInsertion = remindersDao.getReminders()
        Assert.assertEquals("No items are inserted in the database", sizeBeforeInsertion + 1, dataAfterInsertion.size)
        Assert.assertTrue("The list doesn't contain the inserted item", dataAfterInsertion.contains(testData))
    }

    @Test
    fun testReminderById() = runBlockingTest {
        remindersDao.saveReminder(testData)

        val savedData = remindersDao.getReminderById(testData.id)

        Assert.assertTrue("Not Data is saved", savedData != null)
        Assert.assertEquals("Titles are not equal", testData.title, savedData?.title)
        Assert.assertEquals("Descriptions are not equal", testData.description, savedData?.description)
        Assert.assertEquals("Locations are not equal", testData.location, savedData?.location)
        Assert.assertEquals("Latitudes are not equal", testData.latitude, savedData?.latitude)
        Assert.assertEquals("Longitudes are not equal", testData.longitude, savedData?.longitude)
    }

    @Test
    fun testDeletionAll() = runBlockingTest {
        remindersDao.saveReminder(testData)

        Assert.assertTrue("Items are present in the database", remindersDao.getReminders().isNotEmpty())

        remindersDao.deleteAllReminders()
        Assert.assertTrue("There are no reminders in the database", remindersDao.getReminders().isEmpty())
    }

    @After
    fun clearDatabase() {
        database.clearAllTables()
        database.close()
    }
}