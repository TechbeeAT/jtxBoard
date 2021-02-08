package at.bitfire.notesx5.database.properties

import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.Nullable
import androidx.room.*
import at.bitfire.notesx5.database.COLUMN_ID
import at.bitfire.notesx5.database.ICalObject
import kotlinx.android.parcel.Parcelize

/** The name of the the table.  */
const val TABLE_NAME_COMMENT = "comment"

/** The name of the ID column.  */
const val COLUMN_COMMENT_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects. */
const val COLUMN_COMMENT_ICALOBJECT_ID = "icalObjectId"


/** The names of all the other columns  */
const val COLUMN_COMMENT_TEXT = "text"
const val COLUMN_COMMENT_ALTREPPARAM = "altrepparam"
const val COLUMN_COMMENT_LANGUAGEPARAM = "languageparam"



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
    @ColumnInfo(name = COLUMN_COMMENT_ALTREPPARAM)                 var altrepparam: String? = null,
    @ColumnInfo(name = COLUMN_COMMENT_LANGUAGEPARAM)               var languageparam: String? = null
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

        if (values?.containsKey(COLUMN_COMMENT_ALTREPPARAM) == true && values.getAsString(COLUMN_COMMENT_ALTREPPARAM).isNotBlank()) {
            this.altrepparam = values.getAsString(COLUMN_COMMENT_ALTREPPARAM)
        }
        if (values?.containsKey(COLUMN_COMMENT_LANGUAGEPARAM) == true && values.getAsString(COLUMN_COMMENT_LANGUAGEPARAM).isNotBlank()) {
            this.languageparam = values.getAsString(COLUMN_COMMENT_LANGUAGEPARAM)
        }

        return this
    }


}


