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
import at.techbee.jtx.util.DateTimeUtils.getLocalizedDaysOfWeek
import at.techbee.jtx.util.DateTimeUtils.getLocalizedFirstDayOfWeek
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.time.DayOfWeek
import java.util.Locale


@RunWith(AndroidJUnit4::class)
@SmallTest
class DateTimeUtilsAndroidTest {

    private val defaultLocale: Locale = Locale.getDefault()

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun getLocalizedFirstDayOfWeek_GERMAN() {
        Locale.setDefault(Locale.GERMAN)
        assertEquals(DayOfWeek.MONDAY, getLocalizedFirstDayOfWeek())
    }

    @Test
    fun getLocalizedFirstDayOfWeek_US() {
        Locale.setDefault(Locale.US)
        assertEquals(DayOfWeek.SUNDAY, getLocalizedFirstDayOfWeek())
    }

    @Test
    fun getLocalizedFirstDayOfWeek_US_withFirstDayOfWeekOverrideMonday() {
        // Emulates the "Regional preferences -> First day of week = Monday" system setting,
        // which is exposed as the "-u-fw-mon" unicode extension on the default locale.
        Locale.setDefault(Locale.forLanguageTag("en-US-u-fw-mon"))
        assertEquals(DayOfWeek.MONDAY, getLocalizedFirstDayOfWeek())
    }

    @Test
    fun getLocalizedFirstDayOfWeek_GERMAN_withFirstDayOfWeekOverrideSunday() {
        Locale.setDefault(Locale.forLanguageTag("de-DE-u-fw-sun"))
        assertEquals(DayOfWeek.SUNDAY, getLocalizedFirstDayOfWeek())
    }

    @Test
    fun getLocalizedDaysOfWeek_GERMAN_startsWithMonday() {
        Locale.setDefault(Locale.GERMAN)
        assertEquals(
            listOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            ),
            getLocalizedDaysOfWeek()
        )
    }

    @Test
    fun getLocalizedDaysOfWeek_US_startsWithSunday() {
        Locale.setDefault(Locale.US)
        assertEquals(
            listOf(
                DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
            ),
            getLocalizedDaysOfWeek()
        )
    }

    @Test
    fun getLocalizedDaysOfWeek_US_withFirstDayOfWeekOverrideSaturday() {
        Locale.setDefault(Locale.forLanguageTag("en-US-u-fw-sat"))
        assertEquals(
            listOf(
                DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
            ),
            getLocalizedDaysOfWeek()
        )
    }

}
