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
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.widgets.ListWidgetCheckedActionCallback

@Composable
fun ListEntry(
    obj: ICal4List,
    entryColor: ColorProvider,
    textColor: ColorProvider,
    textColorOverdue: ColorProvider,
    checkboxEnd: Boolean,
    showDescription: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {

    val context = LocalContext.current
    val textStyleDate = TextStyle(fontStyle = FontStyle.Italic, fontSize = 12.sp, color = textColor)
    val textStyleDateOverdue = textStyleDate.copy(color = textColorOverdue, fontWeight = FontWeight.Bold)
    val textStyleSummary = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
    val textStyleDescription = TextStyle(color = textColor, fontSize = 12.sp)

    val intent = Intent(context, MainActivity2::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        action = MainActivity2.INTENT_ACTION_OPEN_ICALOBJECT
        putExtra(MainActivity2.INTENT_EXTRA_ITEM2SHOW, obj.id)
    }

    val imageSize = 18.dp

    Box(modifier = modifier) {

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(6.dp)
                .background(entryColor)
                .cornerRadius(8.dp)
                .clickable(onClick = actionStartActivity(intent)),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (obj.module == Module.TODO.name && !checkboxEnd && !obj.isReadOnly){
                CheckBox(
                    checked = obj.percent == 100 || obj.status == Status.COMPLETED.status,
                    onCheckedChange = actionRunCallback<ListWidgetCheckedActionCallback>(
                        parameters = actionParametersOf(
                            ListWidgetCheckedActionCallback.actionWidgetIcalObjectId to obj.id,
                        )
                    )
                )
            }

            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {

                if (obj.dtstart != null || obj.due != null) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (obj.dtstart != null) {
                            Image(
                                provider = ImageProvider(if (obj.module == Module.TODO.name) R.drawable.ic_widget_start else R.drawable.ic_start2),
                                contentDescription = context.getString(R.string.started),
                                modifier = GlanceModifier.size(imageSize).padding(end = 4.dp),
                                colorFilter = ColorFilter.tint(textColor)
                            )
                            Text(
                                text = DateTimeUtils.convertLongToMediumDateShortTimeString(
                                    obj.dtstart,
                                    obj.dtstartTimezone
                                ),
                                style = textStyleDate,
                                modifier = GlanceModifier.padding(end = 8.dp)
                            )
                        }
                        if (obj.due != null) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_widget_due),
                                contentDescription = context.getString(R.string.due),
                                modifier = GlanceModifier.size(imageSize).padding(end = 4.dp),
                                colorFilter = ColorFilter.tint(textColor)
                            )
                            Text(
                                text = DateTimeUtils.convertLongToMediumDateShortTimeString(
                                    obj.due,
                                    obj.dueTimezone
                                ),
                                style = if(ICalObject.isOverdue(obj.status, obj.percent, obj.due, obj.dueTimezone) == true) textStyleDateOverdue else textStyleDate,
                            )
                        }
                    }
                }

                Column(modifier = GlanceModifier.defaultWeight()) {
                    if (!obj.summary.isNullOrEmpty())
                        Text(
                            text = obj.summary!!,
                            style = textStyleSummary,
                            modifier = GlanceModifier.fillMaxWidth()
                        )
                    if (!obj.description.isNullOrEmpty() && showDescription)
                        Text(
                            obj.description!!,
                            maxLines = 2,
                            style = textStyleDescription,
                            modifier = GlanceModifier.fillMaxWidth()
                        )
                }
            }

            if (obj.module == Module.TODO.name && checkboxEnd && !obj.isReadOnly) {
                CheckBox(
                    checked = obj.percent == 100 || obj.status == Status.COMPLETED.status,
                    onCheckedChange = actionRunCallback<ListWidgetCheckedActionCallback>(
                        parameters = actionParametersOf(
                            ListWidgetCheckedActionCallback.actionWidgetIcalObjectId to obj.id,
                        )
                    )
                )
            }
        }
    }
}
