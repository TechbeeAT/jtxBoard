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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject


@Composable
fun DeleteEntryDialog(
    icalObject: ICalObject,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    val maxSummaryLength = 30
    val summaryTrunc = if((icalObject.summary?.length?:0) > maxSummaryLength) icalObject.summary?.take(maxSummaryLength-3) + "..." else icalObject.summary?:""

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            if(icalObject.sequence == 0L)
                Text(stringResource(id = R.string.edit_dialog_sure_to_discard_title))
            else
                Text(stringResource(id = R.string.edit_dialog_sure_to_delete_title, summaryTrunc))
        },
        text = {
            if(icalObject.sequence == 0L)
                Text(stringResource(id = R.string.edit_dialog_sure_to_discard_message),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            else
                Text(stringResource(id = R.string.edit_dialog_sure_to_delete_message, summaryTrunc),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
               },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    stringResource(
                        id = if(icalObject.sequence == 0L)
                            R.string.discard
                        else
                            R.string.delete
                    )
                )
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
fun DeleteEntryDialog_Preview_discard() {
    MaterialTheme {

        DeleteEntryDialog(
            icalObject = ICalObject.createTask("MyTask with a very long description, at least more than 25 characters!"),
            onConfirm = { },
            onDismiss = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DeleteEntryDialog_Preview_delete() {
    MaterialTheme {

        DeleteEntryDialog(
            icalObject = ICalObject.createTask("MyTask with a very long description, at least more than 25 characters!").apply { sequence = 1 },
            onConfirm = { },
            onDismiss = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DeleteEntryDialog_Preview_delete_short() {
    MaterialTheme {

        DeleteEntryDialog(
            icalObject = ICalObject.createTask("MyTask").apply { sequence = 1 },
            onConfirm = { },
            onDismiss = { }
        )
    }
}