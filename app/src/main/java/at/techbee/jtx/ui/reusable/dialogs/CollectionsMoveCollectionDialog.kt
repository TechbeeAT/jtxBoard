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
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.reusable.elements.CollectionsSpinner


@Composable
fun CollectionsMoveCollectionDialog(
    current: CollectionsView,
    allCollections: List<ICalCollection>,
    onEntriesMoved: (old: ICalCollection, new: ICalCollection) -> Unit,
    onDismiss: () -> Unit
) {

    var newCollection by remember { mutableStateOf(current.toICalCollection()) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.collections_dialog_move_title, current.displayName?: "")) },
        text = {
            Box {

                Column {
                    CollectionsSpinner(
                        collections = allCollections,
                        preselected = current.toICalCollection(),
                        includeReadOnly = false,
                        includeVJOURNAL = if((current.numJournals?:0) + (current.numNotes?: 0) > 0) true else null,
                        includeVTODO = if((current.numTodos?:0) > 0) true else null,
                        onSelectionChanged = { selected -> newCollection = selected },
                        showSyncButton = false,
                        showColorPicker = false,
                        enableSelector = true,
                        onColorPicked = { }
                    )

                    Text(stringResource(id = R.string.collection_dialog_move_info))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if(current.collectionId != newCollection.collectionId)
                        onEntriesMoved(current.toICalCollection(), newCollection)
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.ok))
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
fun CollectionsMoveCollectionDialog_Preview() {
    MaterialTheme {

        val collectionCurrent = CollectionsView(
            collectionId = 2L,
            color = Color.Cyan.toArgb(),
            displayName = "Hmmmm",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL",
            supportsVTODO = true,
            supportsVJOURNAL = true,
            numJournals = 0,
            numNotes = 0,
            numTodos = 34
        )

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

        CollectionsMoveCollectionDialog(
            collectionCurrent,
            allCollections = listOf(collection1, collection2, collection3, collection4),
            onDismiss = { },
            onEntriesMoved = { _, _ -> }
        )
    }
}

