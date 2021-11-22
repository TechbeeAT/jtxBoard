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
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import at.techbee.jtx.database.COLUMN_ID
import at.techbee.jtx.database.ICalObject
import kotlinx.parcelize.Parcelize


/** The name of the the table for Unknown properties that are linked to an ICalObject.
 * [https://tools.ietf.org/html/rfc5545#section-3.8.1.10]*/
const val TABLE_NAME_UNKNOWN = "unknown"

/** The name of the ID column for resources.
 * This is the unique identifier of a Resource
 * Type: [Long]*/
const val COLUMN_UNKNOWN_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects.
 * Type: [Long] */
const val COLUMN_UNKNOWN_ICALOBJECT_ID = "icalObjectId"


/* The names of all the other columns  */
/**
 * Purpose:  This property stores the unknown value as json
 * Type: [String]
 */
const val COLUMN_UNKNOWN_VALUE = "value"


@Parcelize
@Entity(tableName = TABLE_NAME_UNKNOWN,
    foreignKeys = [ForeignKey(entity = ICalObject::class,
        parentColumns = arrayOf(COLUMN_ID),
        childColumns = arrayOf(COLUMN_UNKNOWN_ICALOBJECT_ID),
        onDelete = ForeignKey.CASCADE)])
data class Unknown (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_UNKNOWN_ID)
    var unknownId: Long = 0L,

    @ColumnInfo(index = true, name = COLUMN_UNKNOWN_ICALOBJECT_ID)var icalObjectId: Long = 0L,
    @ColumnInfo(name = COLUMN_UNKNOWN_VALUE)            var value: String? = null
): Parcelable


{
    companion object Factory {

        /**
         * Create a new [Unknown] Property from the specified [ContentValues].
         *
         * @param values A [Unknown] that at least contain [COLUMN_UNKNOWN_VALUE].
         * @return A newly created [Unknown] instance.
         */
        fun fromContentValues(values: ContentValues?): Unknown? {

            if (values == null)
                return null

            if(values.getAsString(COLUMN_UNKNOWN_VALUE) == null || values.getAsLong(COLUMN_UNKNOWN_ICALOBJECT_ID) == null)
                return null

            return Unknown().applyContentValues(values)
        }
    }

    fun applyContentValues(values: ContentValues): Unknown {

        values.getAsLong(COLUMN_UNKNOWN_ICALOBJECT_ID)?.let { icalObjectId -> this.icalObjectId = icalObjectId }
        values.getAsString(COLUMN_UNKNOWN_VALUE)?.let { value -> this.value = value }
        return this
    }
}
