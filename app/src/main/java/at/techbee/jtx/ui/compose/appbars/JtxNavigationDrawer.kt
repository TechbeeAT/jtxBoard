package at.techbee.jtx.ui.compose.appbars

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.R
import at.techbee.jtx.ui.compose.destinations.NavigationDrawerDestination
import at.techbee.jtx.ui.theme.JtxBoardTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JtxNavigationDrawer(
    drawerState: DrawerState,
    navController: NavController,
    mainContent: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val items = NavigationDrawerDestination.valuesFor(BuildConfig.FLAVOR).groupBy { it.groupResource }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            JtxNavigationDrawerMenu(
                items = items,
                navController = navController,
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        },
        content = { mainContent() }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun JtxNavigationDrawer_Preview() {
    JtxBoardTheme {

        val context = LocalContext.current
        JtxNavigationDrawer(
            drawerState = rememberDrawerState(DrawerValue.Closed),
            navController = rememberNavController(),
            mainContent = {
                Text("Hello World")
            }
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

    var selectedItem by remember { mutableStateOf(NavigationDrawerDestination.BOARD) }
    val context = LocalContext.current

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp)
        ) {
            Image(
                painterResource(id = R.drawable.ic_jtx_logo),
                contentDescription = null,
                modifier = Modifier
            )
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
                Text(stringResource(id = R.string.app_name), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(stringResource(id = R.string.navigation_drawer_subtitle), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium)
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
                    icon = {
                        item.icon?.let { Icon(it, contentDescription = null) }
                            ?: item.iconResource?.let {
                                Image(
                                    painterResource(id = it),
                                    null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                    },
                    label = { Text(stringResource(item.titleResource)) },
                    selected = item == selectedItem,
                    onClick = {
                        item.navigationAction(navController, context)
                        onCloseDrawer()
                        selectedItem = item
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JtxNavigationDrawerMenu_Preview() {
    JtxBoardTheme {
        val items =
            NavigationDrawerDestination.valuesFor(BuildConfig.FLAVOR).groupBy { it.groupResource }

        JtxNavigationDrawerMenu(
            items = items,
            navController = rememberNavController(),
            onCloseDrawer = {}
        )
    }
}