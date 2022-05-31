/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.JtxBoardTheme

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressElement(
    iCalObjectId: Long,
    progress: Int?,
    isReadOnly: Boolean,
    isLinkedRecurringInstance: Boolean,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        //use progress in state!
        var sliderPosition by mutableStateOf(progress?.toFloat() ?: 0f)

        Text(
            stringResource(id = R.string.progress),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )
        Slider(
            value = sliderPosition,
            valueRange = 0F..100F,
            steps = 100,
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = { onProgressChanged(iCalObjectId, sliderPosition.toInt(), isLinkedRecurringInstance) },
            modifier = Modifier.weight(1f),
            enabled = !isReadOnly
        )
        Text(
            String.format("%.0f%%", sliderPosition),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )
        Checkbox(
            checked = sliderPosition==100f,
            onCheckedChange = {
                sliderPosition = if(it) 100f else 0f
                onProgressChanged(iCalObjectId, if(it) 100 else 0, isLinkedRecurringInstance)
            },
            enabled = !isReadOnly
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressElementPreview() {
    JtxBoardTheme {
        ProgressElement(1L, 57, isReadOnly = false, isLinkedRecurringInstance = false) { _, _, _ -> }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressElementPreview_readonly() {
    JtxBoardTheme {
        ProgressElement(1L, 57, isReadOnly = true, isLinkedRecurringInstance = false) { _, _, _ -> }
    }
}