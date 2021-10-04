package at.techbee.jtx.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import at.techbee.jtx.R
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@SmallTest
class AboutFragmentTest {


    @Test
    fun check_about_tabs() {

        launchFragmentInContainer<AboutFragment>(null, R.style.AppTheme, Lifecycle.State.RESUMED)

        onView(withText(R.string.app_name)).check(matches(isDisplayed()))
        onView(withText(R.string.about_tabitem_libraries)).perform(click())
        onView(withText("AboutLibraries Library")).check(matches(isDisplayed()))

        //onView(withText(R.string.about_tabitem_translations)).perform(click())

        onView(withText(R.string.about_tabitem_jtx)).perform(click())
        onView(withText(R.string.app_name)).check(matches(isDisplayed()))

    }
}