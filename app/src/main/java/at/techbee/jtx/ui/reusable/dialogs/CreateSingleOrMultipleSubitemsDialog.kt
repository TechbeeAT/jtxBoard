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
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module


@Composable
fun CreateSingleOrMultipleSubitemsDialog(
    textToProcess: String,
    module: Module,
    onCreate: (List<ICalObject>) -> Unit,
    onDismiss: () -> Unit
) {

    val splitByLines = textToProcess.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    if (splitByLines.size <= 1) {
        // handle single sub item right now
        onCreate(
            if(module == Module.TODO)
                listOf(ICalObject.createTask(textToProcess))
            else
                listOf(ICalObject.createNote(textToProcess))
        )
        onDismiss()
        return
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                stringResource(
                    id = when (module) {
                        Module.TODO -> R.string.dialog_create_multiple_subtasks
                        else -> R.string.dialog_create_multiple_subnotes
                    }
                )
            )
        },
        text = {
            Text(
                stringResource(
                    id = when (module) {
                        Module.TODO -> R.string.dialog_detected_multiple_subtasks
                        else -> R.string.dialog_detected_multiple_subnotes
                    },
                    splitByLines.size
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(splitByLines.map {
                        if(module == Module.TODO)
                            ICalObject.createTask(it)
                        else
                            ICalObject.createNote(it)
                    })
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.dialog_confirm_create_multiple))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onCreate(
                        if(module == Module.TODO)
                            listOf(ICalObject.createTask(textToProcess))
                        else
                            listOf(ICalObject.createNote(textToProcess))
                    )
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.dialog_confirm_create_single))
            }
        },
    )
}



@Preview(showBackground = true)
@Composable
fun CreateSingleOrMultipleSubitemsDialog_Preview_subtask() {
    MaterialTheme {
        CreateSingleOrMultipleSubitemsDialog(
            textToProcess = "This is" + System.lineSeparator() + System.lineSeparator() + "my text",
            module = Module.TODO,
            onCreate = {},
            onDismiss = {},
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CreateSingleOrMultipleSubitemsDialog_Preview_subnote() {
    MaterialTheme {
        CreateSingleOrMultipleSubitemsDialog(
            textToProcess = "This is" + System.lineSeparator() + System.lineSeparator() + "my text",
            module = Module.NOTE,
            onCreate = {},
            onDismiss = {},
        )
    }
}

