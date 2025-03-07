/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import at.techbee.jtx.ui.settings.DropdownSettingOption
import net.fortuna.ical4j.model.Recur
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone


class ICalObjectTest {


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
    fun setUpdatedProgress_0() {
        val task = ICalObject.createTask("setUpdatedProgress_0")
        task.setUpdatedProgress(0, true)

        assertEquals("setUpdatedProgress_0", task.summary)
        assertNull(task.percent)
        assertEquals(Status.NEEDS_ACTION.status, task.status)
        assertNotNull(task.lastModified)
        assertEquals(true, task.dirty)
    }

    @Test
    fun setUpdatedProgress_keep_in_process() {
        val task = ICalObject.createTask("setUpdatedProgress_needs_action_in_Progress")
        task.status = Status.NEEDS_ACTION.status
        task.setUpdatedProgress(1, true)
        task.setUpdatedProgress(0, true)

        assertEquals("setUpdatedProgress_needs_action_in_Progress", task.summary)
        assertNull(task.percent)
        assertEquals(Status.NEEDS_ACTION.status, task.status)
        //assertNotNull(task.dtstart)
        //assertNull(task.completed)
        assertNotNull(task.lastModified)
        assertEquals(2, task.sequence)
        assertEquals(true, task.dirty)
    }

    @Test
    fun setUpdatedProgress_in_Process() {
        val task = ICalObject.createTask("setUpdatedProgress_in_Progress")
        task.setUpdatedProgress(50, true)

        assertEquals("setUpdatedProgress_in_Progress", task.summary)
        assertEquals(50, task.percent)
        assertEquals(Status.IN_PROCESS.status, task.status)
        //assertNull(task.status)
        //assertNotNull(task.dtstart)
        //assertNull(task.completed)
        assertNotNull(task.lastModified)
        assertEquals(1, task.sequence)
        assertEquals(true, task.dirty)
    }

    @Test
    fun setUpdatedProgress_completed() {
        val task = ICalObject.createTask("setUpdatedProgress_completed")
        task.status = Status.NEEDS_ACTION.status
        task.setUpdatedProgress(100, true)

        assertEquals("setUpdatedProgress_completed", task.summary)
        assertEquals(100, task.percent)
        assertEquals(Status.COMPLETED.status, task.status)
        //assertNotNull(task.dtstart)
        //assertNotNull(task.completed)
        assertNotNull(task.lastModified)
        assertEquals(1, task.sequence)
        assertEquals(true, task.dirty)
    }

    @Test
    fun setUpdatedProgress_completed1() {
        val task = ICalObject.createTask("setUpdatedProgress")
        task.dtstartTimezone = null
        task.dtstart = null
        task.dueTimezone = null
        task.due = null
        task.setUpdatedProgress(100, true)
        assertNull(task.completedTimezone)
        assertNotNull(task.completed)
    }

    @Test
    fun setUpdatedProgress_completed2() {
        val task = ICalObject.createTask("setUpdatedProgress")
        task.dtstartTimezone = TZ_ALLDAY
        task.setUpdatedProgress(100, true)
        assertEquals(TZ_ALLDAY, task.completedTimezone)
        assertNotNull(task.completed)
    }

    @Test
    fun setUpdatedProgress_completed2_no_sync() {
        val task = ICalObject.createTask("setUpdatedProgress")
        task.dtstartTimezone = TZ_ALLDAY
        task.setUpdatedProgress(100, false)
        assertNull(task.completed)
        assertNull(task.status)
    }

    @Test
    fun setUpdatedProgress_completed_reset() {
        val task = ICalObject.createTask("setUpdatedProgress")
        task.dtstartTimezone = TZ_ALLDAY
        task.setUpdatedProgress(100, true)
        assertEquals(TZ_ALLDAY, task.completedTimezone)
        assertNotNull(task.completed)
        task.setUpdatedProgress(32, true)
        assertNull(task.completedTimezone)
        assertNull(task.completed)
    }


    @Test
    fun factory_createJournal() {

        val factoryObject = ICalObject.createJournal()
        val createdObject = ICalObject(
            component = Component.VJOURNAL.name,
            module = Module.JOURNAL.name,
            status = Status.FINAL.status,
            dirty = true,
            // dates and uid must be set explicitely to make the objects equal
            dtstart = factoryObject.dtstart,
            dtstartTimezone = TZ_ALLDAY,
            created = factoryObject.created,
            lastModified = factoryObject.lastModified,
            dtstamp = factoryObject.dtstamp,
            uid = factoryObject.uid,
        )
        assertEquals(createdObject, factoryObject)
    }

    @Test
    fun factory_createNote() {

        val factoryObject = ICalObject.createNote()
        val createdObject = ICalObject(
            component = Component.VJOURNAL.name,
            module = Module.NOTE.name,
            status = Status.FINAL.status,
            dirty = true,
            // dates and uid must be set explicitely to make the objects equal
            dtstart = factoryObject.dtstart,
            created = factoryObject.created,
            lastModified = factoryObject.lastModified,
            dtstamp = factoryObject.dtstamp,
            uid = factoryObject.uid,
        )
        assertEquals(createdObject, factoryObject)
    }

    @Test
    fun factory_createNote_withoutSummary() {

        val factoryObject = ICalObject.createNote()
        val createdObject = ICalObject(
            component = Component.VJOURNAL.name,
            module = Module.NOTE.name,
            status = Status.FINAL.status,
            dirty = true,
            // dates and uid must be set explicitely to make the objects equal
            dtstart = factoryObject.dtstart,
            created = factoryObject.created,
            lastModified = factoryObject.lastModified,
            dtstamp = factoryObject.dtstamp,
            uid = factoryObject.uid,
        )
        assertEquals(createdObject, factoryObject)
    }

    @Test
    fun factory_createNote_withSummary() {

        val factoryObject = ICalObject.createNote("Test Summary")
        val createdObject = ICalObject(
            component = Component.VJOURNAL.name,
            module = Module.NOTE.name,
            status = Status.FINAL.status,
            dirty = true,
            summary = "Test Summary",
            // dates and uid must be set explicitely to make the objects equal
            dtstart = factoryObject.dtstart,
            created = factoryObject.created,
            lastModified = factoryObject.lastModified,
            dtstamp = factoryObject.dtstamp,
            uid = factoryObject.uid,
        )
        assertEquals(createdObject, factoryObject)
    }

    @Test
    fun factory_createTodo() {

        val factoryObject = ICalObject.createTodo()
        val createdObject = ICalObject(
            component = Component.VTODO.name,
            module = Module.TODO.name,
            status = null,
            percent = null,
            priority = null,
            dueTimezone = TZ_ALLDAY,
            completedTimezone = TZ_ALLDAY,
            dirty = true,
            // dates and uid must be set explicitely to make the objects equal
            dtstart = factoryObject.dtstart,
            dtstartTimezone = TZ_ALLDAY,
            created = factoryObject.created,
            lastModified = factoryObject.lastModified,
            dtstamp = factoryObject.dtstamp,
            uid = factoryObject.uid,
        )
        assertEquals(createdObject, factoryObject)
    }

    @Test
    fun factory_createTodo_withSummary() {

        val factoryObject = ICalObject.createTask("Task Summary")
        val createdObject = ICalObject(
            component = Component.VTODO.name,
            module = Module.TODO.name,
            //status = StatusTodo.`NEEDS-ACTION`.name,
            percent = null,
            priority = null,
            dueTimezone = TZ_ALLDAY,
            completedTimezone = TZ_ALLDAY,
            dirty = true,
            summary = "Task Summary",
            // dates and uid must be set explicitely to make the objects equal
            dtstart = factoryObject.dtstart,
            dtstartTimezone = TZ_ALLDAY,
            created = factoryObject.created,
            lastModified = factoryObject.lastModified,
            dtstamp = factoryObject.dtstamp,
            uid = factoryObject.uid,
        )
        assertEquals(createdObject, factoryObject)
    }

    @Test
    fun getRecurId_date() {
        val sampleDate = 1632434400000L   // 2021-09-24
        val recurId = ICalObject.getRecurId(sampleDate, TZ_ALLDAY)
        assertEquals("20210923", recurId)
    }

/* This test would fail in Gitlab as it might take another timezone for the assertion, so it stays deactivated
    @Test
    fun getRecurId_datetime() {

        val sampleDate = 1632474660000L   // 2021-09-24 11:11:00
        val recurId = ICalObject.getRecurId(sampleDate, null)
        assertEquals("20210924T111100", recurId)
    }
 */

/*
    @Test
    fun getRecurId_datetime_withTimezone() {
        val sampleDate = 1632474660000L   // 2021-09-24 11:11:00
        val recurId = ICalObject.getRecurId(sampleDate, "Africa/Banjul")
        assertEquals("20210924T091100;TZID=Africa/Banjul", recurId)
    }
 */


    @Test
    fun getRecur1() {
        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622494800000L
            this.rrule = "FREQ=YEARLY;COUNT=3;INTERVAL=2"
        }
        val recur = item.getRecur()
        assertEquals(Recur.Frequency.YEARLY, recur?.frequency)
        assertEquals(3, recur?.count)
        assertEquals(2, recur?.interval)
    }

    @Test
    fun getRecur_empty() {
        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622494800000L
            this.rrule = null
        }
        val recur = item.getRecur()
        assertNull(recur)
    }

    @Test
    fun getRecur_null() {
        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622494800000L
            this.rrule = "asdf"
        }
        val recur = item.getRecur()
        assertNull(recur)
    }

    @Test
    fun getInstancesFromRrule_Journal_YEARLY() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622494800000L
            this.dtstartTimezone = null
            this.rrule = "FREQ=YEARLY;COUNT=3;INTERVAL=2"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(3,recurList.size)
        assertEquals(1622494800000L, recurList[0])
        assertEquals(1685566800000L, recurList[1])
        assertEquals(1748725200000L, recurList[2])
    }

    @Test
    fun getInstancesFromRrule_Todo_YEARLY() {

        val item = ICalObject.createTodo().apply {
            this.dtstart = 1622494800000L
            this.dtstartTimezone = null
            this.due = 1622494843210L
            this.rrule = "FREQ=YEARLY;COUNT=3;INTERVAL=2"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(3,recurList.size)
        assertEquals(1622494800000L, recurList[0])
        assertEquals(1685566800000L, recurList[1])
        assertEquals(1748725200000L, recurList[2])
    }



    @Test
    fun getInstancesFromRrule_Journal_MONTHLY() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622505600000L
            this.rrule = "FREQ=MONTHLY;COUNT=3;INTERVAL=1;BYMONTHDAY=5"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(3,recurList.size)
        assertEquals(1622851200000L, recurList[0])
        assertEquals(1625443200000L, recurList[1])
        assertEquals(1628121600000L, recurList[2])
    }


    @Test
    fun getInstancesFromRrule_TODO_MONTHLY() {

        val item = ICalObject.createTodo().apply {
            this.dtstart = 1622541600000L
            this.dtstartTimezone = null
            this.due = 1622541650000L
            this.rrule = "FREQ=MONTHLY;COUNT=2;INTERVAL=2;BYMONTHDAY=5"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(2,recurList.size)
        //assertEquals(1622494800000L, recurList[0])
        assertEquals(1622887200000L, recurList[0])
        assertEquals(1628157600000L, recurList[1])
    }


/*
    @Test
    fun getInstancesFromRrule_Journal_WEEKLY() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622764800000L
            this.dtstartTimezone = "GMT"
            this.rrule = "FREQ=WEEKLY;COUNT=6;INTERVAL=2;BYDAY=FR,SA,SU"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(6,recurList.size)
        assertEquals(1622764800000L, recurList[0])
        assertEquals(1622851200000L, recurList[1])
        assertEquals(1622937600000L, recurList[2])
        assertEquals(1623974400000L, recurList[3])
        assertEquals(1624060800000L, recurList[4])
        assertEquals(1624147200000L, recurList[5])
    }
 */

/*
    @Test
    fun getInstancesFromRrule_Journal_WEEKLY_withExceptions() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622541600000L
            this.dtstartTimezone = "Europe/Vienna"
            this.rrule = "FREQ=WEEKLY;COUNT=6;INTERVAL=2;BYDAY=FR,SA,SU"     // TUesday is also considered as DTSTART is on a Tuesday!
            this.exdate = "1622973600000,1624096800000"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(4,recurList.size)
        assertEquals(1622800800000L, recurList[0])
        assertEquals(1622887200000L, recurList[1])
        assertEquals(1624010400000L, recurList[2])
        assertEquals(1624183200000L, recurList[3])
    }
 */

    /*
    @Test
    fun getInstancesFromRrule_Journal_WEEKLY_withExceptions_andAdditions() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622541600000L
            this.dtstartTimezone = "UTC"
            this.rrule = "FREQ=WEEKLY;COUNT=8;INTERVAL=2;BYDAY=TU,FR,SA,SU"
            this.exdate = "1622973600000,1624096800000"
            this.rdate = "1651410000000,1651356000000"
        }

        val recurList = item.getInstancesFromRrule()
        //assertEquals(8,recurList.size)
        assertEquals(1622541600000L, recurList[0])
        assertEquals(1622800800000L, recurList[1])
        assertEquals(1622887200000L, recurList[2])
        assertEquals(1623751200000L, recurList[3])
        assertEquals(1624010400000L, recurList[4])
        assertEquals(1624183200000L, recurList[5])
        assertEquals(1651356000000L, recurList[6])
        assertEquals(1651410000000L, recurList[7])
    }
     */



    @Test
    fun getInstancesFromRrule_Todo_WEEKLY() {

        val item = ICalObject.createTodo().apply {
            this.dtstart = 1641045600000L
            this.due = 1641045605000L
            this.rrule = "FREQ=WEEKLY;COUNT=3;INTERVAL=1;BYDAY=MO"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(3,recurList.size)
    }

    /*
     @Test
     fun getInstancesFromRrule_Journal_DAILY() {

         val item = ICalObject.createJournal().apply {
             this.dtstart = 1622494800000L
             this.dtstartTimezone = "Europe/Vienna"
             this.rrule = "FREQ=DAILY;COUNT=4;INTERVAL=4"
         }

         val recurList = item.getInstancesFromRrule()
         assertEquals(4,recurList.size)
         assertEquals(1622494800000L, recurList[0])
         assertEquals(1622840400000L, recurList[1])
         assertEquals(1623186000000L, recurList[2])
         assertEquals(1623531600000L, recurList[3])
     }


     @Test
     fun getInstancesFromRrule_Journal_DAILY_withAllday() {

         val item = ICalObject.createJournal().apply {
             this.dtstart = 1622505600000L
             this.dtstartTimezone = TZ_ALLDAY
             this.rrule = "FREQ=DAILY;COUNT=4;INTERVAL=4"
         }

         val recurList = item.getInstancesFromRrule()
         assertEquals(4,recurList.size)
         //assertEquals(1622505600000L, recurList[0])
         //assertEquals(1622851200000L, recurList[1])
         //assertEquals(1623196800000L, recurList[2])
         //assertEquals(1623542400000L, recurList[3])
     }


     @Test
     fun getInstancesFromRrule_Todo_DAILY() {

         val item = ICalObject.createTodo().apply {
             this.dtstart = 1622541600000L
             this.dtstartTimezone = "Europe/Vienna"
             this.due = 1622541600000L
             this.rrule = "FREQ=DAILY;COUNT=2;INTERVAL=4"
         }

         val recurList = item.getInstancesFromRrule()
         assertEquals(2,recurList.size)
         //assertEquals(1622541600000L, recurList[0])
         //assertEquals(1622887200000L, recurList[1])
     }
      */


    @Test
    fun getInstancesFromRrule_unsupported_TodoWithDue() {

        val item = ICalObject.createTodo().apply {
            //this.dtstart = 1622494801230L
            this.due = 1622541600000L
            this.rrule = "FREQ=DAILY;COUNT=2;INTERVAL=4"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(0,recurList.size)
    }

    @Test
    fun getInstancesFromRrule_unsupported_Note() {

        val item = ICalObject.createNote().apply {
            //this.dtstart = 1622494801230L
            //this.due = 1622541600000L
            this.rrule = "FREQ=DAILY;COUNT=2;INTERVAL=4"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(0,recurList.size)
    }

    @Test
    fun getInstancesFromRrule_weekly_until() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1652707613327L
            this.rrule = "FREQ=WEEKLY;UNTIL=20220614T220000Z"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(5,recurList.size)
    }

    @Test
    fun getInstancesFromRrule_unsupported_weekly_until_byday() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1652788800000L
            this.rrule = "FREQ=WEEKLY;UNTIL=20220614T220000Z;BYDAY=TU,TH"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(9,recurList.size)
    }



    @Test
    fun getInstancesFromRrule_daily_until() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1652707613327L
            this.rrule = "FREQ=WEEKLY;UNTIL=20220730"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(11,recurList.size)
    }

    @Test
    fun getInstancesFromRrule_daily_until_allday() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1652659200000L
            this.dtstartTimezone = TZ_ALLDAY
            this.rrule = "FREQ=DAILY;UNTIL=20220519"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(4,recurList.size)
    }



    @Test
    fun getInstancesFromRrule_faultyRule() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622494801230L
            this.rrule = "FREQ=DAILY;COUNT=2;INTERVAL=4;WHATEVER"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(0,recurList.size)
    }



    @Test
    fun classification_getListFromStringList() {

        val classList = Classification.getListFromStringList(listOf("PRIVATE", "PUBLIC", "CONFIDENTIAL").toSet())
        assertTrue(classList.contains(Classification.CONFIDENTIAL))
        assertTrue(classList.contains(Classification.PRIVATE))
        assertTrue(classList.contains(Classification.PUBLIC))
    }

    @Test
    fun status_getListFromStringList_Journal() {

        val statusList = Status.getListFromStringList(listOf("DRAFT", "FINAL", "CANCELLED").toSet())
        assertTrue(statusList.contains(Status.CANCELLED))
        assertTrue(statusList.contains(Status.DRAFT))
        assertTrue(statusList.contains(Status.FINAL))
    }

    @Test
    fun status_getListFromStringList_Todo() {

        val statusList = Status.getListFromStringList(listOf("CANCELLED", "IN-PROCESS", "COMPLETED", "NEEDS-ACTION").toSet())
        assertTrue(statusList.contains(Status.CANCELLED))
        assertTrue(statusList.contains(Status.IN_PROCESS))
        assertTrue(statusList.contains(Status.COMPLETED))
        assertTrue(statusList.contains(Status.NEEDS_ACTION))
    }

    @Test
    fun classification_getStringSetFromList() {
        val classifications = listOf(Classification.PUBLIC, Classification.PRIVATE, Classification.CONFIDENTIAL)
        assertEquals(listOf("PUBLIC", "PRIVATE", "CONFIDENTIAL").toSet(), Classification.getStringSetFromList(classifications))
    }

    @Test
    fun statusJournal_getStringSetFromList() {
        val statusJournals = listOf(Status.FINAL, Status.DRAFT, Status.CANCELLED)
        assertEquals(listOf("DRAFT", "FINAL", "CANCELLED").toSet(), Status.getStringSetFromList(statusJournals))
    }

    @Test
    fun statusTodo_getStringSetFromList() {
        val statusTodos = listOf(Status.NEEDS_ACTION, Status.COMPLETED, Status.IN_PROCESS, Status.CANCELLED)
        assertEquals(listOf("CANCELLED", "IN-PROCESS", "COMPLETED", "NEEDS-ACTION").toSet(), Status.getStringSetFromList(statusTodos))
    }

    @Test
    fun status_getStringSetFromList_Journals() {
        val status = Status.valuesFor(Module.JOURNAL)
        assertEquals(listOf("NO_STATUS", "DRAFT", "FINAL", "CANCELLED").toSet(), Status.getStringSetFromList(status))
    }

    @Test
    fun status_getStringSetFromList_Todos() {
        val status = Status.valuesFor(Module.TODO)
        assertEquals(listOf("NO_STATUS", "CANCELLED", "IN-PROCESS", "COMPLETED", "NEEDS-ACTION").toSet(), Status.getStringSetFromList(status))
    }

    @Test
    fun getValidTimezoneOrNull_getNull() = assertNull(ICalObject.getValidTimezoneOrNull(null))

    @Test
    fun getValidTimezoneOrNull_getTZ_ALLDAY() = assertEquals(TZ_ALLDAY, ICalObject.getValidTimezoneOrNull(TZ_ALLDAY))

    @Test
    fun getValidTimezoneOrNull_getTZ_ValidTZ() = assertEquals(TimeZone.getTimeZone("Europe/Vienna").id, ICalObject.getValidTimezoneOrNull(TimeZone.getTimeZone("Europe/Vienna").id))

    @Test
    fun getValidTimezoneOrNull_getTZ_InvalidTZ() = assertEquals("GMT", ICalObject.getValidTimezoneOrNull(TimeZone.getTimeZone("Invalid").id))

    @Test
    fun parseSummaryAndDescriptionTest() {
        val textSummary = "This should be in the #summary"
        val textDescription = "This should be in the description\nAdding further #lines\nand #categories here\n"
        val text = textSummary + System.lineSeparator() + textDescription

        val journal = ICalObject.createJournal()
        journal.parseSummaryAndDescription(text)

        assertEquals(textSummary, journal.summary)
        assertEquals(textDescription, journal.description)
    }

    @Test
    fun parseURLTest() {
        val text = "This should be in the #summary." +
                " This should be in the description\n" +
                "This is my link https://www.orf.at/ " +
                " Adding further #lines\nand #categories here\n"

        val journal = ICalObject.createJournal()
        journal.parseURL(text)

        assertEquals("https://www.orf.at", journal.url)
    }

    @Test
    fun parseURLTest2() {
        val text = "This should be in the #summary." +
                " This should be in the description\n" +
                "This is my link www.orf.at" +
                " Adding further #lines\nand #categories here\n"

        val journal = ICalObject.createJournal()
        journal.parseURL(text)

        assertEquals("www.orf.at", journal.url)
    }

    @Test
    fun parseURLTest3() {
        val text = "This should be in the #summary." +
                " This should be in the description\n" +
                "This is my link https://orf.at" +
                " Adding further #lines\nand #categories here\n"

        val journal = ICalObject.createJournal()
        journal.parseURL(text)

        assertEquals("https://orf.at", journal.url)
    }

    @Test
    fun parseLatLng_Test_AppleMaps() {
        val text = "https://maps.apple.com/?daddr=51.53941363621893,3.866636929370127"
        val journal = ICalObject.createJournal()
        journal.parseLatLng(text)
        assertEquals(51.53941363621893, journal.geoLat)
        assertEquals(3.866636929370127, journal.geoLong)
    }

    @Test
    fun parseLatLng_Test_GoogleMaps() {
        val text = "https://www.google.at/maps/@50.8557355,4.42243,14z"
        val journal = ICalObject.createJournal()
        journal.parseLatLng(text)
        assertEquals(50.8557355, journal.geoLat)
        assertEquals(4.42243, journal.geoLong)
    }

    @Test
    fun parseLatLng_Test_BingMaps() {
        val text = "https://www.bing.com/maps?FORM=Z9LH2&cp=50.854125%7E4.271001&lvl=14.0"
        val journal = ICalObject.createJournal()
        journal.parseLatLng(text)
        assertEquals(50.854125, journal.geoLat)
        assertEquals(4.271001, journal.geoLong)
    }

    @Test
    fun parseLatLng_Test_OpenStreetMaps() {
        val text = "https://www.openstreetmap.org/#map=13/50.8047/4.5844"
        val journal = ICalObject.createJournal()
        journal.parseLatLng(text)
        assertEquals(50.8047, journal.geoLat)
        assertEquals(4.5844, journal.geoLong)
    }

    @Test
    fun parseLatLng_Test2_faulty() {
        val text = "https://maps.apple.com/?daddr=51.53941363621893,"
        val journal = ICalObject.createJournal()
        journal.parseLatLng(text)
        assertNull(journal.geoLat)
        assertNull(journal.geoLong)
    }

    @Test
    fun parseLatLng_Test3_empty() {
        val text = "https://maps.apple.com/?daddr=51.53941363621893,"
        val journal = ICalObject.createJournal()
        journal.parseLatLng(text)
        assertNull(journal.geoLat)
        assertNull(journal.geoLong)
    }

    @Test fun getModuleFromString_journal() = assertEquals(Module.JOURNAL, ICalObject.createJournal().getModuleFromString())
    @Test fun getModuleFromString_note() = assertEquals(Module.NOTE, ICalObject.createNote().getModuleFromString())
    @Test fun getModuleFromString_task() = assertEquals(Module.TODO, ICalObject.createTodo().getModuleFromString())
    @Test fun getModuleFromString_invalid() = assertEquals(Module.NOTE, ICalObject.createJournal().apply { this.module = "asdf" }.getModuleFromString())


    @Test fun getLatLongString1() = assertEquals("(1.11100, 12345.12312)", ICalObject.getLatLongString(1.111, 12345.123123123))
    @Test fun getLatLongString_null() = assertNull(ICalObject.getLatLongString(null, 2.222))

    /*
    @Test fun getAsRecurId_1() {
        assertEquals("20230101T000000", ICalObject.getAsRecurId(1672527600000L, "Europe/Vienna"))
    }
     */
    @Test fun getAsRecurId_ALLDAY() = assertEquals("20230101", ICalObject.getAsRecurId(1672531200000, TZ_ALLDAY))

    @Test fun setDefaultStartDateFromSettings_TestStart() {
        val iCalObject = ICalObject.createTodo()
        iCalObject.setDefaultStartDateFromSettings(
            DropdownSettingOption.DEFAULT_DATE_SAME_DAY,
            LocalTime.of(15,0),
            "GMT"
        )

        val now = LocalDateTime.now().withHour(15).withMinute(0)
        val entryStart = ZonedDateTime.ofInstant(Instant.ofEpochMilli(iCalObject.dtstart!!), ZoneId.of("UTC")).toLocalDateTime()
        assertEquals(now.year, entryStart.year)
        assertEquals(now.monthValue, entryStart.monthValue)
        assertEquals(now.dayOfMonth, entryStart.dayOfMonth)
        assertEquals(now.hour, entryStart.hour)
        assertEquals(now.minute, entryStart.minute)
    }

    @Test fun setDefaultDueDateFromSettings_TestDue() {
        val iCalObject = ICalObject.createTodo()
        iCalObject.setDefaultDueDateFromSettings(
            DropdownSettingOption.DEFAULT_DATE_NEXT_DAY,
            LocalTime.of(22,15),
            "GMT"
        )

        val now = LocalDateTime.now().plusDays(1).withHour(22).withMinute(15)
        val entryStart = ZonedDateTime.ofInstant(Instant.ofEpochMilli(iCalObject.due!!), ZoneId.of("UTC")).toLocalDateTime()
        assertEquals(now.year, entryStart.year)
        assertEquals(now.monthValue, entryStart.monthValue)
        assertEquals(now.dayOfMonth, entryStart.dayOfMonth)
        assertEquals(now.hour, entryStart.hour)
        assertEquals(now.minute, entryStart.minute)
    }

    @Test fun setDefaultJournalDateFromSettings_Test() {
        val iCalObject = ICalObject.createJournal()
        iCalObject.setDefaultJournalDateFromSettings(
            DropdownSettingOption.DEFAULT_JOURNALS_DATE_PREVIOUS_DAY
        )

        val now = LocalDateTime.now().minusDays(1)
        val entryStart = ZonedDateTime.ofInstant(Instant.ofEpochMilli(iCalObject.dtstart!!), ZoneId.of("UTC")).toLocalDateTime()
        assertEquals(now.year, entryStart.year)
        assertEquals(now.monthValue, entryStart.monthValue)
        assertEquals(now.dayOfMonth, entryStart.dayOfMonth)
        assertEquals(TZ_ALLDAY, iCalObject.dtstartTimezone)
    }
}