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
    val setting: String,
    val stringResource: Int,
    val group: DetailSettingsOptionGroup,
    val default: Boolean,
    val possibleFor: List<Module>
    )
{
    ENABLE_DTSTART(
        setting = "enableStarted",
        stringResource = R.string.started,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_DUE(
        setting = "enableDue",
        stringResource = R.string.due,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_COMPLETED(
        setting = "enableCompleted",
        stringResource = R.string.completed,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_STATUS(
        setting = "enableStatus",
        stringResource = R.string.status,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_CLASSIFICATION(
        setting = "enableClassification",
        stringResource = R.string.classification,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_PRIORITY(
        setting = "enablePriority",
        stringResource = R.string.priority,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_CATEGORIES(
        setting = "enableCategories",
        stringResource = R.string.categories,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_ATTENDEES(
        setting = "enableAttendees",
        stringResource = R.string.attendees,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_RESOURCES(
        setting = "enableResources",
        stringResource = R.string.resources,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_CONTACT(
        setting = "enableContact",
        stringResource = R.string.contact,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_LOCATION(
        setting = "enableLocation",
        stringResource = R.string.location,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_URL(
        setting = "enableURL",
        stringResource = R.string.url,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_SUBTASKS(
        setting = "enableSubtasks",
        stringResource = R.string.subtasks,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_SUBNOTES(
        setting = "enableSubnotes",
        stringResource = R.string.view_feedback_linked_notes,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_ATTACHMENTS(
        setting = "enableAttachments",
        stringResource = R.string.attachments,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_RECURRENCE(
        setting = "enableRecurrence",
        stringResource = R.string.recurrence,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_ALARMS(
        setting = "enableAlarms",
        stringResource = R.string.alarms,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_COMMENTS(
        setting = "enableComments",
        stringResource = R.string.comments,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_AUTOSAVE(
        setting = "enableAutosave",
        stringResource = R.string.menu_view_autosave,
        group = DetailSettingsOptionGroup.GENERAL,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_MARKDOWN(
        setting = "enableMarkdown",
        stringResource = R.string.menu_view_markdown_formatting,
        group = DetailSettingsOptionGroup.GENERAL,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    )
}


data class DetailSettings(
    val prefs: SharedPreferences
) {
    val detailSetting = mutableStateMapOf<DetailSettingsOption, Boolean>().apply {
        DetailSettingsOption.values().forEach { detailSettingOption ->
            this[detailSettingOption] = prefs.getBoolean(detailSettingOption.setting, detailSettingOption.default)
        }
    }

    fun save() {
        prefs.edit().apply {
            DetailSettingsOption.values().forEach { detailSettingOption ->
                putBoolean(detailSettingOption.setting, detailSetting[detailSettingOption] ?: true)
            }
        }.apply()
    }
}