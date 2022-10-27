/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.content.Intent
import androidx.annotation.StringRes
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
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.widgets.elements.JournalEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class JournalsWidget : GlanceAppWidget() {

    val surface: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.surface.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.surface.getColor(LocalContext.current),
        )

    val onSurface: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.onSurface.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.onSurface.getColor(LocalContext.current),
        )

    val primary: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.primary.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.primary.getColor(LocalContext.current),
        )

    val onPrimary: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.onPrimary.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.onPrimary.getColor(LocalContext.current),
        )

    val primaryContainer: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.primaryContainer.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.primaryContainer.getColor(LocalContext.current),
        )

    val onPrimaryContainer: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.onPrimaryContainer.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.onPrimaryContainer.getColor(LocalContext.current),
        )


    @Composable
    private fun stringResource(@StringRes id: Int) =
        LocalContext.current.getString(id)

    @Composable
    override fun Content() {
        Column(
            modifier = GlanceModifier
                .appWidgetBackground()
                .fillMaxSize()
                .background(surface),
        ) {
            val context = LocalContext.current
            //val journals by journalsList

            val prefs = currentState<Preferences>()
            val journalsList = prefs[JournalsWidgetReceiver.journalsList]?.map { Json.decodeFromString<ICal4List>(it) }

            val addJournalIntent = Intent(context, MainActivity2::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = MainActivity2.INTENT_ACTION_ADD_JOURNAL
            }

            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(primary)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.journals_widget_title),
                    style = TextStyle(
                        color = onPrimary,
                        fontSize = 20.sp,
                    ),
                    modifier = GlanceModifier
                        .defaultWeight(),
                )
                Text(
                    text = "+",
                    style = TextStyle(
                        color = onPrimary,
                        fontSize = 24.sp,
                    ),
                    modifier = GlanceModifier
                        .clickable(actionStartActivity(addJournalIntent))
                        .padding(horizontal = 8.dp)
                )
            }
            LazyColumn(
                modifier = GlanceModifier
                    //.defaultWeight()
                    .padding(8.dp).background(primaryContainer)
            ) {

                items(journalsList?.toList()?: emptyList()) { entry ->
                    JournalEntry(
                        obj = entry,
                        textColor = onPrimaryContainer
                    )
                }
            }
        }
    }
}