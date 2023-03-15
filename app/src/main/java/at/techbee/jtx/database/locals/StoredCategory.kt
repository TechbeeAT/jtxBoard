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


/** The name of the the table for categories that are stored only locally. */
const val TABLE_NAME_STORED_CATEGORIES = "stored_categories"
const val COLUMN_STORED_CATEGORY_NAME = "category"
const val COLUMN_STORED_CATEGORY_COLOR = "color"


@Parcelize
@Entity(tableName = TABLE_NAME_STORED_CATEGORIES)
data class StoredCategory (

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(index = true, name = COLUMN_STORED_CATEGORY_NAME)
    var category: String,

    @ColumnInfo(name = COLUMN_STORED_CATEGORY_COLOR) var color: Int?
): Parcelable {

    companion object {
        fun getColorForCategory(category: String, storedCategories: List<StoredCategory>): Color? {
            return storedCategories.find { it.category == category }?.color?.let { Color(it) }
        }
    }
}
