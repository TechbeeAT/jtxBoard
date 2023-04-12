/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.ui.reusable.elements.ColorSelectorRow
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CollectionsAddOrEditDialog(
    current: ICalCollection,
    onCollectionChanged: (ICalCollection) -> Unit,
    onDismiss: () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    var collectionName by remember { mutableStateOf(current.displayName ?: "") }
    var collectionColor by remember { mutableStateOf(current.color?.let { Color(it) }) }
    var noCollectionNameError by remember { mutableStateOf(false) }


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            if (current.collectionId == 0L)
                Text(text = stringResource(id = R.string.collections_dialog_add_local_collection_title))
            else
                Text(text = stringResource(id = R.string.collections_dialog_edit_local_collection_title))
        },
        text = {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    OutlinedTextField(
                        value = collectionName,
                        onValueChange = { collectionName = it },
                        label = { Text(stringResource(id = R.string.collection)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        isError = noCollectionNameError
                    )
                }

                ColorSelectorRow(
                    selectedColor = collectionColor,
                    onColorChanged = { collectionColor = it })

                HarmonyColorPicker(
                    color = if(collectionColor == null || collectionColor == Color.Transparent) HsvColor.from(Color.White) else HsvColor.from(collectionColor!!),
                    harmonyMode = ColorHarmonyMode.NONE,
                    modifier = Modifier.size(300.dp),
                    onColorChanged = { hsvColor ->
                        collectionColor = hsvColor.toColor()
                    })

            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (collectionName.isBlank())
                        noCollectionNameError = true
                    else {
                        current.displayName = collectionName
                        current.color = collectionColor?.toArgb()
                        onCollectionChanged(current)
                        onDismiss()
                    }
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
fun CollectionsEditDialog_Preview() {
    MaterialTheme {

        val collection1 = ICalCollection(
            collectionId = 1L,
            color = Color.Cyan.toArgb(),
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )

        CollectionsAddOrEditDialog(
            collection1,
            onCollectionChanged = { },
            onDismiss = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionsEditDialog_Preview2() {
    MaterialTheme {

        val collection1 = ICalCollection(
            collectionId = 0L,
            color = null,
            displayName = "Collection Display Name",
            description = "Here comes the desc",
            accountName = "My account",
            accountType = "LOCAL"
        )

        CollectionsAddOrEditDialog(
            collection1,
            onCollectionChanged = { },
            onDismiss = { }
        )
    }
}