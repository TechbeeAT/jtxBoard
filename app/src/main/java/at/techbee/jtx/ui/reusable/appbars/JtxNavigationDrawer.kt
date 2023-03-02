package at.techbee.jtx.ui.reusable.appbars

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.R
import at.techbee.jtx.ui.reusable.destinations.NavigationDrawerDestination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JtxNavigationDrawer(
    drawerState: DrawerState,
    navController: NavController,
    mainContent: @Composable () -> Unit,
    paddingValues: PaddingValues = PaddingValues()
) {
    val scope = rememberCoroutineScope()
    val items =
        NavigationDrawerDestination.valuesFor(BuildConfig.FLAVOR).groupBy { it.groupRes }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            JtxNavigationDrawerMenu(
                items = items,
                navController = navController,
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        },
        content = { mainContent() },
        modifier = Modifier.padding(paddingValues),
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun JtxNavigationDrawer_Preview() {
    MaterialTheme {

        JtxNavigationDrawer(
            drawerState = rememberDrawerState(DrawerValue.Open),
            navController = rememberNavController(),
            mainContent = { }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JtxNavigationDrawerMenu(
    items: Map<Int?, List<NavigationDrawerDestination>>,
    navController: NavController,
    onCloseDrawer: () -> Unit
) {

    val context = LocalContext.current

    ModalDrawerSheet(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(id = R.drawable.ic_jtx_logo),
                contentDescription = null
            )
            Column {
                Text(
                    stringResource(id = R.string.app_name),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    stringResource(id = R.string.navigation_drawer_subtitle),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Spacer(modifier = Modifier.padding(top = 16.dp))

        items.entries.forEach { entry ->

            entry.key?.let {
                Divider(modifier = Modifier.padding(16.dp))
                Text(
                    text = stringResource(id = it),
                    modifier = Modifier.padding(start = 24.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            entry.value.forEach { item ->

                NavigationDrawerItem(
                    icon = item.getIconComposable(
                        modifier = Modifier.size(24.dp),
                        tint = if(item == NavigationDrawerDestination.TWITTER) Color.Unspecified
                        else if (item == NavigationDrawerDestination.MASTODON) Color.Unspecified
                        else if(item.name == navController.currentDestination?.route) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    ),
                    label = { Text(stringResource(item.titleResource)) },
                    selected = item.name == navController.currentDestination?.route,
                    onClick = {
                        item.navigationAction(navController, context)
                        onCloseDrawer()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = MaterialTheme.colorScheme.surface,
                        unselectedIconColor = MaterialTheme.colorScheme.surface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JtxNavigationDrawerMenu_Preview() {
    MaterialTheme {
        val items =
            NavigationDrawerDestination.valuesFor(BuildConfig.FLAVOR).groupBy { it.groupRes }

        JtxNavigationDrawerMenu(
            items = items,
            navController = rememberNavController(),
            onCloseDrawer = {}
        )
    }
}