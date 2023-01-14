/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import java.util.*


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimezoneAutocompleteTextfield(
    timezone: String?,
    onTimezoneChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    var selectedTimezone by remember { mutableStateOf(timezone ?: TimeZone.getDefault().id) }

    var timezonesFiltered by remember { mutableStateOf(TimeZone.getAvailableIDs()) }

    LaunchedEffect(listState) {
        if(timezonesFiltered.indexOf(selectedTimezone) > -1)
            listState.animateScrollToItem(timezonesFiltered.indexOf(selectedTimezone))   // scroll to selected timezone
    }

    Column(modifier = modifier) {

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            items(items = timezonesFiltered)  { timezone ->
                FilterChip(
                    selected = selectedTimezone == timezone,
                    label = { Text(timezone) },
                    onClick = {
                        selectedTimezone = timezone
                        onTimezoneChanged(selectedTimezone)
                        keyboardController?.hide()
                    }
                )
            }
        }

        OutlinedTextField(
            value = selectedTimezone,
            onValueChange = {
                selectedTimezone = it
                onTimezoneChanged(selectedTimezone)
                timezonesFiltered = if(it.isEmpty()) TimeZone.getAvailableIDs() else TimeZone.getAvailableIDs().filter { tz -> tz.lowercase().contains(it.lowercase()) }.toTypedArray()
            },
            label = { Text(stringResource(id = R.string.timezone)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            isError = !TimeZone.getAvailableIDs().contains(selectedTimezone),
            trailingIcon = {
                if (selectedTimezone.isNotEmpty()) {
                    IconButton(onClick = {
                        selectedTimezone = ""
                        onTimezoneChanged(selectedTimezone)
                        timezonesFiltered = TimeZone.getAvailableIDs()
                    }) {
                        Icon(
                            Icons.Outlined.Close,
                            stringResource(id = R.string.delete)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(!TimeZone.getAvailableIDs().contains(selectedTimezone)) {
            Text(
                text = stringResource(id = R.string.invalid_timezone_message),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TimezoneAutocompleteTextfield_Preview() {
    MaterialTheme {
        TimezoneAutocompleteTextfield(
            timezone = null,
            onTimezoneChanged = { }
        )
    }
}