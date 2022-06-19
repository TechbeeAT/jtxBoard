/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.Typography
import java.text.SimpleDateFormat


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AboutJtx(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // let the bee talk, just for fun ;-)
    var clickCount by remember { mutableStateOf(-1) }
    val messages = arrayOf("Bzzzz", "Bzzzzzzzzz", "I'm working here", "What's up?", "If it's for coffee, then yes")

    Column(
        modifier = modifier
            .fillMaxSize()
            .scrollable(scrollState, orientation = Orientation.Vertical),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_jtx_logo),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .padding(top = 24.dp) )
        Text(
            text = stringResource(id = R.string.app_name),
            modifier = Modifier.padding(top = 16.dp),
            style = Typography.headlineMedium,
        )
        Text(
            text = stringResource(id = R.string.about_app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
            modifier = Modifier.padding(top = 4.dp),
            style = Typography.bodyLarge,
        )
        Text(
            text = stringResource(id = R.string.about_app_codename, BuildConfig.versionCodename),
            modifier = Modifier.padding(top = 4.dp),
            style = Typography.bodyLarge,
        )
        Text(
            text = stringResource(id = R.string.about_app_build_date, SimpleDateFormat.getDateInstance().format(BuildConfig.buildTime)),
            modifier = Modifier.padding(top = 4.dp),
            style = Typography.bodyLarge,
        )

        Text(
            text = stringResource(id = R.string.about_app_terms),
            modifier = Modifier.padding(top = 16.dp),
            style = Typography.titleMedium,
        )
        TextButton(
            content = {
                Text(
                    text = stringResource(id = R.string.link_jtx_terms),
                    style = Typography.bodyLarge,
                )
            },
            modifier = Modifier.padding(top = 4.dp),
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.link_jtx_terms))))
            }
        )
        Text(
            text = stringResource(id = R.string.about_app_copyright),
            modifier = Modifier.padding(top = 16.dp),
            style = Typography.bodyLarge,
        )

        Crossfade(targetState = clickCount) { clicks ->
            Image(
                painter = if(clicks < 4) painterResource(id = R.drawable.logo_techbee) else painterResource(id = R.drawable.logo_techbee_front),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .combinedClickable(
                        enabled = true,
                        onClickLabel = "Clickable image",
                        onClick = {
                            clickCount += 1
                        }
                    )
            )
        }
        AnimatedVisibility(visible = clickCount >= 0) {
            Text(
                text = "\"" + messages[if(clickCount>4) 4 else clickCount] + "\"",
                style = Typography.bodySmall,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutJtx_Preview() {
    JtxBoardTheme {
        AboutJtx()
    }
}