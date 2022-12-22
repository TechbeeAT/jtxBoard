package at.techbee.jtx.ui.reusable.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RadiobuttonWithText(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
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
        Column(modifier = Modifier.fillMaxWidth()) {
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
                    modifier = Modifier.widthIn(max = 150.dp),
                    fontStyle = FontStyle.Italic
                )
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
            onClick = { }
        )    }
}

@Preview(showBackground = true)
@Composable
fun RadiobuttonWithText_Subtext_Preview() {
    MaterialTheme {
        RadiobuttonWithText(
            text = "Radio Text",
            isSelected = true,
            onClick = { },
            subtext = "Subtext with explanation"
        )
    }
}
