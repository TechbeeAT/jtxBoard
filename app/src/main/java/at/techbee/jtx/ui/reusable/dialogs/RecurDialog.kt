/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.UiUtil.asDayOfWeek
import net.fortuna.ical4j.model.Date
import net.fortuna.ical4j.model.NumberList
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.Recur.Frequency
import net.fortuna.ical4j.model.WeekDay
import net.fortuna.ical4j.model.WeekDayList
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.absoluteValue


@Composable
fun RecurDialog(
    dtstart: Long,
    dtstartTimezone: String?,
    onRecurUpdated: (Recur?) -> Unit,
    onDismiss: () -> Unit
) {

    val shadowICalObject = ICalObject.createJournal().apply {
        this.dtstart = dtstart
        this.dtstartTimezone = dtstartTimezone
    }

    val dtstartWeekday = when (ZonedDateTime.ofInstant(
        Instant.ofEpochMilli(shadowICalObject.dtstart ?: 0L),
        DateTimeUtils.requireTzId(shadowICalObject.dtstartTimezone)
    ).dayOfWeek) {
        DayOfWeek.MONDAY -> WeekDay.MO
        DayOfWeek.TUESDAY -> WeekDay.TU
        DayOfWeek.WEDNESDAY -> WeekDay.WE
        DayOfWeek.THURSDAY -> WeekDay.TH
        DayOfWeek.FRIDAY -> WeekDay.FR
        DayOfWeek.SATURDAY -> WeekDay.SA
        DayOfWeek.SUNDAY -> WeekDay.SU
        else -> null
    }
    //var currentRRule by rememberSaveable { mutableStateOf(icalObject.getRecur()) }

    var frequency by rememberSaveable { mutableStateOf(if (shadowICalObject.getRecur() == null) Frequency.DAILY else shadowICalObject.getRecur()?.frequency) }
    var interval by rememberSaveable { mutableStateOf(if (shadowICalObject.getRecur() == null) 1 else shadowICalObject.getRecur()?.interval?.let { if (it <= 0) null else it }) }
    var count by rememberSaveable { mutableStateOf(if (shadowICalObject.getRecur() == null) 1 else shadowICalObject.getRecur()?.count?.let { if (it <= 0) null else it }) }
    var until by rememberSaveable { mutableStateOf(shadowICalObject.getRecur()?.until) }
    val dayList =
        remember { shadowICalObject.getRecur()?.dayList?.toMutableStateList() ?: mutableStateListOf() }
    val monthDayList =
        remember { mutableStateListOf(shadowICalObject.getRecur()?.monthDayList?.firstOrNull() ?: 1) }

    var frequencyExpanded by rememberSaveable { mutableStateOf(false) }
    var intervalExpanded by rememberSaveable { mutableStateOf(false) }
    var monthDayListExpanded by rememberSaveable { mutableStateOf(false) }
    var endAfterExpaneded by rememberSaveable { mutableStateOf(false) }
    var endsExpanded by rememberSaveable { mutableStateOf(false) }
    var showDatepicker by rememberSaveable { mutableStateOf(false) }


    fun buildRRule(): Recur? {
        val updatedRRule = Recur.Builder().apply {
            if (interval != null && interval!! > 1)
                interval(interval!!)
            until?.let { until(it) }
            count?.let { count(it) }
            frequency(frequency ?: Frequency.DAILY)

            if (frequency == Frequency.WEEKLY || dayList.isNotEmpty()) {    // there might be a dayList also for DAILY recurrences coming from Thunderbird!
                val newDayList = WeekDayList().apply {
                    dayList.forEach { weekDay -> this.add(weekDay) }
                    if (!dayList.contains(dtstartWeekday))
                        dayList.add(dtstartWeekday)
                }
                dayList(newDayList)
            }
            if (frequency == Frequency.MONTHLY) {
                val newMonthList = NumberList().apply {
                    monthDayList.forEach { monthDay -> this.add(monthDay) }
                }
                monthDayList(newMonthList)
            }
        }.build()
        return updatedRRule
    }

    if (showDatepicker) {
        DatePickerDialog(
            datetime = until?.time ?: shadowICalObject.dtstart ?: System.currentTimeMillis(),
            timezone = ICalObject.TZ_ALLDAY,
            dateOnly = true,
            allowNull = false,
            onConfirm = { datetime, _ ->
                datetime?.let { until = Date(it) }
                //onRecurUpdated(buildRRule())
            },
            onDismiss = { showDatepicker = false }
        )
    }



    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(id = R.string.recurrence)) },
        properties = DialogProperties(usePlatformDefaultWidth = false),   // Workaround due to Google Issue: https://issuetracker.google.com/issues/194911971?pli=1
        text = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    //.verticalScroll(rememberScrollState()),
            ) {

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
                                    if (interval == null || interval!! < 1)
                                        "1"
                                    else
                                        interval?.toString() ?: "1"
                                )

                                DropdownMenu(
                                    expanded = intervalExpanded,
                                    onDismissRequest = { intervalExpanded = false }
                                ) {
                                    for (number in 1..100) {
                                        DropdownMenuItem(
                                            onClick = {
                                                interval = number
                                                intervalExpanded = false
                                            },
                                            text = { Text("$number") }
                                        )
                                    }
                                }
                            }
                        )

                        AssistChip(
                            onClick = { frequencyExpanded = true },
                            label = {
                                Text(
                                    when (frequency) {
                                        Frequency.YEARLY -> stringResource(id = R.string.edit_recur_year)
                                        Frequency.MONTHLY -> stringResource(id = R.string.edit_recur_month)
                                        Frequency.WEEKLY -> stringResource(id = R.string.edit_recur_week)
                                        Frequency.DAILY -> stringResource(id = R.string.edit_recur_day)
                                        Frequency.HOURLY -> stringResource(id = R.string.edit_recur_hour)
                                        Frequency.MINUTELY -> stringResource(id = R.string.edit_recur_minute)
                                        Frequency.SECONDLY -> stringResource(id = R.string.edit_recur_second)
                                        else -> "not supported"
                                    }
                                )

                                DropdownMenu(
                                    expanded = frequencyExpanded,
                                    onDismissRequest = { frequencyExpanded = false }
                                ) {

                                    Recur.Frequency.entries.reversed().forEach { frequency2select ->
                                        if (shadowICalObject.dtstartTimezone == ICalObject.TZ_ALLDAY
                                            && listOf(
                                                Frequency.SECONDLY,
                                                Frequency.MINUTELY,
                                                Frequency.HOURLY
                                            ).contains(frequency2select)
                                        )
                                            return@forEach
                                        if (frequency2select == Frequency.SECONDLY)
                                            return@forEach

                                        DropdownMenuItem(
                                            onClick = {
                                                frequency = frequency2select
                                                frequencyExpanded = false
                                            },
                                            text = {
                                                Text(
                                                    when (frequency2select) {
                                                        Frequency.YEARLY -> stringResource(id = R.string.edit_recur_year)
                                                        Frequency.MONTHLY -> stringResource(id = R.string.edit_recur_month)
                                                        Frequency.WEEKLY -> stringResource(id = R.string.edit_recur_week)
                                                        Frequency.DAILY -> stringResource(id = R.string.edit_recur_day)
                                                        Frequency.HOURLY -> stringResource(id = R.string.edit_recur_hour)
                                                        Frequency.MINUTELY -> stringResource(id = R.string.edit_recur_minute)
                                                        //Frequency.SECONDLY -> stringResource(id = R.string.edit_recur_second)
                                                        else -> frequency2select.name
                                                    }
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }

                    AnimatedVisibility(frequency == Frequency.WEEKLY || dayList.isNotEmpty()) {

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
                                FilterChip(
                                    selected = dayList.contains(weekday),
                                    onClick = {
                                        if (dayList.contains(weekday))
                                            dayList.remove(weekday)
                                        else
                                            (dayList).add(weekday)
                                    },
                                    enabled = dtstartWeekday != weekday,
                                    label = {
                                        Text(
                                            weekday.asDayOfWeek()?.getDisplayName(
                                                TextStyle.SHORT,
                                                Locale.getDefault()
                                            ) ?: ""
                                        )
                                    }
                                )
                            }
                        }
                    }

                    AnimatedVisibility(frequency == Frequency.MONTHLY) {

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
                                    val monthDay = monthDayList.firstOrNull() ?: 1
                                    Text(
                                        if (monthDay < 0)
                                            stringResource(id = R.string.edit_recur_LAST_day_of_the_month) + if (monthDay < -1) " - ${monthDay.absoluteValue - 1}" else ""
                                        else
                                            DateTimeUtils.getLocalizedOrdinalFor(monthDay)
                                    )

                                    DropdownMenu(
                                        expanded = monthDayListExpanded,
                                        onDismissRequest = { monthDayListExpanded = false }
                                    ) {
                                        for (number in 1..31) {
                                            DropdownMenuItem(
                                                onClick = {
                                                    monthDayList.clear()
                                                    monthDayList.add(number)
                                                    monthDayListExpanded = false
                                                },
                                                text = {
                                                    Text(DateTimeUtils.getLocalizedOrdinalFor(number))
                                                }
                                            )
                                        }

                                        for (number in 1..31) {
                                            DropdownMenuItem(
                                                onClick = {
                                                    monthDayList.clear()
                                                    monthDayList.add(number * (-1))
                                                    monthDayListExpanded = false
                                                },
                                                text = {
                                                    Text(stringResource(id = R.string.edit_recur_LAST_day_of_the_month) + if (number > 1) " - ${number - 1}" else "")
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
                                        count != null -> stringResource(id = R.string.edit_recur_ends_after)
                                        until != null -> stringResource(id = R.string.edit_recur_ends_on)
                                        else -> stringResource(id = R.string.edit_recur_ends_never)
                                    }
                                )

                                DropdownMenu(
                                    expanded = endsExpanded,
                                    onDismissRequest = { endsExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                            count = 1
                                            until = null
                                            endsExpanded = false
                                        },
                                        text = { Text(stringResource(id = R.string.edit_recur_ends_after)) }
                                    )
                                    DropdownMenuItem(
                                        onClick = {
                                            count = null
                                            until = Date(
                                                shadowICalObject.dtstart ?: System.currentTimeMillis()
                                            )
                                            endsExpanded = false
                                        },
                                        text = { Text(stringResource(id = R.string.edit_recur_ends_on)) }
                                    )
                                    DropdownMenuItem(
                                        onClick = {
                                            count = null
                                            until = null
                                            endsExpanded = false
                                        },
                                        text = { Text(stringResource(id = R.string.edit_recur_ends_never)) }
                                    )
                                }
                            }
                        )

                        AnimatedVisibility(count != null) {
                            AssistChip(
                                onClick = { endAfterExpaneded = true },
                                label = {
                                    Text((count ?: 1).toString())

                                    DropdownMenu(
                                        expanded = endAfterExpaneded,
                                        onDismissRequest = { endAfterExpaneded = false }
                                    ) {
                                        for (number in 1..100) {
                                            DropdownMenuItem(
                                                onClick = {
                                                    count = number
                                                    endAfterExpaneded = false
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

                        AnimatedVisibility(count != null) {
                            Text(stringResource(R.string.edit_recur_x_times))
                        }

                        AnimatedVisibility(until != null) {
                            AssistChip(
                                onClick = { showDatepicker = true },
                                label = {
                                    Text(
                                        DateTimeUtils.convertLongToFullDateString(
                                            until?.time,
                                            ICalObject.TZ_ALLDAY
                                        )
                                    )
                                }
                            )
                        }
                    }
                }


                shadowICalObject.rrule = buildRRule()?.toString()

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    val instances = shadowICalObject.getInstancesFromRrule()
                    if (instances.isNotEmpty())
                        Text(
                            text = stringResource(R.string.preview),
                            style = MaterialTheme.typography.labelMedium,
                            fontStyle = FontStyle.Italic
                        )
                    instances.forEach { instanceDate ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = DateTimeUtils.convertLongToFullDateTimeString(
                                    instanceDate,
                                    shadowICalObject.dtstartTimezone
                                ),
                                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = {
                        onRecurUpdated(null)
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.list_item_remove_recurrence))
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text(stringResource(id = R.string.cancel))
                }

                TextButton(
                    onClick = {
                        onRecurUpdated(if(shadowICalObject.getInstancesFromRrule().size > 1) buildRRule() else null)
                        onDismiss()
                    }
                ) {
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    )
}


@Preview(showBackground = true)
@Composable
fun RecurDialog_Preview() {
    MaterialTheme {
        RecurDialog(
            dtstart = System.currentTimeMillis(),
            dtstartTimezone = null,
            onDismiss = {},
            onRecurUpdated = {}
        )
    }
}