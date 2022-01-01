/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import at.techbee.jtx.util.DateTimeUtils.addLongToCSVString
import at.techbee.jtx.util.DateTimeUtils.convertLongToDayString
import at.techbee.jtx.util.DateTimeUtils.convertLongToYearString
import at.techbee.jtx.util.DateTimeUtils.getAttachmentSizeString
import at.techbee.jtx.util.DateTimeUtils.getLongListfromCSVString
import at.techbee.jtx.util.DateTimeUtils.getTodayAsLong
import at.techbee.jtx.util.DateTimeUtils.isValidEmail
import at.techbee.jtx.util.DateTimeUtils.isValidURL
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class UtilKtTest {

    private val sampleDateTime = 1632395090107L   // = Thu Sep 23 2021 11:04:50 (UTC)
    private val sampleTimezone = "UTC+13"

    private val sampleDateTime2 = 1640991660000L  // = Sat Jan 01 2022 00:01:00
    private val sampleTimezone2 = "Europe/Lisbon"

    // TODO: Those tests might fail in the future as the methods return locales, check for a better solution
    //@Test fun convertLongToDateString() = assertEquals("", convertLongToDateString(sampleDateTime))
    //@Test fun convertLongToTimeString()  = assertEquals("00:04", convertLongToTimeString(sampleDateTime, sampleTimezone))
    //@Test fun convertLongToMonthString() = assertEquals("September", convertLongToMonthString(sampleDateTime))

    @Test fun convertLongToDayString_test() = assertEquals("23", convertLongToDayString(sampleDateTime, null))
    @Test fun convertLongToYearString_test() = assertEquals("2021", convertLongToYearString(sampleDateTime, null))
    @Test fun convertLongToDayString_test_timezone_next_day() = assertEquals("24", convertLongToDayString(sampleDateTime, sampleTimezone))

    @Test fun convertLongToDayString_test_timezone_last_year() {
        //assertEquals("01", convertLongToDayString(sampleDateTime2, null))
        //assertEquals("01", convertLongToMonthString(sampleDateTime2, null))
        //assertEquals("2022", convertLongToYearString(sampleDateTime2, null))

        assertEquals("31", convertLongToDayString(sampleDateTime2, sampleTimezone2))
        //assertEquals("12", convertLongToMonthString(sampleDateTime2, sampleTimezone2))
        assertEquals("2021", convertLongToYearString(sampleDateTime2, sampleTimezone2))
    }



    @Test fun isValidEmail_testTrue() = assertTrue(isValidEmail("valid@email.com"))
    @Test fun isValidEmail_testFalse1() = assertFalse(isValidEmail("invalid.com"))
    @Test fun isValidEmail_testFalse2() = assertFalse(isValidEmail("invalid@com"))

    @Test fun isValidURL_testTrue1() = assertTrue(isValidURL("example.com"))
    @Test fun isValidURL_testTrue2() = assertTrue(isValidURL("www.example.com"))
    @Test fun isValidURL_testTrue3() = assertTrue(isValidURL("http://example.com"))
    @Test fun isValidURL_testTrue4() = assertTrue(isValidURL("https://www.example.com/asdf"))
    @Test fun isValidURL_testFalse1() = assertFalse(isValidURL("AABB"))
    @Test fun isValidURL_testFalse2() = assertFalse(isValidURL("asdf://AABB.com"))

    @Test fun getAttachmentSizeString_bytes() = assertEquals("100 Bytes", getAttachmentSizeString(100))
    @Test fun getAttachmentSizeString_kilobytes() = assertEquals("1 KB", getAttachmentSizeString(1024))
    @Test fun getAttachmentSizeString_megabytes() = assertEquals("1 MB", getAttachmentSizeString(1048576))

    @Test fun addLongToCSVString_test() = assertEquals(("1622800800000,1622887200000,1624010400000"), addLongToCSVString("1622800800000,1622887200000", 1624010400000L))
    @Test fun addLongToCSVString_noDuplicate() = assertEquals(("1622800800000,1622887200000,1624010400000"), addLongToCSVString("1622800800000,1622887200000,1624010400000", 1624010400000L))
    @Test fun addLongToCSVString_fromEmpty() = assertEquals(("1624010400000"), addLongToCSVString(null, 1624010400000L))
    @Test fun addLongToCSVString_fromEmpty2() = assertEquals(("1624010400000"), addLongToCSVString("", 1624010400000L))
    @Test fun addLongToCSVString_fromEmpty3() = assertEquals(("1624010400000"), addLongToCSVString("   ", 1624010400000L))

    @Test fun getLongListfromCSVString_test() {
        val list = getLongListfromCSVString("1622800800000,1622887200000,1624010400000")
        assertEquals(1622800800000L, list[0])
        assertEquals(1622887200000L, list[1])
        assertEquals(1624010400000L, list[2])
    }

    @Test fun getLongListfromCSVString_oneException() {
        val list = getLongListfromCSVString("asdf,1622887200000,1624010400000")
        assertEquals(1622887200000L, list[0])
        assertEquals(1624010400000L, list[1])
    }

    @Test fun getLongListfromCSVString_empty() = assertEquals(emptyList<Long>(), getLongListfromCSVString(null))
    @Test fun getLongListfromCSVString_empty2() = assertEquals(emptyList<Long>(), getLongListfromCSVString("asdf"))

    // one day in millis is 86400000, we get the UTC time so the UTC time divided by 86400000 must always have 0 rest
    @Test fun getTodayAsLong_test() {
        val today = getTodayAsLong()
        val offset = ZoneId.systemDefault().rules.getOffset(Instant.now())
        val todayUTC = today + offset.totalSeconds*1000
        assertTrue(todayUTC%86400000 == 0L)
    }
}