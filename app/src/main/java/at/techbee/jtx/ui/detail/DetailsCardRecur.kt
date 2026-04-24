/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.annotation.SuppressLint
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import at.techbee.jtx.ui.reusable.dialogs.DatePickerDialog
import at.techbee.jtx.ui.reusable.dialogs.DetachFromSeriesDialog
import at.techbee.jtx.ui.reusable.elements.CheckboxWithText
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.DateTimeUtils.requireTzId
import at.techbee.jtx.util.UiUtil.asDayOfWeek
import net.fortuna.ical4j.model.Date
import net.fortuna.ical4j.model.Month
import net.fortuna.ical4j.model.MonthList
import net.fortuna.ical4j.model.NumberList
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.Recur.Frequency
import net.fortuna.ical4j.model.WeekDay
import net.fortuna.ical4j.model.WeekDayList
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.absoluteValue


@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsCardRecur(
    icalObject: ICalObject,
    seriesInstances: List<ICalObject>,
    seriesElement: ICalObject?,
    isEditMode: Boolean,
    hasChildren: Boolean,
    onRecurUpdated: (Recur?) -> Unit,
    goToDetail: (itemId: Long, editMode: Boolean, list: List<Long>) -> Unit,
    unlinkFromSeries: (instances: List<ICalObject>, series: ICalObject?, deleteAfterUnlink: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
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

    var frequency by rememberSaveable { mutableStateOf(icalObject.getRecur()?.frequency) }
    var interval by rememberSaveable { mutableStateOf(icalObject.getRecur()?.interval?.let { if(it<=0) null else it }) }
    var count by rememberSaveable { mutableStateOf(icalObject.getRecur()?.count?.let { if (it<=0) null else it }) }
    var until by rememberSaveable { mutableStateOf(icalObject.getRecur()?.until) }
    val weekDayList = remember { icalObject.getRecur()?.dayList?.toMutableStateList() ?: mutableStateListOf() }
    val monthDayList = remember { icalObject.getRecur()?.monthDayList?.toMutableStateList() ?: mutableStateListOf() }
    //val setPosList = remember { icalObject.getRecur()?.setPosList?.toMutableStateList() ?: mutableStateListOf() }
    val monthList = remember { icalObject.getRecur()?.monthList?.toMutableStateList() ?: mutableStateListOf() }

    var frequencyExpanded by rememberSaveable { mutableStateOf(false) }
    var intervalExpanded by rememberSaveable { mutableStateOf(false) }
    var weekDayListExpanded by rememberSaveable { mutableStateOf(false) }
    var monthDayListExpanded by rememberSaveable { mutableStateOf(false) }
    var monthListExpanded by rememberSaveable { mutableStateOf(false) }
    var weekDayWithOffsetListExpanded by rememberSaveable { mutableStateOf(false) }
    var endAfterExpaneded by rememberSaveable { mutableStateOf(false) }
    var endsExpanded by rememberSaveable { mutableStateOf(false) }
    var showDatepicker by rememberSaveable { mutableStateOf(false) }
    var showDetachSingleFromSeriesDialog by rememberSaveable { mutableStateOf(false) }
    var showDetachAllFromSeriesDialog by rememberSaveable { mutableStateOf(false) }

    val weekdays = if (DateTimeUtils.isLocalizedWeekstartMonday())
        listOf(WeekDay.MO, WeekDay.TU, WeekDay.WE, WeekDay.TH, WeekDay.FR, WeekDay.SA, WeekDay.SU)
    else
        listOf(WeekDay.SU, WeekDay.MO, WeekDay.TU, WeekDay.WE, WeekDay.TH, WeekDay.FR, WeekDay.SA)


    fun buildRRule(): Recur? {
        if(frequency == null)
            return null
        else {
            val updatedRRule = Recur.Builder().apply {
                if(interval != null && interval!! > 1)
                    interval(interval!!)
                until?.let { until(it) }
                count?.let { count(it) }

                frequency(frequency ?: Frequency.DAILY)

                if(weekDayList.isNotEmpty()) {    // there might be a dayList also for DAILY recurrences coming from Thunderbird!
                    val newDayList = WeekDayList().apply {
                        this.addAll(weekDayList)
                        if(weekDayList.none { it.day == dtstartWeekday?.day })
                            weekDayList.add(dtstartWeekday)
                    }
                    dayList(newDayList)
                }
                if(monthDayList.isNotEmpty()) {
                    val newMonthDayList = NumberList().apply {
                        this.addAll(monthDayList)
                    }
                    monthDayList(newMonthDayList)
                }
                if(monthList.isNotEmpty()) {
                    val newMonthList = MonthList().apply {
                        this.addAll(monthList)
                    }
                    monthList(newMonthList)
                }
            }.build()
            //Log.d("RRULE", updatedRRule.toString())
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

    ElevatedCard(modifier = modifier) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            HeadlineWithIcon(
                icon = Icons.Outlined.EventRepeat,
                iconDesc = stringResource(id = R.string.recurrence),
                text = stringResource(id = R.string.recurrence),
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(isEditMode && icalObject.dtstart == null) {
                Text(
                    text = stringResource(id = R.string.edit_recur_toast_requires_start_date),
                    style = MaterialTheme.typography.bodySmall
                )
            }

                    AssistChip(
                        onClick = { frequencyExpanded = true },
                        enabled = isEditMode && icalObject.recurid == null && icalObject.dtstart != null,
                        label = {
                            Text(
                                when {
                                    frequency == Frequency.YEARLY && weekDayList.isNotEmpty() -> stringResource(id = R.string.recur_yearly_by_day)
                                    frequency == Frequency.YEARLY && monthDayList.isNotEmpty() -> stringResource(id = R.string.recur_yearly_by_date)
                                    frequency == Frequency.MONTHLY && weekDayList.isNotEmpty() -> stringResource(id = R.string.recur_monthly_by_day)
                                    frequency == Frequency.MONTHLY && monthDayList.isNotEmpty() -> stringResource(id = R.string.recur_monthly_by_date)
                                    frequency == Frequency.WEEKLY -> stringResource(id = R.string.recur_weekly)
                                    frequency == Frequency.DAILY -> stringResource(id = R.string.recur_daily)
                                    frequency == null -> stringResource(id = R.string.recur_no_recurrence)
                                    else -> frequency?.name ?: ""
                                }
                            )

                            DropdownMenu(
                                expanded = frequencyExpanded,
                                onDismissRequest = { frequencyExpanded = false }
                            ) {

                                //No recurrence
                                DropdownMenuItem(
                                    onClick = {
                                        frequency = null
                                        count = null
                                        interval = null
                                        monthDayList.clear()
                                        weekDayList.clear()
                                        monthList.clear()
                                        onRecurUpdated(buildRRule())
                                        frequencyExpanded = false
                                    },
                                    text = { Text(stringResource(id = R.string.recur_no_recurrence)) }
                                )

                                //Yearly by day
                                DropdownMenuItem(
                                    onClick = {
                                        frequency = Frequency.YEARLY
                                        if(count == null)
                                            count = 1
                                        if(interval == null)
                                            interval = 1
                                        monthDayList.clear()
                                        monthList.clear()
                                        if(monthList.isEmpty())
                                            monthList.add(Month.valueOf(1))
                                        weekDayList.clear()
                                        weekDayList.add(WeekDay(dtstartWeekday, 1))
                                        onRecurUpdated(buildRRule())
                                        frequencyExpanded = false
                                    },
                                    text = { Text(stringResource(id = R.string.recur_yearly_by_day)) }
                                )

                                //Yearly by date
                                DropdownMenuItem(
                                    onClick = {
                                        frequency = Frequency.YEARLY
                                        if(count == null)
                                            count = 1
                                        if(interval == null)
                                            interval = 1
                                        weekDayList.clear()
                                        if(monthList.isEmpty())
                                            monthList.add(Month.valueOf(1))
                                        if(monthDayList.isEmpty())
                                            monthDayList.add(1)
                                        onRecurUpdated(buildRRule())
                                        frequencyExpanded = false
                                    },
                                    text = { Text(stringResource(id = R.string.recur_yearly_by_date)) }
                                )

                                //Monthly by day
                                DropdownMenuItem(
                                    onClick = {
                                        frequency = Frequency.MONTHLY
                                        if(count == null)
                                            count = 1
                                        if(interval == null)
                                            interval = 1
                                        weekDayList.clear()
                                        weekDayList.add(WeekDay(dtstartWeekday, 1))
                                        monthDayList.clear()
                                        monthList.clear()
                                        onRecurUpdated(buildRRule())
                                        frequencyExpanded = false
                                    },
                                    text = { Text(stringResource(id = R.string.recur_monthly_by_day)) }
                                )

                                //Monthly by date
                                DropdownMenuItem(
                                    onClick = {
                                        frequency = Frequency.MONTHLY
                                        if(count == null)
                                            count = 1
                                        if(interval == null)
                                            interval = 1
                                        if(monthDayList.isEmpty())
                                            monthDayList.add(1)
                                        monthList.clear()
                                        weekDayList.clear()
                                        onRecurUpdated(buildRRule())
                                        frequencyExpanded = false
                                    },
                                    text = { Text(stringResource(id = R.string.recur_monthly_by_date)) }
                                )

                                //Weekly
                                DropdownMenuItem(
                                    onClick = {
                                        frequency = Frequency.WEEKLY
                                        if(count == null)
                                            count = 1
                                        if(interval == null)
                                            interval = 1
                                        monthList.clear()
                                        monthDayList.clear()
                                        weekDayList.clear()
                                        weekDayList.add(dtstartWeekday)
                                        onRecurUpdated(buildRRule())
                                        frequencyExpanded = false
                                    },
                                    text = { Text(stringResource(id = R.string.recur_weekly)) }
                                )

                                //Daily
                                DropdownMenuItem(
                                    onClick = {
                                        frequency = Frequency.DAILY
                                        if(count == null)
                                            count = 1
                                        if(interval == null)
                                            interval = 1
                                        weekDayList.clear()
                                        monthDayList.clear()
                                        monthList.clear()
                                        onRecurUpdated(buildRRule())
                                        frequencyExpanded = false
                                    },
                                    text = { Text(stringResource(id = R.string.recur_daily)) }
                                )
                            }
                        }
                    )


                    AnimatedVisibility(frequency != null) {
                    AssistChip(
                        onClick = { intervalExpanded = true },
                        enabled = isEditMode && icalObject.recurid == null,
                        label = {
                            Text(
                                when(frequency) {
                                    Frequency.YEARLY -> when (interval) {
                                        null, 1 -> stringResource(R.string.recur_every_year)
                                        2 -> stringResource(R.string.recur_every_other_year)
                                        in 3..Int.MAX_VALUE -> stringResource(
                                            R.string.recur_every_x_year,
                                            DateTimeUtils.getLocalizedOrdinalFor(interval ?: 1)
                                        )

                                        else -> interval.toString()
                                    }

                                    Frequency.MONTHLY -> when (interval) {
                                        null, 1 -> stringResource(R.string.recur_every_month)
                                        2 -> stringResource(R.string.recur_every_other_month)
                                        in 3..Int.MAX_VALUE -> stringResource(
                                            R.string.recur_every_x_month,
                                            DateTimeUtils.getLocalizedOrdinalFor(interval ?: 1)
                                        )

                                        else -> interval.toString()
                                    }

                                    Frequency.WEEKLY -> when (interval) {
                                        null, 1 -> stringResource(R.string.recur_every_week)
                                        2 -> stringResource(R.string.recur_every_other_week)
                                        in 3..Int.MAX_VALUE -> stringResource(
                                            R.string.recur_every_x_week,
                                            DateTimeUtils.getLocalizedOrdinalFor(interval ?: 1)
                                        )

                                        else -> interval.toString()
                                    }

                                    Frequency.DAILY -> when (interval) {
                                        null, 1 -> stringResource(R.string.recur_every_day)
                                        2 -> stringResource(R.string.recur_every_other_day)
                                        in 3..Int.MAX_VALUE -> stringResource(
                                            R.string.recur_every_x_day,
                                            DateTimeUtils.getLocalizedOrdinalFor(interval ?: 1)
                                        )

                                        else -> interval.toString()
                                    }

                                    else -> interval.toString()
                                }
                            )

                            DropdownMenu(
                                expanded = intervalExpanded,
                                onDismissRequest = { intervalExpanded = false }
                            ) {
                                val maxInterval = when(frequency) {
                                    Frequency.DAILY -> 30
                                    Frequency.WEEKLY -> 52
                                    Frequency.MONTHLY -> 12
                                    Frequency.YEARLY -> 10
                                    else -> 100
                                }
                                for (number in 1..maxInterval) {
                                    DropdownMenuItem(
                                        onClick = {
                                            interval = number
                                            intervalExpanded = false
                                            onRecurUpdated(buildRRule())
                                        },
                                        text = { Text(
                                            when(frequency) {
                                                Frequency.YEARLY -> when (number) {
                                                    1 -> stringResource(R.string.recur_every_year)
                                                    2 -> stringResource(R.string.recur_every_other_year)
                                                    in 3..Int.MAX_VALUE -> stringResource(
                                                        R.string.recur_every_x_year,
                                                        DateTimeUtils.getLocalizedOrdinalFor(number)
                                                    )

                                                    else -> interval.toString()
                                                }
                                                Frequency.MONTHLY -> when (number) {
                                                        1 -> stringResource(R.string.recur_every_month)
                                                        2 -> stringResource(R.string.recur_every_other_month)
                                                        in 3..Int.MAX_VALUE -> stringResource(
                                                            R.string.recur_every_x_month,
                                                            DateTimeUtils.getLocalizedOrdinalFor(number)
                                                        )

                                                        else -> interval.toString()
                                                    }
                                                Frequency.WEEKLY -> when (number) {
                                                        1 -> stringResource(R.string.recur_every_week)
                                                        2 -> stringResource(R.string.recur_every_other_week)
                                                        in 3..Int.MAX_VALUE -> stringResource(
                                                            R.string.recur_every_x_week,
                                                            DateTimeUtils.getLocalizedOrdinalFor(number)
                                                        )

                                                        else -> interval.toString()
                                                    }
                                                Frequency.DAILY -> when (number) {
                                                        1 -> stringResource(R.string.recur_every_day)
                                                        2 -> stringResource(R.string.recur_every_other_day)
                                                        in 3..Int.MAX_VALUE -> stringResource(
                                                            R.string.recur_every_x_day,
                                                            DateTimeUtils.getLocalizedOrdinalFor(number)
                                                        )

                                                        else -> interval.toString()
                                                    }
                                                else -> number.toString()
                                            }
                                        )}
                                    )
                                }
                            }
                        }
                    )
                    }

                    AnimatedVisibility(monthList.isNotEmpty() || weekDayList.isNotEmpty() || monthDayList.isNotEmpty()) {

                        FlowRow (
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterHorizontally
                            ),
                            verticalArrangement = Arrangement.Center
                        ) {

                            if(monthList.isNotEmpty()) {
                                AssistChip(
                                    onClick = { monthListExpanded = true },
                                    enabled = isEditMode && icalObject.recurid == null,
                                    label = {
                                        Text(
                                            monthList
                                                .map { month -> java.time.Month.of(month.monthOfYear) }
                                                .joinToString(
                                                    separator = ", ",
                                                    transform = { month ->
                                                        month.getDisplayName(
                                                            java.time.format.TextStyle.FULL_STANDALONE,
                                                            Locale.getDefault()
                                                        )
                                                    })
                                        )

                                        DropdownMenu(
                                            expanded = monthListExpanded,
                                            onDismissRequest = { monthListExpanded = false }
                                        ) {

                                            java.time.Month.entries.forEach { month ->
                                                DropdownMenuItem(
                                                    onClick = {
                                                        val ical4jmonth = Month(month.value)
                                                        if (monthList.contains(ical4jmonth) && monthList.size > 1)
                                                            monthList.remove(ical4jmonth)
                                                        else
                                                            monthList.add(ical4jmonth)
                                                        monthListExpanded = false
                                                        onRecurUpdated(buildRRule())
                                                    },
                                                    text = {

                                                        CheckboxWithText(
                                                            text = month.getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, LocalLocale.current.platformLocale),
                                                            isSelected = monthList.contains(Month(month.value)),
                                                            onCheckedChange = {
                                                                val ical4jmonth = Month(month.value)
                                                                if (monthList.contains(ical4jmonth) && monthList.size > 1)
                                                                    monthList.remove(ical4jmonth)
                                                                else
                                                                    monthList.add(ical4jmonth)
                                                                monthListExpanded = false
                                                                onRecurUpdated(buildRRule())
                                                            }
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }

                            if(weekDayList.isNotEmpty() && frequency == Frequency.WEEKLY) {
                                AssistChip(
                                    onClick = { weekDayListExpanded = true },
                                    enabled = isEditMode && icalObject.recurid == null,
                                    label = {
                                        Text(
                                            weekDayList.joinToString(
                                                separator = ", ",
                                                transform = { day ->
                                                    day.asDayOfWeek()
                                                        ?.getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, Locale.getDefault())
                                                        ?: day.toString()
                                                }
                                            )
                                        )

                                        DropdownMenu(
                                            expanded = weekDayListExpanded,
                                            onDismissRequest = { weekDayListExpanded = false }
                                        ) {

                                            weekdays.forEach { weekday ->
                                                DropdownMenuItem(
                                                    onClick = {
                                                        if (weekDayList.contains(weekday))
                                                            weekDayList.remove(weekday)
                                                        else
                                                            weekDayList.add(weekday)

                                                        if(weekDayList.isEmpty())
                                                            weekDayList.add(dtstartWeekday)

                                                        onRecurUpdated(buildRRule())
                                                    },
                                                    text = {
                                                        CheckboxWithText(
                                                            text = weekday.asDayOfWeek()?.getDisplayName(
                                                                java.time.format.TextStyle.FULL_STANDALONE,
                                                                Locale.getDefault()
                                                            ) ?: "",
                                                            isSelected = weekDayList.contains(weekday),
                                                            onCheckedChange = {
                                                                if (weekDayList.contains(weekday))
                                                                    weekDayList.remove(weekday)
                                                                else
                                                                    weekDayList.add(weekday)

                                                                if(weekDayList.isEmpty())
                                                                    weekDayList.add(dtstartWeekday)

                                                                onRecurUpdated(buildRRule())
                                                            }
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }

                            if(weekDayList.isNotEmpty() && (frequency == Frequency.MONTHLY || frequency == Frequency.YEARLY)) {
                                AssistChip(
                                    onClick = { weekDayWithOffsetListExpanded = true },
                                    enabled = isEditMode && icalObject.recurid == null,
                                    label = {
                                        Text(
                                            weekDayList.joinToString(
                                                separator = ", ",
                                                transform = { weekDayWithOffset ->
                                                    if (weekDayWithOffset.offset == -1)
                                                        (context.getString(R.string.recur_last_weekday) + " " + WeekDay(weekDayWithOffset.day.name).asDayOfWeek()
                                                            ?.getDisplayName(
                                                                java.time.format.TextStyle.FULL_STANDALONE,
                                                                Locale.getDefault()
                                                            ))
                                                    else
                                                        (DateTimeUtils.getLocalizedOrdinalFor(weekDayWithOffset.offset) + " " + WeekDay(weekDayWithOffset.day.name).asDayOfWeek()
                                                            ?.getDisplayName(
                                                                java.time.format.TextStyle.FULL_STANDALONE,
                                                                Locale.getDefault()
                                                            ))
                                                }
                                            )
                                        )

                                        DropdownMenu(
                                            expanded = weekDayWithOffsetListExpanded,
                                            onDismissRequest = { weekDayWithOffsetListExpanded = false }
                                        ) {

                                            for (weekDayWithOffset in listOf(WeekDay(dtstartWeekday, 1), WeekDay(dtstartWeekday, 2), WeekDay(dtstartWeekday, 3), WeekDay(dtstartWeekday, 4), WeekDay(dtstartWeekday, 5), WeekDay(dtstartWeekday, -1))) {
                                                DropdownMenuItem(
                                                    onClick = {
                                                        if(weekDayList.contains(weekDayWithOffset))
                                                            weekDayList.remove(weekDayWithOffset)
                                                        else
                                                            weekDayList.add(weekDayWithOffset)

                                                        if(weekDayList.isEmpty())
                                                            weekDayList.add(WeekDay(dtstartWeekday, 1))

                                                        onRecurUpdated(buildRRule())
                                                    },
                                                    text = {
                                                        CheckboxWithText(
                                                            text = if (weekDayWithOffset.offset == -1)
                                                                (context.getString(R.string.recur_last_weekday) + " " + WeekDay(weekDayWithOffset.day.name).asDayOfWeek()
                                                                    ?.getDisplayName(
                                                                        java.time.format.TextStyle.FULL_STANDALONE,
                                                                        LocalLocale.current.platformLocale
                                                                    ))
                                                            else
                                                                (DateTimeUtils.getLocalizedOrdinalFor(weekDayWithOffset.offset) + " " + WeekDay(weekDayWithOffset.day.name).asDayOfWeek()
                                                                    ?.getDisplayName(
                                                                        java.time.format.TextStyle.FULL_STANDALONE,
                                                                        LocalLocale.current.platformLocale
                                                                    )),
                                                            isSelected = weekDayList.contains(weekDayWithOffset),
                                                            onCheckedChange = {
                                                                if(weekDayList.contains(weekDayWithOffset))
                                                                    weekDayList.remove(weekDayWithOffset)
                                                                else
                                                                    weekDayList.add(weekDayWithOffset)

                                                                if(weekDayList.isEmpty())
                                                                    weekDayList.add(WeekDay(dtstartWeekday, 1))

                                                                onRecurUpdated(buildRRule())
                                                            }
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }

                            if(monthDayList.isNotEmpty()) {
                                AssistChip(
                                    onClick = { monthDayListExpanded = true },
                                    label = {
                                        Text(
                                            monthDayList.joinToString(
                                                separator = ", ",
                                                transform = { monthDay ->
                                                    if(monthDay == -1)
                                                        context.getString(R.string.recur_last_day_of_the_month)
                                                    else if(monthDay < 0)
                                                        context.getString(R.string.recur_xth_last_day_of_the_month, DateTimeUtils.getLocalizedOrdinalFor(monthDay.absoluteValue))
                                                    else
                                                        context.getString(R.string.recur_xth_day_of_the_month, DateTimeUtils.getLocalizedOrdinalFor(monthDay))
                                                }
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
                                                        Text(context.getString(R.string.recur_xth_day_of_the_month, DateTimeUtils.getLocalizedOrdinalFor(number)))
                                                    }
                                                )
                                            }

                                            HorizontalDivider()

                                            for (number in 1..31) {
                                                DropdownMenuItem(
                                                    onClick = {
                                                        monthDayList.clear()
                                                        monthDayList.add(number*(-1))
                                                        monthDayListExpanded = false
                                                        onRecurUpdated(buildRRule())
                                                    },
                                                    text = {
                                                        if(number == 1)
                                                            Text(context.getString(R.string.recur_last_day_of_the_month))
                                                        else
                                                            Text(context.getString(R.string.recur_xth_last_day_of_the_month, DateTimeUtils.getLocalizedOrdinalFor(number)))
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }


            AnimatedVisibility(frequency != null) {

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.Center
                ) {

                    AssistChip(
                        onClick = { endsExpanded = true },
                        enabled = isEditMode && icalObject.recurid == null,
                        label = {
                            Text(
                                when {
                                    count != null -> stringResource(id = R.string.recur_ends_after)
                                    until != null -> stringResource(id = R.string.recur_ends_on)
                                    else -> stringResource(id = R.string.recur_ends_never)
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
                                    text = { Text(stringResource(id = R.string.recur_ends_after)) }
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        count = null
                                        until = Date(icalObject.dtstart ?: System.currentTimeMillis())
                                        endsExpanded = false
                                        onRecurUpdated(buildRRule())
                                    },
                                    text = { Text(stringResource(id = R.string.recur_ends_on)) }
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        count = null
                                        until = null
                                        endsExpanded = false
                                        onRecurUpdated(buildRRule())
                                    },
                                    text = { Text(stringResource(id = R.string.recur_ends_never)) }
                                )
                            }
                        }
                    )

                    AnimatedVisibility(count != null) {
                        AssistChip(
                            onClick = { endAfterExpaneded = true },
                            enabled = isEditMode && icalObject.recurid == null,
                            label = {
                                Text(stringResource(R.string.recur_x_occurrences, count?:1))

                                DropdownMenu(
                                    expanded = endAfterExpaneded,
                                    onDismissRequest = { endAfterExpaneded = false }
                                ) {
                                    for (number in 1..100) {
                                        DropdownMenuItem(
                                            onClick = {
                                                count = number
                                                endAfterExpaneded = false
                                                onRecurUpdated(buildRRule())
                                            },
                                            text = {
                                                Text(stringResource(R.string.recur_x_occurrences, number))
                                            }
                                        )
                                    }
                                }
                            }
                        )
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
                if(!isEditMode && !icalObject.recurid.isNullOrEmpty()) {
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

                if(!isEditMode && !icalObject.rrule.isNullOrEmpty()) {
                    Button(
                        onClick = { showDetachAllFromSeriesDialog = true }
                    ) {
                        Text(stringResource(id = R.string.details_detach_all_instances))
                    }
                }
            }

            if(isEditMode)
                icalObject.rrule = buildRRule()?.toString()

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                if (isEditMode) {
                    val instances = icalObject.getInstancesFromRrule()
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
                                text = DateTimeUtils.convertLongToFullDateTimeString(instanceDate, icalObject.dtstartTimezone),
                                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                            )
                        }
                    }
                } else {
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
                    } else {
                        TextButton(onClick = { showAllInstances = true }) {
                            Text(stringResource(id = R.string.details_show_all_instances, seriesInstances.size))
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
            isEditMode = false,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            unlinkFromSeries = { _, _, _ -> }
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
            isEditMode = true,
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
            isEditMode = false,
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
            isEditMode = false,
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
            isEditMode = false,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            unlinkFromSeries = { _, _, _ -> }
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
            isEditMode = true,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            unlinkFromSeries = { _, _, _ -> }
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
            seriesInstances = emptyList(),
            seriesElement = null,
            isEditMode = true,
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
            isEditMode = false,
            hasChildren = false,
            onRecurUpdated = { },
            goToDetail = { _, _, _ -> },
            unlinkFromSeries = { _, _, _ -> }
        )
    }
}
