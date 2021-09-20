package at.techbee.jtx.ui

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.getOrAwaitValue
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class IcalEditViewModelTest {


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var icalEditViewModel: IcalEditViewModel

    private var collection1id: Long? = null
    private var collection2id: Long? = null

    private var sampleCategory1 = Category(text = "Techbee")
    private var sampleCategory2 = Category(text = "jtxBoard")
    private var sampleCategory3 = Category(text = "DAVx5")

    private var sampleAttachment1 = Attachment(uri = "content://at.techbee.jtx.fileprovider/jtx_files/1631560872968.aac")
    private var sampleAttachment2 = Attachment(uri = "content://at.techbee.jtx.fileprovider/jtx_files/1631560872969.aac")

    private var sampleAttendee1 = Attendee(caladdress = "info@techbee.at")
    private var sampleAttendee2 = Attendee(caladdress = "contact@techbee.at")
    private var sampleAttendee3 = Attendee(caladdress = "patrick@techbee.at")

    private var sampleComment1 = Comment(text = "Comment1")
    private var sampleComment2 = Comment(text = "Comment2")
    private var sampleComment3 = Comment(text = "Comment3")

    private var sampleResource1 = Resource(text = "Resource1")
    private var sampleResource2 = Resource(text = "Resource2")
    private var sampleResource3 = Resource(text = "Resource3")




    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = ICalDatabase.getInMemoryDB(context).iCalDatabaseDao
        collection1id = database.insertCollectionSync(ICalCollection(displayName = "testcollection1"))
        collection2id = database.insertCollectionSync(ICalCollection(displayName = "testcollection2"))
        application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        icalEditViewModel = IcalEditViewModel(ICalEntity(), database, application)

    }

    @After
    fun tearDown() = ICalDatabase.getInMemoryDB(context).close()


    @Test
    fun updateVisibility_TabGeneral() {

        // TODO: Make more tests like that when the tab assignments are final
        icalEditViewModel = IcalEditViewModel(ICalEntity(), database, application)
        icalEditViewModel.selectedTab = IcalEditViewModel.TAB_GENERAL
        icalEditViewModel.updateVisibility()

        assertTrue(icalEditViewModel.collectionVisible.value!!)
        assertTrue(icalEditViewModel.summaryVisible.value!!)
        assertTrue(icalEditViewModel.descriptionVisible.value!!)
        assertFalse(icalEditViewModel.dateVisible.value!!)
        assertFalse(icalEditViewModel.timeVisible.value!!)
        assertFalse(icalEditViewModel.alldayVisible.value!!)
        assertFalse(icalEditViewModel.timezoneVisible.value!!)
        assertTrue(icalEditViewModel.statusVisible.value!!)
        assertTrue(icalEditViewModel.classificationVisible.value!!)
        assertFalse(icalEditViewModel.urlVisible.value!!)
        assertFalse(icalEditViewModel.locationVisible.value!!)
        assertTrue(icalEditViewModel.categoriesVisible.value!!)
        assertFalse(icalEditViewModel.contactVisible.value!!)
        assertFalse(icalEditViewModel.attendeesVisible.value!!)
        assertFalse(icalEditViewModel.commentsVisible.value!!)

        assertFalse(icalEditViewModel.attachmentsVisible.value!!)
        assertFalse(icalEditViewModel.takePhotoVisible.value!!)
        assertFalse(icalEditViewModel.progressVisible.value!!)
        assertFalse(icalEditViewModel.priorityVisible.value!!)
        assertFalse(icalEditViewModel.subtasksVisible.value!!)
        assertFalse(icalEditViewModel.duedateVisible.value!!)
        assertFalse(icalEditViewModel.duetimeVisible.value!!)
        assertFalse(icalEditViewModel.completeddateVisible.value!!)
        assertFalse(icalEditViewModel.completedtimeVisible.value!!)
        assertFalse(icalEditViewModel.starteddateVisible.value!!)
        assertFalse(icalEditViewModel.startedtimeVisible.value!!)
    }

    @Test
    fun savingClicked_newEntry() = runBlockingTest {

        val updatedEntry = ICalEntity().apply {
            property.module = Module.JOURNAL.name
            property.component = Component.VJOURNAL.name
            property.summary = "New Entry"
            property.description = "New Entry Description"
            property.collectionId = collection1id!!
        }
        icalEditViewModel.iCalObjectUpdated.value = updatedEntry.property
        icalEditViewModel.categoryUpdated.add(sampleCategory1)
        icalEditViewModel.attachmentUpdated.add(sampleAttachment1)
        icalEditViewModel.attendeeUpdated.add(sampleAttendee1)
        icalEditViewModel.commentUpdated.add(sampleComment1)
        icalEditViewModel.resourceUpdated.add(sampleResource1)
        //icalEditViewModel.subtaskUpdated.add()


        icalEditViewModel.update()
        Thread.sleep(100)

        val retrievedEntry = database.get(icalEditViewModel.returnVJournalItemId.value!!).getOrAwaitValue(100)

        assertEquals(updatedEntry.property.module, retrievedEntry?.property?.module)
        assertEquals(updatedEntry.property.component, retrievedEntry?.property?.component)
        assertEquals(updatedEntry.property.summary, retrievedEntry?.property?.summary)
        assertEquals(updatedEntry.property.description, retrievedEntry?.property?.description)
        assertEquals(updatedEntry.property.collectionId, retrievedEntry?.property?.collectionId)

        assertEquals(sampleCategory1.text, retrievedEntry?.categories?.get(0)!!.text)
        assertEquals(sampleAttachment1.uri, retrievedEntry.attachments?.get(0)!!.uri)
        assertEquals(sampleAttendee1.caladdress, retrievedEntry.attendees?.get(0)!!.caladdress)
        assertEquals(sampleComment1.text, retrievedEntry.comments?.get(0)!!.text)
        assertEquals(sampleResource1.text, retrievedEntry.resources?.get(0)!!.text)

    }

    @Test
    fun savingClicked_updateEntry() {
    }

    @Test
    fun deleteClicked() {
    }

    @Test
    fun update() {
    }

    @Test
    fun delete() {
    }
}