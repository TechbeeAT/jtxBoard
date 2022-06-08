/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionCard(
    collection: CollectionsView,
    modifier: Modifier = Modifier
) {

    var menuExpanded by remember { mutableStateOf(false) }


    ElevatedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {

        Box {

            ColoredEdge(null, collection.color)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        collection.displayName ?:"",
                        style = Typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if(collection.description?.isNotBlank() == true) {
                        Text(
                            collection.description ?: "",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {

                        val notAvailable = stringResource(id = R.string.not_available_abbreviation)
                        val numJournals = if(collection.supportsVJOURNAL) collection.numJournals?.toString()?:"0" else notAvailable
                        val numNotes = if(collection.supportsVJOURNAL) collection.numNotes?.toString()?:"0" else notAvailable
                        val numTodos = if(collection.supportsVTODO) collection.numTodos?.toString()?:"0" else notAvailable

                        Text(
                            stringResource(id = R.string.collections_journals_num, numJournals),
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            stringResource(id = R.string.collections_notes_num, numNotes),
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Text(
                            stringResource(id = R.string.collections_tasks_num, numTodos),
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        stringResource(R.string.collections_collection_menu)
                    )
                }

            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { stringResource(id = R.string.delete) },
                    onClick = { /*TODO*/ }
                )
                DropdownMenuItem(
                    text = { stringResource(id = R.string.menu_collection_popup_show_in_davx5) },
                    onClick = { /*TODO*/ }
                )
                DropdownMenuItem(
                    text = { stringResource(id = R.string.menu_collection_popup_export_as_ics) },
                    onClick = { /*TODO*/ }
                )
                DropdownMenuItem(
                    text = { stringResource(id = R.string.menu_collection_popup_import_from_ics) },
                    onClick = { /*TODO*/ }
                )
                DropdownMenuItem(
                    text = { stringResource(id = R.string.menu_collections_popup_move_entries) },
                    onClick = { /*TODO*/ }
                )

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionCardPreview() {
    JtxBoardTheme {

        val collection = CollectionsView().apply {
            this.displayName = "My collection name"
            this.description = "My collection desc\nription"
            this.color = Color.CYAN
            this.numJournals = 24
            this.numNotes = 33
            this.numTodos = 1
            this.supportsVJOURNAL = true
            this.supportsVTODO = true
        }

        CollectionCard(collection)
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionCardPreview2() {
    JtxBoardTheme {

        val collection = CollectionsView().apply {
            this.displayName = "My collection name"
            this.description = "My collection description"
            this.color = Color.CYAN
            this.numJournals = 0
            this.numNotes = 0
            this.numTodos = 0
            this.supportsVJOURNAL = false
            this.supportsVTODO = false
        }

        CollectionCard(collection)
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionCardPreview3() {
    JtxBoardTheme {

        val collection = CollectionsView().apply {
            this.displayName = "My collection name"
            this.color = Color.MAGENTA
            this.supportsVJOURNAL = true
            this.supportsVTODO = true
        }

        CollectionCard(collection)
    }
}