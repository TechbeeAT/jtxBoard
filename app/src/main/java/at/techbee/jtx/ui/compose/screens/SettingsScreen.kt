/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.R
import at.techbee.jtx.settings.DropdownSettingOption
import at.techbee.jtx.ui.compose.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.compose.appbars.JtxTopAppBar
import at.techbee.jtx.ui.compose.elements.DropdownSetting
import at.techbee.jtx.ui.compose.elements.SwitchSetting
import at.techbee.jtx.settings.SwitchSetting.*
import at.techbee.jtx.settings.DropdownSetting.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    currentTheme: MutableState<DropdownSettingOption>,
    currentAudioFormat: MutableState<DropdownSettingOption>,
    autoExpandSubtasks: MutableState<Boolean>,
    autoExpandSubnotes: MutableState<Boolean>,
    autoExpandAttachments: MutableState<Boolean>,
    showProgressForMainTasks: MutableState<Boolean>,
    showProgressForSubTasks: MutableState<Boolean>,
    showSubtasksInTasklist: MutableState<Boolean>,
    showSubnotesInNoteslist: MutableState<Boolean>,
    showSubjournalsInJournallist: MutableState<Boolean>,
    currentDefaultStartDate: MutableState<DropdownSettingOption>,
    currentDefaultDueDate: MutableState<DropdownSettingOption>,
    modifier: Modifier = Modifier
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val subSectionExpanded = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_settings)
            )
        },

        ) {
        Column {
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
                            preselected = currentTheme.value,
                            onSelectionChanged = { selection ->
                                currentTheme.value = selection
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

                        DropdownSetting(
                            setting = SETTING_AUDIO_FORMAT,
                            preselected = currentAudioFormat.value,
                            onSelectionChanged = { selection ->
                                currentAudioFormat.value = selection
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
                            setting = SETTING_AUTO_EXPAND_SUBTASKS,
                            initiallyChecked = autoExpandSubtasks.value,
                            onCheckedChanged = {
                                autoExpandSubtasks.value = it
                                SETTING_AUTO_EXPAND_SUBTASKS.save(it, context)
                            })
                        SwitchSetting(
                            setting = SETTING_AUTO_EXPAND_SUBNOTES,
                            initiallyChecked = autoExpandSubnotes.value,
                            onCheckedChanged = {
                                autoExpandSubnotes.value = it
                                SETTING_AUTO_EXPAND_SUBNOTES.save(it, context)
                            })
                        SwitchSetting(
                            setting = SETTING_AUTO_EXPAND_ATTACHMENTS,
                            initiallyChecked = autoExpandAttachments.value,
                            onCheckedChanged = {
                                autoExpandAttachments.value = it
                                SETTING_AUTO_EXPAND_ATTACHMENTS.save(it, context)
                            })
                        SwitchSetting(
                            setting = SETTING_SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST,
                            initiallyChecked = showProgressForMainTasks.value,
                            onCheckedChanged = {
                                showProgressForMainTasks.value = it
                                SETTING_SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST.save(it, context)
                            })

                        Divider(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .alpha(0.5f)
                        )
                        Text(
                            text = stringResource(id = R.string.settings_group_settings_for_subentries),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                        )

                        AnimatedVisibility(subSectionExpanded.value.not()) {
                            TextButton(onClick = { subSectionExpanded.value = true }) {
                                Text(stringResource(id = R.string.list_expand))
                            }
                        }

                        AnimatedVisibility(subSectionExpanded.value) {
                            Column {
                                SwitchSetting(
                                    setting = SETTING_SHOW_PROGRESS_FOR_SUBTASKS,
                                    initiallyChecked = showProgressForSubTasks.value,
                                    onCheckedChanged = {
                                        showProgressForSubTasks.value = it
                                        SETTING_SHOW_PROGRESS_FOR_SUBTASKS.save(it, context)
                                    })
                                SwitchSetting(
                                    setting = SETTING_SHOW_SUBTASKS_IN_TASKLIST,
                                    initiallyChecked = showSubtasksInTasklist.value,
                                    onCheckedChanged = {
                                        showSubtasksInTasklist.value = it
                                        SETTING_SHOW_SUBTASKS_IN_TASKLIST.save(it, context)
                                    })
                                SwitchSetting(
                                    setting = SETTING_SHOW_SUBNOTES_IN_NOTESLIST,
                                    initiallyChecked = showSubnotesInNoteslist.value,
                                    onCheckedChanged = {
                                        showSubnotesInNoteslist.value = it
                                        SETTING_SHOW_SUBNOTES_IN_NOTESLIST.save(it, context)
                                    })
                                SwitchSetting(
                                    setting = SETTING_SHOW_SUBJOURNALS_IN_JOURNALLIST,
                                    initiallyChecked = showSubjournalsInJournallist.value,
                                    onCheckedChanged = {
                                        showSubjournalsInJournallist.value = it
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
                            text = stringResource(id = R.string.settings_tasks),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                        )

                        DropdownSetting(
                            setting = SETTING_DEFAULT_START_DATE,
                            preselected = currentDefaultStartDate.value,
                            onSelectionChanged = { selection ->
                                currentAudioFormat.value = selection
                                SETTING_DEFAULT_START_DATE.save(selection, context = context)
                            }
                        )
                        DropdownSetting(
                            setting = SETTING_DEFAULT_DUE_DATE,
                            preselected = currentDefaultDueDate.value,
                            onSelectionChanged = { selection ->
                                currentAudioFormat.value = selection
                                SETTING_DEFAULT_DUE_DATE.save(selection, context = context)
                            }
                        )
                    }
                },
                navController = navController
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreen_Preview() {
    MaterialTheme {

        SettingsScreen(
            rememberNavController(),
            currentTheme = mutableStateOf(SETTING_THEME.options.last()),
            currentAudioFormat = mutableStateOf(SETTING_AUDIO_FORMAT.options.first()),
            autoExpandSubtasks = mutableStateOf(false),
            autoExpandSubnotes = mutableStateOf(false),
            autoExpandAttachments = mutableStateOf(false),
            showProgressForMainTasks = mutableStateOf(true),
            showProgressForSubTasks = mutableStateOf(true),
            showSubtasksInTasklist = mutableStateOf(false),
            showSubnotesInNoteslist = mutableStateOf(false),
            showSubjournalsInJournallist = mutableStateOf(false),
            currentDefaultStartDate = mutableStateOf(SETTING_DEFAULT_START_DATE.options.first()),
            currentDefaultDueDate = mutableStateOf(SETTING_DEFAULT_DUE_DATE.options.first()),
        )
    }
}