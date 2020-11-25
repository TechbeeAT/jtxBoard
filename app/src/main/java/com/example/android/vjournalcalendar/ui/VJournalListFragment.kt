package com.example.android.vjournalcalendar.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.vjournalcalendar.R
import com.example.android.vjournalcalendar.database.VJournalDatabase
import com.example.android.vjournalcalendar.database.vJournalItem
import com.example.android.vjournalcalendar.databinding.FragmentVjournalListBinding

class VJournalListFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var vJournalList: MutableList<vJournalItem>
    lateinit var vJournalListAdapter: VJournalListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentVjournalListBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_vjournal_list, container, false)

        val application = requireNotNull(this.activity).application

        val dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao

        /*
        val viewModelFactory = VJournalListViewModelFactory(dataSource, application)
        val vJournalListViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalListViewModel::class.java)
        binding.vJournalListViewModel = vJournalListViewModel

         */


        // binding.setLifecycleOwner(this)
        binding.lifecycleOwner = this


        recyclerView = container!!.findViewById(R.id.vjournal_list_items_recycler_view)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.setHasFixedSize(true)

        vJournalList?.add(vJournalItem(1, "desc", System.currentTimeMillis(),"comm"))
        vJournalList?.add(vJournalItem(2, "desc", System.currentTimeMillis(),"comm"))
        vJournalList?.add(vJournalItem(3, "desc", System.currentTimeMillis(),"comm"))
        vJournalList?.add(vJournalItem(4, "desc", System.currentTimeMillis(),"comm"))

        vJournalListAdapter = VJournalListAdapter(application.applicationContext, vJournalList!!)




        return binding.root
    }
}
