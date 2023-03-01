/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.collections

import android.accounts.Account
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.reusable.dialogs.CollectionsAddOrEditDialog
import at.techbee.jtx.ui.reusable.dialogs.CollectionsDeleteCollectionDialog
import at.techbee.jtx.ui.reusable.dialogs.CollectionsMoveCollectionDialog
import at.techbee.jtx.ui.theme.Typography
import at.techbee.jtx.util.SyncUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionCard(
    collection: CollectionsView,
    allCollections: List<CollectionsView>,
    onCollectionChanged: (ICalCollection) -> Unit,
    onCollectionDeleted: (ICalCollection) -> Unit,
    onEntriesMoved: (old: ICalCollection, new: ICalCollection) -> Unit,
    onImportFromICS: (CollectionsView) -> Unit,
    onExportAsICS: (CollectionsView) -> Unit,
    modifier: Modifier = Modifier
) {

    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var showCollectionsAddOrEditDialog by remember { mutableStateOf(false) }
    var showCollectionsDeleteCollectionDialog by remember { mutableStateOf(false) }
    var showCollectionsMoveCollectionDialog by remember { mutableStateOf(false) }

    if (showCollectionsAddOrEditDialog)
        CollectionsAddOrEditDialog(
            current = collection.toICalCollection(),
            onCollectionChanged = onCollectionChanged,
            onDismiss = { showCollectionsAddOrEditDialog = false }
        )

    if (showCollectionsDeleteCollectionDialog)
        CollectionsDeleteCollectionDialog(
            current = collection,
            onCollectionDeleted = onCollectionDeleted,
            onDismiss = { showCollectionsDeleteCollectionDialog = false }
        )

    if (showCollectionsMoveCollectionDialog)
        CollectionsMoveCollectionDialog(
            current = collection,
            allCollections = mutableListOf<ICalCollection>().apply {
                allCollections.forEach { collection -> this.add(collection.toICalCollection()) }
            },
            onEntriesMoved = onEntriesMoved,
            onDismiss = { showCollectionsMoveCollectionDialog = false }
        )


    ElevatedCard(
        modifier = modifier
    ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        collection.color?.let {
                            Badge(
                                containerColor = Color(it)
                            ) {
                                Text(collection.displayName?.firstOrNull()?.toString() ?: " ")
                            }
                        }
                        Text(
                            collection.displayName ?: collection.accountName ?: collection.accountType ?: "",
                            style = Typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (collection.description?.isNotBlank() == true) {
                        Text(
                            collection.description ?: "",
                            style = Typography.bodySmall,
                        )
                    }


                    if(collection.ownerDisplayName != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.AccountCircle,
                                null,
                                Modifier.size(12.dp)
                            )
                            Text(
                                text = stringResource(R.string.collection_owner_displayname, collection.ownerDisplayName!!),
                                style = Typography.labelSmall,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {

                        val notAvailable = stringResource(id = R.string.not_available_abbreviation)
                        val numJournals =
                            if (collection.supportsVJOURNAL) collection.numJournals?.toString()
                                ?: "0" else notAvailable
                        val numNotes =
                            if (collection.supportsVJOURNAL) collection.numNotes?.toString()
                                ?: "0" else notAvailable
                        val numTodos = if (collection.supportsVTODO) collection.numTodos?.toString()
                            ?: "0" else notAvailable

                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = stringResource(id = R.string.collections_journals_num, numJournals),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = stringResource(id = R.string.collections_notes_num, numNotes),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = stringResource(id = R.string.collections_tasks_num, numTodos),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }



                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (collection.readonly)
                        IconButton(
                            onClick = {  },
                            enabled = false
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_readonly),
                                contentDescription = stringResource(id = R.string.readyonly),
                            )
                        }


                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            stringResource(R.string.collections_collection_menu)
                        )

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            if (collection.accountType == LOCAL_ACCOUNT_TYPE) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.edit)) },
                                    leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                                    onClick = {
                                        showCollectionsAddOrEditDialog = true
                                        menuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.delete)) },
                                    leadingIcon = { Icon(Icons.Outlined.Delete, null) },
                                    onClick = {
                                        showCollectionsDeleteCollectionDialog = true
                                        menuExpanded = false
                                    }
                                )
                            }
                            if (collection.accountType != LOCAL_ACCOUNT_TYPE) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.menu_collection_popup_show_in_davx5)) },
                                    leadingIcon = { Icon(Icons.Outlined.Sync, null) },
                                    onClick = {
                                        SyncUtil.openDAVx5AccountActivity(Account(collection.accountName, collection.accountType), context)
                                        menuExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.menu_collection_popup_export_as_ics)) },
                                leadingIcon = { Icon(Icons.Outlined.Download, null) },
                                onClick = {
                                    onExportAsICS(collection)
                                    menuExpanded = false
                                }
                            )
                            if (!collection.readonly) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.menu_collection_popup_import_from_ics)) },
                                    leadingIcon = { Icon(Icons.Outlined.Upload, null) },
                                    onClick = {
                                        onImportFromICS(collection)
                                        menuExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.menu_collections_popup_move_entries)) },
                                leadingIcon = { Icon(Icons.Outlined.MoveDown, null) },
                                onClick = {
                                    showCollectionsMoveCollectionDialog = true
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }

            }
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionCardPreview() {
    MaterialTheme {

        val collection = CollectionsView().apply {
            this.displayName = "My collection name"
            this.description = "My collection desc\nription"
            this.color = Color.Cyan.toArgb()
            this.numJournals = 24
            this.numNotes = 33
            this.numTodos = 1
            this.supportsVJOURNAL = true
            this.supportsVTODO = true
        }

        CollectionCard(
            collection = collection,
            allCollections = listOf(collection),
            onCollectionChanged = { },
            onCollectionDeleted = { },
            onEntriesMoved = { _, _ -> },
            onImportFromICS = { },
            onExportAsICS = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionCardPreview2() {
    MaterialTheme {

        val collection = CollectionsView().apply {
            this.displayName = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla nisi sem, sollicitudin tristique leo eget, iaculis pharetra lacus."
            this.description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla nisi sem, sollicitudin tristique leo eget, iaculis pharetra lacus. In ex mi, sollicitudin sit amet hendrerit vitae, egestas vitae tortor. Sed dui mi, consequat vel felis sit amet, sagittis mollis urna. Donec varius nec diam et faucibus. Suspendisse potenti. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Fusce eget condimentum justo, at finibus dolor. Quisque posuere erat vel tellus fringilla iaculis. Nullam massa mauris, sodales sit amet scelerisque maximus, interdum in ex."
            this.color = Color.Cyan.toArgb()
            this.numJournals = 0
            this.numNotes = 0
            this.numTodos = 0
            this.supportsVJOURNAL = false
            this.supportsVTODO = false
            this.readonly = true
            this.ownerDisplayName = "Owner John Doe"
        }

        CollectionCard(
            collection,
            allCollections = listOf(collection),
            onCollectionChanged = { },
            onCollectionDeleted = { },
            onEntriesMoved = { _, _ -> },
            onImportFromICS = { },
            onExportAsICS = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionCardPreview3() {
    MaterialTheme {

        val collection = CollectionsView().apply {
            this.displayName = "My collection name"
            this.color = Color.Magenta.toArgb()
            this.supportsVJOURNAL = true
            this.supportsVTODO = true
        }

        CollectionCard(
            collection,
            allCollections = listOf(collection),
            onCollectionChanged = { },
            onCollectionDeleted = { },
            onEntriesMoved = { _, _ -> },
            onImportFromICS = { },
            onExportAsICS = { }
        )
    }
}