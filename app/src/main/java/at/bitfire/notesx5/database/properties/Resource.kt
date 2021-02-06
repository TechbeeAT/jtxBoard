package at.bitfire.notesx5.database.properties


import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.Nullable
import androidx.room.*
import at.bitfire.notesx5.database.*
import kotlinx.android.parcel.Parcelize


/** The name of the the table.  */
const val TABLE_NAME_RESOURCE = "resource"

/** The name of the ID column.  */
const val COLUMN_RESOURCE_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects. */
const val COLUMN_RESOURCE_ICALOBJECT_ID = "icalObjectId"


/** The names of all the other columns  */
const val COLUMN_RESOURCE_TEXT = "text"
const val COLUMN_RESOURCE_RELTYPEPARAM = "reltypeparam"
const val COLUMN_RESOURCE_OTHERPARAM = "otherparam"


@Parcelize
@Entity(tableName = TABLE_NAME_RESOURCE,
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf(COLUMN_ID),
                childColumns = arrayOf(COLUMN_RESOURCE_ICALOBJECT_ID),
                onDelete = ForeignKey.CASCADE)])
data class Resource (

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_RESOURCE_ID)
        var resourceId: Long = 0L,

        @ColumnInfo(index = true, name = COLUMN_RESOURCE_ICALOBJECT_ID)var icalObjectId: Long = 0L,
        @ColumnInfo(name = COLUMN_RESOURCE_TEXT)            var text: String? = "",
        @ColumnInfo(name = COLUMN_RESOURCE_RELTYPEPARAM)    var reltypeparam: String? = null,
        @ColumnInfo(name = COLUMN_RESOURCE_OTHERPARAM)      var otherparam: String? = null
): Parcelable


{
        companion object Factory {

                /**
                 * Create a new [Resource] from the specified [ContentValues].
                 *
                 * @param values A [Resource] that at least contain [COLUMN_RESOURCE_TEXT] and [COLUMN_RESOURCE_ICALOBJECT_ID].
                 * @return A newly created [Resource] instance.
                 */
                fun fromContentValues(@Nullable values: ContentValues?): Resource? {

                        if (values == null)
                                return null

                        if(values.getAsString(COLUMN_RESOURCE_TEXT) == null || values.getAsLong(COLUMN_RESOURCE_ICALOBJECT_ID) == null)
                                return null

                        val resource = Resource(icalObjectId = values.getAsLong(COLUMN_RESOURCE_ICALOBJECT_ID), text = values.getAsString(COLUMN_RESOURCE_TEXT))


                        if (values.containsKey(COLUMN_RESOURCE_RELTYPEPARAM)) {
                                resource.reltypeparam = values.getAsString(COLUMN_RESOURCE_RELTYPEPARAM)
                        }
                        if (values.containsKey(COLUMN_RESOURCE_OTHERPARAM)) {
                                resource.otherparam = values.getAsString(COLUMN_RESOURCE_OTHERPARAM)
                        }

                        return resource
                }
        }
}




