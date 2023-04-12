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

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(if(isEditMode) 4.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if((icalObject.module == Module.JOURNAL.name || icalObject.module == Module.TODO.name)
                && (icalObject.dtstart != null || (isEditMode && (enableDtstart || icalObject.getModuleFromString() == Module.JOURNAL)))) {
                HorizontalDateCard(
                    datetime = icalObject.dtstart,
                    timezone = if((icalObject.due != null && icalObject.dueTimezone == TZ_ALLDAY) || (icalObject.completed != null && icalObject.completedTimezone == TZ_ALLDAY)) TZ_ALLDAY else icalObject.dtstartTimezone,
                    isEditMode = isEditMode,
                    onDateTimeChanged = { datetime, timezone ->
                        if((icalObject.due ?: Long.MAX_VALUE) <= (datetime ?: Long.MIN_VALUE)) {
                            Toast.makeText(
                                context,
                                context.getText(R.string.edit_validation_errors_dialog_due_date_before_dtstart),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            icalObject.dtstart = datetime
                            icalObject.dtstartTimezone = timezone
                            onDtstartChanged(datetime, timezone)
                        }
                    },
                    pickerMaxDate = DateTimeUtils.getDateWithoutTime(icalObject.due, icalObject.dueTimezone)?.let {
                        if(icalObject.dueTimezone == TZ_ALLDAY)
                            it - (1).days.inWholeMilliseconds
                        else
                            it
                        },
                    labelTop = if(icalObject.module == Module.TODO.name)
                        stringResource(id = R.string.started)
                    else
                        null,
                    allowNull = icalObject.module == Module.TODO.name,
                    dateOnly = (icalObject.due != null && icalObject.dueTimezone == TZ_ALLDAY) || (icalObject.completed != null && icalObject.completedTimezone == TZ_ALLDAY),
                    enforceTime = (icalObject.due != null && icalObject.dueTimezone != TZ_ALLDAY) || (icalObject.completed != null && icalObject.completedTimezone != TZ_ALLDAY)
                )
            }

            AnimatedVisibility (icalObject.module == Module.TODO.name
                && (icalObject.due != null || (isEditMode && enableDue))) {
                HorizontalDateCard(
                    datetime = icalObject.due,
                    timezone = if((icalObject.dtstart != null && icalObject.dtstartTimezone == TZ_ALLDAY) || (icalObject.completed != null && icalObject.completedTimezone == TZ_ALLDAY)) TZ_ALLDAY else icalObject.dueTimezone,
                    isEditMode = isEditMode,
                    onDateTimeChanged = { datetime, timezone ->
                        if((datetime ?: Long.MAX_VALUE) <= (icalObject.dtstart ?: Long.MIN_VALUE)) {
                            Toast.makeText(
                                context,
                                context.getText(R.string.edit_validation_errors_dialog_due_date_before_dtstart),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            icalObject.due = datetime
                            icalObject.dueTimezone = timezone
                            onDueChanged(datetime, timezone)
                        }
                    },
                    pickerMinDate = DateTimeUtils.getDateWithoutTime(icalObject.dtstart, icalObject.dtstartTimezone)?.let {
                        if(icalObject.dtstartTimezone == TZ_ALLDAY)
                            it + (1).days.inWholeMilliseconds
                        else
                            it
                    },
                    labelTop = stringResource(id = R.string.due),
                    allowNull = icalObject.module == Module.TODO.name,
                    dateOnly = (icalObject.dtstart != null && icalObject.dtstartTimezone == TZ_ALLDAY) || (icalObject.completed != null && icalObject.completedTimezone == TZ_ALLDAY),
                    enforceTime = (icalObject.dtstart != null && icalObject.dtstartTimezone != TZ_ALLDAY) || (icalObject.completed != null && icalObject.completedTimezone != TZ_ALLDAY)
                )
            }
            AnimatedVisibility (icalObject.module == Module.TODO.name
                && (icalObject.completed != null || (isEditMode && enableCompleted))) {
                HorizontalDateCard(
                    datetime = icalObject.completed,
                    timezone = if((icalObject.dtstart != null && icalObject.dtstartTimezone == TZ_ALLDAY) || (icalObject.due != null && icalObject.dueTimezone == TZ_ALLDAY)) TZ_ALLDAY else icalObject.completedTimezone,
                    isEditMode = isEditMode,
                    onDateTimeChanged = { datetime, timezone ->
                        icalObject.completed = datetime
                        icalObject.completedTimezone = timezone
                        onCompletedChanged(datetime, timezone)
                    },
                    pickerMinDate = DateTimeUtils.getDateWithoutTime(icalObject.dtstart, icalObject.dtstartTimezone),
                    labelTop = stringResource(id = R.string.completed),
                    allowNull = icalObject.module == Module.TODO.name,
                    dateOnly = (icalObject.dtstart != null && icalObject.dtstartTimezone == TZ_ALLDAY) || (icalObject.due != null && icalObject.dueTimezone == TZ_ALLDAY),
                    enforceTime = (icalObject.dtstart != null && icalObject.dtstartTimezone != TZ_ALLDAY) || (icalObject.due != null && icalObject.dueTimezone != TZ_ALLDAY),
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