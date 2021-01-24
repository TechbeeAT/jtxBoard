package at.bitfire.notesx5.ui


import android.app.Application
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.DatePicker
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import at.bitfire.notesx5.R
import at.bitfire.notesx5.database.ICalDatabase
import at.bitfire.notesx5.database.ICalObject
import at.bitfire.notesx5.database.relations.ICalEntity
import at.bitfire.notesx5.databinding.FragmentIcalListBinding
import com.google.android.material.tabs.TabLayout
import java.util.*


class IcalListFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private var recyclerView: RecyclerView? = null
    //private var linearLayoutManager: LinearLayoutManager? = null
    private var staggeredGridLayoutManager: StaggeredGridLayoutManager? = null

    private var icalListAdapter: IcalListAdapter? = null

    private lateinit var icalListViewModel: IcalListViewModel

    private lateinit var binding: FragmentIcalListBinding
    private lateinit var application: Application

    private lateinit var gotodateMenuItem: MenuItem


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {


        // Get a reference to the binding object and inflate the fragment views.
        binding = FragmentIcalListBinding.inflate(inflater, container, false)

        // set up DB DAO
        application = requireNotNull(this.activity).application
        val dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao


        // create the view model through the view model factory
        val viewModelFactory = IcalListViewModelFactory(dataSource, application)
        icalListViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(IcalListViewModel::class.java)

        binding.vJournalListViewModel = icalListViewModel
        binding.lifecycleOwner = this

        // add menu
        setHasOptionsMenu(true)


        // set up recycler view
        recyclerView = binding.vjournalListItemsRecyclerView
        //linearLayoutManager = LinearLayoutManager(application.applicationContext)
        staggeredGridLayoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        recyclerView?.layoutManager = staggeredGridLayoutManager
        recyclerView?.setHasFixedSize(true)

        // create adapter and provide data
        //vJournalListAdapter = VJournalListAdapter(application.applicationContext, vJournalListViewModel.vjournalList, vJournalListViewModel.vjournaListCount)
        icalListAdapter = IcalListAdapter(application.applicationContext, icalListViewModel.vJournalList)

        recyclerView?.adapter = icalListAdapter


        // pass filter arguments to view model
        val arguments = IcalListFragmentArgs.fromBundle((arguments!!))

        if (arguments.category2filter?.isNotEmpty() == true)
            icalListViewModel.searchCategories = arguments.category2filter!!.toMutableList()

        if (arguments.classification2filter?.isNotEmpty() == true)
            icalListViewModel.searchClassification = arguments.classification2filter!!.toMutableList()

        if (arguments.status2filter?.isNotEmpty() == true)
            icalListViewModel.searchStatus = arguments.status2filter!!.toMutableList()

        if (arguments.collection2filter?.isNotEmpty() == true)
            icalListViewModel.searchCollection = arguments.collection2filter!!.toMutableList()

        if(arguments.category2filter?.isNotEmpty() == true || arguments.classification2filter?.isNotEmpty() == true || arguments.status2filter?.isNotEmpty() == true || arguments.collection2filter?.isNotEmpty() == true)
            icalListViewModel.updateSearch()   // updateSearch() only if there was at least one filter criteria



        // Observe the vjournalList for Changes, on any change the recycler view must be updated, additionally the Focus Item might be updated
        icalListViewModel.vJournalList.observe(viewLifecycleOwner, {
            icalListAdapter!!.notifyDataSetChanged()

            if (arguments.item2focus != 0L && icalListViewModel.vJournalList.value?.size!! > 0) {
                //Log.println(Log.INFO, "vJournalListFragment", arguments.vJournalItemId.toString())
                icalListViewModel.setFocusItem(arguments.item2focus)
            }
        })



        // Observe the focus item to scroll automatically to the right position (newly updated or inserted item)
        icalListViewModel.focusItemId.observe(viewLifecycleOwner, {

            val pos = icalListViewModel.getFocusItemPosition()
            Log.println(Log.INFO, "vJournalListViewModel", "Item Position: ${pos.toString()}")

            recyclerView?.scrollToPosition(pos)
            //vJournalListViewModel.resetFocusItem()
            Log.println(Log.INFO, "vJournalListViewModel", "Scrolling now to: ${pos.toString()}")

        })

        binding.tabLayoutJournalNotes.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                icalListViewModel.resetFocusItem()


                when (tab?.position) {
                    0 -> {
                        icalListViewModel.searchComponent = "JOURNAL"
                        gotodateMenuItem.isVisible = true
                        staggeredGridLayoutManager!!.spanCount = 1
                    }
                    1 -> {
                        icalListViewModel.searchComponent = "NOTE"
                        gotodateMenuItem.isVisible = false     // no date search for notes
                        staggeredGridLayoutManager!!.spanCount = 2
                    }
                    2 -> {
                        icalListViewModel.searchComponent = "TODO"
                        gotodateMenuItem.isVisible = false     // no date search for notes
                        staggeredGridLayoutManager!!.spanCount = 1
                    }
                    else -> {
                        icalListViewModel.searchComponent = "JOURNAL"
                        gotodateMenuItem.isVisible = true
                        staggeredGridLayoutManager!!.spanCount = 1
                    }
                }
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

        // initialize the floating action button only onStart, otherwise the fragment might not be created yet
        val fab: View = requireNotNull(activity).findViewById(R.id.fab)
        fab.setOnClickListener { _ ->

            val newICalObject =
                    when(binding.tabLayoutJournalNotes.selectedTabPosition) {
                        0 -> ICalEntity(ICalObject.createJournal())
                        1 -> ICalEntity(ICalObject.createNote())
                        2 -> ICalEntity(ICalObject.createTodo())
                        else -> ICalEntity(ICalObject.createJournal())
                    }
            this.findNavController().navigate(
                    IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(newICalObject))
        }

        super.onStart()
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

// START Set up Datepicker
            val c = Calendar.getInstance()
                c.timeInMillis = System.currentTimeMillis()

                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)
                val dpd = DatePickerDialog(activity!!, this, year, month, day)


                val startItem = icalListViewModel.vJournalList.value?.lastOrNull()
                val endItem = icalListViewModel.vJournalList.value?.firstOrNull()


                if (startItem?.property?.dtstart != null && endItem?.property?.dtend != null) {
                    dpd.datePicker.minDate = startItem.property.dtstart!!
                    dpd.datePicker.maxDate = endItem.property.dtstart!!
                }

                dpd.show()
            }

        if (item.itemId == R.id.menu_list_filter) {

            this.findNavController().navigate(
                    IcalListFragmentDirections.actionIcalListFragmentToIcalFilterFragment().apply {
                        this.category2preselect = icalListViewModel.searchCategories.toTypedArray()
                        this.classification2preselect = icalListViewModel.searchClassification.toIntArray()
                        this.status2preselect = icalListViewModel.searchStatus.toIntArray()
                        this.collection2preselect = icalListViewModel.searchCollection.toTypedArray()
                        this.component2preselect = icalListViewModel.searchComponent
                    })
        }

        if (item.itemId == R.id.menu_list_clearfilter) {
            icalListViewModel.clearFilter()
        }

        if (item.itemId == R.id.menu_list_add_journal) {
            val newICalObject = ICalEntity(ICalObject.createJournal())
            this.findNavController().navigate(
                IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(newICalObject))
        }

        if (item.itemId == R.id.menu_list_add_note) {
            val newICalObject = ICalEntity(ICalObject.createNote())

            this.findNavController().navigate(
                    IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(newICalObject))
        }

        if (item.itemId == R.id.menu_list_add_todo) {
            val newICalObject = ICalEntity(ICalObject.createTodo())

            this.findNavController().navigate(
                    IcalListFragmentDirections.actionIcalListFragmentToIcalEditFragment(newICalObject))
        }




        return super.onOptionsItemSelected(item)
    }





    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {

        // create a Calendar Object out of the selected dates
        val selectedDate = Calendar.getInstance()
        selectedDate.set(Calendar.YEAR, year)
        selectedDate.set(Calendar.MONTH, month)
        selectedDate.set(Calendar.DAY_OF_MONTH, day)

        // find the item with the same date
        var foundItem = icalListViewModel.vJournalList.value?.find { item ->
            val cItem = Calendar.getInstance()
            cItem.timeInMillis = item.property.dtstart?: 0L

            // if this condition is true, the item is considered as found
            cItem.get(Calendar.YEAR) == year && cItem.get(Calendar.MONTH) == month && cItem.get(Calendar.DAY_OF_MONTH) == day
        }

        // if no exact match was found, find the closest match
        if (foundItem == null) {
            var datediff = 0L
            icalListViewModel.vJournalList.value?.forEach { item ->
                val cItem = Calendar.getInstance()
                cItem.timeInMillis = item.property.dtstart?: 0L

                if (datediff == 0L || kotlin.math.abs(cItem.timeInMillis - selectedDate.timeInMillis) < datediff) {
                    datediff = kotlin.math.abs(cItem.timeInMillis - selectedDate.timeInMillis)
                    foundItem = item

                }
            }
        }

        if (foundItem != null)
            icalListViewModel.setFocusItem(foundItem!!.property.id)

    }
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


