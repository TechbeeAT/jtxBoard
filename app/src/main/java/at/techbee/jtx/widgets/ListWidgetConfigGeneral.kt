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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.ui.list.*
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListWidgetConfigGeneral(
    listSettings: ListSettings,
    selectedModule: MutableState<Module>,
    modifier: Modifier = Modifier
) {

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
            mainAxisAlignment = FlowMainAxisAlignment.Center
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
                    modifier = Modifier.fillMaxWidth().alpha(0.5f)
                )
            },
            singleLine = true,
            maxLines = 1,
            colors = TextFieldDefaults.textFieldColors(),
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

        Divider(modifier = Modifier.padding(vertical = 8.dp))

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

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        HeadlineWithIcon(
            icon = Icons.Outlined.Opacity,
            iconDesc = stringResource(id = R.string.opacity),
            text = stringResource(id = R.string.opacity)
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
                thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = listSettings.widgetAlpha.value),
                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = listSettings.widgetAlpha.value)
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
                thumbColor = MaterialTheme.colorScheme.surface.copy(alpha = listSettings.widgetAlphaEntries.value),
                activeTrackColor = MaterialTheme.colorScheme.surface.copy(alpha = listSettings.widgetAlphaEntries.value)
            ),
            steps = 20,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = listSettings.widgetAlpha.value))
                .padding(horizontal = 8.dp)
        )
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

