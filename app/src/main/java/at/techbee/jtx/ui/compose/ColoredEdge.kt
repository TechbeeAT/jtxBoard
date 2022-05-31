/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.JtxBoardTheme

@Composable
fun ColoredEdge(colorItem: Int?, colorCollection: Int?) {

    Box {
        colorCollection?.let {
            Icon(
                painter = painterResource(R.drawable.ic_card_colored_edge),
                stringResource(R.string.color),
                modifier = Modifier.size(60.dp).alpha(0.5f),
                tint = Color(it),

            )
        }
        colorItem?.let {
            Icon(
                painter = painterResource(R.drawable.ic_card_colored_edge),
                stringResource(R.string.color),
                modifier = Modifier.size(30.dp).alpha(0.5f),
                tint = Color(it)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ColoredEdgePreview() {
    JtxBoardTheme {
        ColoredEdge(Color.Cyan.toArgb(), Color.Magenta.toArgb())
    }
}

