/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.collections

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MoveDown
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.reusable.dialogs.CollectionsAddOrEditDialog
import at.techbee.jtx.ui.reusable.dialogs.CollectionsDeleteCollectionDialog
import at.techbee.jtx.ui.reusable.dialogs.CollectionsMoveCollectionDialog
import at.techbee.jtx.ui.reusable.elements.ListBadge
import at.techbee.jtx.ui.theme.Typography
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncApp
import at.techbee.jtx.util.SyncUtil


@Composable
fun CollectionCard(
    collection: CollectionsView,
    allCollections: List<CollectionsView>,
    settingAccessibilityMode: Boolean,
    onCollectionChanged: (ICalCollection) -> Unit,
    onCollectionDeleted: (ICalCollection) -> Unit,
    onEntriesMoved: (old: ICalCollection, new: ICalCollection) -> Unit,
    onImportFromICS: (CollectionsView) -> Unit,
    onImportFromTxt: (CollectionsView) -> Unit,
    onExportAsICS: (CollectionsView) -> Unit,
    modifier: Modifier = Modifier
) {

    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var showCollectionsAddOrEditDialog by rememberSaveable { mutableStateOf(false) }
    var showCollectionsDeleteCollectionDialog by rememberSaveable { mutableStateOf(false) }
    var showCollectionsMoveCollectionDialog by rememberSaveable { mutableStateOf(false) }
    val syncApp = SyncApp.fromAccountType(collection.accountType)


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
                        ListBadge(
                            icon = Icons.Outlined.FolderOpen,
                            iconDesc = stringResource(id = R.string.collection),
                            containerColor = collection.color?.let { Color (it) } ?: MaterialTheme.colorScheme.primaryContainer,
                            isAccessibilityMode = settingAccessibilityMode
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            collection.displayName?.let {
                                Text(
                                    text = it,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = Typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            if(collection.accountType != LOCAL_ACCOUNT_TYPE) {
                                Text(
                                    text = try { Uri.parse(collection.url).host } catch (e: NullPointerException) { null } ?: "",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.alpha(0.5f)
                                )
                            }
                        }
                    }
                    if (collection.description?.isNotBlank() == true) {
                        Text(
                            collection.description ?: "",
                            style = Typography.bodySmall,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {

                        collection.ownerDisplayName?.let {
                            ListBadge(
                                icon = Icons.Outlined.AccountCircle,
                                iconDesc = null,
                                text = it,
                                isAccessibilityMode = settingAccessibilityMode,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }

                        collection.lastSync?.let {
                            ListBadge(
                                icon = Icons.Outlined.Sync,
                                iconDesc = null,
                                text = DateTimeUtils.convertLongToMediumDateShortTimeString(
                                    it,
                                    null
                                ),
                                isAccessibilityMode = settingAccessibilityMode,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
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

                        ListBadge(
                            text = stringResource(id = R.string.collections_journals_num, numJournals),
                            isAccessibilityMode = settingAccessibilityMode
                        )
                        ListBadge(
                            text = stringResource(id = R.string.collections_notes_num, numNotes),
                            isAccessibilityMode = settingAccessibilityMode
                        )
                        ListBadge(
                            text = stringResource(id = R.string.collections_tasks_num, numTodos),
                            isAccessibilityMode = settingAccessibilityMode
                        )
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
                            if (syncApp != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_collection_popup_show_in_sync_app, syncApp.appName)) },
                                    leadingIcon = { Icon(Icons.Outlined.Sync, null) },
                                    onClick = {
                                        SyncUtil.openSyncAppAccountActivity(syncApp, collection.toICalCollection().getAccount(), context)
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
                            if (!collection.readonly) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.menu_collection_popup_import_from_textfile)) },
                                    leadingIcon = { Icon(Icons.Outlined.Upload, null) },
                                    onClick = {
                                        menuExpanded = false
                                        onImportFromTxt(collection)
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
            onImportFromTxt = { },
            onExportAsICS = { },
            settingAccessibilityMode = false
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
            this.color = null
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
            onImportFromTxt = { },
            onExportAsICS = { },
            settingAccessibilityMode = false
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
            this.url = "https://www.example.com/asdf/09809898797/index.php?whatever"
            this.accountType = "remote_account_type"
            this.ownerDisplayName = "Owner John Doe"
            this.lastSync = System.currentTimeMillis()
        }

        CollectionCard(
            collection,
            allCollections = listOf(collection),
            onCollectionChanged = { },
            onCollectionDeleted = { },
            onEntriesMoved = { _, _ -> },
            onImportFromICS = { },
            onImportFromTxt = { },
            onExportAsICS = { },
            settingAccessibilityMode = true
        )
    }
}