package com.example.android.vjournalcalendar.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.vjournalcalendar.R
import com.example.android.vjournalcalendar.database.VJournalDatabase
import com.example.android.vjournalcalendar.databinding.FragmentVjournalListBinding

class VJournalListFragment: Fragment() {

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

        // binding.setLifecycleOwner(this)
        binding.lifecycleOwner = this

        return binding.root
    }
}
