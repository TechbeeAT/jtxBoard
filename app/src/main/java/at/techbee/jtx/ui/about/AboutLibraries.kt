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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                libraries = listOf(
                    Library(
                        uniqueId = "uniqueId",
                        artifactVersion = "v2.2.0",
                        name = "jtx Board",
                        description = "Description",
                        website = "https://jtx.techbee.at",
                        developers = emptyList(),
                        organization = Organization("Techbee e.U.", "https://techbee.at"),
                        scm = null,
                        licenses = setOf(License("jtx LIcense", "https://jtx.techbee.at", hash = ""))
                        //...
                    ),
                    Library(
                        uniqueId = "uniqueId",
                        artifactVersion = "v2.2.0",
                        name = "jtx Board",
                        description = "Description",
                        website = "https://jtx.techbee.at",
                        developers = emptyList(),
                        organization = Organization("Techbee e.U.", "https://techbee.at"),
                        scm = null,
                        licenses = setOf(License("jtx LIcense", "https://jtx.techbee.at", hash = ""))
                        //...
                    )
                ),
                licenses = emptySet()
            )
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = library.name,
                    modifier = Modifier.weight(1f).animateContentSize(),
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = if(expanded) 5 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                val version = library.artifactVersion
                if (version != null) {
                    Text(
                        text = version,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            if (library.author.isNotBlank()) {
                Text(text = library.author)
            }

            AnimatedVisibility(expanded && library.description?.isNotBlank() == true) {
                Text(
                    text = library.description ?: ""
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (library.licenses.isNotEmpty()) {
                    library.licenses.forEach { license ->

                        val uri = try { Uri.parse(license.url) } catch (e: NullPointerException) { null }

                        ElevatedAssistChip(
                            onClick = {
                                if(uri != null)
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                      },
                            label = {
                                Text(
                                    text = license.name
                                )
                            },
                            trailingIcon = {
                                if(uri != null)
                                    Icon(Icons.Outlined.OpenInNew, stringResource(id = R.string.open_in_browser))
                            }
                        )
                    }
                }

                if (library.website?.isNotEmpty() == true) {

                    val uri = try { Uri.parse(library.website) } catch (e: java.lang.NullPointerException) { null }

                    ElevatedAssistChip(
                        onClick = {
                            if(uri != null)
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                  },
                        label = {
                            Text(
                                text = stringResource(id = R.string.website)
                            )
                        },
                        trailingIcon = {
                            Icon(Icons.Outlined.OpenInNew, stringResource(id = R.string.open_in_browser))
                        }
                    )
                }
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
                developers = emptyList(),
                organization = Organization("Techbee e.U.", "https://techbee.at"),
                scm = null,
                licenses = setOf(License("jtx License", "https://jtx.techbee.at", hash = ""))
                //...
            )
        )
    }
}
