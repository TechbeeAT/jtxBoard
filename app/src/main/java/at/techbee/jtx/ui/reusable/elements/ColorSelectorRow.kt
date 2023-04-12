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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatColorReset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp



@Composable
fun ColorSelectorRow(
    selectedColor: Color?,
    modifier: Modifier = Modifier,
    onColorChanged: (Color?) -> Unit
) {

    val defaultColors = arrayListOf(
        Color.Transparent,
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow,
        Color.Cyan,
        Color.Magenta,
        Color.LightGray
    )


    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        LazyRow {
            items(defaultColors) { color ->
                SmallFloatingActionButton(
                    modifier = Modifier
                        .padding(2.dp)
                        .border(
                            2.dp,
                            if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        ),
                    containerColor = if (color == Color.Transparent) Color.White else color,
                    onClick = {
                        onColorChanged(if (color == Color.Transparent) null else color)
                    },
                    content = {
                        if (color == Color.Transparent)
                            Icon(Icons.Outlined.FormatColorReset, null)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ColorSelectorRow_Preview() {
    MaterialTheme {
        ColorSelectorRow(
            selectedColor = Color.Cyan,
            onColorChanged = { }
        )
    }
}