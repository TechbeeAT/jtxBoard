package at.bitfire.notesx5

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*

private const val CODE_ALL_ICALOBJECTS = 0
private const val CODE_SINGLE_ICALOBJECT = 1
private const val CODE_SINGLE_ATTENDEE = 2
private const val CODE_SINGLE_CATEGORY = 3
private const val CODE_SINGLE_COMMENT = 4
private const val CODE_SINGLE_CONTACT = 5
private const val CODE_SINGLE_ORGANIZER = 6
private const val CODE_SINGLE_RELATEDTO = 7
private const val CODE_SINGLE_RESOURCE = 8

private const val CODE_ALL_ATTENDEES_FOR_ICALOBJECT = 102
private const val CODE_ALL_CATEGORIES_FOR_ICALOBJECT = 103
private const val CODE_ALL_COMMENTS_FOR_ICALOBJECT = 104
private const val CODE_ALL_CONTACTS_FOR_ICALOBJECT = 105
private const val CODE_ALL_ORGANIZER_FOR_ICALOBJECT = 106
private const val CODE_ALL_RELATEDTO_FOR_ICALOBJECT = 107
private const val CODE_ALL_RESOURCE_FOR_ICALOBJECT = 108

private const val CODE_SINGLE_ATTENDEE_FOR_ICALOBJECT = 10202
private const val CODE_SINGLE_CATEGORY_FOR_ICALOBJECT = 10203
private const val CODE_SINGLE_COMMENT_FOR_ICALOBJECT = 10204
private const val CODE_SINGLE_CONTACT_FOR_ICALOBJECT = 10205
private const val CODE_SINGLE_ORGANIZER_FOR_ICALOBJECT = 10206
private const val CODE_SINGLE_RELATEDTO_FOR_ICALOBJECT = 10207
private const val CODE_SINGLE_RESOURCE_FOR_ICALOBJECT = 10208

const val AUTHORITY = "at.bitfire.notesx5.provider"

/** The URI for the Icalobject table.  */



class ContentProvider : ContentProvider() {


    private lateinit var database: ICalDatabaseDao


    private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        /*
         * The calls to addURI() go here, for all of the content URI patterns that the provider
         * should recognize. For this snippet, only the calls for table 3 are shown.
         */

        /*
         * Sets the integer value for multiple rows in table 3 to 1. Notice that no wildcard is used
         * in the path
         */
        addURI(AUTHORITY, "icalobject", CODE_ALL_ICALOBJECTS)

        /*
* Sets the code for a single row to 2. In this case, the "#" wildcard is
* used. "content://com.example.app.provider/table3/3" matches, but
* "content://com.example.app.provider/table3 doesn't.
*/
        addURI(AUTHORITY, "icalobject/#", CODE_SINGLE_ICALOBJECT)
        addURI(AUTHORITY, "attendee/#", CODE_SINGLE_ATTENDEE)
        addURI(AUTHORITY, "category/#", CODE_SINGLE_CATEGORY)
        addURI(AUTHORITY, "comment/#", CODE_SINGLE_COMMENT)
        addURI(AUTHORITY, "contact/#", CODE_SINGLE_CONTACT)
        addURI(AUTHORITY, "organizer/#", CODE_SINGLE_ORGANIZER)
        addURI(AUTHORITY, "relatedto/#", CODE_SINGLE_RELATEDTO)
        addURI(AUTHORITY, "resource/#", CODE_SINGLE_RESOURCE)

        addURI(AUTHORITY, "icalobject/#/attendee", CODE_ALL_ATTENDEES_FOR_ICALOBJECT)
        addURI(AUTHORITY, "icalobject/#/category", CODE_ALL_CATEGORIES_FOR_ICALOBJECT)
        addURI(AUTHORITY, "icalobject/#/comment", CODE_ALL_COMMENTS_FOR_ICALOBJECT)
        addURI(AUTHORITY, "icalobject/#/contact", CODE_ALL_CONTACTS_FOR_ICALOBJECT)
        addURI(AUTHORITY, "icalobject/#/organizer", CODE_ALL_ORGANIZER_FOR_ICALOBJECT)
        addURI(AUTHORITY, "icalobject/#/relatedto", CODE_ALL_RELATEDTO_FOR_ICALOBJECT)
        addURI(AUTHORITY, "icalobject/#/resource", CODE_ALL_RESOURCE_FOR_ICALOBJECT)

        addURI(AUTHORITY, "icalobject/#/attendee/#", CODE_SINGLE_ATTENDEE_FOR_ICALOBJECT)
        addURI(AUTHORITY, "icalobject/#/category/#", CODE_SINGLE_CATEGORY_FOR_ICALOBJECT)
        addURI(AUTHORITY, "icalobject/#/comment/#", CODE_SINGLE_COMMENT_FOR_ICALOBJECT)
        addURI(AUTHORITY, "icalobject/#/contact/#", CODE_SINGLE_CONTACT_FOR_ICALOBJECT)
        addURI(AUTHORITY, "icalobject/#/organizer/#", CODE_SINGLE_ORGANIZER_FOR_ICALOBJECT)
        addURI(AUTHORITY, "icalobject/#/relatedto/#", CODE_SINGLE_RELATEDTO_FOR_ICALOBJECT)
        addURI(AUTHORITY, "icalobject/#/resource/#", CODE_SINGLE_RESOURCE_FOR_ICALOBJECT)


    }



    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        TODO("Implement this to handle requests to delete one or more rows")
    }

    override fun getType(uri: Uri): String? {
        TODO("Implement this to handle requests for the MIME type of the data" +
                "at the given URI")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {

        TODO("Implement this to handle requests to insert a new row.")
    }

    override fun onCreate(): Boolean {

        //TODO is this okay at all?????
        val context = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            this.requireContext()
        } else {
            context!!.applicationContext
            TODO("VERSION.SDK_INT < R")
        }
        database = ICalDatabase.getInstance(context).iCalDatabaseDao

        return true
        TODO("Implement this to initialize your content provider on startup.")


    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {

        // TODO: validate uri and throw IllegalArgumentException if wrong

        val args = arrayListOf<String>()

        if (uri.pathSegments.size >= 2)
            args.add(uri.pathSegments[1].toLong().toString())      // add first argument (must be Long! String is expected, toLong would make other values null
        if (uri.pathSegments.size >= 4)
            args.add(uri.pathSegments[3].toLong().toString())      // add second argument (must be Long! String is expected, toLong would make other values null



        var queryString = "SELECT "
        if (projection.isNullOrEmpty())
            queryString += "*"
        else
            queryString += projection.joinToString(separator = ", ")

        queryString += " FROM "

        when (sUriMatcher.match(uri)) {
            CODE_ALL_ICALOBJECTS -> queryString += TABLE_NAME_ICALOBJECT

            CODE_SINGLE_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ?"
            CODE_SINGLE_ATTENDEE -> queryString += "$TABLE_NAME_ATTENDEE WHERE $TABLE_NAME_ATTENDEE.$COLUMN_ATTENDEE_ID = ?"
            CODE_SINGLE_CATEGORY -> queryString += "$TABLE_NAME_CATEGORY WHERE $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ID = ?"
            CODE_SINGLE_COMMENT -> queryString += "$TABLE_NAME_COMMENT WHERE $TABLE_NAME_COMMENT.$COLUMN_COMMENT_ID = ?"
            CODE_SINGLE_CONTACT -> queryString += "$TABLE_NAME_CONTACT WHERE $TABLE_NAME_CONTACT.$COLUMN_CONTACT_ID = ?"
            CODE_SINGLE_ORGANIZER -> queryString += "$TABLE_NAME_ORGANIZER WHERE $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_ID = ?"
            CODE_SINGLE_RELATEDTO -> queryString += "$TABLE_NAME_RELATEDTO WHERE $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_ID = ?"
            CODE_SINGLE_RESOURCE -> queryString += "$TABLE_NAME_RESOURCE WHERE $TABLE_NAME_RESOURCE.$COLUMN_RESOURCE_ID = ?"

            CODE_ALL_ATTENDEES_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_ATTENDEE ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_ATTENDEE.$COLUMN_ATTENDEE_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ?"
            CODE_ALL_CATEGORIES_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_CATEGORY ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ?"
            CODE_ALL_COMMENTS_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_COMMENT ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_COMMENT.$COLUMN_COMMENT_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ?"
            CODE_ALL_CONTACTS_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_CONTACT ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_CONTACT.$COLUMN_CONTACT_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ?"
            CODE_ALL_ORGANIZER_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_ORGANIZER ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ?"
            CODE_ALL_RELATEDTO_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_RELATEDTO ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ?"
            CODE_ALL_RESOURCE_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_RESOURCE ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_RESOURCE.$COLUMN_RESOURCE_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ?"

            CODE_SINGLE_ATTENDEE_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_ATTENDEE ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_ATTENDEE.$COLUMN_ATTENDEE_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ? AND $TABLE_NAME_ATTENDEE.$COLUMN_ATTENDEE_ID = ?"
            CODE_SINGLE_CATEGORY_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_CATEGORY ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ? AND $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ID = ?"
            CODE_SINGLE_COMMENT_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_COMMENT ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_COMMENT.$COLUMN_COMMENT_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ? AND $TABLE_NAME_COMMENT.$COLUMN_COMMENT_ID = ?"
            CODE_SINGLE_CONTACT_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_CONTACT ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_CONTACT.$COLUMN_CONTACT_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ? AND $TABLE_NAME_CONTACT.$COLUMN_CONTACT_ID = ?"
            CODE_SINGLE_ORGANIZER_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_ORGANIZER ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ? AND $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_ID = ?"
            CODE_SINGLE_RELATEDTO_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_RELATEDTO ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ? AND $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_ID = ?"
            CODE_SINGLE_RESOURCE_FOR_ICALOBJECT -> queryString += "$TABLE_NAME_ICALOBJECT INNER JOIN $TABLE_NAME_RESOURCE ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_RESOURCE.$COLUMN_RESOURCE_ICALOBJECT_ID WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ? AND $TABLE_NAME_RESOURCE.$COLUMN_RESOURCE_ID = ?"

            else -> throw IllegalArgumentException("Unknown URI: " + uri)
        }

        if (selection != null && sUriMatcher.match(uri) == CODE_ALL_ICALOBJECTS)
            queryString += " WHERE $selection"
        if (selection != null && sUriMatcher.match(uri) != CODE_ALL_ICALOBJECTS)
            queryString += " AND ($selection)"

        selectionArgs?.forEach { args.add(it) }          // add all selection args to the args array, no further validation needed here

        if (!sortOrder.isNullOrBlank())
            queryString += " ORDER BY $sortOrder"


        val query = SimpleSQLiteQuery(queryString, args.toArray())

        return database.getCursor(query)

    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {


        TODO("Implement this to handle requests to update one or more rows.")
    }

}

