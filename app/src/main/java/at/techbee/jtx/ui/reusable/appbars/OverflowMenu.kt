package at.techbee.jtx.ui.reusable.appbars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import at.techbee.jtx.R

@Composable
fun OverflowMenu(
    menuExpanded: MutableState<Boolean>,
    dropdownMenuItems: @Composable () -> Unit
) {

    DropdownMenu(
        expanded = menuExpanded.value,
        onDismissRequest = { menuExpanded.value = false }
    ) {
        dropdownMenuItems()
    }
    IconButton(onClick = { menuExpanded.value = true }) {
        Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(id = R.string.more))
    }
}
