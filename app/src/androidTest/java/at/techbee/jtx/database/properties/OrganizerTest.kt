/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import android.content.ContentValues
import org.junit.Test

import org.junit.Assert.*

class OrganizerTest {
// Android Test as Content Values need Android libraries to run


    @Test
    fun createFromContentValues() {

        val sampleOrganizer = Organizer(
            icalObjectId = 1L,
            caladdress = "info@techbee.at",
            cn = "Organizer",
            dir = "Organizer_DIR",
            sentby = "info@techbee.at",
            language = "EN",
            other = "nothing"
        )

        val cv = ContentValues(7).apply {
            put(COLUMN_ORGANIZER_ICALOBJECT_ID, sampleOrganizer.icalObjectId)
            put(COLUMN_ORGANIZER_CALADDRESS, sampleOrganizer.caladdress)
            put(COLUMN_ORGANIZER_CN, sampleOrganizer.cn)
            put(COLUMN_ORGANIZER_DIR, sampleOrganizer.dir)
            put(COLUMN_ORGANIZER_SENTBY, sampleOrganizer.sentby)
            put(COLUMN_ORGANIZER_LANGUAGE, sampleOrganizer.language)
            put(COLUMN_ORGANIZER_OTHER, sampleOrganizer.other)
        }

        val cvOrganizer = Organizer.fromContentValues(cv)
        assertEquals(sampleOrganizer, cvOrganizer)
    }

    @Test
    fun createFromContentValuesWithoutIcalobjectId() {

        val cv = ContentValues(1).apply {
            put(COLUMN_ORGANIZER_CALADDRESS,  "info@techbee.at")
        }

        val cvOrganizer = Organizer.fromContentValues(cv)
        assertNull(cvOrganizer)
    }

    @Test
    fun createFromContentValuesWithoutCaladdress() {


        val cv = ContentValues(1).apply {
            put(COLUMN_ORGANIZER_ICALOBJECT_ID, 1L)
        }

        val cvOrganizer = Organizer.fromContentValues(cv)
        assertNull(cvOrganizer)
    }
}