package com.example.android.vjournalcalendar.ui

import android.app.Application
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.vjournalcalendar.R
import com.example.android.vjournalcalendar.database.VJournalDatabase
import com.example.android.vjournalcalendar.databinding.FragmentVjournalListBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import java.lang.Math.abs
import java.util.*


class VJournalListFragment : Fragment(),  DatePickerDialog.OnDateSetListener{

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


        val arguments = VJournalListFragmentArgs.fromBundle((arguments!!))

        // set the filter String, default is "%"
        //TODO add other filter criteria
        vJournalListViewModel.setFilter(vJournalListViewModel.SEARCH_GLOBAL, arguments.categoryFilterString)



        // Observe the vjournalList for Changes, on any change the recycler view must be updated, additionally the Focus Item might be updated
        vJournalListViewModel.vJournalList.observe(viewLifecycleOwner, Observer {
            vJournalListAdapter!!.notifyDataSetChanged()

            if (arguments.vJournalItemId != 0L) {
                Log.println(Log.INFO, "vJournalListFragment", arguments.vJournalItemId.toString())
                vJournalListViewModel.setFocusItem(arguments.vJournalItemId)
            }

        })


        // Observe the focus item to scroll automatically to the right position (newly updated or inserted item)
        vJournalListViewModel.vJournalFocusItem.observe(viewLifecycleOwner, Observer {

            val pos = vJournalListViewModel.getFocusItemPosition()
            //Log.println(Log.INFO, "vJournalListViewModel", "Item Position: ${pos.toString()}")

            if (pos != null && pos != -1)
                recyclerView?.smoothScrollToPosition(pos)
        })

        binding.tabLayoutJournalNotes.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> {
                        vJournalListViewModel.setFilter(vJournalListViewModel.SEARCH_COMPONENT, "JOURNAL")
                        gotodateMenuItem.isVisible = true
                    }
                    1 -> {
                        vJournalListViewModel.setFilter(vJournalListViewModel.SEARCH_COMPONENT, "NOTE")
                        gotodateMenuItem.isVisible = false     // no date search for notes
                    }
                    else -> {
                        vJournalListViewModel.setFilter(vJournalListViewModel.SEARCH_COMPONENT, "JOURNAL")
                        gotodateMenuItem.isVisible = true
                    }
                }
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
                    VJournalListFragmentDirections.actionVjournalListFragmentListToVJournalItemEditFragment().setVJournalItemEditId(0))
        }
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vjournal_list, menu)

        // START Set up Search
        val searchMenuItem = menu.findItem(R.id.vjournal_list_search)
        val searchView = searchMenuItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNullOrEmpty())
                    vJournalListViewModel.setFilter(vJournalListViewModel.SEARCH_GLOBAL, "%")      // todo handle more
                else
                    vJournalListViewModel.setFilter(vJournalListViewModel.SEARCH_GLOBAL, "%$query%")

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNullOrEmpty())
                    vJournalListViewModel.setFilter(vJournalListViewModel.SEARCH_GLOBAL, "%")      // todo handle more
                else
                    vJournalListViewModel.setFilter(vJournalListViewModel.SEARCH_GLOBAL, "%$newText%")
                return false
            }
        })

        // END Set up Search

        // START Set up Datepicker
        gotodateMenuItem = menu.findItem(R.id.vjournal_list_gotodate)
        gotodateMenuItem.setOnMenuItemClickListener {

            val c = Calendar.getInstance()
            c.timeInMillis = System.currentTimeMillis()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val dpd = DatePickerDialog(activity!!, this, year, month, day)


            val startItem = vJournalListViewModel.vJournalList.value?.lastOrNull()
            val endItem = vJournalListViewModel.vJournalList.value?.firstOrNull()


            if (startItem != null && endItem != null) {
                dpd.datePicker.minDate = startItem.dtstart
                dpd.datePicker.maxDate = endItem.dtstart
            }

            dpd.show()
            true


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
        }

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
            cItem.timeInMillis = item.dtstart

            // if this condition is true, the item is considered as found
            cItem.get(Calendar.YEAR) == year && cItem.get(Calendar.MONTH) == month && cItem.get(Calendar.DAY_OF_MONTH) == day
        }

        // if no exact match was found, find the closest match
        if (foundItem == null) {
            var datediff = 0L
            vJournalListViewModel.vJournalList.value?.forEach { item ->
                val cItem = Calendar.getInstance()
                cItem.timeInMillis = item.dtstart

                if (datediff == 0L || kotlin.math.abs(cItem.timeInMillis - selectedDate.timeInMillis) < datediff) {
                    datediff = kotlin.math.abs(cItem.timeInMillis - selectedDate.timeInMillis)
                    foundItem = item

                }
            }
        }

        if (foundItem != null)
           vJournalListViewModel.setFocusItem(foundItem!!.id)

    }
}
