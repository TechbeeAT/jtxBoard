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

/* The names of all the other columns  */
/**
 * Purpose:  This column/property defines the url of the collection.
 * Type: [String]
 */
const val COLUMN_COLLECTION_URL = "url"
/**
 * Purpose:  This column/property defines the display name of the collection.
 * Type: [String]
 */
const val COLUMN_COLLECTION_DISPLAYNAME = "displayname"
/**
 * Purpose:  This column/property defines a description of the collection.
 * Type: [String]
 */
const val COLUMN_COLLECTION_DESCRIPTION = "description"
/**
 * Purpose:  This column/property defines the owner of the collection.
 * Type: [String]
 */
const val COLUMN_COLLECTION_OWNER = "owner"
/**
 * Purpose:  This column/property defines the color of the collection items.
 * This color can also be overwritten by the color in an ICalObject.
 * Type: [String]
 */
const val COLUMN_COLLECTION_COLOR = "color"
/**
 * Purpose:  This column/property defines the if the collection supports VEVENTs.
 * Type: [Boolean]
 */
const val COLUMN_COLLECTION_SUPPORTSVEVENT = "supportsVEVENT"
/**
 * Purpose:  This column/property defines the if the collection supports VTODOs.
 * Type: [Boolean]
 */
const val COLUMN_COLLECTION_SUPPORTSVTODO = "supportsVTODO"
/**
 * Purpose:  This column/property defines the if the collection supports VJOURNALs.
 * Type: [Boolean]
 */
const val COLUMN_COLLECTION_SUPPORTSVJOURNAL = "supportsVJOURNAL"
/**
 * Purpose:  This column/property defines the if the account name under which the collection resides.
 * Type: [String]
 */
const val COLUMN_COLLECTION_ACCOUNT_NAME = "accountname"
/**
 * Purpose:  This column/property defines the if the account type under which the collection resides.
 * Type: [String]
 */
const val COLUMN_COLLECTION_ACCOUNT_TYPE = "accounttype"
/**
 * Purpose:  This column/property defines a field for the Sync Version for the Sync Adapter
 * Type: [String]
 */
const val COLUMN_COLLECTION_SYNC_VERSION = "syncversion"
/**
 * Purpose:  This column/property defines if a collection is marked as read-only by the Sync Adapter
 * Type: [Boolean]
 */
const val COLUMN_COLLECTION_READONLY = "readonly"


@Parcelize
@Entity(tableName = TABLE_NAME_COLLECTION)
data class ICalCollection(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_COLLECTION_ID)   var collectionId: Long = 0L,

        @ColumnInfo(name = COLUMN_COLLECTION_URL)               var url: String = "LOCAL",

        @ColumnInfo(name = COLUMN_COLLECTION_DISPLAYNAME)       var displayName: String? = "LOCAL",
        @ColumnInfo(name = COLUMN_COLLECTION_DESCRIPTION)       var description: String? = null,
        @ColumnInfo(name = COLUMN_COLLECTION_OWNER)             var owner: String? = null,
        @ColumnInfo(name = COLUMN_COLLECTION_COLOR)             var color: String? = null,

        /** whether the collection supports VEVENT; in case of calendars: null means true */
        @ColumnInfo(name = COLUMN_COLLECTION_SUPPORTSVEVENT)          var supportsVEVENT: Boolean? = null,

        /** whether the collection supports VTODO; in case of calendars: null means true */
        @ColumnInfo(name = COLUMN_COLLECTION_SUPPORTSVTODO)     var supportsVTODO: Boolean? = null,

        /** whether the collection supports VJOURNAL; in case of calendars: null means true */
        @ColumnInfo(name = COLUMN_COLLECTION_SUPPORTSVJOURNAL)    var supportsVJOURNAL: Boolean? = null,

        /** Account name */
        @ColumnInfo(name = COLUMN_COLLECTION_ACCOUNT_NAME)            var accountName: String? = null,

        /** Webcal subscription source URL */
        @ColumnInfo(name = COLUMN_COLLECTION_ACCOUNT_TYPE)            var accountType: String? = null,

        /** Webcal subscription source URL */
        @ColumnInfo(name = COLUMN_COLLECTION_SYNC_VERSION)            var syncversion: String? = null,

        /** Webcal subscription source URL */
        @ColumnInfo(name = COLUMN_COLLECTION_READONLY)            var readonly: Boolean = false


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



        fun applyContentValues(values: ContentValues):ICalCollection {

                values.getAsString(COLUMN_COLLECTION_URL)?.let { url -> this.url = url }
                /*
                if (values?.containsKey(COLUMN_COLLECTION_URL) == true && values.getAsString(COLUMN_COLLECTION_URL).isNotBlank()) {
                        this.url = values.getAsString(COLUMN_COLLECTION_URL)
                }

                 */
                // TODO: Für alle mit der gleichen Struktur übernehmen!
                if (values?.containsKey(COLUMN_COLLECTION_DISPLAYNAME) == true && values.getAsString(COLUMN_COLLECTION_DISPLAYNAME).isNotBlank()) {
                        this.displayName = values.getAsString(COLUMN_COLLECTION_DISPLAYNAME)
                }
                if (values?.containsKey(COLUMN_COLLECTION_DESCRIPTION) == true && values.getAsString(COLUMN_COLLECTION_DESCRIPTION).isNotBlank()) {
                        this.description = values.getAsString(COLUMN_COLLECTION_DESCRIPTION)
                }
                if (values?.containsKey(COLUMN_COLLECTION_OWNER) == true && values.getAsString(COLUMN_COLLECTION_OWNER).isNotBlank()) {
                        this.owner = values.getAsString(COLUMN_COLLECTION_OWNER)
                }
                if (values?.containsKey(COLUMN_COLLECTION_COLOR) == true && values.getAsString(COLUMN_COLLECTION_COLOR).isNotBlank()) {
                        this.color = values.getAsString(COLUMN_COLLECTION_COLOR)
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
                if (values?.containsKey(COLUMN_COLLECTION_SYNC_VERSION) == true && values.getAsString(COLUMN_COLLECTION_SYNC_VERSION).isNotBlank()) {
                        this.syncversion = values.getAsString(COLUMN_COLLECTION_SYNC_VERSION)
                }
                if (values?.containsKey(COLUMN_COLLECTION_READONLY) == true && values.getAsBoolean(COLUMN_COLLECTION_READONLY) != null) {
                        this.readonly = values.getAsBoolean(COLUMN_COLLECTION_READONLY)
                }

                return this
        }
}

