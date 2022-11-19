/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.widgets.ListWidgetReceiver
import at.techbee.jtx.widgets.ListWidgetConfig
import at.techbee.jtx.widgets.ListWidgetConfigContent
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val TAG = "WidgetConfigAct"

class ListWidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        Log.d(TAG, "appWidgetId $appWidgetId")
        val glanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)
        //Log.d(TAG, "glanceId $glanceId")


        setContent {
            JtxBoardTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current

                    var currentFilterConfig by remember { mutableStateOf<ListWidgetConfig?>(null)}

                    LaunchedEffect(true) {
                        currentFilterConfig = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)[ListWidgetReceiver.filterConfig]?.let { filterConfig -> Json.decodeFromString<ListWidgetConfig>(filterConfig) } ?: ListWidgetConfig()
                    }

                    currentFilterConfig?.let {
                        ListWidgetConfigContent(
                            initialConfig = it,
                            onFinish = { listWidgetConfig ->

                                scope.launch {
                                    glanceId.let { glanceId ->
                                        updateAppWidgetState(
                                            context,
                                            PreferencesGlanceStateDefinition,
                                            glanceId
                                        ) { pref ->
                                            pref.toMutablePreferences().apply {
                                                this[ListWidgetReceiver.filterConfig] =
                                                    Json.encodeToString(listWidgetConfig)
                                            }
                                        }
                                        ListWidgetReceiver.setOneTimeWork(context)
                                        Log.d(TAG, "Widget update requested")
                                    }

                                    val resultValue = Intent().putExtra(
                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                        appWidgetId
                                    )
                                    setResult(Activity.RESULT_OK, resultValue)
                                    finish()
                                }
                            },
                            onCancel = {
                                setResult(Activity.RESULT_CANCELED)
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}
