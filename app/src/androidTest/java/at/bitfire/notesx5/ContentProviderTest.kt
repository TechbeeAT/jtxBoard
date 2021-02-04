package at.bitfire.notesx5

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import at.bitfire.notesx5.database.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@SmallTest
class ContentProviderTest {

    private var mContentResolver: ContentResolver? = null
    private val URI_ICALOBJECT = Uri.parse("content://$AUTHORITY/$TABLE_NAME_ICALOBJECT")

    private lateinit var database: ICalDatabaseDao



    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        ICalDatabase.switchToInMemory(context)
        mContentResolver = context.contentResolver

        database = ICalDatabase.getInstance(context).iCalDatabaseDao
    }

    @Test
    fun icalObject_initiallyEmpty() {
        val cursor: Cursor? = mContentResolver?.query(URI_ICALOBJECT, arrayOf<String>(COLUMN_ID), null, null, null)
        assertThat(cursor, notNullValue())
        assertThat(cursor?.count, `is`(0))
        cursor?.close()
    }

    @Test
    fun icalObject_oneEntry() {
        GlobalScope.launch {
            val newEntry = database.insertJournal(ICalObject(component = "NOTE", summary = "noteSummary", description = "noteDesc"))
        }

        val cursor: Cursor? = mContentResolver?.query(URI_ICALOBJECT, arrayOf<String>(COLUMN_ID), null, null, null)
        assertThat(cursor, notNullValue())
        assertThat(cursor?.count, `is`(1))
        cursor?.close()
    }





    @Test
    fun delete() {
    }

    @Test
    fun getType() {
    }

    @Test
    fun insert() {
    }

    @Test
    fun onCreate() {
    }

    @Test
    fun query() {

    }

    @Test
    fun update() {
    }
}