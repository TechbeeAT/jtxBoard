/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.ui.reusable.dialogs.DetachFromSeriesDialog
import at.techbee.jtx.ui.reusable.dialogs.RecurDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.util.DateTimeUtils
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.Recur.Frequency


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsCardRecur(
    icalObject: ICalObject,
    isReadOnly: Boolean,
    seriesInstances: List<ICalObject>,
    seriesElement: ICalObject?,
    hasChildren: Boolean,
    onRecurUpdated: (Recur?) -> Unit,
    goToDetail: (itemId: Long, editMode: Boolean, list: List<Long>) -> Unit,
    unlinkFromSeries: (instances: List<ICalObject>, series: ICalObject?, deleteAfterUnlink: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    var showRecurDialog by rememberSaveable { mutableStateOf(false) }
    var showDetachSingleFromSeriesDialog by rememberSaveable { mutableStateOf(false) }
    var showDetachAllFromSeriesDialog by rememberSaveable { mutableStateOf(false) }


    if (showDetachSingleFromSeriesDialog) {
        DetachFromSeriesDialog(
            detachAll = false,
            onConfirm = { unlinkFromSeries(listOf(icalObject), null, false) },
            onDismiss = { showDetachSingleFromSeriesDialog = false }
        )
    }

    if (showDetachAllFromSeriesDialog) {
        DetachFromSeriesDialog(
            detachAll = true,
            onConfirm = { unlinkFromSeries(seriesInstances, seriesElement, true) },
            onDismiss = { showDetachAllFromSeriesDialog = false }
        )
    }

    if(showRecurDialog && icalObject.dtstart != null) {
        RecurDialog(
            dtstart = icalObject.dtstart!!,
            dtstartTimezone = icalObject.dtstartTimezone,
            onRecurUpdated = onRecurUpdated,
            onDismiss = { showRecurDialog = false }
        )
    }

    ElevatedCard(
        onClick = {
            if(icalObject.dtstart != null && icalObject.recurid == null && !isReadOnly)
                showRecurDialog = true
        },
        modifier = modifier
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HeadlineWithIcon(
                    icon = Icons.Outlined.EventRepeat,
                    iconDesc = null,
                    text = stringResource(id = R.string.recurrence)
                )
            }

            icalObject.getRecur()?.let { recur ->
                if(recur.experimentalValues?.isNotEmpty() == true
                    || recur.hourList?.isNotEmpty() == true
                    || recur.minuteList?.isNotEmpty() == true
                    || recur.monthList?.isNotEmpty() == true
                    || recur.secondList?.isNotEmpty() == true
                    || recur.setPosList?.isNotEmpty() == true
                    || recur.skip != null
                    || recur.weekNoList?.isNotEmpty() == true
                    || recur.weekStartDay != null
                    || recur.yearDayList?.isNotEmpty() == true
                    || (recur.monthDayList?.size?:0) > 1
                ) {
                    Text(stringResource(id = R.string.details_recur_unknown_rrule_dialog_message))
                }
            }

            AnimatedVisibility(icalObject.dtstart == null) {
                Text(
                    text = stringResource(id = R.string.edit_recur_toast_requires_start_date),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            AnimatedVisibility(icalObject.dtstart != null && icalObject.recurid == null && icalObject.rrule == null) {
                Text(
                    text = stringResource(id = R.string.recur_not_set),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if(icalObject.recurid != null) {
                Text(
                    text = stringResource(id = if (icalObject.sequence == 0L) R.string.details_unchanged_part_of_series else R.string.details_changed_part_of_series),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            AnimatedVisibility(hasChildren) {
                Text(
                    text = stringResource(id = R.string.details_series_attention_subentries),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            FlowRow(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if(!icalObject.recurid.isNullOrEmpty()) {
                    Button(
                        onClick = {
                            seriesElement?.id?.let { goToDetail(it, false, emptyList()) }
                        }
                    ) {
                        Text(stringResource(id = R.string.details_go_to_series))
                    }

                    Button(
                        onClick = { showDetachSingleFromSeriesDialog = true }
                    ) {
                        Text(stringResource(id = R.string.details_detach_from_series))
                    }
                }

                if(seriesInstances.isNotEmpty() && icalObject.recurid == null) {
                    Button(
                        onClick = { showDetachAllFromSeriesDialog = true }
                    ) {
                        Text(stringResource(id = R.string.details_detach_all_instances))
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                var showAllInstances by remember { mutableStateOf(false) }

                if(showAllInstances) {
                    seriesInstances.forEach { instance ->
                        ElevatedCard(
                            onClick = {
                                goToDetail(instance.id, false, seriesInstances.map { it.id })
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = DateTimeUtils.convertLongToFullDateTimeString(
                                        instance.dtstart,
                                        instance.dtstartTimezone
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                if (instance.sequence == 0L) {
                                    Icon(
                                        Icons.Outlined.EventRepeat,
                                        stringResource(R.string.list_item_recurring),
                                        modifier = Modifier
                                            .size(14.dp)
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_recur_exception),
                                        stringResource(R.string.list_item_edited_recurring),
                                        modifier = Modifier
                                            .size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                } else if(icalObject.rrule != null && seriesInstances.isNotEmpty()) {
                    TextButton(onClick = { showAllInstances = true }) {
                        Text(stringResource(id = R.string.details_show_all_instances, seriesInstances.size))
                    }
                }

                val exceptions = DateTimeUtils.getLongListfromCSVString(icalObject.exdate)
                if(exceptions.isNotEmpty())
                    Text(
                        text = stringResource(id = R.string.recurrence_exceptions),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp)
                    )

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    exceptions.forEach { exception ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = DateTimeUtils.convertLongToFullDateTimeString(exception, icalObject.dtstartTimezone),
                                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                                style = TextStyle(textDecoration = TextDecoration.LineThrough)
                            )
                        }
                    }
                }

                val additions = DateTimeUtils.getLongListfromCSVString(icalObject.rdate)
                if(additions.isNotEmpty())
                    Text(
                        text = stringResource(id = R.string.recurrence_additions),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp)
                    )
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    additions.forEach { addition ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = DateTimeUtils.convertLongToFullDateTimeString(addition, icalObject.dtstartTimezone),
                                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun DetailsCardRecur_Preview() {
    MaterialTheme {

        val recur = Recur
            .Builder()
            .count(5)
            .frequency(Frequency.WEEKLY)
            .interval(2)
            .build()

        DetailsCardRecur(
            icalObject = ICalObject.createTodo().apply {
                dtstart = System.currentTimeMillis()
                dtstartTimezone = null
                due = System.currentTimeMillis()
                dueTimezone = null
                rrule = recur.toString()
                exdate = "1661890454701,1661990454701"
                rdate = "1661890454701,1661990454701"
            },
            seriesInstances = listOf(
                ICalObject.createTodo().apply {
                    dtstart = System.currentTimeMillis()
                    dtstartTimezone = null
                    due = System.currentTimeMillis()
                    dueTimezone = null
                    rrule = "123"
                }
            ),
            seriesElement = null,
            isReadOnly = false,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            unlinkFromSeries = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardRecur_Preview_read_only2() {
    MaterialTheme {

        val recur = Recur
            .Builder()
            .count(5)
            .frequency(Frequency.WEEKLY)
            .interval(2)
            .build()

        DetailsCardRecur(
            icalObject = ICalObject.createTodo().apply {
                dtstart = System.currentTimeMillis()
                dtstartTimezone = null
                due = System.currentTimeMillis()
                dueTimezone = null
                rrule = recur.toString()
            },
            seriesInstances = listOf(
                ICalObject.createTodo().apply {
                    dtstart = System.currentTimeMillis()
                    dtstartTimezone = null
                    due = System.currentTimeMillis()
                    dueTimezone = null
                    rrule = "123"
                }
            ),
            seriesElement = null,
            isReadOnly = true,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            unlinkFromSeries = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardRecur_Preview_unchanged_recur() {
    MaterialTheme {

        DetailsCardRecur(
            icalObject = ICalObject.createTodo().apply {
                dtstart = System.currentTimeMillis()
                dtstartTimezone = null
                due = System.currentTimeMillis()
                dueTimezone = null
                recurid = "uid"
                sequence = 0L
            },
            seriesInstances = listOf(
                ICalObject.createTodo().apply {
                    dtstart = System.currentTimeMillis()
                    dtstartTimezone = null
                    due = System.currentTimeMillis()
                    dueTimezone = null
                    rrule = "123"
                }
            ),
            seriesElement = null,
            isReadOnly = false,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            unlinkFromSeries = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardRecur_Preview_changed_recur() {
    MaterialTheme {

        DetailsCardRecur(
            icalObject = ICalObject.createTodo().apply {
                dtstart = System.currentTimeMillis()
                dtstartTimezone = null
                due = System.currentTimeMillis()
                dueTimezone = null
                recurid = "uid"
                sequence = 1L
            },
            seriesInstances = listOf(
                ICalObject.createTodo().apply {
                    dtstart = System.currentTimeMillis()
                    dtstartTimezone = null
                    due = System.currentTimeMillis()
                    dueTimezone = null
                    rrule = "123"
                }
            ),
            seriesElement = null,
            isReadOnly = false,
            hasChildren = true,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            unlinkFromSeries = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardRecur_Preview_off() {
    MaterialTheme {

        DetailsCardRecur(
            icalObject = ICalObject.createTodo().apply {
                dtstart = System.currentTimeMillis()
                dtstartTimezone = null
                due = System.currentTimeMillis()
                dueTimezone = null
            },
            seriesInstances = listOf(
                ICalObject.createTodo().apply {
                    dtstart = System.currentTimeMillis()
                    dtstartTimezone = null
                    due = System.currentTimeMillis()
                    dueTimezone = null
                    rrule = "123"
                }
            ),
            seriesElement = null,
            isReadOnly = false,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            unlinkFromSeries = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardRecur_Preview_read_only() {
    MaterialTheme {

        DetailsCardRecur(
            icalObject = ICalObject.createTodo().apply {
                dtstart = System.currentTimeMillis()
                dtstartTimezone = null
                due = System.currentTimeMillis()
                dueTimezone = null
            },
            seriesInstances = listOf(
                ICalObject.createTodo().apply {
                    dtstart = System.currentTimeMillis()
                    dtstartTimezone = null
                    due = System.currentTimeMillis()
                    dueTimezone = null
                    rrule = "123"
                }
            ),
            seriesElement = null,
            isReadOnly = true,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            unlinkFromSeries = { _, _, _ -> }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardRecur_Preview_view_no_dtstart() {
    MaterialTheme {

        DetailsCardRecur(
            icalObject = ICalObject.createTodo().apply {
                dtstart = null
                dtstartTimezone = null
                due = null
                dueTimezone = null
            },
            seriesInstances = emptyList(),
            seriesElement = null,
            isReadOnly = false,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            unlinkFromSeries = { _, _, _ -> }
        )
    }
}