/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.bottomsheets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
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
fun SearchTextBottomSheet(
    initialSeachText: String?,
    onSearchTextChanged: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {

    var searchText by remember { mutableStateOf(initialSeachText) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        OutlinedTextField(
            value = searchText ?: "",
            onValueChange = {
                searchText = it
                onSearchTextChanged(it)
                            },
            trailingIcon = {
                AnimatedVisibility(searchText?.isNotEmpty() == true) {
                    IconButton(onClick = {
                        searchText = ""
                        onSearchTextChanged("")
                    }
                    ) {
                        Icon(Icons.Outlined.SearchOff, null)
                    }
                }
            },
            label = { Text(stringResource(id = R.string.search)) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
        )

    }
}

@Preview(showBackground = true)
@Composable
fun SearchTextBottomSheet_Preview() {
    MaterialTheme {
        SearchTextBottomSheet(
            initialSeachText = "",
            onSearchTextChanged = { },
            focusRequester = FocusRequester()
        )
    }
}