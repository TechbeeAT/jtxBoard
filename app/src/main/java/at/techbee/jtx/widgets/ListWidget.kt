/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.text.*
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.widgets.elements.ListEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class ListWidget : GlanceAppWidget() {


    @Composable
    override fun Content() {

        val context = LocalContext.current

        Log.d("ListWidget", "appWidgetId in ListWidget: ${GlanceAppWidgetManager(context).getAppWidgetId(LocalGlanceId.current)}")
        Log.d("ListWidget", "glanceId in ListWidget: ${LocalGlanceId.current}")

        val prefs = currentState<Preferences>()
        val listWidgetConfig = prefs[ListWidgetReceiver.filterConfig]?.let { filterConfig ->
            Json.decodeFromString<ListWidgetConfig>(filterConfig)
        }
        val list = prefs[ListWidgetReceiver.list]?.map {
            Json.decodeFromString<ICal4List>(it)
        } ?: emptyList()

        val groupedList = list.groupBy { it.vtodoUidOfParent }

        val flattenedList: MutableList<ICal4List> = mutableListOf()
        groupedList[null]?.forEach { parents ->
            flattenedList.add(parents)
            groupedList[parents.uid]?.let { flattenedList.addAll(it) }
        }

        val mainIntent = Intent(context, MainActivity2::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val imageSize = 36.dp


        GlanceTheme {

            Column(
                modifier = GlanceModifier
                    .appWidgetBackground()
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
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
                            .clickable(actionStartActivity(mainIntent))
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
                            .clickable(actionRunCallback<ListWidgetOpenConfigActionCallback>())
                            .padding(8.dp)
                            .size(imageSize),
                    )

                    TintImage(
                        resource = R.drawable.ic_add,
                        tintColor = GlanceTheme.colors.onPrimaryContainer,
                        contentDescription = context.getString(R.string.add),
                        imageHeight = imageSize.px,
                        modifier = GlanceModifier
                            .clickable(actionStartActivity(addNewIntent))
                            .padding(8.dp)
                            .size(imageSize),
                    )
                }

                if(flattenedList.isNotEmpty()) {
                    LazyColumn(
                        modifier = GlanceModifier
                            //.defaultWeight()
                            .padding(bottom = 2.dp, start = 2.dp, end = 2.dp, top = 0.dp)
                            .background(GlanceTheme.colors.primaryContainer)
                            .cornerRadius(8.dp)
                    ) {

                        items(
                            if(listWidgetConfig?.flatView == false) flattenedList else list) { entry ->

                            Column(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp, start = if(entry.isChildOfTodo && listWidgetConfig?.flatView == false) 16.dp else 0.dp)
                            ) {
                                ListEntry(
                                    obj = entry,
                                    textColor = GlanceTheme.colors.onSurface,
                                    containerColor = GlanceTheme.colors.surface,
                                    checkboxEnd = listWidgetConfig?.checkboxPositionEnd ?: false
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = GlanceModifier.padding(8.dp).fillMaxWidth().fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = context.getString(R.string.widget_list_no_entries_found),
                            style = TextStyle(
                                color = GlanceTheme.colors.onPrimaryContainer,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }
    }
}
