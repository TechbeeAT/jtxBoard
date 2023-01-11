package at.techbee.jtx.ui.list

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import at.techbee.jtx.R
import at.techbee.jtx.database.Module
import kotlinx.coroutines.launch

enum class ListTopAppBarMode { SEARCH, ADD_ENTRY }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTopAppBar(
    drawerState: DrawerState,
    listTopAppBarMode: ListTopAppBarMode,
    module: Module,
    searchText: MutableState<String?>,
    onSearchTextUpdated: () -> Unit,
    onCreateNewEntry: (String) -> Unit,
    actions: @Composable () -> Unit = { }
) {

    val coroutineScope = rememberCoroutineScope()
    var newEntryText by remember { mutableStateOf("")}

    CenterAlignedTopAppBar(
        title = {

            OutlinedTextField(
                value = when(listTopAppBarMode) {
                    ListTopAppBarMode.SEARCH -> searchText.value ?: ""
                    ListTopAppBarMode.ADD_ENTRY -> newEntryText
                },
                onValueChange = {
                    when(listTopAppBarMode) {
                        ListTopAppBarMode.SEARCH -> {
                            searchText.value = it.ifBlank { null }
                            onSearchTextUpdated()
                        }
                        ListTopAppBarMode.ADD_ENTRY -> {
                            newEntryText = it
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(32.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = {
                    Crossfade(listTopAppBarMode) { mode ->
                        when(mode) {
                            ListTopAppBarMode.SEARCH -> {
                                Crossfade(module) { module ->
                                    Text(
                                        when (module) {
                                            Module.JOURNAL -> stringResource(id = R.string.search_journals)
                                            Module.NOTE -> stringResource(id = R.string.search_notes)
                                            Module.TODO -> stringResource(id = R.string.search_todos)
                                        }
                                    )
                                }
                            }
                            ListTopAppBarMode.ADD_ENTRY -> {
                                Crossfade(module) { module ->
                                    Text(
                                        when (module) {
                                            Module.JOURNAL -> stringResource(id = R.string.toolbar_text_add_journal)
                                            Module.NOTE -> stringResource(id = R.string.toolbar_text_add_note)
                                            Module.TODO -> stringResource(id = R.string.toolbar_text_add_task)
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                singleLine = true,
                maxLines = 1,
                leadingIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                if(drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        },
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
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
                    Row {
                        AnimatedVisibility(listTopAppBarMode == ListTopAppBarMode.ADD_ENTRY && newEntryText.isNotEmpty()) {
                            IconButton(onClick = {
                                onCreateNewEntry(newEntryText)
                                newEntryText = ""
                            }) {
                                Crossfade(module) {
                                    when (it) {
                                        Module.JOURNAL -> Icon(Icons.Outlined.EventNote, stringResource(R.string.toolbar_text_add_journal))
                                        Module.NOTE -> Icon(Icons.Outlined.NoteAdd, stringResource(R.string.toolbar_text_add_note))
                                        Module.TODO -> Icon(Icons.Outlined.AddTask, stringResource(R.string.toolbar_text_add_task))
                                    }
                                }
                            }
                        }
                        actions()
                    }
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if(listTopAppBarMode == ListTopAppBarMode.ADD_ENTRY && newEntryText.isNotBlank()) {
                        onCreateNewEntry(newEntryText)
                        newEntryText = ""
                    }
                })
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
                listTopAppBarMode = ListTopAppBarMode.SEARCH,
                module = Module.TODO,
                searchText = remember { mutableStateOf("") },
                onSearchTextUpdated = { },
                onCreateNewEntry = { },
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


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ListTopAppBar_Preview_add_entry() {
    MaterialTheme {
        Scaffold(
            topBar = { ListTopAppBar(
                drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
                listTopAppBarMode = ListTopAppBarMode.ADD_ENTRY,
                module = Module.TODO,
                searchText = remember { mutableStateOf("") },
                onSearchTextUpdated = { },
                onCreateNewEntry = { },
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
