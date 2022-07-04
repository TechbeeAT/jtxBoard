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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressElement(
    iCalObjectId: Long,
    progress: Int?,
    isReadOnly: Boolean,
    isLinkedRecurringInstance: Boolean,
    sliderIncrement: Int,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit
) {

    val initialProgress = progress?.let { ((it / sliderIncrement) * sliderIncrement).toFloat() } ?: 0f
    var sliderPosition by remember { mutableStateOf(initialProgress) }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {


        Text(
            stringResource(id = R.string.progress),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )
        Slider(
            value = sliderPosition,
            valueRange = 0F..100F,
            steps = (100 / sliderIncrement)-1,
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = {
                sliderPosition = sliderPosition/sliderIncrement*sliderIncrement
                onProgressChanged(
                    iCalObjectId,
                    (sliderPosition/sliderIncrement*sliderIncrement).toInt(),
                    isLinkedRecurringInstance
                )
            },
            modifier = Modifier.weight(1f),
            enabled = !isReadOnly
        )
        Text(
            String.format("%.0f%%", sliderPosition),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )
        Checkbox(
            checked = sliderPosition == 100f,
            onCheckedChange = {
                sliderPosition = if (it) 100f else 0f     // update comes from state change!
                onProgressChanged(iCalObjectId, if (it) 100 else 0, isLinkedRecurringInstance)
            },
            enabled = !isReadOnly
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressElementPreview() {
    MaterialTheme {
        ProgressElement(
            iCalObjectId = 1L,
            progress = 57,
            isReadOnly = false,
            isLinkedRecurringInstance = false,
            sliderIncrement = 50,
            onProgressChanged = { _, _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressElementPreview_readonly() {
    MaterialTheme {
        ProgressElement(
            iCalObjectId = 1L,
            progress = 57,
            isReadOnly = true,
            isLinkedRecurringInstance = false,
            sliderIncrement = 5,
            onProgressChanged = { _, _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressElementPreview_increment25() {
    MaterialTheme {
        ProgressElement(
            iCalObjectId = 1L,
            progress = 57,
            isReadOnly = false,
            isLinkedRecurringInstance = false,
            onProgressChanged = { _, _, _ -> },
            sliderIncrement = 20
        )

    }
}