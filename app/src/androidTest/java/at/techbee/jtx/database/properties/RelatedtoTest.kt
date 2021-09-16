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

class RelatedtoTest {
// Android Test as Content Values need Android libraries to run


    @Test
    fun createFromContentValues() {

        val sampleRelatedto = Relatedto(
            icalObjectId = 1L,
            linkedICalObjectId = 1L,
            text = "Random UID",
            reltype = Reltype.CHILD.name,
            other = "nothing"
        )

        val cv = ContentValues().apply {
            put(COLUMN_RELATEDTO_ICALOBJECT_ID, sampleRelatedto.icalObjectId)
            put(COLUMN_RELATEDTO_LINKEDICALOBJECT_ID, sampleRelatedto.linkedICalObjectId)
            put(COLUMN_RELATEDTO_TEXT, sampleRelatedto.text)
            put(COLUMN_RELATEDTO_RELTYPE, sampleRelatedto.reltype)
            put(COLUMN_RELATEDTO_OTHER, sampleRelatedto.other)
        }

        val cvRelatedto = Relatedto.fromContentValues(cv)
        assertEquals(sampleRelatedto, cvRelatedto)
    }

    @Test
    fun createFromContentValuesWithoutIcalobjectId() {

        val cv = ContentValues().apply {
            put(COLUMN_RELATEDTO_LINKEDICALOBJECT_ID, 1L)
        }

        val cvRelatedto = Relatedto.fromContentValues(cv)
        assertNull(cvRelatedto)
    }
}