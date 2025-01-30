/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.theme.jtxCardBorderStrokeWidth
import kotlin.time.Duration.Companion.days


@Composable
fun ListCardWeek(
    iCalObject: ICal4List,
    selected: Boolean,
    modifier: Modifier = Modifier,
    isDueDate: Boolean = false
) {
    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else CardDefaults.elevatedCardColors().containerColor,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else CardDefaults.elevatedCardColors().contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(),
        border = iCalObject.colorItem?.let { BorderStroke(jtxCardBorderStrokeWidth, Color(it)) },
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            val isOverdue = ICalObject.isOverdue(iCalObject.status, iCalObject.percent, iCalObject.due, iCalObject.dueTimezone) ?: false
            var text = if (iCalObject.status == Status.COMPLETED.status || iCalObject.percent == 100)
                "✔"
            else if (isDueDate) "❗"
            else ""
            text += if(iCalObject.summary?.isNotBlank() == true) iCalObject.summary!!.trim() else iCalObject.description ?: ""

            Text(
                text = text,
                textDecoration = if (iCalObject.status == Status.CANCELLED.status) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = when {
                    isDueDate && isOverdue -> MaterialTheme.colorScheme.error
                    isDueDate && !isOverdue -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.bodySmall, 
                lineHeight = 12.sp,
                modifier = if (iCalObject.status == Status.CANCELLED.status) Modifier.alpha(0.5F) else Modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardWeek_JOURNAL() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            dtstart = System.currentTimeMillis()
            colorCollection = Color.Green.toArgb()
            colorItem = Color.Magenta.toArgb()
        }
        ListCardWeek(
            iCalObject = icalobject,
            selected = false)
    }
}



@Preview(showBackground = true)
@Composable
fun ListCardWeek_NOTE() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VJOURNAL.name
            module = Module.NOTE.name
            dtstart = null
            dtstartTimezone = null
            status = Status.CANCELLED.status
        }
        ListCardWeek(
            iCalObject = icalobject,
            selected = true,
            modifier = Modifier.width(150.dp)
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ListCardWeek_TODO_due() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VTODO.name
            module = Module.TODO.name
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis() + (1).days.inWholeMilliseconds
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        ListCardWeek(
            iCalObject = icalobject,
            selected = false,
            isDueDate = true
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ListCardWeek_TODO_overdue() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VTODO.name
            module = Module.TODO.name
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis() - (1).days.inWholeMilliseconds
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        }
        ListCardWeek(
            iCalObject = icalobject,
            selected = false,
            isDueDate = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListCardWeek_TODO_overdue_done() {
    MaterialTheme {

        val icalobject = ICal4List.getSample().apply {
            component = Component.VTODO.name
            module = Module.TODO.name
            dtstart = System.currentTimeMillis()
            due = System.currentTimeMillis() - (1).days.inWholeMilliseconds
            summary =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            status = Status.COMPLETED.status
        }
        ListCardWeek(
            iCalObject = icalobject,
            selected = false,
            isDueDate = true
        )
    }
}