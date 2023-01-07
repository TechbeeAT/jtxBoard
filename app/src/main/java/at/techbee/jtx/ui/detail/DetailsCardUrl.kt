/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.detail

import android.content.ActivityNotFoundException
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.util.UiUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsCardUrl(
    initialUrl: String,
    isEditMode: Boolean,
    onUrlUpdated: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.url)
    var url by rememberSaveable { mutableStateOf(initialUrl) }
    val uriHandler = LocalUriHandler.current

    ElevatedCard(modifier = modifier, onClick = {
        try {
            if (url.isNotBlank() && !isEditMode)
                uriHandler.openUri(url)
        } catch (e: ActivityNotFoundException) {
            Log.d("PropertyCardUrl", "Failed opening Uri $url\n$e")
        }
    }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),) {

            Crossfade(isEditMode) {
                if(!it) {

                    Column {
                        HeadlineWithIcon(icon = Icons.Outlined.Link, iconDesc = headline, text = headline)
                        Text(url)
                    }
                } else {

                    OutlinedTextField(
                        value = url,
                        leadingIcon = { Icon(Icons.Outlined.Link, headline) },
                        trailingIcon = {
                            IconButton(onClick = {
                                url = ""
                                onUrlUpdated(url)
                            }) {
                                if (url.isNotEmpty())
                                    Icon(Icons.Outlined.Clear, stringResource(id = R.string.delete))
                            }
                        },
                        singleLine = true,
                        isError = url.isNotEmpty() && !UiUtil.isValidURL(url),
                        label = { Text(headline) },
                        onValueChange = { newUrl ->
                            url = newUrl
                            onUrlUpdated(url)
                        },
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
fun DetailsCardUrl_Preview() {
    MaterialTheme {
        DetailsCardUrl(
            initialUrl = "www.orf.at",
            isEditMode = false,
            onUrlUpdated = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardUrl_Preview_edit() {
    MaterialTheme {
        DetailsCardUrl(
            initialUrl = "www.bitfire.at",
            isEditMode = true,
            onUrlUpdated = {  }
        )
    }
}