package at.techbee.jtx.ui.compose.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.ui.compose.appbars.ListBottomAppBar
import at.techbee.jtx.ui.theme.JtxBoardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelledCheckbox(
    text: String,
    isChecked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {

        Text(text = text)
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChanged(it) },
            enabled = true,
        )

    }
}



@Preview(showBackground = true)
@Composable
fun LabelledCheckbox_Preview() {
    JtxBoardTheme {
        LabelledCheckbox("Checkbox Text", true, { })
    }
}
