package at.techbee.jtx.ui.reusable.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown

/**
 * Custom Markdown component that supports interactive checkboxes in checklists
 *
 * This component renders Markdown content with interactive checkboxes for task lists.
 * When a checkbox is clicked, the underlying Markdown content is updated to reflect
 * the new state (checked/unchecked).
 *
 * @param content The Markdown content to render
 * @param onContentChange Callback when content changes (e.g., checkbox state changes)
 * @param modifier Modifier for the component
 */
@Composable
fun InteractiveMarkdown(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val segments = remember(content) {
        parseMarkdownSegments(content)
    }

    val checkboxStates = remember(content) {
        segments.mapNotNull { segment ->
            (segment as? MarkdownSegment.CheckboxItem)?.let { mutableStateOf(it.isChecked) }
        }.toMutableList()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        var checkboxIndex = 0

        segments.forEach { segment ->
            when (segment) {
                is MarkdownSegment.MarkdownText -> {
                    if (segment.content.isNotBlank()) {
                        Markdown(
                            content = segment.content,
                            imageTransformer = Coil3ImageTransformerImpl,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is MarkdownSegment.CheckboxItem -> {
                    val currentCheckboxIndex = checkboxIndex++
                    InteractiveCheckboxItem(
                        isChecked = checkboxStates[currentCheckboxIndex].value,
                        text = segment.text,
                        onCheckedChange = { newState ->
                            checkboxStates[currentCheckboxIndex].value = newState
                            onContentChange(updateCheckboxState(content, segment.lineIndex, newState))
                        }
                    )
                }
            }
        }
    }
}

private val checkboxLineRegex = Regex("""^(\s*)-\s*\[([ xX])]\s*(.*)$""")

sealed interface MarkdownSegment {
    data class MarkdownText(val content: String) : MarkdownSegment
    data class CheckboxItem(
        val lineIndex: Int,
        val text: String,
        val isChecked: Boolean
    ) : MarkdownSegment
}

fun parseMarkdownSegments(content: String): List<MarkdownSegment> {
    if (content.isEmpty()) return emptyList()

    val segments = mutableListOf<MarkdownSegment>()
    val markdownLines = mutableListOf<String>()

    fun flushMarkdownLines() {
        if (markdownLines.isEmpty()) return
        segments.add(MarkdownSegment.MarkdownText(markdownLines.joinToString("\n")))
        markdownLines.clear()
    }

    content.split('\n').forEachIndexed { index, line ->
        val match = checkboxLineRegex.matchEntire(line)
        if (match != null) {
            flushMarkdownLines()
            segments.add(
                MarkdownSegment.CheckboxItem(
                    lineIndex = index,
                    text = match.groupValues[3],
                    isChecked = match.groupValues[2].equals("x", ignoreCase = true)
                )
            )
        } else {
            markdownLines.add(line)
        }
    }

    flushMarkdownLines()

    return segments
}

fun updateCheckboxState(
    originalContent: String,
    lineIndex: Int,
    newState: Boolean
): String {
    val lines = originalContent.split('\n').toMutableList()
    if (lineIndex !in lines.indices) return originalContent

    val match = checkboxLineRegex.matchEntire(lines[lineIndex]) ?: return originalContent
    val indentation = match.groupValues[1]
    val textContent = match.groupValues[3]
    lines[lineIndex] = "$indentation- [${if (newState) "x" else " "}] $textContent"

    return lines.joinToString("\n")
}

/**
 * Custom composable for rendering interactive checkboxes
 */
@Composable
fun InteractiveCheckboxItem(
    isChecked: Boolean,
    text: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .fillMaxWidth()
    ) {
        Icon(
            imageVector = if (isChecked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
            contentDescription = if (isChecked) "Checked" else "Unchecked",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
