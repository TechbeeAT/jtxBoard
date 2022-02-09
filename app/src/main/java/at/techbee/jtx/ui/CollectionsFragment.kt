/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui


import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import at.techbee.jtx.*
import at.techbee.jtx.database.*
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.util.SyncUtil
import at.techbee.jtx.util.SyncUtil.Companion.openDAVx5AccountsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ClassCastException
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import at.techbee.jtx.databinding.*
import at.techbee.jtx.util.DateTimeUtils
import java.io.IOException
import java.io.OutputStream


class CollectionsFragment : Fragment() {

    lateinit var binding: FragmentCollectionsBinding
    lateinit var application: Application
    private lateinit var dataSource: ICalDatabaseDao
    private val collectionsViewModel: CollectionsViewModel by activityViewModels()

    private lateinit var inflater: LayoutInflater
    private var optionsMenu: Menu? = null

    private var ics: String? = null
    private val getFileUriForSavingICS = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        this.inflater = inflater
        this.binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application
        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        // add menu
        setHasOptionsMenu(true)

        binding.model = collectionsViewModel
        binding.lifecycleOwner = viewLifecycleOwner

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

        return binding.root
    }

    override fun onResume() {

        collectionsViewModel.isDavx5Compatible.postValue(SyncUtil.isDAVx5CompatibleWithJTX(application))

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarTitle(getString(R.string.toolbar_text_collections), null)
        } catch (e: ClassCastException) {
            Log.d("setToolbarText", "Class cast to MainActivity failed (this is common for tests but doesn't really matter)\n$e")
        }
        super.onResume()
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
                            collectionsViewModel.requestICSForCollection(context, collection.toICalCollection())
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
                    }
                    true
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

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(dialogBinding.root)
            .setIcon(R.drawable.ic_color)
            .setPositiveButton(R.string.save)  { _, _ ->
                collection.displayName = dialogBinding.collectionDialogEdittext.text.toString()
                collection.color = dialogBinding.collectionDialogColorPicker.color
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
                currentCollection.numJournals?:0 > 0 && !it.supportsVJOURNAL -> return@forEach
                currentCollection.numNotes?:0 > 0 && !it.supportsVJOURNAL -> return@forEach
                currentCollection.numTodos?:0 > 0 && !it.supportsVTODO -> return@forEach
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
                collectionsViewModel.moveCollectionItems(currentCollection.collectionId, possibleCollections[selectedCollectionPos].collectionId)
            }
            .setNeutralButton(R.string.cancel) { _, _ ->  }
            .show()
    }
}


