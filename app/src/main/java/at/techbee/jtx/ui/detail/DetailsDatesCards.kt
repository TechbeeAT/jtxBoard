/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.reusable.cards.HorizontalDateCard
import at.techbee.jtx.util.DateTimeUtils
import kotlin.time.Duration.Companion.days


@Composable
fun DetailsDatesCards(
    icalObject: ICalObject,
    isEditMode: Boolean,
    enableDtstart: Boolean,
    enableDue: Boolean,
    enableCompleted: Boolean,
    allowCompletedChange: Boolean,
    onDtstartChanged: (Long?, String?) -> Unit,
    onDueChanged: (Long?, String?) -> Unit,
    onCompletedChanged: (Long?, String?) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    if(icalObject.module == Module.NOTE.name)
        return

    var dtstart by remember { mutableStateOf(icalObject.dtstart) }
    var dtstartTimezone by remember { mutableStateOf(icalObject.dtstartTimezone) }
    var due by remember { mutableStateOf(icalObject.due) }
    var dueTimezone by remember { mutableStateOf(icalObject.dueTimezone) }
    var completed by remember { mutableStateOf(icalObject.completed) }
    var completedTimezone by remember { mutableStateOf(icalObject.completedTimezone) }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(if(isEditMode) 4.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if((icalObject.module == Module.JOURNAL.name || icalObject.module == Module.TODO.name)
                && (dtstart != null || (isEditMode && (enableDtstart || icalObject.getModuleFromString() == Module.JOURNAL)))) {
                HorizontalDateCard(
                    datetime = dtstart,
                    timezone = if(dtstart == null && due != null) dueTimezone else dtstartTimezone,
                    isEditMode = isEditMode,
                    onDateTimeChanged = { datetime, timezone ->
                        if((due ?: Long.MAX_VALUE) <= (datetime ?: Long.MIN_VALUE)) {
                            Toast.makeText(
                                context,
                                context.getText(R.string.edit_validation_errors_dialog_due_date_before_dtstart),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            dtstart = datetime
                            dtstartTimezone = timezone
                            onDtstartChanged(datetime, timezone)

                            if(due != null && (dueTimezone == TZ_ALLDAY && dtstartTimezone != TZ_ALLDAY)) {
                                due = DateTimeUtils.getDateWithoutTime(due, timezone)
                                dueTimezone = timezone
                                onDueChanged(due, dueTimezone)
                            }

                            if(due != null && (dueTimezone != TZ_ALLDAY && dtstartTimezone == TZ_ALLDAY)) {
                                dueTimezone = timezone
                                onDueChanged(due, dueTimezone)
                            }
                        }
                    },
                    pickerMaxDate = DateTimeUtils.getDateWithoutTime(due, dueTimezone)?.let {
                        if(dueTimezone == TZ_ALLDAY)
                            it - (1).days.inWholeMilliseconds
                        else
                            it
                        },
                    labelTop = if(icalObject.module == Module.TODO.name)
                        stringResource(id = R.string.started)
                    else
                        null,
                    allowNull = icalObject.module == Module.TODO.name,
                    dateOnly = false
                )
            }

            AnimatedVisibility (icalObject.module == Module.TODO.name
                && (due != null || (isEditMode && enableDue))) {
                HorizontalDateCard(
                    datetime = due,
                    timezone = if(due == null && dtstart != null) dtstartTimezone else dueTimezone,
                    isEditMode = isEditMode,
                    onDateTimeChanged = { datetime, timezone ->
                        if((datetime ?: Long.MAX_VALUE) <= (dtstart ?: Long.MIN_VALUE)) {
                            Toast.makeText(
                                context,
                                context.getText(R.string.edit_validation_errors_dialog_due_date_before_dtstart),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            due = datetime
                            dueTimezone = timezone
                            onDueChanged(datetime, timezone)
                        }

                        if(dtstart != null && (dtstartTimezone == TZ_ALLDAY && dueTimezone != TZ_ALLDAY)) {
                            dtstart = DateTimeUtils.getDateWithoutTime(dtstart, timezone)
                            dtstartTimezone = timezone
                            onDtstartChanged(dtstart, dtstartTimezone)
                        }

                        if(dtstart != null && (dtstartTimezone != TZ_ALLDAY && dueTimezone == TZ_ALLDAY)) {
                            dtstartTimezone = timezone
                            onDtstartChanged(dtstart, dtstartTimezone)
                        }
                    },
                    pickerMinDate = DateTimeUtils.getDateWithoutTime(dtstart, dtstartTimezone)?.let {
                        if(dtstartTimezone == TZ_ALLDAY)
                            it + (1).days.inWholeMilliseconds
                        else
                            it
                    },
                    labelTop = stringResource(id = R.string.due),
                    allowNull = icalObject.module == Module.TODO.name,
                    dateOnly = false,
                )
            }
            AnimatedVisibility (icalObject.module == Module.TODO.name
                && (completed != null || (isEditMode && enableCompleted))) {
                HorizontalDateCard(
                    datetime = completed,
                    timezone = if((dtstart != null && dtstartTimezone == TZ_ALLDAY) || (due != null && dueTimezone == TZ_ALLDAY)) TZ_ALLDAY else completedTimezone,
                    isEditMode = isEditMode,
                    onDateTimeChanged = { datetime, timezone ->
                        completed = datetime
                        completedTimezone = timezone
                        onCompletedChanged(datetime, timezone)
                    },
                    pickerMinDate = DateTimeUtils.getDateWithoutTime(dtstart, dtstartTimezone),
                    labelTop = stringResource(id = R.string.completed),
                    allowNull = icalObject.module == Module.TODO.name,
                    dateOnly = false,
                    enabled = allowCompletedChange
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsCardDates_Journal_Preview() {
    MaterialTheme {
        DetailsDatesCards(
            icalObject = ICalObject.createJournal(),
            isEditMode = false,
            enableDtstart = true,
            enableDue = false,
            enableCompleted = false,
            allowCompletedChange = true,
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
        DetailsDatesCards(
            icalObject = ICalObject.createTodo().apply {
                this.dtstart = System.currentTimeMillis()
                this.dtstartTimezone = TZ_ALLDAY
                this.completed = System.currentTimeMillis()
                this.completedTimezone = TZ_ALLDAY
            },
            isEditMode = false,
            enableDtstart = true,
            enableDue = false,
            enableCompleted = false,
            allowCompletedChange = true,
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
        DetailsDatesCards(
            icalObject = ICalObject.createJournal(),
            isEditMode = true,
            enableDtstart = true,
            enableDue = true,
            enableCompleted = false,
            allowCompletedChange = true,
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
        DetailsDatesCards(
            icalObject = ICalObject.createTodo().apply {
                this.due = System.currentTimeMillis()
                this.dueTimezone = TZ_ALLDAY
            },
            isEditMode = true,
            enableDtstart = true,
            enableDue = false,
            enableCompleted = true,
            allowCompletedChange = true,
            onDtstartChanged = { _, _ -> },
            onDueChanged = { _, _ -> },
            onCompletedChanged = { _, _ -> }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardDates_Todo_edit_Preview_completed_hidden() {
    MaterialTheme {
        DetailsDatesCards(
            icalObject = ICalObject.createTodo().apply {
                this.due = System.currentTimeMillis()
                this.dueTimezone = TZ_ALLDAY
            },
            isEditMode = true,
            enableDtstart = true,
            enableDue = false,
            enableCompleted = false,
            allowCompletedChange = true,
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
        DetailsDatesCards(
            icalObject = ICalObject.createNote(),
            isEditMode = true,
            enableDtstart = true,
            enableDue = true,
            enableCompleted = true,
            allowCompletedChange = true,
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
        DetailsDatesCards(
            icalObject = ICalObject.createNote(),
            isEditMode = false,
            enableDtstart = true,
            enableDue = true,
            enableCompleted = true,
            allowCompletedChange = true,
            onDtstartChanged = { _, _ -> },
            onDueChanged = { _, _ -> },
            onCompletedChanged = { _, _ -> }
        )
    }
}