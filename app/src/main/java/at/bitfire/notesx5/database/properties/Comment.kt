package at.bitfire.notesx5.database.properties

import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.Nullable
import androidx.room.*
import at.bitfire.notesx5.database.COLUMN_ID
import at.bitfire.notesx5.database.ICalObject
import kotlinx.android.parcel.Parcelize

/** The name of the the table for Comments that are linked to an ICalObject.
 * [https://tools.ietf.org/html/rfc5545#section-3.8.1.4]*/
const val TABLE_NAME_COMMENT = "comment"

/** The name of the ID column for comments.
 * This is the unique identifier of a Comment
 * Type: [Long]*/
const val COLUMN_COMMENT_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects.
 * Type: [Long] */
const val COLUMN_COMMENT_ICALOBJECT_ID = "icalObjectId"


/** The names of all the other columns  */
/**
 * Purpose:  This property specifies non-processing information intended to provide a comment to the calendar user.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.4]
 * Type: [String]
 */
const val COLUMN_COMMENT_TEXT = "text"
/**
 * Purpose:  To specify the language for text values in a property or property parameter, in this case of the comment.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.4]
 * Type: [String]
 */
const val COLUMN_COMMENT_ALTREP = "altrep"
/**
 * Purpose:  To specify an alternate text representation for the property value, in this case of the comment.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.4]
 * Type: [String]
 */
const val COLUMN_COMMENT_LANGUAGE = "language"
/**
 * Purpose:  To specify other properties for the comment.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.4]
 * Type: [String]
 */
const val COLUMN_COMMENT_OTHER = "other"




@Parcelize
@Entity(tableName = TABLE_NAME_COMMENT,
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf(COLUMN_ID),
                childColumns = arrayOf(COLUMN_COMMENT_ICALOBJECT_ID),
                onDelete = ForeignKey.CASCADE)])
data class Comment (

        @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_COMMENT_ID)
    var commentId: Long = 0L,

        @ColumnInfo(index = true, name = COLUMN_COMMENT_ICALOBJECT_ID) var icalObjectId: Long = 0L,
        @ColumnInfo(name = COLUMN_COMMENT_TEXT)                        var text: String = "",
        @ColumnInfo(name = COLUMN_COMMENT_ALTREP)                 var altrep: String? = null,
        @ColumnInfo(name = COLUMN_COMMENT_LANGUAGE)               var language: String? = null,
        @ColumnInfo(name = COLUMN_COMMENT_OTHER)                      var other: String? = null
): Parcelable


{
    companion object Factory {

        /**
         * Create a new [Comment] from the specified [ContentValues].
         *
         * @param values A [Comment] that at least contain [COLUMN_COMMENT_TEXT] and [COLUMN_COMMENT_ICALOBJECT_ID]
         * @return A newly created [Comment] instance.
         */
        fun fromContentValues(@Nullable values: ContentValues?): Comment? {

            if (values == null)
                return null

            if (values.getAsLong(COLUMN_COMMENT_ICALOBJECT_ID) == null || values.getAsString(COLUMN_COMMENT_TEXT) == null)     // at least a icalObjectId and text must be given for a Comment!
                return null

            return Comment().applyContentValues(values)
        }
    }

    fun applyContentValues(values: ContentValues?): Comment {

        if(values?.containsKey(COLUMN_COMMENT_ICALOBJECT_ID) == true && values.getAsLong(COLUMN_COMMENT_ICALOBJECT_ID) != null)
            this.icalObjectId = values.getAsLong(COLUMN_COMMENT_ICALOBJECT_ID)

        if(values?.containsKey(COLUMN_COMMENT_TEXT) == true && values.getAsString(COLUMN_COMMENT_TEXT).isNotBlank())
            this.text = values.getAsString(COLUMN_COMMENT_TEXT)

        if (values?.containsKey(COLUMN_COMMENT_ALTREP) == true && values.getAsString(COLUMN_COMMENT_ALTREP).isNotBlank()) {
            this.altrep = values.getAsString(COLUMN_COMMENT_ALTREP)
        }
        if (values?.containsKey(COLUMN_COMMENT_LANGUAGE) == true && values.getAsString(COLUMN_COMMENT_LANGUAGE).isNotBlank()) {
            this.language = values.getAsString(COLUMN_COMMENT_LANGUAGE)
        }
        if (values?.containsKey(COLUMN_COMMENT_OTHER) == true && values.getAsString(COLUMN_COMMENT_OTHER).isNotBlank()) {
            this.other = values.getAsString(COLUMN_COMMENT_OTHER)
        }

        return this
    }


}


