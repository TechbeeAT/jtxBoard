package at.techbee.jtx.ui.compose.stateholder

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.preference.PreferenceManager
import at.techbee.jtx.settings.DropdownSetting
import at.techbee.jtx.settings.DropdownSettingOption
import at.techbee.jtx.settings.SwitchSetting

class SettingsStateHolder(val context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    var settingTheme = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_THEME.key, DropdownSetting.SETTING_THEME.default.key) } ?: DropdownSetting.SETTING_THEME.default )
    var settingAudioFormat = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(DropdownSetting.SETTING_AUDIO_FORMAT.key, DropdownSetting.SETTING_AUDIO_FORMAT.default.key) } ?: DropdownSetting.SETTING_AUDIO_FORMAT.default )
    var settingAutoExpandSubtasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_AUTO_EXPAND_SUBTASKS.key, SwitchSetting.SETTING_AUTO_EXPAND_SUBTASKS.default))
    var settingAutoExpandSubnotes = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_AUTO_EXPAND_SUBNOTES.key, SwitchSetting.SETTING_AUTO_EXPAND_SUBNOTES.default))
    var settingAutoExpandAttachments = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_AUTO_EXPAND_ATTACHMENTS.key, SwitchSetting.SETTING_AUTO_EXPAND_ATTACHMENTS.default))

    var settingShowProgressForMainTasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST.key, SwitchSetting.SETTING_SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST.default))
    var settingShowProgressForSubTasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SHOW_PROGRESS_FOR_SUBTASKS.key, SwitchSetting.SETTING_SHOW_PROGRESS_FOR_SUBTASKS.default))
    var settingShowSubtasksInTasklist = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SHOW_SUBTASKS_IN_TASKLIST.key, SwitchSetting.SETTING_SHOW_SUBTASKS_IN_TASKLIST.default))
    var settingShowSubnotesInNoteslist = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SHOW_SUBNOTES_IN_NOTESLIST.key, SwitchSetting.SETTING_SHOW_SUBNOTES_IN_NOTESLIST.default))
    var settingShowSubjournalsInJournallist = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SHOW_SUBJOURNALS_IN_JOURNALLIST.key, SwitchSetting.SETTING_SHOW_SUBJOURNALS_IN_JOURNALLIST.default))

    var settingDefaultStartDate = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(DropdownSetting.SETTING_DEFAULT_START_DATE.key, DropdownSetting.SETTING_DEFAULT_START_DATE.default.key) } ?: DropdownSetting.SETTING_DEFAULT_START_DATE.default )
    var settingDefaultDueDate = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(DropdownSetting.SETTING_DEFAULT_DUE_DATE.key, DropdownSetting.SETTING_DEFAULT_DUE_DATE.default.key) } ?: DropdownSetting.SETTING_DEFAULT_DUE_DATE.default )
}


