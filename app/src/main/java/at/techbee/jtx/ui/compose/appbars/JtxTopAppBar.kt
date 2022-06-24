package at.techbee.jtx.ui.compose.appbars

import androidx.compose.animation.Crossfade
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.ListSettings
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.ui.compose.elements.LabelledCheckbox
import at.techbee.jtx.ui.theme.JtxBoardTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JtxTopAppBar(
    drawerState: DrawerState,
    title: String,
    actions: @Composable () -> Unit = { }
) {

    val coroutineScope = rememberCoroutineScope()

    CenterAlignedTopAppBar(
        title = { Text(title)},
        navigationIcon = {
            IconButton(onClick = {
                coroutineScope.launch {
                    if(drawerState.isClosed) drawerState.open() else drawerState.close()
                }
            }) {
                Crossfade(targetState = drawerState) {
                    when (it.targetValue) {
                        DrawerValue.Open -> Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Localized description"
                        )
                        DrawerValue.Closed -> Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Localized description"
                        )
                        else -> Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Localized description"
                        )
                    }
                }

            }
        },
        actions = { actions() }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun JtxTopAppBar_Preview() {
    JtxBoardTheme {
        Scaffold(
            topBar = { JtxTopAppBar(
                drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
                title = "My Title comes here",
                actions = {
                    IconButton(onClick = { /* doSomething() */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = "Localized description"
                        )
                    }
                }
            ) },
            content = {}
        )
    }
}
