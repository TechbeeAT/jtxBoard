/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.locals.StoredListSetting
import at.techbee.jtx.database.locals.StoredListSettingData


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveListSettingsPresetDialog(
    currentSetting: StoredListSetting,
    storedListSettings: List<StoredListSetting>,
    onConfirm: (StoredListSetting) -> Unit,
    onDismiss: () -> Unit
) {

    var currentText by rememberSaveable { mutableStateOf(currentSetting.name) }
    var settingToSave by rememberSaveable { mutableStateOf(currentSetting) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.dialog_save_current_filter_config)) },
        text = {

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxWidth()
            ) {

                if(storedListSettings.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        storedListSettings.forEach { listSetting ->
                            FilterChip(
                                selected = listSetting.module == settingToSave.module && listSetting.id == settingToSave.id && listSetting.name == settingToSave.name,
                                onClick = {
                                    currentText = listSetting.name
                                    settingToSave =
                                        StoredListSetting(
                                            id = listSetting.id,
                                            module = listSetting.module,
                                            name = currentText,
                                            storedListSettingData = currentSetting.storedListSettingData
                                        )
                                },
                                label = { Text(listSetting.name) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = currentText,
                    isError = currentText.isBlank(),
                    onValueChange = { newText ->
                        currentText = newText
                        settingToSave =
                            StoredListSetting(
                                id = 0L,
                                module = currentSetting.module,
                                name = newText,
                                storedListSettingData = currentSetting.storedListSettingData
                            )
                    },
                    maxLines = 1,
                    label = { Text(stringResource(R.string.dialog_save_current_filter_my_filter)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if(currentText.isNotBlank()) {
                            onConfirm(settingToSave)
                            onDismiss()
                        }
                    })
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = currentText.isNotBlank(),
                onClick = {
                        onConfirm(settingToSave)
                        onDismiss()
                }
            ) {
                Text(stringResource(id = if(storedListSettings.any { listSetting -> listSetting.module == settingToSave.module && listSetting.id == settingToSave.id && listSetting.name == settingToSave.name }) R.string.update else R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SaveListSettingsPresetDialog_Preview() {

    val setting1 = StoredListSetting(module = Module.TODO, name = "first", storedListSettingData = StoredListSettingData())
    val setting2 = StoredListSetting(module = Module.TODO, name = "second", storedListSettingData = StoredListSettingData(searchCategories = listOf("cat1")))

    MaterialTheme {
        SaveListSettingsPresetDialog(
            currentSetting = setting1,
            storedListSettings = listOf(setting1, setting2),
            onConfirm = { },
            onDismiss = { }
        )
    }
}
