/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.elements

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R

@Composable
fun FilterSection(
    icon: ImageVector,
    headline: String,
    onResetSelection: () -> Unit,
    onInvertSelection: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    ) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HeadlineWithIcon(
                icon = icon,
                iconDesc = headline,
                text = headline,
            )
            Row {
                IconButton(onClick = { onInvertSelection() }) {
                    Icon(Icons.Outlined.SwapHoriz, stringResource(id = R.string.invert_selection))
                }
                IconButton(onClick = { onResetSelection() }) {
                    Icon(Icons.Outlined.DeleteSweep, stringResource(id = R.string.delete))
                }
            }
        }
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun FilterSection_Preview() {
    MaterialTheme {
        FilterSection(
            icon = Icons.Outlined.Folder,
            headline = stringResource(id = R.string.collection),
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text("Here comes the content")
                } },
            onResetSelection = {  },
            onInvertSelection = {  })
    }
}