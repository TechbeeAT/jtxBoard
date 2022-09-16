/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import at.techbee.jtx.ui.compose.dialogs.DatePickerDialog
import at.techbee.jtx.ui.compose.elements.HeadlineWithIcon
import at.techbee.jtx.util.DateTimeUtils
import net.fortuna.ical4j.model.Date
import net.fortuna.ical4j.model.NumberList
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.WeekDay
import java.time.DayOfWeek
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsCardRecur(
    icalObject: ICalObject,
    isEditMode: Boolean,
    onRecurUpdated: (Recur?) -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.recurrence)
    var updatedRRule by rememberSaveable { mutableStateOf(icalObject.getRecur()) }

    var frequencyExpanded by rememberSaveable { mutableStateOf(false) }
    var intervalExpanded by rememberSaveable { mutableStateOf(false) }
    var monthDayListExpanded by rememberSaveable { mutableStateOf(false) }
    var endAfterExpaneded by rememberSaveable { mutableStateOf(false) }
    var endsExpanded by rememberSaveable { mutableStateOf(false) }

    var showDatepicker by rememberSaveable { mutableStateOf(false) }

    if (showDatepicker) {
        DatePickerDialog(
            datetime = updatedRRule?.until?.time ?: icalObject.dtstart ?: System.currentTimeMillis(),
            timezone = TZ_ALLDAY,
            dateOnly = true,
            allowNull = false,
            onConfirm = { datetime, _ ->
                updatedRRule = Recur.Builder().apply {
                    interval(updatedRRule?.interval ?: 1)
                    //if((updatedRRule?.count ?: -1) > 0)
                    //    count(updatedRRule?.count)
                    if(updatedRRule?.frequency != null)
                        frequency(updatedRRule?.frequency ?: Recur.Frequency.DAILY)
                    until(Date(datetime!!))
                    updatedRRule?.dayList?.let { dayList(it) }
                    updatedRRule?.monthDayList?.let { monthDayList(it) }
                }.build()
                onRecurUpdated(updatedRRule)
            },
            onDismiss = { showDatepicker = false }
        )
    }


    ElevatedCard(modifier = modifier) {

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
                    iconDesc = headline,
                    text = headline
                )

                AnimatedVisibility(isEditMode) {
                    Switch(
                        checked = updatedRRule != null,
                        enabled = icalObject.dtstart != null,
                        onCheckedChange = {
                            updatedRRule = if (it)
                                Recur("FREQ=DAILY;COUNT=1;INTERVAL=1")
                            else
                                null
                            onRecurUpdated(updatedRRule)
                        }
                    )
                }
            }

            AnimatedVisibility(isEditMode && icalObject.dtstart == null) {
                Text(stringResource(id = R.string.edit_recur_toast_requires_start_date))
            }

            AnimatedVisibility(isEditMode && updatedRRule != null) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {


                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        Text(stringResource(id = R.string.edit_recur_repeat_every_x))

                        AssistChip(
                            onClick = { intervalExpanded = true },
                            label = {
                                Text(
                                    if((updatedRRule?.interval ?: -1) < 1)
                                        "1"
                                    else
                                        updatedRRule?.interval?.toString() ?: "1"
                                )

                                DropdownMenu(
                                    expanded = intervalExpanded,
                                    onDismissRequest = { intervalExpanded = false }
                                ) {
                                    for (number in 1..100) {
                                        DropdownMenuItem(
                                            onClick = {
                                                updatedRRule = Recur.Builder().apply {
                                                    interval(number)
                                                    if((updatedRRule?.count ?: -1) > 0)
                                                        count(updatedRRule?.count)
                                                    if(updatedRRule?.frequency != null)
                                                        frequency(updatedRRule?.frequency ?: Recur.Frequency.DAILY)
                                                    updatedRRule?.until?.let { until(updatedRRule?.until!!) }
                                                    updatedRRule?.dayList?.let { dayList(it) }
                                                    updatedRRule?.monthDayList?.let { monthDayList(it) }
                                                }.build()
                                                intervalExpanded = false
                                                onRecurUpdated(updatedRRule)
                                            },
                                            text = {
                                                Text(DateTimeUtils.getLocalizedOrdinalFor(number))
                                            }
                                        )
                                    }
                                }
                            }
                        )

                        AssistChip(
                            onClick = { frequencyExpanded = true },
                            label = {
                                Text(
                                    when (updatedRRule?.frequency) {
                                        Recur.Frequency.YEARLY -> stringResource(id = R.string.edit_recur_year)
                                        Recur.Frequency.MONTHLY -> stringResource(id = R.string.edit_recur_month)
                                        Recur.Frequency.WEEKLY -> stringResource(id = R.string.edit_recur_week)
                                        Recur.Frequency.DAILY -> stringResource(id = R.string.edit_recur_day)
                                        else -> "not supported"
                                    }
                                )

                                DropdownMenu(
                                    expanded = frequencyExpanded,
                                    onDismissRequest = { frequencyExpanded = false }
                                ) {

                                    Recur.Frequency.values().reversed().forEach { frequency ->
                                        DropdownMenuItem(
                                            onClick = {
                                                updatedRRule = Recur.Builder().apply {
                                                    interval(updatedRRule?.interval ?: 1)
                                                    if((updatedRRule?.count ?: -1) > 0)
                                                        count(updatedRRule?.count)
                                                    frequency(frequency)
                                                    updatedRRule?.until?.let { until(Date(it)) }
                                                    if(frequency == Recur.Frequency.WEEKLY)
                                                        updatedRRule?.dayList?.let { dayList(it) }
                                                    if(frequency == Recur.Frequency.MONTHLY)
                                                        updatedRRule?.monthDayList?.let { monthDayList(it) }
                                                }.build()
                                                onRecurUpdated(updatedRRule)
                                                frequencyExpanded = false
                                            },
                                            text = {
                                                Text(
                                                    when (frequency) {
                                                        Recur.Frequency.YEARLY -> stringResource(id = R.string.edit_recur_year)
                                                        Recur.Frequency.MONTHLY -> stringResource(id = R.string.edit_recur_month)
                                                        Recur.Frequency.WEEKLY -> stringResource(id = R.string.edit_recur_week)
                                                        Recur.Frequency.DAILY -> stringResource(id = R.string.edit_recur_day)
                                                        else -> frequency.name
                                                    }
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }


                    AnimatedVisibility(updatedRRule?.frequency == Recur.Frequency.WEEKLY) {

                        val weekdays = if (DateTimeUtils.isLocalizedWeekstartMonday())
                            listOf(
                                WeekDay.MO,
                                WeekDay.TU,
                                WeekDay.WE,
                                WeekDay.TH,
                                WeekDay.FR,
                                WeekDay.SA,
                                WeekDay.SU
                            )
                        else
                            listOf(
                                WeekDay.SU,
                                WeekDay.MO,
                                WeekDay.TU,
                                WeekDay.WE,
                                WeekDay.TH,
                                WeekDay.FR,
                                WeekDay.SA
                            )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterHorizontally
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            Text(stringResource(id = R.string.edit_recur_on_weekday))

                            weekdays.forEach { weekday ->
                                // WORKAROUND - otherwise recomposition does not trigger properly
                                var selected by remember { mutableStateOf(updatedRRule?.dayList?.contains(weekday) == true) }
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        if (updatedRRule?.dayList?.contains(weekday) == true)
                                            updatedRRule?.dayList?.remove(weekday)
                                        else
                                            updatedRRule?.dayList?.add(weekday)

                                        selected = !selected

                                        updatedRRule = Recur.Builder().apply {
                                            interval(updatedRRule?.interval ?: 1)
                                            if((updatedRRule?.count ?: -1) > 0)
                                                count(updatedRRule?.count)
                                            frequency(updatedRRule?.frequency?: Recur.Frequency.DAILY)
                                            updatedRRule?.until?.let { until(Date(it)) }
                                            if(updatedRRule?.dayList?.isNotEmpty() == true)
                                                dayList(updatedRRule?.dayList)
                                            //updatedRRule?.monthDayList?.let { monthDayList(it) }
                                        }.build()
                                        onRecurUpdated(updatedRRule)
                                    },
                                    label = {
                                        Text(
                                            when (weekday) {
                                                WeekDay.MO -> DayOfWeek.MONDAY.getDisplayName(
                                                    java.time.format.TextStyle.SHORT,
                                                    Locale.getDefault()
                                                )
                                                WeekDay.TU -> DayOfWeek.TUESDAY.getDisplayName(
                                                    java.time.format.TextStyle.SHORT,
                                                    Locale.getDefault()
                                                )
                                                WeekDay.WE -> DayOfWeek.WEDNESDAY.getDisplayName(
                                                    java.time.format.TextStyle.SHORT,
                                                    Locale.getDefault()
                                                )
                                                WeekDay.TH -> DayOfWeek.THURSDAY.getDisplayName(
                                                    java.time.format.TextStyle.SHORT,
                                                    Locale.getDefault()
                                                )
                                                WeekDay.FR -> DayOfWeek.FRIDAY.getDisplayName(
                                                    java.time.format.TextStyle.SHORT,
                                                    Locale.getDefault()
                                                )
                                                WeekDay.SA -> DayOfWeek.SATURDAY.getDisplayName(
                                                    java.time.format.TextStyle.SHORT,
                                                    Locale.getDefault()
                                                )
                                                WeekDay.SU -> DayOfWeek.SUNDAY.getDisplayName(
                                                    java.time.format.TextStyle.SHORT,
                                                    Locale.getDefault()
                                                )
                                                else -> {
                                                    ""
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }

                    AnimatedVisibility(updatedRRule?.frequency == Recur.Frequency.MONTHLY) {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterHorizontally
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            Text(stringResource(id = R.string.edit_recur_on_the_x_day_of_month))

                            AssistChip(
                                onClick = { monthDayListExpanded = true },
                                label = {
                                    Text(
                                        DateTimeUtils.getLocalizedOrdinalFor(
                                            updatedRRule?.monthDayList?.firstOrNull() ?: 1
                                        )
                                    )

                                    DropdownMenu(
                                        expanded = monthDayListExpanded,
                                        onDismissRequest = { monthDayListExpanded = false }
                                    ) {
                                        for (number in 1..31) {
                                            DropdownMenuItem(
                                                onClick = {
                                                    updatedRRule = Recur.Builder().apply {
                                                        interval(updatedRRule?.interval ?: 1)
                                                        if((updatedRRule?.count ?: -1) > 0)
                                                            count(updatedRRule?.count)
                                                        frequency(updatedRRule?.frequency?: Recur.Frequency.DAILY)
                                                        updatedRRule?.until?.let { until(Date(it)) }
                                                        //updatedRRule?.dayList?.let { dayList(it) }
                                                        monthDayList(NumberList(number.toString()))
                                                    }.build()
                                                    monthDayListExpanded = false
                                                    onRecurUpdated(updatedRRule)
                                                },
                                                text = {
                                                    Text(DateTimeUtils.getLocalizedOrdinalFor(number))
                                                }
                                            )
                                        }
                                    }
                                }
                            )

                            Text(stringResource(id = R.string.edit_recur_x_day_of_the_month))
                        }

                    }


                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {

                        AssistChip(
                            onClick = { endsExpanded = true },
                            label = {

                                Text(
                                    when {
                                        (updatedRRule?.count ?: -1) > 0 -> stringResource(id = R.string.edit_recur_ends_after)
                                        updatedRRule?.until != null -> stringResource(id = R.string.edit_recur_ends_on)
                                        else -> stringResource(id = R.string.edit_recur_ends_never)
                                    }
                                )

                                DropdownMenu(
                                    expanded = endsExpanded,
                                    onDismissRequest = { endsExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                            updatedRRule = Recur.Builder().apply {
                                                interval(updatedRRule?.interval ?: 1)
                                                count(1)
                                                frequency(updatedRRule?.frequency?: Recur.Frequency.DAILY)
                                                //until(Date(icalObject.dtstart ?: System.currentTimeMillis()))
                                                updatedRRule?.dayList?.let { dayList(it) }
                                                updatedRRule?.monthDayList?.let { monthDayList(it) }
                                            }.build()
                                            endsExpanded = false
                                            onRecurUpdated(updatedRRule)
                                        },
                                        text = { Text(stringResource(id = R.string.edit_recur_ends_after)) }
                                    )
                                    DropdownMenuItem(
                                        onClick = {
                                            updatedRRule = Recur.Builder().apply {
                                                interval(updatedRRule?.interval ?: 1)
                                                //if((updatedRRule?.count ?: -1) > 0)
                                                //    count(updatedRRule?.count)
                                                frequency(updatedRRule?.frequency?: Recur.Frequency.DAILY)
                                                until(Date(icalObject.dtstart ?: System.currentTimeMillis()))
                                                updatedRRule?.dayList?.let { dayList(it) }
                                                updatedRRule?.monthDayList?.let { monthDayList(it) }
                                            }.build()
                                            endsExpanded = false
                                            onRecurUpdated(updatedRRule)
                                        },
                                        text = { Text(stringResource(id = R.string.edit_recur_ends_on)) }
                                    )
                                    DropdownMenuItem(
                                        onClick = {
                                            updatedRRule = Recur.Builder().apply {
                                                interval(updatedRRule?.interval ?: 1)
                                                //if((updatedRRule?.count ?: -1) > 0)
                                                //    count(updatedRRule?.count)
                                                frequency(updatedRRule?.frequency?: Recur.Frequency.DAILY)
                                                //until(Date(icalObject.dtstart ?: System.currentTimeMillis()))
                                                updatedRRule?.dayList?.let { dayList(it) }
                                                updatedRRule?.monthDayList?.let { monthDayList(it) }
                                            }.build()
                                            endsExpanded = false
                                            onRecurUpdated(updatedRRule)
                                        },
                                        text = { Text(stringResource(id = R.string.edit_recur_ends_never)) }
                                    )
                                }
                            }
                        )

                        AnimatedVisibility((updatedRRule?.count ?: -1) > 0) {
                            AssistChip(
                                onClick = { endAfterExpaneded = true },
                                label = {
                                    Text((updatedRRule?.count?:1).toString())

                                    DropdownMenu(
                                        expanded = endAfterExpaneded,
                                        onDismissRequest = { endAfterExpaneded = false }
                                    ) {
                                        for (number in 1..ICalObject.DEFAULT_MAX_RECUR_INSTANCES) {
                                            DropdownMenuItem(
                                                onClick = {
                                                    updatedRRule = Recur.Builder().apply {
                                                        interval(updatedRRule?.interval ?: 1)
                                                        count(number)
                                                        frequency(updatedRRule?.frequency?: Recur.Frequency.DAILY)
                                                        //until(Date(icalObject.dtstart ?: System.currentTimeMillis()))
                                                        updatedRRule?.dayList?.let { dayList(it) }
                                                        updatedRRule?.monthDayList?.let { monthDayList(it) }
                                                    }.build()
                                                    endAfterExpaneded = false
                                                    onRecurUpdated(updatedRRule)
                                                },
                                                text = {
                                                    Text(number.toString())
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        AnimatedVisibility((updatedRRule?.count ?: -1) > 0) {
                            Text(stringResource(R.string.edit_recur_x_times))
                        }

                        AnimatedVisibility(updatedRRule?.until != null) {
                            AssistChip(
                                onClick = { showDatepicker = true },
                                label = {
                                    Text(
                                        DateTimeUtils.convertLongToFullDateString(
                                            updatedRRule?.until?.time,
                                            TZ_ALLDAY
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            icalObject.rrule = updatedRRule?.toString()
            icalObject.getInstancesFromRrule().forEach { instanceDate ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = DateTimeUtils.convertLongToFullDateTimeString(instanceDate, icalObject.dtstartTimezone),
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            val exceptions = DateTimeUtils.getLongListfromCSVString(icalObject.exdate)
            if(exceptions.isNotEmpty())
                Text(
                    text = stringResource(id = R.string.recurrence_exceptions),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                )
            exceptions.forEach { exception ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = DateTimeUtils.convertLongToFullDateTimeString(exception, icalObject.dtstartTimezone),
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            val additions = DateTimeUtils.getLongListfromCSVString(icalObject.rdate)
            if(additions.isNotEmpty())
                Text(
                    text = stringResource(id = R.string.recurrence_additions),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                )
            additions.forEach { addition ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = DateTimeUtils.convertLongToFullDateTimeString(addition, icalObject.dtstartTimezone),
                        modifier = Modifier.padding(4.dp)
                    )
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
            .frequency(Recur.Frequency.WEEKLY)
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
            isEditMode = false,
            onRecurUpdated = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardRecur_Preview_edit() {
    MaterialTheme {

        val recur = Recur
            .Builder()
            .count(5)
            .frequency(Recur.Frequency.WEEKLY)
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
            isEditMode = true,
            onRecurUpdated = { }
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
            isEditMode = false,
            onRecurUpdated = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardRecur_Preview_edit_off() {
    MaterialTheme {

        DetailsCardRecur(
            icalObject = ICalObject.createTodo().apply {
                dtstart = System.currentTimeMillis()
                dtstartTimezone = null
                due = System.currentTimeMillis()
                dueTimezone = null
            },
            isEditMode = true,
            onRecurUpdated = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardRecur_Preview_edit_no_dtstart() {
    MaterialTheme {

        DetailsCardRecur(
            icalObject = ICalObject.createTodo().apply {
                dtstart = null
                dtstartTimezone = null
                due = null
                dueTimezone = null
            },
            isEditMode = true,
            onRecurUpdated = { }
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
            isEditMode = false,
            onRecurUpdated = { }
        )
    }
}