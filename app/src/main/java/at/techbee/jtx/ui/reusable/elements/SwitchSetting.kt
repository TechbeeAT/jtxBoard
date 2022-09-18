package at.techbee.jtx.ui.reusable.elements

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.ui.settings.SwitchSetting


@Composable
fun SwitchSetting(
    setting: SwitchSetting,
    initiallyChecked: Boolean,
    onCheckedChanged: (checked: Boolean) -> Unit,
    modifier: Modifier = Modifier
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
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChanged(it)
            },
            modifier = Modifier.padding(12.dp)
        )


    }
}


@Preview(showBackground = true)
@Composable
fun SwitchSetting_preview() {
    MaterialTheme {

        SwitchSetting(
            setting = SwitchSetting.SETTING_AUTO_EXPAND_ATTACHMENTS,
            initiallyChecked = false,
            onCheckedChanged = { }
        )
    }
}
