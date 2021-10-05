/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

import android.content.ContentValues
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class ICalObjectAndroidTest {
// Android Test as Content Values need Android libraries to run


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = ICalDatabase.getInMemoryDB(context).iCalDatabaseDao

        database.insertCollectionSync(ICalCollection(collectionId = 1L, displayName = "testcollection automated tests"))
        database.insertCollectionSync(ICalCollection(collectionId = 2L, accountType = "remote", accountName = "remote", displayName = "testcollection automated tests"))

    }


    @Test
    fun recreateRecurring_journal() = runBlockingTest {

        val item = ICalObject.createJournal().apply {
            // from  fun getInstancesFromRrule_Journal_WEEKLY_withExceptions()
            this.collectionId = 1L
            this.dtstart = 1622541600000L
            this.rrule = "FREQ=WEEKLY;COUNT=2;INTERVAL=2;BYDAY=FR,SA,SU"
            this.exdate = "1622973600000,1624096800000"
        }

        val id = database.insertICalObject(item)
        val savedItem = database.getSync(id)
        savedItem?.property?.recreateRecurring(database)

        val recurList = database.getRecurInstances(id).getOrAwaitValue()
        assertEquals(4, recurList.size)

        database.deleteRecurringInstances(id)
        val recurListEmpty = database.getRecurInstances(id).getOrAwaitValue()
        assertEquals(0, recurListEmpty.size)
    }

    @Test
    fun recreateRecurring_todo() = runBlockingTest {

        val item = ICalObject.createTodo().apply {
            // from  fun getInstancesFromRrule_Journal_WEEKLY_withExceptions()
            this.dtstart = 1622541600000L
            this.due = 1622548800000L
            this.rrule = "FREQ=WEEKLY;COUNT=2;INTERVAL=2;BYDAY=FR,SA,SU"
            this.exdate = "1622973600000,1624096800000"
            this.rdate = "1651410000000,1654088400000"
        }

        val id = database.insertICalObject(item)
        val savedItem = database.getSync(id)
        savedItem?.property?.recreateRecurring(database)

        val recurList = database.getRecurInstances(id).getOrAwaitValue()
        assertEquals(6, recurList.size)

        assertEquals(1622808000000L, recurList[0]?.due)
        assertEquals(1622894400000L, recurList[1]?.due)

        database.deleteRecurringInstances(id)
        val recurListEmpty = database.getRecurInstances(id).getOrAwaitValue()
        assertEquals(0, recurListEmpty.size)
    }


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
    fun deleteItemWithChildren_LocalCollection() = runBlockingTest{
        val idParent = database.insertICalObject(ICalObject.createJournal().apply { this.collectionId = 1L })
        val idChild1 = database.insertICalObject(ICalObject.createJournal().apply { this.collectionId = 1L })
        val idChild2 = database.insertICalObject(ICalObject.createJournal().apply { this.collectionId = 1L })
        val idChild3 = database.insertICalObject(ICalObject.createJournal().apply { this.collectionId = 1L })

        database.insertRelatedto(Relatedto().apply {
            icalObjectId = idParent
            linkedICalObjectId = idChild1
            reltype = Reltype.CHILD.name
        })
        database.insertRelatedto(Relatedto().apply {
            icalObjectId = idParent
            linkedICalObjectId = idChild2
            reltype = Reltype.CHILD.name
        })
        database.insertRelatedto(Relatedto().apply {
            icalObjectId = idChild1
            linkedICalObjectId = idChild3
            reltype = Reltype.CHILD.name
        })

        //make sure everything was correctly inserted
        assertEquals(3,database.getAllRelatedto().getOrAwaitValue().size)

        ICalObject.deleteItemWithChildren(idParent, 1L, database)

        assertEquals(0,database.getAllRelatedto().getOrAwaitValue().size)
        assertEquals(null, database.getSync(idParent))
        assertEquals(null, database.getSync(idChild1))
        assertEquals(null, database.getSync(idChild2))
        assertEquals(null, database.getSync(idChild3))
    }

    @Test
    fun deleteItemWithChildren_RemoteCollection() = runBlockingTest {
        val idParent = database.insertICalObject(ICalObject.createJournal().apply { this.collectionId = 2L })
        val idChild1 = database.insertICalObject(ICalObject.createJournal().apply { this.collectionId = 2L })
        val idChild2 = database.insertICalObject(ICalObject.createJournal().apply { this.collectionId = 2L })
        val idChild3 = database.insertICalObject(ICalObject.createJournal().apply { this.collectionId = 2L })

        database.insertRelatedto(Relatedto().apply {
            icalObjectId = idParent
            linkedICalObjectId = idChild1
            reltype = Reltype.CHILD.name
        })
        database.insertRelatedto(Relatedto().apply {
            icalObjectId = idParent
            linkedICalObjectId = idChild2
            reltype = Reltype.CHILD.name
        })
        database.insertRelatedto(Relatedto().apply {
            icalObjectId = idChild1
            linkedICalObjectId = idChild3
            reltype = Reltype.CHILD.name
        })

        //make sure everything was correctly inserted
        assertEquals(3,database.getAllRelatedto().getOrAwaitValue().size)

        ICalObject.deleteItemWithChildren(idParent, 2L, database)

        assertTrue(database.getSync(idParent)?.property?.deleted!!)
        assertTrue(database.getSync(idChild1)?.property?.deleted!!)
        assertTrue(database.getSync(idChild2)?.property?.deleted!!)
        assertTrue(database.getSync(idChild3)?.property?.deleted!!)
    }
}