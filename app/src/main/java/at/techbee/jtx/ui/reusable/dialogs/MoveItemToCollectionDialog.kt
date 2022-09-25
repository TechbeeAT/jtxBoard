/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection


@Composable
fun MoveItemToCollectionDialog(
    newCollection: ICalCollection,
    onMoveConfirmed: () -> Unit,
    onDismiss: () -> Unit
) {


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.dialog_move_item_to_collection_title, newCollection.displayName?:newCollection.accountName?:"")) },
        text = { Text(stringResource(id = R.string.dialog_move_item_to_collection_text)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onMoveConfirmed()
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
fun MoveItemToCollectionDialog_Preview() {
    MaterialTheme {

        val collection = ICalCollection(
            collectionId = 1L,
            displayName = "Collection Display Name",
        )

        MoveItemToCollectionDialog(
            collection,
            onMoveConfirmed = { },
            onDismiss = { }
        )
    }
}

