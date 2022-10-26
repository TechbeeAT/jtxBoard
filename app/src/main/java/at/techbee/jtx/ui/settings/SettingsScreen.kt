/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.R
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.elements.DropdownSetting
import at.techbee.jtx.ui.reusable.elements.SwitchSetting
import at.techbee.jtx.ui.settings.DropdownSetting.*
import at.techbee.jtx.ui.settings.SwitchSetting.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsStateHolder: SettingsStateHolder,
    modifier: Modifier = Modifier
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val subSectionExpanded = remember { mutableStateOf(false) }

    val languageOptions = mutableListOf<Locale?>(null)
    for (language in BuildConfig.TRANSLATION_ARRAY) {
        languageOptions.add(Locale.forLanguageTag(language))
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

                    Text(
                        text = stringResource(id = R.string.settings_app),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    DropdownSetting(
                        setting = SETTING_THEME,
                        preselected = settingsStateHolder.settingTheme.value,
                        onSelectionChanged = { selection ->
                            settingsStateHolder.settingTheme.value = selection
                            SETTING_THEME.save(selection, context = context)
                            when (selection) {
                                DropdownSettingOption.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_YES
                                )
                                DropdownSettingOption.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_NO
                                )
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
                            if (languageOptions.contains(appCompatLocales[i]!!)) {
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
                                text = selectedLanguage?.displayLanguage ?: stringResource(id = R.string.settings_select_language_system),
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
                            languageOptions.forEach { locale ->
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
                                            text = locale?.displayLanguage ?: stringResource(id = R.string.settings_select_language_system),
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
                        preselected = settingsStateHolder.settingAudioFormat.value,
                        onSelectionChanged = { selection ->
                            settingsStateHolder.settingAudioFormat.value = selection
                            SETTING_AUDIO_FORMAT.save(selection, context = context)
                        }
                    )

                    Divider(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .alpha(0.5f)
                    )
                    Text(
                        text = stringResource(id = R.string.settings_list),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                    )

                    SwitchSetting(
                        setting = SETTING_SHOW_ONE_RECUR_ENTRY_IN_FUTURE,
                        initiallyChecked = settingsStateHolder.settingShowOneRecurEntryInFuture.value,
                        onCheckedChanged = {
                            settingsStateHolder.settingShowOneRecurEntryInFuture.value = it
                            SETTING_SHOW_ONE_RECUR_ENTRY_IN_FUTURE.save(it, context)
                        })

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
                    SwitchSetting(
                        setting = SETTING_SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST,
                        initiallyChecked = settingsStateHolder.settingShowProgressForMainTasks.value,
                        onCheckedChanged = {
                            settingsStateHolder.settingShowProgressForMainTasks.value = it
                            SETTING_SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST.save(it, context)
                        })


                    AnimatedVisibility(subSectionExpanded.value.not()) {
                        TextButton(onClick = { subSectionExpanded.value = true }) {
                            Text(stringResource(id = R.string.list_expand))
                        }
                    }

                    AnimatedVisibility(subSectionExpanded.value) {
                        Column {
                            SwitchSetting(
                                setting = SETTING_SHOW_PROGRESS_FOR_SUBTASKS,
                                initiallyChecked = settingsStateHolder.settingShowProgressForSubTasks.value,
                                onCheckedChanged = {
                                    settingsStateHolder.settingShowProgressForSubTasks.value = it
                                    SETTING_SHOW_PROGRESS_FOR_SUBTASKS.save(it, context)
                                })
                            SwitchSetting(
                                setting = SETTING_SHOW_SUBTASKS_IN_TASKLIST,
                                initiallyChecked = settingsStateHolder.settingShowSubtasksInTasklist.value,
                                onCheckedChanged = {
                                    settingsStateHolder.settingShowSubtasksInTasklist.value = it
                                    SETTING_SHOW_SUBTASKS_IN_TASKLIST.save(it, context)
                                })
                            SwitchSetting(
                                setting = SETTING_SHOW_SUBNOTES_IN_NOTESLIST,
                                initiallyChecked = settingsStateHolder.settingShowSubnotesInNoteslist.value,
                                onCheckedChanged = {
                                    settingsStateHolder.settingShowSubnotesInNoteslist.value = it
                                    SETTING_SHOW_SUBNOTES_IN_NOTESLIST.save(it, context)
                                })
                            SwitchSetting(
                                setting = SETTING_SHOW_SUBJOURNALS_IN_JOURNALLIST,
                                initiallyChecked = settingsStateHolder.settingShowSubjournalsInJournallist.value,
                                onCheckedChanged = {
                                    settingsStateHolder.settingShowSubjournalsInJournallist.value = it
                                    SETTING_SHOW_SUBJOURNALS_IN_JOURNALLIST.save(it, context)
                                })
                        }

                    }



                    Divider(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .alpha(0.5f)
                    )
                    Text(
                        text = stringResource(id = R.string.settings_details),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                    )

                    SwitchSetting(
                        setting = SETTING_DETAILS_AUTOSAVE,
                        initiallyChecked = settingsStateHolder.settingAutosave.value,
                        onCheckedChanged = {
                            settingsStateHolder.settingAutosave.value = it
                            SETTING_DETAILS_AUTOSAVE.save(it, context)
                        })

                    SwitchSetting(
                        setting = SETTING_DETAILS_ENABLE_MARKDOWN,
                        initiallyChecked = settingsStateHolder.settingEnableMarkdownFormattting.value,
                        onCheckedChanged = {
                            settingsStateHolder.settingEnableMarkdownFormattting.value = it
                            SETTING_DETAILS_ENABLE_MARKDOWN.save(it, context)
                        })

                    Divider(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .alpha(0.5f)
                    )
                    Text(
                        text = stringResource(id = R.string.settings_tasks),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                    )

                    DropdownSetting(
                        setting = SETTING_DEFAULT_START_DATE,
                        preselected = settingsStateHolder.settingDefaultStartDate.value,
                        onSelectionChanged = { selection ->
                            settingsStateHolder.settingDefaultStartDate.value = selection
                            SETTING_DEFAULT_START_DATE.save(selection, context = context)
                        }
                    )
                    DropdownSetting(
                        setting = SETTING_DEFAULT_DUE_DATE,
                        preselected = settingsStateHolder.settingDefaultDueDate.value,
                        onSelectionChanged = { selection ->
                            settingsStateHolder.settingDefaultDueDate.value = selection
                            SETTING_DEFAULT_DUE_DATE.save(selection, context = context)
                        }
                    )
                    DropdownSetting(
                        setting = SETTING_PROGRESS_STEP,
                        preselected = settingsStateHolder.settingStepForProgress.value,
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
                        preselected = settingsStateHolder.settingAutoAlarm.value,
                        onSelectionChanged = { selection ->
                            settingsStateHolder.settingAutoAlarm.value = selection
                            SETTING_AUTO_ALARM.save(selection, context = context)
                            scope.launch(Dispatchers.IO) { Alarm.scheduleNextNotifications(context) }
                        }
                    )
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
            settingsStateHolder = SettingsStateHolder(LocalContext.current)
        )
    }
}