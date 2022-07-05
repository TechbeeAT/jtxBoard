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

    private lateinit var application: Application

    private var optionsMenu: Menu? = null
    private var gotodateMenuItem: MenuItem? = null

    private var settings: SharedPreferences? = null
    private lateinit var arguments: IcalListFragmentArgs

    private var allCollections = listOf<ICalCollection>()
    var currentWriteableCollections = listOf<ICalCollection>()



    companion object {
        const val PREFS_LAST_USED_COLLECTION = "lastUsedCollection"
        const val PREFS_MODULE = "prefsModule"
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        application = requireNotNull(this.activity).application

        // add menu
        setHasOptionsMenu(true)

        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
        arguments = IcalListFragmentArgs.fromBundle((requireArguments()))

        val modelJournals = IcalListViewModel(application, Module.JOURNAL)
        val modelNotes = IcalListViewModel(application, Module.NOTE)
        val modelTasks = IcalListViewModel(application, Module.TODO)




/*
        if(arguments.item2focus != 0L)
            icalListViewModel.scrollOnceId.postValue(arguments.item2focus)

        icalListViewModel.isSynchronizing.observe(viewLifecycleOwner) {
            binding.listProgressIndicator.visibility = if(it) View.VISIBLE else View.INVISIBLE
        }

        icalListViewModel.iCal4List.observe(viewLifecycleOwner) {
            updateMenuVisibilities()
        }

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



        ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
            modelJournals.isSynchronizing.postValue(SyncUtil.isJtxSyncRunning(requireContext()))
            modelNotes.isSynchronizing.postValue(SyncUtil.isJtxSyncRunning(requireContext()))
            modelTasks.isSynchronizing.postValue(SyncUtil.isJtxSyncRunning(requireContext()))
        }


        return ComposeView(requireContext()).apply {
        }
    }


    override fun onResume() {

        updateMenuVisibilities()

        super.onResume()
    }

    /*
        override fun onResume() {
        super.onResume()

        try {
            val activity = requireActivity() as MainActivity
            val toolbarText = getString(R.string.toolbar_text_jtx_board)
            val toolbarSubtitle = when (module) {
                Module.JOURNAL -> getString(R.string.toolbar_text_jtx_board_journals_overview)
                Module.NOTE -> getString(R.string.toolbar_text_jtx_board_notes_overview)
                Module.TODO -> getString(R.string.toolbar_text_jtx_board_tasks_overview)
            }
            activity.setToolbarTitle(toolbarText, toolbarSubtitle)
        } catch (e: ClassCastException) {
            Log.d(
                "setToolbarText",
                "Class cast to MainActivity failed (this is common for tests but doesn't really matter)\n$e"
            )
        }
    }
     */



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

        gotodateMenuItem?.isVisible = icalListViewModel.searchModule == Module.JOURNAL.name
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_quick_journal).isVisible = icalListViewModel.searchModule == Module.JOURNAL.name
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_quick_note).isVisible = icalListViewModel.searchModule == Module.NOTE.name
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_quick_todo).isVisible = icalListViewModel.searchModule == Module.TODO.name

        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_toggle_completed_tasks).isChecked = icalListViewModel.isExcludeDone
        if(icalListViewModel.searchModule == Module.TODO.name) {
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_overdue).isChecked = icalListViewModel.isFilterOverdue
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_due_today).isChecked = icalListViewModel.isFilterDueToday
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_due_tomorrow).isChecked = icalListViewModel.isFilterDueTomorrow
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_due_in_future).isChecked = icalListViewModel.isFilterDueFuture
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_no_dates_set).isChecked = icalListViewModel.isFilterNoDatesSet
        }
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_overdue).isVisible = icalListViewModel.searchModule == Module.TODO.name
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_due_today).isVisible = icalListViewModel.searchModule == Module.TODO.name
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_due_tomorrow).isVisible = icalListViewModel.searchModule == Module.TODO.name
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_due_in_future).isVisible = icalListViewModel.searchModule == Module.TODO.name
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_no_dates_set).isVisible = icalListViewModel.searchModule == Module.TODO.name

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
            /*
            R.id.menu_list_viewmode_list -> icalListViewModel.viewMode.postValue(PREFS_VIEWMODE_LIST)
            R.id.menu_list_viewmode_grid -> icalListViewModel.viewMode.postValue(PREFS_VIEWMODE_GRID)
            R.id.menu_list_viewmode_compact -> {
                if ((BuildConfig.FLAVOR == MainActivity.BUILD_FLAVOR_GOOGLEPLAY && BillingManager.getInstance()?.isProPurchased?.value == false)) {
                    val snackbar = Snackbar.make(requireView(), R.string.buypro_snackbar_please_purchase_pro, Snackbar.LENGTH_LONG)
                    snackbar.setAction(R.string.more) {
                        findNavController().navigate(R.id.action_global_buyProFragment)
                    }
                    snackbar.show()
                } else {
                    icalListViewModel.viewMode.postValue(PREFS_VIEWMODE_COMPACT)
                }
            }
            R.id.menu_list_viewmode_kanban -> {
                if ((BuildConfig.FLAVOR == MainActivity.BUILD_FLAVOR_GOOGLEPLAY && BillingManager.getInstance()?.isProPurchased?.value == false)) {
                    val snackbar = Snackbar.make(requireView(), R.string.buypro_snackbar_please_purchase_pro, Snackbar.LENGTH_LONG)
                    snackbar.setAction(R.string.more) {
                        findNavController().navigate(R.id.action_global_buyProFragment)
                    }
                    snackbar.show()
                } else {
                    icalListViewModel.viewMode.postValue(PREFS_VIEWMODE_KANBAN)
                }
            }
             */
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
