package at.techbee.jtx.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import at.techbee.jtx.R
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@SmallTest
class AdinfoFragmentTest {


    @Test
    fun check_texts() {
        launchFragmentInContainer<AdInfoFragment>(null, R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(R.string.adinfo_text)).check(matches(isDisplayed()))
        onView(withText(R.string.adinfo_adfree_furtherdownloadoptions)).check(matches(isDisplayed()))
        onView(withText(R.string.adinfo_adfree_text)).check(matches(isDisplayed()))
        onView(withId(R.id.adinfo_button_playstore)).check(matches(isDisplayed()))
    }

    @Test
    fun open_playstore() {
        launchFragmentInContainer<AdInfoFragment>(null, R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withId(R.id.adinfo_button_playstore)).perform(click())
    }

    @Test
    fun open_link() {
        launchFragmentInContainer<AdInfoFragment>(null, R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withId(R.id.adinfo_adfree_download_link)).perform(click())
    }
}