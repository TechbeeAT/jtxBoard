package at.bitfire.notesx5

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*


private const val CODE_ICALOBJECTS_DIR = 1
private const val CODE_ATTENDEES_DIR = 2
private const val CODE_CATEGORIES_DIR = 3
private const val CODE_COMMENTS_DIR = 4
private const val CODE_CONTACTS_DIR = 5
private const val CODE_ORGANIZER_DIR = 6
private const val CODE_RELATEDTO_DIR = 7
private const val CODE_RESOURCE_DIR = 8


private const val CODE_ICALOBJECT_ITEM = 101
private const val CODE_ATTENDEE_ITEM = 102
private const val CODE_CATEGORY_ITEM = 103
private const val CODE_COMMENT_ITEM = 104
private const val CODE_CONTACT_ITEM = 105
private const val CODE_ORGANIZER_ITEM = 106
private const val CODE_RELATEDTO_ITEM = 107
private const val CODE_RESOURCE_ITEM = 108


const val AUTHORITY = "at.bitfire.notesx5.provider"

/** The URI for the Icalobject table.  */



class SyncContentProvider : ContentProvider() {


    private lateinit var database: ICalDatabaseDao
    //private lateinit var context: Context


    private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        /* see https://github.com/android/architecture-components-samples/blob/4c606ccd83fded22a52d0d994b0cb043dafc6dd7/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/provider/SampleContentProvider.java
         * for a good sample and some explanations
         */
        addURI(AUTHORITY, "icalobject", CODE_ICALOBJECTS_DIR)
        addURI(AUTHORITY, "attendee", CODE_ATTENDEES_DIR)
        addURI(AUTHORITY, "category", CODE_CATEGORIES_DIR)
        addURI(AUTHORITY, "comment", CODE_COMMENTS_DIR)
        addURI(AUTHORITY, "contact", CODE_CONTACTS_DIR)
        addURI(AUTHORITY, "organizer", CODE_ORGANIZER_DIR)
        addURI(AUTHORITY, "relatedto", CODE_RELATEDTO_DIR)
        addURI(AUTHORITY, "resource", CODE_RESOURCE_DIR)

        addURI(AUTHORITY, "icalobject/#", CODE_ICALOBJECT_ITEM)
        addURI(AUTHORITY, "attendee/#", CODE_ATTENDEE_ITEM)
        addURI(AUTHORITY, "category/#", CODE_CATEGORY_ITEM)
        addURI(AUTHORITY, "comment/#", CODE_COMMENT_ITEM)
        addURI(AUTHORITY, "contact/#", CODE_CONTACT_ITEM)
        addURI(AUTHORITY, "organizer/#", CODE_ORGANIZER_ITEM)
        addURI(AUTHORITY, "relatedto/#", CODE_RELATEDTO_ITEM)
        addURI(AUTHORITY, "resource/#", CODE_RESOURCE_ITEM)

    }



    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {

        val count: Int

        when (sUriMatcher.match(uri)) {
            CODE_ICALOBJECTS_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot delete without ID ($uri)")
            CODE_ATTENDEES_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot delete without ID ($uri)")
            CODE_CATEGORIES_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot delete without ID ($uri)")
            CODE_COMMENTS_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot delete without ID ($uri)")
            CODE_CONTACTS_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot delete without ID ($uri)")
            CODE_ORGANIZER_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot delete without ID ($uri)")
            CODE_RELATEDTO_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot delete without ID ($uri)")
            CODE_RESOURCE_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot delete without ID ($uri)")

            CODE_ICALOBJECT_ITEM -> count = database.deleteICalObjectById(ContentUris.parseId(uri))
            CODE_ATTENDEE_ITEM -> count = database.deleteAttendeeById(ContentUris.parseId(uri))
            CODE_CATEGORY_ITEM -> count = database.deleteCategoryById(ContentUris.parseId(uri))
            CODE_COMMENT_ITEM -> count = database.deleteCommentById(ContentUris.parseId(uri))
            CODE_CONTACT_ITEM -> count = database.deleteContactById(ContentUris.parseId(uri))
            CODE_ORGANIZER_ITEM -> count = database.deleteOrganizerById(ContentUris.parseId(uri))
            CODE_RELATEDTO_ITEM -> count = database.deleteRelatedtoById(ContentUris.parseId(uri))
            CODE_RESOURCE_ITEM -> count = database.deleteResourceById(ContentUris.parseId(uri))

            else -> throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }

        if (context == null)
            return 0

        context!!.contentResolver.notifyChange(uri, null)
        return count

    }

    override fun getType(uri: Uri): String? {
        TODO("Implement this to handle requests for the MIME type of the data" +
                "at the given URI")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {

        val id: Long?

        when (sUriMatcher.match(uri)) {
            CODE_ICALOBJECTS_DIR -> id = ICalObject.fromContentValues(values)?.let { database.insertICalObjectSync(it) }
            CODE_ATTENDEES_DIR -> id = Attendee.fromContentValues(values)?.let { database.insertAttendeeSync(it) }
            CODE_CATEGORIES_DIR -> id = Category.fromContentValues(values)?.let { database.insertCategorySync(it) }
            CODE_COMMENTS_DIR -> id = Comment.fromContentValues(values)?.let { database.insertCommentSync(it) }
            CODE_CONTACTS_DIR -> id = Contact.fromContentValues(values)?.let { database.insertContactSync(it) }
            CODE_ORGANIZER_DIR -> id = Organizer.fromContentValues(values)?.let { database.insertOrganizerSync(it) }
            CODE_RELATEDTO_DIR -> id = Relatedto.fromContentValues(values)?.let { database.insertRelatedtoSync(it) }
            CODE_RESOURCE_DIR -> id = Resource.fromContentValues(values)?.let { database.insertResourceSync(it) }

            CODE_ICALOBJECT_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_ATTENDEE_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_CATEGORY_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_COMMENT_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_CONTACT_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_ORGANIZER_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_RELATEDTO_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")
            CODE_RESOURCE_ITEM -> throw IllegalArgumentException("Invalid URI, cannot insert with ID ($uri)")

            else -> throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }

        if (context == null)
            return null

        if(id == null)
            return null

        context!!.contentResolver.notifyChange(uri, null)
        Log.println(Log.INFO, "newContentUri", ContentUris.withAppendedId(uri, id).toString())
        return ContentUris.withAppendedId(uri, id)

    }

    override fun onCreate(): Boolean {

        if(context?.applicationContext == null)
            return false

        database = ICalDatabase.getInstance(context!!.applicationContext).iCalDatabaseDao
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {

        // TODO: validate uri and throw IllegalArgumentException if wrong

        val args = arrayListOf<String>()

        if (uri.pathSegments.size >= 2)
            args.add(uri.pathSegments[1].toLong().toString())      // add first argument (must be Long! String is expected, toLong would make other values null


        var queryString = "SELECT "
        if (projection.isNullOrEmpty())
            queryString += "*"
        else
            queryString += projection.joinToString(separator = ", ")

        queryString += " FROM "

        when (sUriMatcher.match(uri)) {
            CODE_ICALOBJECTS_DIR -> queryString += TABLE_NAME_ICALOBJECT
            CODE_ATTENDEES_DIR -> queryString += TABLE_NAME_ATTENDEE
            CODE_CATEGORIES_DIR -> queryString += TABLE_NAME_CATEGORY
            CODE_COMMENTS_DIR -> queryString += TABLE_NAME_COMMENT
            CODE_CONTACTS_DIR -> queryString += TABLE_NAME_CONTACT
            CODE_ORGANIZER_DIR -> queryString += TABLE_NAME_ORGANIZER
            CODE_RELATEDTO_DIR -> queryString += TABLE_NAME_RELATEDTO
            CODE_RESOURCE_DIR -> queryString += TABLE_NAME_RESOURCE

            CODE_ICALOBJECT_ITEM -> queryString += "$TABLE_NAME_ICALOBJECT WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ID = ?"
            CODE_ATTENDEE_ITEM -> queryString += "$TABLE_NAME_ATTENDEE WHERE $TABLE_NAME_ATTENDEE.$COLUMN_ATTENDEE_ID = ?"
            CODE_CATEGORY_ITEM -> queryString += "$TABLE_NAME_CATEGORY WHERE $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ID = ?"
            CODE_COMMENT_ITEM -> queryString += "$TABLE_NAME_COMMENT WHERE $TABLE_NAME_COMMENT.$COLUMN_COMMENT_ID = ?"
            CODE_CONTACT_ITEM -> queryString += "$TABLE_NAME_CONTACT WHERE $TABLE_NAME_CONTACT.$COLUMN_CONTACT_ID = ?"
            CODE_ORGANIZER_ITEM -> queryString += "$TABLE_NAME_ORGANIZER WHERE $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_ID = ?"
            CODE_RELATEDTO_ITEM -> queryString += "$TABLE_NAME_RELATEDTO WHERE $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_ID = ?"
            CODE_RESOURCE_ITEM -> queryString += "$TABLE_NAME_RESOURCE WHERE $TABLE_NAME_RESOURCE.$COLUMN_RESOURCE_ID = ?"

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        if (selection != null && sUriMatcher.match(uri) < 99)      // < 99 are DIRs and have no parameter!
            queryString += " WHERE $selection"
        if (selection != null && sUriMatcher.match(uri) > 99)
            queryString += " AND ($selection)"

        selectionArgs?.forEach { args.add(it) }          // add all selection args to the args array, no further validation needed here

        if (!sortOrder.isNullOrBlank())
            queryString += " ORDER BY $sortOrder"

        val query = SimpleSQLiteQuery(queryString, args.toArray())

        Log.println(Log.INFO, "SyncContentProvider", "Query prepared: $queryString")
        Log.println(Log.INFO, "SyncContentProvider", "Query args prepared: ${args.joinToString(separator = ", ")}")

        return database.getCursor(query)

    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {

        val count: Int

        when (sUriMatcher.match(uri)) {
            CODE_ICALOBJECTS_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot update without ID ($uri)")
            CODE_ATTENDEES_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot update without ID ($uri)")
            CODE_CATEGORIES_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot update without ID ($uri)")
            CODE_COMMENTS_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot update without ID ($uri)")
            CODE_CONTACTS_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot update without ID ($uri)")
            CODE_ORGANIZER_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot update without ID ($uri)")
            CODE_RELATEDTO_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot update without ID ($uri)")
            CODE_RESOURCE_DIR -> throw java.lang.IllegalArgumentException("Invalid URI, cannot update without ID ($uri)")

            CODE_ICALOBJECT_ITEM -> database.getICalObjectByIdSync(ContentUris.parseId(uri)).also {
                if (it != null)
                    count = database.updateICalObjectSync(it.applyContentValues(values))
                else throw java.lang.IllegalArgumentException("Invalid URI, ID not found ($uri)")
            }
            CODE_ATTENDEE_ITEM -> database.getAttendeeByIdSync(ContentUris.parseId(uri)).also {
                if (it != null)
                    count = database.updateAttendeeSync(it.applyContentValues(values))
                else throw java.lang.IllegalArgumentException("Invalid URI, ID not found ($uri)")
            }
            CODE_CATEGORY_ITEM -> database.getCategoryByIdSync(ContentUris.parseId(uri)).also {
                if (it != null)
                    count = database.updateCategorySync(it.applyContentValues(values))
                else throw java.lang.IllegalArgumentException("Invalid URI, ID not found ($uri)")
            }
            CODE_COMMENT_ITEM -> database.getCommentByIdSync(ContentUris.parseId(uri)).also {
                if (it != null)
                    count = database.updateCommentSync(it.applyContentValues(values))
                else throw java.lang.IllegalArgumentException("Invalid URI, ID not found ($uri)")
            }
            CODE_CONTACT_ITEM -> database.getContactByIdSync(ContentUris.parseId(uri)).also {
                if (it != null)
                    count = database.updateContactSync(it.applyContentValues(values))
                else throw java.lang.IllegalArgumentException("Invalid URI, ID not found ($uri)")
            }
            CODE_ORGANIZER_ITEM -> database.getOrganizerByIdSync(ContentUris.parseId(uri)).also {
                if (it != null)
                    count = database.updateOrganizerSync(it.applyContentValues(values))
                else throw java.lang.IllegalArgumentException("Invalid URI, ID not found ($uri)")
            }
            CODE_RELATEDTO_ITEM -> database.getRelatedtoByIdSync(ContentUris.parseId(uri)).also {
                if (it != null)
                    count = database.updateRelatedtoSync(it.applyContentValues(values))
                else throw java.lang.IllegalArgumentException("Invalid URI, ID not found ($uri)")
            }
            CODE_RESOURCE_ITEM -> database.getResourceByIdSync(ContentUris.parseId(uri)).also {
                if (it != null)
                    count = database.updateResourceSync(it.applyContentValues(values))
                else throw java.lang.IllegalArgumentException("Invalid URI, ID not found ($uri)")
            }

            else -> throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }

        if (context == null)
            return 0

        context!!.contentResolver.notifyChange(uri, null)
        return count

    }

}

