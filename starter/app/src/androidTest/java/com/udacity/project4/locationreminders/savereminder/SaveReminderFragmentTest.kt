package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.data.FakeAndroidDataSource
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.*
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SaveReminderViewModel
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Before
    fun injectFakes() {
        stopKoin()

        val override = module {
            single {
                SaveReminderViewModel(
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

        viewModel = GlobalContext.get().koin.get()
    }

    @Test
    fun testNavigationToReminderListFragment() {
        viewModel.reminderTitle.postValue("Title")
        viewModel.reminderDescription.postValue("Description")
        viewModel.reminderSelectedLocationStr.postValue("Location")
        viewModel.latitude.postValue(77.0)
        viewModel.longitude.postValue(28.3)

        val scenario = launchFragmentInContainer<SaveReminderFragment>(themeResId = R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())
        Mockito.verify(navController).popBackStack()
    }

    @Test
    fun testWithoutTitleWillFailToSave() {
        viewModel.reminderSelectedLocationStr.postValue("Location")
        viewModel.latitude.postValue(77.0)
        viewModel.longitude.postValue(28.3)

        val scenario = launchFragmentInContainer<SaveReminderFragment>(themeResId = R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(ViewMatchers.withId(R.id.reminderDescription)).perform(typeText("Description"))
        closeSoftKeyboard()
        onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        Assert.assertTrue(viewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_enter_title)
    }

    @Test
    fun testWithoutLocationWillFailToSave() {
        val scenario = launchFragmentInContainer<SaveReminderFragment>(themeResId = R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(ViewMatchers.withId(R.id.reminderTitle)).perform(typeText("Title"))
        onView(ViewMatchers.withId(R.id.reminderDescription)).perform(typeText("Description"))
        closeSoftKeyboard()
        onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())

        Assert.assertTrue(viewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_select_location)
    }
}