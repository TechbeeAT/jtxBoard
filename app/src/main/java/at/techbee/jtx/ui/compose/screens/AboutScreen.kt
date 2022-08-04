package at.techbee.jtx.ui.compose.screens


import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.R
import at.techbee.jtx.ui.compose.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.compose.appbars.JtxTopAppBar
import at.techbee.jtx.ui.compose.destinations.AboutTabDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    translators: MutableLiveData<MutableSet<Pair<String, String>>>,
    releaseinfo: MutableLiveData<MutableSet<Pair<String, String>>>,
    navController: NavHostController
) {

    var selectedTab by remember { mutableStateOf(0) }
    val screens = listOf(
        AboutTabDestination.Jtx,
        AboutTabDestination.Releasenotes,
        AboutTabDestination.Libraries,
        AboutTabDestination.Translations,
        AboutTabDestination.Thanks
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    Scaffold(
        topBar = { JtxTopAppBar(
            drawerState = drawerState,
            title = stringResource(id = R.string.navigation_drawer_about)
        ) },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                JtxNavigationDrawer(
                    drawerState = drawerState,
                    mainContent = {
                        Column {
                            TabRow(selectedTabIndex = selectedTab) {
                                screens.forEach { screen ->
                                    Tab(selected = selectedTab == screen.tabIndex,
                                        onClick = { selectedTab = screen.tabIndex },
                                        text = { Icon(screen.icon, stringResource(id = screen.titleResource)) })
                                }
                            }

                            Crossfade(targetState = selectedTab) {
                                when (it) {
                                    AboutTabDestination.Jtx.tabIndex -> AboutJtx()
                                    AboutTabDestination.Releasenotes.tabIndex -> AboutReleaseinfo(releaseinfo)
                                    AboutTabDestination.Libraries.tabIndex -> AboutLibraries()
                                    AboutTabDestination.Translations.tabIndex -> AboutTranslations(translators)
                                    AboutTabDestination.Thanks.tabIndex -> AboutSpecialThanks()
                                }
                            }

                        }
                    },
                    navController = navController
                )
            }
        }
    )
}



@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    MaterialTheme {
        AboutScreen(
            MutableLiveData(
                mutableSetOf(
                    Pair("Patrick", "German, English, French"),
                    Pair("Ioannis", "Greek"),
                    Pair("Luis", "Portuguese")
                )
            ),
            MutableLiveData(
                mutableSetOf(
                    Pair("v1.2.0", "- jtx Board now comes with a refactored list view with a more dynamic handling of subtasks, sub notes and attachments!\n- The new grid view option gives a more compact overview of journals, notes and tasks!\n- jtx Board is now also available in Spanish and Chinese!"),
                    Pair("v1.2.0", "- jtx Board now comes with a refactored list view with a more dynamic handling of subtasks, sub notes and attachments!\n- The new grid view option gives a more compact overview of journals, notes and tasks!\n- jtx Board is now also available in Spanish and Chinese!"),
                    Pair("v1.2.0", "- jtx Board now comes with a refactored list view with a more dynamic handling of subtasks, sub notes and attachments!\n- The new grid view option gives a more compact overview of journals, notes and tasks!\n- jtx Board is now also available in Spanish and Chinese!")
                )
            ),
            rememberNavController()
        )
    }
}

