package at.techbee.jtx.ui.reusable.dialogs

/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R


@Composable
fun DeleteVisibleDialog(
    numEntriesToDelete: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.list_dialog_delete_visible_title)) },
        text = {
            Column {
                Text(stringResource(id = R.string.list_dialog_delete_visible_message, numEntriesToDelete))
                Text(
                    text = stringResource(id = R.string.list_dialog_delete_visible_message2),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
            }
               },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
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
fun DeleteVisibleDialog_Preview() {
    MaterialTheme {

        DeleteVisibleDialog(
            numEntriesToDelete = 5,
            onConfirm = { },
            onDismiss = { }
        )
    }
}

