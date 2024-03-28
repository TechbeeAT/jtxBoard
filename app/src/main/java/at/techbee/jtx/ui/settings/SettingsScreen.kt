/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.NotificationPublisher
import at.techbee.jtx.R
import at.techbee.jtx.ui.GlobalStateHolder
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.settings.DropdownSetting.SETTING_AUDIO_FORMAT
import at.techbee.jtx.ui.settings.DropdownSetting.SETTING_AUTO_ALARM
import at.techbee.jtx.ui.settings.DropdownSetting.SETTING_DEFAULT_DUE_DATE
import at.techbee.jtx.ui.settings.DropdownSetting.SETTING_DEFAULT_JOURNALS_DATE
import at.techbee.jtx.ui.settings.DropdownSetting.SETTING_DEFAULT_START_DATE
import at.techbee.jtx.ui.settings.DropdownSetting.SETTING_DISPLAY_TIMEZONE
import at.techbee.jtx.ui.settings.DropdownSetting.SETTING_FONT
import at.techbee.jtx.ui.settings.DropdownSetting.SETTING_PROGRESS_STEP
import at.techbee.jtx.ui.settings.DropdownSetting.SETTING_PROTECT_BIOMETRIC
import at.techbee.jtx.ui.settings.DropdownSetting.SETTING_THEME
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_ACCESSIBILITY_MODE
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_AUTO_EXPAND_ATTACHMENTS
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_AUTO_EXPAND_SUBNOTES
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_AUTO_EXPAND_SUBTASKS
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_DISABLE_ALARMS_FOR_READONLY
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_ENABLE_JOURNALS
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_ENABLE_NOTES
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_ENABLE_TASKS
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_FULLSCREEN_ALARMS
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_JOURNALS_SET_DEFAULT_CURRENT_LOCATION
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_KEEP_STATUS_PROGRESS_COMPLETED_IN_SYNC
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_LINK_PROGRESS_TO_SUBTASKS
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_NOTES_SET_DEFAULT_CURRENT_LOCATION
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_SHOW_PROGRESS_FOR_MAINTASKS
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_SHOW_PROGRESS_FOR_SUBTASKS
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_STICKY_ALARMS
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_SYNC_ON_PULL_REFRESH
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_SYNC_ON_START
import at.techbee.jtx.ui.settings.SwitchSetting.SETTING_TASKS_SET_DEFAULT_CURRENT_LOCATION
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


enum class SettingsScreenSection { APP_SETTINGS, ACTIVE_MODUES, ITEM_LIST, JOURNALS_SETTINGS, NOTES_SETTINGS, TASKS_SETTINGS, TASKS_SETTINGS_STATUS, ALARMS_SETTINGS, SYNC_SETTINGS }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsStateHolder: SettingsStateHolder,
    globalStateHolder: GlobalStateHolder,
    modifier: Modifier = Modifier
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expandedSection by remember { mutableStateOf<SettingsScreenSection?>(SettingsScreenSection.APP_SETTINGS) }
    fun expandOrCollapse(selectedSection: SettingsScreenSection) {
        expandedSection = if (expandedSection == selectedSection) null else selectedSection
    }

    val languageOptions = mutableListOf<Locale?>(null)
    for (language in BuildConfig.TRANSLATION_ARRAY) {
        if(language == "zh-rTW")
            languageOptions.add(Locale.TRADITIONAL_CHINESE)
        else
            languageOptions.add(Locale.forLanguageTag(language))
    }

    var pendingSettingProtectiometric: DropdownSettingOption? by remember { mutableStateOf(null) }
    if(globalStateHolder.isAuthenticated.value && pendingSettingProtectiometric != null) {
        settingsStateHolder.settingProtectBiometric.value = pendingSettingProtectiometric!!
        SETTING_PROTECT_BIOMETRIC.saveSetting(pendingSettingProtectiometric!!, settingsStateHolder.prefs)
        pendingSettingProtectiometric = null
    }

    val locationPermissionState = if (!LocalInspectionMode.current) rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    ) else null



    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_settings)
            )
        },

        ) { paddingValues ->
        JtxNavigationDrawer(
            drawerState = drawerState,
            mainContent = {

                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    ExpandableSettingsSection(
                        headerText = R.string.settings_app,
                        expanded = expandedSection == SettingsScreenSection.APP_SETTINGS,
                        onToggle = { expandOrCollapse(SettingsScreenSection.APP_SETTINGS) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        
                        DropdownSettingElement(
                            setting = SETTING_THEME,
                            selected = settingsStateHolder.settingTheme.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingTheme.value = selection
                                SETTING_THEME.saveSetting(selection, settingsStateHolder.prefs)
                                when (selection) {
                                    DropdownSettingOption.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                    DropdownSettingOption.THEME_TRUE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                    DropdownSettingOption.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                                }
                            }
                        )

                        DropdownSettingElement(
                            setting = SETTING_FONT,
                            selected = settingsStateHolder.settingFont.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingFont.value = selection
                                SETTING_FONT.saveSetting(selection, settingsStateHolder.prefs)
                            }
                        )

                        /* Special Handling for Language Selector */
                        val appCompatLocales = AppCompatDelegate.getApplicationLocales()
                        var defaultLocale: Locale? = null
                        if(!appCompatLocales.isEmpty) {
                            for (i in 0 until appCompatLocales.size()) {
                                val locale = appCompatLocales[i] ?: continue
                                if (languageOptions.contains(appCompatLocales[i]!!) || appCompatLocales[i] == Locale.TRADITIONAL_CHINESE) {
                                    defaultLocale = locale
                                    break
                                }
                            }
                        }
                        var selectedLanguage by remember { mutableStateOf(defaultLocale) }
                        var languagesExpanded by remember { mutableStateOf(false) } // initial value

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = modifier.clickable { languagesExpanded = true }
                        ) {

                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                modifier = Modifier.padding(16.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp)
                            ) {

                                Text(
                                    text = stringResource(id = R.string.settings_select_language),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = selectedLanguage?.displayName ?: stringResource(id = R.string.settings_select_language_system),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp))

                            DropdownMenu(
                                expanded = languagesExpanded,
                                onDismissRequest = { languagesExpanded = false },
                            ) {
                                languageOptions.sortedBy { it?.displayName }.forEach { locale ->
                                    DropdownMenuItem(
                                        onClick = {
                                            languagesExpanded = false
                                            selectedLanguage = locale
                                            AppCompatDelegate.setApplicationLocales(
                                                locale?.let { LocaleListCompat.create(it) } ?:  LocaleListCompat.getEmptyLocaleList()
                                            )
                                        },
                                        text = {
                                            Text(
                                                text = locale?.displayName ?: stringResource(id = R.string.settings_select_language_system),
                                                modifier = Modifier
                                                    .align(Alignment.Start)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        DropdownSettingElement(
                            setting = SETTING_AUDIO_FORMAT,
                            selected = settingsStateHolder.settingAudioFormat.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingAudioFormat.value = selection
                                SETTING_AUDIO_FORMAT.saveSetting(selection, settingsStateHolder.prefs)
                            }
                        )

                        if(globalStateHolder.biometricStatus == BiometricManager.BIOMETRIC_SUCCESS) {
                            DropdownSettingElement(
                                setting = SETTING_PROTECT_BIOMETRIC,
                                selected = settingsStateHolder.settingProtectBiometric.value,
                                onSelectionChanged = { selection ->
                                    if(settingsStateHolder.settingProtectBiometric.value == selection)
                                        return@DropdownSettingElement

                                    if(!globalStateHolder.isAuthenticated.value) {
                                        val promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
                                            .setTitle(context.getString(R.string.settings_protect_biometric))
                                            .setSubtitle(context.getString(R.string.settings_protect_biometric_info_on_settings_unlock))
                                            .setNegativeButtonText(context.getString(R.string.cancel))
                                            .build()
                                        globalStateHolder.biometricPrompt?.authenticate(promptInfo)
                                        pendingSettingProtectiometric = selection
                                    } else {
                                        settingsStateHolder.settingProtectBiometric.value = selection
                                        SETTING_PROTECT_BIOMETRIC.saveSetting(selection, settingsStateHolder.prefs)
                                    }
                                }
                            )
                        }

                        DropdownSettingElement(
                            setting = SETTING_DISPLAY_TIMEZONE,
                            selected = settingsStateHolder.settingDisplayTimezone.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingDisplayTimezone.value = selection
                                SETTING_DISPLAY_TIMEZONE.saveSetting(selection, settingsStateHolder.prefs)
                            }
                        )

                        SwitchSettingElement(
                            setting = SETTING_ACCESSIBILITY_MODE,
                            checked = settingsStateHolder.settingAccessibilityMode,
                            onCheckedChanged = {
                                settingsStateHolder.settingAccessibilityMode.value = it
                                SETTING_ACCESSIBILITY_MODE.saveSetting(it, settingsStateHolder.prefs)
                            }
                        )
                    }

                    ExpandableSettingsSection(
                        headerText = R.string.settings_modules,
                        expanded = expandedSection == SettingsScreenSection.ACTIVE_MODUES,
                        onToggle = { expandOrCollapse(SettingsScreenSection.ACTIVE_MODUES) },
                        modifier = Modifier.fillMaxWidth()
                    ) {


                        SwitchSettingElement(
                            setting = SETTING_ENABLE_JOURNALS,
                            checked = settingsStateHolder.settingEnableJournals,
                            onCheckedChanged = {
                                settingsStateHolder.settingEnableJournals.value = it
                                SETTING_ENABLE_JOURNALS.saveSetting(it, settingsStateHolder.prefs)
                            },
                            enabled = !(!settingsStateHolder.settingEnableNotes.value && !settingsStateHolder.settingEnableTasks.value)
                        )
                        SwitchSettingElement(
                            setting = SETTING_ENABLE_NOTES,
                            checked = settingsStateHolder.settingEnableNotes,
                            onCheckedChanged = {
                                settingsStateHolder.settingEnableNotes.value = it
                                SETTING_ENABLE_NOTES.saveSetting(it, settingsStateHolder.prefs)
                            },
                            enabled = !(!settingsStateHolder.settingEnableJournals.value && !settingsStateHolder.settingEnableTasks.value)
                        )
                        SwitchSettingElement(
                            setting = SETTING_ENABLE_TASKS,
                            checked = settingsStateHolder.settingEnableTasks,
                            onCheckedChanged = {
                                settingsStateHolder.settingEnableTasks.value = it
                                SETTING_ENABLE_TASKS.saveSetting(it, settingsStateHolder.prefs)
                            },
                            enabled = !(!settingsStateHolder.settingEnableJournals.value && !settingsStateHolder.settingEnableNotes.value)
                        )
                    }

                    ExpandableSettingsSection(
                        headerText = R.string.settings_list,
                        expanded = expandedSection == SettingsScreenSection.ITEM_LIST,
                        onToggle = { expandOrCollapse(SettingsScreenSection.ITEM_LIST) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SwitchSettingElement(
                            setting = SETTING_AUTO_EXPAND_SUBTASKS,
                            checked = settingsStateHolder.settingAutoExpandSubtasks,
                            onCheckedChanged = {
                                settingsStateHolder.settingAutoExpandSubtasks.value = it
                                SETTING_AUTO_EXPAND_SUBTASKS.saveSetting(it, settingsStateHolder.prefs)
                            })
                        SwitchSettingElement(
                            setting = SETTING_AUTO_EXPAND_SUBNOTES,
                            checked = settingsStateHolder.settingAutoExpandSubnotes,
                            onCheckedChanged = {
                                settingsStateHolder.settingAutoExpandSubnotes.value = it
                                SETTING_AUTO_EXPAND_SUBNOTES.saveSetting(it, settingsStateHolder.prefs)
                            })
                        SwitchSettingElement(
                            setting = SETTING_AUTO_EXPAND_ATTACHMENTS,
                            checked = settingsStateHolder.settingAutoExpandAttachments,
                            onCheckedChanged = {
                                settingsStateHolder.settingAutoExpandAttachments.value = it
                                SETTING_AUTO_EXPAND_ATTACHMENTS.saveSetting(it, settingsStateHolder.prefs)
                            })
                    }

                    ExpandableSettingsSection(
                        headerText = R.string.settings_journals,
                        expanded = expandedSection == SettingsScreenSection.JOURNALS_SETTINGS,
                        onToggle = { expandOrCollapse(SettingsScreenSection.JOURNALS_SETTINGS) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DropdownSettingElement(
                            setting = SETTING_DEFAULT_JOURNALS_DATE,
                            selected = settingsStateHolder.settingDefaultJournalsDate.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingDefaultJournalsDate.value = selection
                                SETTING_DEFAULT_JOURNALS_DATE.saveSetting(selection, settingsStateHolder.prefs)
                            }
                        )
                        SwitchSettingElement(
                            setting = SETTING_JOURNALS_SET_DEFAULT_CURRENT_LOCATION,
                            checked = settingsStateHolder.settingSetDefaultCurrentLocationJournals,
                            onCheckedChanged = { checked ->
                                settingsStateHolder.settingSetDefaultCurrentLocationJournals.value = checked
                                SETTING_JOURNALS_SET_DEFAULT_CURRENT_LOCATION.saveSetting(checked, settingsStateHolder.prefs)
                                if(checked && locationPermissionState?.permissions?.all { it.status.shouldShowRationale } == false && locationPermissionState.permissions.none { it.status.isGranted })
                                    locationPermissionState.launchMultiplePermissionRequest()
                            })
                    }

                    ExpandableSettingsSection(
                        headerText = R.string.settings_notes,
                        expanded = expandedSection == SettingsScreenSection.NOTES_SETTINGS,
                        onToggle = { expandOrCollapse(SettingsScreenSection.NOTES_SETTINGS) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SwitchSettingElement(
                            setting = SETTING_NOTES_SET_DEFAULT_CURRENT_LOCATION,
                            checked = settingsStateHolder.settingSetDefaultCurrentLocationNotes,
                            onCheckedChanged = { checked ->
                                settingsStateHolder.settingSetDefaultCurrentLocationNotes.value = checked
                                SETTING_NOTES_SET_DEFAULT_CURRENT_LOCATION.saveSetting(checked, settingsStateHolder.prefs)
                                if(checked && locationPermissionState?.permissions?.all { it.status.shouldShowRationale } == false && locationPermissionState.permissions.none { it.status.isGranted })
                                    locationPermissionState.launchMultiplePermissionRequest()
                            })
                    }

                    ExpandableSettingsSection(
                        headerText = R.string.settings_tasks,
                        expanded = expandedSection == SettingsScreenSection.TASKS_SETTINGS,
                        onToggle = { expandOrCollapse(SettingsScreenSection.TASKS_SETTINGS) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DropdownSettingElement(
                            setting = SETTING_DEFAULT_START_DATE,
                            selected = settingsStateHolder.settingDefaultStartDate.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingDefaultStartDate.value = selection
                                SETTING_DEFAULT_START_DATE.saveSetting(selection, settingsStateHolder.prefs)
                            }
                        )
                        AnimatedVisibility(settingsStateHolder.settingDefaultStartDate.value != DropdownSettingOption.DEFAULT_DATE_NONE) {
                            TimeSettingElement(
                                setting = TimeSetting.SETTING_DEFAULT_START_TIME,
                                dialogTitleRes = TimeSetting.SETTING_DEFAULT_START_TIME.title,
                                selectedTime = settingsStateHolder.settingDefaultStartTime.value,
                                onSelectionChanged = { selectedTime ->
                                    settingsStateHolder.settingDefaultStartTime.value = selectedTime
                                    TimeSetting.SETTING_DEFAULT_START_TIME.saveSetting(selectedTime, settingsStateHolder.prefs)
                                    if(settingsStateHolder.settingDefaultDueDate.value != DropdownSettingOption.DEFAULT_DATE_NONE) {
                                        if (selectedTime == null) {
                                            settingsStateHolder.settingDefaultDueTime.value = null
                                            TimeSetting.SETTING_DEFAULT_DUE_TIME.saveSetting(null, settingsStateHolder.prefs)
                                        } else if(settingsStateHolder.settingDefaultDueTime.value == null) {
                                            settingsStateHolder.settingDefaultDueTime.value = selectedTime
                                            TimeSetting.SETTING_DEFAULT_DUE_TIME.saveSetting(selectedTime, settingsStateHolder.prefs)
                                        }
                                    }
                                }
                            )
                        }
                        AnimatedVisibility(settingsStateHolder.settingDefaultStartDate.value != DropdownSettingOption.DEFAULT_DATE_NONE && settingsStateHolder.settingDefaultStartTime.value != null) {
                            DropdownSettingTimezoneElement(
                                setting = DropdownSettingTimezone.SETTING_DEFAULT_START_TIMEZONE,
                                selected = settingsStateHolder.settingDefaultStartTimezone.value,
                                onSelectionChanged = { selection ->
                                    settingsStateHolder.settingDefaultStartTimezone.value = selection
                                    DropdownSettingTimezone.SETTING_DEFAULT_START_TIMEZONE.saveSetting(selection, settingsStateHolder.prefs)
                                }
                            )
                        }

                        DropdownSettingElement(
                            setting = SETTING_DEFAULT_DUE_DATE,
                            selected = settingsStateHolder.settingDefaultDueDate.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingDefaultDueDate.value = selection
                                SETTING_DEFAULT_DUE_DATE.saveSetting(selection, settingsStateHolder.prefs)
                            }
                        )
                        AnimatedVisibility(settingsStateHolder.settingDefaultDueDate.value != DropdownSettingOption.DEFAULT_DATE_NONE) {
                            TimeSettingElement(
                                setting = TimeSetting.SETTING_DEFAULT_DUE_TIME,
                                dialogTitleRes = TimeSetting.SETTING_DEFAULT_DUE_TIME.title,
                                selectedTime = settingsStateHolder.settingDefaultDueTime.value,
                                onSelectionChanged = { selectedTime ->
                                    settingsStateHolder.settingDefaultDueTime.value = selectedTime
                                    TimeSetting.SETTING_DEFAULT_DUE_TIME.saveSetting(selectedTime, settingsStateHolder.prefs)
                                    if(settingsStateHolder.settingDefaultStartDate.value != DropdownSettingOption.DEFAULT_DATE_NONE) {
                                        if (selectedTime == null) {
                                            settingsStateHolder.settingDefaultStartTime.value = null
                                            TimeSetting.SETTING_DEFAULT_START_TIME.saveSetting(null, settingsStateHolder.prefs)
                                        } else if(settingsStateHolder.settingDefaultStartTime.value == null) {
                                            settingsStateHolder.settingDefaultStartTime.value = selectedTime
                                            TimeSetting.SETTING_DEFAULT_START_TIME.saveSetting(selectedTime, settingsStateHolder.prefs)
                                        }
                                    }
                                }
                            )
                        }

                        AnimatedVisibility(settingsStateHolder.settingDefaultDueDate.value != DropdownSettingOption.DEFAULT_DATE_NONE && settingsStateHolder.settingDefaultDueTime.value != null) {
                            DropdownSettingTimezoneElement(
                                setting = DropdownSettingTimezone.SETTING_DEFAULT_DUE_TIMEZONE,
                                selected = settingsStateHolder.settingDefaultDueTimezone.value,
                                onSelectionChanged = { selection ->
                                    settingsStateHolder.settingDefaultDueTimezone.value = selection
                                    DropdownSettingTimezone.SETTING_DEFAULT_DUE_TIMEZONE.saveSetting(selection, settingsStateHolder.prefs)
                                }
                            )
                        }

                        SwitchSettingElement(
                            setting = SETTING_TASKS_SET_DEFAULT_CURRENT_LOCATION,
                            checked = settingsStateHolder.settingSetDefaultCurrentLocationTasks,
                            onCheckedChanged = { checked ->
                                settingsStateHolder.settingSetDefaultCurrentLocationTasks.value = checked
                                SETTING_TASKS_SET_DEFAULT_CURRENT_LOCATION.saveSetting(checked, settingsStateHolder.prefs)
                                if(checked && locationPermissionState?.permissions?.all { it.status.shouldShowRationale } == false && locationPermissionState.permissions.none { it.status.isGranted })
                                    locationPermissionState.launchMultiplePermissionRequest()
                            }
                        )
                    }


                    ExpandableSettingsSection(
                        headerText = R.string.settings_tasks_status_progress,
                        expanded = expandedSection == SettingsScreenSection.TASKS_SETTINGS_STATUS,
                        onToggle = { expandOrCollapse(SettingsScreenSection.TASKS_SETTINGS_STATUS) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SwitchSettingElement(
                            setting = SETTING_SHOW_PROGRESS_FOR_MAINTASKS,
                            checked = settingsStateHolder.settingShowProgressForMainTasks,
                            onCheckedChanged = {
                                settingsStateHolder.settingShowProgressForMainTasks.value = it
                                SETTING_SHOW_PROGRESS_FOR_MAINTASKS.saveSetting(it, settingsStateHolder.prefs)
                            }
                        )
                        SwitchSettingElement(
                            setting = SETTING_SHOW_PROGRESS_FOR_SUBTASKS,
                            checked = settingsStateHolder.settingShowProgressForSubTasks,
                            onCheckedChanged = {
                                settingsStateHolder.settingShowProgressForSubTasks.value = it
                                SETTING_SHOW_PROGRESS_FOR_SUBTASKS.saveSetting(it, settingsStateHolder.prefs)
                            }
                        )
                        DropdownSettingElement(
                            setting = SETTING_PROGRESS_STEP,
                            selected = settingsStateHolder.settingStepForProgress.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingStepForProgress.value = selection
                                SETTING_PROGRESS_STEP.saveSetting(selection, settingsStateHolder.prefs)
                            }
                        )
                        SwitchSettingElement(
                            setting = SETTING_LINK_PROGRESS_TO_SUBTASKS,
                            checked = settingsStateHolder.settingLinkProgressToSubtasks,
                            onCheckedChanged = {
                                settingsStateHolder.settingLinkProgressToSubtasks.value = it
                                SETTING_LINK_PROGRESS_TO_SUBTASKS.saveSetting(it, settingsStateHolder.prefs)
                            }
                        )
                        SwitchSettingElement(
                            setting = SETTING_KEEP_STATUS_PROGRESS_COMPLETED_IN_SYNC,
                            checked = settingsStateHolder.settingKeepStatusProgressCompletedInSync,
                            onCheckedChanged = {
                                settingsStateHolder.settingKeepStatusProgressCompletedInSync.value = it
                                SETTING_KEEP_STATUS_PROGRESS_COMPLETED_IN_SYNC.saveSetting(it, settingsStateHolder.prefs)
                            }
                        )
                    }

                    ExpandableSettingsSection(
                        headerText = R.string.settings_alarms,
                        expanded = expandedSection == SettingsScreenSection.ALARMS_SETTINGS,
                        onToggle = { expandOrCollapse(SettingsScreenSection.ALARMS_SETTINGS) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SwitchSettingElement(
                            setting = SETTING_DISABLE_ALARMS_FOR_READONLY,
                            checked = settingsStateHolder.settingDisableAlarmsReadonly,
                            onCheckedChanged = {
                                settingsStateHolder.settingDisableAlarmsReadonly.value = it
                                SETTING_DISABLE_ALARMS_FOR_READONLY.saveSetting(it, settingsStateHolder.prefs)
                            }
                        )
                        DropdownSettingElement(
                            setting = SETTING_AUTO_ALARM,
                            selected = settingsStateHolder.settingAutoAlarm.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingAutoAlarm.value = selection
                                SETTING_AUTO_ALARM.saveSetting(selection, settingsStateHolder.prefs)
                                scope.launch(Dispatchers.IO) { NotificationPublisher.scheduleNextNotifications(context) }
                            }
                        )
                        SwitchSettingElement(
                            setting = SETTING_STICKY_ALARMS,
                            checked = settingsStateHolder.settingStickyAlarms,
                            onCheckedChanged = {
                                settingsStateHolder.settingStickyAlarms.value = it
                                SETTING_STICKY_ALARMS.saveSetting(it, settingsStateHolder.prefs)
                            }
                        )
                        SwitchSettingElement(
                            setting = SETTING_FULLSCREEN_ALARMS,
                            checked = settingsStateHolder.settingFullscreenAlarms,
                            onCheckedChanged = {
                                if(it && !NotificationManagerCompat.from(context).canUseFullScreenIntent()) {
                                    Toast.makeText(context, R.string.settings_fullscreen_alarms_toast, Toast.LENGTH_LONG).show()
                                    val intent = Intent().apply {
                                        when {
                                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                                                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                                //putExtra(Settings.EXTRA_CHANNEL_ID, MainActivity2.NOTIFICATION_CHANNEL_ALARMS)
                                            }
                                            else -> {
                                                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                                                putExtra("app_package", context.packageName)
                                                putExtra("app_uid", context.applicationInfo.uid)
                                            }
                                        }
                                    }
                                    settingsStateHolder.settingFullscreenAlarms.value = false
                                    SETTING_FULLSCREEN_ALARMS.saveSetting(false, settingsStateHolder.prefs)
                                    context.startActivity(intent)
                                } else {
                                    settingsStateHolder.settingFullscreenAlarms.value = it
                                    SETTING_FULLSCREEN_ALARMS.saveSetting(it, settingsStateHolder.prefs)
                                    scope.launch(Dispatchers.IO) {
                                        NotificationPublisher.scheduleNextNotifications(context)
                                    }
                                }
                            }
                        )
                    }

                    ExpandableSettingsSection(
                        headerText = R.string.settings_sync,
                        expanded = expandedSection == SettingsScreenSection.SYNC_SETTINGS,
                        onToggle = { expandOrCollapse(SettingsScreenSection.SYNC_SETTINGS) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SwitchSettingElement(
                            setting = SETTING_SYNC_ON_START,
                            checked = settingsStateHolder.settingSyncOnStart,
                            onCheckedChanged = {
                                settingsStateHolder.settingSyncOnStart.value = it
                                SETTING_SYNC_ON_START.saveSetting(it, settingsStateHolder.prefs)
                            }
                        )
                        SwitchSettingElement(
                            setting = SETTING_SYNC_ON_PULL_REFRESH,
                            checked = settingsStateHolder.settingSyncOnPullRefresh,
                            onCheckedChanged = {
                                settingsStateHolder.settingSyncOnPullRefresh.value = it
                                SETTING_SYNC_ON_PULL_REFRESH.saveSetting(it, settingsStateHolder.prefs)
                            }
                        )
                    }
                }
            },
            navController = navController,
            paddingValues = paddingValues
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreen_Preview() {
    MaterialTheme {

        SettingsScreen(
            rememberNavController(),
            settingsStateHolder = SettingsStateHolder(LocalContext.current),
            globalStateHolder = GlobalStateHolder(LocalContext.current)
        )
    }
}