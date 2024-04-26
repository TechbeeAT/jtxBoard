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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Badge
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.Typography
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.entity.Organization
import com.mikepenz.aboutlibraries.ui.compose.util.author
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf


@Composable
fun AboutLibraries(
    libraries: Libs
) {

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top)
    ) {
        item {
            Text(
                stringResource(id = R.string.about_tabitem_libraries),
                style = Typography.titleLarge,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        items(items = libraries.libraries) { library ->
            AboutLibrariesLib(library = library)
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AboutLibraries_Preview() {
    MaterialTheme {
        AboutLibraries(
            libraries = Libs(
                libraries = persistentListOf(
                    Library(
                        uniqueId = "uniqueId",
                        artifactVersion = "v2.2.0",
                        name = "jtx Board",
                        description = "Description",
                        website = "https://jtx.techbee.at",
                        developers = persistentListOf(),
                        organization = Organization("Techbee e.U.", "https://techbee.at"),
                        scm = null,
                        licenses = persistentSetOf(License("jtx LIcense", "https://jtx.techbee.at", hash = ""))
                        //...
                    ),
                    Library(
                        uniqueId = "uniqueId",
                        artifactVersion = "v2.2.0",
                        name = "jtx Board",
                        description = "Description",
                        website = "https://jtx.techbee.at",
                        developers = persistentListOf(),
                        organization = Organization("Techbee e.U.", "https://techbee.at"),
                        scm = null,
                        licenses = persistentSetOf(License("jtx LIcense", "https://jtx.techbee.at", hash = ""))
                        //...
                    )
                ),
                licenses = persistentSetOf()
            )
        )
    }
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AboutLibrariesLib(
    library: Library
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top)
        ) {

            if(library.author.isNotBlank()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (library.author.isNotBlank()) {
                        Badge {
                            Text(
                                text = library.author,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp)

                            )
                        }
                    }

                    library.licenses.forEach { license ->
                        Badge {
                            Text(
                                text = license.name,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween, 
                modifier = Modifier.animateContentSize()
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = library.name,
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = if(expanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    library.artifactVersion?.let {

                        Text(
                            text = it,
                            modifier = Modifier.padding(horizontal = 8.dp).alpha(0.6f)
                        )
                    }
                }

                Row {

                    if (library.licenses.isNotEmpty()) {
                        library.licenses.forEach { license ->

                            val uri = try { Uri.parse(license.url) } catch (e: NullPointerException) { null } ?: return@forEach


                            IconButton(
                                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) },
                                content = {
                                    Icon(Icons.Outlined.Balance, stringResource(id = R.string.open_in_browser))
                                }
                            )
                        }
                    }

                    if (library.website?.isNotEmpty() == true) {

                        val uri = try { Uri.parse(library.website) } catch (e: java.lang.NullPointerException) { null }

                        IconButton(
                            onClick = {
                                if(uri != null)
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            },
                            content = {
                                Icon(Icons.Outlined.Public, stringResource(id = R.string.open_in_browser))
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(library.description?.isNotBlank() == true) {
                Text(
                    text = library.description ?: "",
                    maxLines = if(expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AboutLibrariesLib_Preview() {
    MaterialTheme {
        AboutLibrariesLib(
            Library(
                uniqueId = "uniqueId",
                artifactVersion = "v2.2.0",
                name = "jtx Board",
                description = "Description",
                website = "https://jtx.techbee.at",
                developers = persistentListOf(),
                organization = Organization("Techbee e.U.", "https://techbee.at"),
                scm = null,
                licenses = persistentSetOf(License("jtx License", "https://jtx.techbee.at", hash = ""))
                //...
            )
        )
    }
}
