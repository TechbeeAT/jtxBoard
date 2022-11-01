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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.WidgetConfigActivity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.widgets.elements.JournalEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class ListWidget : GlanceAppWidget() {

    private val surface: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.surface.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.surface.getColor(LocalContext.current),
        )

    private val onSurface: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.onSurface.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.onSurface.getColor(LocalContext.current),
        )

    private val primary: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.primary.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.primary.getColor(LocalContext.current),
        )

    private val onPrimary: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.onPrimary.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.onPrimary.getColor(LocalContext.current),
        )

    private val primaryContainer: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.primaryContainer.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.primaryContainer.getColor(LocalContext.current),
        )

    private val onPrimaryContainer: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.onPrimaryContainer.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.onPrimaryContainer.getColor(LocalContext.current),
        )


    @Composable
    override fun Content() {

        Column(
            modifier = GlanceModifier
                .appWidgetBackground()
                .fillMaxSize()
                .padding(4.dp)
                .background(primary),
        ) {
            val context = LocalContext.current

            val prefs = currentState<Preferences>()
            val journalsList = prefs[JournalsWidgetReceiver.journalsList]?.map {
                Json.decodeFromString<ICal4List>(it)
            }

            val addJournalIntent = Intent(context, MainActivity2::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = MainActivity2.INTENT_ACTION_ADD_JOURNAL
            }

            val configIntent = Intent(context, WidgetConfigActivity::class.java)
            // TODO
            //configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);    //set widget id


            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(primary),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = context.getString(R.string.widget_list_journals_title),
                    style = TextStyle(
                        color = onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier
                        .defaultWeight()
                        .padding(8.dp),
                )
                val buttonSize = 36.dp
                //Log.v("Widget", "Size: ${buttonSize.px} px")

                TintImage(
                    resource = R.drawable.ic_settings,
                    tintColor = onPrimary,
                    contentDescription = context.getString(R.string.widget_list_settings),
                    imageHeight = buttonSize.px,
                    modifier = GlanceModifier
                        .clickable(actionStartActivity(configIntent))
                        .padding(8.dp)
                        .size(buttonSize),
                )

                TintImage(
                    resource = R.drawable.ic_edit,
                    tintColor = onPrimary,
                    contentDescription = context.getString(R.string.widget_list_journals_new),
                    imageHeight = buttonSize.px,
                    modifier = GlanceModifier
                        .clickable(actionStartActivity(addJournalIntent))
                        .padding(8.dp)
                        .size(buttonSize),
                )
            }


            LazyColumn(
                modifier = GlanceModifier
                    //.defaultWeight()
                    .padding(4.dp),
                    //.background(primaryContainer)
                    //.cornerRadius(16.dp)
            ) {

                items(journalsList?.toList() ?: emptyList()) { entry ->
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        JournalEntry(
                            obj = entry,
                            textColor = onPrimaryContainer,
                            containerColor = primaryContainer
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