package at.bitfire.notesx5

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import at.bitfire.notesx5.database.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ICalDatabaseTest {


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
    }



    @Test
    fun insertAndCount() = runBlocking {
        assertThat(database.getCount(), `is`(0))
        database.insertICalObject(ICalObject.createJournal())
        assertThat(database.getCount(), `is`(1))

        //assertEquals(vJournalItem, retrievedItem)
    }

    @Test
    fun insert_and_retrieve_ICalObject() = runBlocking {
        val newEntry = database.insertICalObject(ICalObject.createNote("myTestJournal"))
        val retrievedEntry = database.get(newEntry)
        assertThat(retrievedEntry?.value?.property?.summary, `is`("myTestJournal"))

    }


    @After
    fun closeDb() {
        db.close()
    }


}

