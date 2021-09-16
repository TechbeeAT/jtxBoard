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

class ContactTest {
// Android Test as Content Values need Android libraries to run


    @Test
    fun createFromContentValues() {

        val sampleContact = Contact(
            icalObjectId = 1L,
            text = "contact",
            altrep = "Kontakt",
            language = "EN",
            other = "nothing"
        )

        val cv = ContentValues().apply {
            put(COLUMN_CONTACT_ICALOBJECT_ID, sampleContact.icalObjectId)
            put(COLUMN_CONTACT_TEXT, sampleContact.text)
            put(COLUMN_CONTACT_ALTREP, sampleContact.altrep)
            put(COLUMN_CONTACT_LANGUAGE, sampleContact.language)
            put(COLUMN_CONTACT_OTHER, sampleContact.other)
        }

        val cvContact = Contact.fromContentValues(cv)
        assertEquals(sampleContact, cvContact)
    }

    @Test
    fun createFromContentValuesWithoutIcalobjectId() {

        val cv = ContentValues().apply {
            put(COLUMN_CONTACT_TEXT,  "contact")
        }

        val cvContact = Contact.fromContentValues(cv)
        assertNull(cvContact)
    }

    @Test
    fun createFromContentValuesWithoutText() {


        val cv = ContentValues().apply {
            put(COLUMN_CONTACT_ICALOBJECT_ID, 1L)
        }

        val cvContact = Contact.fromContentValues(cv)
        assertNull(cvContact)
    }
}