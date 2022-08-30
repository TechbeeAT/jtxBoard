/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.elements

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatColorReset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ColorSelectorRow(
    initialColor: Color?,
    onColorChanged: (Color?) -> Unit,
    modifier: Modifier = Modifier
) {

    val defaultColors = arrayListOf(
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow
    )

    var selectedColor by remember { mutableStateOf(initialColor) }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {


        SmallFloatingActionButton(
            modifier = Modifier
                .padding(2.dp)
                .border(2.dp, if(selectedColor == null) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(16.dp))
                .alpha(0.5f),
            containerColor = Color.White,
            onClick = {
                selectedColor = null
                onColorChanged(null)
                      },
            content = { Icon(Icons.Outlined.FormatColorReset, null) },
        )

        defaultColors.forEach { defaultColor ->
            SmallFloatingActionButton(
                modifier = Modifier
                    .padding(2.dp)
                    .border(2.dp, if(selectedColor == defaultColor) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(16.dp))
                    .alpha(0.5f),
                containerColor = defaultColor,
                onClick = {
                    selectedColor = defaultColor
                    onColorChanged(selectedColor) },
                content = { }
            )

        }


    }
}

@Preview(showBackground = true)
@Composable
fun ColorSelectorRow_preview() {
    MaterialTheme {
        ColorSelectorRow(Color.Magenta, { })
    }
}
