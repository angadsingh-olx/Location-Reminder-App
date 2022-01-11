package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.utils.getOrAwaitValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var reminderDataSource: FakeDataSource

    private lateinit var viewModel: SaveReminderViewModel

    @Before
    fun setupViewModel() {
        stopKoin()
        reminderDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @Test
    fun testValidationWithNoTitleData() {
        val testData = ReminderDataItem(
            null,
            "Description",
            "location",
            0.0, 0.0
        )

        val result = viewModel.validateAndSaveReminder(testData)
        Assert.assertFalse("The Reminder Data is inserted as validation check fails", result)
        Assert.assertEquals("The UI doesn't show a snackbar with error message", viewModel.showSnackBarInt.getOrAwaitValue(), R.string.err_enter_title)
    }

    @Test
    fun testValidationWithNoLocationData() {
        val testData = ReminderDataItem(
            "Title",
            "Description",
            null,
            0.0, 0.0
        )

        val result = viewModel.validateAndSaveReminder(testData)
        Assert.assertFalse("The Reminder Data is inserted as validation check fails", result)
        Assert.assertEquals("The UI doesn't show a snackbar with error message", viewModel.showSnackBarInt.getOrAwaitValue(), R.string.err_select_location)
    }

    @Test
    fun testValidationWithCompleteData() {
        val testData = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            0.0, 0.0
        )

        val result = viewModel.validateAndSaveReminder(testData)
        Assert.assertTrue("The Reminder Data is not inserted as validation fails", result)
        Assert.assertEquals("The UI doesn't show the successful toast message", viewModel.showToast.getOrAwaitValue(),
            (ApplicationProvider.getApplicationContext() as Application).getString(R.string.reminder_saved)
        )
        Assert.assertEquals("The doesn't navigate back to last screen", viewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }

    @Test
    fun testLoaderShownWhenSavingData() {
        val testData = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            0.0, 0.0
        )

        mainCoroutineRule.pauseDispatcher()
        viewModel.validateAndSaveReminder(testData)
        Assert.assertTrue("The UI is not showing the loader", viewModel.showLoading.getOrAwaitValue())

        mainCoroutineRule.resumeDispatcher()
        Assert.assertFalse("The UI is showing the loader after data is saved", viewModel.showLoading.getOrAwaitValue())
    }
}