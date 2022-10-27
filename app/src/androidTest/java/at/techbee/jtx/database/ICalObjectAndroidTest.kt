/*
 * Copyright (c) Techbee e.U.
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
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class ICalObjectAndroidTest {
// Android Test as Content Values need Android libraries to run


    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ICalDatabase.switchToInMemory(context)
        database = ICalDatabase.getInstance(context).iCalDatabaseDao

        database.insertCollectionSync(
            ICalCollection(
                collectionId = 1L,
                displayName = "testcollection automated tests"
            )
        )
        database.insertCollectionSync(
            ICalCollection(
                collectionId = 2L,
                accountType = "remote",
                accountName = "remote",
                displayName = "testcollection automated tests"
            )
        )
    }


    @Test
    fun recreateRecurring_journal() = runTest {

        val item = ICalObject.createJournal().apply {
            // from  fun getInstancesFromRrule_Journal_WEEKLY_withExceptions()
            this.collectionId = 1L
            this.dtstart = 1622541600000L
            this.rrule = "FREQ=WEEKLY;COUNT=2;INTERVAL=2;BYDAY=FR,SA,SU"
            this.exdate = "1622973600000,1624096800000"
        }

        val id = database.insertICalObject(item)
        val savedItem = database.getSync(id)
        savedItem?.property?.recreateRecurring(context)

        val recurList = database.getRecurInstances(id)
        assertEquals(6, recurList.size)

        database.deleteRecurringInstances(id)
        val recurListEmpty = database.getRecurInstances(id)
        assertEquals(0, recurListEmpty.size)
    }

    @Test
    fun recreateRecurring_todo() = runTest {

        val item = ICalObject.createTodo().apply {
            // from  fun getInstancesFromRrule_Journal_WEEKLY_withExceptions()
            this.dtstart = 1663718400000L
            this.due = 1663804800000L
            this.rrule = "FREQ=WEEKLY;COUNT=2;INTERVAL=2;BYDAY=SU,MO,WE"
            this.exdate = "1664064000000"
            this.rdate = "1664496000000,1662249600000"
        }

        val id = database.insertICalObject(item)
        val savedItem = database.getSync(id)
        savedItem?.property?.recreateRecurring(context)

        val recurList = database.getRecurInstances(id)
        assertEquals(7, recurList.size)

        assertEquals(1663804800000L, recurList[0]?.due)
        //assertEquals(1664236800000L, recurList[1]?.due)  // TODO

        database.deleteRecurringInstances(id)
        val recurListEmpty = database.getRecurInstances(id)
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
            dtstartTimezone = TimeZone.getTimeZone("Europe/Vienna").id,
            dtend = null,
            dtendTimezone = null,
            status = StatusJournal.FINAL.name,
            classification = Classification.PUBLIC.name,
            url = "https://techbee.at",
            contact = "Techbee",
            geoLat = 48.210033,
            geoLong = 16.363449,
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
    fun deleteItemWithChildren_LocalCollection() = runTest {
        val parent = ICalObject.createJournal().apply { this.collectionId = 1L }
        withContext(Dispatchers.IO) {
            val idParent = database.insertICalObject(parent)
            val idChild1 = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 1L })
            val idChild2 = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 1L })
            val idChild3 = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 1L })

            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild1
                text = parent.uid
                reltype = Reltype.PARENT.name
            })
            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild2
                text = parent.uid
                reltype = Reltype.PARENT.name
            })
            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild3
                text = parent.uid
                reltype = Reltype.PARENT.name
            })

            //make sure everything was correctly inserted
            assertEquals(3, database.getAllRelatedto().getOrAwaitValue().size)

            ICalObject.deleteItemWithChildren(idParent, database)

            assertEquals(0, database.getAllRelatedto().getOrAwaitValue().size)
            assertEquals(null, database.getSync(idParent))
            assertEquals(null, database.getSync(idChild1))
            assertEquals(null, database.getSync(idChild2))
            assertEquals(null, database.getSync(idChild3))
        }
    }

    @Test
    fun deleteItemWithChildren_RemoteCollection() = runTest {
        val parent = ICalObject.createJournal().apply { this.collectionId = 2L }
        withContext(Dispatchers.IO) {
            val idParent = database.insertICalObject(parent)
            val idChild1 = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 2L })
            val idChild2 = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 2L })
            val idChild3 = database.insertICalObject(
                ICalObject.createJournal().apply { this.collectionId = 2L })

            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild1
                text = parent.uid
                reltype = Reltype.PARENT.name
            })
            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild2
                text = parent.uid
                reltype = Reltype.PARENT.name
            })
            database.insertRelatedto(Relatedto().apply {
                icalObjectId = idChild3
                text = parent.uid
                reltype = Reltype.PARENT.name
            })
            //make sure everything was correctly inserted
            assertEquals(3, database.getAllRelatedto().getOrAwaitValue().size)

            ICalObject.deleteItemWithChildren(idParent, database)

            assertTrue(database.getSync(idParent)?.property?.deleted!!)
            assertTrue(database.getSync(idChild1)?.property?.deleted!!)
            assertTrue(database.getSync(idChild2)?.property?.deleted!!)
            assertTrue(database.getSync(idChild3)?.property?.deleted!!)
        }
    }


    @Test
    fun deleteItemWithChildren_RecurringInstance_Local() = runTest {
        //Local or remote should not make a difference, the recurring instance must be deleted anyway
        withContext(Dispatchers.IO) {
            val idParent = database.insertICalObject(ICalObject.createJournal().apply {
                this.collectionId = 1L
                this.isRecurLinkedInstance = true
            })
            // a recurring instance cannot have children
            //make sure everything was correctly inserted

            assertNotNull(database.getSync(idParent)?.property)
            ICalObject.deleteItemWithChildren(idParent, database)
            assertNull(database.getSync(idParent)?.property)
        }
    }

    @Test
    fun deleteItemWithChildren_RecurringInstance_Remote() = runTest {
        //Local or remote should not make a difference, the recurring instance must be deleted anyway
        withContext(Dispatchers.IO) {
            val idParent = database.insertICalObject(ICalObject.createJournal().apply {
                this.collectionId = 2L
                this.isRecurLinkedInstance = true
            })
            // a recurring instance cannot have children
            //make sure everything was correctly inserted

            assertNotNull(database.getSync(idParent)?.property)
            ICalObject.deleteItemWithChildren(idParent, database)
            assertNull(database.getSync(idParent)?.property)
        }
    }

    @Test
    fun updateCollectionWithChildren_test() = runTest {
        withContext(Dispatchers.IO) {

            val parent = ICalObject.createJournal().apply { this.collectionId = 1L }
            withContext(Dispatchers.IO) {
                val idParent = database.insertICalObject(parent)
                val idChild1 = database.insertICalObject(
                    ICalObject.createJournal().apply { this.collectionId = 1L })
                val idChild2 = database.insertICalObject(
                    ICalObject.createJournal().apply { this.collectionId = 1L })
                val idChild3 = database.insertICalObject(
                    ICalObject.createJournal().apply { this.collectionId = 1L })

                database.insertRelatedto(Relatedto().apply {
                    icalObjectId = idChild1
                    text = parent.uid
                    reltype = Reltype.PARENT.name
                })
                database.insertRelatedto(Relatedto().apply {
                    icalObjectId = idChild2
                    text = parent.uid
                    reltype = Reltype.PARENT.name
                })
                database.insertRelatedto(Relatedto().apply {
                    icalObjectId = idChild3
                    text = parent.uid
                    reltype = Reltype.PARENT.name
                })

                //make sure everything was correctly inserted
                assertEquals(3, database.getAllRelatedto().getOrAwaitValue().size)

                val newParentId = ICalObject.updateCollectionWithChildren(idParent, null, 2L, database, context)
                Thread.sleep(500)

                val newParent = database.getICalObjectById(newParentId)
                Thread.sleep(100)
                assertEquals(2L, newParent?.collectionId)

                val children = database.getRelatedChildren(newParent?.id ?: 0L)
                assertEquals(3, children.size)
                assertEquals(2L, database.getICalObjectById(children[0])?.collectionId)
                assertEquals(2L, database.getICalObjectById(children[1])?.collectionId)
            }
        }
    }


    @Test
    fun makeRecurringException_Test() = runTest {
        withContext(Dispatchers.IO) {

            val idParent =
                database.insertICalObject(ICalObject.createJournal().apply {
                    this.collectionId = 2L
                    this.rrule = "FREQ=DAILY;COUNT=5;INTERVAL=1"
                })
            val parent = database.getICalObjectById(idParent)
            parent!!.recreateRecurring(context)

            val instances = database.getRecurInstances(idParent)

            //make sure instances are there as expected
            assertEquals(5, instances.size)

            ICalObject.makeRecurringException(instances[0]!!, database)
            ICalObject.makeRecurringException(instances[1]!!, database)

            val parentAfterUpdate = database.getICalObjectById(idParent)
            val instance0 = database.getICalObjectById(instances[0]!!.id)
            val instance1 = database.getICalObjectById(instances[1]!!.id)
            val instance2 = database.getICalObjectById(instances[2]!!.id)
            val instance3 = database.getICalObjectById(instances[3]!!.id)
            val instance4 = database.getICalObjectById(instances[4]!!.id)

            assertEquals("${instance0!!.dtstart},${instance1!!.dtstart}",
                parentAfterUpdate!!.exdate
            )
            assertEquals(false, instance0.isRecurLinkedInstance)
            assertEquals(false, instance1.isRecurLinkedInstance)
            assertEquals(true, instance2!!.isRecurLinkedInstance)
            assertEquals(true, instance3!!.isRecurLinkedInstance)
            assertEquals(true, instance4!!.isRecurLinkedInstance)
        }
    }


    @Test
    fun makeRecurringException_Test2() = runTest {

        withContext(Dispatchers.IO) {

            val idParent =
                database.insertICalObject(ICalObject.createJournal().apply {
                    this.collectionId = 2L
                    this.rrule = null
                })

            val parent = database.getICalObjectById(idParent)
            parent!!.recreateRecurring(context)

            val instances = database.getRecurInstances(idParent)

            //make sure no instances were inserted are there as expected
            assertEquals(0, instances.size)

            //nothing should happen here
            ICalObject.makeRecurringException(parent, database)

            val parentAfterUpdate = database.getICalObjectById(idParent)

            assertNull(parentAfterUpdate!!.exdate)
            assertFalse(parentAfterUpdate.isRecurLinkedInstance)
        }
    }

    @Test
    fun getRecurInfo_linkedToSeries() {
        val item = ICalObject.createJournal("Test")
        item.isRecurLinkedInstance = true
        item.recurOriginalIcalObjectId = 1L
        assertTrue(
            item.getRecurInfo(context)
                ?.contains(context.getString(R.string.view_share_part_of_series)) == true
        )
    }


    @Test
    fun getRecurInfo_exceptionToSeries() {
        val item = ICalObject.createJournal("Test")
        item.isRecurLinkedInstance = false
        item.recurOriginalIcalObjectId = 1L
        assertTrue(
            item.getRecurInfo(context)
                ?.contains(context.getString(R.string.view_share_exception_of_series)) == true
        )
    }

    @Test
    fun getRecurInfo_ruleDesc() {
        val item = ICalObject.createJournal("Test")
        item.rrule = "FREQ=DAILY;COUNT=5;INTERVAL=1"
        val expectedString =
            context.getString(R.string.view_share_repeats) + " 1 " + context.getString(R.string.edit_recur_day) + " 5 " + context.getString(
                R.string.edit_recur_x_times
            )
        assertTrue(item.getRecurInfo(context)?.contains(expectedString) == true)
    }
}
