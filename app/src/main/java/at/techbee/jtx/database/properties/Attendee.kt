/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.Nullable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.core.util.PatternsCompat
import androidx.room.*
import at.techbee.jtx.R
import at.techbee.jtx.database.COLUMN_ID
import at.techbee.jtx.database.ICalObject
import kotlinx.parcelize.Parcelize


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
        @ColumnInfo(name = COLUMN_ATTENDEE_CALADDRESS)     var caladdress: String = "",
        @ColumnInfo(name = COLUMN_ATTENDEE_CUTYPE)         var cutype: String? = Cutype.INDIVIDUAL.name,
        @ColumnInfo(name = COLUMN_ATTENDEE_MEMBER)         var member: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_ROLE)           var role: String? = Role.`REQ-PARTICIPANT`.name,
        @ColumnInfo(name = COLUMN_ATTENDEE_PARTSTAT)       var partstat: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_RSVP)           var rsvp: Boolean? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_DELEGATEDTO)    var delegatedto: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_DELEGATEDFROM)  var delegatedfrom: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_SENTBY)         var sentby: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_CN)             var cn: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_DIR)            var dir: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_LANGUAGE)       var language: String? = null,
        @ColumnInfo(name = COLUMN_ATTENDEE_OTHER)          var other: String? = null

): Parcelable {
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

                /**
                 * @param [string] to be parsed
                 * @return a new Attendee with the caladdress and (if available) the CN or null if the email-address was not a valid email
                 * The function allows a string as e.g. "jtx@techbee.at <JTX Board>" -> the email-address is extracted and the name is extracted from within the < >
                 * The function also allows email-address only, e.g. "jtx@techbee.at"
                 */
                fun fromString(string: String?): Attendee? {
                        if(string.isNullOrEmpty())
                                return null

                        val attendee = Attendee()
                        PatternsCompat.EMAIL_ADDRESS.toRegex().find(string)?.let { matchResult ->
                                attendee.caladdress = "mailto:${matchResult.value}"
                                string.substringBefore(matchResult.value).let { name ->
                                        val cleanName = name.substringBefore("<").trim()
                                        if(cleanName.isNotBlank())
                                                attendee.cn = name.substringBefore("<").trim()
                                }
                        }
                        return if(attendee.caladdress.isNotEmpty())
                                attendee
                        else null
                }
        }


        fun applyContentValues(values: ContentValues): Attendee {

                values.getAsLong(COLUMN_ATTENDEE_ICALOBJECT_ID)?.let { icalObjectId -> this.icalObjectId = icalObjectId }
                values.getAsString(COLUMN_ATTENDEE_CALADDRESS)?.let { caladdress -> this.caladdress = caladdress }
                values.getAsString(COLUMN_ATTENDEE_CUTYPE)?.let { cutype -> this.cutype = cutype }
                values.getAsString(COLUMN_ATTENDEE_MEMBER)?.let { member -> this.member = member }
                values.getAsString(COLUMN_ATTENDEE_ROLE)?.let { role -> this.role = role }
                values.getAsString(COLUMN_ATTENDEE_PARTSTAT)?.let { partstat -> this.partstat = partstat }
                values.getAsString(COLUMN_ATTENDEE_RSVP)?.let { rsvp -> this.rsvp = rsvp == "1" }
                values.getAsString(COLUMN_ATTENDEE_DELEGATEDTO)?.let { delegatedto -> this.delegatedto = delegatedto }
                values.getAsString(COLUMN_ATTENDEE_DELEGATEDFROM)?.let { delegatedfrom -> this.delegatedfrom = delegatedfrom }
                values.getAsString(COLUMN_ATTENDEE_SENTBY)?.let { sentby -> this.sentby = sentby }
                values.getAsString(COLUMN_ATTENDEE_CN)?.let { cn -> this.cn = cn }
                values.getAsString(COLUMN_ATTENDEE_DIR)?.let { dir -> this.dir = dir }
                values.getAsString(COLUMN_ATTENDEE_LANGUAGE)?.let { language -> this.language = language }
                values.getAsString(COLUMN_ATTENDEE_OTHER)?.let { other -> this.other = other }

                return this

        }

        /**
         * Returns a string to display the attendee with its CN and Caladdress
         * e.g. John Doe <john@doe.com>  if cn and caladdress are present
         * e.g. john@doe.com  if only caladdress is present
         * null if caladdress is empty
         */
        fun getDisplayString() = when {
                        caladdress.removePrefix("mailto:").isNotEmpty() && cn?.isNotBlank() == true -> "$cn <${caladdress.removePrefix("mailto:")}>"
                        caladdress.removePrefix("mailto:").isNotEmpty() -> caladdress.removePrefix("mailto:")
                        cn?.isNotEmpty() == true -> cn ?: ""
                        else -> ""
        }
}

/** This enum class defines the possible values for the attribute [Attendee.cutype]  */
@Suppress("unused")
enum class Cutype  {
        INDIVIDUAL, GROUP, RESOURCE, ROOM, UNKNOWN
}

/** This enum class defines the possible values for the attribute [Attendee.role]
 * @param [stringResource] is a reference to the String Resource within JTX
 * @param [icon] is a reference to the Drawable Resource within JTX

 */
enum class Role (val stringResource: Int, val icon: ImageVector) {
        CHAIR (R.string.attendee_role_chair, Icons.Default.CoPresent),            //Indicates chair of the calendar entity
        `REQ-PARTICIPANT`(R.string.attendee_role_required_participant, Icons.Default.Person),  //Indicates a participant whose participation is required
        `OPT-PARTICIPANT`(R.string.attendee_role_optional_participant, Icons.Outlined.Person),  //Indicates a participant whose participation is optional
        `NON-PARTICIPANT`(R.string.attendee_role_non_participant, Icons.Default.Info);  //Indicates a participant who is copied for information

        @Composable
        fun Icon() {
                Icon(icon, stringResource(id = stringResource))
        }
}

/** This enum class defines the possible values for the attribute [Attendee.partstat] for the Component VJOURNAL  */
@Suppress("unused")
enum class PartstatJournal  {
        `NEEDS-ACTION`, ACCEPTED, DECLINED
}
/** This enum class defines the possible values for the attribute [Attendee.partstat] for the Component VTODO  */
@Suppress("unused")
enum class PartstatTodo  {
        `NEEDS-ACTION`, ACCEPTED, DECLINED, TENTATIVE, DELEGATED, COMPLETED, `IN-PROCESS`
}