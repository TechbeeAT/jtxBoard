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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.appwidget.GlanceAppWidgetManager
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.widgets.ListWidget
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID


        setContent {
            JtxBoardTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    WidgetConfigContent(
                        onFinish = {
                            val resultValue = Intent().putExtra(
                                AppWidgetManager.EXTRA_APPWIDGET_ID,
                                appWidgetId
                            )
                            setResult(Activity.RESULT_OK, resultValue)
                            finish()
                        },
                        onCancel = {
                            setResult(Activity.RESULT_CANCELED)
                        }
                    )
                }
            }
        }

    }
}

@Composable
fun WidgetConfigContent(
    onFinish: () -> Unit,
    onCancel: () -> Unit
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(
            content = { Text(stringResource(id = R.string.ok)) },
            onClick = {
                scope.launch {
                    GlanceAppWidgetManager(context).getGlanceIds(ListWidget::class.java)
                        .forEach { glanceId ->

                            glanceId.let {
                                /*
                        updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->
                            pref.toMutablePreferences().apply {
                                this[JournalsWidgetReceiver.journalsList] = entries.map { entry -> Json.encodeToString(entry) }.toSet()
                            }
                        }
                         */
                                ListWidget().update(context, it)
                                //Log.d(TAG, "Widget updated")

                            }
                        }
                    onFinish()
                }
            }
        )

        Button(
            content = { Text(stringResource(id = R.string.cancel)) },
            onClick = { onCancel()
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun WidgetConfigContent_Preview() {
    MaterialTheme {
        WidgetConfigContent(
            onFinish = { },
            onCancel = { }
        )
    }
}