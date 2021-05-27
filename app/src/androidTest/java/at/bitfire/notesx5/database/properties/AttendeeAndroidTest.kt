/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.database.properties

import android.content.ContentValues
import org.junit.Test

import org.junit.Assert.*

class AttendeeAndroidTest {
// Android Test as Content Values need Android libraries to run

    @Test
    fun fromContentValues_correct_Content_Values() {
        val conval = ContentValues()
        conval.put(COLUMN_ATTENDEE_ICALOBJECT_ID, 1L)
        conval.put(COLUMN_ATTENDEE_CALADDRESS, "mailto:test@test.com")

        val attendee = Attendee.fromContentValues(conval)

        assertEquals(attendee!!.icalObjectId, conval.get(COLUMN_ATTENDEE_ICALOBJECT_ID))
        assertEquals(attendee.caladdress, conval.get(COLUMN_ATTENDEE_CALADDRESS))
    }

    @Test
    fun fromContentValues_missing_caladdress() {
        val conval = ContentValues()
        conval.put(COLUMN_ATTENDEE_ICALOBJECT_ID, 1L)
        val attendee = Attendee.fromContentValues(conval)

        assertNull(attendee)
    }

    @Test
    fun fromContentValues_missing_icalobjectid() {
        val conval = ContentValues()
        conval.put(COLUMN_ATTENDEE_CALADDRESS, "mailto:test@test.com")
        val attendee = Attendee.fromContentValues(conval)

        assertNull(attendee)
    }

    @Test
    fun applyContentValues_icalObjectId() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = 1L

        conval.put(COLUMN_ATTENDEE_ICALOBJECT_ID, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.icalObjectId = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_caladdress() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "https://mycaladdress.com"

        conval.put(COLUMN_ATTENDEE_CALADDRESS, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.caladdress = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_cutype() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = Cutype.INDIVIDUAL.name

        conval.put(COLUMN_ATTENDEE_CUTYPE, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.cutype = newVal

        assertEquals(attendee, attendee2)
    }


    @Test
    fun applyContentValues_memberparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "https://mycalmembership.com"

        conval.put(COLUMN_ATTENDEE_MEMBER, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.member = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_roleparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = Role.`NON-PARTICIPANT`.name

        conval.put(COLUMN_ATTENDEE_ROLE, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.role = newVal

        assertEquals(attendee, attendee2)
    }


    @Test
    fun applyContentValues_partstatparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "ACCEPTED"

        conval.put(COLUMN_ATTENDEE_PARTSTAT, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.partstat = newVal

        assertEquals(attendee, attendee2)
    }


    @Test
    fun applyContentValues_rvspparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = true

        conval.put(COLUMN_ATTENDEE_RSVP, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.rsvp = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_deltoparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "mailto:me@home.com"

        conval.put(COLUMN_ATTENDEE_DELEGATEDTO, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.delegatedto = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_delfromparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "mailto:me@home.com"

        conval.put(COLUMN_ATTENDEE_DELEGATEDFROM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.delegatedfrom = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_sentbyparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "mailto:me@home.com"

        conval.put(COLUMN_ATTENDEE_SENTBY, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.sentby = newVal

        assertEquals(attendee, attendee2)
    }


    @Test
    fun applyContentValues_cnparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "Patrick"

        conval.put(COLUMN_ATTENDEE_CN, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.cn = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_dirparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "/a/b/c"

        conval.put(COLUMN_ATTENDEE_DIR, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.dir = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_languageparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "en"

        conval.put(COLUMN_ATTENDEE_LANGUAGE, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.language = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_otherparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "whatever"

        conval.put(COLUMN_ATTENDEE_OTHER, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.other = newVal

        assertEquals(attendee, attendee2)
    }

}