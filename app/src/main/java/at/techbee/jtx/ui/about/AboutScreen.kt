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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.BuildFlavor
import at.techbee.jtx.R
import at.techbee.jtx.ui.buypro.BuyProScreenContent
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
    isPurchased: State<Boolean?>,
    priceLive: LiveData<String?>,
    purchaseDateLive: LiveData<String?>,
    orderIdLive: LiveData<String?>,
    launchBillingFlow: () -> Unit,
    navController: NavHostController
) {

    val screens = mutableListOf<AboutTabDestination>().apply {
        add(AboutTabDestination.Jtx)
        if(BuildFlavor.getCurrent().hasBilling && isPurchased.value == true)
            add(AboutTabDestination.JtxBoardPro)
        addAll(
            listOf(
                AboutTabDestination.Releasenotes,
                AboutTabDestination.Libraries,
                AboutTabDestination.Translations,
                AboutTabDestination.Contributors
            )
        )
    }
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
                            screens.forEachIndexed { index, screen ->
                                Tab(selected = pagerState.currentPage == index,
                                    onClick = {
                                        scope.launch { pagerState.animateScrollToPage(index) }
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
                                screens.indexOf(AboutTabDestination.Jtx) -> AboutJtx()
                                screens.indexOf(AboutTabDestination.Releasenotes) -> AboutReleaseinfo(releaseinfo)
                                screens.indexOf(AboutTabDestination.Libraries) -> AboutLibraries(libraries)
                                screens.indexOf(AboutTabDestination.Translations) -> AboutTranslations(translators)
                                screens.indexOf(AboutTabDestination.Contributors) -> AboutContributors(contributors)
                                screens.indexOf(AboutTabDestination.JtxBoardPro) -> BuyProScreenContent(
                                    isPurchased = isPurchased,
                                    priceLive = priceLive,
                                    purchaseDateLive = purchaseDateLive,
                                    orderIdLive = orderIdLive,
                                    launchBillingFlow = launchBillingFlow
                                )
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
            isPurchased = remember { mutableStateOf(null) },
            priceLive = MutableLiveData("€ 3,29"),
            orderIdLive = MutableLiveData("93287z4"),
            purchaseDateLive = MutableLiveData("Thursday, 1. Jannuary 2021"),
            launchBillingFlow = { },
            navController = rememberNavController()
        )
    }
}

