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
class AdinfoFragmentTest {


    @Test
    fun check_texts() {
        launchFragmentInContainer<AdInfoFragment>(null, R.style.AppTheme, Lifecycle.State.RESUMED)
        onView(withText(R.string.adinfo_text)).check(matches(isDisplayed()))
        onView(withText(R.string.adinfo_adfree_text)).check(matches(isDisplayed()))
        //onView(withId(R.id.adinfo_adfree_text)).check(matches(isDisplayed()))
        //onView(withId(R.id.adinfo_card_purchase)).check(matches(isDisplayed()))

    }

}