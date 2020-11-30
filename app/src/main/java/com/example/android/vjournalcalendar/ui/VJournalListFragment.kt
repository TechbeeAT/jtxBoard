package com.example.android.vjournalcalendar.ui

import android.os.Bundle
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



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentVjournalListBinding = FragmentVjournalListBinding.inflate(inflater, container, false)

        // set up DB DAO
        val application = requireNotNull(this.activity).application
        val dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao

        // create the view model through the view model factory
        val viewModelFactory = VJournalListViewModelFactory(dataSource, application)
        val vJournalListViewModel =
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
        vJournalListAdapter = VJournalListAdapter(application.applicationContext, vJournalListViewModel.vjournalList, vJournalListViewModel.vjournaListCount)


        // make sure the list gets updated with observers
        vJournalListViewModel.vjournaListCount.observe(viewLifecycleOwner, Observer {
            vJournalListAdapter!!.notifyDataSetChanged()
        })

        vJournalListViewModel.vjournalList.observe(viewLifecycleOwner, Observer {
            vJournalListAdapter!!.notifyDataSetChanged()
        })


        recyclerView?.adapter = vJournalListAdapter



        val fab: View = requireNotNull(activity).findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show()

            this.findNavController().navigate(
                    VJournalListFragmentDirections.actionVjournalListFragmentListToVJournalItemFragment().setVJournalItemId(0))

            fab.visibility = View.INVISIBLE
        }


        return binding.root

    }
}
