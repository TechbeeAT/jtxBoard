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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import at.techbee.jtx.*
import at.techbee.jtx.database.*
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.databinding.*
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.util.SyncUtil.Companion.openDAVx5AccountsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class CollectionsFragment : Fragment() {

    private var _binding: FragmentCollectionsBinding? = null
    private val binding get() = _binding!!

    lateinit var application: Application
    private lateinit var dataSource: ICalDatabaseDao
    private val collectionsViewModel: CollectionsViewModel by activityViewModels()

    private lateinit var inflater: LayoutInflater
    private var optionsMenu: Menu? = null

    private var iCalString2Import: String? = null
    private var iCalImportSnackbar: Snackbar? = null

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
    private val getFileUriForSavingAllCollections = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
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

    var icsFilepickerTargetCollection: CollectionsView? = null
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

        this.inflater = inflater
        this._binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application
        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        // add menu
        setHasOptionsMenu(true)

        binding.model = collectionsViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val arguments = CollectionsFragmentArgs.fromBundle((requireArguments()))
        arguments.iCalString?.let { iCalString2Import = it }

        collectionsViewModel.localCollections.observe(viewLifecycleOwner) {
            binding.collectionsLocalNolocalcollections.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
            addCollectionView(it, true)
        }

        collectionsViewModel.remoteCollections.observe(viewLifecycleOwner) {
            binding.collectionsRemoteNoremotecollections.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
            addCollectionView(it, false)
        }

        collectionsViewModel.isDavx5Compatible.observe(viewLifecycleOwner) {
            optionsMenu?.findItem(R.id.menu_collections_add_remote)?.isVisible = it
        }

        collectionsViewModel.resultInsertedFromICS.observe(viewLifecycleOwner) {
            if(it == null)
                return@observe
            Snackbar.make(this.requireView(), getString(R.string.collections_snackbar_x_items_added, it.first, it.second), Snackbar.LENGTH_LONG).show()
            collectionsViewModel.isProcessing.postValue(false)
            collectionsViewModel.resultInsertedFromICS.postValue(null)
        }

        return binding.root
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun addCollectionView(collections: List<CollectionsView>, isLocal: Boolean) {

        val parent = if(isLocal)
            binding.collectionsLocalLinearlayout
        else
            binding.collectionsRemoteLinearlayout
        parent.removeAllViews()

        collections.forEach { collection ->
            val collectionItemBinding = FragmentCollectionItemBinding.inflate(inflater, parent, true)
            collectionItemBinding.collectionAccount.text = collection.accountName
            if(collection.supportsVJOURNAL)
                collectionItemBinding.collectionJournalsNum.text = getString(R.string.collections_journals_num, collection.numJournals.toString())
            else
                collectionItemBinding.collectionJournalsNum.text = getString(R.string.collections_journals_num, getString(R.string.not_available_abbreviation))
            if(collection.supportsVJOURNAL)
                collectionItemBinding.collectionNotesNum.text = getString(R.string.collections_notes_num, collection.numNotes.toString())
            else
                collectionItemBinding.collectionNotesNum.text = getString(R.string.collections_notes_num, getString(R.string.not_available_abbreviation))
            if(collection.supportsVTODO)
                collectionItemBinding.collectionTasksNum.text = getString(R.string.collections_tasks_num, collection.numTodos.toString())
            else
                collectionItemBinding.collectionTasksNum.text = getString(R.string.collections_tasks_num, getString(R.string.not_available_abbreviation))
            collectionItemBinding.collectionCollection.text = collection.displayName

            if(collection.description.isNullOrEmpty())
                collectionItemBinding.collectionCollectionDescription.visibility = View.GONE
            else
                collectionItemBinding.collectionCollectionDescription.text = collection.description

            // applying the color
            ICalObject.applyColorOrHide(collectionItemBinding.collectionColorbar, collection.color)

            collectionItemBinding.collectionMenu.setOnClickListener {
                val popup = PopupMenu(requireContext(), it)
                val inflater: MenuInflater = popup.menuInflater
                inflater.inflate(R.menu.menu_collection_popup, popup.menu)

                if(isLocal) {
                    popup.menu.findItem(R.id.menu_collection_popup_show_in_davx5).isVisible = false
                    if(collectionsViewModel.localCollections.value?.size == 1)               // we don't allow the deletion of the last local collection
                        popup.menu.findItem(R.id.menu_collection_popup_delete).isVisible = false
                } else {
                    popup.menu.findItem(R.id.menu_collection_popup_delete).isVisible = false
                    popup.menu.findItem(R.id.menu_collection_popup_edit).isVisible = false
                }

                popup.show()
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_collection_popup_edit -> showEditCollectionDialog(collection.toICalCollection())
                        R.id.menu_collection_popup_delete -> showDeleteCollectionDialog(collection.toICalCollection())
                        R.id.menu_collection_popup_show_in_davx5 -> openDAVx5AccountsActivity(context)                 // TODO: Replace by new intent to open the specific account
                        R.id.menu_collection_popup_export_as_ics -> {
                            collectionsViewModel.requestICSForCollection(collection.toICalCollection())
                            collectionsViewModel.collectionICS.observe(viewLifecycleOwner) { ics ->
                                if(ics.isNullOrEmpty())
                                    return@observe
                                this.ics = ics
                                getFileUriForSavingICS.launch("${collection.displayName}_${DateTimeUtils.convertLongToYYYYMMDDString(System.currentTimeMillis(), null)}.ics")
                                //Log.d("collectionICS", ics)
                                collectionsViewModel.collectionICS.removeObservers(viewLifecycleOwner)
                                collectionsViewModel.collectionICS.postValue(null)
                            }
                        }
                        R.id.menu_collections_popup_move_entries -> showMoveEntriesDialog(collection)
                        R.id.menu_collection_popup_import_from_ics -> importFromICS(collection)
                    }
                    true
                }
            }


            // only if we have an iCalString2Import we would react on the click
            collectionItemBinding.root.setOnClickListener {
                iCalString2Import?.let {
                    collectionsViewModel.isProcessing.postValue(true)
                    collectionsViewModel.insertICSFromReader(collection.toICalCollection(), it)
                    iCalString2Import = null
                    iCalImportSnackbar?.dismiss()
                }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_collections, menu)
        optionsMenu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_collections_add_local -> showEditCollectionDialog(ICalCollection.createLocalCollection(application))
            R.id.menu_collections_add_remote -> openDAVx5AccountsActivity(context)
            R.id.menu_collections_export_all -> exportAll()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Shows a dialog to insert or update a collection
     * @param [collection] a new or existing collection that should be inserted/udpated
     */
    private fun showEditCollectionDialog(collection: ICalCollection) {

        val title = if(collection.collectionId == 0L)
            getString(R.string.collections_dialog_add_local_collection_title)
        else
            getString(R.string.collections_dialog_edit_local_collection_title)

        val dialogBinding = FragmentCollectionDialogBinding.inflate(inflater)
        if(collection.collectionId != 0L) {
            dialogBinding.collectionDialogEdittext.setText(collection.displayName)
            collection.color?.let{ dialogBinding.collectionDialogColorPicker.color = it }
        }
        dialogBinding.collectionDialogColorPicker.showOldCenterColor = false

        dialogBinding.collectionDialogColorPicker.visibility = if(collection.color != null) View.VISIBLE else View.GONE
        dialogBinding.collectionDialogAddColor.isChecked = collection.color != null
        dialogBinding.collectionDialogAddColor.setOnCheckedChangeListener { _, checked ->
            dialogBinding.collectionDialogColorPicker.visibility = if(checked) View.VISIBLE else View.GONE
        }


        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(dialogBinding.root)
            .setIcon(R.drawable.ic_color)
            .setPositiveButton(R.string.save)  { _, _ ->
                collection.displayName = dialogBinding.collectionDialogEdittext.text.toString()
                if(dialogBinding.collectionDialogAddColor.isChecked)
                    collection.color = dialogBinding.collectionDialogColorPicker.color
                else
                    collection.color = null
                collectionsViewModel.saveCollection(collection)
            }
            .setNeutralButton(R.string.cancel)  { _, _ -> /* nothing to do */  }
            .show()
    }

    private fun showDeleteCollectionDialog(collection: ICalCollection) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.collections_dialog_delete_local_title, collection.displayName))
            .setMessage(getString(R.string.collections_dialog_delete_local_message))
            .setIcon(R.drawable.ic_collection)
            .setPositiveButton(R.string.delete)  { _, _ ->
                collectionsViewModel.deleteCollection(collection)
            }
            .setNeutralButton(R.string.cancel)  { _, _ -> /* nothing to do */  }
            .show()
    }

    private fun showMoveEntriesDialog(currentCollection: CollectionsView) {

        /**
         * PREPARE DIALOG
         */
        val collectionMoveDialogBinding = FragmentCollectionMoveDialogBinding.inflate(layoutInflater)

        val title = getString(R.string.collections_dialog_move_title, currentCollection.displayName)
        val allCollections = mutableListOf<CollectionsView>()
        allCollections.addAll(collectionsViewModel.localCollections.value ?: emptyList())
        allCollections.addAll(collectionsViewModel.remoteCollections.value ?: emptyList())
        val possibleCollections = mutableListOf<ICalCollection>()
        val possibleCollectionsNames = mutableListOf<String>()

        allCollections.forEach {
            when {
                it.readonly -> return@forEach
                (currentCollection.numJournals ?: 0) > 0 && !it.supportsVJOURNAL -> return@forEach
                (currentCollection.numNotes ?: 0) > 0 && !it.supportsVJOURNAL -> return@forEach
                (currentCollection.numTodos ?: 0) > 0 && !it.supportsVTODO -> return@forEach
                currentCollection.collectionId == it.collectionId -> return@forEach
                else -> possibleCollections.add(it.toICalCollection())
            }
        }

        possibleCollections.forEach { collection ->
            if(collection.displayName?.isNotEmpty() == true && collection.accountName?.isNotEmpty() == true)
                possibleCollectionsNames.add(collection.displayName + " (" + collection.accountName + ")")
            else
                possibleCollectionsNames.add(collection.displayName?: "-")
        }

        var selectedCollectionPos = 0

        collectionMoveDialogBinding.collectionMoveDialogCollectionSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, possibleCollectionsNames)
        collectionMoveDialogBinding.collectionMoveDialogCollectionSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(p0: AdapterView<*>?, view: View?, pos: Int, p3: Long) {

                    selectedCollectionPos = pos

                    // update color of colorbar
                    try {
                        possibleCollections[pos].color.let { color ->
                            if(color == null)
                                collectionMoveDialogBinding.collectionMoveDialogColorbar.visibility = View.INVISIBLE
                            else {collectionMoveDialogBinding.collectionMoveDialogColorbar.visibility = View.VISIBLE
                                collectionMoveDialogBinding.collectionMoveDialogColorbar.setColorFilter(color)
                            }
                        }
                    } catch (e: IllegalArgumentException) {
                        //Log.i("Invalid color","Invalid Color cannot be parsed: ${color}")
                        collectionMoveDialogBinding.collectionMoveDialogColorbar.visibility = View.INVISIBLE
                    }
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }


        /**
         * SHOW DIALOG
         * The result is taken care of in the observer
         */
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(collectionMoveDialogBinding.root)
            .setPositiveButton(R.string.ok) { _, _ ->
                if(!collectionMoveDialogBinding.collectionMoveDialogCollectionSpinner.adapter.isEmpty)       // we only do something if there were actually entries
                    collectionsViewModel.moveCollectionItems(currentCollection.collectionId, possibleCollections[selectedCollectionPos].collectionId)
            }
            .setNeutralButton(R.string.cancel) { _, _ ->  }
            .show()
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

    private fun exportAll() {

        val allCollections: MutableList<ICalCollection> = mutableListOf()
        collectionsViewModel.localCollections.value?.forEach { collection ->
            allCollections.add(collection.toICalCollection())
        }
        collectionsViewModel.remoteCollections.value?.forEach { collection ->
            allCollections.add(collection.toICalCollection())
        }

        collectionsViewModel.requestAllForExport(allCollections)

        collectionsViewModel.allCollectionICS.observe(viewLifecycleOwner) { allIcs ->
            if(allIcs.isNullOrEmpty())
                return@observe
            this.allExportIcs.addAll(allIcs)
            getFileUriForSavingAllCollections.launch("jtxBoard_${DateTimeUtils.convertLongToYYYYMMDDString(System.currentTimeMillis(), TimeZone.getDefault().id)}.zip")
            collectionsViewModel.allCollectionICS.removeObservers(viewLifecycleOwner)
            collectionsViewModel.allCollectionICS.postValue(null)
        }
    }
}


