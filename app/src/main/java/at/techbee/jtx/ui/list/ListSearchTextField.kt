/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListSearchTextField(
    initialSeachText: String?,
    onSearchTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var searchText by remember { mutableStateOf(initialSeachText) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(
            value = searchText ?: "",
            onValueChange = {
                searchText = it
                onSearchTextChanged(it)
                            },
            label = { Text(stringResource(id = R.string.search)) },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
            )
    }
}

@Preview(showBackground = true)
@Composable
fun ListSearchTextField_Preview() {
    MaterialTheme {
        ListSearchTextField(
            initialSeachText = "",
            onSearchTextChanged = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}