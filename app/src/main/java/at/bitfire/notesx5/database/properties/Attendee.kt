package at.bitfire.notesx5.database.properties

import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.*
import at.bitfire.notesx5.database.COLUMN_COMPONENT
import at.bitfire.notesx5.database.COLUMN_ID
import at.bitfire.notesx5.database.ICalObject
import kotlinx.android.parcel.Parcelize


/** The name of the the table.  */
const val TABLE_NAME_ATTENDEE = "attendee"

/** The name of the ID column.  */
const val COLUMN_ATTENDEE_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects. */
const val COLUMN_ATTENDEE_ICALOBJECT_ID = "icalObjectId"


/** The names of all the other columns  */
const val COLUMN_ATTENDEE_CALADDRESS = "caladdress"
const val COLUMN_ATTENDEE_CUTYPEPARAM = "cutypeparam"
const val COLUMN_ATTENDEE_MEMBERPARAM = "memberparam"
const val COLUMN_ATTENDEE_ROLEPARAM = "roleparam"
const val COLUMN_ATTENDEE_ROLEPARAMX = "roleparamX"
const val COLUMN_ATTENDEE_PARTSTATPARAM = "partstatparam"
const val COLUMN_ATTENDEE_RSVPPARAM = "rsvpparam"
const val COLUMN_ATTENDEE_DELTOPARAM = "deltoparam"
const val COLUMN_ATTENDEE_DELFROMPARAM = "delfromparam"
const val COLUMN_ATTENDEE_SENTBYPARAM = "sentbyparam"
const val COLUMN_ATTENDEE_CNPARAM = "cnparam"
const val COLUMN_ATTENDEE_DIRPARAM = "dirparam"
const val COLUMN_ATTENDEE_LANGUAGEPARAM = "languageparam"
const val COLUMN_ATTENDEE_OTHERPARAM = "otherparam"




@Parcelize
@Entity(tableName = TABLE_NAME_ATTENDEE,
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf(COLUMN_ID),
                childColumns = arrayOf(COLUMN_ATTENDEE_ICALOBJECT_ID),
                onDelete = ForeignKey.CASCADE)]
)
data class Attendee (

        @PrimaryKey(autoGenerate = true)         // TODO Doublecheck ALL types here, crosscheck with RFC 5545
        @ColumnInfo(index = true, name = COLUMN_ATTENDEE_ID)    var attendeeId: Long = 0L,

        @ColumnInfo(index = true, name = COLUMN_ATTENDEE_ICALOBJECT_ID)       var icalObjectId: Long = 0L,
        @ColumnInfo(name = COLUMN_ATTENDEE_CALADDRESS)          var caladdress: String = "",
        @ColumnInfo(name = COLUMN_ATTENDEE_CUTYPEPARAM)         var cutypeparam: String = "INDIVIDUAL",
        @ColumnInfo(name = COLUMN_ATTENDEE_MEMBERPARAM)         var memberparam: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_ROLEPARAM)           var roleparam: Int? = 1,
        @ColumnInfo(name = COLUMN_ATTENDEE_ROLEPARAMX)          var roleparamX: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_PARTSTATPARAM)       var partstatparam: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_RSVPPARAM)           var rsvpparam: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_DELTOPARAM)          var deltoparam: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_DELFROMPARAM)        var delfromparam: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_SENTBYPARAM)         var sentbyparam: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_CNPARAM)             var cnparam: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_DIRPARAM)            var dirparam: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_LANGUAGEPARAM)       var languageparam: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_OTHERPARAM)          var otherparam: String? = null

): Parcelable