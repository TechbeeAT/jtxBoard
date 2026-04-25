package at.techbee.jtx.ui.reusable.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun InteractiveMarkdownPreview() {
    val markdownContent = remember { mutableStateOf(
        """
        # Task List

        Here are some tasks:

        - [ ] Buy groceries
        - [x] Finish report
        - [ ] Call client

        Some regular text here.

        - [ ] Another task
        """.trimIndent()
    )}

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Interactive Markdown Checklist:", modifier = Modifier.padding(bottom = 8.dp))

        InteractiveMarkdown(
            content = markdownContent.value,
            onContentChange = { updatedContent ->
                markdownContent.value = updatedContent
            }
        )

        Text("Current content:", modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
        Text(markdownContent.value, modifier = Modifier.padding(8.dp))
    }
}
