/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui


import android.accounts.AccountManager
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.techbee.jtx.monetization.AdManager
import at.techbee.jtx.MainActivity
import at.techbee.jtx.PermissionsHelper
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.databinding.FragmentIcalListBinding
import at.techbee.jtx.databinding.FragmentIcalListQuickaddDialogBinding
import at.techbee.jtx.monetization.BillingManager
import at.techbee.jtx.util.DateTimeUtils.requireTzId
import at.techbee.jtx.util.SyncUtil
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import java.lang.ClassCastException
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


class IcalListFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    //private var staggeredGridLayoutManager: StaggeredGridLayoutManager? = null

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

        const val PREFS_LAST_USED_COLLECTION = "lastUsedCollection"
        const val PREFS_MODULE = "prefsModule"
        const val PREFS_ISFIRSTRUN = "isFirstRun"

        //Journal
        const val PREFS_JOURNAL_COLLECTION = "prefsJournalCollection"
        const val PREFS_JOURNAL_ACCOUNT = "prefsJournalAccount"
        const val PREFS_JOURNAL_CATEGORIES = "prefsJournalCategories"
        const val PREFS_JOURNAL_CLASSIFICATION = "prefsJournalClassification"
        const val PREFS_JOURNAL_STATUS_JOURNAL = "prefsJournalStatusJournal"
        const val PREFS_JOURNAL_STATUS_TODO = "prefsJournalStatusTodo"
        const val PREFS_JOURNAL_EXCLUDE_DONE = "prefsJournalExcludeDone"

        //Note
        const val PREFS_NOTE_COLLECTION = "prefsNoteCollection"
        const val PREFS_NOTE_ACCOUNT = "prefsNoteAccount"
        const val PREFS_NOTE_CATEGORIES = "prefsNoteCategories"
        const val PREFS_NOTE_CLASSIFICATION = "prefsNoteClassification"
        const val PREFS_NOTE_STATUS_JOURNAL = "prefsNoteStatusJournal"
        const val PREFS_NOTE_STATUS_TODO = "prefsNoteStatusTodo"
        const val PREFS_NOTE_EXCLUDE_DONE = "prefsNoteExcludeDone"
        //Todos
        const val PREFS_TODO_COLLECTION = "prefsTodoCollection"
        const val PREFS_TODO_ACCOUNT = "prefsTodoAccount"
        const val PREFS_TODO_CATEGORIES = "prefsTodoCategories"
        const val PREFS_TODO_CLASSIFICATION = "prefsTodoClassification"
        //const val PREFS_TODO_STATUS_JOURNAL = "prefsTodoStatusJournal"
        const val PREFS_TODO_STATUS_TODO = "prefsTodoStatusTodo"
        const val PREFS_TODO_EXCLUDE_DONE = "prefsTodoExcludeDone"
        const val PREFS_TODO_FILTER_OVERDUE = "prefsTodoFilterOverdue"
        const val PREFS_TODO_FILTER_DUE_TODAY = "prefsTodoFilterToday"
        const val PREFS_TODO_FILTER_DUE_TOMORROW = "prefsTodoFilterTomorrow"
        const val PREFS_TODO_FILTER_DUE_FUTURE = "prefsTodoFilterFuture"


        const val SETTINGS_SHOW_SUBTASKS_IN_LIST = "settings_show_subtasks_of_journals_and_todos_in_tasklist"
        const val SETTINGS_SHOW_SUBNOTES_IN_LIST = "settings_show_subnotes_of_journals_and_tasks_in_noteslist"
        const val SETTINGS_SHOW_SUBJOURNALS_IN_LIST = "settings_show_subjournals_of_notes_and_tasks_in_journallist"


        const val TAB_INDEX_JOURNAL = 0
        const val TAB_INDEX_NOTE = 1
        const val TAB_INDEX_TODO = 2
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        binding = FragmentIcalListBinding.inflate(inflater, container, false)

        application = requireNotNull(this.activity).application
        dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao


        // create the view model through the view model factory
        val viewModelFactory = IcalListViewModelFactory(dataSource, application)
        icalListViewModel = ViewModelProvider(this, viewModelFactory)[IcalListViewModel::class.java]

        binding.lifecycleOwner = viewLifecycleOwner

        // add menu
        setHasOptionsMenu(true)

        settings = PreferenceManager.getDefaultSharedPreferences(context)

        // set up recycler view
        recyclerView = binding.vjournalListItemsRecyclerView
        linearLayoutManager = LinearLayoutManager(application.applicationContext)
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.setHasFixedSize(false)


        arguments = IcalListFragmentArgs.fromBundle((requireArguments()))
        prefs = requireActivity().getSharedPreferences(PREFS_LIST_VIEW, Context.MODE_PRIVATE)

        loadFilterArgsAndPrefs()

        // only ad the welcomeEntries on first install and exclude all installs that didn't have this preference before (installed before 1641596400000L = 2022/01/08
        val firstInstall = context?.packageManager?.getPackageInfo(requireContext().packageName, 0)?.firstInstallTime ?: System.currentTimeMillis()
        if(prefs.getBoolean(PREFS_ISFIRSTRUN, true)) {
            if (firstInstall > 1641596400000L)
                icalListViewModel.addWelcomeEntries(requireContext())
            prefs.edit().putBoolean(PREFS_ISFIRSTRUN, false).apply()
        }


        icalListViewModel.iCal4List.observe(viewLifecycleOwner) {

            //updateMenuVisibilities()
            binding.listProgressIndicator.visibility = View.GONE

            // if the hash is the same as before, the list has not changed and we don't continue (such triggers happen regularly when DAVx5 is doing the sync)
            // searchStatusTodo needs special handling, if it was changed (although the basic list might be the same), the subtasks might change and they
            // need to be rebuilt with the new setting
            if (lastIcal4ListHash == it.hashCode() && lastSearchStatusTodo == icalListViewModel.searchStatusTodo)
                return@observe
            lastIcal4ListHash = it.hashCode()
            lastSearchStatusTodo = icalListViewModel.searchStatusTodo

            //recyclerView?.adapter?.notifyDataSetChanged()
            // instead of notifyDataSetChanged, we initialize the recycler view from scratch in the observer (instead of onCreateView)
            // this seems to be more efficient and also makes sure that the list gets purged before it's rebuild and avoids strange layout behaviours
            when (icalListViewModel.searchModule) {
                Module.JOURNAL.name -> recyclerView?.adapter = IcalListAdapterJournal(requireContext(), icalListViewModel)
                Module.NOTE.name -> recyclerView?.adapter = IcalListAdapterNote(requireContext(), icalListViewModel)
                Module.TODO.name -> recyclerView?.adapter = IcalListAdapterTodo(requireContext(), icalListViewModel)
            }

            if (lastSearchModule != icalListViewModel.searchModule)          // we do the animation only if the module was changed. Otherwise animation would also be done when e.g. a progress is changed.
                recyclerView?.scheduleLayoutAnimation()
            lastSearchModule = icalListViewModel.searchModule               // remember the last list size and search module

            updateMenuVisibilities()

            if (icalListViewModel.scrollOnceId != null)
                scrollOnce()

        }

        // This observer is needed in order to make sure that the Subtasks are retrieved!
        icalListViewModel.allSubtasks.observe(viewLifecycleOwner) {
            //if(it.isNotEmpty())
            //recyclerView?.adapter?.notifyDataSetChanged()
            // trying to skip this code. This might have the effect, that subtasks that are added during the sync might not be immediately available, but improves the performance as the list does not get updated all the time
        }


        // observe to make sure that it gets updated
        icalListViewModel.allRemoteCollections.observe(viewLifecycleOwner) {
            // check if any accounts were removed, retrieve all DAVx5 Accounts
            val accounts = AccountManager.get(context).getAccountsByType(ICalCollection.DAVX5_ACCOUNT_TYPE)
            icalListViewModel.removeDeletedAccounts(accounts)
        }


        // observe the directEditEntity. This is set in the Adapter on long click through the model. On long click we forward the user directly to the edit fragment
        icalListViewModel.directEditEntity.observe(viewLifecycleOwner) {
            if (it == null)
                return@observe
            icalListViewModel.directEditEntity.removeObservers(viewLifecycleOwner)
            icalListViewModel.directEditEntity.value = null      // invalidate so that on click on back, the value is empty and doesn't create unexpected behaviour!
            this.findNavController().navigate(IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(it))
        }


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
            override fun onTabReselected(tab: TabLayout.Tab?) { }
        })

        binding.listBottomBar.setOnMenuItemClickListener { menuitem ->

            when (menuitem.itemId) {
                R.id.menu_list_bottom_filter -> goToFilter()
                R.id.menu_list_bottom_clearfilter -> resetFilter()
                R.id.menu_list_bottom_quick_journal -> showQuickAddDialog()
                R.id.menu_list_bottom_quick_note -> showQuickAddDialog()
                R.id.menu_list_bottom_quick_todo -> showQuickAddDialog()
                R.id.menu_list_bottom_toggle_completed_tasks -> toggleMenuCheckboxFilter(menuitem)
                R.id.menu_list_bottom_filter_overdue -> toggleMenuCheckboxFilter(menuitem)
                R.id.menu_list_bottom_filter_due_today -> toggleMenuCheckboxFilter(menuitem)
                R.id.menu_list_bottom_filter_due_tomorrow -> toggleMenuCheckboxFilter(menuitem)
                R.id.menu_list_bottom_filter_due_in_future -> toggleMenuCheckboxFilter(menuitem)
            }
            false
        }


        ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE or ContentResolver.SYNC_OBSERVER_TYPE_PENDING) {
            icalListViewModel.showSyncProgressIndicator.postValue(
                ContentResolver.getCurrentSyncs().isNotEmpty()
            )
        }

        icalListViewModel.showSyncProgressIndicator.observe(viewLifecycleOwner) {
            if(it)
                binding.listSyncProgressIndicator.visibility = View.VISIBLE
            else
                binding.listSyncProgressIndicator.visibility = View.INVISIBLE
        }


        return binding.root
    }



    override fun onResume() {

        icalListViewModel.searchSettingShowAllSubtasksInTasklist = settings?.getBoolean(SETTINGS_SHOW_SUBTASKS_IN_LIST, false) ?: false
        icalListViewModel.searchSettingShowAllSubnotesInNoteslist = settings?.getBoolean(SETTINGS_SHOW_SUBNOTES_IN_LIST, false) ?: false
        icalListViewModel.searchSettingShowAllSubjournalsinJournallist = settings?.getBoolean(SETTINGS_SHOW_SUBJOURNALS_IN_LIST, false) ?: false

        applyFilters()
        updateMenuVisibilities()

        when(icalListViewModel.searchModule) {
            Module.JOURNAL.name -> recyclerView?.adapter = IcalListAdapterJournal(requireContext(), icalListViewModel)
            Module.NOTE.name -> recyclerView?.adapter = IcalListAdapterNote(requireContext(), icalListViewModel)
            Module.TODO.name -> recyclerView?.adapter = IcalListAdapterTodo(requireContext(), icalListViewModel)
        }

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
                binding.fab.setImageResource(R.drawable.ic_add)
                binding.fab.setOnClickListener { goToEdit(ICalEntity(ICalObject.createJournal())) }
            }
            Module.NOTE.name -> {
                binding.fab.setImageResource(R.drawable.ic_add_note)
                binding.fab.setOnClickListener { goToEdit(ICalEntity(ICalObject.createNote())) }
            }
            Module.TODO.name -> {
                binding.fab.setImageResource(R.drawable.ic_todo_add)
                binding.fab.setOnClickListener { goToEdit(ICalEntity(ICalObject.createTodo())) }
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
        }
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_overdue).isVisible = icalListViewModel.searchModule == Module.TODO.name
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_due_today).isVisible = icalListViewModel.searchModule == Module.TODO.name
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_due_tomorrow).isVisible = icalListViewModel.searchModule == Module.TODO.name
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter_due_in_future).isVisible = icalListViewModel.searchModule == Module.TODO.name


        // don't show the option to clear the filter if no filter was set
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_filter)?.isVisible = !isFilterActive()
        binding.listBottomBar.menu.findItem(R.id.menu_list_bottom_clearfilter)?.isVisible = isFilterActive()

        if(!SyncUtil.isDAVx5CompatibleWithJTX(application))
            optionsMenu?.findItem(R.id.menu_list_syncnow)?.isVisible = false

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
        // The next line is commented out as it has become less useful to use the searchModule from the args. It might make more sense to only use the value in the shared preferences to always return to the list where the user got deeper into details.
        //icalListViewModel.searchModule = arguments.module2show ?: prefs.getString(PREFS_MODULE, null) ?: Module.JOURNAL.name
        icalListViewModel.searchModule = prefs.getString(PREFS_MODULE, null) ?: Module.JOURNAL.name

        when(icalListViewModel.searchModule) {
            Module.JOURNAL.name -> {
                icalListViewModel.searchCategories = arguments.category2filter?.toMutableList() ?: prefs.getStringSet(PREFS_JOURNAL_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchCollection = arguments.collection2filter?.toMutableList() ?: prefs.getStringSet(PREFS_JOURNAL_COLLECTION, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchAccount = arguments.account2filter?.toMutableList() ?: prefs.getStringSet(PREFS_JOURNAL_ACCOUNT, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchStatusJournal = arguments.statusJournal2filter?.toMutableList() ?: StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_JOURNAL_STATUS_JOURNAL, null))
                icalListViewModel.searchStatusTodo = arguments.statusTodo2filter?.toMutableList() ?: StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_JOURNAL_STATUS_TODO, null))
                icalListViewModel.searchClassification = arguments.classification2filter?.toMutableList() ?: Classification.getListFromStringList(prefs.getStringSet(PREFS_JOURNAL_CLASSIFICATION, null))
                icalListViewModel.isExcludeDone = prefs.getBoolean(PREFS_JOURNAL_EXCLUDE_DONE, false)
                icalListViewModel.isFilterOverdue = false
                icalListViewModel.isFilterDueToday = false
                icalListViewModel.isFilterDueTomorrow = false
                icalListViewModel.isFilterDueFuture = false
            }
            Module.NOTE.name -> {
                icalListViewModel.searchCategories = arguments.category2filter?.toMutableList() ?: prefs.getStringSet(PREFS_NOTE_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchCollection = arguments.collection2filter?.toMutableList() ?: prefs.getStringSet(PREFS_NOTE_COLLECTION, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchAccount = arguments.account2filter?.toMutableList() ?: prefs.getStringSet(PREFS_NOTE_ACCOUNT, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchStatusJournal = arguments.statusJournal2filter?.toMutableList() ?: StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_NOTE_STATUS_JOURNAL, null))
                icalListViewModel.searchStatusTodo = arguments.statusTodo2filter?.toMutableList() ?: StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_NOTE_STATUS_TODO, null))
                icalListViewModel.searchClassification = arguments.classification2filter?.toMutableList() ?: Classification.getListFromStringList(prefs.getStringSet(PREFS_NOTE_CLASSIFICATION, null))
                icalListViewModel.isExcludeDone = prefs.getBoolean(PREFS_NOTE_EXCLUDE_DONE, false)
                icalListViewModel.isFilterOverdue = false
                icalListViewModel.isFilterDueToday = false
                icalListViewModel.isFilterDueTomorrow = false
                icalListViewModel.isFilterDueFuture = false
            }
            Module.TODO.name -> {
                icalListViewModel.searchCategories = arguments.category2filter?.toMutableList() ?: prefs.getStringSet(PREFS_TODO_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchCollection = arguments.collection2filter?.toMutableList() ?: prefs.getStringSet(PREFS_TODO_COLLECTION, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchAccount = arguments.account2filter?.toMutableList() ?: prefs.getStringSet(PREFS_TODO_ACCOUNT, null)?.toMutableList() ?: mutableListOf()
                //icalListViewModel.searchStatusJournal = arguments.statusJournal2filter?.toMutableList() ?: StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_TODO_STATUS_JOURNAL, null))
                icalListViewModel.searchStatusTodo = arguments.statusTodo2filter?.toMutableList() ?: StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_TODO_STATUS_TODO, null))
                icalListViewModel.searchClassification = arguments.classification2filter?.toMutableList() ?: Classification.getListFromStringList(prefs.getStringSet(PREFS_TODO_CLASSIFICATION, null))
                icalListViewModel.isExcludeDone = prefs.getBoolean(PREFS_TODO_EXCLUDE_DONE, false)
                icalListViewModel.isFilterOverdue = prefs.getBoolean(PREFS_TODO_FILTER_OVERDUE, false)
                icalListViewModel.isFilterDueToday = prefs.getBoolean(PREFS_TODO_FILTER_DUE_TODAY, false)
                icalListViewModel.isFilterDueTomorrow = prefs.getBoolean(PREFS_TODO_FILTER_DUE_TOMORROW, false)
                icalListViewModel.isFilterDueFuture = prefs.getBoolean(PREFS_TODO_FILTER_DUE_FUTURE, false)
            }
        }

        icalListViewModel.scrollOnceId = arguments.item2focus    // or null

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
                icalListViewModel.searchAccount = prefs.getStringSet(PREFS_JOURNAL_ACCOUNT, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchStatusJournal = StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_JOURNAL_STATUS_JOURNAL, null))
                icalListViewModel.searchStatusTodo = StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_JOURNAL_STATUS_TODO, null))
                icalListViewModel.searchClassification = Classification.getListFromStringList(prefs.getStringSet(PREFS_JOURNAL_CLASSIFICATION, null))
                icalListViewModel.isExcludeDone = prefs.getBoolean(PREFS_JOURNAL_EXCLUDE_DONE, false)
                icalListViewModel.isFilterOverdue = false
                icalListViewModel.isFilterDueToday = false
                icalListViewModel.isFilterDueTomorrow = false
                icalListViewModel.isFilterDueFuture = false
            }
            Module.NOTE.name -> {
                icalListViewModel.searchCategories = prefs.getStringSet(PREFS_NOTE_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchCollection = prefs.getStringSet(PREFS_NOTE_COLLECTION, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchAccount = prefs.getStringSet(PREFS_NOTE_ACCOUNT, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchStatusJournal = StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_NOTE_STATUS_JOURNAL, null))
                icalListViewModel.searchStatusTodo = StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_NOTE_STATUS_TODO, null))
                icalListViewModel.searchClassification = Classification.getListFromStringList(prefs.getStringSet(PREFS_NOTE_CLASSIFICATION, null))
                icalListViewModel.isExcludeDone = prefs.getBoolean(PREFS_NOTE_EXCLUDE_DONE, false)
                icalListViewModel.isFilterOverdue = false
                icalListViewModel.isFilterDueToday = false
                icalListViewModel.isFilterDueTomorrow = false
                icalListViewModel.isFilterDueFuture = false
            }
            Module.TODO.name -> {
                icalListViewModel.searchCategories = prefs.getStringSet(PREFS_TODO_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchCollection = prefs.getStringSet(PREFS_TODO_COLLECTION, null)?.toMutableList() ?: mutableListOf()
                icalListViewModel.searchAccount = prefs.getStringSet(PREFS_TODO_ACCOUNT, null)?.toMutableList() ?: mutableListOf()
                //icalListViewModel.searchStatusJournal = StatusJournal.getListFromStringList(prefs.getStringSet(PREFS_TODO_STATUS_JOURNAL, null))
                icalListViewModel.searchStatusTodo = StatusTodo.getListFromStringList(prefs.getStringSet(PREFS_TODO_STATUS_TODO, null))
                icalListViewModel.searchClassification = Classification.getListFromStringList(prefs.getStringSet(PREFS_TODO_CLASSIFICATION, null))
                icalListViewModel.isExcludeDone = prefs.getBoolean(PREFS_TODO_EXCLUDE_DONE, false)
                icalListViewModel.isFilterOverdue = prefs.getBoolean(PREFS_TODO_FILTER_OVERDUE, false)
                icalListViewModel.isFilterDueToday = prefs.getBoolean(PREFS_TODO_FILTER_DUE_TODAY, false)
                icalListViewModel.isFilterDueTomorrow = prefs.getBoolean(PREFS_TODO_FILTER_DUE_TOMORROW, false)
                icalListViewModel.isFilterDueFuture = prefs.getBoolean(PREFS_TODO_FILTER_DUE_FUTURE, false)
            }
        }
    }


    fun applyFilters() {

        binding.listProgressIndicator.visibility = View.VISIBLE
        updateToolbarText()
        icalListViewModel.updateSearch()
        savePrefs()
    }

    private fun savePrefs() {
        when (icalListViewModel.searchModule) {
            Module.JOURNAL.name -> {
                prefs.edit().putStringSet(PREFS_JOURNAL_COLLECTION, icalListViewModel.searchCollection.toSet()).apply()
                prefs.edit().putStringSet(PREFS_JOURNAL_ACCOUNT, icalListViewModel.searchAccount.toSet()).apply()
                prefs.edit().putStringSet(PREFS_JOURNAL_STATUS_JOURNAL, StatusJournal.getStringSetFromList(icalListViewModel.searchStatusJournal)).apply()
                prefs.edit().putStringSet(PREFS_JOURNAL_STATUS_TODO, StatusTodo.getStringSetFromList(icalListViewModel.searchStatusTodo)).apply()
                prefs.edit().putStringSet(PREFS_JOURNAL_CLASSIFICATION, Classification.getStringSetFromList(icalListViewModel.searchClassification)).apply()
                prefs.edit().putStringSet(PREFS_JOURNAL_CATEGORIES, icalListViewModel.searchCategories.toSet()).apply()
                prefs.edit().putBoolean(PREFS_JOURNAL_EXCLUDE_DONE, icalListViewModel.isExcludeDone).apply()
            }
            Module.NOTE.name -> {
                prefs.edit().putStringSet(PREFS_NOTE_COLLECTION, icalListViewModel.searchCollection.toSet()).apply()
                prefs.edit().putStringSet(PREFS_NOTE_ACCOUNT, icalListViewModel.searchAccount.toSet()).apply()
                prefs.edit().putStringSet(PREFS_NOTE_STATUS_JOURNAL, StatusJournal.getStringSetFromList(icalListViewModel.searchStatusJournal)).apply()
                prefs.edit().putStringSet(PREFS_NOTE_STATUS_TODO, StatusTodo.getStringSetFromList(icalListViewModel.searchStatusTodo)).apply()
                prefs.edit().putStringSet(PREFS_NOTE_CLASSIFICATION, Classification.getStringSetFromList(icalListViewModel.searchClassification)).apply()
                prefs.edit().putStringSet(PREFS_NOTE_CATEGORIES, icalListViewModel.searchCategories.toSet()).apply()
                prefs.edit().putBoolean(PREFS_NOTE_EXCLUDE_DONE, icalListViewModel.isExcludeDone).apply()
            }
            Module.TODO.name -> {
                prefs.edit().putStringSet(PREFS_TODO_COLLECTION, icalListViewModel.searchCollection.toSet()).apply()
                prefs.edit().putStringSet(PREFS_TODO_ACCOUNT, icalListViewModel.searchAccount.toSet()).apply()
                //prefs.edit().putStringSet(PREFS_TODO_STATUS_JOURNAL, StatusJournal.getStringSetFromList(icalListViewModel.searchStatusJournal)).apply()
                prefs.edit().putStringSet(PREFS_TODO_STATUS_TODO, StatusTodo.getStringSetFromList(icalListViewModel.searchStatusTodo)).apply()
                prefs.edit().putStringSet(PREFS_TODO_CLASSIFICATION, Classification.getStringSetFromList(icalListViewModel.searchClassification)).apply()
                prefs.edit().putStringSet(PREFS_TODO_CATEGORIES, icalListViewModel.searchCategories.toSet()).apply()
                prefs.edit().putBoolean(PREFS_TODO_EXCLUDE_DONE, icalListViewModel.isExcludeDone).apply()
                prefs.edit().putBoolean(PREFS_TODO_FILTER_OVERDUE, icalListViewModel.isFilterOverdue).apply()
                prefs.edit().putBoolean(PREFS_TODO_FILTER_DUE_TODAY, icalListViewModel.isFilterDueToday).apply()
                prefs.edit().putBoolean(PREFS_TODO_FILTER_DUE_TOMORROW, icalListViewModel.isFilterDueTomorrow).apply()
                prefs.edit().putBoolean(PREFS_TODO_FILTER_DUE_FUTURE, icalListViewModel.isFilterDueFuture).apply()
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
        searchView.queryHint = getString(R.string.search)

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
            R.id.menu_list_gotodate -> showScrollToDate()
            R.id.menu_list_filter -> goToFilter()
            R.id.menu_list_clearfilter -> resetFilter()
            R.id.menu_list_delete_visible -> deleteVisible()
            R.id.menu_list_syncnow -> SyncUtil.syncAllAccounts(requireContext())
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * Resets the focus item
     * Clears all filter criteria
     * Clears the preferences with the saved search criteria
     */
    private fun resetFilter() {

        lastIcal4ListHash = 0    // make sure the whole view gets reloaded
        icalListViewModel.clearFilter()

        when (icalListViewModel.searchModule) {
            Module.JOURNAL.name -> {
                prefs.edit().remove(PREFS_JOURNAL_COLLECTION).apply()
                prefs.edit().remove(PREFS_JOURNAL_ACCOUNT).apply()
                prefs.edit().remove(PREFS_JOURNAL_STATUS_JOURNAL).apply()
                prefs.edit().remove(PREFS_JOURNAL_STATUS_TODO).apply()
                prefs.edit().remove(PREFS_JOURNAL_CLASSIFICATION).apply()
                prefs.edit().remove(PREFS_JOURNAL_CATEGORIES).apply()
                prefs.edit().remove(PREFS_JOURNAL_EXCLUDE_DONE).apply()
            }
            Module.NOTE.name -> {
                prefs.edit().remove(PREFS_NOTE_COLLECTION).apply()
                prefs.edit().remove(PREFS_NOTE_ACCOUNT).apply()
                prefs.edit().remove(PREFS_NOTE_STATUS_JOURNAL).apply()
                prefs.edit().remove(PREFS_NOTE_STATUS_TODO).apply()
                prefs.edit().remove(PREFS_NOTE_CLASSIFICATION).apply()
                prefs.edit().remove(PREFS_NOTE_CATEGORIES).apply()
                prefs.edit().remove(PREFS_NOTE_EXCLUDE_DONE).apply()
            }
            Module.TODO.name -> {
                prefs.edit().remove(PREFS_TODO_COLLECTION).apply()
                prefs.edit().remove(PREFS_TODO_ACCOUNT).apply()
                //prefs.edit().remove(PREFS_TODO_STATUS_JOURNAL).apply()
                prefs.edit().remove(PREFS_TODO_STATUS_TODO).apply()
                prefs.edit().remove(PREFS_TODO_CLASSIFICATION).apply()
                prefs.edit().remove(PREFS_TODO_CATEGORIES).apply()
                prefs.edit().remove(PREFS_TODO_EXCLUDE_DONE).apply()
                prefs.edit().remove(PREFS_TODO_FILTER_OVERDUE).apply()
                prefs.edit().remove(PREFS_TODO_FILTER_DUE_TODAY).apply()
                prefs.edit().remove(PREFS_TODO_FILTER_DUE_TOMORROW).apply()
                prefs.edit().remove(PREFS_TODO_FILTER_DUE_FUTURE).apply()

            }
        }
    }

    private fun goToFilter() {

        this.findNavController().navigate(
            IcalListFragmentDirections.actionIcalListFragmentToIcalFilterFragment().apply {
                this.category2preselect = icalListViewModel.searchCategories.toTypedArray()
                this.statusJournal2preselect = icalListViewModel.searchStatusJournal.toTypedArray()
                this.statusTodo2preselect = icalListViewModel.searchStatusTodo.toTypedArray()
                this.classification2preselect = icalListViewModel.searchClassification.toTypedArray()
                this.collection2preselect = icalListViewModel.searchCollection.toTypedArray()
                this.account2preselect = icalListViewModel.searchAccount.toTypedArray()
                this.module2preselect = icalListViewModel.searchModule
            })
    }

    private fun goToEdit(iCalObject: ICalEntity) {
        this.findNavController().navigate(IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(iCalObject))
    }

    /*
    private fun applyQuickFilterJournal(statusList: MutableList<StatusJournal>) {

        //resetFilter(false)
        icalListViewModel.searchStatusJournal = statusList
        applyFilters()
    }
     */


    private fun toggleMenuCheckboxFilter(menuitem: MenuItem) {
        when (menuitem.itemId) {
            R.id.menu_list_bottom_toggle_completed_tasks -> icalListViewModel.isExcludeDone = !icalListViewModel.isExcludeDone
            R.id.menu_list_bottom_filter_overdue -> icalListViewModel.isFilterOverdue = !icalListViewModel.isFilterOverdue
            R.id.menu_list_bottom_filter_due_today -> icalListViewModel.isFilterDueToday = !icalListViewModel.isFilterDueToday
            R.id.menu_list_bottom_filter_due_tomorrow -> icalListViewModel.isFilterDueTomorrow = !icalListViewModel.isFilterDueTomorrow
            R.id.menu_list_bottom_filter_due_in_future -> icalListViewModel.isFilterDueFuture = !icalListViewModel.isFilterDueFuture
            else -> return
        }
        lastIcal4ListHash = 0     // makes the recycler view refresh everything (necessary for subtasks!)
        applyFilters()
    }

    private fun showScrollToDate() {

        // Create a custom date validator to only enable dates that are in the list
        val customDateValidator = object : CalendarConstraints.DateValidator {
            override fun describeContents(): Int {
                return 0
            }

            override fun writeToParcel(dest: Parcel?, flags: Int) {}
            override fun isValid(date: Long): Boolean {

                icalListViewModel.iCal4List.value?.forEach {
                    val zonedDtstart = ZonedDateTime.ofInstant(Instant.ofEpochMilli(it.property.dtstart?:0L), requireTzId(it.property.dtstartTimezone))
                    val zonedSelection = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault())

                    if(zonedDtstart.dayOfMonth == zonedSelection.dayOfMonth && zonedDtstart.monthValue == zonedSelection.monthValue && zonedDtstart.year == zonedSelection.year)
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
                .setTitleText(R.string.edit_datepicker_dialog_select_date)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

        datePicker.addOnPositiveButtonClickListener {
            // Respond to positive button click.
            val zonedSelection = ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())

            // find the item with the same date
            val matchedItem = icalListViewModel.iCal4List.value?.find { item ->
                val zonedMatch = ZonedDateTime.ofInstant(Instant.ofEpochMilli(item.property.dtstart ?: 0L), requireTzId(item.property.dtstartTimezone))
                zonedSelection.dayOfMonth == zonedMatch.dayOfMonth && zonedSelection.monthValue == zonedMatch.monthValue && zonedSelection.year == zonedMatch.year
            }
            if (matchedItem != null) {
                icalListViewModel.scrollOnceId = matchedItem.property.id
                scrollOnce()
            }
        }

        datePicker.show(parentFragmentManager, "menu_list_gotodate")
    }

    private fun scrollOnce() {

        icalListViewModel.scrollOnceId?.let { scrollOnceId ->
            val scrollToItem = icalListViewModel.iCal4List.value?.find { listItem -> listItem.property.id == scrollOnceId }
            val scrollToItemPos = icalListViewModel.iCal4List.value?.indexOf(scrollToItem)

            scrollToItemPos?.let { pos ->
                linearLayoutManager?.scrollToPositionWithOffset(pos, 0)
                icalListViewModel.scrollOnceId = null
            }
        }
    }


    private fun isFilterActive() = icalListViewModel.searchCategories.isNotEmpty() || icalListViewModel.searchOrganizer.isNotEmpty() || (icalListViewModel.searchModule == Module.JOURNAL.name && icalListViewModel.searchStatusJournal.isNotEmpty()) || (icalListViewModel.searchModule == Module.NOTE.name && icalListViewModel.searchStatusJournal.isNotEmpty()) || (icalListViewModel.searchModule == Module.TODO.name && icalListViewModel.searchStatusTodo.isNotEmpty()) || icalListViewModel.searchClassification.isNotEmpty() || icalListViewModel.searchCollection.isNotEmpty() || icalListViewModel.searchAccount.isNotEmpty() || icalListViewModel.isExcludeDone || icalListViewModel.isFilterOverdue || icalListViewModel.isFilterDueToday || icalListViewModel.isFilterDueTomorrow || icalListViewModel.isFilterDueFuture

    private fun deleteVisible() {

        val itemIds = mutableListOf<Long>()
        icalListViewModel.iCal4List.value?.forEach {
            if(!it.property.isLinkedRecurringInstance)
                itemIds.add(it.property.id)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.list_dialog_delete_visible_title))
            .setMessage(getString(R.string.list_dialog_delete_visible_message, itemIds.size))
            .setPositiveButton(R.string.delete) { _, _ ->
                icalListViewModel.delete(itemIds)
                Attachment.scheduleCleanupJob(requireContext())
            }
            .setNeutralButton(R.string.cancel) { _, _ ->  // nothing to do
            }
            .show()
    }


    /**
     * This function takes care of the dialog to add a quick journal/note/to-do out of a dialog
     * The user can add a summary/description and select the collection
     */
    private fun showQuickAddDialog() {

        /**
         * PREPARE DIALOG
         */
        val quickAddDialogBinding = FragmentIcalListQuickaddDialogBinding.inflate(layoutInflater)

        val title = when(icalListViewModel.searchModule) {
            Module.JOURNAL.name -> getString(R.string.menu_list_quick_journal)
            Module.NOTE.name -> getString(R.string.menu_list_quick_note)
            Module.TODO.name -> getString(R.string.menu_list_quick_todo)
            else -> ""
        }
        var selectedCollectionPos = 0


        /**
         * Add the observer for the collections
         */
        icalListViewModel.allCollections.observe(viewLifecycleOwner) {

            if(it.isEmpty())
                return@observe

            val allCollectionNames: MutableList<String> = mutableListOf()
            icalListViewModel.allCollections.value?.forEach { collection ->
                if(collection.displayName?.isNotEmpty() == true && collection.accountName?.isNotEmpty() == true)
                    allCollectionNames.add(collection.displayName + " (" + collection.accountName + ")")
                else
                    allCollectionNames.add(collection.displayName?: "-")
            }
            quickAddDialogBinding.listQuickaddDialogCollectionSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, allCollectionNames)

            // set the default selection for the spinner.
            val lastUsedCollectionId = prefs.getLong(PREFS_LAST_USED_COLLECTION, 1L)
            val lastUsedCollection = icalListViewModel.allCollections.value?.find { colllections -> colllections.collectionId == lastUsedCollectionId }
            selectedCollectionPos = icalListViewModel.allCollections.value?.indexOf(lastUsedCollection) ?: 0
            quickAddDialogBinding.listQuickaddDialogCollectionSpinner.setSelection(selectedCollectionPos)


            quickAddDialogBinding.listQuickaddDialogCollectionSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(p0: AdapterView<*>?, view: View?, pos: Int, p3: Long) {

                        icalListViewModel.allCollections.removeObservers(viewLifecycleOwner)     // remove the observer, the live data must NOT change the data in the background anymore! (esp. the item positions)
                        selectedCollectionPos = pos

                        // update color of colorbar
                        try {
                        icalListViewModel.allCollections.value?.get(pos)?.color.let { color ->
                            if(color == null)
                                quickAddDialogBinding.listQuickaddDialogColorbar.visibility = View.INVISIBLE
                            else {quickAddDialogBinding.listQuickaddDialogColorbar.visibility = View.VISIBLE
                                quickAddDialogBinding.listQuickaddDialogColorbar.setColorFilter(color)
                            }
                        }
                        } catch (e: IllegalArgumentException) {
                            //Log.i("Invalid color","Invalid Color cannot be parsed: ${color}")
                            quickAddDialogBinding.listQuickaddDialogColorbar.visibility = View.INVISIBLE
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                }
        }


        val sr: SpeechRecognizer? = when {
            SpeechRecognizer.isRecognitionAvailable(requireContext()) -> SpeechRecognizer.createSpeechRecognizer(requireContext())
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(requireContext()) -> SpeechRecognizer.createOnDeviceSpeechRecognizer(requireContext())
            else -> null
        }

        if(sr != null) {
            quickAddDialogBinding.listQuickaddDialogTextinput.endIconDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_microphone)
            quickAddDialogBinding.listQuickaddDialogTextinput.setEndIconOnClickListener {
                // Record Audio Permission is needed to access the microphone
                PermissionsHelper.checkPermissionRecordAudio(requireActivity())

                val srIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                srIntent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                srIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                srIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

                sr.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(p0: Bundle?) {
                        context?.let { Toast.makeText(it, R.string.list_quickadd_dialog_sr_start_listening, Toast.LENGTH_SHORT).show() }
                    }
                    override fun onBeginningOfSpeech() { }
                    override fun onEndOfSpeech() {
                        context?.let { Toast.makeText(it,R.string.list_quickadd_dialog_sr_stop_listening, Toast.LENGTH_SHORT).show() }
                    }
                    override fun onRmsChanged(p0: Float) {}
                    override fun onBufferReceived(p0: ByteArray?) {}
                    override fun onError(errorCode: Int) {
                        context?.let { Toast.makeText(it,getString(R.string.list_quickadd_dialog_sr_error, errorCode.toString()), Toast.LENGTH_SHORT).show()  }
                        return
                    }
                    override fun onPartialResults(bundle: Bundle?) {
                        val data: ArrayList<String>? = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        // the bundle contains multiple possible results with the result of the highest probability on top. We show only the must likely result at position 0.
                        if (data?.isNotEmpty() == true) {
                            quickAddDialogBinding.listQuickaddDialogSrPartialResult.text = data[0]
                            quickAddDialogBinding.listQuickaddDialogSrPartialResult.visibility = View.VISIBLE
                        }
                    }
                    override fun onEvent(p0: Int, p1: Bundle?) {}
                    override fun onResults(bundle: Bundle?) {
                        quickAddDialogBinding.listQuickaddDialogSrPartialResult.visibility = View.GONE
                        val data: ArrayList<String>? = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        // add a return if there is already text present to add it in a new line
                        if(quickAddDialogBinding.listQuickaddDialogEdittext.text?.isNotBlank() == true)
                            quickAddDialogBinding.listQuickaddDialogEdittext.text?.append("\n")
                        // the bundle contains multiple possible results with the result of the highest probability on top. We show only the must likely result at position 0.
                        if (data?.isNotEmpty() == true)
                            quickAddDialogBinding.listQuickaddDialogEdittext.text = quickAddDialogBinding.listQuickaddDialogEdittext.text?.append(data[0])
                    }
                })
                sr.startListening(srIntent)
            }
        }


        /**
         * Inner function to handle the creation of a new quick-entry
         */
        fun createNewQuickEntry() {
            val text = quickAddDialogBinding.listQuickaddDialogEdittext.text.toString()
            if(text.isBlank()) {    // don't add the entry if no text was entered
                Toast.makeText(context, R.string.list_quickadd_toast_no_summary_description, Toast.LENGTH_LONG).show()
                return
            }
            val newIcalObject = when(icalListViewModel.searchModule) {
                Module.JOURNAL.name -> ICalObject.createJournal()
                Module.NOTE.name -> ICalObject.createNote(null)
                Module.TODO.name -> ICalObject.createTask(null)
                else -> null
            }
            newIcalObject?.let {
                it.parseSummaryAndDescription(text)
                it.collectionId = icalListViewModel.allCollections.value?.get(selectedCollectionPos)?.collectionId ?: 1L
                val categories = Category.extractHashtagsFromText(text)

                prefs.edit().putLong(PREFS_LAST_USED_COLLECTION, it.collectionId).apply()       // save last used collection for next time

                icalListViewModel.insertQuickItem(it, categories)

                if(AdManager.getInstance()?.isAdFlavor() == true && BillingManager.getInstance()?.isAdFreeSubscriptionPurchased?.value == false)
                    AdManager.getInstance()?.showInterstitialAd(requireActivity())     // don't forget to show an ad if applicable ;-)
            }
        }

        /**
         * SHOW DIALOG
         * The result is taken care of in the observer
         */
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setIcon(R.drawable.ic_add_quick)
            .setView(quickAddDialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                createNewQuickEntry()
                icalListViewModel.quickInsertedEntity.observe(viewLifecycleOwner) { entity ->
                    if(entity == null)
                        return@observe
                    icalListViewModel.quickInsertedEntity.removeObservers(viewLifecycleOwner)
                    icalListViewModel.quickInsertedEntity.value = null     // invalidate so that on click on back, the value is empty and doesn't create unexpected behaviour!
                }
            }
            .setNeutralButton(R.string.cancel) { _, _ ->  }
            .setNegativeButton(R.string.save_and_edit) { _, _ ->
                createNewQuickEntry()
                icalListViewModel.quickInsertedEntity.observe(viewLifecycleOwner) {
                    if (it == null)
                        return@observe
                    icalListViewModel.quickInsertedEntity.removeObservers(viewLifecycleOwner)
                    icalListViewModel.quickInsertedEntity.value = null      // invalidate so that on click on back, the value is empty and doesn't create unexpected behaviour!
                    this.findNavController().navigate(IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(it))
                }
            }
            .show()
    }
}
