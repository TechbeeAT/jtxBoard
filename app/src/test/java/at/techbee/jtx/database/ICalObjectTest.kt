/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

import android.content.Context
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject.Factory.TZ_ALLDAY
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.*


@RunWith(MockitoJUnitRunner::class)
class ICalObjectTest {

    @Mock
    private lateinit var mockContext: Context


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
        assertNull(task.percent)
        assertNull(task.status)
        assertNotNull(task.lastModified)
        assertEquals(true, task.dirty)
    }

    @Test
    fun setUpdatedProgress_needs_action() {
        val task = ICalObject.createTask("setUpdatedProgress_needs_action_in_Progress")
        task.status = StatusTodo.`NEEDS-ACTION`.name
        task.setUpdatedProgress(1)
        task.setUpdatedProgress(0)

        assertEquals("setUpdatedProgress_needs_action_in_Progress", task.summary)
        assertNull(task.percent)
        assertEquals(StatusTodo.`NEEDS-ACTION`.name, task.status)
        //assertNotNull(task.dtstart)
        //assertNull(task.completed)
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
        //assertEquals(StatusTodo.`IN-PROCESS`.name, task.status)
        assertNull(task.status)
        //assertNotNull(task.dtstart)
        //assertNull(task.completed)
        assertNotNull(task.lastModified)
        assertEquals(1, task.sequence)
        assertEquals(true, task.dirty)
    }

    @Test
    fun setUpdatedProgress_completed() {
        val task = ICalObject.createTask("setUpdatedProgress_completed")
        task.status = StatusTodo.`NEEDS-ACTION`.name
        task.setUpdatedProgress(100)

        assertEquals("setUpdatedProgress_completed", task.summary)
        assertEquals(100, task.percent)
        assertEquals(StatusTodo.COMPLETED.name, task.status)
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
        task.setUpdatedProgress(100)
        assertNull(task.completedTimezone)
        assertNotNull(task.completed)
    }

    @Test
    fun setUpdatedProgress_completed2() {
        val task = ICalObject.createTask("setUpdatedProgress")
        task.dtstartTimezone = TZ_ALLDAY
        task.setUpdatedProgress(100)
        assertEquals(TZ_ALLDAY, task.completedTimezone)
        assertNotNull(task.completed)
    }

    @Test
    fun setUpdatedProgress_completed_reset() {
        val task = ICalObject.createTask("setUpdatedProgress")
        task.dtstartTimezone = TZ_ALLDAY
        task.setUpdatedProgress(100)
        assertEquals(TZ_ALLDAY, task.completedTimezone)
        assertNotNull(task.completed)
        task.setUpdatedProgress(32)
        assertNull(task.completedTimezone)
        assertNull(task.completed)
    }


    @Test
    fun factory_createJournal() {

        val factoryObject = ICalObject.createJournal()
        val createdObject = ICalObject(
            component = Component.VJOURNAL.name,
            module = Module.JOURNAL.name,
            status = StatusJournal.FINAL.name,
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
            status = StatusJournal.FINAL.name,
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
            status = StatusJournal.FINAL.name,
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
            status = StatusJournal.FINAL.name,
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


    @Test
    fun getRecurId_datetime_withTimezone() {

        val sampleDate = 1632474660000L   // 2021-09-24 11:11:00
        val recurId = ICalObject.getRecurId(sampleDate, "Africa/Banjul")
        assertEquals("20210924T091100;TZID=Africa/Abidjan", recurId)
    }




    @Test
    fun statusJournal_getStringResource_cancelled() {
        assertEquals(mockContext.getString(R.string.journal_status_cancelled), StatusJournal.getStringResource(mockContext, StatusJournal.CANCELLED.name))
    }

    @Test
    fun statusTodo_getStringResource_cancelled() {
        assertEquals(mockContext.getString(R.string.todo_status_needsaction), StatusTodo.getStringResource(mockContext, StatusTodo.`NEEDS-ACTION`.name))
    }

    @Test
    fun classification_getStringResource_confidential() {
        assertEquals(mockContext.getString(R.string.classification_confidential), Classification.getStringResource(mockContext, Classification.CONFIDENTIAL.name))
    }

    @Test
    fun getInstancesFromRrule_Journal_YEARLY() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622494800000L
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
            this.due = 1622541650000L
            this.rrule = "FREQ=MONTHLY;COUNT=2;INTERVAL=2;BYMONTHDAY=5"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(2,recurList.size)
        //assertEquals(1622494800000L, recurList[0])
        assertEquals(1622887200000L, recurList[0])
        assertEquals(1628157600000L, recurList[1])
    }



    @Test
    fun getInstancesFromRrule_Journal_WEEKLY() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622541600000L
            this.rrule = "FREQ=WEEKLY;COUNT=2;INTERVAL=2;BYDAY=FR,SA,SU"         // TUesday is also considered as DTSTART is on a Tuesday!
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(8,recurList.size)
        /*
        assertEquals(1622800800000L, recurList[0])
        assertEquals(1622887200000L, recurList[1])
        assertEquals(1622973600000L, recurList[2])
        assertEquals(1624010400000L, recurList[3])
        assertEquals(1624096800000L, recurList[4])
        assertEquals(1624183200000L, recurList[5])
         */
    }


    @Test
    fun getInstancesFromRrule_Journal_WEEKLY_withExceptions() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622541600000L
            this.rrule = "FREQ=WEEKLY;COUNT=2;INTERVAL=2;BYDAY=FR,SA,SU"     // TUesday is also considered as DTSTART is on a Tuesday!
            this.exdate = "1622973600000,1624096800000"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(6,recurList.size)
        /*
        assertEquals(1622800800000L, recurList[0])
        assertEquals(1622887200000L, recurList[1])
        assertEquals(1624010400000L, recurList[2])
        assertEquals(1624183200000L, recurList[3])
         */
    }

    @Test
    fun getInstancesFromRrule_Journal_WEEKLY_withExceptions_andAdditions() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622541600000L
            this.rrule = "FREQ=WEEKLY;COUNT=2;INTERVAL=2;BYDAY=TU,FR,SA,SU"
            this.exdate = "1622973600000,1624096800000"
            this.rdate = "1651410000000,1654088400000"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(8,recurList.size)
        //assertEquals(1622800800000L, recurList[0])
        //assertEquals(1622887200000L, recurList[1])
        //assertEquals(1624010400000L, recurList[2])
        //assertEquals(1624183200000L, recurList[3])
        //assertEquals(1651410000000L, recurList[4])
        //assertEquals(1654088400000L, recurList[5])
    }



    @Test
    fun getInstancesFromRrule_Todo_WEEKLY() {

        val item = ICalObject.createTodo().apply {
            this.dtstart = 1641045600000L
            this.due = 1641045605000L
            this.rrule = "FREQ=WEEKLY;COUNT=3;INTERVAL=1;BYDAY=MO"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(6,recurList.size)
    }


    @Test
    fun getInstancesFromRrule_Journal_DAILY() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622494800000L
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
    fun getInstancesFromRrule_Journal_DAILY_withTimezone() {

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
            this.dtstart = 1622494800000L
            this.dtstartTimezone = TZ_ALLDAY
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
    fun getInstancesFromRrule_Todo_DAILY() {

        val item = ICalObject.createTodo().apply {
            this.dtstart = 1622541600000L
            this.due = 1622541600000L
            this.rrule = "FREQ=DAILY;COUNT=2;INTERVAL=4"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(2,recurList.size)
        assertEquals(1622541600000L, recurList[0])
        assertEquals(1622887200000L, recurList[1])
    }


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
    fun statusJournal_getListFromStringList() {

        val statusList = StatusJournal.getListFromStringList(listOf("DRAFT", "FINAL", "CANCELLED").toSet())
        assertTrue(statusList.contains(StatusJournal.CANCELLED))
        assertTrue(statusList.contains(StatusJournal.DRAFT))
        assertTrue(statusList.contains(StatusJournal.FINAL))
    }

    @Test
    fun statusTodo_getListFromStringList() {

        val statusList = StatusTodo.getListFromStringList(listOf("CANCELLED", "IN-PROCESS", "COMPLETED", "NEEDS-ACTION").toSet())
        assertTrue(statusList.contains(StatusTodo.CANCELLED))
        assertTrue(statusList.contains(StatusTodo.`IN-PROCESS`))
        assertTrue(statusList.contains(StatusTodo.COMPLETED))
        assertTrue(statusList.contains(StatusTodo.`NEEDS-ACTION`))
    }

    @Test
    fun classification_getStringSetFromList() {
        val classifications = listOf(Classification.PUBLIC, Classification.PRIVATE, Classification.CONFIDENTIAL)
        assertEquals(listOf("PUBLIC", "PRIVATE", "CONFIDENTIAL").toSet(), Classification.getStringSetFromList(classifications))
    }

    @Test
    fun statusJournal_getStringSetFromList() {
        val statusJournals = listOf(StatusJournal.FINAL, StatusJournal.DRAFT, StatusJournal.CANCELLED)
        assertEquals(listOf("DRAFT", "FINAL", "CANCELLED").toSet(), StatusJournal.getStringSetFromList(statusJournals))
    }

    @Test
    fun statusTodo_getStringSetFromList() {
        val statusTodos = listOf(StatusTodo.`NEEDS-ACTION`, StatusTodo.COMPLETED, StatusTodo.`IN-PROCESS`, StatusTodo.CANCELLED)
        assertEquals(listOf("CANCELLED", "IN-PROCESS", "COMPLETED", "NEEDS-ACTION").toSet(), StatusTodo.getStringSetFromList(statusTodos))
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
    fun retrieveCount_count_present() {
        val todo = ICalObject.createTodo()
        todo.rrule = "FREQ=DAILY;COUNT=4;INTERVAL=5"
        assertEquals(4, todo.retrieveCount())
    }

    @Test
    fun retrieveCount_only_FREQ_and_interval() {
        val todo = ICalObject.createTodo()
        todo.rrule = "FREQ=DAILY;INTERVAL=5"
        assertEquals(ICalObject.DEFAULT_MAX_RECUR_INSTANCES, todo.retrieveCount())
    }

    @Test
    fun retrieveCount_until_and_interval_daily() {
        val todo = ICalObject.createTodo()
        todo.dtstart = 1652117734839L
        todo.rrule = "FREQ=DAILY;UNTIL=20220621T173534Z;INTERVAL=2"
        assertEquals(22, todo.retrieveCount())
    }

    @Test
    fun retrieveCount_until_and_interval_monthly() {
        val todo = ICalObject.createTodo()
        todo.dtstart = 1652117734839L
        todo.rrule = "FREQ=MONTHLY;UNTIL=20220621T070000Z;INTERVAL=2"
        assertEquals(1, todo.retrieveCount())
    }

    @Test
    fun retrieveCount_until_and_interval_monthly2() {
        val todo = ICalObject.createTodo()
        todo.dtstart = 1652117734839L
        todo.rrule = "FREQ=MONTHLY;UNTIL=20220821T070000Z;INTERVAL=1"
        assertEquals(4, todo.retrieveCount())
    }

    @Test
    fun parseDate_test1() {
        val todo = ICalObject.createTodo()
        todo.parseDate("Here is a text with !20.04.2022 a date.")
        assertEquals(null, todo.dtstart)
        assertNotNull(todo.due)
        assertEquals(TZ_ALLDAY, todo.dueTimezone)
    }

    @Test
    fun parseDate_test2() {
        val todo = ICalObject.createTodo()
        todo.parseDate("What about !20220101")
        assertEquals(null, todo.dtstart)
        assertNotNull(todo.due)
        assertEquals(TZ_ALLDAY, todo.dueTimezone)
    }

    @Test
    fun parseDate_test3() {
        val todo = ICalObject.createTodo()
        todo.parseDate("What about !02/20/2022 11:23")
        assertEquals(null, todo.dtstart)
        assertNotNull(todo.due)
        assertNull(todo.dueTimezone)
    }

    @Test
    fun parseDate_journal_test1() {
        val journal = ICalObject.createJournal()
        journal.parseDate("Here is a text with !20.04.2022 a date.")
        assertEquals(null, journal.due)
        assertNotNull(journal.dtstart)
        assertEquals(TZ_ALLDAY, journal.dtstartTimezone)
    }

    @Test
    fun parseDate_journal_test2() {
        val journal = ICalObject.createJournal()
        journal.parseDate("What about !20220101")
        assertEquals(null, journal.due)
        assertNotNull(journal.dtstart)
        assertEquals(TZ_ALLDAY, journal.dtstartTimezone)
    }

    @Test
    fun parseDate_journal_test3() {
        val journal = ICalObject.createJournal()
        journal.parseDate("What about !02/20/2022 11:23")
        assertEquals(null, journal.due)
        assertNotNull(journal.dtstart)
        assertNull(journal.dtstartTimezone)
    }

    @Test
    fun parseDate_note_test() {
        val note = ICalObject.createNote()
        note.parseDate("What about !02/20/2022 11:23")
        assertNull(note.due)
        assertNull(note.dtstart)
        assertNull(note.dtstartTimezone)
    }

}