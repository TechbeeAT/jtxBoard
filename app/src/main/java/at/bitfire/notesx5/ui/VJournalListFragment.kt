package at.bitfire.notesx5.ui


import android.app.Application
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.DatePicker
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.bitfire.notesx5.R
import at.bitfire.notesx5.database.VJournalDatabase
import at.bitfire.notesx5.databinding.FragmentVjournalListBinding
import com.google.android.material.tabs.TabLayout
import java.util.*


class VJournalListFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private var recyclerView: RecyclerView? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var vJournalListAdapter: VJournalListAdapter? = null

    private lateinit var vJournalListViewModel: VJournalListViewModel

    private lateinit var binding: FragmentVjournalListBinding
    private lateinit var application: Application

    private lateinit var gotodateMenuItem: MenuItem


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {


        // Get a reference to the binding object and inflate the fragment views.
        binding = FragmentVjournalListBinding.inflate(inflater, container, false)

        // set up DB DAO
        application = requireNotNull(this.activity).application
        val dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao


        // create the view model through the view model factory
        val viewModelFactory = VJournalListViewModelFactory(dataSource, application)
        vJournalListViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalListViewModel::class.java)

        binding.vJournalListViewModel = vJournalListViewModel
        binding.lifecycleOwner = this

        // add menu
        setHasOptionsMenu(true)


        // set up recycler view
        recyclerView = binding.vjournalListItemsRecyclerView
        linearLayoutManager = LinearLayoutManager(application.applicationContext)
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.setHasFixedSize(true)

        // create adapter and provide data
        //vJournalListAdapter = VJournalListAdapter(application.applicationContext, vJournalListViewModel.vjournalList, vJournalListViewModel.vjournaListCount)
        vJournalListAdapter = VJournalListAdapter(application.applicationContext, vJournalListViewModel.vJournalList)

        recyclerView?.adapter = vJournalListAdapter


        // pass filter arguments to view model
        val arguments = VJournalListFragmentArgs.fromBundle((arguments!!))
        if (!arguments.category2filter.isNullOrEmpty())
            vJournalListViewModel.searchCategories = arguments.category2filter!!.toMutableList()

        if (!arguments.organizer2filter.isNullOrEmpty())
            vJournalListViewModel.searchOrganizer = arguments.organizer2filter!!.toMutableList()

        if (!arguments.classification2filter.isNullOrEmpty())
            vJournalListViewModel.searchClassification = arguments.classification2filter!!.toMutableList()

        if (!arguments.status2filter.isNullOrEmpty())
            vJournalListViewModel.searchStatus = arguments.status2filter!!.toMutableList()

        if (!arguments.collection2filter.isNullOrEmpty())
            vJournalListViewModel.searchCollection = arguments.collection2filter!!.toMutableList()





        // Observe the vjournalList for Changes, on any change the recycler view must be updated, additionally the Focus Item might be updated
        vJournalListViewModel.vJournalList.observe(viewLifecycleOwner, Observer {
            vJournalListAdapter!!.notifyDataSetChanged()

            if (arguments.item2focus != 0L) {
                //Log.println(Log.INFO, "vJournalListFragment", arguments.vJournalItemId.toString())
                vJournalListViewModel.setFocusItem(arguments.item2focus)
            }
        })

        vJournalListViewModel.vJournalList.observe(viewLifecycleOwner, {
            if (vJournalListViewModel.vJournalList.value?.size!! > 0)
                Log.println(Log.INFO, "getVJournalItemswithComments", vJournalListViewModel.vJournalList.value!![0]!!.vJournalItem.summary.toString())
            
            vJournalListViewModel.vJournalList.value?.forEach { it ->

                it.vComment?.forEach {
                    Log.println(Log.INFO, "getVJournalItemswithComments", it.comment.toString())
                }

                it.vCategory?.forEach {
                    Log.println(Log.INFO, "getVJournalItemswithComments", it.categories.toString())
                }

            }

        })


        // Observe the focus item to scroll automatically to the right position (newly updated or inserted item)
        vJournalListViewModel.focusItemId.observe(viewLifecycleOwner, Observer {

            val pos = vJournalListViewModel.getFocusItemPosition()
            Log.println(Log.INFO, "vJournalListViewModel", "Item Position: ${pos.toString()}")

            recyclerView?.scrollToPosition(pos)
            //vJournalListViewModel.resetFocusItem()
            Log.println(Log.INFO, "vJournalListViewModel", "Scrolling now to: ${pos.toString()}")

        })

        binding.tabLayoutJournalNotes.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                vJournalListViewModel.resetFocusItem()

                when (tab?.position) {
                    0 -> {
                        vJournalListViewModel.searchComponent = "JOURNAL"
                        gotodateMenuItem.isVisible = true
                    }
                    1 -> {
                        vJournalListViewModel.searchComponent = "NOTE"
                        gotodateMenuItem.isVisible = false     // no date search for notes
                    }
                    else -> {
                        vJournalListViewModel.searchComponent = "JOURNAL"
                        gotodateMenuItem.isVisible = true
                    }
                }
                vJournalListViewModel.updateSearch()

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

            this.findNavController().navigate(
                    VJournalListFragmentDirections.actionVjournalListFragmentListToVJournalItemEditFragment().setItem2edit(0))
        }

        super.onStart()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vjournal_list, menu)


        // Tell the variable the menu item to later make it visible or invisible
        gotodateMenuItem = menu.findItem(R.id.vjournal_list_gotodate)

        // add listener for search!
        val searchMenuItem = menu.findItem(R.id.vjournal_list_search)
        val searchView = searchMenuItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                // nothing to do as the the search is already updated with the text input
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {

                if (query.isEmpty())
                    vJournalListViewModel.searchText = "%"
                else
                    vJournalListViewModel.searchText = "%$query%"

                vJournalListViewModel.updateSearch()
                return false
            }
        })



    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.vjournal_list_gotodate) {

// START Set up Datepicker
            val c = Calendar.getInstance()
                c.timeInMillis = System.currentTimeMillis()

                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)
                val dpd = DatePickerDialog(activity!!, this, year, month, day)


                val startItem = vJournalListViewModel.vJournalList.value?.lastOrNull()
                val endItem = vJournalListViewModel.vJournalList.value?.firstOrNull()


                if (startItem != null && endItem != null) {
                    dpd.datePicker.minDate = startItem.vJournalItem.dtstart
                    dpd.datePicker.maxDate = endItem.vJournalItem.dtstart
                }

                dpd.show()
            }

        if (item.itemId == R.id.vjournal_filter) {

            this.findNavController().navigate(
                    VJournalListFragmentDirections.actionVjournalListFragmentListToVJournalFilterFragment())
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
        var foundItem = vJournalListViewModel.vJournalList.value?.find { item ->
            val cItem = Calendar.getInstance()
            cItem.timeInMillis = item.vJournalItem.dtstart

            // if this condition is true, the item is considered as found
            cItem.get(Calendar.YEAR) == year && cItem.get(Calendar.MONTH) == month && cItem.get(Calendar.DAY_OF_MONTH) == day
        }

        // if no exact match was found, find the closest match
        if (foundItem == null) {
            var datediff = 0L
            vJournalListViewModel.vJournalList.value?.forEach { item ->
                val cItem = Calendar.getInstance()
                cItem.timeInMillis = item.vJournalItem.dtstart

                if (datediff == 0L || kotlin.math.abs(cItem.timeInMillis - selectedDate.timeInMillis) < datediff) {
                    datediff = kotlin.math.abs(cItem.timeInMillis - selectedDate.timeInMillis)
                    foundItem = item

                }
            }
        }

        if (foundItem != null)
            vJournalListViewModel.setFocusItem(foundItem!!.vJournalItem.id)

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


