/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.theme.jtxCardBorderStrokeWidth


@Composable
fun ListCardWeek(
    iCalObject: ICal4List,
    categories: List<Category>,
    //resources: List<Resource>,
    //selected: Boolean,
    //progressUpdateDisabled: Boolean,
    //settingIsAccessibilityMode: Boolean,
    //markdownEnabled: Boolean,
    //player: MediaPlayer?,
    modifier: Modifier = Modifier,
    //onProgressChanged: (itemId: Long, newPercent: Int) -> Unit
) {
    Card(
        /*
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
         */
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(),
        border = iCalObject.colorItem?.let { BorderStroke(jtxCardBorderStrokeWidth, Color(it)) },
        modifier = modifier
    ) {
        Column(
            //verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {

            ListTopRowSimple(
                ical4List = iCalObject,
                categories = categories
            )


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (iCalObject.module == Module.TODO.name && iCalObject.dtstart != null) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_widget_start),
                        contentDescription = stringResource(id = R.string.started),
                        modifier = Modifier.size(12.dp)
                    )

                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (iCalObject.module == Module.TODO.name && iCalObject.due != null) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_widget_due),
                        contentDescription = stringResource(id = R.string.due),
                        tint = if (ICalObject.isOverdue(
                                iCalObject.status,
                                iCalObject.percent,
                                iCalObject.due,
                                iCalObject.dueTimezone
                            ) == true
                        ) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(12.dp)
                    )

                    Text(
                        text = "8.00 pm",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            Text(
                text = if(iCalObject.summary?.isNotBlank() == true) iCalObject.summary!!.trim() else iCalObject.description ?: "",
                textDecoration = if (iCalObject.status == Status.CANCELLED.status) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            /*

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {

                Text(
                    text = if(iCalObject.summary?.isNotBlank() == true) iCalObject.summary!!.trim() else iCalObject.description ?: "",
                    textDecoration = if (iCalObject.status == Status.CANCELLED.status) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .weight(1f)
                )


                if (iCalObject.module == Module.TODO.name)
                    Checkbox(
                        checked = iCalObject.percent == 100 || iCalObject.status == Status.COMPLETED.status,
                        enabled = !iCalObject.isReadOnly && !progressUpdateDisabled,
                        onCheckedChange = {
                            /*
                            onProgressChanged(
                                iCalObject.id,
                                if (it) 100 else 0
                            )

                             */
                        }
                    )
            }

             */
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardWeek_JOURNAL() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            dtstart = System.currentTimeMillis()
            colorCollection = Color.Green.toArgb()
            colorItem = Color.Magenta.toArgb()
        }
        ListCardWeek(
            icalobject,
            categories = emptyList(),
            //resources = emptyList(),
            //progressUpdateDisabled = false
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ListCardWeek_NOTE() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VJOURNAL.name
            module = Module.NOTE.name
            dtstart = null
            dtstartTimezone = null
            status = Status.CANCELLED.status
        }
        ListCardWeek(
            icalobject,
            categories = emptyList(),
            //resources = emptyList(),
            //progressUpdateDisabled = false,
            modifier = Modifier.width(150.dp)
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ListCardWeek_TODO() {
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
        ListCardWeek(
            icalobject,
            categories = emptyList(),
            //resources = emptyList(),
            //progressUpdateDisabled = false
        )
    }
}

/*

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
            storedCategories = listOf(StoredCategory("Test", Color.Cyan.toArgb())),
            storedResources = listOf(StoredResource("Projector", Color.Green.toArgb())),
            storedStatuses = listOf(ExtendedStatus("Individual", Module.JOURNAL, Status.FINAL, Color.Green.toArgb())),
            selected = false,
            progressUpdateDisabled = false,
            settingIsAccessibilityMode = false,
            markdownEnabled = false,
            player = null,
            onProgressChanged = { _, _ -> }, modifier = Modifier.width(150.dp)
        )
    }
}
 */