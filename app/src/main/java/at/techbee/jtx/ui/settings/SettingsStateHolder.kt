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

    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    var settingEnableJournals = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_ENABLE_JOURNALS.key, SwitchSetting.SETTING_ENABLE_JOURNALS.default))
    var settingEnableNotes = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_ENABLE_NOTES.key, SwitchSetting.SETTING_ENABLE_NOTES.default))
    var settingEnableTasks = mutableStateOf(prefs.getBoolean(SwitchSetting.SETTING_ENABLE_TASKS.key, SwitchSetting.SETTING_ENABLE_TASKS.default))

    var settingTheme = mutableStateOf(DropdownSetting.SETTING_THEME.getSetting(prefs))
    var settingAudioFormat = mutableStateOf(DropdownSetting.SETTING_AUDIO_FORMAT.getSetting(prefs))
    var settingAutoExpandSubtasks = mutableStateOf(SwitchSetting.SETTING_AUTO_EXPAND_SUBTASKS.getSetting(prefs))
    var settingAutoExpandSubnotes = mutableStateOf(SwitchSetting.SETTING_AUTO_EXPAND_SUBNOTES.getSetting(prefs))
    var settingAutoExpandAttachments = mutableStateOf(SwitchSetting.SETTING_AUTO_EXPAND_ATTACHMENTS.getSetting(prefs))

    var settingShowProgressForMainTasks = mutableStateOf(SwitchSetting.SETTING_SHOW_PROGRESS_FOR_MAINTASKS.getSetting(prefs))
    var settingShowProgressForSubTasks = mutableStateOf(SwitchSetting.SETTING_SHOW_PROGRESS_FOR_SUBTASKS.getSetting(prefs))

    var settingDefaultJournalsDate = mutableStateOf(DropdownSetting.SETTING_DEFAULT_JOURNALS_DATE.getSetting(prefs))
    var settingDefaultStartDate = mutableStateOf(DropdownSetting.SETTING_DEFAULT_START_DATE.getSetting(prefs))
    var settingDefaultStartTime = mutableStateOf(TimeSetting.SETTING_DEFAULT_START_TIME.getSetting(prefs))
    var settingDefaultDueDate = mutableStateOf(DropdownSetting.SETTING_DEFAULT_DUE_DATE.getSetting(prefs))
    var settingDefaultDueTime = mutableStateOf(TimeSetting.SETTING_DEFAULT_DUE_TIME.getSetting(prefs))
    var settingStepForProgress = mutableStateOf(DropdownSetting.SETTING_PROGRESS_STEP.getSetting(prefs))
    var settingDisableAlarmsReadonly = mutableStateOf(SwitchSetting.SETTING_DISABLE_ALARMS_FOR_READONLY.getSetting(prefs))
    var settingAutoAlarm = mutableStateOf(DropdownSetting.SETTING_AUTO_ALARM.getSetting(prefs))
    var settingLinkProgressToSubtasks = mutableStateOf(SwitchSetting.SETTING_LINK_PROGRESS_TO_SUBTASKS.getSetting(prefs))
    var settingKeepStatusProgressCompletedInSync = mutableStateOf(SwitchSetting.SETTING_KEEP_STATUS_PROGRESS_COMPLETED_IN_SYNC.getSetting(prefs))
    var settingProtectBiometric = mutableStateOf(DropdownSetting.SETTING_PROTECT_BIOMETRIC.getSetting(prefs))

    var settingSetDefaultCurrentLocationJournals = mutableStateOf(SwitchSetting.SETTING_JOURNALS_SET_DEFAULT_CURRENT_LOCATION.getSetting(prefs))
    var settingSetDefaultCurrentLocationNotes = mutableStateOf(SwitchSetting.SETTING_NOTES_SET_DEFAULT_CURRENT_LOCATION.getSetting(prefs))
    var settingSetDefaultCurrentLocationTasks = mutableStateOf(SwitchSetting.SETTING_TASKS_SET_DEFAULT_CURRENT_LOCATION.getSetting(prefs))

    var settingStickyAlarms = mutableStateOf(SwitchSetting.SETTING_STICKY_ALARMS.getSetting(prefs))

    var settingSyncOnStart = mutableStateOf(SwitchSetting.SETTING_SYNC_ON_START.getSetting(prefs))
    var settingSyncOnPullRefresh = mutableStateOf(SwitchSetting.SETTING_SYNC_ON_PULL_REFRESH.getSetting(prefs))

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

    val detailTopAppBarMode = mutableStateOf(
        DetailTopAppBarMode.values().find { it.name == prefs.getString(PREFS_DETAIL_TOP_APP_BAR_MODE, null) }?: DetailTopAppBarMode.ADD_SUBTASK
    )
    fun setDetailTopAppBarMode(mode: DetailTopAppBarMode) {
        detailTopAppBarMode.value = mode
        prefs.edit().putString(PREFS_DETAIL_TOP_APP_BAR_MODE, mode.name).apply()
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


