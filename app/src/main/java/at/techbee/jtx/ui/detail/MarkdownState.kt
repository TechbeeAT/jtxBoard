package at.techbee.jtx.ui.detail

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getSelectedText
import androidx.compose.ui.text.input.getTextAfterSelection
import androidx.compose.ui.text.input.getTextBeforeSelection

enum class MarkdownState {
    DISABLED, OBSERVING, BOLD, ITALIC, UNDERLINED, STRIKETHROUGH, H1, H2, H3;

    fun format(textFieldValue: TextFieldValue): TextFieldValue {
        return when(this) {
            DISABLED -> textFieldValue
            OBSERVING -> textFieldValue
            BOLD -> addTags(textFieldValue, "**", "**")
            ITALIC -> addTags(textFieldValue, "*", "*")
            UNDERLINED -> addTags(textFieldValue, "_", "_")
            STRIKETHROUGH -> addTags(textFieldValue, "~", "~")
            H1 -> addTags(textFieldValue, "# ", "")
            H2 -> addTags(textFieldValue, "## ", "")
            H3 -> addTags(textFieldValue, "## ", "")
        }
    }

    private fun addTags(textFieldValue: TextFieldValue, before: String, after: String) =
        TextFieldValue(
            textFieldValue.getTextBeforeSelection(textFieldValue.annotatedString.length)
                .plus(AnnotatedString(before))
                .plus(textFieldValue.getSelectedText())
                .plus(AnnotatedString(after))
                .plus(textFieldValue.getTextAfterSelection(textFieldValue.annotatedString.length)),
            selection = TextRange(textFieldValue.selection.min+(before.length), textFieldValue.selection.max+(after.length))
        )
}