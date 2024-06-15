/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.reusable.elements.CollectionsSpinner


@Composable
fun CollectionSelectorDialog(
    module: Module,
    presetCollectionId: Long,
    allWritableCollections: List<ICalCollection>,
    onCollectionConfirmed: (selectedCollection: ICalCollection) -> Unit,
    onDismiss: () -> Unit
) {
    if(allWritableCollections.isEmpty())
        return
    var selectedCollection by remember { mutableStateOf(allWritableCollections.find { it.collectionId == presetCollectionId } ?: allWritableCollections.first()) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.dialog_collection_new_entries_title)) },
        text = {
            Box {

                Column {
                    CollectionsSpinner(
                        collections = allWritableCollections,
                        preselected = selectedCollection,
                        includeReadOnly = false,
                        includeVJOURNAL = if(module == Module.JOURNAL || module == Module.NOTE) true else null,
                        includeVTODO = if(module == Module.TODO) true else null,
                        onSelectionChanged = { selected -> selectedCollection = selected },
                        showSyncButton = false,
                        showColorPicker = false,
                        enableSelector = true,
                        onColorPicked = { }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCollectionConfirmed(selectedCollection)
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text( stringResource(id = R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CollectionSelectorDialog_Preview() {
    MaterialTheme {

        val collection1 = ICalCollection(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Only VJournal",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL",
            supportsVJOURNAL = true,
            supportsVTODO = false
        )
        val collection2 = ICalCollection(
            collectionId = 2L,
            color = Color.Cyan.toArgb(),
            displayName = "Only VTodo",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL",
            supportsVJOURNAL = false,
            supportsVTODO = true
        )
        val collection3 = ICalCollection(
            collectionId = 3L,
            color = Color.Cyan.toArgb(),
            displayName = "both",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL",
            supportsVJOURNAL = true,
            supportsVTODO = true
        )
        val collection4 = ICalCollection(
            collectionId = 4L,
            color = Color.Cyan.toArgb(),
            displayName = "none",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL",
            supportsVJOURNAL = false,
            supportsVTODO = false
        )

        CollectionSelectorDialog(
            module = Module.JOURNAL,
            presetCollectionId = 3L,
            allWritableCollections = listOf(collection1, collection2, collection3, collection4),
            onDismiss = { },
            onCollectionConfirmed = {}
        )
    }
}

