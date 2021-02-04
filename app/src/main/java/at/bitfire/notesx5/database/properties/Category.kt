package at.bitfire.notesx5.database.properties

import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.*
import at.bitfire.notesx5.database.COLUMN_ID
import at.bitfire.notesx5.database.ICalObject
import kotlinx.android.parcel.Parcelize

/** The name of the the table.  */
const val TABLE_NAME_CATEGORY = "category"

/** The name of the ID column.  */
const val COLUMN_CATEGORY_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects. */
const val COLUMN_CATEGORY_ICALOBJECT_ID = "icalObjectId"


/** The names of all the other columns  */
const val COLUMN_CATEGORY_TEXT = "text"
const val COLUMN_CATEGORY_LANGUAGEPARAM = "languageparam"
const val COLUMN_CATEGORY_OTHERPARAM = "otherparam"


@Parcelize
@Entity(tableName = TABLE_NAME_CATEGORY,
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf(COLUMN_ID),
                childColumns = arrayOf(COLUMN_CATEGORY_ICALOBJECT_ID),
                onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = [COLUMN_CATEGORY_ID, COLUMN_CATEGORY_ICALOBJECT_ID]),
                Index(value = [COLUMN_CATEGORY_TEXT])])

data class Category (

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_CATEGORY_ID)
        var categoryId: Long = 0L,

        @ColumnInfo(index = true, name = COLUMN_CATEGORY_ICALOBJECT_ID)     var icalObjectId: Long = 0L,
        @ColumnInfo(name = COLUMN_CATEGORY_TEXT)                            var text: String = "",
        @ColumnInfo(name = COLUMN_CATEGORY_LANGUAGEPARAM)                   var languageparam: String? = null,
        @ColumnInfo(name = COLUMN_CATEGORY_OTHERPARAM)                      var otherparam: String? = null
): Parcelable

