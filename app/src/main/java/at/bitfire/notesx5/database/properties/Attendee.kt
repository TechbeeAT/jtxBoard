package at.bitfire.notesx5.database.properties

import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.Nullable
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



{
        companion object Factory {

                /**
                 * Create a new [Attendee] from the specified [ContentValues].
                 *
                 * @param values A [Attendee] that at least contain [COLUMN_ATTENDEE_CALADDRESS] and [COLUMN_ATTENDEE_ICALOBJECT_ID]
                 * @return A newly created [Attendee] instance.
                 */
                fun fromContentValues(@Nullable values: ContentValues?): Attendee? {

                        if (values == null)
                                return null

                        if (values.getAsLong(COLUMN_ATTENDEE_ICALOBJECT_ID) == null || values.getAsString(COLUMN_ATTENDEE_CALADDRESS) == null)     // at least a icalObjectId and text must be given for an Attendee!
                                return null

                        val attendee = Attendee(icalObjectId = values.getAsLong(COLUMN_ATTENDEE_ICALOBJECT_ID), caladdress = values.getAsString(COLUMN_ATTENDEE_CALADDRESS))


                        if (values.containsKey(COLUMN_ATTENDEE_CUTYPEPARAM)) {
                                attendee.cutypeparam = values.getAsString(COLUMN_ATTENDEE_CUTYPEPARAM)
                        }
                        if (values.containsKey(COLUMN_ATTENDEE_MEMBERPARAM)) {
                                attendee.memberparam = values.getAsString(COLUMN_ATTENDEE_MEMBERPARAM)
                        }
                        if (values.containsKey(COLUMN_ATTENDEE_ROLEPARAM)) {
                                attendee.roleparam = values.getAsInteger(COLUMN_ATTENDEE_ROLEPARAM)
                        }
                        if (values.containsKey(COLUMN_ATTENDEE_ROLEPARAMX)) {
                                attendee.roleparamX = values.getAsString(COLUMN_ATTENDEE_ROLEPARAMX)
                        }
                        if (values.containsKey(COLUMN_ATTENDEE_PARTSTATPARAM)) {
                                attendee.partstatparam = values.getAsString(COLUMN_ATTENDEE_PARTSTATPARAM)
                        }
                        if (values.containsKey(COLUMN_ATTENDEE_RSVPPARAM)) {
                                attendee.rsvpparam = values.getAsString(COLUMN_ATTENDEE_RSVPPARAM)
                        }
                        if (values.containsKey(COLUMN_ATTENDEE_DELTOPARAM)) {
                                attendee.deltoparam = values.getAsString(COLUMN_ATTENDEE_DELTOPARAM)
                        }
                        if (values.containsKey(COLUMN_ATTENDEE_DELFROMPARAM)) {
                                attendee.delfromparam = values.getAsString(COLUMN_ATTENDEE_DELFROMPARAM)
                        }
                        if (values.containsKey(COLUMN_ATTENDEE_SENTBYPARAM)) {
                                attendee.sentbyparam = values.getAsString(COLUMN_ATTENDEE_SENTBYPARAM)
                        }
                        if (values.containsKey(COLUMN_ATTENDEE_CNPARAM)) {
                                attendee.cnparam = values.getAsString(COLUMN_ATTENDEE_CNPARAM)
                        }
                        if (values.containsKey(COLUMN_ATTENDEE_DIRPARAM)) {
                                attendee.dirparam = values.getAsString(COLUMN_ATTENDEE_DIRPARAM)
                        }
                        if (values.containsKey(COLUMN_ATTENDEE_LANGUAGEPARAM)) {
                                attendee.languageparam = values.getAsString(COLUMN_ATTENDEE_LANGUAGEPARAM)
                        }
                        if (values.containsKey(COLUMN_ATTENDEE_OTHERPARAM)) {
                                attendee.otherparam = values.getAsString(COLUMN_ATTENDEE_OTHERPARAM)
                        }

                        return attendee
                }
        }
}

