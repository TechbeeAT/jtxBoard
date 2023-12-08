/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import kotlinx.coroutines.launch

enum class DetailOptionsBottomSheetTabs { VISIBILITY, ORDER }


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun DetailOptionsBottomSheet(
    module: Module,
    detailSettings: DetailSettings,
    onListSettingsChanged: () -> Unit,
    modifier: Modifier = Modifier,
    initialTab: DetailOptionsBottomSheetTabs = DetailOptionsBottomSheetTabs.VISIBILITY
) {

    val scope = rememberCoroutineScope()

    val detailOptionTabs = DetailOptionsBottomSheetTabs.entries
    val pagerState = rememberPagerState(initialPage = detailOptionTabs.indexOf(initialTab), pageCount = { detailOptionTabs.size })

    Column(
        modifier = modifier
    ) {

        SecondaryTabRow(selectedTabIndex = pagerState.currentPage) {
            detailOptionTabs.forEach { tab ->
                Tab(
                    selected = pagerState.currentPage == detailOptionTabs.indexOf(tab),
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(detailOptionTabs.indexOf(tab))
                        }
                    },
                    content = {
                        Text(
                            stringResource(id = when(tab) {
                                DetailOptionsBottomSheetTabs.VISIBILITY -> R.string.visibility
                                DetailOptionsBottomSheetTabs.ORDER -> R.string.order
                            } ),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier.height(50.dp)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { page ->
            when (detailOptionTabs[page]) {
                DetailOptionsBottomSheetTabs.VISIBILITY -> {
                    Column(modifier = modifier.verticalScroll(rememberScrollState())) {

                        Text(
                            text = stringResource(R.string.details_show_hide_elements),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )


                        FlowRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            detailSettings.detailSetting
                                .filter { it.key.group == DetailSettingsOptionGroup.ELEMENT && it.key.possibleFor.contains(module) }
                                .toSortedMap(compareBy { it.ordinal })
                                .forEach { (setting, enabled) ->

                                    FilterChip(
                                        selected = enabled,
                                        onClick = {
                                            detailSettings.detailSetting[setting] = !detailSettings.detailSetting.getOrDefault(setting, false)
                                            if (detailSettings.detailSetting[DetailSettingsOption.ENABLE_SUMMARY] == false && detailSettings.detailSetting[DetailSettingsOption.ENABLE_DESCRIPTION] == false)
                                                detailSettings.detailSetting[DetailSettingsOption.ENABLE_SUMMARY] = true

                                            onListSettingsChanged()
                                        },
                                        label = { Text(stringResource(id = setting.stringResource)) },
                                        trailingIcon = {
                                            Crossfade(enabled, label = "${setting.key}_enabed") {
                                                if (it)
                                                    Icon(Icons.Outlined.Visibility, stringResource(id = R.string.visible))
                                                else
                                                    Icon(Icons.Outlined.VisibilityOff, stringResource(id = R.string.invisible))
                                            }
                                        },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                        }
                    }
                }
                DetailOptionsBottomSheetTabs.ORDER -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = modifier.verticalScroll(rememberScrollState()).fillMaxWidth()
                    ) {

                        detailSettings.detailSettingOrder.forEach {

                            ElevatedAssistChip(
                                onClick = { },
                                enabled = when(it) {
                                              DetailsScreenSection.COLLECTION -> true
                                              DetailsScreenSection.DATES -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_STATUS] != false || detailSettings.detailSetting[DetailSettingsOption.ENABLE_CLASSIFICATION] != false || detailSettings.detailSetting[DetailSettingsOption.ENABLE_DTSTART] != false || detailSettings.detailSetting[DetailSettingsOption.ENABLE_STATUS] != false || detailSettings.detailSetting[DetailSettingsOption.ENABLE_CLASSIFICATION] != false || detailSettings.detailSetting[DetailSettingsOption.ENABLE_DUE] != false || detailSettings.detailSetting[DetailSettingsOption.ENABLE_STATUS] != false || detailSettings.detailSetting[DetailSettingsOption.ENABLE_CLASSIFICATION] != false || detailSettings.detailSetting[DetailSettingsOption.ENABLE_COMPLETED] != false
                                              DetailsScreenSection.SUMMARYDESCRIPTION -> true
                                              DetailsScreenSection.PROGRESS -> module == Module.TODO
                                              DetailsScreenSection.STATUSCLASSIFICATIONPRIORITY -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_STATUS] != false || detailSettings.detailSetting[DetailSettingsOption.ENABLE_CLASSIFICATION] != false || detailSettings.detailSetting[DetailSettingsOption.ENABLE_PRIORITY] != false
                                              DetailsScreenSection.CATEGORIES -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_CATEGORIES] != false
                                              DetailsScreenSection.PARENTS -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_PARENTS] != false
                                              DetailsScreenSection.SUBTASKS -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_SUBTASKS] != false
                                              DetailsScreenSection.SUBNOTES -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_SUBNOTES] != false
                                              DetailsScreenSection.RESOURCES -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_RESOURCES] != false
                                              DetailsScreenSection.ATTENDEES -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_ATTENDEES] != false
                                              DetailsScreenSection.CONTACT -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_CONTACT] != false
                                              DetailsScreenSection.URL -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_URL] != false
                                              DetailsScreenSection.LOCATION -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_LOCATION] != false
                                              DetailsScreenSection.COMMENTS -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_COMMENTS] != false
                                              DetailsScreenSection.ATTACHMENTS -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_ATTACHMENTS] != false
                                              DetailsScreenSection.ALARMS -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_ALARMS] != false
                                              DetailsScreenSection.RECURRENCE -> detailSettings.detailSetting[DetailSettingsOption.ENABLE_RECURRENCE] != false
                                },
                                label = { Text(stringResource(id = it.stringRes)) },
                                leadingIcon = {
                                    val index = detailSettings.detailSettingOrder.indexOf(it)
                                    Text((index+1).toString())
                                },
                                trailingIcon = {
                                              Row {
                                                  if(it != detailSettings.detailSettingOrder.firstOrNull()) {
                                                      IconButton(onClick = {
                                                          val index = detailSettings.detailSettingOrder.indexOf(it)
                                                          detailSettings.detailSettingOrder.remove(it)
                                                          detailSettings.detailSettingOrder.add(index-1, it)
                                                          onListSettingsChanged()
                                                      }) {
                                                          Icon(
                                                              Icons.Outlined.KeyboardArrowUp,
                                                              "Up"
                                                          )
                                                      }
                                                  } else { Box(modifier = Modifier.size(48.dp))}

                                                  if(it != detailSettings.detailSettingOrder.lastOrNull()) {
                                                      IconButton(onClick = {
                                                          val index = detailSettings.detailSettingOrder.indexOf(it)
                                                          detailSettings.detailSettingOrder.remove(it)
                                                          detailSettings.detailSettingOrder.add(index+1, it)
                                                          onListSettingsChanged()
                                                      }) {
                                                          Icon(
                                                              Icons.Outlined.KeyboardArrowDown,
                                                              "Down"
                                                          )
                                                      }
                                                  } else { Box(modifier = Modifier.size(48.dp))}

                                              }
                                },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailOptionsBottomSheet_Preview_TODO() {
    MaterialTheme {
        val detailSettings = DetailSettings()
        detailSettings.load(Module.TODO, LocalContext.current)

        DetailOptionsBottomSheet(
            module = Module.TODO,
            detailSettings = detailSettings,
            onListSettingsChanged = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailOptionsBottomSheet_Preview_JOURNAL() {
    MaterialTheme {
        val detailSettings = DetailSettings()
        detailSettings.load(Module.JOURNAL, LocalContext.current)

        DetailOptionsBottomSheet(
            module = Module.JOURNAL,
            detailSettings = detailSettings,
            onListSettingsChanged = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}



@Preview(showBackground = true)
@Composable
fun DetailOptionsBottomSheetVisibility_Preview_JOURNAL() {
    MaterialTheme {
        val detailSettings = DetailSettings()
        detailSettings.load(Module.JOURNAL, LocalContext.current)

        DetailOptionsBottomSheet(
            module = Module.JOURNAL,
            detailSettings = detailSettings,
            onListSettingsChanged = { },
            modifier = Modifier.fillMaxWidth(),
            initialTab = DetailOptionsBottomSheetTabs.ORDER
        )
    }
}
