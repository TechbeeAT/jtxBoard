/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.list.ListSettings
import at.techbee.jtx.ui.reusable.dialogs.ColorPickerDialog
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.ui.theme.getContrastSurfaceColorFor


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListWidgetConfigGeneral(
    listSettings: ListSettings,
    selectedModule: MutableState<Module>,
    modifier: Modifier = Modifier
) {

    var showColorPickerBackground by remember { mutableStateOf(false) }
    var showColorPickerEntryBackground by remember { mutableStateOf(false) }

    val widgetColorCalculated = listSettings.widgetColor.value?.let { Color(it).copy(alpha = listSettings.widgetAlpha.value) } ?: MaterialTheme.colorScheme.primary.copy(alpha = listSettings.widgetAlpha.value)
    val widgetColorEntriesCalculated = listSettings.widgetColorEntries.value?.let { Color(it).copy(alpha = listSettings.widgetAlphaEntries.value) } ?: MaterialTheme.colorScheme.surface.copy(alpha = listSettings.widgetAlphaEntries.value)

    if(showColorPickerBackground) {
        ColorPickerDialog(
            initialColor = listSettings.widgetColor.value,
            onColorChanged = { listSettings.widgetColor.value = it },
            onDismiss = { showColorPickerBackground = false }
        )
    }

    if(showColorPickerEntryBackground) {
        ColorPickerDialog(
            initialColor = listSettings.widgetColorEntries.value,
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

            Module.values().forEach { module ->
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

            FilterChip(
                selected = listSettings.checkboxPositionEnd.value,
                onClick = {
                    listSettings.checkboxPositionEnd.value = !listSettings.checkboxPositionEnd.value
                },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!listSettings.checkboxPositionEnd.value)
                            Icon(Icons.Outlined.CheckBox, "Start", modifier = Modifier.padding(end = 4.dp))
                        Text(stringResource(id = R.string.widget_list_configuration_checkbox_position))
                        if (listSettings.checkboxPositionEnd.value)
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
            icon = Icons.Outlined.Opacity,
            iconDesc = stringResource(id = R.string.opacity),
            text = stringResource(id = R.string.opacity)
        )
        Text(
            text = stringResource(R.string.widget_list_opacity_warning),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            fontStyle = FontStyle.Italic
        )

        Text(
            text = stringResource(R.string.widget_list_configuration_widget_background),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(8.dp)
        )
        Slider(
            value = listSettings.widgetAlpha.value,
            valueRange = 0f..1f,
            onValueChange = {
                listSettings.widgetAlpha.value = it
            },
            colors = SliderDefaults.colors(
                thumbColor = widgetColorCalculated,
                activeTrackColor = widgetColorCalculated
            ),
            steps = 20,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            text = stringResource(R.string.widget_list_configuration_entries_background),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(8.dp)
        )
        Slider(
            value = listSettings.widgetAlphaEntries.value,
            valueRange = 0f..1f,
            onValueChange = {
                listSettings.widgetAlphaEntries.value = it
            },
            colors = SliderDefaults.colors(
                thumbColor = widgetColorEntriesCalculated,
                activeTrackColor = widgetColorEntriesCalculated
            ),
            steps = 20,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(listSettings.widgetColor.value?.let { Color(it).copy(alpha = listSettings.widgetAlpha.value) }
                    ?: MaterialTheme.colorScheme.primary.copy(alpha = listSettings.widgetAlpha.value))
                .padding(horizontal = 8.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        HeadlineWithIcon(
            icon = Icons.Outlined.Colorize,
            iconDesc = stringResource(id = R.string.color),
            text = stringResource(id = R.string.color)
        )
        Text(
            text = stringResource(R.string.widget_list_color_warning),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            fontStyle = FontStyle.Italic
        )

        FlowRow(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
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


@Preview(showBackground = true)
@Composable
fun ListWidgetConfigGeneral_Preview() {
    MaterialTheme {
        ListWidgetConfigGeneral(
            listSettings = ListSettings(),
            selectedModule = remember { mutableStateOf(Module.TODO)},
            modifier = Modifier.fillMaxSize()
        )
    }
}

