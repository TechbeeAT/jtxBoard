package at.techbee.jtx

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class AlarmFullscreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JtxBoardTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    FullscreenAlarmScreen(
                        iCalObject = ICalObject.createNote("TODO!!!!"),
                        alarm = Alarm.createDisplayAlarm(), //TODO("not yet implemented"),
                        isReadOnly = false,
                        onDismiss = {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullscreenAlarmScreen(
    iCalObject: ICalObject,
    alarm: Alarm,
    isReadOnly: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Alarm,
            contentDescription = null,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = iCalObject.summary?:"",
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = iCalObject.description?:"",
            textAlign = TextAlign.Center,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis
        )

        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if(iCalObject.dtstart != null) {
                Text(
                    text = stringResource(id = R.string.started),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = DateTimeUtils.convertLongToFullDateTimeString(iCalObject.dtstart, iCalObject.dtstartTimezone),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if(iCalObject.due != null) {
                Text(
                    text = stringResource(id = R.string.due),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = DateTimeUtils.convertLongToFullDateTimeString(iCalObject.due, iCalObject.dueTimezone),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!isReadOnly && alarm.alarmId != 0L)
                ElevatedAssistChip(
                    onClick = { scope.launch(Dispatchers.IO) { NotificationPublisher.addPostponedAlarm(alarm.alarmId, (1).hours.inWholeMilliseconds, context) }  },
                    label = { Text(stringResource(id = R.string.notification_add_1h)) }
                )
            if (!isReadOnly && alarm.alarmId != 0L)
                ElevatedAssistChip(
                    onClick = { scope.launch(Dispatchers.IO) { NotificationPublisher.addPostponedAlarm(alarm.alarmId, (1).hours.inWholeMilliseconds, context) } },
                    label = { Text(stringResource(id = R.string.notification_add_1d)) })
            if (!isReadOnly)
                ElevatedAssistChip(
                    onClick = {
                        val settingsStateHolder = SettingsStateHolder(context)
                        scope.launch {
                            NotificationPublisher.setToDone(alarm.alarmId, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value, settingsStateHolder.settingLinkProgressToSubtasks.value, context) }
                              },
                    label = { Text(stringResource(id = R.string.notification_done)) }
                )
        }

        ElevatedAssistChip(
            onClick = { onDismiss() },
            label = { Text(stringResource(R.string.close)) })
    }
}

@Preview(showBackground = true)
@Composable
fun FullscreenAlarmScreen_Preview() {
    MaterialTheme {
        FullscreenAlarmScreen(
            ICalObject.createTodo().apply {
                summary = "My summary - this is now very long to make several rows"
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                due = System.currentTimeMillis()
                dueTimezone = null
                dtstart = System.currentTimeMillis() - (1).days.inWholeMicroseconds
                dtstartTimezone = null
            },
            alarm = Alarm.createDisplayAlarm(),
            isReadOnly = false,
            onDismiss = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FullscreenAlarmScreen_Preview_readonly() {
    MaterialTheme {
        FullscreenAlarmScreen(
            ICalObject.createTodo().apply {
                summary = "My summary - this is now very long to make several rows"
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                due = System.currentTimeMillis()
                dueTimezone = null
                dtstart = System.currentTimeMillis() - (1).days.inWholeMicroseconds
                dtstartTimezone = null
            },
            alarm = Alarm.createDisplayAlarm(),
            isReadOnly = true,
            onDismiss = { }
        )
    }
}