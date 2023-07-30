/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.about


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import com.mikepenz.aboutlibraries.Libs
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AboutScreen(
    translators: List<String>,
    releaseinfo: List<Release>,
    contributors: List<Contributor>,
    libraries: Libs,
    navController: NavHostController
) {

    val screens = listOf(
        AboutTabDestination.Jtx,
        AboutTabDestination.Releasenotes,
        AboutTabDestination.Libraries,
        AboutTabDestination.Translations,
        AboutTabDestination.Contributors
    )
    val pagerState = rememberPagerState(initialPage = screens.indexOf(AboutTabDestination.Jtx), pageCount = { screens.size })
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(id = R.string.navigation_drawer_about)
            )
        },
        content = { paddingValues ->
            JtxNavigationDrawer(
                drawerState = drawerState,
                mainContent = {
                    Column {
                        TabRow(selectedTabIndex = pagerState.currentPage) {
                            screens.forEach { screen ->
                                Tab(selected = pagerState.currentPage == screen.tabIndex,
                                    onClick = {
                                        scope.launch { pagerState.animateScrollToPage(screen.tabIndex) }
                                              },
                                    text = {
                                        Icon(
                                            screen.icon,
                                            stringResource(id = screen.titleResource)
                                        )
                                    })
                            }
                        }
                        HorizontalPager(
                            state = pagerState,
                            verticalAlignment = Alignment.Top
                        ) { page ->
                            when (page) {
                                AboutTabDestination.Jtx.tabIndex -> AboutJtx()
                                AboutTabDestination.Releasenotes.tabIndex -> AboutReleaseinfo(releaseinfo)
                                AboutTabDestination.Libraries.tabIndex -> AboutLibraries(libraries)
                                AboutTabDestination.Translations.tabIndex -> AboutTranslations(translators)
                                AboutTabDestination.Contributors.tabIndex -> AboutContributors(contributors)
                            }
                        }
                    }
                },
                navController = navController,
                paddingValues = paddingValues
            )
        }
    )
}


@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    MaterialTheme {
        AboutScreen(
            translators = listOf("Patrick", "Ioannis", "Luis"),
            contributors = listOf(Contributor.getSample()),
            releaseinfo = listOf(
                Release(
                    "v1.2.0",
                    "- jtx Board now comes with a refactored list view with a more dynamic handling of subtasks, sub notes and attachments!\n- The new grid view option gives a more compact overview of journals, notes and tasks!\n- jtx Board is now also available in Spanish and Chinese!",
                    prerelease = false,
                    githubUrl = "https://github.com/TechbeeAT/jtxBoard/releases"
                ),
                Release(
                    "v1.2.1",
                    "- jtx Board now comes with a refactored list view with a more dynamic handling of subtasks, sub notes and attachments!\n- The new grid view option gives a more compact overview of journals, notes and tasks!\n- jtx Board is now also available in Spanish and Chinese!",
                    prerelease = false,
                    githubUrl = "https://github.com/TechbeeAT/jtxBoard/releases"
                ),
                Release(
                    "v1.2.2",
                    "- jtx Board now comes with a refactored list view with a more dynamic handling of subtasks, sub notes and attachments!\n- The new grid view option gives a more compact overview of journals, notes and tasks!\n- jtx Board is now also available in Spanish and Chinese!",
                    prerelease = false,
                    githubUrl = "https://github.com/TechbeeAT/jtxBoard/releases"
                )
            ),
            libraries = Libs(emptyList(), emptySet()),
            navController = rememberNavController()
        )
    }
}

