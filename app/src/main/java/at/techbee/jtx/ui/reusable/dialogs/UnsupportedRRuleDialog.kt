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


@Composable
fun UnsupportedRRuleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.details_recur_unknown_rrule_dialog_title)) },
        text = { Text(stringResource(id = R.string.details_recur_unknown_rrule_dialog_message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
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
fun UnsupportedRRuleDialog_Preview() {
    MaterialTheme {
        UnsupportedRRuleDialog(
            onConfirm = { },
            onDismiss = { }
        )
    }
}

