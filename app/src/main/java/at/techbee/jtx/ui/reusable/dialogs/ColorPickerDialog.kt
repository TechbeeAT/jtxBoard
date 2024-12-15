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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.elements.ColorSelector
import com.github.skydoves.colorpicker.compose.rememberColorPickerController


@Composable
fun ColorPickerDialog(
    initialColorInt: Int?,
    onColorChanged: (Int?) -> Unit,
    onDismiss: () -> Unit,
    additionalColorsInt: List<Int> = emptyList()
    ) {

    val colorPickerController = rememberColorPickerController()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(id = R.string.color)) },
        properties = DialogProperties(usePlatformDefaultWidth = false),   // Workaround due to Google Issue: https://issuetracker.google.com/issues/194911971?pli=1
        text = {

            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top) {

                ColorSelector(
                    initialColorInt = initialColorInt,
                    colorPickerController = colorPickerController,
                    additionalColorsInt = additionalColorsInt
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onColorChanged(
                        if(colorPickerController.selectedColor.value == Color.Unspecified)
                            null
                        else
                            colorPickerController.selectedColor.value.toArgb())
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
            initialColorInt = Color.Cyan.toArgb(),
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
            initialColorInt = Color.Red.toArgb()+1,
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
            initialColorInt = null,
            onColorChanged = { },
            onDismiss = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ColorPickerDialog_Preview_additional_colors() {
    MaterialTheme {

        ColorPickerDialog(
            initialColorInt = null,
            onColorChanged = { },
            onDismiss = { },
            additionalColorsInt = listOf(Color.DarkGray.toArgb(), Color.Black.toArgb())
        )
    }
}