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
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
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

    /*
    @Test
    fun icalObject_initiallyEmpty() {
        val cursor: Cursor? = mContentResolver?.query(URI_ICALOBJECT, arrayOf<String>(COLUMN_ID), null, null, null)
        assertThat(cursor, notNullValue())
        assertThat(cursor?.count, `is`(0))
        Log.println(Log.INFO, "icalObject_initiallyEmpty", "Assert successful, DB is empty (Cursor count: ${cursor?.count})")
        cursor?.close()
    }

     */


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

    @Test
    fun icalObject_insert_find_update()  {

        // INSERT a new value
        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "note2update")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)

        // QUERY the new value
        val cursor: Cursor? = mContentResolver?.query(newUri!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY), null, null, null)
        assertThat(cursor, notNullValue())
        assertThat(cursor?.count, `is`(1))             // inserted object was found


//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")

        // UPDATE the new value
        val updatedContentValues = ContentValues()
        updatedContentValues.put(COLUMN_DESCRIPTION, "description was updated")
        val countUpdated = mContentResolver?.update(newUri!!, updatedContentValues, null, null)

        assertThat(countUpdated, notNullValue())
        assertThat(countUpdated, `is`(1))
        Log.println(Log.INFO, "icalObject_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        assertThat(cursor2, notNullValue())
        assertThat(cursor2?.count, `is`(1))             // inserted object was found
        cursor2?.close()
    }


    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_invalid_url()  {
        val uriInvalid = Uri.parse("content://$AUTHORITY/invalid")
        mContentResolver?.query(uriInvalid, arrayOf<String>(COLUMN_ID), null, null, null)
    }

    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_valid_url_with_wrong_parameter()  {
        val uriWrong = Uri.parse("content://$AUTHORITY/$TABLE_NAME_ICALOBJECT/asdf")
        mContentResolver?.query(uriWrong, arrayOf<String>(COLUMN_ID), null, null, null)
    }


    @Test
    fun check_for_SQL_injection_through_contentValues()  {

        // INSERT a new value, this one must remain
        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "note2update")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)


        val contentValuesCurrupted = ContentValues()
        contentValuesCurrupted.put(COLUMN_SUMMARY, "note2corrupted\"; delete * from $TABLE_NAME_ICALOBJECT")
        val newUri2 = mContentResolver?.insert(URI_ICALOBJECT, contentValuesCurrupted)


        val cursor: Cursor? = mContentResolver?.query(newUri2!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY), null, null, null)
        assertThat(cursor, notNullValue())
        assertThat(cursor?.count, `is`(1))
//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")

        cursor?.close()
    }


    @Test
    fun check_for_SQL_injection_through_query()  {

        // INSERT a new value, this one must remain
        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "note2check")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)

        val cursor: Cursor? = mContentResolver?.query(newUri!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY), "$COLUMN_SUMMARY = ?); DELETE * FROM $TABLE_NAME_ICALOBJECT", arrayOf("note2check"), null)
        assertThat(cursor, notNullValue())
        assertThat(cursor?.count, `is`(1))
//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")
        cursor?.close()

        val cursor2: Cursor? = mContentResolver?.query(URI_ICALOBJECT, arrayOf<String>(COLUMN_ID), null, null, null)
        assertThat(cursor2, notNullValue())
        assertThat(cursor2?.count, not(0))     // there must be entries! Delete must not be executed!
        Log.println(Log.INFO, "icalObject_initiallyEmpty", "Assert successful, DB is empty (Cursor count: ${cursor?.count})")
        cursor?.close()
    }



    /*
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

     */
}