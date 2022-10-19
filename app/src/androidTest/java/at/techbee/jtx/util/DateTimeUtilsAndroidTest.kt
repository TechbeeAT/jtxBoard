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
import at.techbee.jtx.util.DateTimeUtils.getLocalizedOrdinal
import at.techbee.jtx.util.DateTimeUtils.isLocalizedWeekstartMonday
import org.junit.Assert.*

import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
@SmallTest
class DateTimeUtilsAndroidTest {


    @Test
    fun getLocalizedOrdinal_GERMAN() {

        Locale.setDefault(Locale.GERMAN)
        val ordinal = getLocalizedOrdinal(1, 4, false)

        assertEquals("1.", ordinal[0])
        assertEquals("2.", ordinal[1])
        assertEquals("3.", ordinal[2])
        assertEquals("4.", ordinal[3])
    }

    @Test
    fun getLocalizedOrdinal_ENGLISH() {

        Locale.setDefault(Locale.ENGLISH)
        val ordinal = getLocalizedOrdinal(1, 5, false)

        assertEquals("1st", ordinal[0])
        assertEquals("2nd", ordinal[1])
        assertEquals("3rd", ordinal[2])
        assertEquals("4th", ordinal[3])
        assertEquals("5th", ordinal[4])
    }

    @Test
    fun getLocalizedOrdinal_FRENCH() {

        Locale.setDefault(Locale.FRENCH)
        val ordinal = getLocalizedOrdinal(1, 3, true)

        assertEquals("-", ordinal[0])
        assertEquals("1er", ordinal[1])
        assertEquals("2e", ordinal[2])
        assertEquals("3e", ordinal[3])
    }


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