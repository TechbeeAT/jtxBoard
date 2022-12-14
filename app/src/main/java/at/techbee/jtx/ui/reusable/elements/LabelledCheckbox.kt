package at.techbee.jtx.ui.reusable.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LabelledCheckbox(
    text: String,
    isChecked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChanged(it) },
            enabled = true,
        )
        Text(text = text)
    }
}



@Preview(showBackground = true)
@Composable
fun LabelledCheckbox_Preview() {
    MaterialTheme {
        LabelledCheckbox("Checkbox Text", true, { })
    }
}
