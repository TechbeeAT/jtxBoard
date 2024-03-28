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
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.BuildFlavor
import at.techbee.jtx.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.TimeZone

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
        database = ICalDatabase.getInstance(context).iCalDatabaseDao()

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
            status = Status.FINAL.status,
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


    /*
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

            ICalObject.unlinkFromSeries(instances[0]!!, database)
            ICalObject.unlinkFromSeries(instances[1]!!, database)

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
            ICalObject.unlinkFromSeries(parent, database)

            val parentAfterUpdate = database.getICalObjectById(idParent)

            assertNull(parentAfterUpdate!!.exdate)
            assertFalse(parentAfterUpdate.isRecurLinkedInstance)
        }
    }
     */


    @Test
    fun getRecurInfo_linkedToSeries() {
        val item = ICalObject.createJournal("Test")
        item.recurid = "recurid"
        assertTrue(
            item.getRecurInfo(context)
                ?.contains(context.getString(R.string.view_share_part_of_series)) == true
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

    @Test fun getMapLink_gplay() {
        assertEquals(
            Uri.parse("https://www.google.com/maps/search/?api=1&query=1.111%2C2.222"),
            ICalObject.getMapLink(1.111, 2.222, null, BuildFlavor.GPLAY)
        )
    }

    @Test fun getMapLink_gplay_location() {
        assertEquals(
            Uri.parse("https://www.google.com/maps/search/urania/"),
            ICalObject.getMapLink(null, null, "urania", BuildFlavor.GPLAY)
        )
    }

    @Test fun getMapLink_ose() {
        assertEquals(
            Uri.parse("https://www.openstreetmap.org/#map=15/1.111/2.222").toString(),
            ICalObject.getMapLink(1.111, 2.222, null, BuildFlavor.OSE).toString()
        )
    }

    @Test fun getMapLink_ose_location() {
        assertEquals(
            Uri.parse("https://www.openstreetmap.org/search?query=urania").toString(),
            ICalObject.getMapLink(null, null, "urania", BuildFlavor.OSE).toString()
        )
    }

    @Test fun getMapLink_empty() {
        assertNull(ICalObject.getMapLink(null, null, null, BuildFlavor.OSE))
    }
}
