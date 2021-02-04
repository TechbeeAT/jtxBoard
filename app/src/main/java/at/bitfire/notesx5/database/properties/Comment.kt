package at.bitfire.notesx5.database.properties

import android.os.Parcelable
import android.provider.BaseColumns
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



