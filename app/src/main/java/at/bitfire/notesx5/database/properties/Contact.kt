/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.database.properties


import android.content.ContentValues
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.*
import at.bitfire.notesx5.database.COLUMN_ID
import at.bitfire.notesx5.database.ICalObject
import kotlinx.parcelize.Parcelize

/** The name of the the table for Contact that are linked to an ICalObject.
 * [https://tools.ietf.org/html/rfc5545#section-3.8.4.2]
 */
const val TABLE_NAME_CONTACT = "contact"

/** The name of the ID column for the contact.
 * This is the unique identifier of a Contact
 * Type: [Long]*/
const val COLUMN_CONTACT_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects.
 * Type: [Long] */
const val COLUMN_CONTACT_ICALOBJECT_ID = "icalObjectId"


/* The names of all the other columns  */
/**
 * Purpose:  This property defines the name of the contact for a calendar component.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.2]
 * Type: [String]
 */
const val COLUMN_CONTACT_TEXT = "text"
/**
 * Purpose:  To specify an alternate text representation for the property value, in this case of the comment.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.4]
 * Type: [String]
 */
const val COLUMN_CONTACT_ALTREP = "altrep"
/**
 * Purpose:  To specify the language for text values in a property or property parameter, in this case of the contact.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.2] and [https://tools.ietf.org/html/rfc5545#section-3.2.10]
 * Type: [String]
 */
const val COLUMN_CONTACT_LANGUAGE = "language"
/**
 * Purpose:  To specify other properties for the contact.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.2]
 * Type: [String]
 */
const val COLUMN_CONTACT_OTHER = "other"



@Parcelize
@Entity(tableName = TABLE_NAME_CONTACT,
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf(COLUMN_ID),
                childColumns = arrayOf(COLUMN_CONTACT_ICALOBJECT_ID),
                onDelete = ForeignKey.CASCADE)])
data class Contact (

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_CONTACT_ID)
        var contactId: Long = 0L,

        @ColumnInfo(index = true, name = COLUMN_CONTACT_ICALOBJECT_ID) var icalObjectId: Long = 0L,
        @ColumnInfo(name = COLUMN_CONTACT_TEXT)                        var text: String = "",
        @ColumnInfo(name = COLUMN_CONTACT_ALTREP)                      var altrep: String? = null,
        @ColumnInfo(name = COLUMN_CONTACT_LANGUAGE)                    var language: String? = null,
        @ColumnInfo(name = COLUMN_CONTACT_OTHER)                       var other: String? = null

): Parcelable


{
        companion object Factory {

                /**
                 * Create a new [Contact] from the specified [ContentValues].
                 *
                 * @param values A [Contact] that at least contain [COLUMN_CONTACT_TEXT] and [COLUMN_CONTACT_ICALOBJECT_ID]
                 * @return A newly created [Contact] instance.
                 */
                fun fromContentValues(values: ContentValues?): Contact? {

                        if (values == null)
                                return null

                        if (values.getAsLong(COLUMN_CONTACT_ICALOBJECT_ID) == null || values.getAsString(COLUMN_CONTACT_TEXT) == null)     // at least a icalObjectId and text must be given for a Contact!
                                return null

                        return Contact().applyContentValues(values)
                }
        }

        fun applyContentValues(values: ContentValues): Contact {

                values.getAsLong(COLUMN_CONTACT_ICALOBJECT_ID)?.let { icalObjectId -> this.icalObjectId = icalObjectId }
                values.getAsString(COLUMN_CONTACT_TEXT)?.let { text -> this.text = text }
                values.getAsString(COLUMN_CONTACT_LANGUAGE)?.let { language -> this.language = language }
                values.getAsString(COLUMN_CONTACT_OTHER)?.let { other -> this.other = other }

                return this
        }


        fun getICalString(): String {

                var content = "CONTACT"
                if (altrep?.isNotEmpty() == true)
                        content += ";ALTREP=\"$altrep\""
                if (language?.isNotEmpty() == true)
                        content += ";LANGUAGE=$language"
                content += ":$text\r\n"

                return content
        }
}



