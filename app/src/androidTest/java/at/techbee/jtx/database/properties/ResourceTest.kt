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

class ResourceTest {
// Android Test as Content Values need Android libraries to run


    @Test
    fun createFromContentValues() {

        val sampleResource = Resource(
            icalObjectId = 1L,
            text = "category",
            altrep = "Kategorie",
            language = "EN",
            other = "nothing"
        )

        val cv = ContentValues().apply {
            put(COLUMN_RESOURCE_ICALOBJECT_ID, sampleResource.icalObjectId)
            put(COLUMN_RESOURCE_TEXT, sampleResource.text)
            put(COLUMN_RESOURCE_ALTREP, sampleResource.altrep)
            put(COLUMN_RESOURCE_LANGUAGE, sampleResource.language)
            put(COLUMN_RESOURCE_OTHER, sampleResource.other)
        }

        val cvResource = Resource.fromContentValues(cv)
        assertEquals(sampleResource, cvResource)
    }

    @Test
    fun createFromContentValuesWithoutIcalobjectId() {

        val cv = ContentValues().apply {
            put(COLUMN_RESOURCE_TEXT,  "projector")
        }

        val cvResource = Resource.fromContentValues(cv)
        assertNull(cvResource)
    }

    @Test
    fun createFromContentValuesWithoutText() {


        val cv = ContentValues().apply {
            put(COLUMN_RESOURCE_ICALOBJECT_ID, 1L)
        }

        val cvResource = Resource.fromContentValues(cv)
        assertNull(cvResource)
    }
}