/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.CheckboxDefaults
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
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
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.CheckboxPosition

@Composable
fun ListEntry(
    obj: ICal4List,
    entryColor: ColorProvider,
    textColor: ColorProvider,
    headerTextColor: ColorProvider,
    checkboxPosition: CheckboxPosition,
    showDescription: Boolean,
    onCheckedChange: (iCalObjectId: Long, checked: Boolean) -> Unit,
    modifier: GlanceModifier = GlanceModifier
) {

    val context = LocalContext.current
    val textStyleMetaInfo = TextStyle(fontStyle = FontStyle.Italic, fontSize = 12.sp, color = headerTextColor)
    val textStyleDateOverdue = textStyleMetaInfo.copy(color = ColorProvider(Color.Red), fontWeight = FontWeight.Bold)
    val textStyleSummary = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
    val textStyleDescription = TextStyle(color = textColor, fontSize = 12.sp)

    val colorChanged = textStyleMetaInfo.color != GlanceTheme.colors.onSurface

    val intent = Intent(context, MainActivity2::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        action = MainActivity2.INTENT_ACTION_OPEN_ICALOBJECT
        putExtra(MainActivity2.INTENT_EXTRA_ITEM2SHOW, obj.id)
    }

    val imageSize = 18.dp
    val checked = obj.percent == 100 || obj.status == Status.COMPLETED.status

    Column(modifier = modifier) {

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(6.dp)
                .background(entryColor)
                .cornerRadius(8.dp)
                .clickable(onClick = actionStartActivity(intent)),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (obj.module == Module.TODO.name && checkboxPosition == CheckboxPosition.START && !obj.isReadOnly){
                CheckBox(
                    checked = checked,
                    onCheckedChange = { onCheckedChange(obj.id, checked) },
                    colors = if(colorChanged) CheckboxDefaults.colors(textColor.getColor(context), textColor.getColor(context)) else CheckboxDefaults.colors()
                )
            }

            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start
                ) {
                    if (obj.dtstart != null) {
                        Image(
                            provider = ImageProvider(if (obj.module == Module.TODO.name) R.drawable.ic_widget_start else R.drawable.ic_start2),
                            contentDescription = context.getString(R.string.started),
                            modifier = GlanceModifier.size(imageSize).padding(end = 4.dp),
                            colorFilter = ColorFilter.tint(textStyleMetaInfo.color)
                        )
                        Text(
                            text = ICalObject.getDtstartTextInfo(
                                module = obj.getModule(),
                                dtstart = obj.dtstart,
                                dtstartTimezone = obj.dtstartTimezone,
                                shortStyle = true,
                                context = context
                            ),
                            style = textStyleMetaInfo,
                            maxLines = 1,
                            modifier = GlanceModifier.padding(end = 8.dp)
                        )
                    }
                    if (obj.due != null) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_due),
                            contentDescription = context.getString(R.string.due),
                            modifier = GlanceModifier.size(imageSize).padding(end = 4.dp),
                            colorFilter = ColorFilter.tint(textStyleMetaInfo.color)
                        )
                        Text(
                            text = ICalObject.getDueTextInfo(
                                status = obj.status,
                                due = obj.due,
                                dueTimezone = obj.dueTimezone,
                                percent = obj.percent,
                                context = context
                            ),
                            maxLines = 1,
                            style = if(ICalObject.isOverdue(obj.status, obj.percent, obj.due, obj.dueTimezone) == true) textStyleDateOverdue else textStyleMetaInfo,
                            modifier = GlanceModifier.padding(end = 8.dp)
                        )
                    }

                    if (obj.priority in 1..9) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_priority),
                            contentDescription = context.getString(R.string.priority),
                            modifier = GlanceModifier.size(imageSize).padding(end = 4.dp),
                            colorFilter = ColorFilter.tint(textStyleMetaInfo.color)
                        )
                        Text(
                            text = obj.priority.toString(),
                            maxLines = 1,
                            style = textStyleMetaInfo,
                            modifier = GlanceModifier.padding(end = 8.dp)
                        )
                    }

                    if ((obj.status != null && obj.status != Status.FINAL.status) || obj.xstatus != null) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_status),
                            contentDescription = context.getString(R.string.status),
                            modifier = GlanceModifier.size(imageSize).padding(end = 4.dp),
                            colorFilter = ColorFilter.tint(textStyleMetaInfo.color)
                        )
                        Text(
                            text = obj.xstatus
                                ?: Status.getStatusFromString(obj.status)?.stringResource?.let { context.getString(it) }
                                ?:"",
                            maxLines = 1,
                            style = textStyleMetaInfo,
                            modifier = GlanceModifier.padding(end = 8.dp)
                        )
                    }

                    if (obj.classification != null && obj.classification != Classification.PUBLIC.classification) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_classification),
                            contentDescription = context.getString(R.string.classification),
                            modifier = GlanceModifier.size(imageSize).padding(end = 4.dp),
                            colorFilter = ColorFilter.tint(textStyleMetaInfo.color)
                        )
                        Text(
                            text = Classification.getClassificationFromString(obj.classification)?.let { context.getString(it.stringResource) } ?: "",
                            maxLines = 1,
                            style = textStyleMetaInfo,
                            modifier = GlanceModifier.padding(end = 8.dp)
                        )
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

            if (obj.module == Module.TODO.name && checkboxPosition == CheckboxPosition.END && !obj.isReadOnly) {
                CheckBox(
                    checked = checked,
                    onCheckedChange = { onCheckedChange(obj.id, checked) },
                    colors = if(colorChanged) CheckboxDefaults.colors(textColor.getColor(context), textColor.getColor(context)) else CheckboxDefaults.colors()
                )
            }
        }
    }
}
