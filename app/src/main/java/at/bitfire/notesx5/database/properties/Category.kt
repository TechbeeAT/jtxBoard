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


{
    companion object Factory {

        /**
         * Create a new [Category] from the specified [ContentValues].
         *
         * @param values A [Category] that at least contain [COLUMN_CATEGORY_TEXT] and [COLUMN_CATEGORY_ICALOBJECT_ID]
         * @return A newly created [Category] instance.
         */
        fun fromContentValues(@Nullable values: ContentValues?): Category? {

            if (values == null)
                return null

            if (values.getAsLong(COLUMN_CATEGORY_ICALOBJECT_ID) == null || values.getAsString(COLUMN_CATEGORY_TEXT) == null)     // at least a icalObjectId and text must be given for a Comment!
                return null

            return Category().applyContentValues(values)
        }
    }


    fun applyContentValues(values: ContentValues?): Category {

        if(values?.containsKey(COLUMN_CATEGORY_ICALOBJECT_ID) == true && values.getAsLong(COLUMN_CATEGORY_ICALOBJECT_ID) == null)
            this.icalObjectId = values.getAsLong(COLUMN_CATEGORY_ICALOBJECT_ID)

        if(values?.containsKey(COLUMN_CATEGORY_TEXT) == true && values.getAsString(COLUMN_CATEGORY_TEXT).isNotBlank())
            this.text = values.getAsString(COLUMN_CATEGORY_TEXT)

        if (values?.containsKey(COLUMN_CATEGORY_LANGUAGEPARAM) == true && values.getAsString(COLUMN_CATEGORY_LANGUAGEPARAM) != null) {
            this.languageparam = values.getAsString(COLUMN_CATEGORY_LANGUAGEPARAM)
        }
        if (values?.containsKey(COLUMN_CATEGORY_OTHERPARAM) == true && values.getAsString(COLUMN_CATEGORY_OTHERPARAM).isNotBlank()) {
            this.otherparam = values.getAsString(COLUMN_CATEGORY_OTHERPARAM)
        }

        return this

    }


}

