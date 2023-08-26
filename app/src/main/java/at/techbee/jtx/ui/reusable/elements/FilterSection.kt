/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.elements

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.list.AnyAllNone

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    icon: ImageVector,
    headline: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    initialAnyAllNone: AnyAllNone? = null,
    showDefaultMenu: Boolean = true,
    customMenu: @Composable () -> Unit = { },
    onResetSelection: () -> Unit,
    onInvertSelection: () -> Unit,
    onAnyAllNoneChanged: (AnyAllNone) -> Unit = { },
    content: @Composable () -> Unit,
    ) {

    var expanded by remember { mutableStateOf(false) }
    var currentAnyAllNone by remember { mutableStateOf(initialAnyAllNone ?: AnyAllNone.values().first()) }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            //modifier = Modifier.weight(1f)
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

            if(initialAnyAllNone != null) {
                FilterChip(
                    selected = false,
                    onClick = {
                        currentAnyAllNone = AnyAllNone.values()[(AnyAllNone.values().indexOf(currentAnyAllNone)+1)%AnyAllNone.values().size]
                        onAnyAllNoneChanged(currentAnyAllNone)
                    },
                    label = { Text(stringResource(id = currentAnyAllNone.stringResource)) }
                )

                /*
                AllAnyNone.values().forEach { allAnyNone ->
                    FilterChip(
                        selected = allAnyNone == currentAllAnyNone,
                        onClick = {
                            currentAllAnyNone = allAnyNone
                            onAllAnyNoneChanged(allAnyNone)
                            expanded = false
                        },
                        label = { Text(currentAllAnyNone.name) }
                    )
                }

                 */
            }

            if(showDefaultMenu) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Outlined.MoreVert, stringResource(id = R.string.more))

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
            onAnyAllNoneChanged = { },
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


@Preview(showBackground = true)
@Composable
fun FilterSection_Preview_Category() {
    MaterialTheme {
        FilterSection(
            icon = Icons.Outlined.Label,
            headline = stringResource(id = R.string.category),
            //subtitle = "Here comes the subtitle",
            initialAnyAllNone = AnyAllNone.ANY,
            onAnyAllNoneChanged = { },
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