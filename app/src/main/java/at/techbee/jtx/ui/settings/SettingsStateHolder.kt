/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.preference.PreferenceManager
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.detail.DetailTopAppBarMode

class SettingsStateHolder(val context: Context) {

    companion object {
        private const val SETTINGS_PRO_INFO_SHOWN = "settingsProInfoShown"
        private const val PREFS_LAST_MODULE = "lastUsedModule"
        private const val PREFS_DETAIL_TOP_APP_BAR_MODE = "detailTopAppBarMode"
    }

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    var settingEnableJournals = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_ENABLE_JOURNALS.key, SwitchSetting.SETTING_ENABLE_JOURNALS.default))
    var settingEnableNotes = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_ENABLE_NOTES.key, SwitchSetting.SETTING_ENABLE_NOTES.default))
    var settingEnableTasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_ENABLE_TASKS.key, SwitchSetting.SETTING_ENABLE_TASKS.default))

    var settingTheme = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_THEME.key, DropdownSetting.SETTING_THEME.default.key) } ?: DropdownSetting.SETTING_THEME.default )
    var settingAudioFormat = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_AUDIO_FORMAT.key, DropdownSetting.SETTING_AUDIO_FORMAT.default.key) } ?: DropdownSetting.SETTING_AUDIO_FORMAT.default )
    var settingAutoExpandSubtasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_AUTO_EXPAND_SUBTASKS.key, SwitchSetting.SETTING_AUTO_EXPAND_SUBTASKS.default))
    var settingAutoExpandSubnotes = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_AUTO_EXPAND_SUBNOTES.key, SwitchSetting.SETTING_AUTO_EXPAND_SUBNOTES.default))
    var settingAutoExpandAttachments = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_AUTO_EXPAND_ATTACHMENTS.key, SwitchSetting.SETTING_AUTO_EXPAND_ATTACHMENTS.default))

    var settingShowProgressForMainTasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SHOW_PROGRESS_FOR_MAINTASKS.key, SwitchSetting.SETTING_SHOW_PROGRESS_FOR_MAINTASKS.default))
    var settingShowProgressForSubTasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SHOW_PROGRESS_FOR_SUBTASKS.key, SwitchSetting.SETTING_SHOW_PROGRESS_FOR_SUBTASKS.default))

    var settingDefaultJournalsDate = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_DEFAULT_JOURNALS_DATE.key, DropdownSetting.SETTING_DEFAULT_JOURNALS_DATE.default.key) } ?: DropdownSetting.SETTING_DEFAULT_JOURNALS_DATE.default )
    var settingDefaultStartDate = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_DEFAULT_START_DATE.key, DropdownSetting.SETTING_DEFAULT_START_DATE.default.key) } ?: DropdownSetting.SETTING_DEFAULT_START_DATE.default )
    var settingDefaultDueDate = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_DEFAULT_DUE_DATE.key, DropdownSetting.SETTING_DEFAULT_DUE_DATE.default.key) } ?: DropdownSetting.SETTING_DEFAULT_DUE_DATE.default )
    var settingStepForProgress = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_PROGRESS_STEP.key, DropdownSetting.SETTING_PROGRESS_STEP.default.key) } ?: DropdownSetting.SETTING_PROGRESS_STEP.default )
    var settingDisableAlarmsReadonly = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_DISABLE_ALARMS_FOR_READONLY.key, SwitchSetting.SETTING_DISABLE_ALARMS_FOR_READONLY.default))
    var settingAutoAlarm = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_AUTO_ALARM.key, DropdownSetting.SETTING_AUTO_ALARM.default.key) } ?: DropdownSetting.SETTING_AUTO_ALARM.default )
    var settingLinkProgressToSubtasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_LINK_PROGRESS_TO_SUBTASKS.key, SwitchSetting.SETTING_LINK_PROGRESS_TO_SUBTASKS.default))
    var settingKeepStatusProgressCompletedInSync = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_KEEP_STATUS_PROGRESS_COMPLETED_IN_SYNC.key, SwitchSetting.SETTING_KEEP_STATUS_PROGRESS_COMPLETED_IN_SYNC.default))
    var settingProtectBiometric = mutableStateOf(DropdownSettingOption.values().find { setting -> setting.key == prefs.getString(
        DropdownSetting.SETTING_PROTECT_BIOMETRIC.key, DropdownSetting.SETTING_PROTECT_BIOMETRIC.default.key) } ?: DropdownSetting.SETTING_PROTECT_BIOMETRIC.default )

    var settingSetDefaultCurrentLocationJournals = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_JOURNALS_SET_DEFAULT_CURRENT_LOCATION.key, SwitchSetting.SETTING_JOURNALS_SET_DEFAULT_CURRENT_LOCATION.default))
    var settingSetDefaultCurrentLocationNotes = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_NOTES_SET_DEFAULT_CURRENT_LOCATION.key, SwitchSetting.SETTING_NOTES_SET_DEFAULT_CURRENT_LOCATION.default))
    var settingSetDefaultCurrentLocationTasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_TASKS_SET_DEFAULT_CURRENT_LOCATION.key, SwitchSetting.SETTING_TASKS_SET_DEFAULT_CURRENT_LOCATION.default))

    var settingStickyAlarms = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_STICKY_ALARMS.key, SwitchSetting.SETTING_STICKY_ALARMS.default))

    var settingSyncOnStart = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SYNC_ON_START.key, SwitchSetting.SETTING_SYNC_ON_START.default))
    var settingSyncOnPullRefresh = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_SYNC_ON_PULL_REFRESH.key, SwitchSetting.SETTING_SYNC_ON_PULL_REFRESH.default))

    //invisible settings
    var proInfoShown = mutableStateOf(prefs.getBoolean(SETTINGS_PRO_INFO_SHOWN, false))
        set(newValue) {
            prefs.edit().putBoolean(SETTINGS_PRO_INFO_SHOWN, newValue.value).apply()
            field = newValue
        }

    var lastUsedModule = mutableStateOf(
        try { Module.valueOf(prefs.getString(PREFS_LAST_MODULE, null)?: Module.JOURNAL.name) } catch (e: java.lang.IllegalArgumentException) { Module.JOURNAL }
    )
        set(newValue) {
            prefs.edit().putString(PREFS_LAST_MODULE, newValue.value.name).apply()
            field = newValue
        }

    var detailTopAppBarMode = mutableStateOf(
        try { DetailTopAppBarMode.valueOf(prefs.getString(PREFS_DETAIL_TOP_APP_BAR_MODE, null)?: DetailTopAppBarMode.ADD_SUBTASK.name) } catch (e: java.lang.IllegalArgumentException) { DetailTopAppBarMode.ADD_SUBTASK }
    )
        set(newValue) {
            prefs.edit().putString(PREFS_DETAIL_TOP_APP_BAR_MODE, newValue.value.name).apply()
            field = newValue
        }

    /*
    var showJtx20releaseinfo = mutableStateOf(prefs.getBoolean("jtx_2.0_beta_info_shown", context.packageManager.getPackageInfoCompat(context.packageName, 0).firstInstallTime < 1665260058251))
        set(newValue) {
            prefs.edit().putBoolean("jtx_2.0_beta_info_shown", newValue.value).apply()
            field = newValue
        }

    var showV20009releaseInfo = mutableStateOf(prefs.getBoolean("showV20009releaseInfo", context.packageManager.getPackageInfoCompat(context.packageName, 0).firstInstallTime < 1668463407363))
        set(newValue) {
            prefs.edit().putBoolean("showV20009releaseInfo", newValue.value).apply()
            field = newValue
        }
     */
}


