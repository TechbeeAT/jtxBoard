package at.bitfire.notesx5.database

import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.*
import kotlinx.android.parcel.Parcelize


/** The name of the the table for Collections.
 * ICalObjects MUST be linked to a collection! */
const val TABLE_NAME_COLLECTION = "collection"

/** The name of the ID column for collections.
 * This is the unique identifier of a Collection
 * Type: [Long]*/
const val COLUMN_COLLECTION_ID = BaseColumns._ID

/** The names of all the other columns  */
const val COLUMN_COLLECTION_URL = "url"
const val COLUMN_COLLECTION_DISPLAYNAME = "displayname"
const val COLUMN_COLLECTION_DESCRIPTION = "description"
const val COLUMN_COLLECTION_OWNER = "owner"
const val COLUMN_COLLECTION_COLOR = "color"
const val COLUMN_COLLECTION_TIMEZONE = "timezone"
const val COLUMN_COLLECTION_SOURCE = "source"
const val COLUMN_COLLECTION_SUPPORTSVEVENT = "supportsVEVENT"
const val COLUMN_COLLECTION_SUPPORTSVTODO = "supportsVTODO"
const val COLUMN_COLLECTION_SUPPORTSVJOURNAL = "supportsVJOURNAL"
const val COLUMN_COLLECTION_ACCOUNT_NAME = "accountname"
const val COLUMN_COLLECTION_ACCOUNT_TYPE = "accounttype"


@Parcelize
@Entity(tableName = TABLE_NAME_COLLECTION)
data class ICalCollection(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_COLLECTION_ID)   var collectionId: Long = 0L,

        //var type: String,
        @ColumnInfo(name = COLUMN_COLLECTION_URL)               var url: String = "LOCAL",

        //var privWriteContent: Boolean = true,
        //var privUnbind: Boolean = true,
        //var forceReadOnly: Boolean = false,

        @ColumnInfo(name = COLUMN_COLLECTION_DISPLAYNAME)       var displayName: String? = "LOCAL",
        @ColumnInfo(name = COLUMN_COLLECTION_DESCRIPTION)       var description: String? = null,
        @ColumnInfo(name = COLUMN_COLLECTION_OWNER)             var owner: String? = null,
        @ColumnInfo(name = COLUMN_COLLECTION_COLOR)             var color: Int? = null,

        /** timezone definition (full VTIMEZONE) - not a TZID! **/
        @ColumnInfo(name = COLUMN_COLLECTION_TIMEZONE)          var timezone: String? = null,

        /** whether the collection supports VEVENT; in case of calendars: null means true */
        @ColumnInfo(name = COLUMN_COLLECTION_SUPPORTSVEVENT)          var supportsVEVENT: Boolean? = null,

        /** whether the collection supports VTODO; in case of calendars: null means true */
        @ColumnInfo(name = COLUMN_COLLECTION_SUPPORTSVTODO)     var supportsVTODO: Boolean? = null,

        /** whether the collection supports VJOURNAL; in case of calendars: null means true */
        @ColumnInfo(name = COLUMN_COLLECTION_SUPPORTSVJOURNAL)    var supportsVJOURNAL: Boolean? = null,

        /** Webcal subscription source URL */
        @ColumnInfo(name = COLUMN_COLLECTION_SOURCE)            var source: String? = null,

        /** Account name */
        @ColumnInfo(name = COLUMN_COLLECTION_ACCOUNT_NAME)            var accountName: String? = null,


        /** Webcal subscription source URL */
        @ColumnInfo(name = COLUMN_COLLECTION_ACCOUNT_TYPE)            var accountType: String? = null

        /** whether this collection has been selected for synchronization */
        //var sync: Boolean = false
): Parcelable {
        companion object Factory {


                /**
                 * Create a new [ICalCollection] from the specified [ContentValues].
                 *
                 * @param values A [ICalCollection] that at least contain [.COLUMN_NAME].
                 * @return A newly created [ICalCollection] instance.
                 */
                fun fromContentValues(values: ContentValues?): ICalCollection? {

                        // TODO initialize specific component based on values!
                        // TODO validate some inputs, especially Int Inputs!

                        if (values == null)
                                return null

                        return ICalCollection().applyContentValues(values)

                }

        }



        fun applyContentValues(values: ContentValues?):ICalCollection {
                if (values?.containsKey(COLUMN_COLLECTION_URL) == true && values.getAsString(COLUMN_COLLECTION_URL).isNotBlank()) {
                        this.url = values.getAsString(COLUMN_COLLECTION_URL)
                }
                if (values?.containsKey(COLUMN_COLLECTION_DISPLAYNAME) == true && values.getAsString(COLUMN_COLLECTION_DISPLAYNAME).isNotBlank()) {
                        this.displayName = values.getAsString(COLUMN_COLLECTION_DISPLAYNAME)
                }
                if (values?.containsKey(COLUMN_COLLECTION_DESCRIPTION) == true && values.getAsString(COLUMN_COLLECTION_DESCRIPTION).isNotBlank()) {
                        this.description = values.getAsString(COLUMN_COLLECTION_DESCRIPTION)
                }
                if (values?.containsKey(COLUMN_COLLECTION_OWNER) == true && values.getAsString(COLUMN_COLLECTION_OWNER).isNotBlank()) {
                        this.owner = values.getAsString(COLUMN_COLLECTION_OWNER)
                }
                if (values?.containsKey(COLUMN_COLLECTION_COLOR) == true && values.getAsInteger(COLUMN_COLLECTION_COLOR) != null) {
                        this.color = values.getAsInteger(COLUMN_COLLECTION_COLOR)
                }
                if (values?.containsKey(COLUMN_COLLECTION_TIMEZONE) == true && values.getAsString(COLUMN_COLLECTION_TIMEZONE).isNotBlank()) {
                        this.timezone = values.getAsString(COLUMN_COLLECTION_TIMEZONE)
                }
                if (values?.containsKey(COLUMN_COLLECTION_SUPPORTSVEVENT) == true && values.getAsBoolean(COLUMN_COLLECTION_SUPPORTSVEVENT) != null) {
                        this.supportsVEVENT = values.getAsBoolean(COLUMN_COLLECTION_SUPPORTSVEVENT)
                }
                if (values?.containsKey(COLUMN_COLLECTION_SUPPORTSVTODO) == true && values.getAsBoolean(COLUMN_COLLECTION_SUPPORTSVTODO) != null) {
                        this.supportsVTODO = values.getAsBoolean(COLUMN_COLLECTION_SUPPORTSVTODO)
                }
                if (values?.containsKey(COLUMN_COLLECTION_SUPPORTSVJOURNAL) == true && values.getAsBoolean(COLUMN_COLLECTION_SUPPORTSVJOURNAL) != null) {
                        this.supportsVJOURNAL = values.getAsBoolean(COLUMN_COLLECTION_SUPPORTSVJOURNAL)
                }
                if (values?.containsKey(COLUMN_COLLECTION_SOURCE) == true && values.getAsString(COLUMN_COLLECTION_SOURCE).isNotBlank()) {
                        this.source = values.getAsString(COLUMN_COLLECTION_SOURCE)
                }

                return this
        }
}

