/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class ListWidget : GlanceAppWidget() {

    companion object {
        const val MAX_ENTRIES = 50
    }

    @Composable
    override fun Content() {

        val context = LocalContext.current

        Log.d("ListWidget", "appWidgetId in ListWidget: ${GlanceAppWidgetManager(context).getAppWidgetId(LocalGlanceId.current)}")
        Log.d("ListWidget", "glanceId in ListWidget: ${LocalGlanceId.current}")

        val prefs = currentState<Preferences>()
        val listWidgetConfig = prefs[ListWidgetReceiver.filterConfig]?.let { filterConfig ->
            Json.decodeFromString<ListWidgetConfig>(filterConfig)
        }

        val list = prefs[ListWidgetReceiver.list]?.map { Json.decodeFromString<ICal4ListWidget>(it) } ?: emptyList()
        val subtasks = prefs[ListWidgetReceiver.subtasks]?.map { Json.decodeFromString<ICal4ListWidget>(it) } ?: emptyList()
        val subnotes = prefs[ListWidgetReceiver.subnotes]?.map { Json.decodeFromString<ICal4ListWidget>(it) } ?: emptyList()
        val listExceedLimits = prefs[ListWidgetReceiver.listExceedsLimits] ?: false

        val backgorundColor = if((listWidgetConfig?.widgetAlpha ?: 1F) == 1F)
            GlanceTheme.colors.primaryContainer
        else
            ColorProvider(GlanceTheme.colors.primaryContainer.getColor(context).copy(alpha = listWidgetConfig?.widgetAlpha ?: 1F))

        GlanceTheme {
            ListWidgetContent(
                listWidgetConfig ?: return@GlanceTheme,
                list = list,
                subtasks = subtasks,
                subnotes = subnotes,
                listExceedLimits = listExceedLimits,
                modifier = GlanceModifier
                    .appWidgetBackground()
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
                    .background(backgorundColor)
            )
        }
    }
}
