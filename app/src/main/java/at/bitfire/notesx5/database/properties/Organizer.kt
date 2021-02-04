package at.bitfire.notesx5.database.properties


import android.os.Parcelable
import android.provider.BaseColumns
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
