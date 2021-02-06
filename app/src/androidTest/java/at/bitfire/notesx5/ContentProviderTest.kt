package at.bitfire.notesx5

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import at.bitfire.notesx5.database.*
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
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
        Log.println(Log.INFO, "icalObject_initiallyEmpty", "Assert successful, DB is empty (Cursor count: ${cursor?.count})")
        cursor?.close()
    }

    @Test
    fun icalObject_oneEntry()  {
            //database.insertICalObject(ICalObject(component = "NOTE", summary = "noteSummary", description = "noteDesc"))

        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "noteSummary")

        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)
        Log.println(Log.INFO, "icalObject_oneEntry", "newUri: ${newUri.toString()}")
        val cursor: Cursor? = mContentResolver?.query(URI_ICALOBJECT, arrayOf<String>(COLUMN_ID), null, null, null)
        assertThat(cursor, notNullValue())
        assertThat(cursor?.count, `is`(1))
        //assertThat(cursor?.getString(4), `is`("noteSummary"))
        //Log.println(Log.INFO, "icalObject_oneEntry", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")
        cursor?.close()
    }

    @Test
    fun icalObject_insert_find_delete()  {

        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "note2delete")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)


        val cursor: Cursor? = mContentResolver?.query(newUri!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY), null, null, null)
        assertThat(cursor, notNullValue())
        assertThat(cursor?.count, `is`(1))
//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")

        val countDel: Int? = mContentResolver?.delete(newUri!!, null, null)
        assertThat(countDel, notNullValue())
        assertThat(countDel, `is`(1))
        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, deleted entries: $countDel")

        cursor?.close()
    }

    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_invalid_url()  {
        val uri_invalid = Uri.parse("content://$AUTHORITY/invalid")
        val cursor: Cursor? = mContentResolver?.query(uri_invalid, arrayOf<String>(COLUMN_ID), null, null, null)
    }

    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_valid_url_with_wrong_parameter()  {
        val uri_wrong = Uri.parse("content://$AUTHORITY/$TABLE_NAME_ICALOBJECT/asdf")
        val cursor: Cursor? = mContentResolver?.query(uri_wrong, arrayOf<String>(COLUMN_ID), null, null, null)
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