/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.locals

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


/** The name of the the table for resources that are stored only locally. */
const val TABLE_NAME_STORED_RESOURCES = "stored_resources"
const val COLUMN_STORED_RESOURCE_NAME = "resource"
const val COLUMN_STORED_RESOURCE_COLOR = "color"


@Parcelize
@Entity(tableName = TABLE_NAME_STORED_RESOURCES)
data class StoredResource (

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(index = true, name = COLUMN_STORED_RESOURCE_NAME)
    var resource: String,

    @ColumnInfo(name = COLUMN_STORED_RESOURCE_COLOR) var color: Int?
): Parcelable{

    companion object {
        fun getColorForResource(resource: String?, storedResources: List<StoredResource>): Color? {
            return storedResources.find { it.resource == resource }?.color?.let { Color(it) }
        }
    }
}
