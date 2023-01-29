/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import at.techbee.jtx.ui.reusable.dialogs.DatePickerDialog
import at.techbee.jtx.ui.reusable.dialogs.UnsupportedRRuleDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.DateTimeUtils.requireTzId
import at.techbee.jtx.util.UiUtil.asDayOfWeek
import net.fortuna.ical4j.model.*
import net.fortuna.ical4j.model.Date
import net.fortuna.ical4j.model.Recur.Frequency
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsCardRecur(
    icalObject: ICalObject,
    isEditMode: Boolean,
    hasChildren: Boolean,
    onRecurUpdated: (Recur?) -> Unit,
    goToDetail: (itemId: Long, editMode: Boolean, list: List<Long>) -> Unit,
    goToSeriesElement: (editMode: Boolean) -> Unit,
    goToRecurInstance: (uid: String, date: Long) -> Unit,
    unlinkFromSeries: () -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.recurrence)
    val dtstartWeekday = when (ZonedDateTime.ofInstant(Instant.ofEpochMilli(icalObject.dtstart?:0L), requireTzId(icalObject.dtstartTimezone)).dayOfWeek) {
        DayOfWeek.MONDAY -> WeekDay.MO
        DayOfWeek.TUESDAY -> WeekDay.TU
        DayOfWeek.WEDNESDAY -> WeekDay.WE
        DayOfWeek.THURSDAY -> WeekDay.TH
        DayOfWeek.FRIDAY -> WeekDay.FR
        DayOfWeek.SATURDAY -> WeekDay.SA
        DayOfWeek.SUNDAY -> WeekDay.SU
        else -> null
    }
    //var updatedRRule by rememberSaveable { mutableStateOf(icalObject.getRecur()) }

    var isRecurActivated by rememberSaveable { mutableStateOf(icalObject.getRecur() != null) }
    var frequency by rememberSaveable { mutableStateOf(icalObject.getRecur()?.frequency) }
    var interval by rememberSaveable { mutableStateOf(icalObject.getRecur()?.interval) }
    var count by rememberSaveable { mutableStateOf(icalObject.getRecur()?.count) }
    var until by rememberSaveable { mutableStateOf(icalObject.getRecur()?.until) }
    val dayList = remember { icalObject.getRecur()?.dayList?.toMutableStateList() ?: mutableStateListOf() }
    val monthDayList = remember { mutableStateListOf(icalObject.getRecur()?.monthDayList?.firstOrNull() ?: 1) }

    var frequencyExpanded by rememberSaveable { mutableStateOf(false) }
    var intervalExpanded by rememberSaveable { mutableStateOf(false) }
    var monthDayListExpanded by rememberSaveable { mutableStateOf(false) }
    var endAfterExpaneded by rememberSaveable { mutableStateOf(false) }
    var endsExpanded by rememberSaveable { mutableStateOf(false) }
    var showDatepicker by rememberSaveable { mutableStateOf(false) }



    fun buildRRule(): Recur? {
        if(!isRecurActivated)
            return null
        else {
            val updatedRRule = Recur.Builder().apply {
                interval(interval ?: 1)
                if (until != null)
                    until(until)
                else
                    count(count ?: -1)
                frequency(frequency ?: Frequency.DAILY)

                if(frequency == Frequency.WEEKLY || dayList.isNotEmpty()) {    // there might be a dayList also for DAILY recurrences coming from Thunderbird!
                    val newDayList = WeekDayList().apply {
                        dayList.forEach { weekDay -> this.add(weekDay) }
                        if(!dayList.contains(dtstartWeekday))
                            dayList.add(dtstartWeekday)
                    }
                    dayList(newDayList)
                }
                if(frequency == Frequency.MONTHLY) {
                    val newMonthList = NumberList().apply {
                        monthDayList.forEach { monthDay -> this.add(monthDay) }
                    }
                    monthDayList(newMonthList)
                }
            }.build()
            return updatedRRule
        }
    }

    if (showDatepicker) {
        DatePickerDialog(
            datetime = until?.time ?: icalObject.dtstart ?: System.currentTimeMillis(),
            timezone = TZ_ALLDAY,
            dateOnly = true,
            allowNull = false,
            onConfirm = { datetime, _ ->
                datetime?.let { until = Date(it) }
                onRecurUpdated(buildRRule())
            },
            onDismiss = { showDatepicker = false }
        )
    }

    icalObject.getRecur()?.let { recur ->
        if(isEditMode && (recur.experimentalValues?.isNotEmpty() == true
                    || recur.hourList?.isNotEmpty() == true
                    || recur.minuteList?.isNotEmpty() == true
                    || recur.monthList?.isNotEmpty() == true
                    || recur.secondList?.isNotEmpty() == true
                    || recur.setPosList?.isNotEmpty() == true
                    || recur.skip != null
                    || recur.weekNoList?.isNotEmpty() == true
                    || recur.weekStartDay != null
                    || recur.yearDayList?.isNotEmpty() == true
                    || (recur.monthDayList?.size?:0) > 1)
        ) {
            UnsupportedRRuleDialog(
                onConfirm = {  },
                onDismiss = { goToDetail(icalObject.id, false, emptyList()) }
            )
        }
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

                AnimatedVisibility(isEditMode && icalObject.recurid == null) {
                    Switch(
                        checked = isRecurActivated,
                        enabled = icalObject.dtstart != null,
                        onCheckedChange = {
                            isRecurActivated = it
                            if (it) {
                                frequency = Frequency.DAILY
                                count = 1
                                interval = 1
                                until = null
                                //dayList = null
                                //monthDayList = null
                            }
                            onRecurUpdated(buildRRule())
                        }
                    )
                }
            }

            AnimatedVisibility(isEditMode && icalObject.dtstart == null) {
                Text(
                    text = stringResource(id = R.string.edit_recur_toast_requires_start_date),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            AnimatedVisibility(isEditMode && isRecurActivated) {

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
                                    if((interval ?: -1) < 1)
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
                                                onRecurUpdated(buildRRule())
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

                                    Frequency.values().reversed().forEach { frequency2select ->
                                        if(icalObject.dtstartTimezone == TZ_ALLDAY
                                            && listOf(Frequency.SECONDLY, Frequency.MINUTELY, Frequency.HOURLY).contains(frequency2select))
                                            return@forEach
                                        if(frequency2select == Frequency.SECONDLY)
                                            return@forEach

                                        DropdownMenuItem(
                                            onClick = {
                                                frequency = frequency2select
                                                onRecurUpdated(buildRRule())
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
                                        onRecurUpdated(buildRRule())
                                    },
                                    enabled = dtstartWeekday != weekday,
                                    label = {
                                        Text(
                                        weekday.asDayOfWeek()?.getDisplayName(
                                                java.time.format.TextStyle.SHORT,
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
                                    Text(
                                        DateTimeUtils.getLocalizedOrdinalFor(
                                            monthDayList.firstOrNull() ?: 1
                                        )
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
                                                    onRecurUpdated(buildRRule())
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
                                        (count ?: -1) > 0 -> stringResource(id = R.string.edit_recur_ends_after)
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
                                            onRecurUpdated(buildRRule())
                                        },
                                        text = { Text(stringResource(id = R.string.edit_recur_ends_after)) }
                                    )
                                    DropdownMenuItem(
                                        onClick = {
                                            count = null
                                            until = Date(icalObject.dtstart ?: System.currentTimeMillis())
                                            endsExpanded = false
                                            onRecurUpdated(buildRRule())
                                        },
                                        text = { Text(stringResource(id = R.string.edit_recur_ends_on)) }
                                    )
                                    DropdownMenuItem(
                                        onClick = {
                                            count = null
                                            until = null
                                            endsExpanded = false
                                            onRecurUpdated(buildRRule())
                                        },
                                        text = { Text(stringResource(id = R.string.edit_recur_ends_never)) }
                                    )
                                }
                            }
                        )

                        AnimatedVisibility((count ?: -1) > 0) {
                            AssistChip(
                                onClick = { endAfterExpaneded = true },
                                label = {
                                    Text((count?:1).toString())

                                    DropdownMenu(
                                        expanded = endAfterExpaneded,
                                        onDismissRequest = { endAfterExpaneded = false }
                                    ) {
                                        for (number in 1..ICalObject.DEFAULT_MAX_RECUR_INSTANCES) {
                                            DropdownMenuItem(
                                                onClick = {
                                                    count = number
                                                    endAfterExpaneded = false
                                                    onRecurUpdated(buildRRule())
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

                        AnimatedVisibility((count ?: -1) > 0) {
                            Text(stringResource(R.string.edit_recur_x_times))
                        }

                        AnimatedVisibility(until != null) {
                            AssistChip(
                                onClick = { showDatepicker = true },
                                label = {
                                    Text(
                                        DateTimeUtils.convertLongToFullDateString(
                                            until?.time,
                                            TZ_ALLDAY
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if(icalObject.recurid != null) {
                    Button(
                        onClick = {
                            goToSeriesElement(isEditMode)
                        }
                    ) {
                        Text(stringResource(id = R.string.details_go_to_series))
                    }
                    Button(
                        onClick = { unlinkFromSeries() }
                    ) {
                        Text(stringResource(id = R.string.details_unlink_from_series))
                    }
                    Text(
                        text = stringResource(id = if(icalObject.sequence == 0L) R.string.details_unchanged_part_of_series else R.string.details_changed_part_of_series),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            AnimatedVisibility(hasChildren) {
                Text(
                    text = stringResource(id = R.string.details_series_attention_subentries),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if(isEditMode)
                icalObject.rrule = buildRRule()?.toString()

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                icalObject.getInstancesFromRrule().forEach { instanceDate ->
                    ElevatedCard(
                        onClick = {
                            if(!isEditMode)
                                goToRecurInstance(icalObject.uid, instanceDate)
                                  },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = DateTimeUtils.convertLongToFullDateTimeString(instanceDate, icalObject.dtstartTimezone),
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                        )
                    }
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
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = DateTimeUtils.convertLongToFullDateTimeString(exception, icalObject.dtstartTimezone),
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
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
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
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
            isEditMode = false,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            goToSeriesElement = { },
            goToRecurInstance = { _, _ -> },
            unlinkFromSeries = { }
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
            isEditMode = true,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            goToSeriesElement = { },
            goToRecurInstance = { _, _ -> },
            unlinkFromSeries = { }
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
            isEditMode = false,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            goToSeriesElement = { },
            goToRecurInstance = { _, _ -> },
            unlinkFromSeries = { }
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
            isEditMode = false,
            hasChildren = true,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            goToSeriesElement = { },
            goToRecurInstance = { _, _ -> },
            unlinkFromSeries = { }
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
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            goToSeriesElement = { },
            goToRecurInstance = { _, _ -> },
            unlinkFromSeries = { }
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
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            goToSeriesElement = { },
            goToRecurInstance = { _, _ -> },
            unlinkFromSeries = { }
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
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            goToSeriesElement = { },
            goToRecurInstance = { _, _ -> },
            unlinkFromSeries = { }
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
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            goToSeriesElement = { },
            goToRecurInstance = { _, _ -> },
            unlinkFromSeries = { }
        )
    }
}