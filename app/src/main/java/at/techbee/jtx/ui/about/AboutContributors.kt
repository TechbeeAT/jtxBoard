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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
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
import at.techbee.jtx.ui.theme.Typography


@Composable
fun AboutContributors(
    contributors: List<Contributor>,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        item {
            Text(
                text = stringResource(id = R.string.about_tabitem_contributors),
                style = Typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(contributors.asReversed()) { contributor ->
            ContributorCard(contributor, modifier = Modifier.padding(bottom = 2.dp))
        }

        item {
            TextButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TechbeeAT/jtxBoard/graphs/contributors"))
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo_github),
                    contentDescription = "GitHub",
                    modifier = Modifier.size(24.dp)
                )
                Text(text = "GitHub.com", modifier = Modifier.padding(8.dp))
            }
        }

        item {
            Text(
                text = stringResource(id = R.string.about_special_thanks),
                style = Typography.titleLarge,
                modifier = Modifier.padding(top = 32.dp)
            )
        }

        item {
            ElevatedCard(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.link_ffg))))
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
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
                Text(
                    text = stringResource(id = R.string.link_ffg),
                    style = Typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutContributors_Preview() {
    MaterialTheme {
        AboutContributors(
            contributors = listOf(
                Contributor.getSample(),
                Contributor.getSample()
            )
        )
    }
}