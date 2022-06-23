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
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.ui.compose.appbars.ListBottomAppBar
import at.techbee.jtx.ui.compose.destinations.ListTabDestination
import at.techbee.jtx.ui.compose.screens.FilterScreen
import at.techbee.jtx.ui.compose.screens.ListScreen
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.launch


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
        const val PREFS_ISFIRSTRUN = "isFirstRun"

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


        // only ad the welcomeEntries on first install and exclude all installs that didn't have this preference before (installed before 1641596400000L = 2022/01/08
        /*
        val firstInstall = context?.packageManager?.getPackageInfo(requireContext().packageName, 0)?.firstInstallTime ?: System.currentTimeMillis()
        if(prefs.getBoolean(PREFS_ISFIRSTRUN, true)) {
            if (firstInstall > 1641596400000L)
                icalListViewModel.addWelcomeEntries(requireContext())
            prefs.edit().putBoolean(PREFS_ISFIRSTRUN, false).apply()
        }
        
         */



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

                /*
                if (query.isEmpty())
                    icalListViewModel.searchText = "%"
                else
                    icalListViewModel.searchText = "%$query%"

                icalListViewModel.updateSearch()


                 */
                return false

            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_list_gotodate -> showScrollToDate()
            R.id.menu_list_clearfilter -> resetFilter()
            R.id.menu_list_delete_visible -> deleteVisible()
            R.id.menu_list_syncnow -> SyncUtil.syncAllAccounts(context)
            /*
            R.id.menu_list_viewmode_list -> icalListViewModel.viewMode = PREFS_VIEWMODE_LIST
            R.id.menu_list_viewmode_grid -> icalListViewModel.viewMode = PREFS_VIEWMODE_GRID
            R.id.menu_list_viewmode_compact -> icalListViewModel.viewMode = PREFS_VIEWMODE_COMPACT
            R.id.menu_list_viewmode_kanban -> icalListViewModel.viewMode = PREFS_VIEWMODE_KANBAN

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


    private fun showScrollToDate() {

        /*
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
                var dates = icalListViewModel.iCal4List.value?.map { it.property.dtstart?:System.currentTimeMillis() }?.toList()
                if(dates.isNullOrEmpty())
                    dates = listOf(System.currentTimeMillis())
                setStart(dates.minOf { it })
                setEnd(dates.maxOf { it })
                setValidator(customDateValidator)
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
            if (matchedItem != null)
                icalListViewModel.scrollOnceId.postValue(matchedItem.property.id)
        }

        datePicker.show(parentFragmentManager, "menu_list_gotodate")

         */
    }


    private fun isFilterActive() = false
        /*
        icalListViewModel.searchCategories.isNotEmpty()
                || icalListViewModel.searchOrganizer.isNotEmpty()
                || (icalListViewModel.searchModule == Module.JOURNAL.name && icalListViewModel.searchStatusJournal.isNotEmpty())
                || (icalListViewModel.searchModule == Module.NOTE.name && icalListViewModel.searchStatusJournal.isNotEmpty())
                || (icalListViewModel.searchModule == Module.TODO.name && icalListViewModel.searchStatusTodo.isNotEmpty())
                || icalListViewModel.searchClassification.isNotEmpty() || icalListViewModel.searchCollection.isNotEmpty()
                || icalListViewModel.searchAccount.isNotEmpty()

         */

    private fun deleteVisible() {

        /*

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

         */
    }


    /**
     * This function takes care of the dialog to add a quick journal/note/to-do out of a dialog
     * The user can add a summary/description and select the collection
     */
    private fun showQuickAddDialog() {

        /*

        if ((BuildConfig.FLAVOR == MainActivity.BUILD_FLAVOR_GOOGLEPLAY && BillingManager.getInstance()?.isProPurchased?.value == false)) {
            val snackbar = Snackbar.make(requireView(), R.string.buypro_snackbar_quick_entries_blocked, Snackbar.LENGTH_LONG)
            snackbar.setAction(R.string.more) {
                findNavController().navigate(R.id.action_global_buyProFragment)
            }
            snackbar.show()
            return
        }

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
        var selectedCollectionPos: Int

        if(icalListViewModel.searchModule == Module.JOURNAL.name || icalListViewModel.searchModule == Module.NOTE.name)
            currentWriteableCollections = allCollections.filter {
                it.supportsVJOURNAL && !it.readonly
            }
        else if(icalListViewModel.searchModule == Module.TODO.name)
            currentWriteableCollections = allCollections.filter {
                it.supportsVTODO && !it.readonly
            }

        val allCollectionNames: MutableList<String> = mutableListOf()
        currentWriteableCollections.forEach { collection ->
            if(collection.displayName?.isNotEmpty() == true && collection.accountName?.isNotEmpty() == true)
                allCollectionNames.add(collection.displayName + " (" + collection.accountName + ")")
            else
                allCollectionNames.add(collection.displayName?: "-")
        }
        quickAddDialogBinding.listQuickaddDialogCollectionSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, allCollectionNames)

        // set the default selection for the spinner.
        val lastUsedCollectionId = prefs.getLong(PREFS_LAST_USED_COLLECTION, 1L)
        val lastUsedCollection = currentWriteableCollections.find { colllections -> colllections.collectionId == lastUsedCollectionId }
        selectedCollectionPos = currentWriteableCollections.indexOf(lastUsedCollection)
        quickAddDialogBinding.listQuickaddDialogCollectionSpinner.setSelection(selectedCollectionPos)


        quickAddDialogBinding.listQuickaddDialogCollectionSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(p0: AdapterView<*>?, view: View?, pos: Int, p3: Long) {

                    selectedCollectionPos = pos

                    // update color of colorbar
                    try {
                        currentWriteableCollections[pos].color.let { color ->
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
                Module.TODO.name -> ICalObject.createTask(null).apply {
                    this.setDefaultDueDateFromSettings(requireContext())
                    this.setDefaultStartDateFromSettings(requireContext())
                }
                else -> null
            }
            newIcalObject?.let {
                it.parseSummaryAndDescription(text)
                it.collectionId = currentWriteableCollections[selectedCollectionPos].collectionId
                val categories = Category.extractHashtagsFromText(text)

                prefs.edit().putLong(PREFS_LAST_USED_COLLECTION, it.collectionId).apply()       // save last used collection for next time

                icalListViewModel.insertQuickItem(it, categories)

                if(AdManager.getInstance()?.isAdFlavor() == true && BillingManager.getInstance()?.isProPurchased?.value == false)
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
                    icalListViewModel.scrollOnceId.postValue(entity.property.id)
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

         */
    }


}
