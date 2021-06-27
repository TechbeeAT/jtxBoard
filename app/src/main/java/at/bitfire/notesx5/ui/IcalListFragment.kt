/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.ui


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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import at.bitfire.notesx5.R
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.relations.ICalEntity
import at.bitfire.notesx5.databinding.FragmentIcalListBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import java.util.*


class IcalListFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    //private var linearLayoutManager: LinearLayoutManager? = null
    private var staggeredGridLayoutManager: StaggeredGridLayoutManager? = null

    private var icalListAdapter: IcalListAdapter? = null

    private lateinit var icalListViewModel: IcalListViewModel

    private lateinit var binding: FragmentIcalListBinding
    private lateinit var application: Application
    private lateinit var dataSource: ICalDatabaseDao

    private var gotodateMenuItem: MenuItem? = null

    private lateinit var prefs: SharedPreferences


    companion object {
        const val PREFS_LIST_VIEW = "sharedPreferencesListView"
        const val PREFS_MODULE = "prefsModule"
        const val PREFS_COLLECTION = "prefsCollection"
        const val PREFS_CATEGORIES = "prefsCategories"
        const val PREFS_CLASSIFICATION = "prefsClassification"
        const val PREFS_STATUS_TODO = "prefsStatusTodo"
        const val PREFS_STATUS_JOURNAL = "prefsStatusJournal"
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
        icalListViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(IcalListViewModel::class.java)

        binding.vJournalListViewModel = icalListViewModel
        binding.lifecycleOwner = this

        // add menu
        setHasOptionsMenu(true)

        //doFilesCheck()


        // set up recycler view
        recyclerView = binding.vjournalListItemsRecyclerView
        //linearLayoutManager = LinearLayoutManager(application.applicationContext)
        staggeredGridLayoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        recyclerView?.layoutManager = staggeredGridLayoutManager
        recyclerView?.setHasFixedSize(true)

        // create adapter and provide data
        //vJournalListAdapter = VJournalListAdapter(application.applicationContext, vJournalListViewModel.vjournalList, vJournalListViewModel.vjournaListCount)
        icalListAdapter = IcalListAdapter(application.applicationContext, icalListViewModel)

        recyclerView?.adapter = icalListAdapter


        val arguments = IcalListFragmentArgs.fromBundle((requireArguments()))

        // Observe the vjournalList for Changes, on any change the recycler view must be updated, additionally the Focus Item might be updated
        icalListViewModel.iCal4List.observe(viewLifecycleOwner, {
            icalListAdapter!!.notifyDataSetChanged()

            if (arguments.item2focus != 0L && icalListViewModel.iCal4List.value?.size!! > 0) {
                //Log.println(Log.INFO, "vJournalListFragment", arguments.vJournalItemId.toString())
                icalListViewModel.setFocusItem(arguments.item2focus)
            }

            when (icalListViewModel.searchModule) {
                "NOTE" -> {
                    gotodateMenuItem?.isVisible = false
                    staggeredGridLayoutManager!!.spanCount = 2
                }
                "JOURNAL" -> {
                    gotodateMenuItem?.isVisible = true
                    staggeredGridLayoutManager!!.spanCount = 1
                }
                "TODO" -> {
                    gotodateMenuItem?.isVisible = false
                    staggeredGridLayoutManager!!.spanCount = 1
                }
            }
            binding.listProgressIndicator.visibility = View.GONE
        })

        // This observer is needed in order to make sure that the Subtasks are retrieved!
        icalListViewModel.allSubtasks.observe(viewLifecycleOwner, {})


        // Observe the focus item to scroll automatically to the right position (newly updated or inserted item)
        icalListViewModel.focusItemId.observe(viewLifecycleOwner, {

            val pos = icalListViewModel.getFocusItemPosition()
            Log.println(Log.INFO, "vJournalListViewModel", "Item Position: $pos")

            recyclerView?.scrollToPosition(pos)
            //vJournalListViewModel.resetFocusItem()
            Log.println(Log.INFO, "vJournalListViewModel", "Scrolling now to: $pos")

        })



        binding.tablayoutJournalnotestodos.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.listProgressIndicator.visibility = View.VISIBLE
                icalListViewModel.clearFilter()
                icalListViewModel.resetFocusItem()

                when (tab?.position) {
                    0 -> icalListViewModel.searchModule = Module.JOURNAL.name
                    1 -> icalListViewModel.searchModule = Module.NOTE.name
                    2 -> icalListViewModel.searchModule = Module.TODO.name
                    else -> icalListViewModel.searchModule = Module.JOURNAL.name
                }

                // ATTENTION: As the View is recreated on Resume, the arguments are read again, therefore the searchComponent MUST be updated, otherwise the application will crash!
                getArguments()?.putString("module2show", icalListViewModel.searchModule)
                prefs.edit().putString(PREFS_MODULE, icalListViewModel.searchModule).apply()
                loadFilters()

                icalListViewModel.updateSearch()

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // nothing to do
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // nothing to do
            }
        })

        return binding.root
    }


    override fun onStart() {

        loadFilters()

        // initialize the floating action button only onStart, otherwise the fragment might not be created yet
        binding.fab.setOnClickListener {

            icalListViewModel.resetFocusItem()

            val newICalObject =
                    when(binding.tablayoutJournalnotestodos.selectedTabPosition) {
                        0 -> ICalEntity(ICalObject.createJournal())
                        1 -> ICalEntity(ICalObject.createNote())
                        2 -> ICalEntity(ICalObject.createTodo())
                        else -> ICalEntity(ICalObject.createJournal())
                    }
            this.findNavController().navigate(
                    IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(newICalObject))
        }

        binding.fabFilter.setOnClickListener {

            if (icalListViewModel.searchCategories.isNotEmpty() || icalListViewModel.searchOrganizer.isNotEmpty() || icalListViewModel.searchStatusJournal.isNotEmpty() || icalListViewModel.searchStatusTodo.isNotEmpty() || icalListViewModel.searchClassification.isNotEmpty() || icalListViewModel.searchCollection.isNotEmpty()) {
                binding.fabFilter.setImageResource(R.drawable.ic_filter)
                icalListViewModel.resetFocusItem()
                icalListViewModel.clearFilter()
                prefs.edit().clear().apply()
                arguments?.clear()
            }
            else {
                goToFilterFragment()
            }


        }

        super.onStart()
    }


    fun loadFilters() {

        prefs = activity?.getSharedPreferences(PREFS_LIST_VIEW, Context.MODE_PRIVATE)!!

        // pass filter arguments to view model
        val arguments = IcalListFragmentArgs.fromBundle((requireArguments()))

        // set the search values for the selected component and store in shared preferences or retrieve and set component from the shared preferences
        if (arguments.module2show?.isNotEmpty() == true) {
            icalListViewModel.searchModule = arguments.module2show!!
            prefs.edit().putString(PREFS_MODULE, arguments.module2show).apply()
        } else if (prefs.getString(PREFS_MODULE, null)?.isNotEmpty() == true) {
            icalListViewModel.searchModule = prefs.getString(PREFS_MODULE, null)!!
        }


        // load the shared preferences for the given component
        /*sharedPref = when ( icalListViewModel.searchComponent) {
            Component.JOURNAL.name -> activity?.getSharedPreferences(PREFS_JOURNALS, Context.MODE_PRIVATE)!!
            Component.NOTE.name -> activity?.getSharedPreferences(PREFS_NOTES, Context.MODE_PRIVATE)!!
            Component.TODO.name -> activity?.getSharedPreferences(PREFS_TODOS, Context.MODE_PRIVATE)!!
            else -> activity?.getSharedPreferences(PREFS_JOURNALS, Context.MODE_PRIVATE)!!
        }   */

        // set the search values for categories and store in shared preferences or retrieve and set categories from the shared preferences
        if (arguments.category2filter?.isNotEmpty() == true) {
            icalListViewModel.searchCategories = arguments.category2filter!!.toMutableList()
            prefs.edit().putStringSet(PREFS_CATEGORIES, arguments.category2filter!!.toSet()).apply()
        } else if (prefs.getStringSet(PREFS_CATEGORIES, null)?.isNotEmpty() == true) {
            icalListViewModel.searchCategories = prefs.getStringSet(PREFS_CATEGORIES, null)!!.toMutableList()
        }

        // set the search values for classification and store in shared preferences or retrieve and set classifications from the shared preferences
        if (arguments.classification2filter?.isNotEmpty() == true) {
            icalListViewModel.searchClassification = arguments.classification2filter!!.toMutableList()

            val classificationIds = mutableListOf<String>()
            arguments.classification2filter!!.forEach {
                classificationIds.add(it.name)
            }
            prefs.edit().putStringSet(PREFS_CLASSIFICATION, classificationIds.toSet()).apply()
        } else if (prefs.getStringSet(PREFS_CLASSIFICATION, null)?.isNotEmpty() == true) {
            prefs.getStringSet(PREFS_CLASSIFICATION, null)!!.forEach {
                when (it) {
                    Classification.PUBLIC.name -> icalListViewModel.searchClassification.add(Classification.PUBLIC)
                    Classification.CONFIDENTIAL.name -> icalListViewModel.searchClassification.add(Classification.CONFIDENTIAL)
                    Classification.PRIVATE.name -> icalListViewModel.searchClassification.add(Classification.PRIVATE)
                }
            }
        }

        // set the search values for status (todos) and store in shared preferences or retrieve and set status (todos) from the shared preferences
        if (arguments.statusTodo2filter?.isNotEmpty() == true) {
            icalListViewModel.searchStatusTodo = arguments.statusTodo2filter!!.toMutableList()

            val statusTodoIds = mutableListOf<String>()
            arguments.statusTodo2filter!!.forEach {
                statusTodoIds.add(it.name)
            }
            prefs.edit().putStringSet(PREFS_STATUS_TODO, statusTodoIds.toSet()).apply()
        } else if (prefs.getStringSet(PREFS_STATUS_TODO, null)?.isNotEmpty() == true) {
            prefs.getStringSet(PREFS_STATUS_TODO, null)!!.forEach {
                when (it) {
                    StatusTodo.`NEEDS-ACTION`.name -> icalListViewModel.searchStatusTodo.add(StatusTodo.`NEEDS-ACTION`)
                    StatusTodo.`IN-PROCESS`.name -> icalListViewModel.searchStatusTodo.add(StatusTodo.`IN-PROCESS`)
                    StatusTodo.COMPLETED.name -> icalListViewModel.searchStatusTodo.add(StatusTodo.COMPLETED)
                    StatusTodo.CANCELLED.name -> icalListViewModel.searchStatusTodo.add(StatusTodo.CANCELLED)
                }
            }
        }

        // set the search values for status (journals) and store in shared preferences or retrieve and set status (journals) from the shared preferences
        if (arguments.statusJournal2filter?.isNotEmpty() == true) {
            icalListViewModel.searchStatusJournal = arguments.statusJournal2filter!!.toMutableList()

            val statusJournalIds = mutableListOf<String>()
            arguments.statusJournal2filter!!.forEach {
                statusJournalIds.add(it.name)
            }
            prefs.edit().putStringSet(PREFS_STATUS_JOURNAL, statusJournalIds.toSet()).apply()
        } else if (prefs.getStringSet(PREFS_STATUS_JOURNAL, null)?.isNotEmpty() == true) {
            prefs.getStringSet(PREFS_STATUS_JOURNAL, null)!!.forEach {
                when (it) {
                    StatusJournal.DRAFT.name -> icalListViewModel.searchStatusJournal.add(StatusJournal.DRAFT)
                    StatusJournal.FINAL.name -> icalListViewModel.searchStatusJournal.add(StatusJournal.FINAL)
                    StatusJournal.CANCELLED.name -> icalListViewModel.searchStatusJournal.add(StatusJournal.CANCELLED)
                }
            }
        }

        // set the search values for collections and store in shared preferences or retrieve and set collections from the shared preferences
        if (arguments.collection2filter?.isNotEmpty() == true) {
            icalListViewModel.searchCollection = arguments.collection2filter!!.toMutableList()
            prefs.edit().putStringSet(PREFS_COLLECTION, arguments.collection2filter!!.toSet()).apply()
        } else if (prefs.getStringSet(PREFS_COLLECTION, null)?.isNotEmpty() == true) {
            icalListViewModel.searchCollection = prefs.getStringSet(PREFS_COLLECTION, null)!!.toMutableList()
        }

        //if(arguments.component2show.isNotEmpty() || arguments.category2filter?.isNotEmpty() == true || arguments.classification2filter?.isNotEmpty() == true || arguments.statusJournal2filter?.isNotEmpty() == true || arguments.statusTodo2filter?.isNotEmpty() == true || arguments.collection2filter?.isNotEmpty() == true)
            icalListViewModel.updateSearch()   // updateSearch() only if there was at least one filter criteria

        // Change the filter icon to make clear when a filter is active
        if (icalListViewModel.searchCategories.isNotEmpty() || icalListViewModel.searchOrganizer.isNotEmpty() || icalListViewModel.searchStatusJournal.isNotEmpty() || icalListViewModel.searchStatusTodo.isNotEmpty() || icalListViewModel.searchClassification.isNotEmpty() || icalListViewModel.searchCollection.isNotEmpty())
            binding.fabFilter.setImageResource(R.drawable.ic_filter_delete)
        else
            binding.fabFilter.setImageResource(R.drawable.ic_filter)

        // activate the right tab according to the searchComponent
        when (icalListViewModel.searchModule) {
            "NOTE" -> binding.tablayoutJournalnotestodos.getTabAt(1)?.select()
            "JOURNAL" -> binding.tablayoutJournalnotestodos.getTabAt(0)?.select()
            "TODO" -> binding.tablayoutJournalnotestodos.getTabAt(2)?.select()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_ical_list, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)



        // Tell the variable the menu item to later make it visible or invisible
        gotodateMenuItem = menu.findItem(R.id.menu_list_gotodate)

        // add listener for search!
        val searchMenuItem = menu.findItem(R.id.menu_list_search)
        val searchView = searchMenuItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                // nothing to do as the the search is already updated with the text input
                return false
            }

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

        if (item.itemId == R.id.menu_list_gotodate) {

            // Create a custom date validator to only enable dates that are in the list
            val customDateValidator = object: CalendarConstraints.DateValidator {
                override fun describeContents(): Int {  return 0  }
                override fun writeToParcel(dest: Parcel?, flags: Int) {  }
                override fun isValid(date: Long): Boolean {

                    icalListViewModel.iCal4List.value?.forEach {
                        val itemDateTime = Calendar.getInstance()
                        itemDateTime.timeInMillis = it.property.dtstart?: System.currentTimeMillis()

                        val dateDateTime = Calendar.getInstance()
                        dateDateTime.timeInMillis = date

                        if(itemDateTime.get(Calendar.YEAR) == dateDateTime.get(Calendar.YEAR)
                            && itemDateTime.get(Calendar.MONTH) == dateDateTime.get(Calendar.MONTH)
                            && itemDateTime.get(Calendar.DAY_OF_MONTH) == dateDateTime.get(Calendar.DAY_OF_MONTH))
                            return true
                    }
                    return false
                }
            }

            // Build constraints.
            val constraintsBuilder =
                CalendarConstraints.Builder().apply {

                    val startItem = icalListViewModel.iCal4List.value?.lastOrNull()
                    val endItem = icalListViewModel.iCal4List.value?.firstOrNull()

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
                    cItem.timeInMillis = item.property.dtstart?: 0L

                    // if this condition is true, the item is considered as found
                    cItem.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
                            && cItem.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)
                            && cItem.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
                }
                if (foundItem != null)
                    icalListViewModel.setFocusItem(foundItem.property.id)
            }

            datePicker.show(parentFragmentManager, "menu_list_gotodate")

        }

        if (item.itemId == R.id.menu_list_filter) {

            goToFilterFragment()
        }

        if (item.itemId == R.id.menu_list_clearfilter) {
            icalListViewModel.resetFocusItem()
            icalListViewModel.clearFilter()
            prefs.edit().clear().apply()
            arguments?.clear()
        }

        if (item.itemId == R.id.menu_list_add_journal) {
            icalListViewModel.resetFocusItem()
            val newICalObject = ICalEntity(ICalObject.createJournal())
            this.findNavController().navigate(
                IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(newICalObject))
        }

        if (item.itemId == R.id.menu_list_add_note) {
            icalListViewModel.resetFocusItem()
            val newICalObject = ICalEntity(ICalObject.createNote())

            this.findNavController().navigate(
                    IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(newICalObject))
        }

        if (item.itemId == R.id.menu_list_add_todo) {
            icalListViewModel.resetFocusItem()

            val newICalObject = ICalEntity(ICalObject.createTodo())

            this.findNavController().navigate(
                    IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(newICalObject))
        }

        return super.onOptionsItemSelected(item)
    }




    private fun goToFilterFragment() {
        icalListViewModel.resetFocusItem()
        prefs.edit().clear().apply()

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

/*
    private fun doFilesCheck() {


        val foundFileContentUris = mutableListOf<Uri>()


        val filesPath = File(requireContext().filesDir, ".")
        filesPath.listFiles().forEach {
            Log.d("FileInFolder", it.path.toString())
            val fileContentUri = FileProvider.getUriForFile(requireContext(), AUTHORITY_FILEPROVIDER, it)
            foundFileContentUris.add(fileContentUri)
            Log.d("FileInFolderCUri", fileContentUri.toString())

        }

        val extFilesPath = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())
        extFilesPath.listFiles().forEach {
            Log.d("FileInExtFolder", it.path.toString())
            val fileContentUri = FileProvider.getUriForFile(requireContext(), AUTHORITY_FILEPROVIDER, it)
            foundFileContentUris.add(fileContentUri)
            Log.d("FileInFolderCUri", fileContentUri.toString())
        }

        lifecycleScope.launchWhenStarted {
            val allAttachmentUris = dataSource.getAllAttachmentUris()
            allAttachmentUris.forEach { attachment2keep ->
                foundFileContentUris.remove(Uri.parse(attachment2keep))
            }

            Log.d("remainingItems", foundFileContentUris.toString())

            foundFileContentUris.forEach {
                requireContext().contentResolver.delete(it, null, null)
            }

        }


    }


 */
}


    /*
    binding.topAppBar.setOnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
            R.id.favorite -> {
                // Handle favorite icon press
                true
            }
            R.id.search -> {
                // Handle search icon press
                true
            }
            R.id.more -> {
                // Handle more item (inside overflow menu) press
                true
            }
            else -> false
        }
    }
*/
    // END Set up Search


    /*
    val materialDateBuilder: MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()
    materialDateBuilder.setTitleText("Go to date")
    materialDateBuilder.setTheme(R.style.MaterialDatepicker)
    val materialDatePicker = materialDateBuilder.build()

    materialDatePicker.show(parentFragmentManager, "GOTO_MATERIAL_DATE_PICKER")
    materialDatePicker.addOnPositiveButtonClickListener {
        Toast.makeText(context, "Selected Date is : " + materialDatePicker.headerText, Toast.LENGTH_LONG)
    }

     */


// END Set up Datepicker


/*
        searchView.setOnMenuItem


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                //
                return false
            }
        })

                searchMenuItem.setOnMenuItemClickListener(object: SearchView.OnQueryTextListener   {

        })

 */


