package at.techbee.jtx.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.dialogs.TimePickerDialog
import java.time.LocalTime


@Composable
fun TimeSettingElement(
    setting: TimeSetting,
    selectedTime: LocalTime?,
    @StringRes dialogTitleRes: Int,
    onSelectionChanged: (LocalTime?) -> Unit,
    modifier: Modifier = Modifier
) {

    var showDialog by remember { mutableStateOf(false) }

    if(showDialog) {
        TimePickerDialog(
            time = selectedTime,
            titleRes = dialogTitleRes,
            onConfirm = { newTime -> onSelectionChanged(newTime) },
            onDismiss = { showDialog = false }
        )
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { showDialog = true }
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
                    text = selectedTime?.toString() ?: stringResource(id = R.string.setting_no_time),
                    style = MaterialTheme.typography.bodySmall
            )

        }
    }
}


@Preview(showBackground = true)
@Composable
fun TimeSettingElement_withTime() {
    MaterialTheme {

        TimeSettingElement(
            setting = TimeSetting.SETTING_DEFAULT_START_TIME,
            dialogTitleRes = TimeSetting.SETTING_DEFAULT_START_TIME.title,
            selectedTime = LocalTime.of(13, 15),
            onSelectionChanged = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun TimeSettingElement_withoutTime() {
    MaterialTheme {

        TimeSettingElement(
            setting = TimeSetting.SETTING_DEFAULT_START_TIME,
            dialogTitleRes = TimeSetting.SETTING_DEFAULT_START_TIME.title,
            selectedTime = null,
            onSelectionChanged = { }
        )
    }
}