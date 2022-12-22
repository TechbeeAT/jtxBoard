package at.techbee.jtx.ui.detail

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getSelectedText
import androidx.compose.ui.text.input.getTextAfterSelection
import androidx.compose.ui.text.input.getTextBeforeSelection

/**
 * DISABLED: For fields that don't allow Markdown
 * OBSERVING: Markdown row is active, after changing state the state should change back to observing
 * CLOSED: Markdown is basically available for the field, but was hidden by the user
 * all others are for specific formatting
 */
enum class MarkdownState {
    DISABLED, OBSERVING, CLOSED, BOLD, ITALIC, UNDERLINED, STRIKETHROUGH, H1, H2, H3, HR, UNORDEREDLIST, CODE;

    fun format(textFieldValue: TextFieldValue): TextFieldValue {
        return when(this) {
            DISABLED -> textFieldValue
            OBSERVING -> textFieldValue
            CLOSED -> textFieldValue
            BOLD -> addEnclosingTags(textFieldValue, "**", "**")
            ITALIC -> addEnclosingTags(textFieldValue, "*", "*")
            UNDERLINED -> addEnclosingTags(textFieldValue, "_", "_")
            STRIKETHROUGH -> addEnclosingTags(textFieldValue, "~~", "~~")
            H1 -> addTagAtLineStart(textFieldValue, "# ")
            H2 -> addTagAtLineStart(textFieldValue, "## ")
            H3 -> addTagAtLineStart(textFieldValue, "### ")
            HR -> addTagAtLineStart(textFieldValue, "--------" + System.lineSeparator())
            UNORDEREDLIST -> addTagAtLineStart(textFieldValue, "- ")
            CODE -> addEnclosingTags(textFieldValue, "`", "`")
        }
    }

    private fun addEnclosingTags(textFieldValue: TextFieldValue, before: String, after: String) =
        TextFieldValue(
            textFieldValue.getTextBeforeSelection(textFieldValue.annotatedString.length)
                .plus(AnnotatedString(before))
                .plus(textFieldValue.getSelectedText())
                .plus(AnnotatedString(after))
                .plus(textFieldValue.getTextAfterSelection(textFieldValue.annotatedString.length)),
            selection = TextRange(textFieldValue.selection.min+(before.length), textFieldValue.selection.max+(after.length))
        )

    private fun addTagAtLineStart(textFieldValue: TextFieldValue, tag: String) = TextFieldValue(
        textFieldValue.getTextBeforeSelection(textFieldValue.annotatedString.length)
            .plus(AnnotatedString(
                if(textFieldValue.getTextBeforeSelection(textFieldValue.annotatedString.length).endsWith(System.lineSeparator()))
                    tag
                else
                    System.lineSeparator() + tag
                )
            )
            .plus(textFieldValue.getSelectedText())
            .plus(textFieldValue.getTextAfterSelection(textFieldValue.annotatedString.length)),
        selection = TextRange(textFieldValue.getTextBeforeSelection(textFieldValue.annotatedString.length).length + tag.length+1)
    )
}