/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.Typography


@Composable
fun AboutSpecialThanks(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(id = R.string.about_tabitem_thanks),
            style = Typography.titleLarge,
        )

        Image(
            painter = painterResource(id = R.drawable.logo_ffg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .padding(top = 16.dp, bottom = 16.dp)
        )
        Text(
            text = stringResource(id = R.string.about_thanks_ffg),
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Center
        )
        TextButton(
            content = {
                Text(
                    text = stringResource(id = R.string.link_ffg),
                    style = Typography.titleLarge,
                )
            },
            modifier = Modifier.padding(top = 4.dp),
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.link_ffg))))
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AboutSpecialThanks_Preview() {
    JtxBoardTheme {
        AboutSpecialThanks()
    }
}