/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.ui.graphics.vector.ImageVector
import at.techbee.jtx.R
import java.time.DateTimeException
import java.time.LocalTime

enum class TimeSetting(
    val keyHour: String,
    val keyMinute: String,
    val icon: ImageVector,
    val title: Int,
    //val subtitle: Int? = null
) {
    SETTING_DEFAULT_START_TIME(
        keyHour = "setting_default_start_time_hour",
        keyMinute = "setting_default_start_time_minute",
        icon = Icons.Outlined.AccessTime,
        title = R.string.settings_default_start_time,
    ),
    SETTING_DEFAULT_DUE_TIME(
        keyHour = "setting_default_due_time_hour",
        keyMinute = "setting_default_due_time_minute",
        icon = Icons.Outlined.AccessTime,
        title = R.string.settings_default_due_time,
    )
    ;

    fun saveSetting(time: LocalTime?, prefs: SharedPreferences) {
        if(time == null) {
            prefs.edit()
                .remove(keyHour)
                .remove(keyMinute)
                .apply()
        } else {
            prefs.edit()
                .putInt(keyHour, time.hour)
                .putInt(keyMinute, time.minute)
                .apply()
        }
    }

    fun getSetting(prefs: SharedPreferences): LocalTime? {
        val hour = prefs.getInt(keyHour, -1)
        val minute = prefs.getInt(keyMinute, -1)
        return if(hour == -1 || minute == -1)
            null
        else
            LocalTime.of(hour, minute)
    }

    companion object {
        /**
         * Returns a LocalTime of a hour and minute as strings.
         */
        fun fromStrings(hour: String?, minute: String?): LocalTime? {
            val hourInt = hour?.toIntOrNull()
            val minuteInt = minute?.toIntOrNull()
            if(hourInt == null || minuteInt == null)
                return null

            return try {
                LocalTime.of(hourInt, minuteInt)
            } catch (e: DateTimeException) {
                null
            }
        }
    }
}
