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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R

@Composable
fun ProgressElement(
    label: String?,
    iCalObjectId: Long,
    progress: Int?,
    isReadOnly: Boolean,
    isLinkedRecurringInstance: Boolean,
    sliderIncrement: Int,
    modifier: Modifier = Modifier,
    showSlider: Boolean = true,
    onProgressChanged: (itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) -> Unit
) {

    var sliderPosition by remember { mutableStateOf(
        progress?.let { ((it / sliderIncrement) * sliderIncrement).toFloat() } ?: 0f
    ) }

    //If the progress gets updated in the meantime in the DB, we should get aware and udpate the UI
    var sliding by remember { mutableStateOf(false) }
    var lastKnownProgress by remember { mutableStateOf(progress) }
    if(!sliding && sliderPosition != (progress?.toFloat()?:0f) && progress != lastKnownProgress) {
        sliderPosition = progress?.toFloat() ?: 0f
        lastKnownProgress = progress
    }

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

        Text(
            text = label ?: if (showSlider) stringResource(id = R.string.progress) else stringResource(id = R.string.completed),
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if(label == null && !showSlider) TextAlign.End else null
        )

        if(showSlider) {
            Slider(
                value = sliderPosition,
                valueRange = 0F..100F,
                steps = (100 / sliderIncrement) - 1,
                onValueChange = {
                    sliderPosition = it
                    sliding = true
                                },
                onValueChangeFinished = {
                    sliderPosition = sliderPosition / sliderIncrement * sliderIncrement
                    sliding = false
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
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(50.dp)
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
            label = null,
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
            label = null,
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
fun ProgressElementPreview_increment25_with_label() {
    MaterialTheme {
        ProgressElement(
            label = "could be a subtask",
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
            label = null,
            iCalObjectId = 1L,
            progress = 8,
            isReadOnly = false,
            isLinkedRecurringInstance = false,
            onProgressChanged = { _, _, _ -> },
            sliderIncrement = 1,
        )

    }
}

@Preview(showBackground = true)
@Composable
fun ProgressElementPreview_without_label_and_slider() {
    MaterialTheme {
        ProgressElement(
            label = null,
            iCalObjectId = 1L,
            progress = 100,
            isReadOnly = false,
            isLinkedRecurringInstance = false,
            onProgressChanged = { _, _, _ -> },
            sliderIncrement = 1,
            showSlider = false
        )

    }
}