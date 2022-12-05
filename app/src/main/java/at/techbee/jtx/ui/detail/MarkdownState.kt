package at.techbee.jtx.ui.detail

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getSelectedText
import androidx.compose.ui.text.input.getTextAfterSelection
import androidx.compose.ui.text.input.getTextBeforeSelection

enum class MarkdownState {
    DISABLED, OBSERVING, BOLD, ITALIC, UNDERLINE;

    fun format(textFieldValue: TextFieldValue): TextFieldValue {
        return when(this) {
            DISABLED -> textFieldValue
            OBSERVING -> textFieldValue
            BOLD -> addTags(textFieldValue, "**", "**")
            ITALIC -> addTags(textFieldValue, "*", "*")
            UNDERLINE -> TODO()
        }
    }

    private fun addTags(textFieldValue: TextFieldValue, before: String?, after: String) =
        TextFieldValue(
            textFieldValue.getTextBeforeSelection(Int.MAX_VALUE)
                .plus(AnnotatedString(before?:""))
                .plus(textFieldValue.getSelectedText())
                .plus(AnnotatedString(after?:""))
                .plus(textFieldValue.getTextAfterSelection(Int.MAX_VALUE))
        )
}