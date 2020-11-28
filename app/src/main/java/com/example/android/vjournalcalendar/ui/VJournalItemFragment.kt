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
import com.example.android.vjournalcalendar.databinding.FragmentVjournalItemBinding


class VJournalItemFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.

        val binding: FragmentVjournalItemBinding = FragmentVjournalItemBinding.inflate(inflater, container, false)

        val itemApplication = requireNotNull(this.activity).application

        val dataSource = VJournalDatabase.getInstance(itemApplication).vJournalDatabaseDao



        val viewModelFactory = VJournalItemViewModelFactory(dataSource, itemApplication)
        val vJournalItemViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalItemViewModel::class.java)
        binding.vJournalItemViewModel = vJournalItemViewModel



        binding.lifecycleOwner = this


        return binding.root
    }
}

