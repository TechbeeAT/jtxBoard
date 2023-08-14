/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.elements

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSection(
    icon: ImageVector,
    headline: String,
    onResetSelection: () -> Unit,
    onInvertSelection: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showDefaultMenu: Boolean = true,
    customMenu: @Composable () -> Unit = { },
    content: @Composable () -> Unit,
    ) {

    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(modifier = Modifier.weight(1f)) {
                HeadlineWithIcon(
                    icon = icon,
                    iconDesc = headline,
                    text = headline
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            if(showDefaultMenu) {
                Row {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Outlined.MoreVert, stringResource(id = R.string.more))
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Outlined.SwapHoriz, null) },
                            text = { Text(stringResource(id = R.string.invert_selection)) },
                            onClick = {
                                onInvertSelection()
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Outlined.DeleteSweep, null) },
                            text = { Text(stringResource(id = R.string.clear_selection)) },
                            onClick = {
                                onResetSelection()
                                expanded = false
                            }
                        )
                    }
                }
            } else {
                customMenu()
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterSection_Preview() {
    MaterialTheme {
        FilterSection(
            icon = Icons.Outlined.Folder,
            headline = stringResource(id = R.string.collection),
            subtitle = "Here comes the subtitle",
            onResetSelection = {  },
            onInvertSelection = {  },
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text("Here comes the content")
                } },
        )
    }
}