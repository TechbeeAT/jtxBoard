/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.settings

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R


@Composable
fun ExpandableSettingsSection(
    @StringRes headerText: Int,
    expandedDefault: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {

    var expanded by remember { mutableStateOf(expandedDefault) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        TextButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = stringResource(id = headerText),
                    style = MaterialTheme.typography.labelMedium,
                )
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandMore else Icons.Outlined.ExpandLess,
                    contentDescription = stringResource(id = if (expanded) R.string.list_collapse else R.string.list_expand)
                )
            }
        }

        AnimatedVisibility(expanded) {

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }

        Divider(
            modifier = Modifier
                .padding(top = 8.dp)
                .alpha(0.5f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExpandableSettingsSection_Preview() {
    MaterialTheme {

        ExpandableSettingsSection(
            headerText = R.string.settings_list,
            expandedDefault = true,
            content = { Text("Whatever setting") }
        )
    }
}