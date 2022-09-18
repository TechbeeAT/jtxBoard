package at.techbee.jtx.ui.reusable.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.settings.DropdownSetting
import at.techbee.jtx.settings.DropdownSettingOption


@Composable
fun DropdownSetting(
    setting: DropdownSetting,
    preselected: DropdownSettingOption,
    onSelectionChanged: (selection: DropdownSettingOption) -> Unit,
    modifier: Modifier = Modifier
) {

    var selected by remember { mutableStateOf(preselected) }
    var expanded by remember { mutableStateOf(false) } // initial value


    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { expanded = true }
    ) {

        Icon(
            imageVector = setting.icon,
            contentDescription = null,
        modifier = Modifier.padding(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {

            Text(
                text = stringResource(id = setting.title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(id = selected.text),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Icon(
            imageVector = Icons.Outlined.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.padding(8.dp))

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            setting.options.forEach { option ->

                DropdownMenuItem(
                    onClick = {
                        selected = option
                        expanded = false
                        onSelectionChanged(option)
                    },
                    text = {
                        Text(
                            text = stringResource(id = option.text),
                            modifier = Modifier
                                .align(Alignment.Start)
                        )
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DropdownSetting_Theme() {
    MaterialTheme {

        DropdownSetting(
            setting = DropdownSetting.SETTING_THEME,
            preselected = DropdownSetting.SETTING_THEME.options.last(),
            onSelectionChanged = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DropdownSetting_AudioTheme() {
    MaterialTheme {

        DropdownSetting(
            setting = DropdownSetting.SETTING_AUDIO_FORMAT,
            preselected = DropdownSetting.SETTING_AUDIO_FORMAT.options.last(),
            onSelectionChanged = { }
        )
    }
}
