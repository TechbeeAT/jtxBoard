/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.sqlite.db.SimpleSQLiteQuery
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R
import at.techbee.jtx.database.COLUMN_MODULE
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.database.views.VIEW_NAME_ICAL4LIST
import at.techbee.jtx.widgets.elements.JournalEntryCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "JournalsWidget"

class JournalsWidget : GlanceAppWidget() {

    private val backgroundColor: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.surface.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.surface.getColor(LocalContext.current),
        )

    private val foregroundColor: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.onSurface.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.onSurface.getColor(LocalContext.current),
        )

    private val secondaryBackgroundColor: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.primaryContainer.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.primaryContainer.getColor(LocalContext.current),
        )

    private val secondaryForegroundColor: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.onPrimaryContainer.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.onPrimaryContainer.getColor(LocalContext.current),
        )

    private val accentColor: ColorProvider
        @Composable
        get() = androidx.glance.appwidget.unit.ColorProvider(
            day = WidgetTheme.lightColors.primary.getColor(LocalContext.current),
            night = WidgetTheme.darkColors.primary.getColor(LocalContext.current),
        )

    private val journalsList: MutableState<List<ICal4List>> = mutableStateOf(emptyList())

    @Composable
    private fun stringResource(@StringRes id: Int) =
        LocalContext.current.getString(id)

    @Composable
    override fun Content() {
        Column(
            modifier = GlanceModifier
                .appWidgetBackground()
                .fillMaxSize()
                .background(backgroundColor),
        ) {
            val context = LocalContext.current
            val journals by journalsList

            if (journals.isEmpty())
                loadJournals(context)

            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(secondaryBackgroundColor),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.journals_widget_title),
                    style = TextStyle(
                        color = secondaryForegroundColor,
                        fontSize = 20.sp,
                    ),
                    modifier = GlanceModifier
                        .defaultWeight()
                        .padding(8.dp),
                )
                val buttonSize = 32.dp
                Log.v("Widget", "Size: ${buttonSize.px} px")
                TintImage(
                    resource = R.drawable.ic_add_quick,
                    tintColor = secondaryForegroundColor,
                    contentDescription = stringResource(R.string.journals_widget_new),
                    imageHeight = buttonSize.px,
                    modifier = GlanceModifier
                        .clickable(
                            actionStartActivity(
                                Intent(
                                    "addJournal",
                                    Uri.EMPTY,
                                    context,
                                    MainActivity2::class.java
                                )
                            )
                        )
                        .padding(8.dp)
                        .size(buttonSize),
                )
            }
            LazyColumn(
                modifier = GlanceModifier
                    .defaultWeight()
                    .padding(vertical = 4.dp),
            ) {
                items(journals) {
                    JournalEntryCard(it)
                }
            }
        }
    }

    private fun loadJournals(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.v(TAG, "Loading journals...")

            val entries = ICalDatabase.getInstance(context)
                .iCalDatabaseDao
                .getIcal4List(
                    SimpleSQLiteQuery(
                        "SELECT DISTINCT $VIEW_NAME_ICAL4LIST.* FROM $VIEW_NAME_ICAL4LIST WHERE $COLUMN_MODULE = '${Module.JOURNAL.name}'",
                    ).also { Log.v(TAG, "SQL: ${it.sql}") }
                )
                .value ?: return@launch run { Log.w(TAG, "Could not load journals. Null ical") }
            withContext(Dispatchers.Main) {
                Log.v(TAG, "Got ${entries.size} journals")
                journalsList.value = entries
            }
        }
    }
}


class JournalsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = JournalsWidget()
}