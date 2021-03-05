package at.bitfire.notesx5.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.bitfire.notesx5.database.properties.*
import at.bitfire.notesx5.database.relations.ICalEntity
import at.bitfire.notesx5.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class ICalDatabaseDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    private lateinit var database: ICalDatabaseDao
    private lateinit var db: ICalDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, ICalDatabase::class.java)
                // Allowing main thread queries, just for testing.
                .allowMainThreadQueries()
                .build()
        database = db.iCalDatabaseDao

        database.insertCollectionSync(ICalCollection(collectionId = 1L, displayName = "testcollection automated tests"))
    }



    @Test
    fun insertAndCount() = runBlockingTest {
        assertEquals(database.getCount(), 0)
        database.insertICalObject(ICalObject.createJournal())
        assertEquals(database.getCount(), 1)

        //assertEquals(vJournalItem, retrievedItem)
    }

    @Test
    fun insert_and_retrieve_ICalObject() = runBlockingTest {
        val preparedEntry = ICalObject.createNote("myTestJournal")
        preparedEntry.id = database.insertICalObject(preparedEntry)

        val retrievedEntry = database.get(preparedEntry.id).getOrAwaitValue()
        assertEquals(retrievedEntry?.property, preparedEntry)
    }


    @Test
    fun insert_and_retrieve_ICalEntityObject() = runBlockingTest {
        val preparedICalObject = ICalObject(dtstart = System.currentTimeMillis(), summary="myTestJournal")
        preparedICalObject.id = database.insertICalObject(preparedICalObject)

        val preparedAttendee = Attendee(icalObjectId = preparedICalObject.id, caladdress = "mailto:test@test.net")
        val preparedCategory = Category(icalObjectId = preparedICalObject.id, text = "category")
        val preparedComment = Comment(icalObjectId = preparedICalObject.id, text = "comment")
        //val preparedContact = Contact(icalObjectId = preparedEntry.id, text = "contact")
        val preparedOrganizer = Organizer(icalObjectId = preparedICalObject.id, caladdress = "mailto:test@test.net")
        //val preparedResource = Resource(icalObjectId = preparedEntry.id, text = "resource")

        val preparedSubNote = ICalObject.createNote("Subnote")
        preparedSubNote.id = database.insertICalObject(preparedSubNote)
        val preparedRelatedto = Relatedto(icalObjectId = preparedICalObject.id, linkedICalObjectId = preparedSubNote.id, reltype = "Child")

        preparedAttendee.attendeeId = database.insertAttendee(preparedAttendee)
        preparedCategory.categoryId = database.insertCategory(preparedCategory)
        preparedComment.commentId = database.insertComment(preparedComment)
        //database.insertContact(preparedContact)
        preparedOrganizer.organizerId = database.insertOrganizer(preparedOrganizer)
        //database.insertResource(preparedResource)
        preparedRelatedto.relatedtoId = database.insertRelatedto(preparedRelatedto)

        val preparedIcalEntity = ICalEntity(preparedICalObject, listOf(preparedComment), listOf(preparedCategory), listOf(preparedAttendee), preparedOrganizer, listOf(preparedRelatedto))
        preparedIcalEntity.ICalCollection = database.getCollectionByIdSync(1L)

        val retrievedEntry = database.get(preparedICalObject.id).getOrAwaitValue()
        assertEquals(retrievedEntry, preparedIcalEntity)
    }


    @After
    fun closeDb() {
        db.close()
    }


}