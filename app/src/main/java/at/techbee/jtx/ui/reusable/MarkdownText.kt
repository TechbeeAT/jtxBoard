/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable

import android.content.ActivityNotFoundException
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

const val TAG = "MarkdownText"

/**
 * Provides the default values for calling [MarkdownText] and [markdownAnnotated].
 * @author Arnau Mora
 * @since 20221019
 */
object MarkdownTextDefaults {
    val bodyStyle: TextStyle
        @Composable
        get() = MaterialTheme.typography.bodyMedium

    val headlineDepthStyles
        @Composable
        get() = listOf(
            MaterialTheme.typography.headlineLarge,
            MaterialTheme.typography.headlineMedium,
            MaterialTheme.typography.headlineSmall,
            MaterialTheme.typography.titleLarge,
            MaterialTheme.typography.titleMedium,
            MaterialTheme.typography.titleSmall,
        )

    /**
     * The color given to links in [MarkdownText].
     * @author Arnau Mora
     * @since 20221019
     */
    val linkColor
        @Composable
        get() = MaterialTheme.colorScheme.primary

    /**
     * The character used by [MarkdownText] to mark list items.
     * @author Arnau Mora
     * @since 20221019
     */
    const val bullet = '\u2022'
}

/**
 * Annotates the [String] using Markdown formatting.
 * @author Arnau Mora
 * @since 20221019
 * @param bodyStyle The default style for the body, and non-annotated texts.
 * @param headlineDepthStyles A list of styles that will be used for headlines. Each element of the
 * list matches the depth given by adding `#`. Example: `###` will use the element at `2` of the list.
 * @param bullet The character to use as bullet for lists.
 * @param linkColor The color to use for tinting links.
 * @return As [AnnotatedString] instance formatted with the given markdown.
 */
@Composable
private fun String.markdownAnnotated(
    bodyStyle: TextStyle = MarkdownTextDefaults.bodyStyle,
    headlineDepthStyles: List<TextStyle> = MarkdownTextDefaults.headlineDepthStyles,
    bullet: Char = MarkdownTextDefaults.bullet,
    linkColor: Color = MarkdownTextDefaults.linkColor,
) = buildAnnotatedString {
    val headlineIndex = indexOf('#')
    if (headlineIndex >= 0) {
        // This is header, count depth
        val regex = Regex("[^#]")
        var depth = 0
        while (!regex.matchesAt(this@markdownAnnotated, depth)) depth++
        val headline = substring(depth + 1)
        val headlineTypography = headlineDepthStyles.getOrElse(depth - 1) { TextStyle.Default }
        withStyle(headlineTypography.toSpanStyle()) { append(headline) }
    } else if (startsWith('-')) { // List
        val item = substring(1)
        append("$bullet\t$item")
    } else {
        val lineLength = this@markdownAnnotated.length
        var lastStyle = bodyStyle.toSpanStyle()
        var c = 0
        pushStyle(bodyStyle.toSpanStyle())
        while (c < lineLength) {
            val char = get(c)
            val nextChar = c.takeIf { it + 1 < lineLength }?.let { get(it + 1) }
            if (char == '*' && nextChar == '*') { // Bold
                pop()
                lastStyle = if (lastStyle.fontWeight == FontWeight.Bold)
                    lastStyle.copy(fontWeight = FontWeight.Normal)
                else
                    lastStyle.copy(fontWeight = FontWeight.Bold)
                pushStyle(lastStyle)

                // Add two since the pointer is double
                c += 2
            } else if (char == '*') { // Italic
                pop()
                lastStyle = if (lastStyle.fontStyle == FontStyle.Italic)
                    lastStyle.copy(fontStyle = FontStyle.Normal)
                else
                    lastStyle.copy(fontStyle = FontStyle.Italic)
                pushStyle(lastStyle)
                c++
            } else if (char == '~') { // Strikethrough
                pop()
                lastStyle = if (lastStyle.textDecoration == TextDecoration.LineThrough)
                    lastStyle.copy(textDecoration = TextDecoration.None)
                else
                    lastStyle.copy(textDecoration = TextDecoration.LineThrough)
                pushStyle(lastStyle)
                c++
            } else if (char == '_') { // Underline
                pop()
                lastStyle = if (lastStyle.textDecoration == TextDecoration.Underline)
                    lastStyle.copy(textDecoration = TextDecoration.None)
                else
                    lastStyle.copy(textDecoration = TextDecoration.Underline)
                pushStyle(lastStyle)
                c++
            } else if (char == '[') { // Starts a link
                // Search for the closing tag
                val preClosing = indexOf(']', c + 1)
                // Search for the actual link start
                val lOpen = indexOf('(', c + 1)
                // And the ending
                val lClose = indexOf(')', c + 1)

                // Check if link is valid
                val outOfBounds = preClosing < 0 || lOpen < 0 || lClose < 0
                val overwrites = lOpen > lClose || preClosing > lOpen
                if (outOfBounds || overwrites) {
                    append(char)
                    c++
                } else {
                    val link = substring(lOpen + 1, lClose)
                    val text = substring(c + 1, preClosing)
                    pushStringAnnotation(
                        tag = "link",
                        annotation = link,
                    )
                    pushStyle(
                        lastStyle.copy(
                            textDecoration = TextDecoration.Underline,
                            color = linkColor,
                        ),
                    )
                    append(text)
                    pop()
                    c = lClose + 1
                }
            } else {
                Log.d(TAG, "Appending $char")
                append(char)
                c++
            }
        }
    }
}

/**
 * Creates a Text component that supports markdown formatting.
 * @author Arnau Mora
 * @since 20221019
 * @param markdown The markdown-formatted text to display.
 * @param modifier Modifiers to apply to the wrapper.
 * @param softWrap Whether the text should break at soft line breaks. If false, the glyphs in the
 * text will be positioned as if there was unlimited horizontal space. If [softWrap] is `false`,
 * [overflow] and TextAlign may have unexpected effects.
 * @param overflow How visual overflow should be handled.
 * @param maxLines An optional maximum number of lines for the text to span, wrapping if necessary.
 * If the text exceeds the given number of lines, it will be truncated according to [overflow] and
 * [softWrap]. If it is not null, then it must be greater than zero.
 * @param bodyStyle The default style for the body, and non-annotated texts.
 * @param headlineDepthStyles A list of styles that will be used for headlines. Each element of the
 * list matches the depth given by adding `#`. Example: `###` will use the element at `2` of the list.
 * @param bullet The character to use as bullet for lists.
 * @param linkColor The color to use for tinting links.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Visible,
    maxLines: Int = Int.MAX_VALUE,
    bodyStyle: TextStyle = MarkdownTextDefaults.bodyStyle,
    headlineDepthStyles: List<TextStyle> = MarkdownTextDefaults.headlineDepthStyles,
    bullet: Char = MarkdownTextDefaults.bullet,
    linkColor: Color = MarkdownTextDefaults.linkColor,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier,
    ) {
        markdown.split(System.lineSeparator()).forEach { line ->
            Log.d(TAG, "Line: $line")
            if (line.startsWith("--")) // If starts with at least two '-', add divider
                Divider()
            else {
                val annotatedString = line.markdownAnnotated(
                    bodyStyle, headlineDepthStyles, bullet, linkColor
                )
                Log.d(TAG, "Annotated line: $annotatedString")

                // TODO: Current implementation, since ClickableText is not theming correctly.
                // Reported at https://issuetracker.google.com/issues/255356401
                val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
                val pressIndicator = Modifier.pointerInput(null) {
                    detectTapGestures { pos ->
                        layoutResult.value?.let { layoutResult ->
                            val offset = layoutResult.getOffsetForPosition(pos)
                            annotatedString
                                .getStringAnnotations("link", offset, offset)
                                .firstOrNull()?.let { stringAnnotation ->
                                    try {
                                        uriHandler.openUri(stringAnnotation.item)
                                    } catch (e: ActivityNotFoundException) {
                                        Log.w(TAG, "Could not find link handler.")
                                    }
                                }
                        }
                    }
                }

                Text(
                    text = annotatedString,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                        .then(pressIndicator),
                    overflow = overflow,
                    maxLines = maxLines,
                    style = bodyStyle,
                    softWrap = softWrap,
                    onTextLayout = { layoutResult.value = it },
                )
            }
        }
    }
}

@Preview
@Composable
fun MarkdownTextPreview() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        MarkdownText(
            markdown = listOf(
                "This is markdown text with **bold** content.",
                "This is markdown text with *italic* content.",
                "**This** is where it gets complicated. With **bold and *italic* texts**.",
                "# Headers are also supported",
                "The work for separating sections",
                "## And setting",
                "Sub-sections",
                "### That get",
                "#### Deeper",
                "##### And Deeper",
                "###### And even deeper",
                "Remember _this_ ~not this~? Also works!",
                "[This](https://example.com) is a link.",
                "- Lists",
                "- are",
                "- also",
                "- supported",
                "--------",
                "That is a hr!"
            ).joinToString(System.lineSeparator()),
            modifier = Modifier
                .padding(horizontal = 8.dp),
            bodyStyle = MaterialTheme.typography.bodyMedium,
        )
    }
}