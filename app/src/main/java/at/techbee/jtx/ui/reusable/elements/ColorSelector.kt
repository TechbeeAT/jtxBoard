/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.elements

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
 import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatColorReset
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController


@OptIn(ExperimentalStdlibApi::class)
@Composable
fun ColorSelector(
    initialColorInt: Int?,
    colorPickerController: ColorPickerController,
    modifier: Modifier = Modifier,
    additionalColorsInt: List<Int> = emptyList(),
) {

    val defaultColors = arrayListOf(
        Color.Unspecified,
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow,
        Color.Cyan,
        Color.Magenta,
        Color.LightGray
    )

    val additionalColors = additionalColorsInt.map { Color(it) }
    val initialColor = initialColorInt?.let { Color(it) }
    var hexTextValue: String? by remember { mutableStateOf(initialColorInt?.toHexString()) }

    fun getColorFromHex(hex: String?): Color? {
        return try {
            val hexTextValueWithHashtag = hex?.let { if(it.startsWith("#")) it else "#$it" }
            val androidColor = android.graphics.Color.parseColor(hexTextValueWithHashtag)
            Color(androidColor)
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: NullPointerException) {
            null
        }
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {

        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            SmallFloatingActionButton(
                modifier = Modifier
                    .padding(2.dp)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(16.dp)
                    ),
                containerColor = colorPickerController.selectedColor.value,
                onClick = { /* do nothing */ },
                content = {
                    if (colorPickerController.selectedColor.value == Color.Unspecified)
                        Icon(Icons.Outlined.FormatColorReset, null)
                }
            )

            Text(
                text = stringResource(R.string.selected_color),
                style = MaterialTheme.typography.labelMedium

            )
        }

        LazyRow {
            items(defaultColors) { color ->
                SmallFloatingActionButton(
                    modifier = Modifier
                        .padding(2.dp)
                        .border(
                            2.dp,
                            if (colorPickerController.selectedColor.value == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        ),
                    containerColor = if (color == Color.Unspecified) Color.White else color,
                    onClick = {
                        colorPickerController.selectByColor(color, true)
                    },
                    content = {
                        if (color == Color.Unspecified)
                            Icon(Icons.Outlined.FormatColorReset, null)
                    }
                )
            }
        }

        if(additionalColors.isNotEmpty()) {
            LazyRow {
                items(additionalColors) { color ->

                    SmallFloatingActionButton(
                        modifier = Modifier
                            .padding(2.dp)
                            .border(
                                2.dp,
                                if (colorPickerController.selectedColor.value == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            ),
                        containerColor = if (color == Color.Transparent) Color.White else color,
                        onClick = {
                            colorPickerController.selectByColor(color, true)
                            hexTextValue = color.value.toHexString()
                        },
                        content = {
                            if (color == Color.Transparent)
                                Icon(Icons.Outlined.FormatColorReset, null)
                        }
                    )
                }
            }
        }

        HsvColorPicker(
            controller = colorPickerController,
            onColorChanged = { colorEnvelope ->
                hexTextValue = colorEnvelope.color.toArgb().toHexString()
            },
            initialColor = initialColor,
            modifier = Modifier.padding(14.dp).height(250.dp)
        )

        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(35.dp),
            controller = colorPickerController,
            initialColor = initialColor
        )

        AlphaSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(35.dp),
            controller = colorPickerController,
            initialColor = initialColor
        )

        OutlinedTextField(
            value = hexTextValue?:"",
            onValueChange = {
                hexTextValue = it
                 getColorFromHex(it)?.let { color ->
                    colorPickerController.selectByColor(color, true)
                }
            },
            isError = getColorFromHex(hexTextValue) == null,
            leadingIcon = { Icon(Icons.Outlined.Numbers, null) },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ColorSelectorRow_Preview() {
    MaterialTheme {
        ColorSelector(
            initialColorInt = Color.Cyan.toArgb(),
            colorPickerController = rememberColorPickerController(),
            additionalColorsInt = listOf(Color.DarkGray.toArgb(), Color.Black.toArgb())
        )
    }
}