package at.techbee.jtx.ui

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Organizer
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.properties.Reltype
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class IcalListViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ICalDatabaseDao
    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var icalListViewModel: IcalListViewModel

    @Before
    fun setup()   {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = ICalDatabase.getInMemoryDB(context).iCalDatabaseDao
        database.insertCollectionSync(ICalCollection(collectionId = 1L, displayName = "testcollection automated tests"))
        application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        icalListViewModel = IcalListViewModel(database, application)

    }

    @After
    fun closeDb() {
        ICalDatabase.getInMemoryDB(context).close()
    }


    @Test
    fun setFocusItem() {
        // covered by getFocusItemPosition()
    }

    @Test
    fun getFocusItemPosition() = runBlockingTest {

        icalListViewModel.iCal4List.observeForever {  }

        val id1 = database.insertICalObject(ICalObject.createTask("Test1"))
        database.insertICalObject(ICalObject.createTask("Test2"))
        database.insertICalObject(ICalObject.createTask("Test3"))

        // default search module is for Journals, we change it and update the search
        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.updateSearch()

        icalListViewModel.focusItemId.value = id1

        //sorting is descending, the first inserted item will be at the first position (=0)
        assertEquals(0, icalListViewModel.getFocusItemPosition())

    }

    @Test
    fun resetFocusItem() {
        icalListViewModel.resetFocusItem()
        assertEquals(0L, icalListViewModel.focusItemId.value)
        assertEquals(-1, icalListViewModel.getFocusItemPosition())
    }

    @Test
    fun updateSearch() {
        // Basic Test
        val searchBefore = icalListViewModel.listQuery.value
        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchCategories.add("Test")
        icalListViewModel.updateSearch()

        assertNotEquals(searchBefore, icalListViewModel.listQuery.value)
    }

    @Test
    fun updateSearch_filter_Module() = runBlockingTest {

        icalListViewModel.iCal4List.observeForever {  }

        //inserting 3 Tasks, 2 Journals and 1 Note
        database.insertICalObject(ICalObject.createTask("Task1"))
        database.insertICalObject(ICalObject.createTask("Task2"))
        database.insertICalObject(ICalObject.createTask("Task3"))
        database.insertICalObject(ICalObject.createJournal())
        database.insertICalObject(ICalObject.createJournal())
        database.insertICalObject(ICalObject.createNote("Note1"))

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.searchModule = Module.JOURNAL.name
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.searchModule = Module.NOTE.name
        icalListViewModel.updateSearch()
        assertEquals(1, icalListViewModel.iCal4List.value?.size)
    }

    @Test
    fun updateSearch_filter_Text() = runBlockingTest {

        icalListViewModel.iCal4List.observeForever {  }

        //inserting 3 Tasks, 2 Journals and 1 Note
        database.insertICalObject(ICalObject.createTask("Task1_abc_Text"))
        database.insertICalObject(ICalObject.createTask("Task2_asdf_Text"))
        database.insertICalObject(ICalObject.createTask("Task3_abc"))

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchText = "%abc%"
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4List.value?.size)
    }

    @Test
    fun updateSearch_filter_Categories() = runBlockingTest {

        icalListViewModel.iCal4List.observeForever {  }

        //inserting 3 Tasks, 2 Journals and 1 Note
        val id1 = database.insertICalObject(ICalObject.createTask("Task1"))
        database.insertCategory(Category(icalObjectId = id1, text = "Test1"))
        val id2 = database.insertICalObject(ICalObject.createTask("Task2"))
        database.insertCategory(Category(icalObjectId = id2, text = "Test1"))
        database.insertCategory(Category(icalObjectId = id2, text = "Whatever"))
        database.insertCategory(Category(icalObjectId = id2, text = "No matter"))
        val id3 = database.insertICalObject(ICalObject.createTask("Task3"))
        database.insertCategory(Category(icalObjectId = id3, text = "Whatever"))
        database.insertCategory(Category(icalObjectId = id3, text = "No matter"))
        database.insertICalObject(ICalObject.createTask("Task4"))  // val id4 = ...
        // no categories for id4

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchCategories.add("Test1")
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.searchCategories.add("Whatever")
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4List.value?.size)
    }



    @Test
    fun updateSearch_filter_Collections() = runBlockingTest {

        icalListViewModel.iCal4List.observeForever {  }

        val col1 = database.insertCollectionSync(ICalCollection(displayName = "ABC"))
        val col2 = database.insertCollectionSync(ICalCollection(displayName = "XYZ"))

        database.insertICalObject(ICalObject(collectionId = col1))
        database.insertICalObject(ICalObject(collectionId = col1))
        database.insertICalObject(ICalObject(collectionId = col2))

        icalListViewModel.searchModule = Module.NOTE.name
        icalListViewModel.searchCollection.add("ABC")
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.searchModule = Module.NOTE.name
        icalListViewModel.searchCollection.add("XYZ")
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.searchModule = Module.NOTE.name
        icalListViewModel.searchCollection.clear()
        icalListViewModel.searchCollection.add("XYZ")
        icalListViewModel.updateSearch()
        assertEquals(1, icalListViewModel.iCal4List.value?.size)
    }

    @Test
    fun updateSearch_filter_Organizer() = runBlockingTest {

        icalListViewModel.iCal4List.observeForever {  }

        val id1 = database.insertICalObject(ICalObject.createTask("Task1"))
        database.insertOrganizer(Organizer(icalObjectId = id1, caladdress = "Organizer1@techbee.at"))
        val id2 = database.insertICalObject(ICalObject.createTask("Task2"))
        database.insertOrganizer(Organizer(icalObjectId = id2, caladdress = "Organizer1@techbee.at"))
        database.insertOrganizer(Organizer(icalObjectId = id2, caladdress = "rezinagrO@techbee.at"))
        val id3 = database.insertICalObject(ICalObject.createTask("Task3"))
        database.insertOrganizer(Organizer(icalObjectId = id3, caladdress = "rezinagrO@techbee.at"))
        database.insertICalObject(ICalObject.createTask("Task4"))  // val id4 =
        // no organizer for id4

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchOrganizer.add("Organizer1@techbee.at")
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.searchOrganizer.add("rezinagrO@techbee.at")
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4List.value?.size)
    }

    @Test
    fun updateSearch_filter_StatusJournal() = runBlockingTest {

        icalListViewModel.iCal4List.observeForever {  }

        database.insertICalObject(ICalObject(summary="Note1", module = Module.NOTE.name, component = Component.VJOURNAL.name, status = StatusJournal.CANCELLED.name))
        database.insertICalObject(ICalObject(summary="Note2", module = Module.NOTE.name, component = Component.VJOURNAL.name, status = StatusJournal.DRAFT.name))
        database.insertICalObject(ICalObject(summary="Note3", module = Module.NOTE.name, component = Component.VJOURNAL.name, status = StatusJournal.FINAL.name))
        database.insertICalObject(ICalObject(summary="Note4", module = Module.NOTE.name, component = Component.VJOURNAL.name, status = StatusJournal.CANCELLED.name))

        icalListViewModel.searchModule = Module.NOTE.name
        icalListViewModel.searchStatusJournal.add(StatusJournal.DRAFT)
        icalListViewModel.updateSearch()
        assertEquals(1, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.searchStatusJournal.add(StatusJournal.CANCELLED)
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.searchStatusJournal.add(StatusJournal.FINAL)
        icalListViewModel.updateSearch()
        assertEquals(4, icalListViewModel.iCal4List.value?.size)
    }

    @Test
    fun updateSearch_filter_StatusTodo() = runBlockingTest {

        icalListViewModel.iCal4List.observeForever {  }

        database.insertICalObject(ICalObject(summary="Task1", module = Module.TODO.name, component = Component.VTODO.name, status = StatusTodo.CANCELLED.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name,  status = StatusTodo.`NEEDS-ACTION`.name))
        database.insertICalObject(ICalObject(summary="Task2", module = Module.TODO.name, component = Component.VTODO.name,  status = StatusTodo.`IN-PROCESS`.name))
        database.insertICalObject(ICalObject(summary="Task3", module = Module.TODO.name, component = Component.VTODO.name,  status = StatusTodo.`IN-PROCESS`.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name,  status = StatusTodo.COMPLETED.name))

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchStatusTodo.add(StatusTodo.`NEEDS-ACTION`)
        icalListViewModel.updateSearch()
        assertEquals(1, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.searchStatusTodo.add(StatusTodo.`IN-PROCESS`)
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.searchStatusTodo.add(StatusTodo.COMPLETED)
        icalListViewModel.updateSearch()
        assertEquals(4, icalListViewModel.iCal4List.value?.size)
    }

    @Test
    fun updateSearch_filter_Classification() = runBlockingTest {

        icalListViewModel.iCal4List.observeForever {  }

        database.insertICalObject(ICalObject(summary="Task1", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PUBLIC.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name,  classification = Classification.PUBLIC.name))
        database.insertICalObject(ICalObject(summary="Task2", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PRIVATE.name))
        database.insertICalObject(ICalObject(summary="Task3", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PRIVATE.name))
        database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.CONFIDENTIAL.name))

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchClassification.add(Classification.PUBLIC)
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.searchClassification.add(Classification.PRIVATE)
        icalListViewModel.updateSearch()
        assertEquals(4, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.searchClassification.add(Classification.CONFIDENTIAL)
        icalListViewModel.updateSearch()
        assertEquals(5, icalListViewModel.iCal4List.value?.size)
    }




    @Test
    fun clearFilter() {
        icalListViewModel.clearFilter()
        assertEquals(0, icalListViewModel.searchCategories.size)
        assertEquals(0, icalListViewModel.searchOrganizer.size)
        assertEquals(0, icalListViewModel.searchStatusJournal.size)
        assertEquals(0, icalListViewModel.searchStatusTodo.size)
        assertEquals(0, icalListViewModel.searchClassification.size)
        assertEquals(0, icalListViewModel.searchCollection.size)
    }

    @Test
    fun updateProgress() = runBlockingTest {

        val id = database.insertICalObject(ICalObject.createTask("Test"))
        icalListViewModel.updateProgress(id, 50, false)
        Thread.sleep(100)
        val icalobject = database.getICalObjectById(id)

        assertEquals(50, icalobject?.percent)
    }

    @Test
    fun updateProgress_withUnlink() = runBlockingTest {

        val item = ICalObject.createTask("Test")
        item.isRecurLinkedInstance = true

        val id = database.insertICalObject(item)
        icalListViewModel.updateProgress(id, 50, false)
        Thread.sleep(100)
        val icalobject = database.getICalObjectById(id)

        assertEquals(50, icalobject?.percent)
        assertEquals(false, icalobject?.isRecurLinkedInstance)
    }


    @Test
    fun checkAllRelatedto() = runBlockingTest {

        icalListViewModel.allSubtasks.observeForever {  }

        val parent = database.insertICalObject(ICalObject.createTodo())
        val child1 = database.insertICalObject(ICalObject.createTodo())
        val child2 = database.insertICalObject(ICalObject.createTodo())

        database.insertRelatedto(Relatedto(icalObjectId = parent, linkedICalObjectId = child1, reltype = Reltype.CHILD.name))
        database.insertRelatedto(Relatedto(icalObjectId = parent, linkedICalObjectId = child2, reltype = Reltype.CHILD.name))

        assertEquals(2, icalListViewModel.allSubtasks.value?.size)
    }

    @Test
    fun deleteVisible() = runBlockingTest {

        icalListViewModel.iCal4List.observeForever {  }

        val id1 = database.insertICalObject(ICalObject(summary="Task1", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PUBLIC.name))
        val id2 = database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name,  classification = Classification.PUBLIC.name))
        val id3 = database.insertICalObject(ICalObject(summary="Task2", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PRIVATE.name))
        val id4 = database.insertICalObject(ICalObject(summary="Task3", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.PRIVATE.name))
        val id5 = database.insertICalObject(ICalObject(summary="Task4", module = Module.TODO.name, component = Component.VTODO.name, classification = Classification.CONFIDENTIAL.name))

        icalListViewModel.searchModule = Module.TODO.name
        icalListViewModel.searchClassification.add(Classification.PUBLIC)
        icalListViewModel.updateSearch()
        assertEquals(2, icalListViewModel.iCal4List.value?.size)

        icalListViewModel.delete(listOf(id1, id2))
        Thread.sleep(100)

        icalListViewModel.searchClassification.clear()
        icalListViewModel.updateSearch()
        assertEquals(3, icalListViewModel.iCal4List.value?.size)
    }
}