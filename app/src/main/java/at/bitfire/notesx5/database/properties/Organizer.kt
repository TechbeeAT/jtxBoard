package at.bitfire.notesx5.database.properties


import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.*
import at.bitfire.notesx5.database.COLUMN_ID
import at.bitfire.notesx5.database.ICalObject
import kotlinx.android.parcel.Parcelize


/** The name of the the table for Organizer that are linked to an ICalObject.
 * [https://tools.ietf.org/html/rfc5545#section-3.8.4.3]
 */
const val TABLE_NAME_ORGANIZER = "organizer"

/** The name of the ID column for the organizer.
 * This is the unique identifier of a Organizer
 * Type: [Long]*/
const val COLUMN_ORGANIZER_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects.
 * Type: [Long] */
const val COLUMN_ORGANIZER_ICALOBJECT_ID = "icalObjectId"


/* The names of all the other columns  */
/**
 * Purpose:  This value type is used to identify properties that contain a calendar user address (in this case of the organizer).
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.3] and [https://tools.ietf.org/html/rfc5545#section-3.3.3]
 * Type: [String]
 */
const val COLUMN_ORGANIZER_CALADDRESS = "caladdress"
/**
 * Purpose:  To specify the common name to be associated with the calendar user specified by the property in this case for the organizer.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.3] and [https://tools.ietf.org/html/rfc5545#section-3.2.18]
 * Type: [String]
 */
const val COLUMN_ORGANIZER_CN = "cnparam"
/**
 * Purpose:  To specify reference to a directory entry associated with the calendar user specified by the property in this case for the organizer.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.3] and [https://tools.ietf.org/html/rfc5545#section-3.2.2]
 * Type: [String]
 */
const val COLUMN_ORGANIZER_DIR = "dirparam"
/**
 * Purpose:  To specify the calendar user that is acting on behalf of the calendar user specified by the property in this case for the organizer.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.3] and [https://tools.ietf.org/html/rfc5545#section-3.2.18]
 * Type: [String]
 */
const val COLUMN_ORGANIZER_SENTBY = "sentbyparam"
/**
 * Purpose:  To specify the language for text values in a property or property parameter, in this case of the organizer.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.3] and [https://tools.ietf.org/html/rfc5545#section-3.2.10]
 * Type: [String]
 */
const val COLUMN_ORGANIZER_LANGUAGE = "language"
/**
 * Purpose:  To specify other properties for the organizer.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.3]
 * Type: [String]
 */
const val COLUMN_ORGANIZER_OTHER = "other"



@Parcelize
@Entity(tableName = TABLE_NAME_ORGANIZER,
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf(COLUMN_ID),
                childColumns = arrayOf(COLUMN_ORGANIZER_ICALOBJECT_ID),
                onDelete = ForeignKey.CASCADE)])
data class Organizer (

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_ORGANIZER_ID)
        var organizerId: Long = 0L,

        @ColumnInfo(index = true, name = COLUMN_ORGANIZER_ICALOBJECT_ID) var icalObjectId: Long = 0L,
        @ColumnInfo(name = COLUMN_ORGANIZER_CALADDRESS)     var caladdress: String = "",
        @ColumnInfo(name = COLUMN_ORGANIZER_CN)     var cn: String? = null,
        @ColumnInfo(name = COLUMN_ORGANIZER_DIR)     var dir: String? = null,
        @ColumnInfo(name = COLUMN_ORGANIZER_SENTBY)     var sentby: String? = null,
        @ColumnInfo(name = COLUMN_ORGANIZER_LANGUAGE)     var language: String? = null,
        @ColumnInfo(name = COLUMN_ORGANIZER_OTHER)     var other: String? = null
): Parcelable


{
        companion object Factory {

                /**
                 * Create a new [Organizer] from the specified [ContentValues].
                 *
                 * @param values A [Organizer] that at least contain [COLUMN_ORGANIZER_CALADDRESS] and [COLUMN_ORGANIZER_ICALOBJECT_ID]
                 * @return A newly created [Organizer] instance.
                 */
                fun fromContentValues(values: ContentValues?): Organizer? {

                        if (values == null)
                                return null

                        if (values.getAsLong(COLUMN_ORGANIZER_ICALOBJECT_ID) == null || values.getAsString(COLUMN_ORGANIZER_CALADDRESS) == null)     // at least a icalObjectId and caladdress must be given for an Organizer!
                                return null

                        return Organizer().applyContentValues(values)
                }
        }

        fun applyContentValues(values: ContentValues?): Organizer {

                if(values?.containsKey(COLUMN_ORGANIZER_ICALOBJECT_ID) == true && values.getAsLong(COLUMN_ORGANIZER_ICALOBJECT_ID) != null)
                        this.icalObjectId = values.getAsLong(COLUMN_ORGANIZER_ICALOBJECT_ID)

                if(values?.containsKey(COLUMN_ORGANIZER_CALADDRESS) == true && values.getAsString(COLUMN_ORGANIZER_CALADDRESS).isNotBlank())
                        this.caladdress = values.getAsString(COLUMN_ORGANIZER_CALADDRESS)



                if (values?.containsKey(COLUMN_ORGANIZER_CN) == true && values.getAsString(COLUMN_ORGANIZER_CN).isNotBlank()) {
                        this.cn = values.getAsString(COLUMN_ORGANIZER_CN)
                }
                if (values?.containsKey(COLUMN_ORGANIZER_DIR) == true && values.getAsString(COLUMN_ORGANIZER_DIR).isNotBlank()) {
                        this.dir = values.getAsString(COLUMN_ORGANIZER_DIR)
                }
                if (values?.containsKey(COLUMN_ORGANIZER_SENTBY) == true && values.getAsString(COLUMN_ORGANIZER_SENTBY).isNotBlank()) {
                        this.sentby = values.getAsString(COLUMN_ORGANIZER_SENTBY)
                }
                if (values?.containsKey(COLUMN_ORGANIZER_LANGUAGE) == true && values.getAsString(COLUMN_ORGANIZER_LANGUAGE).isNotBlank()) {
                        this.language = values.getAsString(COLUMN_ORGANIZER_LANGUAGE)
                }
                if (values?.containsKey(COLUMN_ORGANIZER_OTHER) == true && values.getAsString(COLUMN_ORGANIZER_OTHER).isNotBlank()) {
                        this.other = values.getAsString(COLUMN_ORGANIZER_OTHER)
                }

                return this
        }
}


