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
import at.techbee.jtx.R
import java.util.TimeZone


@Composable
fun DropdownSettingTimezoneElement(
    setting: DropdownSettingTimezone,
    selected: String?,
    onSelectionChanged: (selection: String?) -> Unit,
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
                text = selected?.let { "${TimeZone.getTimeZone(it).id} (${TimeZone.getTimeZone(it).displayName})" } ?: stringResource(id = R.string.not_set2),
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
            setting.options.sortedBy { it?.let { TimeZone.getTimeZone(it).id } }.forEach { option ->

                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelectionChanged(option)
                    },
                    text = {
                        Text(
                            text = option?.let { "${TimeZone.getTimeZone(option).id} (${TimeZone.getTimeZone(option).displayName})" } ?: stringResource(R.string.not_set2),
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
fun DropdownSettingElementTimezone_preview() {
    MaterialTheme {
        DropdownSettingTimezoneElement(
            setting = DropdownSettingTimezone.SETTING_DEFAULT_START_TIMEZONE,
            selected = TimeZone.getDefault().toString(),
            onSelectionChanged = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DropdownSettingElementTimezone_preview_not_set() {
    MaterialTheme {
        DropdownSettingTimezoneElement(
            setting = DropdownSettingTimezone.SETTING_DEFAULT_START_TIMEZONE,
            selected = null,
            onSelectionChanged = { }
        )
    }
}
