package at.bitfire.notesx5.database.properties

import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.Nullable
import androidx.room.*
import at.bitfire.notesx5.database.COLUMN_ID
import at.bitfire.notesx5.database.ICalObject
import kotlinx.android.parcel.Parcelize

/** The name of the the table for Categories that are linked to an ICalObject. */
const val TABLE_NAME_CATEGORY = "category"

/** The name of the ID column for categories.
 * This is the unique identifier of a Category
 * Type: [Long]*/
const val COLUMN_CATEGORY_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects.
 * Type: [Long] */
const val COLUMN_CATEGORY_ICALOBJECT_ID = "icalObjectId"


/* The names of all the other columns  */
/**
 * Purpose:  This property defines the name of the category for a calendar component.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.2]
 * Type: [String]
 */
const val COLUMN_CATEGORY_TEXT = "text"
/**
 * Purpose:  To specify the language for text values in a property or property parameter, in this case of the category.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.2] and [https://tools.ietf.org/html/rfc5545#section-3.2.10]
 * Type: [String]
 */
const val COLUMN_CATEGORY_LANGUAGE = "language"
/**
 * Purpose:  To specify other properties for the category.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.2]
 * Type: [String]
 */
const val COLUMN_CATEGORY_OTHER = "other"


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
        @ColumnInfo(name = COLUMN_CATEGORY_TEXT)                       var text: String = "",
        @ColumnInfo(name = COLUMN_CATEGORY_LANGUAGE)                   var language: String? = null,
        @ColumnInfo(name = COLUMN_CATEGORY_OTHER)                      var other: String? = null
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

        if(values?.containsKey(COLUMN_CATEGORY_ICALOBJECT_ID) == true && values.getAsLong(COLUMN_CATEGORY_ICALOBJECT_ID) != null)
            this.icalObjectId = values.getAsLong(COLUMN_CATEGORY_ICALOBJECT_ID)

        if(values?.containsKey(COLUMN_CATEGORY_TEXT) == true && values.getAsString(COLUMN_CATEGORY_TEXT).isNotBlank())
            this.text = values.getAsString(COLUMN_CATEGORY_TEXT)

        if (values?.containsKey(COLUMN_CATEGORY_LANGUAGE) == true && values.getAsString(COLUMN_CATEGORY_LANGUAGE) != null) {
            this.language = values.getAsString(COLUMN_CATEGORY_LANGUAGE)
        }
        if (values?.containsKey(COLUMN_CATEGORY_OTHER) == true && values.getAsString(COLUMN_CATEGORY_OTHER).isNotBlank()) {
            this.other = values.getAsString(COLUMN_CATEGORY_OTHER)
        }

        return this

    }


}

