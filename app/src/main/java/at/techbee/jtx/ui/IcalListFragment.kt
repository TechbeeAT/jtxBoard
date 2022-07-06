/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui


import android.app.Application
import android.content.ContentResolver
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.util.SyncUtil


class IcalListFragment : Fragment() {

    private lateinit var arguments: IcalListFragmentArgs

    companion object {
        const val PREFS_LAST_USED_COLLECTION = "lastUsedCollection"
        const val PREFS_MODULE = "prefsModule"
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {


        // add menu
        setHasOptionsMenu(true)

        arguments = IcalListFragmentArgs.fromBundle((requireArguments()))



/*
        if(arguments.item2focus != 0L)
            icalListViewModel.scrollOnceId.postValue(arguments.item2focus)


        icalListViewModel.viewModeLive.observe(viewLifecycleOwner) {
            if(it == PREFS_VIEWMODE_LIST)
                optionsMenu?.findItem(R.id.menu_list_viewmode_list)?.isChecked = true
            if(it == PREFS_VIEWMODE_GRID)
                optionsMenu?.findItem(R.id.menu_list_viewmode_grid)?.isChecked = true
            if(it == PREFS_VIEWMODE_COMPACT)
                optionsMenu?.findItem(R.id.menu_list_viewmode_compact)?.isChecked = true
            if(it == PREFS_VIEWMODE_KANBAN)
                optionsMenu?.findItem(R.id.menu_list_viewmode_kanban)?.isChecked = true
        }
        
         */


        return ComposeView(requireContext()).apply {
        }
    }


    override fun onResume() {

        updateMenuVisibilities()

        super.onResume()
    }


    /**
     * This function hides/shows the relevant menu entries for the active module.
     */
    private fun updateMenuVisibilities() {
/*
        when (icalListViewModel.searchModule) {
            Module.JOURNAL.name -> {
                binding.fab.setImageResource(R.drawable.ic_add)
                binding.fab.setOnClickListener { goToEdit(ICalEntity(ICalObject.createJournal())) }
            }
            Module.NOTE.name -> {
                binding.fab.setImageResource(R.drawable.ic_add_note)
                binding.fab.setOnClickListener { goToEdit(ICalEntity(ICalObject.createNote())) }
            }
            Module.TODO.name -> {
                binding.fab.setImageResource(R.drawable.ic_todo_add)
                binding.fab.setOnClickListener { goToEdit(ICalEntity(ICalObject.createTodo().apply {
                    this.setDefaultDueDateFromSettings(requireContext())
                    this.setDefaultStartDateFromSettings(requireContext())
                })) }
            }
        }


        // don't show the option to clear the filter if no filter was set
        optionsMenu?.findItem(R.id.menu_list_clearfilter)?.isVisible = isFilterActive()
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter)?.icon =
            if(isFilterActive())
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_filter_delete)
            else
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_filter)

        if(!SyncUtil.isDAVx5CompatibleWithJTX(application))
            optionsMenu?.findItem(R.id.menu_list_syncnow)?.isVisible = false

 */

    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_list_clearfilter -> resetFilter()
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * Resets the focus item
     * Clears all filter criteria
     * Clears the preferences with the saved search criteria
     */
    private fun resetFilter() {
        /*
        icalListViewModel.clearFilter()

         */
    }


    private fun goToEdit(iCalObject: ICalEntity) {
        this.findNavController().navigate(IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(iCalObject))
    }


}
