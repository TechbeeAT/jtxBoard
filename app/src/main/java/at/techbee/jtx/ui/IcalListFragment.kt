/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui


import android.accounts.AccountManager
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcel
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.databinding.FragmentIcalListBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import java.lang.ClassCastException
import java.util.*


class IcalListFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    //private var staggeredGridLayoutManager: StaggeredGridLayoutManager? = null

    //private var icalListAdapter: IcalListAdapter? = null
    private var journalListAdapter: IcalListAdapterJournal? = null
    private var noteListAdapter: IcalListAdapterNote? = null
    private var todoListAdapter: IcalListAdapterTodo? = null

    private lateinit var icalListViewModel: IcalListViewModel

    private lateinit var binding: FragmentIcalListBinding
    private lateinit var application: Application
    private lateinit var dataSource: ICalDatabaseDao

    private var optionsMenu: Menu? = null
    private var gotodateMenuItem: MenuItem? = null

    private var settings: SharedPreferences? = null

    private lateinit var prefs: SharedPreferences
    private lateinit var arguments: IcalListFragmentArgs

    private var lastScrolledFocusItemId: Long? = null

    private var lastSearchModule = ""
    private var lastSearchStatusTodo = mutableListOf<StatusTodo>()
    private var lastIcal4ListHash: Int = 0



    companion object {
        const val PREFS_LIST_VIEW = "sharedPreferencesListView"
        const val PREFS_MODULE = "prefsModule"

        //Journal
        const val PREFS_JOURNAL_COLLECTION = "prefsJournalCollection"
        const val PREFS_JOURNAL_CATEGORIES = "prefsJournalCategories"
        const val PREFS_JOURNAL_CLASSIFICATION = "prefsJournalClassification"
        const val PREFS_JOURNAL_STATUS_JOURNAL = "prefsJournalStatusJournal"
        const val PREFS_JOURNAL_STATUS_TODO = "prefsJournalStatusTodo"
        //Note
        const val PREFS_NOTE_COLLECTION = "prefsNoteCollection"
        const val PREFS_NOTE_CATEGORIES = "prefsNoteCategories"
        const val PREFS_NOTE_CLASSIFICATION = "prefsNoteClassification"
        const val PREFS_NOTE_STATUS_JOURNAL = "prefsNoteStatusJournal"
        const val PREFS_NOTE_STATUS_TODO = "prefsNoteStatusTodo"
        //Todos
        const val PREFS_TODO_COLLECTION = "prefsTodoCollection"
        const val PREFS_TODO_CATEGORIES = "prefsTodoCategories"
        const val PREFS_TODO_CLASSIFICATION = "prefsTodoClassification"
        //const val PREFS_TODO_STATUS_JOURNAL = "prefsTodoStatusJournal"
        const val PREFS_TODO_STATUS_TODO = "prefsTodoStatusTodo"

        const val TAB_INDEX_JOURNAL = 0
        const val TAB_INDEX_NOTE = 1
        const val TAB_INDEX_TODO = 2
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        binding = FragmentIcalListBinding.inflate(inflater, container, false)

        // set up DB DAO
        application = requireNotNull(this.activity).application
        dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao


        // create the view model through the view model factory
        val viewModelFactory = IcalListViewModelFactory(dataSource, application)
        icalListViewModel = ViewModelProvider(this, viewModelFactory).get(IcalListViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner

        // add menu
        setHasOptionsMenu(true)

        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())


        // set up recycler view
        recyclerView = binding.vjournalListItemsRecyclerView
        linearLayoutManager = LinearLayoutManager(application.applicationContext)
        //staggeredGridLayoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        //recyclerView?.layoutManager = staggeredGridLayoutManager
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.setHasFixedSize(false)

        // create adapter and provide data
        //icalListAdapter = IcalListAdapter(application.applicationContext, icalListViewModel)
        journalListAdapter = IcalListAdapterJournal(requireContext(), icalListViewModel)
        noteListAdapter = IcalListAdapterNote(requireContext(), icalListViewModel)
        todoListAdapter = IcalListAdapterTodo(requireContext(), icalListViewModel)

        when(icalListViewModel.searchModule) {
            Module.JOURNAL.name -> recyclerView?.adapter = journalListAdapter
            Module.NOTE.name -> recyclerView?.adapter = noteListAdapter
            Module.TODO.name -> recyclerView?.adapter = todoListAdapter
        }

        //recyclerView?.adapter = icalListAdapter


        arguments = IcalListFragmentArgs.fromBundle((requireArguments()))
        prefs = requireActivity().getSharedPreferences(PREFS_LIST_VIEW, Context.MODE_PRIVATE)

        loadFilterArgsAndPrefs()


        // Observe the vjournalList for Changes, on any change the recycler view must be updated, additionally the Focus Item might be updated
        icalListViewModel.iCal4List.observe(viewLifecycleOwner, {

            binding.listProgressIndicator.visibility = View.GONE

            // if the hash is the same as before, the list has not changed and we don't continue (such triggers happen regularly when DAVx5 is doing the sync)
            // searchStatusTodo needs special handling, if it was changed (although the basic list might be the same), the subtasks might change and they
            // need to be rebuilt with the new setting
            if(lastIcal4ListHash == it.hashCode() && lastSearchStatusTodo == icalListViewModel.searchStatusTodo)
                return@observe
            lastIcal4ListHash = it.hashCode()
            lastSearchStatusTodo = icalListViewModel.searchStatusTodo


            recyclerView?.adapter?.notifyDataSetChanged()

            if(lastSearchModule != icalListViewModel.searchModule)          // we do the animation only if the module was changed. Otherwise animation would also be done when e.g. a progress is changed.
                recyclerView?.scheduleLayoutAnimation()
            lastSearchModule = icalListViewModel.searchModule               // remember the last list size and search module

            updateMenuVisibilities()

            //TODO: Check if this is still ok here!
            icalListViewModel.resetFocusItem()              // reset happens only once in a Module, only when the Module get's changed the scrolling would happen again
            icalListViewModel.focusItemId.value = arguments.item2focus
        })

        // This observer is needed in order to make sure that the Subtasks are retrieved!
        icalListViewModel.allSubtasks.observe(viewLifecycleOwner, {
            if(it.isNotEmpty())
                recyclerView?.adapter?.notifyDataSetChanged()
        })


        // Observe the focus item to scroll automatically to the right position (newly updated or inserted item)
        icalListViewModel.focusItemId.observe(viewLifecycleOwner, {

            // don't scroll if the item is not set or if scrolling was already done for this Module (this avoids jumping around when the live data with ical-entries changes e.g. through updates or the sync)
            if (it != null && lastScrolledFocusItemId != it) {
                val pos = icalListViewModel.getFocusItemPosition()
                if(pos>0) {
                    linearLayoutManager!!.scrollToPositionWithOffset(pos,0)   // offset was necessary for descending sorting, this was changed... offset makes the item always appear 20px from the top (instead of recyclerView?.scrollToPosition(pos)   )
                    //linearLayoutManager!!.smoothScrollToPosition(recyclerView, RecyclerView.State(), pos)
                    //TODO: Try smoothScrollToPosition again as now the items are sorted ascending
                    lastScrolledFocusItemId = it
                }
            }
        })

        // observe to make sure that it gets updated
        icalListViewModel.allRemoteCollections.observe(viewLifecycleOwner, {
            // check if any accounts were removed, retrieve all DAVx5 Accounts
            val accounts = AccountManager.get(context).getAccountsByType(ICalCollection.DAVX5_ACCOUNT_TYPE)
            icalListViewModel.removeDeletedAccounts(accounts)
        })

        binding.tablayoutJournalnotestodos.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {

                binding.listProgressIndicator.visibility = View.VISIBLE
                //resetFilter(false)

                when (tab?.position) {
                    TAB_INDEX_JOURNAL -> icalListViewModel.searchModule = Module.JOURNAL.name
                    TAB_INDEX_NOTE -> icalListViewModel.searchModule = Module.NOTE.name
                    TAB_INDEX_TODO -> icalListViewModel.searchModule = Module.TODO.name
                }

                loadFiltersFromPrefs()
                lastScrolledFocusItemId = null

                applyFilters()

            }
            override fun onTabUnselected(tab: TabLayout.Tab?) { }    // nothing to do
            override fun onTabReselected(tab: TabLayout.Tab?) {
                lastScrolledFocusItemId = null        // makes the view scroll again when the tab is clicked again
                icalListViewModel.resetFocusItem()
            }
        })

        binding.listBottomBar.setOnMenuItemClickListener { menuitem ->

            when (menuitem.itemId) {
                R.id.menu_list_bottom_filter -> goToFilter()
                R.id.menu_list_bottom_clearfilter -> resetFilter()
                R.id.menu_list_bottom_add_journal -> goToEdit(ICalEntity(ICalObject.createJournal()))
                R.id.menu_list_bottom_add_note -> goToEdit(ICalEntity(ICalObject.createNote()))
                R.id.menu_list_bottom_add_todo -> goToEdit(ICalEntity(ICalObject.createTodo()))
                R.id.menu_list_bottom_hide_completed_tasks -> applyQuickFilterTodo(mutableListOf(StatusTodo.`NEEDS-ACTION`, StatusTodo.`IN-PROCESS`))
                R.id.menu_list_bottom_show_completed_tasks -> applyQuickFilterTodo(mutableListOf())
            }
            false
        }

        // update the recycler view as the user might have changed the setting to show subtasks
        prefs.registerOnSharedPreferenceChangeListener { _, _ ->
            recyclerView?.adapter?.notifyDataSetChanged()
        }

        return binding.root
    }



    override fun onResume() {

        icalListViewModel.searchSettingShowSubtasksOfVJOURNALs = settings?.getBoolean("settings_show_subtasks_of_VJOURNALs_in_tasklist", false) ?: false
        applyFilters()
        updateMenuVisibilities()
        super.onResume()
    }

    private fun updateToolbarText() {
        try {
            val activity = requireActivity() as MainActivity
            val toolbarText = getString(R.string.toolbar_text_jtx_board)
            val toolbarSubtitle = when(icalListViewModel.searchModule) {
                Module.JOURNAL.name -> getString(R.string.toolbar_text_jtx_board_journals_overview)
                Module.NOTE.name -> getString(R.string.toolbar_text_jtx_board_notes_overview)
                Module.TODO.name -> getString(R.string.toolbar_text_jtx_board_tasks_overview)
                else -> null
            }

            activity.setToolbarTitle(toolbarText, toolbarSubtitle )
        } catch (e: ClassCastException) {
            Log.d("setToolbarText", "Class cast to MainActivity failed (this is common for tests but doesn't really matter)\n$e")
        }
    }


    /**
     * This function hides/shows the relevant menu entries for the active module.
     */
    private fun updateMenuVisibilities() {

        when (icalListViewModel.searchModule) {
            Module.JOURNAL.name -> {
                gotodateMenuItem?.isVisible = true
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vjournal_drafts)?.isVisible = true
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vjournal_drafts_final)?.isVisible = true
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vjournal_final)?.isVisible = true
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vtodo_exclude_cancelled)?.isVisible = false
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vtodo_completed)?.isVisible = false
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vtodo_open_inprogress)?.isVisible = false

                binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_add_journal).isVisible = false
                binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_add_note).isVisible = true
                binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_add_todo).isVisible = true

                binding.fab.setImageResource(R.drawable.ic_add)
                binding.fab.setOnClickListener { goToEdit(ICalEntity(ICalObject.createJournal())) }
            }
            Module.NOTE.name -> {
                gotodateMenuItem?.isVisible = false
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vjournal_drafts)?.isVisible = true
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vjournal_drafts_final)?.isVisible = true
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vjournal_final)?.isVisible = true
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vtodo_exclude_cancelled)?.isVisible = false
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vtodo_completed)?.isVisible = false
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vtodo_open_inprogress)?.isVisible = false

                binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_add_journal).isVisible = true
                binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_add_note).isVisible = false
                binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_add_todo).isVisible = true

                binding.fab.setImageResource(R.drawable.ic_add_note)
                binding.fab.setOnClickListener { goToEdit(ICalEntity(ICalObject.createNote())) }
            }
            Module.TODO.name -> {
                gotodateMenuItem?.isVisible = false
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vjournal_drafts)?.isVisible = false
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vjournal_drafts_final)?.isVisible = false
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vjournal_final)?.isVisible = false
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vtodo_exclude_cancelled)?.isVisible = true
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vtodo_completed)?.isVisible = true
                optionsMenu?.findItem(R.id.menu_list_quickfilterfilter_vtodo_open_inprogress)?.isVisible = true

                binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_add_journal).isVisible = true
                binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_add_note).isVisible = true
                binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_add_todo).isVisible = false

                binding.fab.setImageResource(R.drawable.ic_todo_add)
                binding.fab.setOnClickListener { goToEdit(ICalEntity(ICalObject.createTodo())) }
            }
        }

        if(isHideCompletedTasksFilterActive()) {
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_show_completed_tasks).isVisible = true
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_hide_completed_tasks).isVisible = false
        } else {
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_show_completed_tasks).isVisible = false
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_hide_completed_tasks).isVisible = true
        }

        // don't show the option to clear the filter if no filter was set
        if (!isFilterActive()) {
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter)?.isVisible = true
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_clearfilter)?.isVisible = false
        } else {
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter)?.isVisible = false
            binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_clearfilter)?.isVisible = true
        }

    }


    /**
     * This function sets the initial search criteria and retrieves them either from args or from
     * the preferences. This method should be called on Create to make sure that the
     * filter criteria in args are correctly loaded. Once this is done, the filter criteria
     * will be saved in preferences and the args are not necessary anymore. loadFiltersFromPrefs()
     * should be used then.
     */
    private fun loadFilterArgsAndPrefs() {
        // check first if the arguments contain the search-property, if not, check in prefs, if this is also null, return a default value
        icalListViewModel.searchModule = arguments.module2show ?: prefs.getString(PREFS_MODULE, null) ?: Module.JOURNAL.name

        when(icalListViewModel.searchModule) {
            Module.JOURNAL.name -> {
                icalListViewModel.searchCategories = arguments.category2filter?.toMutableList() ?: prefs.getStringSet(PREFS_JOURNAL_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchCollection = arguments.collection2filter?.toMutableList() ?: prefs.getStringSet(PREFS_JOURNAL_COLLECTION, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchStatusJournal = arguments.statusJournal2filter?.toMutableList() ?: StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_JOURNAL_STATUS_JOURNAL, null))
                icalListViewModel.searchStatusTodo = arguments.statusTodo2filter?.toMutableList() ?: StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_JOURNAL_STATUS_TODO, null))
                icalListViewModel.searchClassification = arguments.classification2filter?.toMutableList() ?: Classification.getListFromStringList(prefs.getStringSet(PREFS_JOURNAL_CLASSIFICATION, null))
            }
            Module.NOTE.name -> {
                icalListViewModel.searchCategories = arguments.category2filter?.toMutableList() ?: prefs.getStringSet(PREFS_NOTE_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchCollection = arguments.collection2filter?.toMutableList() ?: prefs.getStringSet(PREFS_NOTE_COLLECTION, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchStatusJournal = arguments.statusJournal2filter?.toMutableList() ?: StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_NOTE_STATUS_JOURNAL, null))
                icalListViewModel.searchStatusTodo = arguments.statusTodo2filter?.toMutableList() ?: StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_NOTE_STATUS_TODO, null))
                icalListViewModel.searchClassification = arguments.classification2filter?.toMutableList() ?: Classification.getListFromStringList(prefs.getStringSet(PREFS_NOTE_CLASSIFICATION, null))

            }
            Module.TODO.name -> {
                icalListViewModel.searchCategories = arguments.category2filter?.toMutableList() ?: prefs.getStringSet(PREFS_TODO_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchCollection = arguments.collection2filter?.toMutableList() ?: prefs.getStringSet(PREFS_TODO_COLLECTION, null)?.toMutableList() ?: mutableListOf()
                //icalListViewModel.searchStatusJournal = arguments.statusJournal2filter?.toMutableList() ?: StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_TODO_STATUS_JOURNAL, null))
                icalListViewModel.searchStatusTodo = arguments.statusTodo2filter?.toMutableList() ?: StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_TODO_STATUS_TODO, null))
                icalListViewModel.searchClassification = arguments.classification2filter?.toMutableList() ?: Classification.getListFromStringList(prefs.getStringSet(PREFS_TODO_CLASSIFICATION, null))
            }
        }

        icalListViewModel.focusItemId.value = arguments.item2focus    // or null

        // activate the right tab according to the searchModule
        when (icalListViewModel.searchModule) {
            Module.JOURNAL.name -> binding.tablayoutJournalnotestodos.getTabAt(TAB_INDEX_JOURNAL)?.select()
            Module.NOTE.name -> binding.tablayoutJournalnotestodos.getTabAt(TAB_INDEX_NOTE)?.select()
            Module.TODO.name -> binding.tablayoutJournalnotestodos.getTabAt(TAB_INDEX_TODO)?.select()
        }
    }

    /**
     * This function loads and sets the filter criteria saved in the preferences for the active module.
     * This makes sure that for each module the last used filter is applied again until the user
     * deletes them. This method can be called on every tab change (unlike loadFilterArgsAndPrefs() that
     * also checks if there are filter criteria in the arguments)
     */
    private fun loadFiltersFromPrefs() {

        prefs = requireActivity().getSharedPreferences(PREFS_LIST_VIEW, Context.MODE_PRIVATE)

        when(icalListViewModel.searchModule) {
            Module.JOURNAL.name -> {
                icalListViewModel.searchCategories = prefs.getStringSet(PREFS_JOURNAL_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchCollection = prefs.getStringSet(PREFS_JOURNAL_COLLECTION, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchStatusJournal = StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_JOURNAL_STATUS_JOURNAL, null))
                icalListViewModel.searchStatusTodo = StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_JOURNAL_STATUS_TODO, null))
                icalListViewModel.searchClassification = Classification.getListFromStringList(prefs.getStringSet(PREFS_JOURNAL_CLASSIFICATION, null))
            }
            Module.NOTE.name -> {
                icalListViewModel.searchCategories = prefs.getStringSet(PREFS_NOTE_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchCollection = prefs.getStringSet(PREFS_NOTE_COLLECTION, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchStatusJournal = StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_NOTE_STATUS_JOURNAL, null))
                icalListViewModel.searchStatusTodo = StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_NOTE_STATUS_TODO, null))
                icalListViewModel.searchClassification = Classification.getListFromStringList(prefs.getStringSet(PREFS_NOTE_CLASSIFICATION, null))

            }
            Module.TODO.name -> {
                icalListViewModel.searchCategories = prefs.getStringSet(PREFS_TODO_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchCollection = prefs.getStringSet(PREFS_TODO_COLLECTION, null)?.toMutableList() ?: mutableListOf()
                //icalListViewModel.searchStatusJournal = StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_TODO_STATUS_JOURNAL, null))
                icalListViewModel.searchStatusTodo = StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_TODO_STATUS_TODO, null))
                icalListViewModel.searchClassification = Classification.getListFromStringList(prefs.getStringSet(PREFS_TODO_CLASSIFICATION, null))
            }
        }
    }


    fun applyFilters() {

        binding.listProgressIndicator.visibility = View.VISIBLE
        updateToolbarText()
        icalListViewModel.updateSearch()
        savePrefs()
        icalListViewModel.resetFocusItem()

        when(icalListViewModel.searchModule) {
            Module.JOURNAL.name -> recyclerView?.adapter = journalListAdapter
            Module.NOTE.name -> recyclerView?.adapter = noteListAdapter
            Module.TODO.name -> recyclerView?.adapter = todoListAdapter
        }

        //change background
        when (icalListViewModel.searchModule) {
            Module.JOURNAL.name -> binding.listBackground.setImageResource(R.drawable.bg_journals)
            Module.NOTE.name -> binding.listBackground.setImageResource(R.drawable.bg_notes)
            Module.TODO.name -> binding.listBackground.setImageResource(R.drawable.bg_todos)
            else -> binding.listBackground.setImageResource(R.drawable.bg_journals)
        }
    }

    private fun savePrefs() {
        when (icalListViewModel.searchModule) {
            Module.JOURNAL.name -> {
                prefs.edit().putStringSet(PREFS_JOURNAL_COLLECTION, icalListViewModel.searchCollection.toSet()).apply()
                prefs.edit().putStringSet(PREFS_JOURNAL_STATUS_JOURNAL, StatusJournal.getStringSetFromList(icalListViewModel.searchStatusJournal)).apply()
                prefs.edit().putStringSet(PREFS_JOURNAL_STATUS_TODO, StatusTodo.getStringSetFromList(icalListViewModel.searchStatusTodo)).apply()
                prefs.edit().putStringSet(PREFS_JOURNAL_CLASSIFICATION, Classification.getStringSetFromList(icalListViewModel.searchClassification)).apply()
                prefs.edit().putStringSet(PREFS_JOURNAL_CATEGORIES, icalListViewModel.searchCategories.toSet()).apply()
            }
            Module.NOTE.name -> {
                prefs.edit().putStringSet(PREFS_NOTE_COLLECTION, icalListViewModel.searchCollection.toSet()).apply()
                prefs.edit().putStringSet(PREFS_NOTE_STATUS_JOURNAL, StatusJournal.getStringSetFromList(icalListViewModel.searchStatusJournal)).apply()
                prefs.edit().putStringSet(PREFS_NOTE_STATUS_TODO, StatusTodo.getStringSetFromList(icalListViewModel.searchStatusTodo)).apply()
                prefs.edit().putStringSet(PREFS_NOTE_CLASSIFICATION, Classification.getStringSetFromList(icalListViewModel.searchClassification)).apply()
                prefs.edit().putStringSet(PREFS_NOTE_CATEGORIES, icalListViewModel.searchCategories.toSet()).apply()
            }
            Module.TODO.name -> {
                prefs.edit().putStringSet(PREFS_TODO_COLLECTION, icalListViewModel.searchCollection.toSet()).apply()
                //prefs.edit().putStringSet(PREFS_TODO_STATUS_JOURNAL, StatusJournal.getStringSetFromList(icalListViewModel.searchStatusJournal)).apply()
                prefs.edit().putStringSet(PREFS_TODO_STATUS_TODO, StatusTodo.getStringSetFromList(icalListViewModel.searchStatusTodo)).apply()
                prefs.edit().putStringSet(PREFS_TODO_CLASSIFICATION, Classification.getStringSetFromList(icalListViewModel.searchClassification)).apply()
                prefs.edit().putStringSet(PREFS_TODO_CATEGORIES, icalListViewModel.searchCategories.toSet()).apply()
            }
        }
        prefs.edit().putString(PREFS_MODULE, icalListViewModel.searchModule).apply()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_ical_list, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
        optionsMenu = menu
        gotodateMenuItem = menu.findItem(R.id.menu_list_gotodate)         // Tell the variable the menu item to later make it visible or invisible

        // add listener for search!
        val searchMenuItem = menu.findItem(R.id.menu_list_search)
        val searchView = searchMenuItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            // nothing to do as the the search is already updated with the text input
            override fun onQueryTextSubmit(query: String): Boolean { return false }

            override fun onQueryTextChange(query: String): Boolean {

                if (query.isEmpty())
                    icalListViewModel.searchText = "%"
                else
                    icalListViewModel.searchText = "%$query%"

                icalListViewModel.updateSearch()
                return false
            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_list_gotodate -> scrollToDate()
            R.id.menu_list_filter -> goToFilter()
            R.id.menu_list_clearfilter -> resetFilter()
            R.id.menu_list_add_journal -> goToEdit(ICalEntity(ICalObject.createJournal()))
            R.id.menu_list_add_note -> goToEdit(ICalEntity(ICalObject.createNote()))
            R.id.menu_list_add_todo -> goToEdit(ICalEntity(ICalObject.createTodo()))
            R.id.menu_list_quickfilterfilter_vjournal_drafts_final -> this.applyQuickFilterJournal(mutableListOf(StatusJournal.DRAFT, StatusJournal.FINAL))
            R.id.menu_list_quickfilterfilter_vjournal_drafts -> this.applyQuickFilterJournal(mutableListOf(StatusJournal.DRAFT))
            R.id.menu_list_quickfilterfilter_vjournal_final -> this.applyQuickFilterJournal(mutableListOf(StatusJournal.FINAL))
            R.id.menu_list_quickfilterfilter_vtodo_exclude_cancelled -> applyQuickFilterTodo(mutableListOf(StatusTodo.`NEEDS-ACTION`, StatusTodo.`IN-PROCESS`, StatusTodo.COMPLETED))
            R.id.menu_list_quickfilterfilter_vtodo_open_inprogress -> applyQuickFilterTodo(mutableListOf(StatusTodo.`NEEDS-ACTION`, StatusTodo.`IN-PROCESS`))
            R.id.menu_list_quickfilterfilter_vtodo_completed -> applyQuickFilterTodo(mutableListOf(StatusTodo.COMPLETED))
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * Resets the focus item
     * Clears all filter criteria
     * Clears the preferences with the saved search criteria
     */
    private fun resetFilter() {

        icalListViewModel.resetFocusItem()
        icalListViewModel.clearFilter()

        when (icalListViewModel.searchModule) {
            Module.JOURNAL.name -> {
                prefs.edit().remove(PREFS_JOURNAL_COLLECTION).apply()
                prefs.edit().remove(PREFS_JOURNAL_STATUS_JOURNAL).apply()
                prefs.edit().remove(PREFS_JOURNAL_STATUS_TODO).apply()
                prefs.edit().remove(PREFS_JOURNAL_CLASSIFICATION).apply()
                prefs.edit().remove(PREFS_JOURNAL_CATEGORIES).apply()
            }
            Module.NOTE.name -> {
                prefs.edit().remove(PREFS_NOTE_COLLECTION).apply()
                prefs.edit().remove(PREFS_NOTE_STATUS_JOURNAL).apply()
                prefs.edit().remove(PREFS_NOTE_STATUS_TODO).apply()
                prefs.edit().remove(PREFS_NOTE_CLASSIFICATION).apply()
                prefs.edit().remove(PREFS_NOTE_CATEGORIES).apply()
            }
            Module.TODO.name -> {
                prefs.edit().remove(PREFS_TODO_COLLECTION).apply()
                //prefs.edit().remove(PREFS_TODO_STATUS_JOURNAL).apply()
                prefs.edit().remove(PREFS_TODO_STATUS_TODO).apply()
                prefs.edit().remove(PREFS_TODO_CLASSIFICATION).apply()
                prefs.edit().remove(PREFS_TODO_CATEGORIES).apply()
            }
        }
    }

    private fun goToFilter() {
        icalListViewModel.resetFocusItem()

        this.findNavController().navigate(
            IcalListFragmentDirections.actionIcalListFragmentToIcalFilterFragment().apply {
                this.category2preselect = icalListViewModel.searchCategories.toTypedArray()
                this.statusJournal2preselect = icalListViewModel.searchStatusJournal.toTypedArray()
                this.statusTodo2preselect = icalListViewModel.searchStatusTodo.toTypedArray()
                this.classification2preselect = icalListViewModel.searchClassification.toTypedArray()
                this.collection2preselect = icalListViewModel.searchCollection.toTypedArray()
                this.module2preselect = icalListViewModel.searchModule
            })
    }

    private fun goToEdit(iCalObject: ICalEntity) {
        this.findNavController().navigate(IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(iCalObject))
    }

    private fun applyQuickFilterJournal(statusList: MutableList<StatusJournal>) {

        //resetFilter(false)
        icalListViewModel.searchStatusJournal = statusList
        applyFilters()
    }

    private fun applyQuickFilterTodo(statusList: MutableList<StatusTodo>) {

        icalListViewModel.resetFocusItem()
        icalListViewModel.searchStatusTodo = statusList
        applyFilters()
    }

    /**
     * Returns true if the Filter for Status Todos "In process" and "Needs action" is active
     */
    private fun isHideCompletedTasksFilterActive() = icalListViewModel.searchStatusTodo.size == 2 && icalListViewModel.searchStatusTodo.containsAll(listOf(StatusTodo.`IN-PROCESS`, StatusTodo.`NEEDS-ACTION`))



    private fun scrollToDate() {

        // Create a custom date validator to only enable dates that are in the list
        val customDateValidator = object : CalendarConstraints.DateValidator {
            override fun describeContents(): Int {
                return 0
            }

            override fun writeToParcel(dest: Parcel?, flags: Int) {}
            override fun isValid(date: Long): Boolean {

                icalListViewModel.iCal4List.value?.forEach {
                    val itemDateTime = Calendar.getInstance()
                    itemDateTime.timeInMillis =
                        it.property.dtstart ?: System.currentTimeMillis()

                    val dateDateTime = Calendar.getInstance()
                    dateDateTime.timeInMillis = date

                    if (itemDateTime.get(Calendar.YEAR) == dateDateTime.get(Calendar.YEAR)
                        && itemDateTime.get(Calendar.MONTH) == dateDateTime.get(Calendar.MONTH)
                        && itemDateTime.get(Calendar.DAY_OF_MONTH) == dateDateTime.get(
                            Calendar.DAY_OF_MONTH
                        )
                    )
                        return true
                }
                return false
            }
        }

        // Build constraints.
        val constraintsBuilder =
            CalendarConstraints.Builder().apply {

                val startItem = icalListViewModel.iCal4List.value?.firstOrNull()
                val endItem = icalListViewModel.iCal4List.value?.lastOrNull()

                if (startItem?.property?.dtstart != null && endItem?.property?.dtstart != null) {
                    setStart(startItem.property.dtstart!!)
                    setEnd(endItem.property.dtstart!!)
                    setValidator(customDateValidator)
                }
            }

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

        datePicker.addOnPositiveButtonClickListener {
            // Respond to positive button click.

            // create a Calendar Object out of the selected dates
            val selectedDate = Calendar.getInstance()
            selectedDate.timeInMillis = it

            // find the item with the same date
            val foundItem = icalListViewModel.iCal4List.value?.find { item ->
                val cItem = Calendar.getInstance()
                cItem.timeInMillis = item.property.dtstart ?: 0L

                // if this condition is true, the item is considered as found
                cItem.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
                        && cItem.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)
                        && cItem.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
            }
            if (foundItem != null)
                icalListViewModel.focusItemId.value = foundItem.property.id
        }

        datePicker.show(parentFragmentManager, "menu_list_gotodate")
    }

    private fun isFilterActive() = icalListViewModel.searchCategories.isNotEmpty() || icalListViewModel.searchOrganizer.isNotEmpty() || (icalListViewModel.searchModule == Module.JOURNAL.name && icalListViewModel.searchStatusJournal.isNotEmpty()) || (icalListViewModel.searchModule == Module.NOTE.name && icalListViewModel.searchStatusJournal.isNotEmpty()) || (icalListViewModel.searchModule == Module.TODO.name && icalListViewModel.searchStatusTodo.isNotEmpty()) || icalListViewModel.searchClassification.isNotEmpty() || icalListViewModel.searchCollection.isNotEmpty()


}
