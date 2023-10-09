package at.techbee.jtx

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class AlarmFullscreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alarmId = intent.getLongExtra(NotificationPublisher.ALARM_ID,  0L)
        val icalObjectId = intent.getLongExtra(NotificationPublisher.ICALOBJECT_ID, 0L)
        val database = ICalDatabase.getInstance(this).iCalDatabaseDao()

        if(alarmId == 0L && icalObjectId == 0L) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        setContent {
            JtxBoardTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background.copy(alpha = 0.4f)) {

                    val alarm = database.getAlarm(alarmId).observeAsState(initial = null)
                    val iCalObject = database.getICalObject(icalObjectId).observeAsState(initial = null)

                    if(iCalObject.value != null) {
                        FullscreenAlarmScreen(
                            iCalObject = iCalObject.value!!,
                            alarm = alarm.value,
                            isReadOnly = false,
                            onDismiss = {enforceCancelNotification ->
                                if(enforceCancelNotification || !SettingsStateHolder(this).settingStickyAlarms.value)
                                    NotificationManagerCompat.from(this).cancel(icalObjectId.toInt())
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullscreenAlarmScreen(
    iCalObject: ICalObject,
    alarm: Alarm?,
    isReadOnly: Boolean,
    onDismiss: (enforceCancelNotification: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box {

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
                modifier = Modifier
                    .size(72.dp)
                    .alpha(0.33f)
            )
            Text(
                text = iCalObject.summary?:"",
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.displayMedium
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

            if(!isReadOnly) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (alarm != null && alarm.alarmId != 0L) {
                        TextButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    NotificationPublisher.addPostponedAlarm(alarm.alarmId, (1).hours.inWholeMilliseconds, context)
                                    onDismiss(true)
                                }
                            }
                        )  {
                            Text(stringResource(id = R.string.notification_add_1h))
                        }
                        TextButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    NotificationPublisher.addPostponedAlarm(alarm.alarmId, (1).days.inWholeMilliseconds, context)
                                    onDismiss(true)
                                }
                            }
                        ) {
                            Text(stringResource(id = R.string.notification_add_1d))
                        }
                    }

                    TextButton(
                        onClick = {
                            val settingsStateHolder = SettingsStateHolder(context)
                            scope.launch(Dispatchers.IO) {
                                NotificationPublisher.setToDone(iCalObject.id, settingsStateHolder.settingKeepStatusProgressCompletedInSync.value, settingsStateHolder.settingLinkProgressToSubtasks.value, context)
                                onDismiss(true)
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.notification_done))
                    }
                }
            }

            Button(onClick = {
                val intent = Intent(context, MainActivity2::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    this.action = MainActivity2.INTENT_ACTION_OPEN_ICALOBJECT
                    this.putExtra(MainActivity2.INTENT_EXTRA_ITEM2SHOW, iCalObject.id)
                }
                context.startActivity(intent)
                onDismiss(false)
            }) {
                Text(stringResource(id = R.string.open))
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = { onDismiss(false) }) {
                Icon(Icons.Outlined.Close, stringResource(id = R.string.dismiss))
            }
        }
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
            alarm = Alarm.createDisplayAlarm().apply { alarmId = 1L },
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
            alarm = Alarm.createDisplayAlarm().apply { alarmId = 1L },
            isReadOnly = true,
            onDismiss = { }
        )
    }
}