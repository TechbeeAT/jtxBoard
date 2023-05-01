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
import at.techbee.jtx.database.Module
import kotlinx.parcelize.Parcelize


/** The name of the the table for statuses that are stored only locally. */
const val TABLE_NAME_STORED_STATUS = "stored_status"
const val COLUMN_STORED_STATUS_NAME = "status"
const val COLUMN_STORED_STATUS_COLOR = "color"
const val COLUMN_STORED_STATUS_MODULE = "module"


@Parcelize
@Entity(tableName = TABLE_NAME_STORED_STATUS, primaryKeys = [COLUMN_STORED_STATUS_NAME, COLUMN_STORED_STATUS_MODULE])
data class StoredStatus (

    @ColumnInfo(index = true, name = COLUMN_STORED_STATUS_NAME)    var status: String,
    @ColumnInfo(name = COLUMN_STORED_STATUS_MODULE)                var module: String,
    @ColumnInfo(name = COLUMN_STORED_STATUS_COLOR)                 var color: Int?
): Parcelable{

    companion object {
        fun getColorForStatus(status: String?, storedStatuses: List<StoredStatus>, module: Module): Color? {
            return storedStatuses.find { it.status == status && it.module == module.name }?.color?.let { Color(it) }
        }
    }
}
