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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.ui.reusable.elements.ColorSelector
import com.github.skydoves.colorpicker.compose.rememberColorPickerController


@Composable
fun EditStoredCategoryDialog(
    storedCategory: StoredCategory,
    onStoredCategoryChanged: (StoredCategory) -> Unit,
    onDeleteStoredCategory: (StoredCategory) -> Unit,
    onDismiss: () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    var storedCategoryName by remember { mutableStateOf(storedCategory.category) }
    val colorPickerController = rememberColorPickerController()


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(id = R.string.dialog_edit_stored_category_title)) },
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
                        value = storedCategoryName,
                        onValueChange = { storedCategoryName = it },
                        label = { Text(stringResource(R.string.category)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        isError = storedCategoryName.isEmpty()
                    )
                }

                ColorSelector(
                    initialColorInt = storedCategory.color,
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

                if(storedCategory.category.isNotEmpty()) {
                    TextButton(onClick = {
                        onDeleteStoredCategory(storedCategory)
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.delete))
                    }
                }

                TextButton(
                    onClick = {
                        onStoredCategoryChanged(StoredCategory(storedCategoryName, if(colorPickerController.selectedColor.value == Color.Unspecified) null else colorPickerController.selectedColor.value.toArgb()))
                        onDismiss()
                    },
                    enabled = storedCategoryName.isNotEmpty()
                ) {
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun EditStoredCategoryDialogPreview() {
    MaterialTheme {

        EditStoredCategoryDialog(
            storedCategory = StoredCategory("test", Color.Magenta.toArgb()),
            onStoredCategoryChanged = { },
            onDeleteStoredCategory = { },
            onDismiss = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditStoredCategoryDialogPreview_new() {
    MaterialTheme {

        EditStoredCategoryDialog(
            storedCategory = StoredCategory("", null),
            onStoredCategoryChanged = { },
            onDeleteStoredCategory = { },
            onDismiss = { }
        )
    }
}



