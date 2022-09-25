package at.techbee.jtx.ui.reusable.appbars

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import at.techbee.jtx.R
import at.techbee.jtx.ui.theme.montserratAlternates
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JtxTopAppBar(
    drawerState: DrawerState,
    title: String,
    subtitle: String? = null,
    actions: @Composable () -> Unit = { }
) {

    val coroutineScope = rememberCoroutineScope()

    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontFamily = montserratAlternates
                )
                subtitle?.let {
                    Text(
                        text = subtitle,
                        fontFamily = montserratAlternates,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
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
                            contentDescription = stringResource(id = R.string.navigation_drawer_open)
                        )
                        DrawerValue.Closed -> Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(id = R.string.navigation_drawer_close)
                        )
                        else -> Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(id = R.string.navigation_drawer_open)
                        )
                    }
                }

            }
        },
        actions = { actions() }
    )
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun JtxTopAppBar_Preview() {
    MaterialTheme {
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
            content = {  }
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun JtxTopAppBar_Preview_withSubtitle() {
    MaterialTheme {
        Scaffold(
            topBar = { JtxTopAppBar(
                drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
                title = "My Title comes here",
                subtitle = "Here's my subtitle",
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
