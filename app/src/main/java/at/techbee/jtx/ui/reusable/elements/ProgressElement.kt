/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R

@Composable
fun ProgressElement(
    iCalObjectId: Long,
    progress: Int?,
    isReadOnly: Boolean,
    isLinkedRecurringInstance: Boolean,
    sliderIncrement: Int,
    modifier: Modifier = Modifier,
    showProgressLabel: Boolean = true,
    showSlider: Boolean = true,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit
) {

    var sliderPosition by remember { mutableStateOf(
        progress?.let { ((it / sliderIncrement) * sliderIncrement).toFloat() } ?: 0f
    ) }

    /**
     * When the Element gets reused/recycled e.g. when activating the option hide completed in the
     * list view, the element would disappear and the next icalobject would take its place.
     * But remember would remember the previous state and would show the wrong progress/checked state.
     * Here we make sure, that if the icalobjectid of the current instance has changed, the progress
     * gets reset
     */
    val lastICalObjectId = remember { mutableStateOf(0L) }
    if(lastICalObjectId.value != iCalObjectId) {
        lastICalObjectId.value = iCalObjectId
        sliderPosition = progress?.let { ((it / sliderIncrement) * sliderIncrement).toFloat() } ?: 0f
    }

    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {


        if(showProgressLabel) {
            Text(
                stringResource(id = R.string.progress),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if(showSlider) {
            Slider(
                value = sliderPosition,
                valueRange = 0F..100F,
                steps = (100 / sliderIncrement) - 1,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = {
                    sliderPosition = sliderPosition / sliderIncrement * sliderIncrement
                    onProgressChanged(
                        iCalObjectId,
                        (sliderPosition / sliderIncrement * sliderIncrement).toInt(),
                        isLinkedRecurringInstance
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = !isReadOnly
            )
            Text(
                text = String.format("%.0f%%", sliderPosition),
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(horizontal = 8.dp).width(50.dp)
            )
        }
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
            progress = 100,
            isReadOnly = false,
            isLinkedRecurringInstance = false,
            onProgressChanged = { _, _, _ -> },
            sliderIncrement = 20
        )

    }
}

@Preview(showBackground = true)
@Composable
fun ProgressElementPreview_without_label() {
    MaterialTheme {
        ProgressElement(
            iCalObjectId = 1L,
            progress = 8,
            isReadOnly = false,
            isLinkedRecurringInstance = false,
            onProgressChanged = { _, _, _ -> },
            sliderIncrement = 1,
            showProgressLabel = false
        )

    }
}

@Preview(showBackground = true)
@Composable
fun ProgressElementPreview_without_label_and_slider() {
    MaterialTheme {
        ProgressElement(
            iCalObjectId = 1L,
            progress = 100,
            isReadOnly = false,
            isLinkedRecurringInstance = false,
            onProgressChanged = { _, _, _ -> },
            sliderIncrement = 1,
            showProgressLabel = false,
            showSlider = false
        )

    }
}