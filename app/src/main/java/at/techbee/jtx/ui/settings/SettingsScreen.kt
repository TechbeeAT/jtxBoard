/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.NotificationPublisher
import at.techbee.jtx.R
import at.techbee.jtx.ui.GlobalStateHolder
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.elements.DropdownSetting
import at.techbee.jtx.ui.reusable.elements.SwitchSetting
import at.techbee.jtx.ui.settings.DropdownSetting.*
import at.techbee.jtx.ui.settings.SwitchSetting.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


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
        SETTING_PROTECT_BIOMETRIC.save(pendingSettingProtectiometric!!, context = context)
        pendingSettingProtectiometric = null
    }

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
                        expandedDefault = true, 
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        
                        DropdownSetting(
                            setting = SETTING_THEME,
                            selected = settingsStateHolder.settingTheme.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingTheme.value = selection
                                SETTING_THEME.save(selection, context = context)
                                when (selection) {
                                    DropdownSettingOption.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                    DropdownSettingOption.THEME_TRUE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                    DropdownSettingOption.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                                }
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

                        DropdownSetting(
                            setting = SETTING_AUDIO_FORMAT,
                            selected = settingsStateHolder.settingAudioFormat.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingAudioFormat.value = selection
                                SETTING_AUDIO_FORMAT.save(selection, context = context)
                            }
                        )

                        if(globalStateHolder.biometricStatus == BiometricManager.BIOMETRIC_SUCCESS) {
                            DropdownSetting(
                                setting = SETTING_PROTECT_BIOMETRIC,
                                selected = settingsStateHolder.settingProtectBiometric.value,
                                onSelectionChanged = { selection ->
                                    if(settingsStateHolder.settingProtectBiometric.value == selection)
                                        return@DropdownSetting

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
                                        SETTING_PROTECT_BIOMETRIC.save(selection, context = context)
                                    }
                                }
                            )
                        }
                    }

                    ExpandableSettingsSection(
                        headerText = R.string.settings_modules,
                        expandedDefault = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {


                        SwitchSetting(
                            setting = SETTING_ENABLE_JOURNALS,
                            initiallyChecked = settingsStateHolder.settingEnableJournals.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingEnableJournals.value = it
                                SETTING_ENABLE_JOURNALS.save(it, context)
                            },
                            enabled = !(!settingsStateHolder.settingEnableNotes.value && !settingsStateHolder.settingEnableTasks.value)
                        )
                        SwitchSetting(
                            setting = SETTING_ENABLE_NOTES,
                            initiallyChecked = settingsStateHolder.settingEnableNotes.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingEnableNotes.value = it
                                SETTING_ENABLE_NOTES.save(it, context)
                            },
                            enabled = !(!settingsStateHolder.settingEnableJournals.value && !settingsStateHolder.settingEnableTasks.value)
                        )
                        SwitchSetting(
                            setting = SETTING_ENABLE_TASKS,
                            initiallyChecked = settingsStateHolder.settingEnableTasks.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingEnableTasks.value = it
                                SETTING_ENABLE_TASKS.save(it, context)
                            },
                            enabled = !(!settingsStateHolder.settingEnableJournals.value && !settingsStateHolder.settingEnableNotes.value)
                        )
                    }

                    ExpandableSettingsSection(
                        headerText = R.string.settings_list,
                        expandedDefault = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SwitchSetting(
                            setting = SETTING_AUTO_EXPAND_SUBTASKS,
                            initiallyChecked = settingsStateHolder.settingAutoExpandSubtasks.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingAutoExpandSubtasks.value = it
                                SETTING_AUTO_EXPAND_SUBTASKS.save(it, context)
                            })
                        SwitchSetting(
                            setting = SETTING_AUTO_EXPAND_SUBNOTES,
                            initiallyChecked = settingsStateHolder.settingAutoExpandSubnotes.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingAutoExpandSubnotes.value = it
                                SETTING_AUTO_EXPAND_SUBNOTES.save(it, context)
                            })
                        SwitchSetting(
                            setting = SETTING_AUTO_EXPAND_ATTACHMENTS,
                            initiallyChecked = settingsStateHolder.settingAutoExpandAttachments.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingAutoExpandAttachments.value = it
                                SETTING_AUTO_EXPAND_ATTACHMENTS.save(it, context)
                            })
                    }

                    ExpandableSettingsSection(
                        headerText = R.string.settings_journals,
                        expandedDefault = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DropdownSetting(
                            setting = SETTING_DEFAULT_JOURNALS_DATE,
                            selected = settingsStateHolder.settingDefaultJournalsDate.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingDefaultJournalsDate.value = selection
                                SETTING_DEFAULT_JOURNALS_DATE.save(selection, context = context)
                            }
                        )
                        SwitchSetting(
                            setting = SETTING_JOURNALS_SET_DEFAULT_CURRENT_LOCATION,
                            initiallyChecked = settingsStateHolder.settingSetDefaultCurrentLocationJournals.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingSetDefaultCurrentLocationJournals.value = it
                                SETTING_JOURNALS_SET_DEFAULT_CURRENT_LOCATION.save(it, context)
                            })
                    }

                    ExpandableSettingsSection(
                        headerText = R.string.settings_notes,
                        expandedDefault = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SwitchSetting(
                            setting = SETTING_NOTES_SET_DEFAULT_CURRENT_LOCATION,
                            initiallyChecked = settingsStateHolder.settingSetDefaultCurrentLocationNotes.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingSetDefaultCurrentLocationNotes.value = it
                                SETTING_NOTES_SET_DEFAULT_CURRENT_LOCATION.save(it, context)
                            })
                    }

                    ExpandableSettingsSection(
                        headerText = R.string.settings_tasks,
                        expandedDefault = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SwitchSetting(
                            setting = SETTING_SHOW_PROGRESS_FOR_MAINTASKS,
                            initiallyChecked = settingsStateHolder.settingShowProgressForMainTasks.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingShowProgressForMainTasks.value = it
                                SETTING_SHOW_PROGRESS_FOR_MAINTASKS.save(it, context)
                            })
                        SwitchSetting(
                            setting = SETTING_SHOW_PROGRESS_FOR_SUBTASKS,
                            initiallyChecked = settingsStateHolder.settingShowProgressForSubTasks.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingShowProgressForSubTasks.value = it
                                SETTING_SHOW_PROGRESS_FOR_SUBTASKS.save(it, context)
                            })

                        DropdownSetting(
                            setting = SETTING_DEFAULT_START_DATE,
                            selected = settingsStateHolder.settingDefaultStartDate.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingDefaultStartDate.value = selection
                                SETTING_DEFAULT_START_DATE.save(selection, context = context)
                            }
                        )
                        DropdownSetting(
                            setting = SETTING_DEFAULT_DUE_DATE,
                            selected = settingsStateHolder.settingDefaultDueDate.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingDefaultDueDate.value = selection
                                SETTING_DEFAULT_DUE_DATE.save(selection, context = context)
                            }
                        )
                        DropdownSetting(
                            setting = SETTING_PROGRESS_STEP,
                            selected = settingsStateHolder.settingStepForProgress.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingStepForProgress.value = selection
                                SETTING_PROGRESS_STEP.save(selection, context = context)
                            }
                        )
                        SwitchSetting(
                            setting = SETTING_DISABLE_ALARMS_FOR_READONLY,
                            initiallyChecked = settingsStateHolder.settingDisableAlarmsReadonly.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingDisableAlarmsReadonly.value = it
                                SETTING_DISABLE_ALARMS_FOR_READONLY.save(it, context)
                            }
                        )
                        DropdownSetting(
                            setting = SETTING_AUTO_ALARM,
                            selected = settingsStateHolder.settingAutoAlarm.value,
                            onSelectionChanged = { selection ->
                                settingsStateHolder.settingAutoAlarm.value = selection
                                SETTING_AUTO_ALARM.save(selection, context = context)
                                scope.launch(Dispatchers.IO) { NotificationPublisher.scheduleNextNotifications(context) }
                            }
                        )
                        SwitchSetting(
                            setting = SETTING_LINK_PROGRESS_TO_SUBTASKS,
                            initiallyChecked = settingsStateHolder.settingLinkProgressToSubtasks.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingLinkProgressToSubtasks.value = it
                                SETTING_LINK_PROGRESS_TO_SUBTASKS.save(it, context)
                            }
                        )
                        SwitchSetting(
                            setting = SETTING_KEEP_STATUS_PROGRESS_COMPLETED_IN_SYNC,
                            initiallyChecked = settingsStateHolder.settingKeepStatusProgressCompletedInSync.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingKeepStatusProgressCompletedInSync.value = it
                                SETTING_KEEP_STATUS_PROGRESS_COMPLETED_IN_SYNC.save(it, context)
                            }
                        )
                        SwitchSetting(
                            setting = SETTING_TASKS_SET_DEFAULT_CURRENT_LOCATION,
                            initiallyChecked = settingsStateHolder.settingSetDefaultCurrentLocationTasks.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingSetDefaultCurrentLocationTasks.value = it
                                SETTING_TASKS_SET_DEFAULT_CURRENT_LOCATION.save(it, context)
                            }
                        )
                        SwitchSetting(
                            setting = SETTING_STICKY_ALARMS,
                            initiallyChecked = settingsStateHolder.settingStickyAlarms.value,
                            onCheckedChanged = {
                                settingsStateHolder.settingStickyAlarms.value = it
                                SETTING_STICKY_ALARMS.save(it, context)
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