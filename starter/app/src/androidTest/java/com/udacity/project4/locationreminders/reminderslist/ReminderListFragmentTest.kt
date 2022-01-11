package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var dataSource: FakeAndroidDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun injectFakes() {
        stopKoin()

        val override = module {
            viewModel {
                RemindersListViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as FakeAndroidDataSource
                )
            }
            single { FakeAndroidDataSource() }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(override)
        }

        dataSource = GlobalContext.get().koin.get()
    }

    @Test
    fun testNavigationToSaveReminderFragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun testDisplayDataOnFragment() {
        val testData = ReminderDTO(
            "Title",
            "Description",
            "location",
            0.0, 0.0
        )

        runBlocking { dataSource.saveReminder(testData) }

        val scenario = launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withId(R.id.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.title)).check(ViewAssertions.matches(ViewMatchers.withText(testData.title)))

        onView(withId(R.id.description)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.description)).check(ViewAssertions.matches(ViewMatchers.withText(testData.description)))

        onView(withId(R.id.location)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.location)).check(ViewAssertions.matches(ViewMatchers.withText(testData.location)))
    }

    @Test
    fun testDisplayNoDataOnFragment() {
        runBlocking { dataSource.deleteAllReminders() }

        val scenario = launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.withText((ApplicationProvider.getApplicationContext() as Application).getString(R.string.no_data))))
    }
}