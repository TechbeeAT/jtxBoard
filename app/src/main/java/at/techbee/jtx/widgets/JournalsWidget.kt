/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.content.Intent
import android.graphics.drawable.Icon
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.template.ButtonType.Companion.Icon
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
    override fun Content() {
        Column(
            modifier = GlanceModifier
                .appWidgetBackground()
                .fillMaxSize()
                .background(surface),
        ) {
            val context = LocalContext.current

            val prefs = currentState<Preferences>()
            val journalsList = prefs[JournalsWidgetReceiver.journalsList]?.map { Json.decodeFromString<ICal4List>(it) }

            val addJournalIntent = Intent(context, MainActivity2::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = MainActivity2.INTENT_ACTION_ADD_JOURNAL
            }

            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(primaryContainer)
                    .padding(8.dp),
            ) {

                LazyColumn(
                    modifier = GlanceModifier
                        //.defaultWeight()
                        .padding(8.dp).background(primaryContainer)
                ) {

                    items(journalsList?.toList() ?: emptyList()) { entry ->
                        JournalEntry(
                            obj = entry,
                            textColor = onPrimaryContainer
                        )
                    }
                }

                TintImage(
                    resource = R.drawable.ic_add_quick,
                    contentDescription = context.getString(R.string.shortcut_addJournal_longLabel),
                    tintColor = primary,
                    modifier = GlanceModifier.clickable(actionStartActivity(addJournalIntent))
                )

                /*
                Button(
                    text = "+",
                    onClick = actionStartActivity(addJournalIntent),
                    style = TextStyle(
                        color = onPrimary,
                        fontSize = 24.sp
                    ),
                    modifier = GlanceModifier.size(48.dp)
                )

                 */
            }
        }
    }
}