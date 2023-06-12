package at.techbee.jtx.ui.reusable.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R

@Composable
fun RadiobuttonWithText(
    text: String,
    isSelected: Boolean,
    hasSettings: Boolean,
    onClick: () -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier,
    subtext: String? = null,
) {
    Row(
        modifier = modifier.clickable {
             onClick()
        },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {

        RadioButton(selected = isSelected, onClick = { onClick() })
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 16.dp)
            )
            subtext?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 16.dp),
                    fontStyle = FontStyle.Italic
                )
            }
        }
        if(hasSettings) {
            IconButton(onClick = { onSettingsClicked() }) {
                Icon(Icons.Outlined.Settings, stringResource(id = R.string.kanban_settings))
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun RadiobuttonWithText_Preview() {
    MaterialTheme {
        RadiobuttonWithText(
            text = "Radio Text",
            isSelected = true,
            hasSettings = false,
            onClick = { },
            onSettingsClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RadiobuttonWithText_Preview_withSettings() {
    MaterialTheme {
        RadiobuttonWithText(
            text = "Radio Text",
            isSelected = true,
            hasSettings = true,
            onClick = { },
            onSettingsClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RadiobuttonWithText_Subtext_Preview() {
    MaterialTheme {
        RadiobuttonWithText(
            text = "Radio Text",
            isSelected = true,
            hasSettings = false,
            onClick = { },
            onSettingsClicked = { },
            subtext = "Subtext with explanation"
        )
    }
}
