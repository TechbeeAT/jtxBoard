package at.techbee.jtx.database

import android.content.Context
import at.techbee.jtx.R
import net.fortuna.ical4j.util.MapTimeZoneCache
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner


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
        assertEquals(StatusTodo.`NEEDS-ACTION`.name, task.status)
        //assertNull(task.dtstart)
        //assertNull(task.completed)
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
        assertEquals(StatusTodo.`IN-PROCESS`.name, task.status)
        //assertNotNull(task.dtstart)
        //assertNull(task.completed)
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
        //assertNotNull(task.dtstart)
        //assertNotNull(task.completed)
        assertNotNull(task.lastModified)
        assertEquals(1, task.sequence)
        assertEquals(true, task.dirty)
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
            dtstartTimezone = ICalObject.TZ_ALLDAY,
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
            status = StatusTodo.`NEEDS-ACTION`.name,
            percent = null,
            priority = null,
            dueTimezone = ICalObject.TZ_ALLDAY,
            dirty = true,
            // dates and uid must be set explicitely to make the objects equal
            dtstart = factoryObject.dtstart,
            dtstartTimezone = ICalObject.TZ_ALLDAY,
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
            status = StatusTodo.`NEEDS-ACTION`.name,
            percent = null,
            priority = null,
            dueTimezone = ICalObject.TZ_ALLDAY,
            dirty = true,
            summary = "Task Summary",
            // dates and uid must be set explicitely to make the objects equal
            dtstart = factoryObject.dtstart,
            dtstartTimezone = ICalObject.TZ_ALLDAY,
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
        val recurId = ICalObject.getRecurId(sampleDate, ICalObject.TZ_ALLDAY)
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

    // TODO: Check this test further, it fails because of net.fortuna.ical4j.model.TimeZoneRegistryImpl getTimeZone

    @Test
    fun getRecurId_datetime_withTimezone() {

        // fix for crash when Timezones are needed for ical4j, see https://github.com/ical4j/ical4j/issues/195
        System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache::class.java.name)

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
            this.rrule = "FREQ=WEEKLY;COUNT=2;INTERVAL=2;BYDAY=FR,SA,SU"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(6,recurList.size)
        assertEquals(1622800800000L, recurList[0])
        assertEquals(1622887200000L, recurList[1])
        assertEquals(1622973600000L, recurList[2])
        assertEquals(1624010400000L, recurList[3])
        assertEquals(1624096800000L, recurList[4])
        assertEquals(1624183200000L, recurList[5])
    }


    @Test
    fun getInstancesFromRrule_Journal_WEEKLY_withExceptions() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622541600000L
            this.rrule = "FREQ=WEEKLY;COUNT=2;INTERVAL=2;BYDAY=FR,SA,SU"
            this.exdate = "1622973600000,1624096800000"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(4,recurList.size)
        assertEquals(1622800800000L, recurList[0])
        assertEquals(1622887200000L, recurList[1])
        assertEquals(1624010400000L, recurList[2])
        assertEquals(1624183200000L, recurList[3])
    }

    @Test
    fun getInstancesFromRrule_Journal_WEEKLY_withExceptions_andAdditions() {

        val item = ICalObject.createJournal().apply {
            this.dtstart = 1622541600000L
            this.rrule = "FREQ=WEEKLY;COUNT=2;INTERVAL=2;BYDAY=FR,SA,SU"
            this.exdate = "1622973600000,1624096800000"
            this.rdate = "1651410000000,1654088400000"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(6,recurList.size)
        assertEquals(1622800800000L, recurList[0])
        assertEquals(1622887200000L, recurList[1])
        assertEquals(1624010400000L, recurList[2])
        assertEquals(1624183200000L, recurList[3])
        assertEquals(1651410000000L, recurList[4])
        assertEquals(1654088400000L, recurList[5])
    }



    @Test
    fun getInstancesFromRrule_Todo_WEEKLY() {

        val item = ICalObject.createTodo().apply {
            this.dtstart = 1641045600000L
            this.due = 1641045605000L
            this.rrule = "FREQ=WEEKLY;COUNT=3;INTERVAL=1;BYDAY=MO"
        }

        val recurList = item.getInstancesFromRrule()
        assertEquals(3,recurList.size)
        assertEquals(1641218400000L, recurList[0])
        assertEquals(1641823200000L, recurList[1])
        assertEquals(1642428000000L, recurList[2])
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
            this.dtstartTimezone = ICalObject.TZ_ALLDAY
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
    fun getInstancesFromRrule_unsupported_TodoWithoutDue() {

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
    fun getInstancesFromRrule_unsupported_faultyRule() {

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
}