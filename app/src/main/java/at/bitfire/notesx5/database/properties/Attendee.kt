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


/** The name of the the table for Attendees that are linked to an ICalObject.
 *  [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] */
const val TABLE_NAME_ATTENDEE = "attendee"

/** The name of the ID column.
 * This is the unique identifier of an Attendee
 * Type: [Long] */
const val COLUMN_ATTENDEE_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects.
 * Type: [Long] */
const val COLUMN_ATTENDEE_ICALOBJECT_ID = "icalObjectId"


/* The names of all the other columns  */

/**
 * Purpose:  This value type is used to identify properties that contain a calendar user address (in this case of the attendee).
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] and [https://tools.ietf.org/html/rfc5545#section-3.3.3]
 * Type: [String]
 */
const val COLUMN_ATTENDEE_CALADDRESS = "caladdress"
/**
 * Purpose:  To identify the type of calendar user specified by the property in this case for the attendee.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] and [https://tools.ietf.org/html/rfc5545#section-3.2.3]
 * Type: [String]
 */
const val COLUMN_ATTENDEE_CUTYPE = "cutype"
/**
 * Purpose:  To specify the group or list membership of the calendar user specified by the property in this case for the attendee.
 * The possible values are defined in the enum [Cutype]
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] and [https://tools.ietf.org/html/rfc5545#section-3.2.11]
 * Type: [String]
 */
const val COLUMN_ATTENDEE_MEMBER = "member"
/**
 * Purpose:  To specify the participation role for the calendar user specified by the property in this case for the attendee.
 * The possible values are defined in the enum [Role]
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] and [https://tools.ietf.org/html/rfc5545#section-3.2.16]
 * Type: [String]
 */
const val COLUMN_ATTENDEE_ROLE = "role"
/**
 * Purpose:  To specify the participation status for the calendar user specified by the property in this case for the attendee.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] and [https://tools.ietf.org/html/rfc5545#section-3.2.12]
 * Type: [String]
 */
const val COLUMN_ATTENDEE_PARTSTAT = "partstat"
/**
 * Purpose:  To specify whether there is an expectation of a favor of a reply from the calendar user specified by the property value
 * in this case for the attendee.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] and [https://tools.ietf.org/html/rfc5545#section-3.2.17]
 * Type: [Boolean]
 */
const val COLUMN_ATTENDEE_RSVP = "rsvp"
/**
 * Purpose:  To specify the calendar users to whom the calendar user specified by the property
 * has delegated participation in this case for the attendee.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] and [https://tools.ietf.org/html/rfc5545#section-3.2.5]
 * Type: [String]
 */
const val COLUMN_ATTENDEE_DELEGATEDTO = "delegatedto"
/**
 * Purpose:  To specify the calendar users that have delegated their participation to the calendar user specified by the property
 * in this case for the attendee.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] and [https://tools.ietf.org/html/rfc5545#section-3.2.4]
 * Type: [String]
 */
const val COLUMN_ATTENDEE_DELEGATEDFROM = "delegatedfrom"
/**
 * Purpose:  To specify the calendar user that is acting on behalf of the calendar user specified by the property in this case for the attendee.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] and [https://tools.ietf.org/html/rfc5545#section-3.2.18]
 * Type: [String]
 */
const val COLUMN_ATTENDEE_SENTBY = "sentby"
/**
 * Purpose:  To specify the common name to be associated with the calendar user specified by the property in this case for the attendee.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] and [https://tools.ietf.org/html/rfc5545#section-3.2.18]
 * Type: [String]
 */
const val COLUMN_ATTENDEE_CN = "cn"
/**
 * Purpose:  To specify reference to a directory entry associated with the calendar user specified by the property in this case for the attendee.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] and [https://tools.ietf.org/html/rfc5545#section-3.2.2]
 * Type: [String]
 */
const val COLUMN_ATTENDEE_DIR = "dir"
/**
 * Purpose:  To specify the language for text values in a property or property parameter, in this case of the attendee.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1] and [https://tools.ietf.org/html/rfc5545#section-3.2.10]
 * Type: [String]
 */
const val COLUMN_ATTENDEE_LANGUAGE = "language"
/**
 * Purpose:  To specify other properties for the attendee.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1]
 * Type: [String]
 */
const val COLUMN_ATTENDEE_OTHER = "other"


/**
 * Purpose:  This property / data class defines an "Attendee" within a calendar component.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.1]
 */
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
        @ColumnInfo(name = COLUMN_ATTENDEE_CUTYPE)         var cutype: String? = Cutype.INDIVIDUAL.param,
        @ColumnInfo(name = COLUMN_ATTENDEE_MEMBER)         var member: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_ROLE)           var role: String? = Role.REQ_PARTICIPANT.param,
        @ColumnInfo(name = COLUMN_ATTENDEE_PARTSTAT)       var partstat: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_RSVP)           var rsvp: Boolean? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_DELEGATEDTO)    var delegatedto: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_DELEGATEDFROM)  var delegatedfrom: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_SENTBY)         var sentby: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_CN)             var cn: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_DIR)            var dir: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_LANGUAGE)       var language: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_OTHER)          var other: String? = null

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



                if (values?.containsKey(COLUMN_ATTENDEE_CUTYPE) == true && values.getAsString(COLUMN_ATTENDEE_CUTYPE).isNotBlank()) {
                        this.cutype = values.getAsString(COLUMN_ATTENDEE_CUTYPE)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_MEMBER) == true && values.getAsString(COLUMN_ATTENDEE_MEMBER).isNotBlank()) {
                        this.member = values.getAsString(COLUMN_ATTENDEE_MEMBER)
                }

                if (values?.containsKey(COLUMN_ATTENDEE_ROLE) == true && values.getAsString(COLUMN_ATTENDEE_ROLE).isNotBlank()) {
                        this.role = values.getAsString(COLUMN_ATTENDEE_ROLE)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_PARTSTAT) == true && values.getAsString(COLUMN_ATTENDEE_PARTSTAT).isNotBlank()) {
                        this.partstat = values.getAsString(COLUMN_ATTENDEE_PARTSTAT)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_RSVP) == true && values.getAsBoolean(COLUMN_ATTENDEE_RSVP) != null) {
                        this.rsvp = values.getAsBoolean(COLUMN_ATTENDEE_RSVP)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_DELEGATEDTO) == true && values.getAsString(COLUMN_ATTENDEE_DELEGATEDTO).isNotBlank()) {
                        this.delegatedto = values.getAsString(COLUMN_ATTENDEE_DELEGATEDTO)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_DELEGATEDFROM) == true && values.getAsString(COLUMN_ATTENDEE_DELEGATEDFROM).isNotBlank()) {
                        this.delegatedfrom = values.getAsString(COLUMN_ATTENDEE_DELEGATEDFROM)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_SENTBY) == true && values.getAsString(COLUMN_ATTENDEE_SENTBY).isNotBlank()) {
                        this.sentby = values.getAsString(COLUMN_ATTENDEE_SENTBY)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_CN) == true && values.getAsString(COLUMN_ATTENDEE_CN).isNotBlank()) {
                        this.cn = values.getAsString(COLUMN_ATTENDEE_CN)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_DIR) == true && values.getAsString(COLUMN_ATTENDEE_DIR).isNotBlank()) {
                        this.dir = values.getAsString(COLUMN_ATTENDEE_DIR)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_LANGUAGE) == true && values.getAsString(COLUMN_ATTENDEE_LANGUAGE).isNotBlank()) {
                        this.language = values.getAsString(COLUMN_ATTENDEE_LANGUAGE)
                }
                if (values?.containsKey(COLUMN_ATTENDEE_OTHER) == true && values.getAsString(COLUMN_ATTENDEE_OTHER).isNotBlank()) {
                        this.other = values.getAsString(COLUMN_ATTENDEE_OTHER)
                }

                return this

        }

}

/** This enum class defines the possible values for the attribute [Attendee.cutype]  */
enum class Cutype (val id: Int, val param: String?) {

        INDIVIDUAL(0,"INDIVIDUAL"),
        GROUP(1,"GROUP"),
        RESOURCE(2,"RESOURCE"),
        ROOM(3,"ROOM"),
        UNKNOWN(4,"UNKNOWN")
}

/** This enum class defines the possible values for the attribute [Attendee.role]
 * @param [id] is an ID of the entry
 * @param [param] defines the [Role] how it is stored in the database, this also corresponds to the value that is used for the ICal format
 * @param [stringResource] is a reference to the String Resource within NotesX5
 * @param [icon] is a reference to the Drawable Resource within NotesX5

 */
enum class Role (val id: Int, val param: String?, val stringResource: Int, val icon: Int) {
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
        }

}


