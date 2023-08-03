package at.techbee.jtx.ui.reusable.appbars

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import at.techbee.jtx.R
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.ui.reusable.destinations.NavigationDrawerDestination
import kotlinx.coroutines.launch

@Composable
fun JtxNavigationDrawer(
    drawerState: DrawerState,
    navController: NavController,
    mainContent: @Composable () -> Unit,
    paddingValues: PaddingValues = PaddingValues()
) {
    val scope = rememberCoroutineScope()
    val isProPurchased by BillingManager.getInstance().isProPurchased.observeAsState(false)
    val items = NavigationDrawerDestination.valuesFor(isProPurchased).groupBy { it.groupRes }

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
                HorizontalDivider(modifier = Modifier.padding(16.dp))
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
                        tint = //if(item == NavigationDrawerDestination.TWITTER) Color.Unspecified
                        if (item == NavigationDrawerDestination.MASTODON) Color.Unspecified
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
            NavigationDrawerDestination.valuesFor(false).groupBy { it.groupRes }

        JtxNavigationDrawerMenu(
            items = items,
            navController = rememberNavController(),
            onCloseDrawer = {}
        )
    }
}