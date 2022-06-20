package at.techbee.jtx.ui.compose.appbars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.ui.compose.elements.LabelledCheckbox
import at.techbee.jtx.ui.theme.JtxBoardTheme

@Composable
fun ListBottomAppBar() {

    var menuExpanded by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
    ) {
        DropdownMenuItem(
            text = { LabelledCheckbox(text = stringResource(id = R.string.menu_list_todo_hide_completed), isChecked = true, onCheckedChanged = { /* TODO */ } )},
            onClick = {
                { /* TODO */ }
                menuExpanded = false
            }
        )
    }

    BottomAppBar(
        icons = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(id = R.string.more))
            }
            IconButton(onClick = { /* doSomething() */ }) {
                Icon(Icons.Outlined.FilterList, contentDescription = stringResource(id = R.string.filter))
            }
            IconButton(onClick = { /* doSomething() */ }) {
                Icon(painterResource(id = R.drawable.ic_add_quick), contentDescription = stringResource(id = R.string.menu_list_quick_journal))
            }
        },
        floatingActionButton = {
            // TODO(b/228588827): Replace with Secondary FAB when available.
            FloatingActionButton(
                onClick = { /* do something */ },
                elevation = BottomAppBarDefaults.floatingActionButtonElevation()
            ) {
                Icon(Icons.Filled.Add, "Localized description")
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun ListBottomAppBar_Preview() {
    JtxBoardTheme {
            ListBottomAppBar()
        }
}
