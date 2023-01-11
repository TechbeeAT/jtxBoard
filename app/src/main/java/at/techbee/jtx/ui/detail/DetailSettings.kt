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
import at.techbee.jtx.R
import at.techbee.jtx.database.Module

enum class DetailSettingsOptionGroup { GENERAL, ELEMENT }

enum class DetailSettingsOption(
    val key: String,
    val stringResource: Int,
    val group: DetailSettingsOptionGroup,
    val default: Boolean,
    val possibleFor: List<Module>
    )
{
    ENABLE_DTSTART(
        key = "enableStarted",
        stringResource = R.string.started,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_DUE(
        key = "enableDue",
        stringResource = R.string.due,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_COMPLETED(
        key = "enableCompleted",
        stringResource = R.string.completed,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_STATUS(
        key = "enableStatus",
        stringResource = R.string.status,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_CLASSIFICATION(
        key = "enableClassification",
        stringResource = R.string.classification,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_PRIORITY(
        key = "enablePriority",
        stringResource = R.string.priority,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_CATEGORIES(
        key = "enableCategories",
        stringResource = R.string.categories,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_ATTENDEES(
        key = "enableAttendees",
        stringResource = R.string.attendees,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_RESOURCES(
        key = "enableResources",
        stringResource = R.string.resources,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_CONTACT(
        key = "enableContact",
        stringResource = R.string.contact,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_LOCATION(
        key = "enableLocation",
        stringResource = R.string.location,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_URL(
        key = "enableURL",
        stringResource = R.string.url,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_SUBTASKS(
        key = "enableSubtasks",
        stringResource = R.string.subtasks,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_SUBNOTES(
        key = "enableSubnotes",
        stringResource = R.string.view_feedback_linked_notes,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_ATTACHMENTS(
        key = "enableAttachments",
        stringResource = R.string.attachments,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_RECURRENCE(
        key = "enableRecurrence",
        stringResource = R.string.recurrence,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_ALARMS(
        key = "enableAlarms",
        stringResource = R.string.alarms,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_COMMENTS(
        key = "enableComments",
        stringResource = R.string.comments,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_AUTOSAVE(
        key = "enableAutosave",
        stringResource = R.string.menu_view_autosave,
        group = DetailSettingsOptionGroup.GENERAL,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_MARKDOWN(
        key = "enableMarkdown",
        stringResource = R.string.menu_view_markdown_formatting,
        group = DetailSettingsOptionGroup.GENERAL,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    )
}


data class DetailSettings(val prefs: SharedPreferences) {
    val detailSetting = mutableStateMapOf<DetailSettingsOption, Boolean>().apply {
        DetailSettingsOption.values().forEach { detailSettingOption ->
            this[detailSettingOption] = prefs.getBoolean(detailSettingOption.key, detailSettingOption.default)
        }
    }

    fun save() {
        prefs.edit().apply {
            DetailSettingsOption.values().forEach { detailSettingOption ->
                putBoolean(detailSettingOption.key, detailSetting[detailSettingOption] ?: true)
            }
        }.apply()
    }
}