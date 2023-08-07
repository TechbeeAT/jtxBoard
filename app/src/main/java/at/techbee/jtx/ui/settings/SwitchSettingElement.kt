package at.techbee.jtx.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
fun SwitchSettingElement(
    setting: SwitchSetting,
    initiallyChecked: Boolean,
    onCheckedChanged: (checked: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {

    var checked by remember { mutableStateOf(initiallyChecked) }


    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {

        setting.icon()

        Column(
            modifier = Modifier
                .weight(1f)
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
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChanged(it)
            },
            modifier = Modifier.padding(12.dp),
            enabled = enabled
        )


    }
}


@Preview(showBackground = true)
@Composable
fun SwitchSettingElement_preview() {
    MaterialTheme {

        SwitchSettingElement(
            setting = SwitchSetting.SETTING_AUTO_EXPAND_ATTACHMENTS,
            initiallyChecked = false,
            onCheckedChanged = { }
        )
    }
}
