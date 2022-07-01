/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import at.techbee.jtx.R
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@SmallTest
class SyncFragmentTest {

/*
    @Test
    fun check_texts() {
        launchFragmentInContainer<SyncFragment>(null, R.style.AppTheme, Lifecycle.State.RESUMED)
        //onView(withId(R.id.sync_davx5_logo)).check(matches(isDisplayed()))
        onView(withText(R.string.sync_with_davx5_heading)).check(matches(isDisplayed()))
        onView(withText(R.string.sync_basic_info)).check(matches(isDisplayed()))
        // further checks are skipped here. Other texts would depend if DAVx5 is installed or not, this could be the case for a local test, but for the CI maybe not. It's better to skip the test as it is not that important anyway.
    }

 */
}