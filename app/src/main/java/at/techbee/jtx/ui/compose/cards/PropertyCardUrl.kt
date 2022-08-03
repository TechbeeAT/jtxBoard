/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import android.content.ActivityNotFoundException
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.util.UiUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyCardUrl(
    url: MutableState<String>,
    isEditMode: MutableState<Boolean>,
    onUrlUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.url)
    var urlError by remember { mutableStateOf(false) }

    val uriHandler = LocalUriHandler.current


    ElevatedCard(
        onClick = {
            try {
                if (url.value.isNotBlank() && !isEditMode.value)
                    uriHandler.openUri(url.value)
            } catch (e: ActivityNotFoundException) {
                Log.d("PropertyCardUrl", "Failed opening Uri $url\n$e")
            }
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {

            Crossfade(isEditMode) {
                if (!it.value) {

                    Column {
                        Text(headline, style = MaterialTheme.typography.labelSmall)

                        Row(
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(Icons.Outlined.Link, headline)
                            Text(
                                url.value,
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp)
                                    .align(alignment = Alignment.CenterVertically),
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = url.value,
                        leadingIcon = { Icon(Icons.Outlined.Link, headline) },
                        trailingIcon = {
                            IconButton(onClick = {
                                url.value = ""
                                /*TODO*/
                            }) {
                                if (url.value.isNotEmpty())
                                    Icon(Icons.Outlined.Clear, stringResource(id = R.string.delete))
                            }
                        },
                        singleLine = true,
                        isError = urlError,
                        label = { Text(headline) },
                        onValueChange = { newUrl ->
                            urlError = url.value.isNotEmpty() && !UiUtil.isValidURL(newUrl)
                            url.value = newUrl

                            /* TODO */
                        },
                        colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PropertyCardUrl_Preview() {
    MaterialTheme {
        PropertyCardUrl(
            url = mutableStateOf("www.orf.at"),
            isEditMode = mutableStateOf(false),
            onUrlUpdated = { /*TODO*/ }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PropertyCardUrl_Preview_edit() {
    MaterialTheme {
        PropertyCardUrl(
            url = mutableStateOf("www.bitfire.at"),
            isEditMode = mutableStateOf(true),
            onUrlUpdated = { /*TODO*/ }
        )
    }
}