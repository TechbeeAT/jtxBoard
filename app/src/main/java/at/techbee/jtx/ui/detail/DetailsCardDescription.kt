/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getTextBeforeSelection
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import com.arnyminerz.markdowntext.MarkdownText
import org.apache.commons.lang3.StringUtils


@Composable
fun DetailsCardDescription(
    initialDescription: String?,
    isReadOnly: Boolean,
    markdownState: MutableState<MarkdownState>,
    isMarkdownEnabled: Boolean,
    onDescriptionUpdated: (String?) -> Unit,
    modifier: Modifier = Modifier
) {

    val focusRequester = remember { FocusRequester() }
    var focusRequested by remember { mutableStateOf(false) }
    var isDescriptionFocused by rememberSaveable { mutableStateOf(false)  }
    var description by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(
        TextFieldValue(initialDescription?:"")
    ) }

    LaunchedEffect(focusRequested, isDescriptionFocused) {
        if(focusRequested) {
            try {
                focusRequester.requestFocus()
                focusRequested = false
            } catch (e: Exception) {
                Log.d("DetailsCardDescription", "Requesting Focus failed")
            }
        }
    }


    ElevatedCard(
        onClick = {
            if(!isReadOnly) {
                focusRequested = true
            }
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    HeadlineWithIcon(
                        icon = Icons.Outlined.Summarize,
                        iconDesc = null,
                        text = stringResource(id = R.string.description)
                    )

                    Crossfade(!focusRequested && !isDescriptionFocused && isMarkdownEnabled,
                        label = "descriptionWithMarkdown"
                    ) { withMarkdown ->

                        if(withMarkdown) {
                            MarkdownText(
                                markdown = description.text.trim(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                style = TextStyle(
                                    textDirection = TextDirection.Content,
                                    fontFamily = LocalTextStyle.current.fontFamily
                                ),
                                onClick = {
                                    if (!isReadOnly) {
                                        focusRequested = true
                                    }
                                }
                            )
                        } else {
                            BasicTextField(
                                value = description,
                                textStyle = LocalTextStyle.current,
                                onValueChange = {

                                    // START Create bulletpoint if previous line started with a bulletpoint
                                    val enteredCharIndex =
                                        StringUtils.indexOfDifference(it.text, description.text)
                                    val enteredCharIsReturn =
                                        enteredCharIndex >= 0
                                                && it.text.substring(enteredCharIndex)
                                            .startsWith(System.lineSeparator())
                                                && it.text.length > description.text.length  // excludes backspace!

                                    val before = it.getTextBeforeSelection(Int.MAX_VALUE)
                                    val after =
                                        if (it.selection.start < it.annotatedString.lastIndex) it.annotatedString.subSequence(
                                            it.selection.start,
                                            it.annotatedString.lastIndex + 1
                                        ) else AnnotatedString("")
                                    val lines = before.split(System.lineSeparator())
                                    val previous =
                                        if (lines.lastIndex > 1) lines[lines.lastIndex - 1] else before
                                    val nextLineStartWith = when {
                                        previous.startsWith("- [ ] ") || previous.startsWith("- [x]") -> "- [ ] "
                                        previous.startsWith("* ") -> "* "
                                        previous.startsWith("- ") -> "- "
                                        else -> null
                                    }

                                    description =
                                        if (description.text != it.text && (nextLineStartWith != null) && enteredCharIsReturn)
                                            TextFieldValue(
                                                annotatedString = before.plus(
                                                    AnnotatedString(
                                                        nextLineStartWith
                                                    )
                                                ).plus(after),
                                                selection = TextRange(it.selection.start + nextLineStartWith.length)
                                            )
                                        else
                                            it
                                    // END Create bulletpoint if previous line started with a bulletpoint

                                    onDescriptionUpdated(description.text.ifBlank { null })
                                },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                enabled = !isReadOnly,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = if (isDescriptionFocused || description.text.isNotBlank()) 8.dp else 4.dp)
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { focusState ->
                                        isDescriptionFocused = focusState.hasFocus

                                        if (
                                            focusState.hasFocus
                                            && markdownState.value == MarkdownState.DISABLED
                                            && isMarkdownEnabled
                                        )
                                            markdownState.value = MarkdownState.OBSERVING
                                        else if (!focusState.hasFocus)
                                            markdownState.value = MarkdownState.DISABLED
                                    }
                            )
                        }
                    }
                }
           }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardDescription_Preview_no_Markdown() {
    MaterialTheme {
        DetailsCardDescription(
            initialDescription = "Test" + System.lineSeparator() + "***Tester***",
            isReadOnly = false,
            onDescriptionUpdated = { },
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isMarkdownEnabled = false
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardDescription_Preview_with_Markdown() {
    MaterialTheme {
        DetailsCardDescription(
            initialDescription = "Test" + System.lineSeparator() + "***Tester***",
            isReadOnly = false,
            onDescriptionUpdated = { },
            markdownState = remember { mutableStateOf(MarkdownState.DISABLED) },
            isMarkdownEnabled = true
        )
    }
}

