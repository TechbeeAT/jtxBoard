/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import at.techbee.jtx.util.DateTimeUtils.isLocalizedWeekstartMonday
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale


@RunWith(AndroidJUnit4::class)
@SmallTest
class DateTimeUtilsAndroidTest {


    @Test
    fun isLocalizedWeekstartMonday_GERMAN() {
        Locale.setDefault(Locale.GERMAN)
        assertEquals(true, isLocalizedWeekstartMonday())
    }

    @Test
    fun isLocalizedWeekstartMonday_US() {
        Locale.setDefault(Locale.US)
        assertEquals(false, isLocalizedWeekstartMonday())
    }

}