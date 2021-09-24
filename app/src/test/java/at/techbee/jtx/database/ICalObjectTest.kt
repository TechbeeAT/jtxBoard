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
            percent = 0,
            priority = 0,
            dueTimezone = "ALLDAY",
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
    fun factory_createTodo_withSummary() {

        val factoryObject = ICalObject.createTask("Task Summary")
        val createdObject = ICalObject(
            component = Component.VTODO.name,
            module = Module.TODO.name,
            status = StatusTodo.`NEEDS-ACTION`.name,
            percent = 0,
            priority = 0,
            dueTimezone = "ALLDAY",
            dirty = true,
            summary = "Task Summary",
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
    fun getRecurId_date() {
        val sampleDate = 1632434400000L   // 2021-09-24
        val recurId = ICalObject.getRecurId(sampleDate, "ALLDAY")
        assertEquals("20210923", recurId)
    }

    @Test
    fun getRecurId_datetime() {

        val sampleDate = 1632474660000L   // 2021-09-24 11:11:00
        val recurId = ICalObject.getRecurId(sampleDate, null)
        assertEquals("20210924T111100", recurId)
    }

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

}