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
import at.bitfire.notesx5.database.*
import kotlinx.parcelize.Parcelize


/** The name of the the table for Resources that are linked to an ICalObject.
 * [https://tools.ietf.org/html/rfc5545#section-3.8.1.10]*/
const val TABLE_NAME_RESOURCE = "resource"

/** The name of the ID column for resources.
 * This is the unique identifier of a Resource
 * Type: [Long]*/
const val COLUMN_RESOURCE_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects.
 * Type: [Long] */
const val COLUMN_RESOURCE_ICALOBJECT_ID = "icalObjectId"


/* The names of all the other columns  */
/**
 * Purpose:  This property defines the name of the resource for a calendar component.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.10]
 * Type: [String]
 */
const val COLUMN_RESOURCE_TEXT = "text"
/**
 * Purpose:  To specify the language for text values in a property or property parameter, in this case of the resource.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.10] and [https://tools.ietf.org/html/rfc5545#section-3.2.10]
 * Type: [String]
 */
const val COLUMN_RESOURCE_RELTYPE = "reltype"
/**
 * Purpose:  To specify other properties for the resource.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.10]
 * Type: [String]
 */
const val COLUMN_RESOURCE_OTHER = "other"



@Parcelize
@Entity(tableName = TABLE_NAME_RESOURCE,
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf(COLUMN_ID),
                childColumns = arrayOf(COLUMN_RESOURCE_ICALOBJECT_ID),
                onDelete = ForeignKey.CASCADE)])
data class Resource (

        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_RESOURCE_ID)
        var resourceId: Long = 0L,

        @ColumnInfo(index = true, name = COLUMN_RESOURCE_ICALOBJECT_ID)var icalObjectId: Long = 0L,
        @ColumnInfo(name = COLUMN_RESOURCE_TEXT)            var text: String? = "",
        @ColumnInfo(name = COLUMN_RESOURCE_RELTYPE)    var reltype: String? = null,
        @ColumnInfo(name = COLUMN_RESOURCE_OTHER)      var other: String? = null
): Parcelable


{
        companion object Factory {

                /**
                 * Create a new [Resource] from the specified [ContentValues].
                 *
                 * @param values A [Resource] that at least contain [COLUMN_RESOURCE_TEXT] and [COLUMN_RESOURCE_ICALOBJECT_ID].
                 * @return A newly created [Resource] instance.
                 */
                fun fromContentValues(values: ContentValues?): Resource? {

                        if (values == null)
                                return null

                        if(values.getAsString(COLUMN_RESOURCE_TEXT) == null || values.getAsLong(COLUMN_RESOURCE_ICALOBJECT_ID) == null)
                                return null

                        return Resource().applyContentValues(values)
                }
        }

        fun applyContentValues(values: ContentValues): Resource {

                values.getAsLong(COLUMN_RESOURCE_ICALOBJECT_ID)?.let { icalObjectId -> this.icalObjectId = icalObjectId }
                values.getAsString(COLUMN_RESOURCE_TEXT)?.let { text -> this.text = text }
                values.getAsString(COLUMN_RESOURCE_RELTYPE)?.let { reltype -> this.reltype = reltype }
                values.getAsString(COLUMN_RESOURCE_OTHER)?.let { other -> this.other = other }

                return this
        }
}




