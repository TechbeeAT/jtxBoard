/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

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
import at.techbee.jtx.database.relations.ICal4ListWithRelatedto
import at.techbee.jtx.ui.compose.elements.ColoredEdge
import at.techbee.jtx.ui.compose.elements.ListStatusBar
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.Typography
import at.techbee.jtx.util.DateTimeUtils


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListCardKanban(
    iCalObjectWithRelatedto: ICal4ListWithRelatedto,
    modifier: Modifier = Modifier
) {

    val iCalObject = iCalObjectWithRelatedto.property
    val statusBarVisible by remember {
        mutableStateOf(
            iCalObject.numAttachments > 0 || iCalObject.numSubtasks > 0 || iCalObject.numSubnotes > 0 || iCalObject.isReadOnly || iCalObject.uploadPending || iCalObject.isRecurringInstance || iCalObject.isRecurringOriginal || iCalObject.isLinkedRecurringInstance
        )
    }


    ElevatedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {

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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (iCalObject.module == Module.TODO.name && iCalObject.due != null) {
                                    Text(
                                        iCalObject.getDueTextInfo(LocalContext.current) ?: "",
                                        style = Typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                text = iCalObject.summary ?: "",
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
                                text = iCalObject.description ?: "",
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    JtxBoardTheme {

        val icalobject = ICal4ListWithRelatedto.getSample().apply {
            property.dtstart = System.currentTimeMillis()
        }
        ListCardKanban(
            icalobject,
            modifier = Modifier
                .width(150.dp)
                .height(150.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardKanban_NOTE() {
    JtxBoardTheme {

        val icalobject = ICal4ListWithRelatedto.getSample().apply {
            property.component = Component.VJOURNAL.name
            property.module = Module.NOTE.name
            property.dtstart = null
            property.dtstartTimezone = null
            property.status = StatusJournal.CANCELLED.name
        }
        ListCardKanban(
            icalobject,
            modifier = Modifier.width(150.dp).height(150.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardKanban_TODO() {
    JtxBoardTheme {

        val icalobject = ICal4ListWithRelatedto.getSample().apply {
            property.component = Component.VTODO.name
            property.module = Module.TODO.name
            property.percent = 89
            property.status = StatusTodo.`IN-PROCESS`.name
            property.classification = Classification.CONFIDENTIAL.name
            property.dtstart = System.currentTimeMillis()
            property.due = System.currentTimeMillis()
            property.summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            property.numSubtasks = 5
            property.numAttachments = 4
            property.numSubnotes = 1
            property.uploadPending = true
            property.isReadOnly = true
        }
        ListCardKanban(
            icalobject
        )
    }
}
