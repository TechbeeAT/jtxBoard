/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.provider.BaseColumns
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import at.techbee.jtx.AlarmFullscreenActivity
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.MainActivity2.Companion.INTENT_ACTION_OPEN_ICALOBJECT
import at.techbee.jtx.MainActivity2.Companion.INTENT_EXTRA_ITEM2SHOW
import at.techbee.jtx.NotificationPublisher
import at.techbee.jtx.R
import at.techbee.jtx.database.COLUMN_ID
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.ui.settings.SwitchSetting
import kotlinx.parcelize.Parcelize
import kotlin.time.Duration

/** The name of the the table for Alarms that are linked to an ICalObject.
 * [https://tools.ietf.org/html/rfc5545#section-3.8.1.10]*/
const val TABLE_NAME_ALARM = "alarm"

/** The name of the ID column for alarms.
 * This is the unique identifier of an Alarm
 * Type: [Long]*/
const val COLUMN_ALARM_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects.
 * Type: [Long] */
const val COLUMN_ALARM_ICALOBJECT_ID = "icalObjectId"

/**
 * Each "VALARM" calendar component has a particular type
 * of action with which it is associated.  This property specifies
 * the type of action.  Applications MUST ignore alarms with x-name
 * and iana-token values they don't recognize.
 * Currently only "DISPLAY" is supported, all other values can be stored but are ignored
 * Type: [String]
 */
const val COLUMN_ALARM_ACTION = "action"

/**
 * This property provides a more complete description of the
 * calendar component than that provided by the "SUMMARY" property.
 * Type: [String]
 */
const val COLUMN_ALARM_DESCRIPTION = "description"

/**
 * This property defines a short summary or subject for the
 * calendar component.
 * Type: [String]
 */
const val COLUMN_ALARM_SUMMARY = "summary"

/**
 * This property contains a CSV-list of attendees as Uris
 * e.g. "mailto:contact@techbee.at,mailto:jtx@techbee.at"
 */
const val COLUMN_ALARM_ATTENDEE = "attendee"

/**
 * The alarm can be defined such that it triggers repeatedly.  A
 * definition of an alarm with a repeating trigger MUST include both
 * the "DURATION" and "REPEAT" properties.  The "DURATION" property
 * specifies the delay period, after which the alarm will repeat.
 * Type: [String]
 */
const val COLUMN_ALARM_DURATION = "duration"

/**
 * The "REPEAT" property specifies the number of additional
 * repetitions that the alarm will be triggered.  This repetition
 * count is in addition to the initial triggering of the alarm.  Both
 * of these properties MUST be present in order to specify a
 * repeating alarm.  If one of these two properties is absent, then
 * the alarm will not repeat beyond the initial trigger.
 * Type: [String]
 */
const val COLUMN_ALARM_REPEAT = "repeat"

/**
 * Contains the uri of an attachment
 * Type: [String]
 */
const val COLUMN_ALARM_ATTACH = "attach"

/**
 * Purpose:  To specify other properties for the alarm.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.4.3]
 * Type: [String]
 */
const val COLUMN_ALARM_OTHER = "other"

/**
 * Stores a timestamp with the absolute time when the alarm should be triggered.
 * This value is stored as UNIX timestamp (milliseconds).
 * Either a Alarm Trigger Time OR a Alarm Relative Duration must be provided, but not both!
 * Type: [Long]
 */
const val COLUMN_ALARM_TRIGGER_TIME = "triggerTime"

/**
 * Purpose:  This column/property specifies the timezone of the absolute trigger time.
 * The corresponding datetime is stored in [COLUMN_ALARM_TRIGGER_TIME].
 * The value of a timezone can be:
 * 1. the id of a Java timezone to represent the given timezone.
 * 2. null to represent floating time.
 * If an invalid value is passed, the Timezone is ignored and interpreted as UTC.
 * See [https://tools.ietf.org/html/rfc5545#section-3.8.2.4]
 * Type: [String]
 */
const val COLUMN_ALARM_TRIGGER_TIMEZONE = "triggerTimezone"

/**
 * Purpose:  This property defines the field to which the duration is relatiive to.
 * The possible values of a status are defined in the enum [AlarmRelativeTo].
 * Use e.g. AlarmRelativeTo.START.name to put a correct String value in this field.
 * AlarmRelativeTo.START would make the duration relative to DTSTART.
 * AlarmRelativeTo.END would make the duration relative to DUE (only VTODO is supported!).
 * If no valid AlarmRelativeTo is provided, the default value is AlarmRelativeTo.START.
 * Type: [String]
 */
const val COLUMN_ALARM_TRIGGER_RELATIVE_TO = "triggerRelativeTo"

/**
 * Purpose: Specifying a relative time for the
 * trigger of the alarm.  The default duration is relative to the
 * start of an event or to-do with which the alarm is associated.
 * The duration can be explicitly set to trigger from either the end
 * or the start of the associated event or to-do with the "COLUMN_ALARM_TRIGGER_RELATIVE_TO"
 * parameter.  A value of START will set the alarm to trigger off the
 * start of the associated event or to-do.  A value of END will set
 * the alarm to trigger off the end of the associated event or to-do.
 * Either a positive or negative duration may be specified for the
 * "TRIGGER" property.  An alarm with a positive duration is
 * triggered after the associated start or end of the event or to-do.
 * An alarm with a negative duration is triggered before the
 * associated start or end of the event or to-do.
 * Type: [String]
 */
const val COLUMN_ALARM_TRIGGER_RELATIVE_DURATION = "triggerRelativeDuration"


@Parcelize
@Entity(
    tableName = TABLE_NAME_ALARM,
    foreignKeys = [ForeignKey(
        entity = ICalObject::class,
        parentColumns = arrayOf(COLUMN_ID),
        childColumns = arrayOf(COLUMN_ALARM_ICALOBJECT_ID),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Alarm(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ALARM_ID)
    var alarmId: Long = 0L,

    @ColumnInfo(index = true, name = COLUMN_ALARM_ICALOBJECT_ID) var icalObjectId: Long = 0L,
    @ColumnInfo(name = COLUMN_ALARM_ACTION) var action: String? = null,
    @ColumnInfo(name = COLUMN_ALARM_DESCRIPTION) var description: String? = "",
    @ColumnInfo(name = COLUMN_ALARM_SUMMARY) var summary: String? = null,
    @ColumnInfo(name = COLUMN_ALARM_ATTENDEE) var attendee: String? = null,
    @ColumnInfo(name = COLUMN_ALARM_DURATION) var duration: String? = null,
    @ColumnInfo(name = COLUMN_ALARM_REPEAT) var repeat: String? = null,
    @ColumnInfo(name = COLUMN_ALARM_ATTACH) var attach: String? = null,
    @ColumnInfo(name = COLUMN_ALARM_OTHER) var other: String? = null,
    @ColumnInfo(name = COLUMN_ALARM_TRIGGER_TIME) var triggerTime: Long? = null,
    @ColumnInfo(name = COLUMN_ALARM_TRIGGER_TIMEZONE) var triggerTimezone: String? = null,
    @ColumnInfo(name = COLUMN_ALARM_TRIGGER_RELATIVE_TO) var triggerRelativeTo: String? = null,
    @ColumnInfo(name = COLUMN_ALARM_TRIGGER_RELATIVE_DURATION) var triggerRelativeDuration: String? = null

) : Parcelable {
    companion object Factory {

        /**
         * Create a new [Alarm] Property from the specified [ContentValues].
         *
         * @param values A [Alarm] that at least contain [COLUMN_ALARM_ICALOBJECT_ID].
         * @return A newly created [Alarm] instance.
         */
        fun fromContentValues(values: ContentValues?): Alarm? {

            if (values == null)
                return null

            if (values.getAsLong(COLUMN_ALARM_ICALOBJECT_ID) == null)
                return null

            // time or duration must be present, otherwise the entry is rejected.
            if (values.getAsLong(COLUMN_ALARM_TRIGGER_TIME) == null && values.getAsString(COLUMN_ALARM_TRIGGER_RELATIVE_DURATION) == null)
                return null


            return Alarm().applyContentValues(values)
        }

        /**
         * @return [Alarm] with action set to AlarmAction.DISPLAY
         */
        fun createDisplayAlarm() = Alarm().apply {
            action = AlarmAction.DISPLAY.name
        }

        /**
         * @param [dur] the TriggerDuration of the alarm
         * @param [alarmRelativeTo] a value of the Enum [AlarmRelativeTo] or null
         * @return [Alarm] with action set to AlarmAction.DISPLAY, triggerRelativeDuration set to given duration and triggerRelativeTo set to the given alarmRelativeTo or null
         */
        fun createDisplayAlarm(dur: Duration, alarmRelativeTo: AlarmRelativeTo?, referenceDate: Long, referenceTimezone: String?) = Alarm().apply {
            action = AlarmAction.DISPLAY.name
            this.updateDuration(dur, alarmRelativeTo, referenceDate, referenceTimezone)
        }

        /**
         * @param [time] the timestamp when the alarm should be triggered
         * @param [timezone] the timezone for the trigger or null
         * @return [Alarm] with action set to AlarmAction.DISPLAY, triggerTime set to the given time and triggerTimezone set to the timezone (if passed)
         */
        fun createDisplayAlarm(time: Long, timezone: String?) = Alarm().apply {
            action = AlarmAction.DISPLAY.name
            triggerTime = time
            triggerTimezone = timezone
        }

        fun createNotification(
            iCalObjectId: Long,
            alarmId: Long,
            notificationSummary: String?,
            notificationDescription: String?,
            isReadOnly: Boolean,
            notificationChannel: String,
            isSticky: Boolean,
            context: Context
        ): Notification {

            val intent = Intent(context, MainActivity2::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                this.action = INTENT_ACTION_OPEN_ICALOBJECT
                this.putExtra(INTENT_EXTRA_ITEM2SHOW, iCalObjectId)
            }
            val contentIntent: PendingIntent =
                PendingIntent.getActivity(context, iCalObjectId.toInt(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            /* function to create intents for snooze and done */
            fun createActionIntent(action: String): PendingIntent {
                val actionIntent = Intent(context, NotificationPublisher::class.java).apply {
                    this.action = action
                    putExtra(NotificationPublisher.ALARM_ID, alarmId)
                    putExtra(NotificationPublisher.ICALOBJECT_ID, iCalObjectId)
                }
                return PendingIntent.getBroadcast(
                    context,
                    alarmId.toInt(),
                    actionIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            val fullScreenIntent = Intent(context, AlarmFullscreenActivity::class.java)
            val fullScreenPendingIntent = PendingIntent.getActivity(context, iCalObjectId.toInt(), fullScreenIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            // this is the notification itself that will be put as an Extra into the notificationIntent
            val notification = NotificationCompat.Builder(context, notificationChannel).apply {
                setSmallIcon(R.drawable.ic_notification)
                notificationSummary?.let { setContentTitle(it) }
                notificationDescription?.let { setContentText(it) }
                setContentIntent(contentIntent)
                setFullScreenIntent(fullScreenPendingIntent, true)
                priority = NotificationCompat.PRIORITY_MAX
                setCategory(NotificationCompat.CATEGORY_ALARM)     //  CATEGORY_REMINDER might also be an alternative
                if (isSticky) {
                    setAutoCancel(false)
                    setOngoing(true)
                } else {
                    setAutoCancel(true)
                }
                //.setStyle(NotificationCompat.BigTextStyle().bigText(text))
                if (!isReadOnly && alarmId != 0L) {    // no alarm for readonly entries and implicit alarms that come only from the due date
                    addAction(
                        R.drawable.ic_snooze,
                        context.getString(R.string.notification_add_1h),
                        createActionIntent(NotificationPublisher.ACTION_SNOOZE_1H)
                    )
                    addAction(
                        R.drawable.ic_snooze,
                        context.getString(R.string.notification_add_1d),
                        createActionIntent(NotificationPublisher.ACTION_SNOOZE_1D)
                    )
                    addAction(
                        R.drawable.ic_todo,
                        context.getString(R.string.notification_done),
                        createActionIntent(NotificationPublisher.ACTION_DONE)
                    )
                }
            }.build()

            return notification
        }
    }

    fun applyContentValues(values: ContentValues): Alarm {
        values.getAsLong(COLUMN_ALARM_ICALOBJECT_ID)?.let { icalObjectId -> this.icalObjectId = icalObjectId }
        values.getAsString(COLUMN_ALARM_ACTION)?.let { action -> this.action = action }
        values.getAsString(COLUMN_ALARM_DESCRIPTION)?.let { desc -> this.description = desc }
        values.getAsString(COLUMN_ALARM_SUMMARY)?.let { summary -> this.summary = summary }
        values.getAsString(COLUMN_ALARM_ATTENDEE)?.let { attendee -> this.attendee = attendee }
        values.getAsString(COLUMN_ALARM_DURATION)?.let { duration -> this.duration = duration }
        values.getAsString(COLUMN_ALARM_REPEAT)?.let { repeat -> this.repeat = repeat }
        values.getAsString(COLUMN_ALARM_ATTACH)?.let { attach -> this.attach = attach }
        values.getAsString(COLUMN_ALARM_OTHER)?.let { other -> this.other = other }
        values.getAsLong(COLUMN_ALARM_TRIGGER_TIME)?.let { triggerTime -> this.triggerTime = triggerTime }
        values.getAsString(COLUMN_ALARM_TRIGGER_TIMEZONE)?.let { triggerTimezone -> this.triggerTimezone = triggerTimezone }
        values.getAsString(COLUMN_ALARM_TRIGGER_RELATIVE_TO)?.let { triggerRelativeTo -> this.triggerRelativeTo = triggerRelativeTo }
        values.getAsString(COLUMN_ALARM_TRIGGER_RELATIVE_DURATION)
            ?.let { triggerRelativeDuration -> this.triggerRelativeDuration = triggerRelativeDuration }
        return this
    }

    /**
     * @return The parsed triggerRelativeDuration of this alarm as Duration or null if the value cannot be parsed
     */
    fun getTriggerAsDuration() = try {
        this.triggerRelativeDuration?.let { Duration.parse(it) }
    } catch (e: IllegalArgumentException) {
        Log.w(
            "triggerRelativeDuration",
            "Failed parsing duration: ${this.triggerRelativeDuration}\n$e"
        )
        null
    }

    /**
     * @return the duration as a human readible string, e.g. "7 days before start" or null (if the triggerDuration could not be parsed)
     */
    fun getTriggerDurationAsString(context: Context): String? {

        val dur = getTriggerAsDuration() ?: return null

        // The final structure should be "xx minutes/hours/days before/after-start/due
        // "%1$d %2$s %3$s", e.g. 7 days before start
        val param1Value = when {
            dur.absoluteValue.inWholeMinutes % (24 * 60) == 0L -> dur.absoluteValue.inWholeDays      // if minutes modulo (24h * 60m) has no rest, we have full days and show days
            dur.absoluteValue.inWholeMinutes % (60) == 0L -> dur.absoluteValue.inWholeHours      // if minutes modulo (60m) has no rest, we have full hours and show hours
            dur.absoluteValue.inWholeMinutes > 0L -> dur.absoluteValue.inWholeMinutes      // if minutes modulo (24h * 60m) has no rest, we have full days and show days
            else -> null
        }
        val param2Unit = when {
            dur.absoluteValue.inWholeMinutes % (24 * 60) == 0L -> context.getString(R.string.alarms_days)
            dur.absoluteValue.inWholeMinutes % (60) == 0L -> context.getString(R.string.alarms_hours)
            dur.absoluteValue.inWholeMinutes > 0L -> context.getString(R.string.alarms_minutes)
            else -> null
        }
        val param3BeforeAfterStartDue = when {
            dur.isNegative() && this.triggerRelativeTo == AlarmRelativeTo.END.name -> context.getString(R.string.alarms_before_due)
            !dur.isNegative() && this.triggerRelativeTo == AlarmRelativeTo.END.name -> context.getString(R.string.alarms_after_due)
            dur.isNegative() -> context.getString(R.string.alarms_before_start)
            !dur.isNegative() -> context.getString(R.string.alarms_after_start)
            else -> null
        }

        return when {
            dur == Duration.ZERO && triggerRelativeTo == AlarmRelativeTo.END.name -> context.getString(R.string.alarms_ondue)
            dur == Duration.ZERO -> context.getString(R.string.alarms_onstart)
            param1Value != null -> context.getString(R.string.alarms_duration_full_string, param1Value, param2Unit, param3BeforeAfterStartDue)
            else -> this.triggerRelativeDuration
        }
    }

    fun scheduleNotification(
        context: Context,
        requestCode: Int,
        isReadOnly: Boolean,
        notificationSummary: String?,
        notificationDescription: String?
    ) {

        if ((this.triggerTime ?: 0) < System.currentTimeMillis())
            return

        if (isReadOnly && SettingsStateHolder(context).settingDisableAlarmsReadonly.value)   // don't schedule alarm for read only if option was deactivated!
            return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.i(
                "scheduleNotification",
                "Due to necessity of PendingIntent.FLAG_IMMUTABLE, the notification functionality can only be used from Build Versions > M (Api-Level 23)"
            )
            return
        }

        val notification = createNotification(
            icalObjectId,
            alarmId,
            notificationSummary,
            notificationDescription,
            isReadOnly,
            MainActivity2.NOTIFICATION_CHANNEL_ALARMS,
            PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SwitchSetting.SETTING_STICKY_ALARMS.key, SwitchSetting.SETTING_STICKY_ALARMS.default),
            context
        )

        val notificationIntent = Intent(context, NotificationPublisher::class.java).apply {
            putExtra(NotificationPublisher.NOTIFICATION, notification)
            putExtra(NotificationPublisher.ALARM_ID, alarmId)
            putExtra(NotificationPublisher.ICALOBJECT_ID, icalObjectId)
        }

        // the pendingIntent is initiated that is passed on to the alarm manager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // the alarmManager finally takes care, that the pendingIntent is queued to start the notification Intent that on click would start the contentIntent
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) || Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime!!, pendingIntent)
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime!!, pendingIntent)
    }

    /**
     * Updates the duration of this entry and automatically sets the (absolute) triggerTime and triggerTimezone
     */
    fun updateDuration(dur: Duration, alarmRelativeTo: AlarmRelativeTo?, referenceDate: Long, referenceTimezone: String?) {
        triggerRelativeDuration = dur.toIsoString()
        alarmRelativeTo?.let { triggerRelativeTo = it.name }
        this.triggerTime = referenceDate + dur.inWholeMilliseconds
        this.triggerTimezone = referenceTimezone
    }
}

/** This enum class defines the possible values for the attribute [Alarm.triggerRelativeTo] for the Component VALARM  */
@Suppress("unused")
enum class AlarmRelativeTo {
    START, END
}

/** This enum class defines the possible values for the attribute [Alarm.action] for the Component VALARM  */
@Suppress("unused")
enum class AlarmAction {
    AUDIO, DISPLAY, EMAIL
}