/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets.elements

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.util.DateTimeUtils

@Composable
fun JournalEntry(
    obj: ICal4List,
    textColor: ColorProvider,
    containerColor: ColorProvider
) {

    val context = LocalContext.current
    val textStyleDate = TextStyle(fontStyle = FontStyle.Italic, color = textColor)
    val textStyleSummary = TextStyle(fontWeight = FontWeight.Bold, color = textColor)
    val textStyleDescription = TextStyle(color = textColor)

    val intent = Intent(context, MainActivity2::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        this.action = MainActivity2.INTENT_ACTION_OPEN_ICALOBJECT
        this.putExtra(MainActivity2.INTENT_EXTRA_ITEM2SHOW, obj.id)
    }

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(containerColor)
            .cornerRadius(16.dp)
            .clickable(onClick = actionStartActivity(intent))
    ) {
        Text(
            text = DateTimeUtils.convertLongToFullDateTimeString(obj.dtstart, obj.dtstartTimezone),
            style = textStyleDate
        )
        obj.summary?.let { Text(
            text = it,
            style = textStyleSummary
        ) }
        obj.description?.let { Text(it, maxLines = 2, style = textStyleDescription) }
        //CustomWidgetDivider(color = textColor, modifier = GlanceModifier.padding(top = 8.dp))
    }
}
