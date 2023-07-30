/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.Typography


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AboutTranslations(
    translators: List<String>,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, top = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                stringResource(id = R.string.about_tabitem_translations),
                style = Typography.titleLarge,
            )
        }

        item {
            Text(
                stringResource(id = R.string.about_translations_basic_info),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
        item {
            Text(
                stringResource(id = R.string.about_translations_contribution_info),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
        item {
            Button(
                modifier = Modifier.padding(bottom = 8.dp),
                onClick = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://crowdin.com/project/jtx-board/invite?h=a8fd45e2dfea25534eda503b441476ea1545967")
                    )
                )
            }) {
                Text(stringResource(id = R.string.about_translations_contribution_button))
            }
        }

        items(items = translators) { translator ->
            TranslatorCard(
                name = translator,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .animateItemPlacement()
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AboutTranslations_Preview() {
    MaterialTheme {
        AboutTranslations(listOf("Patrick", "Ioannis", "Luis"))
    }
}
