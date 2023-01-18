package at.techbee.jtx.ui.detail

import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.*
import org.junit.Test

internal class MarkdownStateTest {

    private val defaultTextFieldValue = TextFieldValue(
        annotatedString = AnnotatedString("AaaBbbCcc"),
        selection = TextRange(3,6)
    )

    @Test fun format_DISABLED() = assertEquals(defaultTextFieldValue, MarkdownState.DISABLED.format(defaultTextFieldValue))
    @Test fun format_OBSERVING() = assertEquals(defaultTextFieldValue, MarkdownState.OBSERVING.format(defaultTextFieldValue))

    @Test
    fun format_BOLD_range() {
        val textFieldValue = TextFieldValue(
            annotatedString = AnnotatedString(defaultTextFieldValue.text),
            selection = TextRange(3,6)
        )

        val newValue = MarkdownState.BOLD.format(textFieldValue)
        Log.d("newValue", "$newValue")
        assertEquals("Aaa**Bbb**Ccc", newValue.text)
    }

    @Test
    fun format_BOLD_no_range() {
        val textFieldValue = TextFieldValue(
            annotatedString = AnnotatedString(defaultTextFieldValue.text),
            selection = TextRange(3)
        )

        val newValue = MarkdownState.BOLD.format(textFieldValue)
        Log.d("newValue", "$newValue")
        assertEquals("Aaa****BbbCcc", newValue.text)
    }
}