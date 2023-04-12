/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.util.Log
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
        }?: return

        val list = prefs[ListWidgetReceiver.list]?.map { Json.decodeFromString<ICal4ListWidget>(it) } ?: emptyList()
        val subtasks = prefs[ListWidgetReceiver.subtasks]?.map { Json.decodeFromString<ICal4ListWidget>(it) } ?: emptyList()
        val subnotes = prefs[ListWidgetReceiver.subnotes]?.map { Json.decodeFromString<ICal4ListWidget>(it) } ?: emptyList()
        val listExceedLimits = prefs[ListWidgetReceiver.listExceedsLimits] ?: false

        val backgorundColor = if(listWidgetConfig.widgetAlpha == 1F && listWidgetConfig.widgetColor == null)
            GlanceTheme.colors.primaryContainer
        else if((listWidgetConfig.widgetAlpha) < 1F && listWidgetConfig.widgetColor == null)
            ColorProvider(GlanceTheme.colors.primaryContainer.getColor(context).copy(alpha = listWidgetConfig.widgetAlpha))
        else
            ColorProvider(Color(listWidgetConfig.widgetColor!!).copy(alpha = listWidgetConfig.widgetAlpha))

        val textColor = if(listWidgetConfig.widgetColor == null)
            GlanceTheme.colors.onPrimaryContainer
        else
            ColorProvider(contentColorFor(Color(listWidgetConfig.widgetColor!!).copy(alpha = listWidgetConfig.widgetAlpha)))

        val entryColor = if(listWidgetConfig.widgetAlphaEntries == 1F && listWidgetConfig.widgetColorEntries == null)
            GlanceTheme.colors.surface
        else if(listWidgetConfig.widgetAlphaEntries < 1F && listWidgetConfig.widgetColorEntries == null)
            ColorProvider(GlanceTheme.colors.surface.getColor(context).copy(alpha = listWidgetConfig.widgetAlphaEntries))
        else
            ColorProvider(Color(listWidgetConfig.widgetColorEntries!!).copy(alpha = listWidgetConfig.widgetAlphaEntries))

        val entryTextColor = if(listWidgetConfig.widgetColorEntries == null)
            GlanceTheme.colors.onSurface
        else
            ColorProvider(contentColorFor(Color(listWidgetConfig.widgetColorEntries!!).copy(alpha = listWidgetConfig.widgetAlphaEntries)))

        val entryOverdueTextColor = GlanceTheme.colors.error

        GlanceTheme {
            ListWidgetContent(
                listWidgetConfig,
                list = list,
                subtasks = subtasks,
                subnotes = subnotes,
                listExceedLimits = listExceedLimits,
                textColor = textColor,
                entryColor = entryColor,
                entryTextColor = entryTextColor,
                entryOverdueTextColor = entryOverdueTextColor,
                modifier = GlanceModifier
                    .appWidgetBackground()
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
                    .background(backgorundColor)
            )
        }
    }
}
