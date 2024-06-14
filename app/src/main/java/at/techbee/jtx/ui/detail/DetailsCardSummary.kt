/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon


@Composable
fun DetailsCardSummary(
    initialSummary: String?,
    isReadOnly: Boolean,
    focusRequested: Boolean,
    onSummaryUpdated: (String?) -> Unit,
    modifier: Modifier = Modifier
) {

    val focusRequester = remember { FocusRequester() }
    var isSummaryFocused by rememberSaveable { mutableStateOf(false)  }
    var summary by rememberSaveable { mutableStateOf(initialSummary) }

    LaunchedEffect(focusRequested) {
        if(focusRequested && !isReadOnly)
            focusRequester.requestFocus()
    }

    ElevatedCard(
        onClick = {
            if(!isReadOnly)
                focusRequester.requestFocus()
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
                        text = stringResource(id = R.string.summary)
                    )


                    BasicTextField(
                        value = summary?:"",
                        textStyle = LocalTextStyle.current,
                        onValueChange = { newSummary ->
                            summary = newSummary.ifBlank { null }
                            onSummaryUpdated(summary)
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                        enabled = !isReadOnly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = if (isSummaryFocused || !summary.isNullOrBlank()) 8.dp else 4.dp)
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->
                                isSummaryFocused = focusState.hasFocus
                            }
                    )
                }
           }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardSummary_Preview() {
    MaterialTheme {
        DetailsCardSummary(
            initialSummary = "Test",
            isReadOnly = false,
            focusRequested = false,
            onSummaryUpdated = { }
        )
    }
}

