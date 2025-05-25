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
import java.time.Instant
import java.time.ZonedDateTime


@Composable
fun DetailsCardDates(
    icalObject: ICalObject,
    enableDtstart: Boolean,
    enableDue: Boolean,
    enableCompleted: Boolean,
    allowCompletedChange: Boolean,
    isReadOnly: Boolean,
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

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if((icalObject.module == Module.JOURNAL.name || icalObject.module == Module.TODO.name)
                && (dtstart != null || (enableDtstart || icalObject.getModuleFromString() == Module.JOURNAL))) {
                HorizontalDateCard(
                    datetime = dtstart,
                    timezone = dtstartTimezone,
                    isReadOnly = isReadOnly,
                    onDateTimeChanged = { datetime, timezone ->
                        if((due ?: Long.MAX_VALUE) <= (datetime ?: Long.MIN_VALUE)) {
                            Toast.makeText(context, context.getText(R.string.edit_validation_errors_dialog_due_date_before_dtstart), Toast.LENGTH_LONG).show()
                        } else {
                            dtstart = datetime
                            dtstartTimezone = timezone
                            onDtstartChanged(datetime, timezone)

                            if(datetime == null)
                                return@HorizontalDateCard

                            due?.let {
                                val dueZoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), DateTimeUtils.requireTzId(dueTimezone))
                                if((dueTimezone == TZ_ALLDAY && dtstartTimezone != TZ_ALLDAY)) {
                                    due = dueZoned.withHour(0).withMinute(0).withZoneSameLocal(DateTimeUtils.requireTzId(timezone)).toInstant().toEpochMilli()
                                    dueTimezone = timezone
                                    onDueChanged(due, dueTimezone)
                                } else if (dueTimezone != TZ_ALLDAY && dtstartTimezone == TZ_ALLDAY) {
                                    due = dueZoned.withHour(0).withMinute(0).withZoneSameLocal(DateTimeUtils.requireTzId(timezone)).toInstant().toEpochMilli()
                                    dueTimezone = TZ_ALLDAY
                                    onDueChanged(due, dueTimezone)
                                }
                            }
                        }
                    },
                    pickerMaxDate = due?.let { Instant.ofEpochMilli(it).atZone(DateTimeUtils.requireTzId(dueTimezone)) },
                    labelTop = if(icalObject.module == Module.TODO.name)
                        stringResource(id = R.string.started)
                    else
                        null,
                    allowNull = icalObject.module == Module.TODO.name,
                    dateOnly = false,
                )
            }

            AnimatedVisibility (icalObject.module == Module.TODO.name
                && (due != null || enableDue)) {
                HorizontalDateCard(
                    datetime = due,
                    timezone = dueTimezone,
                    isReadOnly = isReadOnly,
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

                        if(datetime == null)
                            return@HorizontalDateCard

                        dtstart?.let {
                            if((dtstartTimezone == TZ_ALLDAY && dueTimezone != TZ_ALLDAY) || (dtstartTimezone != TZ_ALLDAY && dueTimezone == TZ_ALLDAY)) {
                                val dtstartZoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), DateTimeUtils.requireTzId(dtstartTimezone))
                                dtstart = dtstartZoned.withHour(0).withMinute(0).withZoneSameLocal(DateTimeUtils.requireTzId(timezone)).toInstant().toEpochMilli()
                                dtstartTimezone = timezone
                                onDtstartChanged(dtstart, dtstartTimezone)
                            }
                        }
                    },
                    pickerMinDate = dtstart?.let { Instant.ofEpochMilli(it).atZone(DateTimeUtils.requireTzId(dtstartTimezone)) },
                    labelTop = stringResource(id = R.string.due),
                    allowNull = icalObject.module == Module.TODO.name,
                    dateOnly = false
                )
            }
            AnimatedVisibility (icalObject.module == Module.TODO.name
                && (completed != null || enableCompleted)) {
                HorizontalDateCard(
                    datetime = completed,
                    timezone = completedTimezone,
                    isReadOnly = isReadOnly && allowCompletedChange,
                    onDateTimeChanged = { datetime, timezone ->
                        completed = datetime
                        completedTimezone = timezone
                        onCompletedChanged(datetime, timezone)
                    },
                    pickerMinDate = dtstart?.let { Instant.ofEpochMilli(it).atZone(DateTimeUtils.requireTzId(dtstartTimezone)) },
                    labelTop = stringResource(id = R.string.completed),
                    allowNull = icalObject.module == Module.TODO.name,
                    dateOnly = false
                )
            }
        }

}

@Preview(showBackground = true)
@Composable
fun DetailsCardDates_Journal_Preview() {
    MaterialTheme {
        DetailsCardDates(
            icalObject = ICalObject.createJournal(),
            isReadOnly = false,
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
        DetailsCardDates(
            icalObject = ICalObject.createTodo().apply {
                this.dtstart = System.currentTimeMillis()
                this.dtstartTimezone = TZ_ALLDAY
                this.completed = System.currentTimeMillis()
                this.completedTimezone = TZ_ALLDAY
            },
            isReadOnly = false,
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
        DetailsCardDates(
            icalObject = ICalObject.createJournal(),
            isReadOnly = false,
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
        DetailsCardDates(
            icalObject = ICalObject.createTodo().apply {
                this.due = System.currentTimeMillis()
                this.dueTimezone = TZ_ALLDAY
            },
            isReadOnly = false,
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
        DetailsCardDates(
            icalObject = ICalObject.createTodo().apply {
                this.due = System.currentTimeMillis()
                this.dueTimezone = TZ_ALLDAY
            },
            isReadOnly = false,
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
        DetailsCardDates(
            icalObject = ICalObject.createNote(),
            isReadOnly = false,
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
        DetailsCardDates(
            icalObject = ICalObject.createNote(),
            isReadOnly = false,
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