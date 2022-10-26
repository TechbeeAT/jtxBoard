/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.util.DateTimeUtils

@Composable
fun JournalEntryCard(
    obj: ICal4List,
    modifier: GlanceModifier = GlanceModifier
) {

    val textStyleDate = TextStyle(fontStyle = FontStyle.Italic)
    val textStyleSummary = TextStyle(fontWeight = FontWeight.Bold)

    Card(
        modifier = modifier
    ) {
        Column(
            modifier = GlanceModifier.fillMaxWidth().padding(4.dp)
        ) {
            Text(
                text = DateTimeUtils.convertLongToFullDateTimeString(obj.dtstart, obj.dtstartTimezone),
                style = textStyleDate
            )
            obj.summary?.let { Text(
                text = it,
                style = textStyleSummary
            ) }
            obj.description?.let { Text(it, maxLines = 2) }
        }
    }
}
