/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.cards

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.ui.about.Release
import at.techbee.jtx.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseInfoCard(
    release: Release,
    modifier: Modifier = Modifier
) {

    val uri = try { Uri.parse(release.githubUrl) } catch (e: java.lang.NullPointerException) { null }
    val context = LocalContext.current

    ElevatedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags += Intent.FLAG_ACTIVITY_NEW_TASK
                data = uri
            }
            context.startActivity(intent)
        },
        modifier = modifier
    ) {

            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {

                Text(
                    release.releaseName,
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if(release.releaseText != null)
                    Text(
                        release.releaseText!!,
                        style = Typography.bodyMedium
                    )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReleaseInfoCard_Preview() {
    MaterialTheme {
        ReleaseInfoCard(
            Release(
                "v1.2.0",
                "- jtx Board now comes with a refactored list view with a more dynamic handling of subtasks, sub notes and attachments!\n- The new grid view option gives a more compact overview of journals, notes and tasks!\n- jtx Board is now also available in Spanish and Chinese!",
                false,
                ""
            )
        )
    }
}
