package at.techbee.jtx.ui.list

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTopAppBar(
    drawerState: DrawerState,
    module: Module,
    searchText: MutableState<String?>,
    onSearchTextUpdated: () -> Unit,
    actions: @Composable () -> Unit = { }
) {

    val coroutineScope = rememberCoroutineScope()

    CenterAlignedTopAppBar(
        title = {

            OutlinedTextField(
                value = searchText.value ?: "",
                onValueChange = {
                    searchText.value = it.ifBlank { null }
                    onSearchTextUpdated()
                                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Transparent,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant),
                shape = RoundedCornerShape(32.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = { Text(when(module) {
                        Module.JOURNAL -> stringResource(id = R.string.search_journals)
                        Module.NOTE -> stringResource(id = R.string.search_notes)
                        Module.TODO -> stringResource(id = R.string.search_todos)
                    }
                )},
                singleLine = true,
                maxLines = 1,
                leadingIcon = {
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
                trailingIcon = {
                    actions()
                }
            )
        }
    )
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ListTopAppBar_Preview() {
    MaterialTheme {
        Scaffold(
            topBar = { ListTopAppBar(
                drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
                module = Module.TODO,
                searchText = remember { mutableStateOf("") },
                onSearchTextUpdated = { },
                actions = {
                    IconButton(onClick = { /* doSomething() */ }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Localized description"
                        )
                    }
                }
            ) },
            content = {  }
        )
    }
}
