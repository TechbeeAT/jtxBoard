/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import at.techbee.jtx.R
import at.techbee.jtx.database.Module

sealed class ListTabDestination (
    val module: Module,
    val titleResource: Int,
    //val icon: ImageVector,
    //val badgeCount: Int?
) {
    data object Journals: ListTabDestination(
        module = Module.JOURNAL,
        titleResource = R.string.list_tabitem_journals
    )
    data object Notes: ListTabDestination(
        module = Module.NOTE,
        titleResource = R.string.list_tabitem_notes
    )
    data object Tasks: ListTabDestination(
        module = Module.TODO,
        titleResource = R.string.list_tabitem_todos
    )
}
