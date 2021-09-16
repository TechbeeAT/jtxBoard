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
}