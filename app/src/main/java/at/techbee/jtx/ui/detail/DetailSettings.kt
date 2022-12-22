/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Link
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.vector.ImageVector
import at.techbee.jtx.R
import at.techbee.jtx.database.Module

enum class DetailSettingsOptionGroup { GENERAL, ELEMENT }

enum class DetailSettingsOption(
    val setting: String,
    val stringResource: Int,
    val icon: ImageVector?,
    val group: DetailSettingsOptionGroup,
    val default: Boolean,
    val possibleFor: List<Module>
    )
{
    ENABLE_CATEGORIES(
        setting = "enableCategories",
        stringResource = R.string.categories,
        icon = Icons.Outlined.Category,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_ATTENDEES(
        setting = "enableAttendees",
        stringResource = R.string.attendees,
        icon = Icons.Outlined.Category,   //TODO
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_RESOURCES(
        setting = "enableResources",
        stringResource = R.string.resources,
        icon = Icons.Outlined.Category,  //TODO
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_CONTACT(
        setting = "enableContact",
        stringResource = R.string.contact,
        icon = Icons.Outlined.Category,  //TODO
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_LOCATION(
        setting = "enableLocation",
        stringResource = R.string.location,
        icon = Icons.Outlined.Category,   //TODO
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_URL(
        setting = "enableURL",
        stringResource = R.string.url,
        icon = Icons.Outlined.Link,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_SUBTASKS(
        setting = "enableSubtasks",
        stringResource = R.string.subtasks,
        icon = Icons.Outlined.Category,  //TODO
        group = DetailSettingsOptionGroup.ELEMENT,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_SUBNOTES(
        setting = "enableSubnotes",
        stringResource = R.string.view_feedback_linked_notes,
        icon = Icons.Outlined.Category,   //TODO
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_ATTACHMENTS(
        setting = "enableAttachments",
        stringResource = R.string.attachments,
        icon = Icons.Outlined.Attachment,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_RECURRENCE(
        setting = "enableRecurrence",
        stringResource = R.string.recurrence,
        icon = Icons.Outlined.Category,  // TODO
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_ALARMS(
        setting = "enableAlarms",
        stringResource = R.string.alarms,
        icon = Icons.Outlined.Alarm,
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_COMMENTS(
        setting = "enableComments",
        stringResource = R.string.comments,
        icon = Icons.Outlined.Category,   //TODO
        group = DetailSettingsOptionGroup.ELEMENT,
        default = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_AUTOSAVE(
        setting = "enableAutosave",
        stringResource = R.string.menu_view_autosave,
        icon = Icons.Outlined.Category,   //TODO
        group = DetailSettingsOptionGroup.GENERAL,
        default = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_MARKDOWN(
        setting = "enableMarkdown",
        stringResource = R.string.menu_view_markdown_formatting,
        icon = Icons.Outlined.Category,   //TODO
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