/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import android.content.ContentValues
import org.junit.Assert
import org.junit.Test


class UnknownTest {
// Android Test as Content Values need Android libraries to run


    @Test
    fun createFromContentValues() {

        val sampleUnknown = Unknown(
            icalObjectId = 1L,
            value = "[\"X-UNKNOWN\",\"PropValue\",{\"X-PARAM1\":\"value1\",\"X-PARAM2\":\"value2\"}]",
        )

        val cv = ContentValues(5).apply {
            put(COLUMN_UNKNOWN_ICALOBJECT_ID, sampleUnknown.icalObjectId)
            put(COLUMN_UNKNOWN_VALUE, sampleUnknown.value)
        }

        val cvUnknown = Unknown.fromContentValues(cv)
        Assert.assertEquals(sampleUnknown, cvUnknown)
    }

    @Test
    fun createFromContentValuesWithoutIcalobjectId() {

        val cv = ContentValues(1).apply {
            put(COLUMN_UNKNOWN_VALUE,  "[\"X-UNKNOWN\",\"PropValue\",{\"X-PARAM1\":\"value1\",\"X-PARAM2\":\"value2\"}]")
        }

        val cvUnknown = Unknown.fromContentValues(cv)
        Assert.assertNull(cvUnknown)
    }

    @Test
    fun createFromContentValuesWithoutText() {


        val cv = ContentValues(1).apply {
            put(COLUMN_UNKNOWN_ICALOBJECT_ID, 1L)
        }

        val cvUnknown = Unknown.fromContentValues(cv)
        Assert.assertNull(cvUnknown)
    }
}