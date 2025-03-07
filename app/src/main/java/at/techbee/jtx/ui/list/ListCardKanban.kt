/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import android.media.MediaPlayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.locals.ExtendedStatus
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.elements.AudioPlaybackElement
import at.techbee.jtx.ui.theme.jtxCardBorderStrokeWidth
import com.arnyminerz.markdowntext.MarkdownText


@Composable
fun ListCardKanban(
    iCalObject: ICal4List,
    storedCategories: List<StoredCategory>,
    storedStatuses: List<ExtendedStatus>,
    markdownEnabled: Boolean,
    selected: Boolean,
    player: MediaPlayer?,
    modifier: Modifier = Modifier
) {

    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else CardDefaults.elevatedCardColors().containerColor,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else CardDefaults.elevatedCardColors().contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(),
        border = iCalObject.colorItem?.let { BorderStroke(jtxCardBorderStrokeWidth, Color(it)) },
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.Top),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {

            ListTopRowSimple(
                ical4List = iCalObject,
                storedCategories = storedCategories,
                extendedStatusesAll = storedStatuses
            )

            iCalObject.getAudioAttachmentAsUri()?.let {
                AudioPlaybackElement(
                    uri = it,
                    player = player,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 4.dp)
                )
            }

            if (iCalObject.summary?.isNotBlank() == true)
                Text(
                    text = iCalObject.summary?.trim() ?: "",
                    textDecoration = if (iCalObject.status == Status.CANCELLED.status) TextDecoration.LineThrough else null,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )

            if (iCalObject.description?.isNotBlank() == true) {
                if(markdownEnabled)
                    MarkdownText(
                        markdown = iCalObject.description?.trim() ?: "",
                        maxLines = 4,
                        textDecoration = if (iCalObject.status == Status.CANCELLED.status) TextDecoration.LineThrough else null,
                        overflow = TextOverflow.Ellipsis
                    )
                else
                    Text(
                        text = iCalObject.description?.trim() ?: "",
                        maxLines = 4,
                        textDecoration = if (iCalObject.status == Status.CANCELLED.status) TextDecoration.LineThrough else null,
                        overflow = TextOverflow.Ellipsis
                    )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardKanban_JOURNAL() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            dtstart = System.currentTimeMillis()
        }
        ListCardKanban(
            icalobject,
            storedCategories = emptyList(),
            storedStatuses = emptyList(),
            selected = false,
            markdownEnabled = false,
            player = null,
            modifier = Modifier
                .width(150.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardKanban_NOTE() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VJOURNAL.name
            module = Module.NOTE.name
            dtstart = null
            dtstartTimezone = null
            status = Status.CANCELLED.status
            categories = "Pokemon"
        }
        ListCardKanban(
            icalobject,
            storedCategories = emptyList(),
            storedStatuses = emptyList(),
            selected = true,
            markdownEnabled = false,
            player = null,
            modifier = Modifier
                .width(150.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardKanban_TODO() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.CONFIDENTIAL.name
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            numSubtasks = 5
            numAttachments = 4
            numSubnotes = 1
            uploadPending = true
            isReadOnly = true
        }
        ListCardKanban(
            icalobject,
            storedCategories = emptyList(),
            storedStatuses = emptyList(),
            selected = false,
            markdownEnabled = false,
            player = null,
        )
    }
}
