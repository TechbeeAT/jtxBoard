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
import at.bitfire.notesx5.database.properties.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@SmallTest
class ContentProviderTest {

    private var mContentResolver: ContentResolver? = null


    private val URI_ICALOBJECT = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_ICALOBJECT")
    private val URI_ATTENDEES = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_ATTENDEE")
    private val URI_CATEGORIES = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_CATEGORY")
    private val URI_COMMENTS = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_COMMENT")
    private val URI_CONTACTS = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_CONTACT")
    private val URI_ORGANIZER = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_ORGANIZER")
    private val URI_RELATEDTO = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_RELATEDTO")
    private val URI_RESOURCE = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_RESOURCE")


    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        ICalDatabase.switchToInMemory(context)
        mContentResolver = context.contentResolver
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
        assertNotNull(cursor)
        assertEquals(cursor?.count, 1)
//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")

        val countDel: Int? = mContentResolver?.delete(newUri!!, null, null)
        assertNotNull(countDel)
        assertEquals(countDel,1)
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
        assertNotNull(cursor)
        assertEquals(cursor?.count, 1)             // inserted object was found


//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")

        // UPDATE the new value
        val updatedContentValues = ContentValues()
        updatedContentValues.put(COLUMN_DESCRIPTION, "description was updated")
        val countUpdated = mContentResolver?.update(newUri!!, updatedContentValues, null, null)
        assertEquals(countUpdated, 1)
        Log.println(Log.INFO, "icalObject_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        assertEquals(cursor2?.count,1)             // inserted object was found
        cursor2?.close()

        // DELETE the updated value
        val countDeleted = mContentResolver?.delete(newUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursor3: Cursor? = mContentResolver?.query(newUri!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        assertEquals(cursor3?.count,0)             // inserted object was found
        cursor3?.close()

    }



    @Test
    fun attendee_insert_find_update_delete()  {

        // INSERT a new ICalObject
        val contentValues = ContentValues()
        contentValues.put(COLUMN_SUMMARY, "journal4attendee")
        contentValues.put(COLUMN_DTSTART, System.currentTimeMillis())
        contentValues.put(COLUMN_COMPONENT, "JOURNAL")
        val newUri = mContentResolver?.insert(URI_ICALOBJECT, contentValues)
        val newICalObjectId = newUri?.lastPathSegment?.toLongOrNull()
        Log.println(Log.INFO, "newICalObjectId", newICalObjectId.toString())
        assertNotNull(newICalObjectId)

        // QUERY the new value is skipped, instead we insert a new attendee
        // INSERT a new Attendee
        val attendeeValues = ContentValues()
        attendeeValues.put(COLUMN_ATTENDEE_ICALOBJECT_ID, newICalObjectId)
        attendeeValues.put(COLUMN_ATTENDEE_CALADDRESS, "mailto:test@test.com")
        val newAttendeeUri = mContentResolver?.insert(URI_ATTENDEES, attendeeValues)
        assertNotNull(newAttendeeUri)

        //QUERY the Attendee
        val cursorIcalobject: Cursor? = mContentResolver?.query(newAttendeeUri!!, arrayOf<String>(COLUMN_ATTENDEE_ID), null, null, null)
        assertEquals(cursorIcalobject?.count, 1)             // inserted object was found

        // UPDATE the new value
        val updatedAttendeeValues = ContentValues()
        updatedAttendeeValues.put(COLUMN_ATTENDEE_CALADDRESS, "mailto:test@test.net")
        val countUpdated = mContentResolver?.update(newAttendeeUri!!, updatedAttendeeValues, null, null)
        assertEquals(countUpdated, 1)
        //Log.println(Log.INFO, "attendee_insert_find_update", "Assert successful, found ${cursor?.count} entries, updated entries: $countUpdated")

        // QUERY the updated value
        val cursorAttendee: Cursor? = mContentResolver?.query(newAttendeeUri!!, arrayOf<String>(COLUMN_ATTENDEE_ID, COLUMN_ATTENDEE_CALADDRESS), "$COLUMN_ATTENDEE_CALADDRESS = ?", arrayOf("mailto:test@test.net"), null)
        assertEquals(cursorAttendee?.count,1)             // inserted object was found
        cursorAttendee?.close()

        // DELETE the ICalObject, through the foreign key also the attendee is deleted
        val countDeleted = mContentResolver?.delete(newUri!!, null, null)
        assertEquals(countDeleted, 1)

        // QUERY the delete value, make sure it's really deleted
        //val cursor2: Cursor? = mContentResolver?.query(newUri!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        val cursorDeletedIcalobject: Cursor? = mContentResolver?.query(newUri!!, arrayOf<String>(COLUMN_ID, COLUMN_SUMMARY, COLUMN_DESCRIPTION), "$COLUMN_SUMMARY = ?", arrayOf("note2update"), null)
        assertEquals(cursorDeletedIcalobject?.count,0)             // inserted object was found
        cursorDeletedIcalobject?.close()
        val cursorDeletedAttendee: Cursor? = mContentResolver?.query(newAttendeeUri!!, arrayOf<String>(COLUMN_ATTENDEE_ID, COLUMN_ATTENDEE_CALADDRESS), "$COLUMN_ATTENDEE_CALADDRESS = ?", arrayOf("mailto:test@test.net"), null)
        assertEquals(cursorDeletedAttendee?.count,0)             // inserted object was found
        cursorAttendee?.close()


    }





    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_invalid_url()  {
        val uriInvalid = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/invalid")
        mContentResolver?.query(uriInvalid, arrayOf<String>(COLUMN_ID), null, null, null)
    }

    @Test(expected = IllegalArgumentException::class)                    // needed to assert exceptions, see e.g. https://www.baeldung.com/junit-assert-exception
    fun query_valid_url_with_wrong_parameter()  {
        val uriWrong = Uri.parse("content://$SYNC_PROVIDER_AUTHORITY/$TABLE_NAME_ICALOBJECT/asdf")
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
        assertEquals(cursor?.count, 1)
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
        assertEquals(cursor?.count,1)
//        Log.println(Log.INFO, "icalObject_insert_find_delete", "Assert successful, DB has ${cursor?.count} entries, the new id is ${cursor?.getString(0)}")
        cursor?.close()

        val cursor2: Cursor? = mContentResolver?.query(URI_ICALOBJECT, arrayOf<String>(COLUMN_ID), null, null, null)
        assertTrue(cursor2?.count!! > 0)     // there must be entries! Delete must not be executed!
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