/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.presets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.StoredStatus
import at.techbee.jtx.ui.reusable.elements.ColorSelectorRow
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditStoredStatusDialog(
    storedStatus: StoredStatus,
    isDefaultStatus: Boolean,
    onStoredStatusChanged: (StoredStatus) -> Unit,
    onDeleteStoredStatus: (StoredStatus) -> Unit,
    onDismiss: () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    var storedStatusName by remember { mutableStateOf(storedStatus.status) }
    var storedStatusColor by remember { mutableStateOf(storedStatus.color?.let { Color(it) }) }


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
                        enabled = !isDefaultStatus,
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

                ColorSelectorRow(
                    selectedColor = storedStatusColor,
                    onColorChanged = { storedStatusColor = it })

                HarmonyColorPicker(
                    color = if(storedStatusColor == null || storedStatusColor == Color.Transparent) HsvColor.from(Color.White) else HsvColor.from(storedStatusColor!!),
                    harmonyMode = ColorHarmonyMode.NONE,
                    modifier = Modifier.size(300.dp),
                    onColorChanged = { hsvColor -> storedStatusColor = hsvColor.toColor() })
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

                if(storedStatus.status.isNotEmpty() && !isDefaultStatus) {
                    TextButton(onClick = {
                        onDeleteStoredStatus(storedStatus)
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.delete))
                    }
                }

                TextButton(
                    onClick = {
                        onStoredStatusChanged(StoredStatus(storedStatusName, storedStatus.module, storedStatus.rfcStatus, storedStatusColor?.toArgb()))
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
            storedStatus = StoredStatus("test", Module.JOURNAL.name, Status.FINAL, Color.Magenta.toArgb()),
            isDefaultStatus = false,
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
            storedStatus = StoredStatus("test", Module.JOURNAL.name, Status.FINAL, Color.Magenta.toArgb()),
            isDefaultStatus = true,
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
            storedStatus = StoredStatus("",  Module.JOURNAL.name, Status.FINAL, null),
            isDefaultStatus = false,
            onStoredStatusChanged = { },
            onDeleteStoredStatus = { },
            onDismiss = { }
        )
    }
}