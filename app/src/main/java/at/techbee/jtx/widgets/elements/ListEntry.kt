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
import androidx.glance.*
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.widgets.GlanceTheme
import at.techbee.jtx.widgets.ListWidgetCheckedActionCallback
import at.techbee.jtx.widgets.TintImage
import at.techbee.jtx.widgets.px

@Composable
fun ListEntry(
    obj: ICal4List,
    groupedList: Map<String?, List<ICal4List>>,
    textColor: ColorProvider,
    containerColor: ColorProvider,
    checkboxEnd: Boolean
) {

    if(obj.summary.isNullOrEmpty() && obj.description.isNullOrEmpty())
        return

    val context = LocalContext.current
    val textStyleDate = TextStyle(fontStyle = FontStyle.Italic, fontSize = 12.sp, color = textColor)
    val textStyleSummary =
        TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
    val textStyleDescription = TextStyle(color = textColor, fontSize = 12.sp)

    val intent = Intent(context, MainActivity2::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        this.action = MainActivity2.INTENT_ACTION_OPEN_ICALOBJECT
        this.putExtra(MainActivity2.INTENT_EXTRA_ITEM2SHOW, obj.id)
    }

    val imageSize = 18.dp

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .background(containerColor)
            .cornerRadius(8.dp)
    ) {


        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(onClick = actionStartActivity(intent)),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (obj.module == Module.TODO.name && !checkboxEnd) {
                if(!obj.isReadOnly) {
                    CheckBox(
                        checked = obj.percent == 100,
                        onCheckedChange = actionRunCallback<ListWidgetCheckedActionCallback>(
                            parameters = actionParametersOf(
                                ListWidgetCheckedActionCallback.actionWidgetIcalObjectId to obj.id,
                            )
                        )
                    )
                } else {
                    Spacer(modifier = GlanceModifier.size(24.dp))
                }
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
                            TintImage(
                                resource = if (obj.module == Module.TODO.name) R.drawable.ic_start else R.drawable.ic_start2,
                                tintColor = GlanceTheme.colors.onPrimaryContainer,
                                contentDescription = context.getString(R.string.started),
                                imageHeight = imageSize.px,
                                modifier = GlanceModifier.size(imageSize).padding(end = 4.dp)
                            )
                            Text(
                                text = DateTimeUtils.convertLongToMediumDateString(
                                    obj.dtstart,
                                    obj.dtstartTimezone
                                ),
                                style = textStyleDate,
                                modifier = GlanceModifier.padding(end = 8.dp)
                            )
                        }
                        if (obj.due != null) {
                            TintImage(
                                resource = R.drawable.ic_due,
                                tintColor = GlanceTheme.colors.onPrimaryContainer,
                                contentDescription = context.getString(R.string.due),
                                imageHeight = imageSize.px,
                                modifier = GlanceModifier.size(imageSize).padding(end = 4.dp),
                            )
                            Text(
                                text = DateTimeUtils.convertLongToMediumDateString(
                                    obj.due,
                                    obj.dueTimezone
                                ),
                                style = textStyleDate
                            )
                        }
                    }
                }

                Column(modifier = GlanceModifier.defaultWeight()) {
                    if (!obj.summary.isNullOrEmpty())
                        Text(
                            text = obj.summary!!,
                            style = textStyleSummary,
                            modifier = GlanceModifier.fillMaxWidth().clickable(onClick = actionStartActivity(intent))
                        )
                    if (!obj.description.isNullOrEmpty())
                        Text(
                            obj.description!!,
                            maxLines = 2,
                            style = textStyleDescription,
                            modifier = GlanceModifier.fillMaxWidth().clickable(onClick = actionStartActivity(intent))
                        )
                }
            }

            if (obj.module == Module.TODO.name && checkboxEnd) {
                if(!obj.isReadOnly) {
                    CheckBox(
                        checked = obj.percent == 100,
                        onCheckedChange = actionRunCallback<ListWidgetCheckedActionCallback>(
                            parameters = actionParametersOf(
                                ListWidgetCheckedActionCallback.actionWidgetIcalObjectId to obj.id,
                            )
                        )
                    )
                } else {
                    Spacer(modifier = GlanceModifier.size(24.dp))
                }
            }
        }

        ListSubEntries(
            parentUID = obj.uid,
            groupedList = groupedList,
            textColor = textColor,
            containerColor = containerColor,
            checkboxEnd = checkboxEnd,
            modifier = GlanceModifier.fillMaxWidth()
        )
    }
}
