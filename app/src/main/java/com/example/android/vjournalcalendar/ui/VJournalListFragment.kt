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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.vjournalcalendar.R
import com.example.android.vjournalcalendar.database.VJournalDatabase
import com.example.android.vjournalcalendar.database.vJournalItem
import com.example.android.vjournalcalendar.databinding.FragmentVjournalListBinding

class VJournalListFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private lateinit var vJournalList: LiveData<List<vJournalItem>>
    private var vJournalListAdapter: VJournalListAdapter? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.

        val binding: FragmentVjournalListBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_vjournal_list, container, false)



        val application = requireNotNull(this.activity).application
        val dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao



        val viewModelFactory = VJournalListViewModelFactory(dataSource, application)
        val vJournalListViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalListViewModel::class.java)
        binding.vJournalListViewModel = vJournalListViewModel



        binding.lifecycleOwner = this

        recyclerView = binding.vjournalListItemsRecyclerView
        linearLayoutManager = LinearLayoutManager(application.applicationContext)
        recyclerView?.layoutManager = linearLayoutManager

        recyclerView?.setHasFixedSize(true)



        vJournalList = vJournalListViewModel.vjournalList

        vJournalListAdapter = VJournalListAdapter(application.applicationContext, vJournalListViewModel.vjournalList, vJournalListViewModel.vjournaListCount)


        vJournalListViewModel.vjournaListCount.observe(viewLifecycleOwner, Observer {
            vJournalListAdapter!!.notifyDataSetChanged()
        })

        vJournalListViewModel.vjournalList.observe(viewLifecycleOwner, Observer {
            vJournalListAdapter!!.notifyDataSetChanged()
        })


        recyclerView?.adapter = vJournalListAdapter

        return binding.root


    }
}
