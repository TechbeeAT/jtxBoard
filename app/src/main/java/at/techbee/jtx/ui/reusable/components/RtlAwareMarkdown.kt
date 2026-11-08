package at.techbee.jtx.ui.reusable.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownParagraph
import com.mikepenz.markdown.compose.elements.MarkdownText
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes

/**
 * The default Markdown components render paragraphs and headings with a wrap-content width,
 * so the text box is always placed at the start of the parent (left in an LTR app) and
 * right-to-left paragraphs (e.g. Hebrew, Arabic) end up left-aligned.
 * These components let paragraphs and headings fill the available width and resolve the
 * text direction from their content, so each block is aligned according to its own language.
 */
val rtlAwareMarkdownComponents: MarkdownComponents = markdownComponents(
    paragraph = { model ->
        MarkdownParagraph(
            content = model.content,
            node = model.node,
            modifier = Modifier.fillMaxWidth(),
            style = model.typography.paragraph.withContentDirection()
        )
    },
    heading1 = { RtlAwareHeader(it, it.typography.h1) },
    heading2 = { RtlAwareHeader(it, it.typography.h2) },
    heading3 = { RtlAwareHeader(it, it.typography.h3) },
    heading4 = { RtlAwareHeader(it, it.typography.h4) },
    heading5 = { RtlAwareHeader(it, it.typography.h5) },
    heading6 = { RtlAwareHeader(it, it.typography.h6) },
    setextHeading1 = { RtlAwareHeader(it, it.typography.h1, MarkdownTokenTypes.SETEXT_CONTENT) },
    setextHeading2 = { RtlAwareHeader(it, it.typography.h2, MarkdownTokenTypes.SETEXT_CONTENT) },
)

/**
 * Lets the text direction be determined by the first strong directional character of each paragraph.
 */
fun TextStyle.withContentDirection(): TextStyle = copy(textDirection = TextDirection.Content)

@Composable
private fun RtlAwareHeader(
    model: MarkdownComponentModel,
    style: TextStyle,
    contentChildType: IElementType = MarkdownTokenTypes.ATX_CONTENT
) {
    MarkdownText(
        content = model.content,
        node = model.node,
        style = style.withContentDirection(),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { heading() },
        contentChildType = contentChildType,
    )
}
