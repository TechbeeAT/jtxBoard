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

class CommentTest {
// Android Test as Content Values need Android libraries to run


    @Test
    fun createFromContentValues() {

        val sampleComment = Comment(
            icalObjectId = 1L,
            text = "category",
            altrep = "Kategorie",
            language = "EN",
            other = "nothing"
        )

        val cv = ContentValues().apply {
            put(COLUMN_COMMENT_ICALOBJECT_ID, sampleComment.icalObjectId)
            put(COLUMN_COMMENT_TEXT, sampleComment.text)
            put(COLUMN_COMMENT_ALTREP, sampleComment.altrep)
            put(COLUMN_COMMENT_LANGUAGE, sampleComment.language)
            put(COLUMN_COMMENT_OTHER, sampleComment.other)
        }

        val cvComment = Comment.fromContentValues(cv)
        assertEquals(sampleComment, cvComment)
    }

    @Test
    fun createFromContentValuesWithoutIcalobjectId() {

        val cv = ContentValues().apply {
            put(COLUMN_COMMENT_TEXT,  "comment")
        }

        val cvComment = Comment.fromContentValues(cv)
        assertNull(cvComment)
    }

    @Test
    fun createFromContentValuesWithoutText() {


        val cv = ContentValues().apply {
            put(COLUMN_COMMENT_ICALOBJECT_ID, 1L)
        }

        val cvComment = Comment.fromContentValues(cv)
        assertNull(cvComment)
    }
}