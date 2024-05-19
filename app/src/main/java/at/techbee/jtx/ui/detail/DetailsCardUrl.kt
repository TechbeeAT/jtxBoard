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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import at.techbee.jtx.util.UiUtil


@Composable
fun DetailsCardUrl(
    initialUrl: String,
    isReadOnly: Boolean,
    onUrlUpdated: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val headline = stringResource(id = R.string.url)
    var url by rememberSaveable { mutableStateOf(initialUrl) }
    val isValidURL = UiUtil.isValidURL(url)
    val uriHandler = LocalUriHandler.current
    val focusRequester = remember { FocusRequester() }

    ElevatedCard(
        modifier = modifier,
        onClick = { focusRequester.requestFocus() }
    ) {

        Row {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .weight(1f)
            ) {
                HeadlineWithIcon(icon = Icons.Outlined.Link, iconDesc = headline, text = headline)

                BasicTextField(
                    value = url,
                    textStyle = LocalTextStyle.current,
                    onValueChange = { newUrl ->
                        url = newUrl
                        onUrlUpdated(url)
                    },
                    enabled = !isReadOnly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .focusRequester(focusRequester)
                )

                AnimatedVisibility(!isReadOnly && url.isNotBlank() && !isValidURL) {
                    Text(
                        text = stringResource(id = R.string.invalid_url_message),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }


            AnimatedVisibility(isValidURL) {
                IconButton(onClick = {
                    try {
                        uriHandler.openUri(url)
                    } catch (e: ActivityNotFoundException) {
                        Log.d("PropertyCardUrl", "Failed opening Uri $url\n$e")
                    }
                }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.OpenInNew,
                        stringResource(id = R.string.open_in_browser)
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
            isReadOnly = false,
            onUrlUpdated = { }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardUrl_Preview_emptyUrl() {
    MaterialTheme {
        DetailsCardUrl(
            initialUrl = "",
            isReadOnly = false,
            onUrlUpdated = {  }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DetailsCardUrl_Preview_invalid_URL() {
    MaterialTheme {
        DetailsCardUrl(
            initialUrl = "invalid url",
            isReadOnly = false,
            onUrlUpdated = {  }
        )
    }
}