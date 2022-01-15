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
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import at.techbee.jtx.*
import at.techbee.jtx.database.*
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.databinding.FragmentCollectionDialogBinding
import at.techbee.jtx.databinding.FragmentCollectionItemBinding
import at.techbee.jtx.databinding.FragmentCollectionsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ClassCastException


class CollectionsFragment : Fragment() {

    lateinit var binding: FragmentCollectionsBinding
    lateinit var application: Application
    private lateinit var dataSource: ICalDatabaseDao
    private lateinit var viewModelFactory: CollectionsViewModelFactory
    private lateinit var collectionsViewModel: CollectionsViewModel
    private lateinit var inflater: LayoutInflater


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

        this.viewModelFactory = CollectionsViewModelFactory(dataSource, application)
        collectionsViewModel =
            ViewModelProvider(
                this, viewModelFactory
            )[CollectionsViewModel::class.java]

        //binding.model = collectionsViewModel
        //binding.lifecycleOwner = viewLifecycleOwner

        collectionsViewModel.localCollections.observe(viewLifecycleOwner) {
            binding.collectionsLocalNolocalcollections.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
            addCollectionView(it, true)
        }

        collectionsViewModel.remoteCollections.observe(viewLifecycleOwner) {
            binding.collectionsRemoteNoremotecollections.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
            addCollectionView(it, false)
        }

        return binding.root
    }

    override fun onResume() {

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

            // for now we show the menu only for local collections
            if(isLocal) {
                collectionItemBinding.collectionMenu.setOnClickListener {

                    val popup = PopupMenu(requireContext(), it)
                    val inflater: MenuInflater = popup.menuInflater
                    inflater.inflate(R.menu.menu_collection_popup, popup.menu)
                    popup.show()

                    popup.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.menu_collection_popup_edit -> showEditCollectionDialog(collection.toICalCollection())
                            R.id.menu_collection_popup_delete -> showDeleteCollectionDialog(collection.toICalCollection())
                        }
                        true
                    }
                }
            } else {
                collectionItemBinding.collectionMenu.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_collections, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_collections_add_local -> showEditCollectionDialog(ICalCollection.createLocalCollection(application))
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
            .setIcon(R.drawable.ic_collection)
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
}


