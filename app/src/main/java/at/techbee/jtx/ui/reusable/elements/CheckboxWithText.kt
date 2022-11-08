package at.techbee.jtx.ui.reusable.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CheckboxWithText(
    text: String,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtext: String? = null,
) {
    Row(
        modifier = modifier.clickable {
            onCheckedChange(!isSelected)
        },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {

        Checkbox(
            checked = isSelected,
            onCheckedChange = onCheckedChange
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = text, modifier = Modifier.padding(end = 16.dp))
            subtext?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.widthIn(max = 150.dp),
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun CheckboxWithText_Preview() {
    MaterialTheme {
        CheckboxWithText(
            text = "Radio Text",
            isSelected = false,
            onCheckedChange = { }
        )    }
}

@Preview(showBackground = true)
@Composable
fun CheckboxWithText_Subtext_Preview() {
    MaterialTheme {
        CheckboxWithText(
            text = "Radio Text",
            isSelected = true,
            onCheckedChange = { },
            subtext = "Subtext with explanation",
        )
    }
}
