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
class DonateFragmentTest {


    @Test
    fun check_texts() {

        launchFragmentInContainer<DonateFragment>(null, R.style.AppTheme, Lifecycle.State.RESUMED)

        onView(withText(R.string.donate_thank_you)).check(matches(isDisplayed()))
        onView(withText(R.string.donate_header_text)).check(matches(isDisplayed()))
        onView(withText(R.string.donate_donate_with)).check(matches(isDisplayed()))
        onView(withText(R.string.donate_other_donation_methods)).check(matches(isDisplayed()))
        onView(withId(R.id.donate_other_website)).check(matches(isDisplayed()))
        onView(withId(R.id.donate_other_website)).perform(click())
    }
}