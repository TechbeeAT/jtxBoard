package at.bitfire.notesx5

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import at.bitfire.notesx5.database.VJournalDatabase
import at.bitfire.notesx5.database.VJournalDatabaseDao
import at.bitfire.notesx5.database.vJournalItem
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class VJournalDatabaseTest {


    private lateinit var VJournalDao: VJournalDatabaseDao
    private lateinit var db: VJournalDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, VJournalDatabase::class.java)
                // Allowing main thread queries, just for testing.
                .allowMainThreadQueries()
                .build()
        VJournalDao = db.vJournalDatabaseDao
    }

    @Test
    @Throws(Exception::class)
    suspend fun insert() {
        var vJournalItem = vJournalItem()
        vJournalItem.description = "asdf"
        //vJournalItem.comment = "asfd"
        vJournalItem.dtstamp = System.currentTimeMillis()
        vJournalItem.dtstamp = System.currentTimeMillis()
        vJournalItem.uid = "uid"
        vJournalItem.organizer = "organizer"
        //vJournalItem.categories = listOf("cat1", "cat2")
        vJournalItem.id = VJournalDao.insert(vJournalItem)
        val retrievedItem = VJournalDao.get(vJournalItem.id)
        assertEquals(vJournalItem, retrievedItem)

    }



    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }




}

