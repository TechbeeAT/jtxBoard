/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.about

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.Typography
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributorCard(
    contributor: Contributor,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val openGithubProfileIntent = contributor.url?.let { Intent(Intent.ACTION_VIEW, it) }

    ElevatedCard(
        onClick = { openGithubProfileIntent.let { context.startActivity(it) } },
        modifier = modifier
    ) {

        Row(
            modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = contributor.avatarUrl,
                contentDescription = null,
                placeholder = painterResource(id = R.drawable.ic_person_pin),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Text(
                contributor.login,
                style = Typography.titleMedium,
                //fontWeight = FontWeight.Bold
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            if(openGithubProfileIntent !=null) {
                Icon(
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier
                        .alpha(0.1f)
                        .padding(end = 8.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContributorCard_Preview() {
    MaterialTheme {
        ContributorCard(Contributor.getSample())
    }
}
