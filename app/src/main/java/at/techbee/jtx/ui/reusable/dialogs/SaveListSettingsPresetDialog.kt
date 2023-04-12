/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveListSettingsPresetDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {

    var currentText by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.dialog_save_current_filter_config)) },
        text = {

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedTextField(
                    value = currentText,
                    isError = currentText.isBlank(),
                    onValueChange = { newText ->
                        currentText = newText
                    },
                    maxLines = 1,
                    label = { Text(stringResource(R.string.dialog_save_current_filter_my_filter)) }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = currentText.isNotBlank(),
                onClick = {
                        onConfirm(currentText)
                        onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SaveListSettingsPresetDialog_Preview() {

    MaterialTheme {
        SaveListSettingsPresetDialog(
            onConfirm = { },
            onDismiss = { }
        )
    }
}
