package at.techbee.jtx.ui

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class IcalViewViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var icalViewViewModel: IcalViewViewModel



    @Before
    fun setup()  {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = ICalDatabase.getInMemoryDB(context).iCalDatabaseDao
        database.insertCollectionSync(ICalCollection(collectionId = 1L, displayName = "testcollection automated tests"))
        application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    }

    @After
    fun closeDb() {
        ICalDatabase.getInMemoryDB(context).close()
    }

    @Test
    fun testEditingClicked() = runBlockingTest {

        val preparedEntry = ICalObject.createJournal()
        preparedEntry.id = database.insertICalObject(preparedEntry)

        icalViewViewModel = IcalViewViewModel(preparedEntry.id, database, application)
        Thread.sleep(100)
        icalViewViewModel.icalEntity.getOrAwaitValue()

        icalViewViewModel.editingClicked()

        assertTrue(icalViewViewModel.editingClicked.value!!)
    }

    @Test
    fun testInsertRelatedNote() = runBlockingTest {

        val preparedEntry = ICalObject.createJournal()
        preparedEntry.id = database.insertICalObject(preparedEntry)

        icalViewViewModel = IcalViewViewModel(preparedEntry.id, database, application)
        Thread.sleep(100)
        icalViewViewModel.icalEntity.getOrAwaitValue()
        icalViewViewModel.icalEntity.observeForever {}

        icalViewViewModel.insertRelated("RelatedNote", null)
        Thread.sleep(100)

        val childEntry = database.get(icalViewViewModel.icalEntity.value?.relatedto?.get(0)?.linkedICalObjectId!!)
        childEntry.getOrAwaitValue()

        assertEquals(true, icalViewViewModel.icalEntity.value?.property?.dirty)
        assertEquals(1L,  icalViewViewModel.icalEntity.value?.property?.sequence)
        assertTrue(icalViewViewModel.icalEntity.value?.relatedto?.size!! > 0)
        assertNotNull(childEntry)
    }

    @Test
    fun testInsertRelatedAudioNote() = runBlockingTest {

        val preparedEntry = ICalObject.createJournal()
        preparedEntry.id = database.insertICalObject(preparedEntry)

        icalViewViewModel = IcalViewViewModel(preparedEntry.id, database, application)
        Thread.sleep(100)
        icalViewViewModel.icalEntity.getOrAwaitValue()
        icalViewViewModel.icalEntity.observeForever {}

        icalViewViewModel.insertRelated(null, Attachment(uri = "https://10.0.0.138"))
        Thread.sleep(100)

        val childEntry = database.get(icalViewViewModel.icalEntity.value?.relatedto?.get(0)?.linkedICalObjectId!!)
        childEntry.getOrAwaitValue()

        assertEquals(true, icalViewViewModel.icalEntity.value?.property?.dirty)
        assertEquals(1L,  icalViewViewModel.icalEntity.value?.property?.sequence)
        assertTrue(icalViewViewModel.icalEntity.value?.relatedto?.size!! > 0)
        assertNotNull(childEntry)
        assertTrue(childEntry.value?.attachments?.size!! > 0)

    }

    @Test
    fun testUpdateProgress() = runBlockingTest {

        val preparedEntry = ICalObject.createTask("myTestTask")
        preparedEntry.id = database.insertICalObject(preparedEntry)


        icalViewViewModel = IcalViewViewModel(preparedEntry.id, database, application)
        Thread.sleep(100)
        icalViewViewModel.icalEntity.getOrAwaitValue()

        icalViewViewModel.updateProgress(icalViewViewModel.icalEntity.value!!.property, 88)

        val retrievedObject = database.getICalObjectById(icalViewViewModel.icalEntity.value!!.property.id)

        assertNotNull(icalViewViewModel.icalEntity.value!!.property.dtstart)
        assertEquals(88, icalViewViewModel.icalEntity.value!!.property.percent)
    }

    @Test
    fun testVisibilitySettingsForTask() = runBlockingTest {

        val preparedEntry = ICalObject.createTask("myTestTask")
        preparedEntry.id = database.insertICalObject(preparedEntry)


        icalViewViewModel = IcalViewViewModel(preparedEntry.id, database, application)
        Thread.sleep(100)
        icalViewViewModel.icalEntity.getOrAwaitValue()

        assertNotNull(icalViewViewModel.icalEntity)
        assertEquals(icalViewViewModel.categories.getOrAwaitValue(), listOf<Category>())

        assertNull(icalViewViewModel.dtstartFormatted.getOrAwaitValue())
        assertNotNull(icalViewViewModel.createdFormatted.getOrAwaitValue())
        assertNotNull(icalViewViewModel.lastModifiedFormatted.getOrAwaitValue())
        //assertNull(icalViewViewModel.completedFormatted.getOrAwaitValue())
        //assertNull(icalViewViewModel.startedFormatted.getOrAwaitValue())

        assertEquals(false, icalViewViewModel.dateVisible.getOrAwaitValue())
        assertEquals(false, icalViewViewModel.timeVisible.getOrAwaitValue())
        assertEquals(false, icalViewViewModel.urlVisible.getOrAwaitValue())
        assertEquals(false, icalViewViewModel.attendeesVisible.getOrAwaitValue())
        assertEquals(false, icalViewViewModel.organizerVisible.getOrAwaitValue())
        assertEquals(false, icalViewViewModel.contactVisible.getOrAwaitValue())
        assertEquals(false, icalViewViewModel.commentsVisible.getOrAwaitValue())
        assertEquals(false, icalViewViewModel.attachmentsVisible.getOrAwaitValue())
        assertEquals(false, icalViewViewModel.relatedtoVisible.getOrAwaitValue())
        assertEquals(true, icalViewViewModel.progressVisible.getOrAwaitValue())
        assertEquals(true, icalViewViewModel.priorityVisible.getOrAwaitValue())
        assertEquals(false, icalViewViewModel.subtasksVisible.getOrAwaitValue())
        assertEquals(false, icalViewViewModel.completedVisible.getOrAwaitValue())
        assertEquals(false, icalViewViewModel.startedVisible.getOrAwaitValue())
        assertEquals(false, icalViewViewModel.resourcesVisible.getOrAwaitValue())


        assertEquals(listOf<ICalObject>(), icalViewViewModel.subtasksCountList.getOrAwaitValue())

    }
}