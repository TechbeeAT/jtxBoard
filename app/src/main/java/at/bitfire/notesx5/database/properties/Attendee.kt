package at.bitfire.notesx5.database.properties

import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.Nullable
import androidx.room.*
import at.bitfire.notesx5.R
import at.bitfire.notesx5.database.COLUMN_ID
import at.bitfire.notesx5.database.Classification
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
        @ColumnInfo(name = COLUMN_ATTENDEE_CUTYPEPARAM)         var cutypeparam: String? = Cutypeparam.INDIVIDUAL.param,
        @ColumnInfo(name = COLUMN_ATTENDEE_MEMBERPARAM)         var memberparam: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_ROLEPARAM)           var roleparam: String? = Roleparam.REQ_PARTICIPANT.param,
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

                        val attendee = Attendee()
                        attendee.applyContentValues(values)

                        return Attendee().applyContentValues(values)
                }
        }


        fun applyContentValues(values: ContentValues?):Attendee {

                if(values?.containsKey(COLUMN_ATTENDEE_ICALOBJECT_ID) == true && values.getAsLong(COLUMN_ATTENDEE_ICALOBJECT_ID) != null)
                        this.icalObjectId = values.getAsLong(COLUMN_ATTENDEE_ICALOBJECT_ID)

                if(values?.containsKey(COLUMN_ATTENDEE_CALADDRESS) == true && values.getAsString(COLUMN_ATTENDEE_CALADDRESS).isNotBlank())
                        this.caladdress = values.getAsString(COLUMN_ATTENDEE_CALADDRESS)



                if (values?.containsKey(COLUMN_ATTENDEE_CUTYPEPARAM) == true && values.getAsString(COLUMN_ATTENDEE_CUTYPEPARAM).isNotBlank()) {
                        this.cutypeparam = values.getAsString(COLUMN_ATTENDEE_CUTYPEPARAM)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_MEMBERPARAM) == true && values.getAsString(COLUMN_ATTENDEE_MEMBERPARAM).isNotBlank()) {
                        this.memberparam = values.getAsString(COLUMN_ATTENDEE_MEMBERPARAM)
                }

                if (values?.containsKey(COLUMN_ATTENDEE_ROLEPARAM) == true && values.getAsString(COLUMN_ATTENDEE_ROLEPARAM).isNotBlank()) {
                        this.roleparam = values.getAsString(COLUMN_ATTENDEE_ROLEPARAM)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_PARTSTATPARAM) == true && values.getAsString(COLUMN_ATTENDEE_PARTSTATPARAM).isNotBlank()) {
                        this.partstatparam = values.getAsString(COLUMN_ATTENDEE_PARTSTATPARAM)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_RSVPPARAM) == true && values.getAsString(COLUMN_ATTENDEE_RSVPPARAM).isNotBlank()) {
                        this.rsvpparam = values.getAsString(COLUMN_ATTENDEE_RSVPPARAM)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_DELTOPARAM) == true && values.getAsString(COLUMN_ATTENDEE_DELTOPARAM).isNotBlank()) {
                        this.deltoparam = values.getAsString(COLUMN_ATTENDEE_DELTOPARAM)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_DELFROMPARAM) == true && values.getAsString(COLUMN_ATTENDEE_DELFROMPARAM).isNotBlank()) {
                        this.delfromparam = values.getAsString(COLUMN_ATTENDEE_DELFROMPARAM)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_SENTBYPARAM) == true && values.getAsString(COLUMN_ATTENDEE_SENTBYPARAM).isNotBlank()) {
                        this.sentbyparam = values.getAsString(COLUMN_ATTENDEE_SENTBYPARAM)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_CNPARAM) == true && values.getAsString(COLUMN_ATTENDEE_CNPARAM).isNotBlank()) {
                        this.cnparam = values.getAsString(COLUMN_ATTENDEE_CNPARAM)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_DIRPARAM) == true && values.getAsString(COLUMN_ATTENDEE_DIRPARAM).isNotBlank()) {
                        this.dirparam = values.getAsString(COLUMN_ATTENDEE_DIRPARAM)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_LANGUAGEPARAM) == true && values.getAsString(COLUMN_ATTENDEE_LANGUAGEPARAM).isNotBlank()) {
                        this.languageparam = values.getAsString(COLUMN_ATTENDEE_LANGUAGEPARAM)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_OTHERPARAM) == true && values.getAsString(COLUMN_ATTENDEE_OTHERPARAM).isNotBlank()) {
                        this.otherparam = values.getAsString(COLUMN_ATTENDEE_OTHERPARAM)
                }

                return this

        }

}

enum class Cutypeparam (val id: Int, val param: String?) {

        INDIVIDUAL(0,"INDIVIDUAL"),
        GROUP(1,"GROUP"),
        RESOURCE(2,"RESOURCE"),
        ROOM(3,"ROOM"),
        UNKNOWN(4,"UNKNOWN")
}

enum class Roleparam (val id: Int, val param: String?, val stringResource: Int, val icon: Int) {
        CHAIR (0,"CHAIR", R.string.attendee_role_chair, R.drawable.ic_attendee_chair),            //Indicates chair of the calendar entity
        REQ_PARTICIPANT(1,"REQ-PARTICIPANT", R.string.attendee_role_required_participant, R.drawable.ic_attendee_reqparticipant),  //Indicates a participant whose participation is required
        OPT_PARTICIPANT(2,"OPT-PARTICIPANT", R.string.attendee_role_optional_participant, R.drawable.ic_attendee_optparticipant),  //Indicates a participant whose participation is optional
        NON_PARTICIPANT(3,"NON-PARTICIPANT", R.string.attendee_role_non_participant, R.drawable.ic_attendee_nonparticipant);  //Indicates a participant who is copied for information


        companion object {
                fun getRoleparamById(id: Int): String? {
                        values().forEach {
                                if (it.id == id)
                                        return it.param
                        }
                        return null
                }

                fun getDrawableResourceByParam(param: String?): Int {
                        values().forEach {
                                if (it.param == param)
                                        return it.icon
                        }
                        return R.drawable.ic_attendee_reqparticipant  // default icon
                }

                fun getStringResourceByParam(param: String): Int? {
                        Classification.values().forEach {
                                if (it.param == param)
                                        return it.stringResource
                        }
                        return null
                }
        }

}


