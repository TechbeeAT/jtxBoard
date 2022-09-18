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
import androidx.compose.runtime.*
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
                        includeReadOnly = current.readonly,
                        includeVJOURNAL = current.supportsVJOURNAL,
                        includeVTODO = current.supportsVTODO,
                        onSelectionChanged = { selected -> newCollection = selected }
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
            accountType = "LOCAL"
        )
        val collection1 = ICalCollection(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )
        val collection2 = ICalCollection(
            collectionId = 2L,
            color = Color.Cyan.toArgb(),
            displayName = "Hmmmm",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )
        val collection3 = ICalCollection(
            collectionId = 3L,
            color = Color.Cyan.toArgb(),
            displayName = null,
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )

        CollectionsMoveCollectionDialog(
            collectionCurrent,
            allCollections = listOf(collection1, collection2, collection3),
            onDismiss = { },
            onEntriesMoved = { _, _ -> }
        )
    }
}

