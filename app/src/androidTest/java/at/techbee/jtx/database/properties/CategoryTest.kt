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

class CategoryTest {
// Android Test as Content Values need Android libraries to run


    @Test
    fun createFromContentValues() {

        val sampleCategory = Category(
            icalObjectId = 1L,
            text = "category",
            language = "EN",
            other = "nothing"
        )

        val cv = ContentValues().apply {
            put(COLUMN_CATEGORY_ICALOBJECT_ID, sampleCategory.icalObjectId)
            put(COLUMN_CATEGORY_TEXT, sampleCategory.text)
            put(COLUMN_CATEGORY_LANGUAGE, sampleCategory.language)
            put(COLUMN_CATEGORY_OTHER, sampleCategory.other)
        }

        val cvCategory = Category.fromContentValues(cv)
        assertEquals(sampleCategory, cvCategory)
    }

    @Test
    fun createFromContentValuesWithoutIcalobjectId() {

        val cv = ContentValues().apply {
            put(COLUMN_CATEGORY_TEXT,  "category")
        }

        val cvCategory = Category.fromContentValues(cv)
        assertNull(cvCategory)
    }

    @Test
    fun createFromContentValuesWithoutText() {


        val cv = ContentValues().apply {
            put(COLUMN_CATEGORY_ICALOBJECT_ID, 1L)
        }

        val cvCategory = Category.fromContentValues(cv)
        assertNull(cvCategory)
    }
}