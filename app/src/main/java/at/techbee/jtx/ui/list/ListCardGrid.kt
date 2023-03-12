/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.views.ICal4List


@Composable
fun ListCardGrid(
    iCalObject: ICal4List,
    categories: List<Category>,
    resources: List<Resource>,
    selected: Boolean,
    progressUpdateDisabled: Boolean,
    modifier: Modifier = Modifier,
    onProgressChanged: (itemId: Long, newPercent: Int) -> Unit
) {
    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(),
        border = iCalObject.colorItem?.let { BorderStroke(1.dp, Color(it)) },
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            modifier = Modifier.padding(8.dp)
        ) {

            ListTopFlowRow(
                ical4List = iCalObject,
                categories = categories,
                resources = resources,
                includeJournalDate = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {

                if (iCalObject.summary?.isNotBlank() == true)
                    Text(
                        text = iCalObject.summary?.trim() ?: "",
                        textDecoration = if (iCalObject.status == Status.CANCELLED.status) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .weight(1f)
                    )

                if (iCalObject.module == Module.TODO.name)
                    Checkbox(
                        checked = iCalObject.percent == 100,
                        enabled = !iCalObject.isReadOnly && !progressUpdateDisabled,
                        onCheckedChange = {
                            onProgressChanged(
                                iCalObject.id,
                                if (it) 100 else 0
                            )
                        }
                    )
            }

            if (iCalObject.description?.isNotBlank() == true)
                Text(
                    text = iCalObject.description?.trim() ?: "",
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp)
                )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardGrid_JOURNAL() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            dtstart = System.currentTimeMillis()
            colorCollection = Color.Green.toArgb()
            colorItem = Color.Magenta.toArgb()
        }
        ListCardGrid(
            icalobject,
            categories = emptyList(),
            resources = emptyList(),
            selected = false,
            progressUpdateDisabled = false,
            onProgressChanged = { _, _ -> }, modifier = Modifier
                .width(150.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardGrid_NOTE() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VJOURNAL.name
            module = Module.NOTE.name
            dtstart = null
            dtstartTimezone = null
            status = Status.CANCELLED.status
        }
        ListCardGrid(
            icalobject,
            categories = emptyList(),
            resources = emptyList(),
            selected = true,
            progressUpdateDisabled = false,
            onProgressChanged = { _, _ -> },
            modifier = Modifier.width(150.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardGrid_TODO() {
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
        ListCardGrid(
            icalobject,
            categories = emptyList(),
            resources = emptyList(),
            selected = false,
            progressUpdateDisabled = false,
            onProgressChanged = { _, _ -> }, modifier = Modifier.width(150.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListCardGrid_TODO_short() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.CONFIDENTIAL.classification
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary = "Lorem"
            description = null
            numSubtasks = 5
            numAttachments = 4
            numSubnotes = 1
            uploadPending = true
            isReadOnly = true
            categories = "Simpsons"
        }
        ListCardGrid(
            icalobject,
            categories = emptyList(),
            resources = emptyList(),
            selected = false,
            progressUpdateDisabled = false,
            onProgressChanged = { _, _ -> }, modifier = Modifier.width(150.dp)
        )
    }
}
