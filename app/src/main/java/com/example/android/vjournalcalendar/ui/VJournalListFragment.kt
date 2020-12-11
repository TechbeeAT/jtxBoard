package com.example.android.vjournalcalendar.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.vjournalcalendar.R
import com.example.android.vjournalcalendar.database.VJournalDatabase
import com.example.android.vjournalcalendar.database.vJournalItem
import com.example.android.vjournalcalendar.databinding.FragmentVjournalItemBinding
import com.example.android.vjournalcalendar.databinding.FragmentVjournalListBinding
import com.google.android.material.snackbar.Snackbar

class VJournalListFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var vJournalListAdapter: VJournalListAdapter? = null

    private lateinit var vJournalListViewModel: VJournalListViewModel




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentVjournalListBinding = FragmentVjournalListBinding.inflate(inflater, container, false)

        // set up DB DAO
        val application = requireNotNull(this.activity).application
        val dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao

        // create the view model through the view model factory
        val viewModelFactory = VJournalListViewModelFactory(dataSource, application)
        vJournalListViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalListViewModel::class.java)

        binding.vJournalListViewModel = vJournalListViewModel
        binding.lifecycleOwner = this

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
        vJournalListViewModel.setCategoryFilter(arguments.categoryFilterString)



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

            if (pos != null)
                recyclerView?.scrollToPosition(pos)
        })



        return binding.root

    }

    override fun onStart() {


        // initialize the floating action button only onStart, otherwise the fragment might not be created yet
        val fab: View = requireNotNull(activity).findViewById(R.id.fab)
        fab.setOnClickListener { view ->

            this.findNavController().navigate(
                    VJournalListFragmentDirections.actionVjournalListFragmentListToVJournalItemEditFragment().setVJournalItemEditId(0))

        }
        super.onStart()
    }

}
