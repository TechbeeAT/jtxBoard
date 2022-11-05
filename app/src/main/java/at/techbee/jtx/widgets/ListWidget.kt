/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.ListWidgetConfigActivity
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.widgets.elements.ListEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class ListWidget : GlanceAppWidget() {


    @Composable
    override fun Content() {

        val context = LocalContext.current
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(LocalGlanceId.current)
        val prefs = currentState<Preferences>()
        val listWidgetConfig = prefs[ListWidgetReceiver.filterConfig]?.let { filterConfig ->
            Json.decodeFromString<ListWidgetConfig>(filterConfig)
        }
        val list = prefs[ListWidgetReceiver.list]?.map {
            Json.decodeFromString<ICal4List>(it)
        } ?: emptyList()

        val configIntent = Intent(context, ListWidgetConfigActivity::class.java)
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        val imageSize = 36.dp


        GlanceTheme {

            Column(
                modifier = GlanceModifier
                    .appWidgetBackground()
                    .fillMaxSize()
                    .padding(4.dp)
                    .background(GlanceTheme.colors.primaryContainer),
            ) {

                val addNewIntent = Intent(context, MainActivity2::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    action = when (listWidgetConfig?.module) {
                        Module.JOURNAL -> MainActivity2.INTENT_ACTION_ADD_JOURNAL
                        Module.NOTE -> MainActivity2.INTENT_ACTION_ADD_NOTE
                        Module.TODO -> MainActivity2.INTENT_ACTION_ADD_TODO
                        else -> MainActivity2.INTENT_ACTION_ADD_NOTE
                    }
                }


                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(GlanceTheme.colors.primaryContainer),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TintImage(
                        resource = R.drawable.ic_jtx,
                        tintColor = GlanceTheme.colors.onPrimaryContainer,
                        contentDescription = context.getString(R.string.widget_list_configuration),
                        imageHeight = imageSize.px,
                        modifier = GlanceModifier
                            .clickable(actionStartActivity(configIntent))
                            .padding(8.dp)
                            .size(imageSize),
                    )

                    Text(
                        text = when (listWidgetConfig?.module) {
                            Module.JOURNAL -> context.getString(R.string.list_tabitem_journals)
                            Module.NOTE -> context.getString(R.string.list_tabitem_notes)
                            Module.TODO -> context.getString(R.string.list_tabitem_todos)
                            else -> context.getString(R.string.list_tabitem_notes)
                        },
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier
                            .defaultWeight()
                    )

                    TintImage(
                        resource = R.drawable.ic_settings,
                        tintColor = GlanceTheme.colors.onPrimaryContainer,
                        contentDescription = context.getString(R.string.widget_list_configuration),
                        imageHeight = imageSize.px,
                        modifier = GlanceModifier
                            .clickable(actionStartActivity(configIntent))
                            .padding(8.dp)
                            .size(imageSize),
                    )

                    TintImage(
                        resource = R.drawable.ic_edit,
                        tintColor = GlanceTheme.colors.onPrimaryContainer,
                        contentDescription = context.getString(R.string.add),
                        imageHeight = imageSize.px,
                        modifier = GlanceModifier
                            .clickable(actionStartActivity(addNewIntent))
                            .padding(8.dp)
                            .size(imageSize),
                    )
                }

                LazyColumn(
                    modifier = GlanceModifier
                        //.defaultWeight()
                        .padding(4.dp)
                        .background(GlanceTheme.colors.primaryContainer)
                        .cornerRadius(16.dp)
                ) {

                    items(list) { entry ->

                        Column(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                        ) {
                            ListEntry(
                                obj = entry,
                                textColor = GlanceTheme.colors.onSurface,
                                containerColor = GlanceTheme.colors.surface
                            )
                            Box(
                                modifier = GlanceModifier.fillMaxWidth().height(4.dp)
                            ) { }   // Spacer as .spacedBy is not available in Glance

                        }
                    }
                }
            }
        }
    }
}
