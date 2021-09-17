/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

import android.content.ContentValues
import org.junit.Test

import org.junit.Assert.*
import java.util.*

class ICalObjectTest {
// Android Test as Content Values need Android libraries to run


    @Test
    fun createFromContentValues() {

        val sampleICalObject = ICalObject(
            module = Module.JOURNAL.name,
            component = Component.VJOURNAL.name,
            summary = "Summary",
            description = "Description",
            dtstart = System.currentTimeMillis(),
            dtstartTimezone = TimeZone.getTimeZone("Europe/Vienna").displayName,
            dtend = null,
            dtendTimezone = null,
            status = StatusJournal.FINAL.name,
            classification = Classification.PUBLIC.name,
            url = "https://techbee.at",
            contact = "Techbee",
            geoLat = 48.210033F,
            geoLong = 16.363449F,
            location = "Vienna",
            percent = null,
            due = null,
            dueTimezone = null,
            completed = null,
            completedTimezone = null,
            duration = null,
            uid = ICalObject.generateNewUID(),
            created = System.currentTimeMillis(),
            dtstamp = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis(),
            sequence = 0,
            color = 0,
            other = "Other",
            collectionId = 1L,
            dirty = true,
            deleted = false,
            fileName = "test.ics",
            eTag = "eTag",
            scheduleTag = "scheduleTag",
            flags = 0
        )

        val cv = ContentValues(34).apply {
            put(COLUMN_MODULE, sampleICalObject.module)
            put(COLUMN_SUMMARY, sampleICalObject.summary)
            put(COLUMN_DESCRIPTION, sampleICalObject.description)
            put(COLUMN_DTSTART, sampleICalObject.dtstart)
            put(COLUMN_DTSTART_TIMEZONE, sampleICalObject.dtstartTimezone)
            put(COLUMN_DTEND, sampleICalObject.dtend)
            put(COLUMN_DTEND_TIMEZONE, sampleICalObject.dtendTimezone)
            put(COLUMN_STATUS, sampleICalObject.status)
            put(COLUMN_CLASSIFICATION, sampleICalObject.classification)
            put(COLUMN_URL, sampleICalObject.url)
            put(COLUMN_CONTACT, sampleICalObject.contact)
            put(COLUMN_GEO_LAT, sampleICalObject.geoLat)
            put(COLUMN_GEO_LONG, sampleICalObject.geoLong)
            put(COLUMN_LOCATION, sampleICalObject.location)
            put(COLUMN_PERCENT, sampleICalObject.percent)
            put(COLUMN_DUE, sampleICalObject.due)
            put(COLUMN_DUE_TIMEZONE, sampleICalObject.dueTimezone)
            put(COLUMN_COMPLETED, sampleICalObject.completed)
            put(COLUMN_COMPLETED_TIMEZONE, sampleICalObject.completedTimezone)
            put(COLUMN_DURATION, sampleICalObject.duration)
            put(COLUMN_UID, sampleICalObject.uid)
            put(COLUMN_CREATED, sampleICalObject.created)
            put(COLUMN_DTSTAMP, sampleICalObject.dtstamp)
            put(COLUMN_LAST_MODIFIED, sampleICalObject.lastModified)
            put(COLUMN_SEQUENCE, sampleICalObject.sequence)
            put(COLUMN_COLOR, sampleICalObject.color)
            put(COLUMN_OTHER, sampleICalObject.other)
            put(COLUMN_ICALOBJECT_COLLECTIONID, sampleICalObject.collectionId)
            put(COLUMN_DIRTY, sampleICalObject.dirty)
            put(COLUMN_DELETED, sampleICalObject.deleted)
            put(COLUMN_FILENAME, sampleICalObject.fileName)
            put(COLUMN_ETAG, sampleICalObject.eTag)
            put(COLUMN_SCHEDULETAG, sampleICalObject.scheduleTag)
            put(COLUMN_FLAGS, sampleICalObject.flags)
        }

        val cvICalObject = ICalObject.fromContentValues(cv)
        assertEquals(sampleICalObject, cvICalObject)
    }

    @Test
    fun createFromContentValuesWithoutValues() {

        val cvICalObject = ICalCollection.fromContentValues(null)
        assertNull(cvICalObject)
    }

    @Test
    fun generateNewUIDTest() {
        val uid = ICalObject.generateNewUID()
        assertTrue(uid.isNotBlank())
    }



    @Test
    fun setUpdatedProgress_no_change() {
        val task = ICalObject.createTask("setUpdatedProgress_no_change")
        task.setUpdatedProgress(0)

        assertEquals("setUpdatedProgress_no_change", task.summary)
        assertEquals(0, task.percent)
        assertEquals(StatusTodo.`NEEDS-ACTION`.name, task.status)
        assertNull(task.dtstart)
        assertNull(task.completed)
        assertNotNull(task.lastModified)
        assertEquals(0, task.sequence)
        assertEquals(true, task.dirty)
    }


    @Test
    fun setUpdatedProgress_needs_action() {
        val task = ICalObject.createTask("setUpdatedProgress_needs_action_in_Progress")
        task.setUpdatedProgress(1)
        task.setUpdatedProgress(0)

        assertEquals("setUpdatedProgress_needs_action_in_Progress", task.summary)
        assertEquals(0, task.percent)
        assertEquals(StatusTodo.`NEEDS-ACTION`.name, task.status)
        assertNotNull(task.dtstart)
        assertNull(task.completed)
        assertNotNull(task.lastModified)
        assertEquals(2, task.sequence)
        assertEquals(true, task.dirty)
    }

    @Test
    fun setUpdatedProgress_in_Process() {
        val task = ICalObject.createTask("setUpdatedProgress_in_Progress")
        task.setUpdatedProgress(50)

        assertEquals("setUpdatedProgress_in_Progress", task.summary)
        assertEquals(50, task.percent)
        assertEquals(StatusTodo.`IN-PROCESS`.name, task.status)
        assertNotNull(task.dtstart)
        assertNull(task.completed)
        assertNotNull(task.lastModified)
        assertEquals(1, task.sequence)
        assertEquals(true, task.dirty)
    }

    @Test
    fun setUpdatedProgress_completed() {
        val task = ICalObject.createTask("setUpdatedProgress_completed")
        task.setUpdatedProgress(100)

        assertEquals("setUpdatedProgress_completed", task.summary)
        assertEquals(100, task.percent)
        assertEquals(StatusTodo.COMPLETED.name, task.status)
        assertNotNull(task.dtstart)
        assertNotNull(task.completed)
        assertNotNull(task.lastModified)
        assertEquals(1, task.sequence)
        assertEquals(true, task.dirty)
    }
}