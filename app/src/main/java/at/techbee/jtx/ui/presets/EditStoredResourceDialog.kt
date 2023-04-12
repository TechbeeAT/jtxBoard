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
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.ui.reusable.elements.ColorSelectorRow
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditStoredResourceDialog(
    storedResource: StoredResource,
    onStoredResourceChanged: (StoredResource) -> Unit,
    onDeleteStoredResource: (StoredResource) -> Unit,
    onDismiss: () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    var storedResourceName by remember { mutableStateOf(storedResource.resource) }
    var storedResourceColor by remember { mutableStateOf(storedResource.color?.let { Color(it) }) }


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(id = R.string.dialog_edit_stored_resource_title)) },
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
                        value = storedResourceName,
                        onValueChange = { storedResourceName = it },
                        label = { Text(stringResource(R.string.resources)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        isError = storedResourceName.isEmpty()
                    )
                }

                ColorSelectorRow(
                    selectedColor = storedResourceColor,
                    onColorChanged = { storedResourceColor = it })

                HarmonyColorPicker(
                    color = if(storedResourceColor == null || storedResourceColor == Color.Transparent) HsvColor.from(Color.White) else HsvColor.from(storedResourceColor!!),
                    harmonyMode = ColorHarmonyMode.NONE,
                    modifier = Modifier.size(300.dp),
                    onColorChanged = { hsvColor -> storedResourceColor = hsvColor.toColor() })




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

                if(storedResource.resource.isNotEmpty()) {
                    TextButton(onClick = {
                        onDeleteStoredResource(storedResource)
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.delete))
                    }
                }

                TextButton(
                    onClick = {
                        onStoredResourceChanged(StoredResource(storedResourceName, storedResourceColor?.toArgb()))
                        onDismiss()
                    },
                    enabled = storedResourceName.isNotEmpty()
                ) {
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    )

}

@Preview(showBackground = true)
@Composable
fun EditStoredResourceDialogPreview() {
    MaterialTheme {

        EditStoredResourceDialog(
            storedResource = StoredResource("test", Color.Magenta.toArgb()),
            onStoredResourceChanged = { },
            onDeleteStoredResource = { },
            onDismiss = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun EditStoredResourceDialogPreview_new() {
    MaterialTheme {

        EditStoredResourceDialog(
            storedResource = StoredResource("", null),
            onStoredResourceChanged = { },
            onDeleteStoredResource = { },
            onDismiss = { }
        )
    }
}


