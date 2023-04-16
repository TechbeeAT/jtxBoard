/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.presets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.locals.StoredCategory
import at.techbee.jtx.database.locals.StoredResource
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.elements.HeadlineWithIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun PresetsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val database = ICalDatabase.getInstance(LocalContext.current).iCalDatabaseDao
    val allCategories by database.getAllCategoriesAsText().observeAsState(emptyList())
    val storedCategories by database.getStoredCategories().observeAsState(emptyList())
    val allResources by database.getAllResourcesAsText().observeAsState(emptyList())
    val storedResources by database.getStoredResources().observeAsState(emptyList())


    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(R.string.navigation_drawer_presets)
            )
        },
        content = { paddingValues ->
            JtxNavigationDrawer(
                drawerState = drawerState,
                mainContent = {

                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PresetsScreenContent(
                            allCategories = allCategories,
                            storedCategories = storedCategories,
                            allResources = allResources,
                            storedResources = storedResources,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                navController = navController,
                paddingValues = paddingValues
            )
        }
    )
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetsScreenContent(
    allCategories: List<String>,
    storedCategories: List<StoredCategory>,
    allResources: List<String>,
    storedResources: List<StoredResource>,
    modifier: Modifier = Modifier
) {

    var editCategory by remember { mutableStateOf<StoredCategory?>(null) }
    var editResource by remember { mutableStateOf<StoredResource?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if(editCategory != null) {
        EditStoredCategoryDialog(
            storedCategory = editCategory!!,
            onStoredCategoryChanged = {
                scope.launch(Dispatchers.IO) {
                    ICalDatabase.getInstance(context).iCalDatabaseDao.upsertStoredCategory(it)
                }
            },
            onDeleteStoredCategory = {
                scope.launch(Dispatchers.IO) {
                    ICalDatabase.getInstance(context).iCalDatabaseDao.deleteStoredCategory(it)
                }
            },
            onDismiss = { editCategory = null }
        )
    }

    if(editResource != null) {
        EditStoredResourceDialog(
            storedResource = editResource!!,
            onStoredResourceChanged = {
                scope.launch(Dispatchers.IO) {
                    ICalDatabase.getInstance(context).iCalDatabaseDao.upsertStoredResource(it)
                }
            },
            onDeleteStoredResource = {
                scope.launch(Dispatchers.IO) {
                    ICalDatabase.getInstance(context).iCalDatabaseDao.deleteStoredResource(it)
                }
            },
            onDismiss = { editResource = null }
        )
    }
    
    Column(
        modifier = modifier
    ) {

        HeadlineWithIcon(icon = Icons.Outlined.Label, iconDesc = null, text = "Preset categories")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {

            storedCategories.forEach { storedCategory ->
                ElevatedAssistChip(
                    onClick = { editCategory = storedCategory },
                    label = { Text(storedCategory.category) },
                    colors = storedCategory.color?.let {
                        AssistChipDefaults.assistChipColors(
                            containerColor = Color(it),
                            labelColor = contentColorFor(Color(it))
                        )
                    } ?: AssistChipDefaults.elevatedAssistChipColors()
                )
            }

            allCategories.filter { storedCategories.none { stored -> stored.category == it  } }.forEach { category ->
                ElevatedAssistChip(
                    onClick = { editCategory = StoredCategory(category, null) },
                    label = { Text(category) },
                    modifier = Modifier.alpha(0.5f)
                )
            }

            ElevatedAssistChip(
                onClick = { editCategory = StoredCategory("", null) },
                label = { Text("+") },
                colors = AssistChipDefaults.elevatedAssistChipColors()
            )
        }


        HeadlineWithIcon(
            icon = Icons.Outlined.WorkOutline,
            iconDesc = null,
            text = "Preset resources",
            modifier = Modifier.padding(top = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            storedResources.forEach { storedResource ->
                ElevatedAssistChip(
                    onClick = { editResource = storedResource },
                    label = { Text(storedResource.resource) },
                    colors = storedResource.color?.let {
                        AssistChipDefaults.assistChipColors(
                            containerColor = Color(it),
                            labelColor = contentColorFor(Color(it))
                        )
                    } ?: AssistChipDefaults.elevatedAssistChipColors()
                )
            }

            allResources.filter { storedResources.none { stored -> stored.resource == it  } }.forEach { resource ->
                ElevatedAssistChip(
                    onClick = { editResource = StoredResource(resource, null) },
                    label = { Text(resource) },
                    modifier = Modifier.alpha(0.5f)
                )
            }

            ElevatedAssistChip(
                onClick = { editResource = StoredResource("", null) },
                label = { Text("+") },
                colors = AssistChipDefaults.elevatedAssistChipColors()
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun PresetsScreen_Preview() {
    MaterialTheme {
        PresetsScreenContent(
            allCategories = listOf("existing"),
            storedCategories = listOf(StoredCategory("red", Color.Magenta.toArgb()), StoredCategory("ohne Farbe", null)),
            allResources = listOf("existing resource"),
            storedResources = listOf(StoredResource("blue", Color.Blue.toArgb()), StoredResource("ohne Farbe", null)),
        )
    }
}
