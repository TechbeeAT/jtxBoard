package at.techbee.jtx.ui.reusable.dialogs

/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R


@Composable
fun DetachFromSeriesDialog(
    detachAll: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.dialog_detach_from_series_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(
                    id = if(detachAll)
                        R.string.dialog_detach_from_series_text_all
                    else
                        R.string.dialog_detach_from_series_text_single)
                )
                
                Text(
                    text = stringResource(id = R.string.dialog_detach_from_series_text_attention),
                    color = MaterialTheme.colorScheme.error,
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
                Text(stringResource(id = R.string.dialog_detach_from_series_button))
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
fun DetachFromSeriesDialog_Single_Preview() {
    MaterialTheme {

        DetachFromSeriesDialog(
            detachAll = false,
            onConfirm = { },
            onDismiss = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetachFromSeriesDialog_All_Preview() {
    MaterialTheme {

        DetachFromSeriesDialog(
            detachAll = true,
            onConfirm = { },
            onDismiss = { }
        )
    }
}