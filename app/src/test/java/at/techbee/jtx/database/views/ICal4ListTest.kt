/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.views

import at.techbee.jtx.database.COLUMN_DTSTART
import at.techbee.jtx.database.Module
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.TimeZone

class ICal4ListTest {

    private val systemTimeZone = TimeZone.getDefault()

    /** a timezone with an offset that is not a full hour to also catch rounding to full hours */
    private val testTimeZone = TimeZone.getTimeZone("Australia/Adelaide")   // +09:30/+10:30

    @After
    fun tearDown() {
        TimeZone.setDefault(systemTimeZone)
    }

    /**
     * @return the range (from inclusive, until exclusive) that the given query applies to entries
     * that are NOT all-day entries (the ELSE branch of the CASE), or null if it is not present
     */
    private fun getRangeForEntriesWithTime(sql: String): LongRange? {
        val match = Regex("ELSE $COLUMN_DTSTART >= (\\d+) AND $COLUMN_DTSTART < (\\d+) END").find(sql) ?: return null
        return match.groupValues[1].toLong() until match.groupValues[2].toLong()
    }

    private fun queryForStartToday(): String {
        TimeZone.setDefault(testTimeZone)
        return ICal4List.constructQuery(
            modules = listOf(Module.TODO),
            isFilterStartToday = true,
            hideBiometricProtected = emptyList()
        ).sql
    }

    private fun asEpochMilli(date: LocalDate, time: LocalTime) =
        date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    @Test
    fun constructQuery_filterStartToday_includesEntryLateToday() {
        val range = getRangeForEntriesWithTime(queryForStartToday())
        assertNotNull(range)
        assertTrue(asEpochMilli(LocalDate.now(), LocalTime.of(23, 30)) in range!!)
    }

    @Test
    fun constructQuery_filterStartToday_includesEntryEarlyToday() {
        val range = getRangeForEntriesWithTime(queryForStartToday())
        assertNotNull(range)
        assertTrue(asEpochMilli(LocalDate.now(), LocalTime.of(0, 30)) in range!!)
    }

    @Test
    fun constructQuery_filterStartToday_excludesEntryOfTomorrowMorning() {
        val range = getRangeForEntriesWithTime(queryForStartToday())
        assertNotNull(range)
        assertFalse(asEpochMilli(LocalDate.now().plusDays(1), LocalTime.of(9, 0)) in range!!)
    }

    @Test
    fun constructQuery_filterStartToday_excludesEntryOfYesterdayEvening() {
        val range = getRangeForEntriesWithTime(queryForStartToday())
        assertNotNull(range)
        assertFalse(asEpochMilli(LocalDate.now().minusDays(1), LocalTime.of(22, 0)) in range!!)
    }

    @Test
    fun constructQuery_filterStartToday_allDayEntriesAreComparedInUTC() {
        val sql = queryForStartToday()
        val todayAsAllDay = LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val tomorrowAsAllDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        assertTrue(sql.contains("THEN $COLUMN_DTSTART >= $todayAsAllDay AND $COLUMN_DTSTART < $tomorrowAsAllDay "))
    }
}
