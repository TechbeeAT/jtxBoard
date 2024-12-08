/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.presets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.ui.reusable.elements.ColorSelector
import com.github.skydoves.colorpicker.compose.rememberColorPickerController


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditStoredStatusDialog(
    storedStatus: ExtendedStatus,
    onStoredStatusChanged: (ExtendedStatus) -> Unit,
    onDeleteStoredStatus: (ExtendedStatus) -> Unit,
    onDismiss: () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    var storedStatusName by remember { mutableStateOf(storedStatus.xstatus) }
    var storedStatusRfcStatus by remember { mutableStateOf(storedStatus.rfcStatus) }
    val colorPickerController = rememberColorPickerController()


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(id = R.string.dialog_edit_stored_status_title)) },
        text = {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    OutlinedTextField(
                        value = storedStatusName,
                        onValueChange = { storedStatusName = it },
                        label = { Text(stringResource(R.string.status)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        isError = storedStatusName.isEmpty()
                    )
                }

                Text(
                    text = "maps to",
                    modifier = Modifier.padding(8.dp)
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                    Status.valuesFor(storedStatus.module).forEach { status ->
                        FilterChip(
                            selected = storedStatusRfcStatus == status,
                            onClick = { storedStatusRfcStatus = status },
                            label = { Text(stringResource(id = status.stringResource)) }
                        )
                    }
                }

                Text(
                    text = "Custom statuses are only visible in jtx Board but are mapped to a standard status of your choice for interoperability with other applications.",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )


                ColorSelector(
                    initialColorInt = storedStatus.color,
                    colorPickerController = colorPickerController
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text(stringResource(id = R.string.cancel))
                }

                if(storedStatus.xstatus.isNotEmpty()) {
                    TextButton(onClick = {
                        onDeleteStoredStatus(storedStatus)
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.delete))
                    }
                }

                TextButton(
                    onClick = {
                        onStoredStatusChanged(
                            ExtendedStatus(
                                storedStatusName,
                                storedStatus.module,
                                storedStatusRfcStatus,
                                if(colorPickerController.selectedColor.value == Color.Unspecified)
                                    null
                                else
                                    colorPickerController.selectedColor.value.toArgb()
                            )
                        )
                        onDismiss()
                    },
                    enabled = storedStatusName.isNotEmpty()
                ) {
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun EditStoredStatusDialogPreview_canEdit() {
    MaterialTheme {

        EditStoredStatusDialog(
            storedStatus = ExtendedStatus("test", Module.JOURNAL, Status.NO_STATUS, Color.Magenta.toArgb()),
            onStoredStatusChanged = { },
            onDeleteStoredStatus = { },
            onDismiss = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditStoredStatusDialogPreview_canNOTEdit() {
    MaterialTheme {

        EditStoredStatusDialog(
            storedStatus = ExtendedStatus("test", Module.JOURNAL, Status.NO_STATUS, Color.Magenta.toArgb()),
            onStoredStatusChanged = { },
            onDeleteStoredStatus = { },
            onDismiss = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun EditStoredStatusDialogPreview_new() {
    MaterialTheme {

        EditStoredStatusDialog(
            storedStatus = ExtendedStatus("",  Module.JOURNAL, Status.NO_STATUS, null),
            onStoredStatusChanged = { },
            onDeleteStoredStatus = { },
            onDismiss = { }
        )
    }
}