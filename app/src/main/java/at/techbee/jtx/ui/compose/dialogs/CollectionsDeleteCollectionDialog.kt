/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.theme.JtxBoardTheme


@Composable
fun CollectionsDeleteCollectionDialog(
    current: CollectionsView,
    onCollectionDeleted: (ICalCollection) -> Unit,
    onDismiss: () -> Unit
) {


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.collections_dialog_delete_local_title, current.displayName?:current.accountName?:"")) },
        text = { Text(stringResource(id = R.string.collections_dialog_delete_local_message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onCollectionDeleted(current.toICalCollection())
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.delete))
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
fun CollectionsDeleteCollectionDialog_Preview() {
    JtxBoardTheme {

        val collection1 = CollectionsView(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )

        CollectionsDeleteCollectionDialog(
            collection1,
            onCollectionDeleted = { },
            onDismiss = { }
        )
    }
}

