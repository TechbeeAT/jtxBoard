package at.techbee.jtx.ui.reusable.elements

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import sh.calvin.reorderable.ReorderableScope

@Composable
fun DragHandle(scope: ReorderableScope) {
    IconButton(
        modifier = with(scope) { Modifier.draggableHandle() },
        onClick = { }
    ) {
        Icon(Icons.Outlined.DragHandle, null)
    }
}

@Preview(showBackground = true)
@Composable
fun DragHandle_Preview_without_Scope() {
    IconButton(onClick = {  }) {
        Icon(Icons.Outlined.DragHandle, null)
    }
}