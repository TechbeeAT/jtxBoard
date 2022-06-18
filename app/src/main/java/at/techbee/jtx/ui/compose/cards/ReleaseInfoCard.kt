/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.compose.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseInfoCard(
    releaseName: String,
    releaseText: String,
    modifier: Modifier = Modifier
) {

    ElevatedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {

            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {

                Text(
                    releaseName,
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    releaseText,
                    style = Typography.bodyMedium
                )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReleaseInfoCard_Preview() {
    JtxBoardTheme {
        ReleaseInfoCard(
            "v1.2.0", "- jtx Board now comes with a refactored list view with a more dynamic handling of subtasks, sub notes and attachments!\n- The new grid view option gives a more compact overview of journals, notes and tasks!\n- jtx Board is now also available in Spanish and Chinese!"
        )
    }
}
