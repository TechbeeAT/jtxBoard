/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.ui.theme.JtxBoardTheme

@Composable
fun ColorSelectorRow(
    selectedColor: Color,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier
) {

    val defaultColors = arrayListOf(Color.Cyan, Color.Magenta, Color.Blue, Color.Green, Color.Red, Color.Yellow)

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {


        /*
                                color = collectionColor?.let { Color(it) }?: Color.White,
                        harmonyMode = ColorHarmonyMode.NONE,
                        modifier = Modifier.size(300.dp),
                        onColorChanged = { hsvColor ->
                            collectionColor = hsvColor.toColor().toArgb()
         */

        defaultColors.forEach { defaultColor ->
            SmallFloatingActionButton(
                modifier = Modifier.padding(2.dp).alpha(0.5f),
                containerColor = defaultColor,
                onClick = { onColorChanged(defaultColor) },
                content = {
                    if(selectedColor == defaultColor)
                        Icon(Icons.Outlined.Check, null)
                }
            )

        }


    }
}

@Preview(showBackground = true)
@Composable
fun ColorSelectorRow_preview() {
    JtxBoardTheme {
        ColorSelectorRow(Color.Magenta, { })
    }
}
