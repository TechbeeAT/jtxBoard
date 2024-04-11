/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import android.os.Parcelable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.ui.detail.DetailTopAppBarMode
import at.techbee.jtx.ui.detail.DetailTopAppBarMode.ADD_SUBNOTE
import at.techbee.jtx.ui.detail.DetailTopAppBarMode.ADD_SUBTASK
import kotlinx.parcelize.Parcelize


@Composable
fun CreateMultipleSubitemsDialog(
    numberOfSubitemsDetected: Int,
    onCreateSingle: () -> Unit,
    onCreateMultiple: () -> Unit,
    itemType: DetailTopAppBarMode
) {
    AlertDialog(
        onDismissRequest = { onCreateSingle() },
        title = {
            Text(
                stringResource(
                    id = when (itemType) {
                        ADD_SUBTASK -> R.string.dialog_create_multiple_subtasks
                        ADD_SUBNOTE -> R.string.dialog_create_multiple_subnotes
                    }
                )
            )
        },
        text = {
            Text(
                stringResource(
                    id = when (itemType) {
                        ADD_SUBTASK -> R.string.dialog_detected_multiple_subtasks
                        ADD_SUBNOTE -> R.string.dialog_detected_multiple_subnotes
                    },
                    numberOfSubitemsDetected
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreateMultiple() }
            ) {
                Text(stringResource(id = R.string.dialog_confirm_create_multiple))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onCreateSingle() }
            ) {
                Text(stringResource(id = R.string.dialog_confirm_create_single))
            }
        },
    )
}

@Composable
fun onSingleOrMultipleItemCreation(
    itemType: DetailTopAppBarMode,
    onAddSingle: (String) -> Unit
): (String) -> Unit {

    var previousText by rememberSaveable { mutableStateOf(emptyPreviousText) }

    if (previousText != emptyPreviousText) {
        CreateMultipleSubitemsDialog(
            numberOfSubitemsDetected = previousText.listOfSubitems.size,
            onCreateSingle = {
                onAddSingle(previousText.single)
                previousText = emptyPreviousText
            },
            onCreateMultiple = {
                previousText.listOfSubitems.forEach(onAddSingle)
                previousText = emptyPreviousText
            },
            itemType = itemType
        )
    }

    val onItemCreation = { value: String ->
        val listOfSubitems = value.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (listOfSubitems.size <= 1) {
            // handle single sub item right now
            onAddSingle(value)
        } else {
            // handle multiple sub items within a dialog
            previousText = SingleOrMultipleItems(single = value, listOfSubitems = listOfSubitems)
        }
    }

    return onItemCreation
}

@Parcelize
data class SingleOrMultipleItems(
    val single: String,
    val listOfSubitems: List<String>
) : Parcelable

val emptyPreviousText = SingleOrMultipleItems("", emptyList())

@Preview(showBackground = true)
@Composable
fun CreateMultipleSubitemsDialog_Preview() {
    MaterialTheme {
        CreateMultipleSubitemsDialog(
            numberOfSubitemsDetected = 3,
            onCreateSingle = { },
            onCreateMultiple = { },
            itemType = ADD_SUBNOTE
        )
    }
}

