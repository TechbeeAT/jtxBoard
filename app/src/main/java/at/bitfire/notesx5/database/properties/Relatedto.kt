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
const val TABLE_NAME_RELATEDTO = "relatedto"

/** The name of the ID column.  */
const val COLUMN_RELATEDTO_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects. */
const val COLUMN_RELATEDTO_ICALOBJECT_ID = "icalObjectId"
const val COLUMN_RELATEDTO_LINKEDICALOBJECT_ID = "linkedICalObjectId"


/** The names of all the other columns  */
const val COLUMN_RELATEDTO_TEXT = "text"
const val COLUMN_RELATEDTO_RELTYPEPARAM = "reltypeparam"
const val COLUMN_RELATEDTO_OTHERPARAM = "otherparam"




@Parcelize
@Entity(tableName = TABLE_NAME_RELATEDTO,
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf(COLUMN_ID),
                childColumns = arrayOf(COLUMN_RELATEDTO_ICALOBJECT_ID),
                onDelete = ForeignKey.CASCADE)])
data class Relatedto (

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_RELATEDTO_ID)
        var relatedtoId: Long = 0L,

        @ColumnInfo(index = true, name = COLUMN_RELATEDTO_ICALOBJECT_ID)    var icalObjectId: Long = 0L,
        @ColumnInfo(index = true, name = COLUMN_RELATEDTO_LINKEDICALOBJECT_ID) var linkedICalObjectId: Long,
        @ColumnInfo(name = COLUMN_RELATEDTO_TEXT)                var text: String = "",
        @ColumnInfo(name = COLUMN_RELATEDTO_RELTYPEPARAM)        var reltypeparam: String? = null,
        @ColumnInfo(name = COLUMN_RELATEDTO_OTHERPARAM)          var otherparam: String? = null
): Parcelable


{
        companion object Factory {

                /**
                 * Create a new [Relatedto] from the specified [ContentValues].
                 *
                 * @param values A [Relatedto] that at least contain [COLUMN_RELATEDTO_ICALOBJECT_ID] and [COLUMN_RELATEDTO_LINKEDICALOBJECT_ID].
                 * @return A newly created [Relatedto] instance.
                 */
                fun fromContentValues(@Nullable values: ContentValues?): Relatedto? {

                        if (values == null)
                                return null

                        if (values.getAsLong(COLUMN_RELATEDTO_ICALOBJECT_ID) == null || values.getAsLong(COLUMN_RELATEDTO_LINKEDICALOBJECT_ID) == null)     // at least the the two linking entries must be set!
                                return null


                        val relatedto = Relatedto(icalObjectId = values.getAsLong(COLUMN_RELATEDTO_ICALOBJECT_ID), linkedICalObjectId = values.getAsLong(COLUMN_RELATEDTO_LINKEDICALOBJECT_ID))


                        if (values.containsKey(COLUMN_RELATEDTO_TEXT)) {
                                relatedto.text = values.getAsString(COLUMN_RELATEDTO_TEXT)
                        }
                        if (values.containsKey(COLUMN_RELATEDTO_RELTYPEPARAM)) {
                                relatedto.reltypeparam = values.getAsString(COLUMN_RESOURCE_RELTYPEPARAM)
                        }
                        if (values.containsKey(COLUMN_RELATEDTO_OTHERPARAM)) {
                                relatedto.otherparam = values.getAsString(COLUMN_RESOURCE_OTHERPARAM)
                        }

                        return relatedto
                }
        }

}


