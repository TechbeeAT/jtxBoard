/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui


import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.compose.dialogs.CollectionsAddOrEditDialog
import at.techbee.jtx.ui.compose.screens.CollectionsScreen
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.util.SyncUtil.Companion.openDAVx5AccountsActivity
import com.google.android.material.snackbar.Snackbar
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class CollectionsFragment : Fragment() {

    lateinit var application: Application
    private lateinit var dataSource: ICalDatabaseDao
    private val collectionsViewModel: CollectionsViewModel by activityViewModels()

    private var optionsMenu: Menu? = null

    private var iCalString2Import: String? = null
    private var iCalImportSnackbar: Snackbar? = null

    private var showCollectionsAddDialog = mutableStateOf(false)


    private var ics: String? = null
    private val getFileUriForSavingICS = registerForActivityResult(ActivityResultContracts.CreateDocument("text/calendar")) { uri ->
        if(ics.isNullOrEmpty() || uri == null) {
            Toast.makeText(context, R.string.collections_toast_export_ics_error, Toast.LENGTH_LONG)
            ics = null
            return@registerForActivityResult
        }

        try {
            val output: OutputStream? =
                context?.contentResolver?.openOutputStream(uri)
            output?.write(ics?.toByteArray())
            output?.flush()
            output?.close()
            Toast.makeText(context, R.string.collections_toast_export_ics_success, Toast.LENGTH_LONG)
        } catch (e: IOException) {
            Toast.makeText(context, R.string.collections_toast_export_ics_error, Toast.LENGTH_LONG)
        }
        ics = null
    }

    private var allExportIcs: MutableList<Pair<String, String>> = mutableListOf()  // first of pair is filename, second is ics
    private val getFileUriForSavingAllCollections = registerForActivityResult(ActivityResultContracts.CreateDocument("text/calendar")) { uri ->
        if(allExportIcs.isEmpty() || uri == null) {
            Toast.makeText(context, R.string.collections_toast_export_all_ics_error, Toast.LENGTH_LONG)
            allExportIcs.clear()
            return@registerForActivityResult
        }
        try {
            val output: OutputStream? = context?.contentResolver?.openOutputStream(uri)
            val bos = BufferedOutputStream(output)
            ZipOutputStream(bos).use { zos ->
                allExportIcs.forEach { ics ->
                    // not available on BufferedOutputStream
                    zos.putNextEntry(ZipEntry("${ics.first}.ics"))
                    zos.write(ics.second.toByteArray())
                    zos.closeEntry()
                }
            }
            output?.flush()
            output?.close()
            Toast.makeText(context, R.string.collections_toast_export_all_ics_success, Toast.LENGTH_LONG)
        } catch (e: IOException) {
            Toast.makeText(context, R.string.collections_toast_export_all_ics_error, Toast.LENGTH_LONG)
        }
        allExportIcs.clear()
    }

    private var icsFilepickerTargetCollection: CollectionsView? = null
    private val icsFilepickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //processFileAttachment(result.data?.data)
                val ics = result.data?.data ?: return@registerForActivityResult
                val icsString = context?.contentResolver?.openInputStream(ics)?.readBytes()?.decodeToString() ?: return@registerForActivityResult

                icsFilepickerTargetCollection?.let {
                    collectionsViewModel.isProcessing.postValue(true)
                    collectionsViewModel.insertICSFromReader(it.toICalCollection(), icsString)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        this.application = requireNotNull(this.activity).application
        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        setHasOptionsMenu(true)

        val arguments = CollectionsFragmentArgs.fromBundle((requireArguments()))
        arguments.iCalString?.let { iCalString2Import = it }


        collectionsViewModel.resultInsertedFromICS.observe(viewLifecycleOwner) {
            if(it == null)
                return@observe
            Snackbar.make(this.requireView(), getString(R.string.collections_snackbar_x_items_added, it.first, it.second), Snackbar.LENGTH_LONG).show()
            collectionsViewModel.isProcessing.postValue(false)
            collectionsViewModel.resultInsertedFromICS.postValue(null)
        }

        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                JtxBoardTheme {
                    // A surface container using the 'background' color from the theme

                    if(showCollectionsAddDialog.value)
                        CollectionsAddOrEditDialog(
                            current = ICalCollection.createLocalCollection(application),
                            onCollectionChanged = { collection -> collectionsViewModel.saveCollection(collection) },
                            onDismiss = { showCollectionsAddDialog.value = false }
                        )

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CollectionsScreen(
                            collectionsViewModel = collectionsViewModel,
                            navController = rememberNavController()
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {

        collectionsViewModel.isDavx5Compatible.postValue(SyncUtil.isDAVx5CompatibleWithJTX(application))

        if(iCalString2Import?.isNotEmpty() == true ) {
            iCalImportSnackbar = Snackbar.make(this.requireView(), R.string.collections_snackbar_select_collection_for_ics_import, Snackbar.LENGTH_INDEFINITE)
            iCalImportSnackbar?.show()
        }

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarTitle(getString(R.string.toolbar_text_collections), null)
        } catch (e: ClassCastException) {
            Log.d("setToolbarText", "Class cast to MainActivity failed (this is common for tests but doesn't really matter)\n$e")
        }
        super.onResume()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_collections, menu)
        optionsMenu = menu
    }



    private fun importFromICS(currentCollection: CollectionsView) {

        var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.type = "text/calendar"
        chooseFile = Intent.createChooser(chooseFile, "Choose a file")
        icsFilepickerTargetCollection = currentCollection
        try {
            icsFilepickerLauncher.launch(chooseFile)
        } catch (e: ActivityNotFoundException) {
            Log.e("chooseFileIntent", "Failed to open filepicker\n$e")
            Toast.makeText(context, "Failed to open filepicker", Toast.LENGTH_LONG).show()
        }
    }


}


