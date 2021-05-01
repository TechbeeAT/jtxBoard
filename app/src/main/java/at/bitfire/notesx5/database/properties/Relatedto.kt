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


/** The name of the the table for Relationships (related-to) that are linked to an ICalObject.
 * [https://tools.ietf.org/html/rfc5545#section-3.8.4.5]
 */
const val TABLE_NAME_RELATEDTO = "relatedto"

/** The name of the ID column for the related-to.
 * This is the unique identifier of a Related-to
 * Type: [Long]*/
const val COLUMN_RELATEDTO_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects.
 * Type: [Long] */
const val COLUMN_RELATEDTO_ICALOBJECT_ID = "icalObjectId"

/** The name of the second Foreign Key Column of the related IcalObject
 * Type: [Long]
 */
const val COLUMN_RELATEDTO_LINKEDICALOBJECT_ID = "linkedICalObjectId"


/* The names of all the other columns  */
/**
 * Purpose:  This property is used to represent a relationship or reference between one calendar component and another.
 * The text gives the UID of the related calendar entry.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.5]
 * Type: [String]
 */
const val COLUMN_RELATEDTO_TEXT = "text"
/**
 * Purpose:  To specify the type of hierarchical relationship associated
 * with the calendar component specified by the property.
 * The possible relationship types are defined in the enum [Reltype]
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.5] and [https://tools.ietf.org/html/rfc5545#section-3.2.15]
 * Type: [String]
 */
const val COLUMN_RELATEDTO_RELTYPE = "reltype"
/**
 * Purpose:  To specify other properties for the related-to.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.5]
 * Type: [String]
 */
const val COLUMN_RELATEDTO_OTHER = "other"




@Parcelize
@Entity(tableName = TABLE_NAME_RELATEDTO,
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf(COLUMN_ID),
                childColumns = arrayOf(COLUMN_RELATEDTO_ICALOBJECT_ID),
                onDelete = ForeignKey.CASCADE)],
                indices = [Index(value = [COLUMN_RELATEDTO_ICALOBJECT_ID, COLUMN_RELATEDTO_LINKEDICALOBJECT_ID, COLUMN_RELATEDTO_RELTYPE], unique = true)])
data class Relatedto (

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_RELATEDTO_ID)
        var relatedtoId: Long = 0L,

        @ColumnInfo(index = true, name = COLUMN_RELATEDTO_ICALOBJECT_ID)    var icalObjectId: Long = 0L,
        @ColumnInfo(index = true, name = COLUMN_RELATEDTO_LINKEDICALOBJECT_ID) var linkedICalObjectId: Long = 0L,
        @ColumnInfo(name = COLUMN_RELATEDTO_TEXT)                var text: String? = null,
        @ColumnInfo(name = COLUMN_RELATEDTO_RELTYPE)        var reltype: String? = null,
        @ColumnInfo(name = COLUMN_RELATEDTO_OTHER)          var other: String? = null
): Parcelable {
        companion object Factory {

                /**
                 * Create a new [Relatedto] from the specified [ContentValues].
                 *
                 * @param values A [Relatedto] that at least contain [COLUMN_RELATEDTO_ICALOBJECT_ID] and [COLUMN_RELATEDTO_LINKEDICALOBJECT_ID].
                 * @return A newly created [Relatedto] instance.
                 */
                fun fromContentValues(values: ContentValues?): Relatedto? {

                        if (values == null)
                                return null

                        if (values.getAsLong(COLUMN_RELATEDTO_ICALOBJECT_ID) == null || values.getAsLong(COLUMN_RELATEDTO_LINKEDICALOBJECT_ID) == null)     // at least the the two linking entries must be set!
                                return null

                        return Relatedto().applyContentValues(values)
                }
        }

        fun applyContentValues(values: ContentValues): Relatedto {

                values.getAsLong(COLUMN_RELATEDTO_ICALOBJECT_ID)?.let { icalObjectId -> this.icalObjectId = icalObjectId }
                values.getAsLong(COLUMN_RELATEDTO_LINKEDICALOBJECT_ID)?.let { linkedICalObjectId -> this.linkedICalObjectId = linkedICalObjectId }
                values.getAsString(COLUMN_RELATEDTO_TEXT)?.let { text -> this.text = text }
                values.getAsString(COLUMN_RELATEDTO_RELTYPE)?.let { reltype -> this.reltype = reltype }
                values.getAsString(COLUMN_RELATEDTO_OTHER)?.let { other -> this.other = other }

                return this
        }


        fun getICalString(): String {

                var content = "RELATED-TO"
                if (reltype?.isNotEmpty() == true)
                        content += "RELTYPE=$reltype;"
                content += ":$text\n"

                return content
        }
}

/** This enum class defines the possible values for the attribute [Relatedto.reltype]  */
enum class Reltype {
        PARENT, CHILD, SIBLING
}



