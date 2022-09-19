/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.reusable.cards.HorizontalDateCard
import at.techbee.jtx.util.DateTimeUtils


@Composable
fun DetailsCardDates(
    icalObject: ICalObject,
    isEditMode: Boolean,
    onDtstartChanged: (Long?, String?) -> Unit,
    onDueChanged: (Long?, String?) -> Unit,
    onCompletedChanged: (Long?, String?) -> Unit,
    modifier: Modifier = Modifier
) {

    var dtstart by rememberSaveable { mutableStateOf(icalObject.dtstart) }
    var dtstartTimezone by rememberSaveable { mutableStateOf(icalObject.dtstartTimezone) }
    var due by rememberSaveable { mutableStateOf(icalObject.due) }
    var dueTimezone by rememberSaveable { mutableStateOf(icalObject.dueTimezone) }
    var completed by rememberSaveable { mutableStateOf(icalObject.completed) }
    var completedTimezone by rememberSaveable { mutableStateOf(icalObject.completedTimezone) }

    if(icalObject.module == Module.NOTE.name)
        return

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(if(isEditMode) 4.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if((icalObject.module == Module.JOURNAL.name || icalObject.module == Module.TODO.name)
                && (dtstart != null || isEditMode)) {
                HorizontalDateCard(
                    datetime = dtstart,
                    timezone = dtstartTimezone,
                    isEditMode = isEditMode,
                    onDateTimeChanged = { datetime, timezone ->
                        dtstart = datetime
                        dtstartTimezone = timezone
                        //icalObject.dtstart = dtstart
                        //icalObject.dtstartTimezone = dtstartTimezone
                        onDtstartChanged(datetime, timezone)
                    },
                    pickerMaxDate = DateTimeUtils.getDateWithoutTime(due, dueTimezone),
                    labelTop = if(icalObject.module == Module.TODO.name)
                        stringResource(id = R.string.started)
                    else
                        null,
                    allowNull = icalObject.module == Module.TODO.name
                )
            }

            if(icalObject.module == Module.TODO.name
                && (due != null || isEditMode)) {
                HorizontalDateCard(
                    datetime = due,
                    timezone = dueTimezone,
                    isEditMode = isEditMode,
                    onDateTimeChanged = { datetime, timezone ->
                        due = datetime
                        dueTimezone = timezone
                        onDueChanged(datetime, timezone)
                        //icalObject.due = due
                        //icalObject.dueTimezone = dueTimezone
                    },
                    pickerMinDate = DateTimeUtils.getDateWithoutTime(dtstart, dtstartTimezone),
                    labelTop = stringResource(id = R.string.due),
                    allowNull = icalObject.module == Module.TODO.name
                )
            }
            if(icalObject.module == Module.TODO.name
                && (completed != null || isEditMode)) {
                HorizontalDateCard(
                    datetime = completed,
                    timezone = completedTimezone,
                    isEditMode = isEditMode,
                    onDateTimeChanged = { datetime, timezone ->
                        completed = datetime
                        completedTimezone = timezone
                        onCompletedChanged(datetime, timezone)
                        //icalObject.completed = completed
                        //icalObject.completedTimezone = completedTimezone
                    },
                    pickerMinDate = DateTimeUtils.getDateWithoutTime(dtstart, dtstartTimezone),
                    labelTop = stringResource(id = R.string.completed),
                    allowNull = icalObject.module == Module.TODO.name
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardDates_Journal_Preview() {
    MaterialTheme {
        DetailsCardDates(
            icalObject = ICalObject.createJournal(),
            isEditMode = false,
            onDtstartChanged = { _, _ -> },
            onDueChanged = { _, _ -> },
            onCompletedChanged = { _, _ -> }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun DetailsCardDates_Todo_Preview() {
    MaterialTheme {
        DetailsCardDates(
            icalObject = ICalObject.createTodo().apply {
                this.dtstart = System.currentTimeMillis()
                this.dtstartTimezone = TZ_ALLDAY
                this.completed = System.currentTimeMillis()
                this.completedTimezone = TZ_ALLDAY
            },
            isEditMode = false,
            onDtstartChanged = { _, _ -> },
            onDueChanged = { _, _ -> },
            onCompletedChanged = { _, _ -> }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun DetailsCardDates_Journal_edit_Preview() {
    MaterialTheme {
        DetailsCardDates(
            icalObject = ICalObject.createJournal(),
            isEditMode = true,
            onDtstartChanged = { _, _ -> },
            onDueChanged = { _, _ -> },
            onCompletedChanged = { _, _ -> }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun DetailsCardDates_Todo_edit_Preview() {
    MaterialTheme {
        DetailsCardDates(
            icalObject = ICalObject.createTodo().apply {
                this.due = System.currentTimeMillis()
                this.dueTimezone = TZ_ALLDAY
            },
            isEditMode = true,
            onDtstartChanged = { _, _ -> },
            onDueChanged = { _, _ -> },
            onCompletedChanged = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardDates_Note_edit_Preview() {
    MaterialTheme {
        DetailsCardDates(
            icalObject = ICalObject.createNote(),
            isEditMode = true,
            onDtstartChanged = { _, _ -> },
            onDueChanged = { _, _ -> },
            onCompletedChanged = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardDates_Note_Preview() {
    MaterialTheme {
        DetailsCardDates(
            icalObject = ICalObject.createNote(),
            isEditMode = false,
            onDtstartChanged = { _, _ -> },
            onDueChanged = { _, _ -> },
            onCompletedChanged = { _, _ -> }
        )
    }
}