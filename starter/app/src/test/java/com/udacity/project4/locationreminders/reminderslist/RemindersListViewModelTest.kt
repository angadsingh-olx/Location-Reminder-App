package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var reminderDataSource: FakeDataSource

    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun setupViewModel() {
        stopKoin()
        reminderDataSource = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @Test
    fun testLoadReminderLoading() = mainCoroutineRule.runBlockingTest {

        val testData = ReminderDTO(
            "Title",
            "Description",
            "location",
            0.0, 0.0
        )

        reminderDataSource.saveReminder(testData)
        mainCoroutineRule.pauseDispatcher()
        viewModel.loadReminders()

        Assert.assertTrue("The UI is in loading state", viewModel.showLoading.getOrAwaitValue())

        mainCoroutineRule.resumeDispatcher()

        Assert.assertFalse("The UI is in loaded state", viewModel.showLoading.getOrAwaitValue())
        Assert.assertFalse("The UI is showing the data", viewModel.showNoData.getOrAwaitValue())
    }

    @Test
    fun testLoadReminderShowNoData() {
        reminderDataSource.setReturnError(true)
        viewModel.loadReminders()

        Assert.assertTrue("The UI is showing no data", viewModel.showNoData.getOrAwaitValue())
        Assert.assertEquals("The UI is showing error snackbar", "Reminders not found", viewModel.showSnackBar.getOrAwaitValue())
    }
}