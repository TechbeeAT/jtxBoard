package at.techbee.jtx.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun DropdownSettingElement(
    setting: DropdownSetting,
    selected: DropdownSettingOption,
    onSelectionChanged: (selection: DropdownSettingOption) -> Unit,
    modifier: Modifier = Modifier
) {

    var expanded by remember { mutableStateOf(false) }

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
            setting.subtitle?.let {
                Text(
                    text = stringResource(id = it),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
            }
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
fun DropdownSettingElement_Theme() {
    MaterialTheme {

        DropdownSettingElement(
            setting = DropdownSetting.SETTING_THEME,
            selected = DropdownSetting.SETTING_THEME.options.last(),
            onSelectionChanged = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DropdownSettingElement_Auto_Alarm() {
    MaterialTheme {

        DropdownSettingElement(
            setting = DropdownSetting.SETTING_AUTO_ALARM,
            selected = DropdownSetting.SETTING_AUTO_ALARM.options.last(),
            onSelectionChanged = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DropdownSettingElement_AudioTheme() {
    MaterialTheme {

        DropdownSettingElement(
            setting = DropdownSetting.SETTING_AUDIO_FORMAT,
            selected = DropdownSetting.SETTING_AUDIO_FORMAT.options.last(),
            onSelectionChanged = { }
        )
    }
}
