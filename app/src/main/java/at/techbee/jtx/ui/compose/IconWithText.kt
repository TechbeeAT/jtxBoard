/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.ui.theme.JtxBoardTheme

@Composable
fun IconWithText(icon: ImageVector, iconDesc: String, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Icon(icon, iconDesc)
        Text(text, modifier = Modifier.padding(start = 4.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun IconWithText_Preview() {
    JtxBoardTheme {
        IconWithText(Icons.Outlined.Group, "test", "4")
    }
}