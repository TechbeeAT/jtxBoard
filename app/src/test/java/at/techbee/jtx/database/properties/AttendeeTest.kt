/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import org.junit.Assert.*

import org.junit.Test

class AttendeeTest {

    @Test
    fun getDisplayString_CN_and_CALADDRESS() {
        val attendee = Attendee(caladdress = "mailto:jtx@techbee.at", cn = "JTX")
        assertEquals("JTX <jtx@techbee.at>", attendee.getDisplayString())
    }

    @Test
    fun getDisplayString_CALADDRESS() {
        val attendee = Attendee(caladdress = "mailto:jtx@techbee.at")
        assertEquals("jtx@techbee.at", attendee.getDisplayString())
    }

    @Test
    fun getDisplayString_empty() {
        val attendee = Attendee()
        assertEquals("", attendee.getDisplayString())
    }

    @Test
    fun attendeeFromString_CN_and_CALADDRESS_whitespace() {
        val attendee = Attendee.fromString("JTX Board <jtx@techbee.at>")
        assertEquals("mailto:jtx@techbee.at", attendee?.caladdress)
        assertEquals("JTX Board", attendee?.cn)
    }

    @Test
    fun attendeeFromString_CN_and_CALADDRESS() {
        val attendee = Attendee.fromString("JTX Board<jtx@techbee.at>")
        assertEquals("mailto:jtx@techbee.at", attendee?.caladdress)
        assertEquals("JTX Board", attendee?.cn)
    }

    @Test
    fun attendeeFromString_CALADDRESS() {
        val attendee = Attendee.fromString("jtx@techbee.at   ")
        assertEquals("mailto:jtx@techbee.at", attendee?.caladdress)
        assertNull(attendee?.cn)
    }

    @Test
    fun attendeeFromString_null() {
        val attendee = Attendee.fromString("jtx(at)techbee.at <JTX Board>")
        assertNull(attendee)
    }

    @Test
    fun attendeeFromString_null2() {
        val attendee = Attendee.fromString(null)
        assertNull(attendee)
    }
}