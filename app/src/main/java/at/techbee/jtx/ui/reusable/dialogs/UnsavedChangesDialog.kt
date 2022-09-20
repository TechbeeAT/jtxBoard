package at.techbee.jtx.ui.reusable.dialogs

/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R


@Composable
fun UnsavedChangesDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { onDiscard() },
        title = { Text(stringResource(id = R.string.dialog_unsaved_changes_title)) },
        text = { Text(stringResource(id = R.string.dialog_unsaved_changes_text)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave()
                }
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDiscard()
                }
            ) {
                Text( stringResource(id = R.string.discard))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun UnsavedChangesDialog_Preview() {
    MaterialTheme {

        UnsavedChangesDialog(
            onSave = { },
            onDiscard = { }
        )
    }
}

