/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.Module
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "JournalsWidgetRec"


class JournalsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = JournalsWidget()

    private val coroutineScope = MainScope()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        observeData(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        observeData(context)
    }

    private fun observeData(context: Context) {
        coroutineScope.launch(Dispatchers.IO) {

            Log.v(TAG, "Loading journals...")
            val entries = ICalDatabase.getInstance(context)
                .iCalDatabaseDao
                .getIcal4ListByModuleSync(Module.JOURNAL)

            GlanceAppWidgetManager(context).getGlanceIds(JournalsWidget::class.java).forEach { glanceId ->

                glanceId.let {
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->
                        pref.toMutablePreferences().apply {
                            this[journalsList] = entries.map { entry -> Json.encodeToString(entry) }.toSet()
                        }
                    }
                    glanceAppWidget.update(context, it)
                }
            }
        }
    }

    companion object {
        val journalsList = stringSetPreferencesKey("journalsList")
    }
}