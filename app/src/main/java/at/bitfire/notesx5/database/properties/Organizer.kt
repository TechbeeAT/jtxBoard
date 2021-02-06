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
const val TABLE_NAME_ORGANIZER = "organizer"

/** The name of the ID column.  */
const val COLUMN_ORGANIZER_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects. */
const val COLUMN_ORGANIZER_ICALOBJECT_ID = "icalObjectId"


/** The names of all the other columns  */
const val COLUMN_ORGANIZER_CALADDRESS = "caladdress"
const val COLUMN_ORGANIZER_CNPARAM = "cnparam"
const val COLUMN_ORGANIZER_DIRPARAM = "dirparam"
const val COLUMN_ORGANIZER_SENTBYPARAM = "sentbyparam"
const val COLUMN_ORGANIZER_LANGUAGEPARAM = "languageparam"
const val COLUMN_ORGANIZER_OTHERPARAM = "otherparam"



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
        @ColumnInfo(name = COLUMN_ORGANIZER_CNPARAM)     var cnparam: String? = null,
        @ColumnInfo(name = COLUMN_ORGANIZER_DIRPARAM)     var dirparam: String? = null,
        @ColumnInfo(name = COLUMN_ORGANIZER_SENTBYPARAM)     var sentbyparam: String? = null,
        @ColumnInfo(name = COLUMN_ORGANIZER_LANGUAGEPARAM)     var languageparam: String? = null,
        @ColumnInfo(name = COLUMN_ORGANIZER_OTHERPARAM)     var otherparam: String? = null
): Parcelable



{
        companion object Factory {

                /**
                 * Create a new [Organizer] from the specified [ContentValues].
                 *
                 * @param values A [Organizer] that at least contain [COLUMN_ORGANIZER_CALADDRESS] and [COLUMN_ORGANIZER_ICALOBJECT_ID]
                 * @return A newly created [Organizer] instance.
                 */
                fun fromContentValues(@Nullable values: ContentValues?): Organizer? {

                        if (values == null)
                                return null

                        if (values.getAsLong(COLUMN_ORGANIZER_ICALOBJECT_ID) == null || values.getAsString(COLUMN_ORGANIZER_CALADDRESS) == null)     // at least a icalObjectId and caladdress must be given for an Organizer!
                                return null

                        val organizer = Organizer(icalObjectId = values.getAsLong(COLUMN_ORGANIZER_ICALOBJECT_ID), caladdress = values.getAsString(COLUMN_ORGANIZER_CALADDRESS))

                        if (values.containsKey(COLUMN_ORGANIZER_CNPARAM)) {
                                organizer.cnparam = values.getAsString(COLUMN_ORGANIZER_CNPARAM)
                        }
                        if (values.containsKey(COLUMN_ORGANIZER_DIRPARAM)) {
                                organizer.dirparam = values.getAsString(COLUMN_ORGANIZER_DIRPARAM)
                        }
                        if (values.containsKey(COLUMN_ORGANIZER_SENTBYPARAM)) {
                                organizer.sentbyparam = values.getAsString(COLUMN_ORGANIZER_SENTBYPARAM)
                        }
                        if (values.containsKey(COLUMN_ORGANIZER_LANGUAGEPARAM)) {
                                organizer.languageparam = values.getAsString(COLUMN_ORGANIZER_LANGUAGEPARAM)
                        }
                        if (values.containsKey(COLUMN_ORGANIZER_OTHERPARAM)) {
                                organizer.otherparam = values.getAsString(COLUMN_ORGANIZER_OTHERPARAM)
                        }

                        return organizer
                }
        }

}


