/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.presets

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.Module
import at.techbee.jtx.ui.reusable.appbars.JtxNavigationDrawer
import at.techbee.jtx.ui.reusable.appbars.JtxTopAppBar
import at.techbee.jtx.ui.reusable.appbars.OverflowMenu
import at.techbee.jtx.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.IOException


@Composable
fun PresetsScreen(
    navController: NavHostController
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val database = ICalDatabase.getInstance(LocalContext.current).iCalDatabaseDao()
    val allCategories by database.getAllCategoriesAsText().observeAsState(emptyList())
    val storedCategories by database.getStoredCategories().observeAsState(emptyList())
    val allResources by database.getAllResourcesAsText().observeAsState(emptyList())
    val storedResources by database.getStoredResources().observeAsState(emptyList())
    val allXStatuses = mapOf(
        Pair(Module.JOURNAL, database.getAllXStatusesFor(Module.JOURNAL.name).observeAsState(emptyList())),
        Pair(Module.NOTE, database.getAllXStatusesFor(Module.NOTE.name).observeAsState(emptyList())),
        Pair(Module.TODO, database.getAllXStatusesFor(Module.TODO.name).observeAsState(emptyList()))
    )
    val extendedStatuses by database.getStoredStatuses().observeAsState(emptyList())
    val storedListSettings by database.getStoredListSettings(modules = listOf(Module.JOURNAL.name, Module.NOTE.name, Module.TODO.name)).observeAsState(emptyList())

    val launcherExportPresets = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { exportPresetsFilepath ->
        if(exportPresetsFilepath == null)
            return@rememberLauncherForActivityResult

        val presetData = PresetData(
            storedCategories = storedCategories,
            storedResources = storedResources,
            storedStatuses = extendedStatuses,
            storedListSettings = storedListSettings
        )
        val presetDataJson = Json.encodeToString(presetData)
        Log.d("presetDataJsonExport", presetDataJson)
        try {
            scope.launch(Dispatchers.IO) {
                context.contentResolver?.openOutputStream(exportPresetsFilepath)?.use { outputStream ->
                    outputStream.write(presetDataJson.toByteArray())
                }
                context.getString(R.string.presets_saved)
            }
        } catch (e: IOException) {
            toastMessage= context.getString(R.string.presets_export_error)
        }
    }

    val launcherImportPresets = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { filepath ->
        if(filepath == null)
            return@rememberLauncherForActivityResult

        context.contentResolver?.openInputStream(filepath)?.use { inputStream ->
            val presetDataJson = inputStream.readBytes().decodeToString()
            val presetData = try {
                Json.decodeFromString<PresetData>(presetDataJson)
            } catch (e: Exception) {
                toastMessage = context.getString(R.string.presets_import_invalid_file)
                return@rememberLauncherForActivityResult
            }
            Log.d("presetDataJsonImport", presetDataJson)
            scope.launch(Dispatchers.IO) {
                presetData.storedCategories.forEach { database.upsertStoredCategory(it) }
                presetData.storedResources.forEach { database.upsertStoredResource(it) }
                presetData.storedStatuses.forEach { database.upsertStoredStatus(it) }
                presetData.storedListSettings.forEach {
                    if(storedListSettings.filter { existing -> existing.module == it.module && existing.name == it.name }.size == 1)
                        it.id = storedListSettings.first { existing -> existing.module == it.module && existing.name == it.name }.id
                    else
                        it.id = 0L
                    database.upsertStoredListSetting(it)
                }
                toastMessage = context.getString(R.string.presets_imported)
            }
        }
    }

    LaunchedEffect(toastMessage) {
        if(toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
            toastMessage = null
        }
    }

    Scaffold(
        topBar = {
            JtxTopAppBar(
                drawerState = drawerState,
                title = stringResource(R.string.navigation_drawer_presets),
                actions = {
                    val menuExpanded = remember { mutableStateOf(false) }

                    OverflowMenu(menuExpanded = menuExpanded) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.menu_presets_import_presets)) },
                            onClick = {
                                menuExpanded.value = false
                                launcherImportPresets.launch(arrayOf("application/json"))
                            },
                            leadingIcon = { Icon(Icons.Outlined.FileUpload, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.menu_presets_export_presets)) },
                            onClick = {
                                menuExpanded.value = false
                                launcherExportPresets.launch("jtxBoard_presets_${DateTimeUtils.convertLongToYYYYMMDDString(System.currentTimeMillis(),null)}.ics")
                            },
                            leadingIcon = { Icon(Icons.Outlined.FileDownload, null) }
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            JtxNavigationDrawer(
                drawerState = drawerState,
                mainContent = {
                    PresetsScreenContent(
                        allCategories = allCategories,
                        storedCategories = storedCategories,
                        allResources = allResources,
                        storedResources = storedResources,
                        allXStatuses = allXStatuses,
                        extendedStatuses = extendedStatuses,
                        storedListSettings = storedListSettings,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    )
                },
                navController = navController,
                paddingValues = paddingValues
            )
        }
    )
}


data class XStatusStatusPair(
    val xstatus: String,
    val status: String?
)