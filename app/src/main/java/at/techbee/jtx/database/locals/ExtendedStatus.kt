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
import at.techbee.jtx.database.Status
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


/** The name of the the table for statuses that are stored only locally. */
const val TABLE_NAME_EXTENDED_STATUS = "extended_status"
const val COLUMN_EXTENDED_STATUS_NAME = "xstatus"
const val COLUMN_EXTENDED_STATUS_RFCSTATUS = "rfcstatus"
const val COLUMN_EXTENDED_STATUS_COLOR = "color"
const val COLUMN_EXTENDED_STATUS_MODULE = "module"


@Serializable
@Parcelize
@Entity(tableName = TABLE_NAME_EXTENDED_STATUS, primaryKeys = [COLUMN_EXTENDED_STATUS_NAME, COLUMN_EXTENDED_STATUS_MODULE])
data class ExtendedStatus (

    @ColumnInfo(index = true, name = COLUMN_EXTENDED_STATUS_NAME)    var xstatus: String,
    @ColumnInfo(name = COLUMN_EXTENDED_STATUS_MODULE)                var module: Module,
    @ColumnInfo(name = COLUMN_EXTENDED_STATUS_RFCSTATUS)             var rfcStatus: Status,
    @ColumnInfo(name = COLUMN_EXTENDED_STATUS_COLOR)                 var color: Int?
): Parcelable{

    companion object {
        fun getColorForStatus(status: String?, extendedStatuses: List<ExtendedStatus>, module: String?): Color? {
            return extendedStatuses.find { it.xstatus == status && it.module.name == module }?.color?.let { Color(it) }
        }
    }
}
