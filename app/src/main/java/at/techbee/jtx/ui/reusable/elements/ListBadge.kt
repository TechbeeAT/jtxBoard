/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.reusable.elements

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Badge
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.ui.settings.SettingsStateHolder
import at.techbee.jtx.ui.theme.getContrastSurfaceColorFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListBadge(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    iconDesc: String? = null,
    text: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    isAccessibilityMode: Boolean
) {
    val contentColor = MaterialTheme.colorScheme.getContrastSurfaceColorFor(containerColor)

    Badge(
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = iconDesc,
                    tint = contentColor,
                    modifier = if(isAccessibilityMode) Modifier.size(16.dp) else Modifier.size(12.dp)
                )
            }
            iconRes?.let {
                Icon(
                    painterResource(id = it),
                    contentDescription = iconDesc,
                    tint = contentColor,
                    modifier = if(isAccessibilityMode) Modifier.size(16.dp) else Modifier.size(12.dp)
                )
            }
            text?.let {
                Text(
                    text = it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = if(isAccessibilityMode) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListBadge_Preview() {
    MaterialTheme {
        ElevatedCard {
            ListBadge(
                icon = Icons.Outlined.Folder,
                iconDesc = "Collections",
                text = "Collections",
                isAccessibilityMode = false
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ListBadge_Preview_accessibility() {
    MaterialTheme {
        ElevatedCard {
            ListBadge(
                icon = Icons.Outlined.Folder,
                iconDesc = "Collections",
                text = "Collections",
                isAccessibilityMode = true
            )
        }
    }
}