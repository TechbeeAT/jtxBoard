/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.elements.ColorSelectorRow
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker


@OptIn(ExperimentalComposeUiApi::class)
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
        properties = DialogProperties(usePlatformDefaultWidth = false),   // Workaround due to Google Issue: https://issuetracker.google.com/issues/194911971?pli=1
        text = {

            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {


                ColorSelectorRow(
                    selectedColor = selectedColor,
                    onColorChanged = { selectedColor = it })

                HarmonyColorPicker(
                    color = if(selectedColor == null || selectedColor == Color.Transparent) HsvColor.from(Color.White) else HsvColor.from(selectedColor!!),
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
fun ColorPickerDialog_Preview_ColorFABs() {
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
fun ColorPickerDialog_Preview_ColorWheel() {
    MaterialTheme {

        ColorPickerDialog(
            initialColor = Color.Red.toArgb()+1,
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