/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import com.google.accompanist.flowlayout.FlowRow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailOptionsBottomSheet(
    module: Module,
    detailSettings: DetailSettings,
    onListSettingsChanged: () -> Unit,
    modifier: Modifier = Modifier
) {


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
                            if(detailSettings.detailSetting[DetailSettingsOption.ENABLE_SUMMARY] == false && detailSettings.detailSetting[DetailSettingsOption.ENABLE_DESCRIPTION] == false)
                                detailSettings.detailSetting[DetailSettingsOption.ENABLE_SUMMARY] = true

                            onListSettingsChanged()
                        },
                        label = { Text(stringResource(id = setting.stringResource)) },
                        trailingIcon = {
                            Crossfade(enabled) {
                                if(it)
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

@Preview(showBackground = true)
@Composable
fun DetailOptionsBottomSheet_Preview_TODO() {
    MaterialTheme {
        val detailSettings = DetailSettings()

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

        DetailOptionsBottomSheet(
            module = Module.JOURNAL,
            detailSettings = detailSettings,
            onListSettingsChanged = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
