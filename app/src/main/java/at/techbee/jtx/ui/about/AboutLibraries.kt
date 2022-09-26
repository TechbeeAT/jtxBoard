/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.Typography
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults


@Composable
fun AboutLibraries() {

    LibrariesContainer(
        header = {
            item {
                Text(
                    stringResource(id = R.string.about_tabitem_libraries),
                    style = Typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = LibraryDefaults.libraryColors(
            backgroundColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface,
            badgeBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            badgeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        onLibraryClick = { }  // Workaround to avoid crash, TODO: reactivate when problem solved, see https://gitlab.com/techbeeat1/jtx/-/issues/370
    )
}



@Preview(showBackground = true)
@Composable
fun AboutLibraries_Preview() {
    MaterialTheme {
        AboutLibraries()
    }
}
