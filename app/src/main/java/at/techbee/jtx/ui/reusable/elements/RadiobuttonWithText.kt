package at.techbee.jtx.ui.reusable.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadiobuttonWithText(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable {
             onClick()
        },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {

        RadioButton(selected = isSelected, onClick = { onClick() })
        Text(text = text, modifier = Modifier.padding(end = 16.dp))
    }
}



@Preview(showBackground = true)
@Composable
fun RadiobuttonWithText_Preview() {
    MaterialTheme {
        RadiobuttonWithText("Radio Text", true, { })
    }
}
