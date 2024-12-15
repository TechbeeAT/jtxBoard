/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.list.CheckboxPosition
import at.techbee.jtx.ui.list.ListSettings
import at.techbee.jtx.ui.list.MAX_ITEMS_PER_SECTION
import at.techbee.jtx.ui.reusable.dialogs.ColorPickerDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.ui.theme.getContrastSurfaceColorFor


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListWidgetConfigGeneral(
    listSettings: ListSettings,
    selectedModule: MutableState<Module>,
    allCategoriesLive: LiveData<List<String>>,
    modifier: Modifier = Modifier
) {

    var showColorPickerBackground by rememberSaveable { mutableStateOf(false) }
    var showColorPickerEntryBackground by rememberSaveable { mutableStateOf(false) }
    val allCategories by allCategoriesLive.observeAsState(emptyList())

    val widgetColorCalculated = listSettings.widgetColor.value?.let { Color(it) } ?: MaterialTheme.colorScheme.primaryContainer
    val widgetColorEntriesCalculated = listSettings.widgetColorEntries.value?.let { Color(it) } ?: MaterialTheme.colorScheme.surface
    val currentSurfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val currentPrimaryContainerColor = MaterialTheme.colorScheme.primaryContainer.toArgb()


    if(showColorPickerBackground) {
        ColorPickerDialog(
            initialColorInt = listSettings.widgetColor.value,
            onColorChanged = { listSettings.widgetColor.value = it },
            onDismiss = { showColorPickerBackground = false }
        )
    }

    if(showColorPickerEntryBackground) {
        ColorPickerDialog(
            initialColorInt = listSettings.widgetColorEntries.value,
            onColorChanged = { listSettings.widgetColorEntries.value = it },
            onDismiss = { showColorPickerEntryBackground = false }
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = stringResource(R.string.widget_list_configuration_beta_info),
            style = MaterialTheme.typography.labelMedium,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            Module.entries.forEach { module ->
                FilterChip(
                    selected = module == selectedModule.value,
                    onClick = {
                        selectedModule.value = module
                        listSettings.reset()
                    },
                    label = {
                        Text(
                            stringResource(
                                id = when (module) {
                                    Module.JOURNAL -> R.string.list_tabitem_journals
                                    Module.NOTE -> R.string.list_tabitem_notes
                                    Module.TODO -> R.string.list_tabitem_todos
                                }
                            ),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }

        OutlinedTextField(
            value = listSettings.widgetHeader.value,
            onValueChange = { listSettings.widgetHeader.value = it },
            placeholder = {
                Text(text = when(selectedModule.value) {
                    Module.JOURNAL -> stringResource(id = R.string.list_tabitem_journals)
                    Module.NOTE -> stringResource(id = R.string.list_tabitem_notes)
                    Module.TODO -> stringResource(id = R.string.list_tabitem_todos)
                },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.5f)
                )
            },
            singleLine = true,
            maxLines = 1,
            colors = OutlinedTextFieldDefaults.colors(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
            trailingIcon = {
                AnimatedVisibility(listSettings.widgetHeader.value.isNotEmpty()) {
                    IconButton(onClick = { listSettings.widgetHeader.value = "" }) {
                        Icon(Icons.Outlined.Close, stringResource(id = R.string.delete))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        HeadlineWithIcon(
            icon = Icons.Outlined.Settings,
            iconDesc = stringResource(id = R.string.widget_list_view_settings),
            text = stringResource(id = R.string.widget_list_view_settings)

        )
        FlowRow(modifier = Modifier.fillMaxWidth()) {

            FilterChip(
                selected = listSettings.flatView.value,
                onClick = {
                    listSettings.flatView.value = !listSettings.flatView.value
                },
                label = { Text(stringResource(id = R.string.menu_list_flat_view)) },
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            AssistChip(
                onClick = {
                    listSettings.checkboxPosition.value = when(listSettings.checkboxPosition.value) {
                        CheckboxPosition.START -> CheckboxPosition.END
                        CheckboxPosition.END -> CheckboxPosition.OFF
                        CheckboxPosition.OFF -> CheckboxPosition.START
                    }
                },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (listSettings.checkboxPosition.value == CheckboxPosition.START)
                            Icon(Icons.Outlined.CheckBox, "Start", modifier = Modifier.padding(end = 4.dp))
                        Text(stringResource(id = R.string.widget_list_configuration_checkbox_position))
                        if (listSettings.checkboxPosition.value == CheckboxPosition.END)
                            Icon(Icons.Outlined.CheckBox, "End", modifier = Modifier.padding(start = 4.dp))
                    }
                },
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            FilterChip(
                selected = listSettings.showOneRecurEntryInFuture.value,
                onClick = {
                    listSettings.showOneRecurEntryInFuture.value = !listSettings.showOneRecurEntryInFuture.value
                },
                label = { Text(stringResource(id = R.string.menu_list_limit_recur_entries)) },
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            FilterChip(
                selected = listSettings.showDescription.value,
                onClick = {
                    listSettings.showDescription.value = !listSettings.showDescription.value
                },
                label = { Text(stringResource(id = R.string.widget_list_show_description)) },
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            FilterChip(
                selected = listSettings.showSubtasks.value,
                onClick = {
                    listSettings.showSubtasks.value = !listSettings.showSubtasks.value
                },
                label = { Text(stringResource(id = R.string.widget_list_show_subtasks)) },
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            FilterChip(
                selected = listSettings.showSubnotes.value,
                onClick = {
                    listSettings.showSubnotes.value = !listSettings.showSubnotes.value
                },
                label = { Text(stringResource(id = R.string.widget_list_show_subnotes)) },
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }

        Text(
            text = stringResource(R.string.widget_list_configuration_only_one_hierarchy_layer_supported),
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))


        HeadlineWithIcon(
            icon = Icons.AutoMirrored.Outlined.Label,
            iconDesc = stringResource(id = R.string.category),
            text = stringResource(id = R.string.category)
        )

        Text(
            text = stringResource(R.string.widget_list_default_categories),
            style = MaterialTheme.typography.labelMedium,
            fontStyle = FontStyle.Italic
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            var maxEntries by rememberSaveable { mutableIntStateOf(MAX_ITEMS_PER_SECTION) }
            val allCategoriesSorted =
                if (allCategories.size > maxEntries) allCategories else allCategories.sortedBy { it.lowercase() }
            allCategoriesSorted.forEachIndexed { index, category ->
                if (index > maxEntries - 1)
                    return@forEachIndexed

                FilterChip(
                    selected = listSettings.defaultCategories.contains(category),
                    onClick = {
                        if(listSettings.defaultCategories.contains(category))
                            listSettings.defaultCategories.remove(category)
                        else
                            listSettings.defaultCategories.add(category)
                    },
                    label = { Text(category) }
                )
            }

            if (allCategories.size > maxEntries) {
                TextButton(onClick = { maxEntries = Int.MAX_VALUE }) {
                    Text(
                        stringResource(
                            R.string.filter_options_more_entries,
                            allCategories.size - maxEntries
                        )
                    )
                }
            }
            if (maxEntries == Int.MAX_VALUE) {
                TextButton(onClick = { maxEntries = MAX_ITEMS_PER_SECTION }) {
                    Text(stringResource(R.string.filter_options_less_entries))
                }
            }

        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeadlineWithIcon(
                icon = Icons.Outlined.Colorize,
                iconDesc = stringResource(id = R.string.color),
                text = stringResource(id = R.string.color)
            )

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.system_colors),
                style = MaterialTheme.typography.labelMedium
            )
            Switch(
                checked = listSettings.widgetColor.value == null,
                onCheckedChange = {
                    if(it) {
                        listSettings.widgetColor.value = null
                        listSettings.widgetColorEntries.value = null
                    } else {
                        listSettings.widgetColor.value = currentSurfaceColor
                        listSettings.widgetColorEntries.value = currentPrimaryContainerColor
                    }
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        AnimatedVisibility(listSettings.widgetColor.value != null) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FlowRow(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    AssistChip(
                        modifier = Modifier.padding(2.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = widgetColorCalculated,
                            labelColor = MaterialTheme.colorScheme.getContrastSurfaceColorFor(widgetColorCalculated)
                        ),
                        onClick = { showColorPickerBackground = true },
                        label = { Text(stringResource(R.string.widget_list_configuration_widget_background)) }
                    )
                    AssistChip(
                        modifier = Modifier.padding(2.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = widgetColorEntriesCalculated,
                            labelColor = MaterialTheme.colorScheme.getContrastSurfaceColorFor(widgetColorEntriesCalculated)
                        ),
                        onClick = { showColorPickerEntryBackground = true },
                        label = { Text(stringResource(R.string.widget_list_configuration_entries_background), modifier = Modifier.padding(horizontal = 8.dp)) }
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Preview(showBackground = true)
@Composable
fun ListWidgetConfigGeneral_Preview() {
    MaterialTheme {
        ListWidgetConfigGeneral(
            listSettings = ListSettings(),
            selectedModule = remember { mutableStateOf(Module.TODO)},
            allCategoriesLive = MutableLiveData(listOf("Category1", "#MyHashTag", "Whatever")),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ListWidgetConfigGeneral_Preview_withColor() {
    MaterialTheme {
        ListWidgetConfigGeneral(
            listSettings = ListSettings().apply {
                this.widgetColor.value = Color.Red.toArgb()
            },
            selectedModule = remember { mutableStateOf(Module.TODO)},
            allCategoriesLive = MutableLiveData(listOf("Category1", "#MyHashTag", "Whatever")),
            modifier = Modifier.fillMaxSize()
        )
    }
}


