/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateMapOf


data class DetailSettings(
    val prefs: SharedPreferences
) {

    val switchSetting = mutableStateMapOf(
        Pair(ENABLE_CATEGORIES, prefs.getBoolean(ENABLE_CATEGORIES, true)),
        Pair(ENABLE_ATTENDEES, prefs.getBoolean(ENABLE_ATTENDEES, false)),
        Pair(ENABLE_RESOURCES, prefs.getBoolean(ENABLE_RESOURCES, false)),
        Pair(ENABLE_CONTACT, prefs.getBoolean(ENABLE_CONTACT, false)),
        Pair(ENABLE_LOCATION, prefs.getBoolean(ENABLE_LOCATION, false)),
        Pair(ENABLE_URL, prefs.getBoolean(ENABLE_URL, false)),
        Pair(ENABLE_SUBTASKS, prefs.getBoolean(ENABLE_SUBTASKS, true)),
        Pair(ENABLE_SUBNOTES, prefs.getBoolean(ENABLE_SUBNOTES, false)),
        Pair(ENABLE_ATTACHMENTS, prefs.getBoolean(ENABLE_ATTACHMENTS, false)),
        Pair(ENABLE_RECURRENCE, prefs.getBoolean(ENABLE_RECURRENCE, false)),
        Pair(ENABLE_ALARMS, prefs.getBoolean(ENABLE_ALARMS, false)),
        Pair(ENABLE_COMMENTS, prefs.getBoolean(ENABLE_COMMENTS, false)),
        Pair(ENABLE_AUTOSAVE, prefs.getBoolean(ENABLE_AUTOSAVE, true)),
        Pair(ENABLE_MARKDOWN, prefs.getBoolean(ENABLE_MARKDOWN, true)),
        )

    companion object {
        const val ENABLE_CATEGORIES = "enableCategories"
        const val ENABLE_ATTENDEES = "enableAttendees"
        const val ENABLE_RESOURCES = "enableResources"
        const val ENABLE_CONTACT = "enableContact"
        const val ENABLE_LOCATION = "enableLocation"
        const val ENABLE_URL = "enableURL"
        const val ENABLE_SUBTASKS = "enableSubtasks"
        const val ENABLE_SUBNOTES = "enableSubnotes"
        const val ENABLE_ATTACHMENTS = "enableAttachments"
        const val ENABLE_RECURRENCE = "enableRecurrence"
        const val ENABLE_ALARMS = "enableAlarms"
        const val ENABLE_COMMENTS = "enableComments"
        const val ENABLE_AUTOSAVE = "enableAutosave"
        const val ENABLE_MARKDOWN = "enableMarkdown"
    }

    fun save() {
        prefs.edit().apply {
            putBoolean(ENABLE_CATEGORIES, switchSetting[ENABLE_CATEGORIES] ?: true)
            putBoolean(ENABLE_ATTENDEES, switchSetting[ENABLE_ATTENDEES] ?: true)
            putBoolean(ENABLE_RESOURCES, switchSetting[ENABLE_RESOURCES] ?: true)
            putBoolean(ENABLE_CONTACT, switchSetting[ENABLE_CONTACT] ?: true)
            putBoolean(ENABLE_LOCATION, switchSetting[ENABLE_LOCATION] ?: true)
            putBoolean(ENABLE_URL, switchSetting[ENABLE_URL] ?: true)
            putBoolean(ENABLE_SUBTASKS, switchSetting[ENABLE_SUBTASKS] ?: true)
            putBoolean(ENABLE_SUBNOTES, switchSetting[ENABLE_SUBNOTES] ?: true)
            putBoolean(ENABLE_ATTACHMENTS, switchSetting[ENABLE_ATTACHMENTS] ?: true)
            putBoolean(ENABLE_RECURRENCE, switchSetting[ENABLE_RECURRENCE] ?: true)
            putBoolean(ENABLE_ALARMS, switchSetting[ENABLE_ALARMS] ?: true)
            putBoolean(ENABLE_COMMENTS, switchSetting[ENABLE_COMMENTS] ?: true)
            putBoolean(ENABLE_AUTOSAVE, switchSetting[ENABLE_AUTOSAVE] ?: true)
            putBoolean(ENABLE_MARKDOWN, switchSetting[ENABLE_MARKDOWN] ?: true)
        }.apply()
    }
}