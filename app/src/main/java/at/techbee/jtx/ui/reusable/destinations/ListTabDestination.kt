package at.techbee.jtx.ui.reusable.destinations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import at.techbee.jtx.R
import at.techbee.jtx.database.Module

sealed class ListTabDestination (
    val tabIndex: Int,
    val module: Module,
    val titleResource: Int,
    val icon: ImageVector,
    //val badgeCount: Int?
) {
    object Journals: ListTabDestination(
        tabIndex = 0,
        module = Module.JOURNAL,
        titleResource = R.string.list_tabitem_journals,
        icon = Icons.Outlined.Info,
    )
    object Notes: ListTabDestination(
        tabIndex = 1,
        module = Module.NOTE,
        titleResource = R.string.list_tabitem_notes,
        icon = Icons.Outlined.NewReleases,
    )
    object Tasks: ListTabDestination(
        tabIndex = 2,
        module = Module.TODO,
        titleResource = R.string.list_tabitem_todos,
        icon = Icons.Outlined.DataObject,
    )
}
