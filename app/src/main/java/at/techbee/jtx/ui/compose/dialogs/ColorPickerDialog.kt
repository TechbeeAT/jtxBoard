/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.compose.elements.ColorSelectorRow
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker


@Composable
fun ColorPickerDialog(
    initialColor: Int?,
    onColorChanged: (Int?) -> Unit,
    onDismiss: () -> Unit
) {

    var selectedColor by remember { mutableStateOf(initialColor?.let {Color(it)}) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(id = R.string.color)) },
        text = {
            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                    ColorSelectorRow(
                        initialColor = selectedColor,
                        onColorChanged = { newColor -> selectedColor = newColor },
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp)
                    )

                    HarmonyColorPicker(
                        color = selectedColor ?: Color.White,
                        harmonyMode = ColorHarmonyMode.NONE,
                        modifier = Modifier.size(300.dp),
                        onColorChanged = { hsvColor -> selectedColor = hsvColor.toColor()
                        })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onColorChanged(selectedColor?.toArgb())
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
                Text( stringResource(id = R.string.cancel))
            }
        }
    )

 }

@Preview(showBackground = true)
@Composable
fun ColorPickerDialog_Preview() {
    MaterialTheme {

        ColorPickerDialog(
            initialColor = Color.Cyan.toArgb(),
            onColorChanged = { },
            onDismiss = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ColorPickerDialog_Preview_initially_null() {
    MaterialTheme {

        ColorPickerDialog(
            initialColor = null,
            onColorChanged = { },
            onDismiss = { }
        )
    }
}