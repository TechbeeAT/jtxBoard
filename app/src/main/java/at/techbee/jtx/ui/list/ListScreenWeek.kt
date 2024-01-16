/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.relations.ICal4ListRel
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.util.DateTimeUtils
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun ListScreenWeek(
    list: List<ICal4ListRel>,
    selectedEntries: SnapshotStateList<Long>,
    scrollOnceId: MutableLiveData<Long?>,
    onClick: (itemId: Long, list: List<ICal4List>, isReadOnly: Boolean) -> Unit,
    onLongClick: (itemId: Long, list: List<ICal4List>) -> Unit,
) {

    val currentDate = remember { LocalDate.now() }
    val currentMonth = remember(currentDate) { currentDate.yearMonth }
    val startMonth = remember(currentDate) { currentMonth.minusMonths(500) }
    val endMonth = remember(currentDate) { currentMonth.plusMonths(500) }
    val daysOfWeek = remember { daysOfWeek() }

    val scrollId by scrollOnceId.observeAsState(null)
    val weekState = rememberWeekCalendarState(
        startDate = startMonth.atStartOfMonth(),
        endDate = endMonth.atEndOfMonth(),
        firstVisibleWeekDate = currentDate,
        firstDayOfWeek = daysOfWeek.first(),
    )
    val visibleWeek = rememberFirstVisibleWeekAfterScroll(weekState)
    MonthAndWeekCalendarTitle(
        currentMonth = visibleWeek.days.first().date.yearMonth,
        weekState = weekState,
    )

    LaunchedEffect(list, scrollId) {
        if (scrollId == null)
            return@LaunchedEffect

        val foundItem = list.find { it.iCal4List.id == scrollId }?: return@LaunchedEffect
        val date = foundItem.iCal4List.dtstart?:return@LaunchedEffect
        val localDate = LocalDate.ofInstant(Instant.ofEpochMilli(date), DateTimeUtils.requireTzId(foundItem.iCal4List.dtstartTimezone))
        weekState.animateScrollToWeek(localDate)
        scrollOnceId.postValue(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            //.background(Color.White)
    ) {

        //CalendarHeader
        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            for (dayOfWeek in daysOfWeek) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        WeekCalendar(
            state = weekState,
            dayContent = { day ->
                // val isSelectable = day.position == WeekDayPosition.RangeDate
                Day(
                    day = day.date,
                    list = list,
                    selectedEntries = selectedEntries,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
            }
        )
    }
}



@Composable
fun MonthAndWeekCalendarTitle(
    currentMonth: YearMonth,
    weekState: WeekCalendarState,
) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = {
            coroutineScope.launch {
                val targetDate = weekState.firstVisibleWeek.days.first().date.minusDays(1)
                weekState.animateScrollToWeek(targetDate)
            }
        }) {
            Icon(Icons.Outlined.ChevronLeft, stringResource(id = R.string.previous))
        }

        Column(
            modifier = Modifier.weight(1f).padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = currentMonth.year.toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),  //currentMonth.displayText(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        }

        IconButton(onClick = {
            coroutineScope.launch {
                val targetDate = weekState.firstVisibleWeek.days.last().date.plusDays(1)
                weekState.animateScrollToWeek(targetDate)
            }
        }) {
            Icon(Icons.Outlined.ChevronRight, stringResource(id = R.string.next))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Day(
    day: LocalDate,
    list: List<ICal4ListRel>,
    selectedEntries: SnapshotStateList<Long>,
    onClick: (itemId: Long, list: List<ICal4List>, isReadOnly: Boolean) -> Unit,
    onLongClick: (itemId: Long, list: List<ICal4List>) -> Unit
) {

    val list4day = list.filter {
        val localDateTimeStart = LocalDate.ofInstant(Instant.ofEpochMilli(it.iCal4List.dtstart?:0L), DateTimeUtils.requireTzId(it.iCal4List.dtstartTimezone))
        val localDateTimeDueStart = LocalDate.ofInstant(Instant.ofEpochMilli(it.iCal4List.due?:0L), DateTimeUtils.requireTzId(it.iCal4List.dueTimezone))
        localDateTimeStart.atStartOfDay() == day.atStartOfDay()
                || localDateTimeDueStart.atStartOfDay() == day.atStartOfDay()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f) // This is important for square-sizing!
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                fontSize = 14.sp,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            list4day.forEach { iCal4ListRel ->
                ListCardWeek(
                    iCalObject = iCal4ListRel.iCal4List,
                    isDueDate = day.atStartOfDay() == LocalDate.ofInstant(
                        Instant.ofEpochMilli(
                            iCal4ListRel.iCal4List.due ?: 0L
                        ), DateTimeUtils.requireTzId(iCal4ListRel.iCal4List.dueTimezone)
                    ).atStartOfDay(),
                    selected = selectedEntries.contains(iCal4ListRel.iCal4List.id),
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {
                                onClick(
                                    iCal4ListRel.iCal4List.id,
                                    list4day.map { it.iCal4List },
                                    iCal4ListRel.iCal4List.isReadOnly
                                )
                            },
                            onLongClick = {
                                if (!iCal4ListRel.iCal4List.isReadOnly)
                                    onLongClick(
                                        iCal4ListRel.iCal4List.id,
                                        list4day.map { it.iCal4List })
                            }
                        )
                        .aspectRatio(1f)
                        .padding(2.dp)
                )
            }
        }
    }
}


/**
 * Find first visible week in a paged week calendar **after** scrolling stops.
 */
@Composable
fun rememberFirstVisibleWeekAfterScroll(state: WeekCalendarState): Week {
    val visibleWeek = remember(state) { mutableStateOf(state.firstVisibleWeek) }
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }
            .filter { scrolling -> !scrolling }
            .collect { visibleWeek.value = state.firstVisibleWeek }
    }
    return visibleWeek.value
}




@Preview(showBackground = true)
@Composable
fun ListScreenWeek_TODO() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            id = 1L
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.PUBLIC.classification
            dtstart = null
            due = null
            numAttachments = 0
            numSubnotes = 0
            numSubtasks = 0
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        val icalobject2 = ICal4List.getSample().apply {
            id = 2L
            component = Component.VTODO.name
            module = Module.TODO.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.CONFIDENTIAL.classification
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            colorItem = Color.Blue.toArgb()
        }
        ListScreenWeek(
            list = listOf(
                ICal4ListRel(icalobject, emptyList(), emptyList(), emptyList()),
                ICal4ListRel(icalobject2, emptyList(), emptyList(), emptyList())
            ),
            selectedEntries = remember { mutableStateListOf() },
            scrollOnceId = MutableLiveData(null),
            //isPullRefreshEnabled = true,
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            //onSyncRequested = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListScreenWeek_JOURNAL() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            id = 1L
            component = Component.VJOURNAL.name
            module = Module.JOURNAL.name
            percent = 89
            status = Status.FINAL.status
            classification = Classification.PUBLIC.classification
            dtstart = null
            due = null
            numAttachments = 0
            numSubnotes = 0
            numSubtasks = 0
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        val icalobject2 = ICal4List.getSample().apply {
            id = 2L
            component = Component.VJOURNAL.name
            module = Module.JOURNAL.name
            percent = 89
            status = Status.IN_PROCESS.status
            classification = Classification.CONFIDENTIAL.classification
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis()
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            colorItem = Color.Blue.toArgb()
        }
        ListScreenWeek(
            list = listOf(
                ICal4ListRel(icalobject, emptyList(), emptyList(), emptyList()),
                ICal4ListRel(icalobject2, emptyList(), emptyList(), emptyList())
            ),
            selectedEntries = remember { mutableStateListOf() },
            scrollOnceId = MutableLiveData(null),
            //isPullRefreshEnabled = true,
            onClick = { _, _, _ -> },
            onLongClick = { _, _ -> },
            //onSyncRequested = { }
        )
    }
}

