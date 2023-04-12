/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateMapOf
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.list.ListSettings
import at.techbee.jtx.ui.list.ListViewModel

enum class DetailSettingsOptionGroup { GENERAL, ELEMENT }

enum class DetailSettingsOption(
    val key: String,
    @StringRes val stringResource: Int,
    val group: DetailSettingsOptionGroup,
    val defaultForJournals: Boolean,
    val defaultForNotes: Boolean,
    val defaultForTasks: Boolean,
    val possibleFor: List<Module>
    )
{
    ENABLE_SUMMARY(
        key = "enableSummary",
        stringResource = R.string.summary,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = true,
        defaultForNotes = false,
        defaultForTasks = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_DESCRIPTION(
        key = "enableDescription",
        stringResource = R.string.description,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = true,
        defaultForNotes = true,
        defaultForTasks = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_DTSTART(
        key = "enableStarted",
        stringResource = R.string.started,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = true,
        defaultForNotes = false,
        defaultForTasks = true,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_DUE(
        key = "enableDue",
        stringResource = R.string.due,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = true,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_COMPLETED(
        key = "enableCompleted",
        stringResource = R.string.completed,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = false,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_STATUS(
        key = "enableStatus",
        stringResource = R.string.status,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = true,
        defaultForNotes = false,
        defaultForTasks = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_CLASSIFICATION(
        key = "enableClassification",
        stringResource = R.string.classification,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_PRIORITY(
        key = "enablePriority",
        stringResource = R.string.priority,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = false,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_CATEGORIES(
        key = "enableCategories",
        stringResource = R.string.categories,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = true,
        defaultForNotes = true,
        defaultForTasks = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_ATTENDEES(
        key = "enableAttendees",
        stringResource = R.string.attendees,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_RESOURCES(
        key = "enableResources",
        stringResource = R.string.resources,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = false,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_CONTACT(
        key = "enableContact",
        stringResource = R.string.contact,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_LOCATION(
        key = "enableLocation",
        stringResource = R.string.location,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_URL(
        key = "enableURL",
        stringResource = R.string.url,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_SUBTASKS(
        key = "enableSubtasks",
        stringResource = R.string.subtasks,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = true,
        defaultForNotes = false,
        defaultForTasks = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_SUBNOTES(
        key = "enableSubnotes",
        stringResource = R.string.view_feedback_linked_notes,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_ATTACHMENTS(
        key = "enableAttachments",
        stringResource = R.string.attachments,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_RECURRENCE(
        key = "enableRecurrence",
        stringResource = R.string.recurrence,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_ALARMS(
        key = "enableAlarms",
        stringResource = R.string.alarms,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = true,
        possibleFor = listOf(Module.TODO)
    ),
    ENABLE_COMMENTS(
        key = "enableComments",
        stringResource = R.string.comments,
        group = DetailSettingsOptionGroup.ELEMENT,
        defaultForJournals = false,
        defaultForNotes = false,
        defaultForTasks = false,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_AUTOSAVE(
        key = "enableAutosave",
        stringResource = R.string.menu_view_autosave,
        group = DetailSettingsOptionGroup.GENERAL,
        defaultForJournals = true,
        defaultForNotes = true,
        defaultForTasks = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    ),
    ENABLE_MARKDOWN(
        key = "enableMarkdown",
        stringResource = R.string.menu_view_markdown_formatting,
        group = DetailSettingsOptionGroup.GENERAL,
        defaultForJournals = true,
        defaultForNotes = true,
        defaultForTasks = true,
        possibleFor = listOf(Module.JOURNAL, Module.NOTE, Module.TODO)
    );

    fun getDefault(module: Module) = when(module) {
            Module.JOURNAL -> defaultForJournals
            Module.NOTE -> defaultForNotes
            Module.TODO -> defaultForTasks
        }
}


class DetailSettings {
    val detailSetting = mutableStateMapOf<DetailSettingsOption, Boolean>()
    private var currentModule: Module? = null
    private var prefs: SharedPreferences? = null

    var listSettings: ListSettings? = null  // List settings get overwritten on load but will not be saved!

    fun save() {
        prefs?.edit().apply {
            DetailSettingsOption.values().forEach { detailSettingOption ->
                this?.putBoolean(detailSettingOption.key, detailSetting[detailSettingOption] ?: true)
            }
        }?.apply()
    }

    fun load(module: Module, context: Context) {
        if(currentModule != module || detailSetting.isEmpty() || prefs == null) {
            currentModule = module
            prefs = when (module) {
                Module.JOURNAL -> context.getSharedPreferences(DetailViewModel.PREFS_DETAIL_JOURNALS, Context.MODE_PRIVATE)
                Module.NOTE -> context.getSharedPreferences(DetailViewModel.PREFS_DETAIL_NOTES, Context.MODE_PRIVATE)
                Module.TODO -> context.getSharedPreferences(DetailViewModel.PREFS_DETAIL_TODOS, Context.MODE_PRIVATE)
            }

            detailSetting.apply {
                DetailSettingsOption.values().forEach { detailSettingOption ->
                    prefs?.getBoolean(detailSettingOption.key, detailSettingOption.getDefault(module))?.let { this[detailSettingOption] = it }
                }
            }

            //Load some settings from ListSettings
            listSettings = when (module) {
                Module.JOURNAL -> context.getSharedPreferences(ListViewModel.PREFS_LIST_JOURNALS, Context.MODE_PRIVATE)
                Module.NOTE -> context.getSharedPreferences(ListViewModel.PREFS_LIST_NOTES, Context.MODE_PRIVATE)
                Module.TODO -> context.getSharedPreferences(ListViewModel.PREFS_LIST_TODOS, Context.MODE_PRIVATE)
            }.let { ListSettings.fromPrefs(it)  }

        }
    }
}