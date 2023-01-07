/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.database.*
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.reusable.elements.ColoredEdge
import at.techbee.jtx.ui.reusable.elements.ListStatusBar
import at.techbee.jtx.ui.theme.Typography
import at.techbee.jtx.util.DateTimeUtils


@Composable
fun ListCardKanban(
    iCalObject: ICal4List,
    selected: Boolean,
    modifier: Modifier = Modifier
) {

    val statusBarVisible by remember {
        mutableStateOf(
            iCalObject.numAttachments > 0 || iCalObject.numSubtasks > 0 || iCalObject.numSubnotes > 0 || iCalObject.isReadOnly || iCalObject.uploadPending || iCalObject.isRecurringInstance || iCalObject.isRecurringOriginal || iCalObject.isLinkedRecurringInstance
        )
    }

    ElevatedCard(modifier = modifier) {

        Box {

            ColoredEdge(iCalObject.colorItem, iCalObject.colorCollection)

            Column(verticalArrangement = Arrangement.SpaceBetween) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 4.dp, start = 8.dp, end = 8.dp)
                            .fillMaxWidth(),
                    ) {

                        if (iCalObject.categories?.isNotEmpty() == true
                            || (iCalObject.module == Module.TODO.name && iCalObject.due != null)
                            || (iCalObject.module == Module.JOURNAL.name && iCalObject.dtstart != null)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                iCalObject.categories?.let {
                                    Text(
                                        it,
                                        style = Typography.labelMedium,
                                        fontStyle = FontStyle.Italic,
                                        modifier = Modifier
                                            .padding(end = 16.dp)
                                            .weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (iCalObject.module == Module.JOURNAL.name && iCalObject.dtstart != null) {
                                    Text(
                                        DateTimeUtils.convertLongToShortDateTimeString(
                                            iCalObject.dtstart,
                                            iCalObject.dtstartTimezone
                                        ),
                                        style = Typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Italic,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (iCalObject.module == Module.TODO.name && iCalObject.due != null) {
                                    Text(
                                        ICalObject.getDueTextInfo(due = iCalObject.due, dueTimezone = iCalObject.dueTimezone, percent = iCalObject.percent, context = LocalContext.current),
                                        style = Typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Italic,
                                        color = if(ICalObject.isOverdue(iCalObject.percent, iCalObject.due, iCalObject.dueTimezone) == true) MaterialTheme.colorScheme.error else LocalContentColor.current,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .weight(1f)
                ) {

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp)
                            .weight(1f)

                    ) {


                        if (iCalObject.summary?.isNotBlank() == true)
                            Text(
                                text = iCalObject.summary?.trim() ?: "",
                                textDecoration = if (iCalObject.status == StatusJournal.CANCELLED.name || iCalObject.status == StatusTodo.CANCELLED.name) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .weight(1f)
                            )

                        if (iCalObject.description?.isNotBlank() == true)
                            Text(
                                text = iCalObject.description?.trim() ?: "",
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                    }
                }

                AnimatedVisibility(visible = statusBarVisible) {
                    ListStatusBar(
                        numAttachments = iCalObject.numAttachments,
                        numSubtasks = iCalObject.numSubtasks,
                        numSubnotes = iCalObject.numSubnotes,
                        isReadOnly = iCalObject.isReadOnly,
                        uploadPending = iCalObject.uploadPending,
                        isRecurringOriginal = iCalObject.isRecurringOriginal,
                        isRecurringInstance = iCalObject.isRecurringInstance,
                        isLinkedRecurringInstance = iCalObject.isLinkedRecurringInstance,
                        component = iCalObject.component,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 4.dp).weight(0.2f)
                    )
                }
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
            selected = false,
            modifier = Modifier
                .width(150.dp)
                .height(150.dp)
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
            status = StatusJournal.CANCELLED.name
        }
        ListCardKanban(
            icalobject,
            selected = true,
            modifier = Modifier.width(150.dp).height(150.dp)
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
            status = StatusTodo.`IN-PROCESS`.name
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
            selected = false,
        )
    }
}
