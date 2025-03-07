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
import androidx.compose.material.icons.outlined.Language
import androidx.compose.ui.graphics.vector.ImageVector
import at.techbee.jtx.R
import java.util.TimeZone

enum class DropdownSettingTimezone(
    val key: String,
    val icon: ImageVector,
    val title: Int,
    val subtitle: Int? = null,
    val options: List<String?>,
    val default: String?
) {
    SETTING_DEFAULT_START_TIMEZONE(
        key = "setting_default_start_timezone",
        icon = Icons.Outlined.Language,
        title = R.string.settings_default_start_timezone,
        options = mutableListOf<String?>().apply {
            add(null)
            addAll(TimeZone.getAvailableIDs())
        },
        default = null
    ),
    SETTING_DEFAULT_DUE_TIMEZONE(
        key = "setting_default_due_timezone",
        icon = Icons.Outlined.Language,
        title = R.string.settings_default_due_timezone,
        options = mutableListOf<String?>().apply {
            add(null)
            addAll(TimeZone.getAvailableIDs())
        },
        default = null
    );

    fun saveSetting(newTimezone: String?, prefs: SharedPreferences) =
        prefs.edit().putString(key, newTimezone).apply()

    fun getSetting(prefs: SharedPreferences) = TimeZone.getAvailableIDs().find { timezone ->
        timezone == prefs.getString(key, null)
    } ?: default

    companion object {
        /**
         * Returns the string of the timezone if it's a valid timezone, otherwise null
         */
        fun fromStringValidated(string: String?) = if(TimeZone.getAvailableIDs().contains(string)) string else null
    }
}
