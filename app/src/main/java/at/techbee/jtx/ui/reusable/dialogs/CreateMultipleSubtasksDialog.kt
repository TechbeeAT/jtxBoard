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
fun CreateMultipleSubtasksDialog(
    numberOfSubtasksDetected: Int,
    onCreateSingle: () -> Unit,
    onCreateMultiple: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onCreateSingle() },
        title = { Text(stringResource(id = R.string.dialog_create_multiple)) },
        text = {
            Text(
                stringResource(
                    id = R.string.dialog_detected_multiple_subtasks,
                    numberOfSubtasksDetected
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

@Preview(showBackground = true)
@Composable
fun CreateMultipleSubtasksDialog_Preview() {
    MaterialTheme {
        CreateMultipleSubtasksDialog(
            numberOfSubtasksDetected = 3,
            onCreateSingle = { },
            onCreateMultiple = { },
        )
    }
}

