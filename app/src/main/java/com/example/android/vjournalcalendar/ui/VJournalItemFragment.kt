package com.example.android.vjournalcalendar.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.android.vjournalcalendar.database.VJournalDatabase
import com.example.android.vjournalcalendar.databinding.FragmentVjournalItemBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.*


class VJournalItemFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.

        val binding: FragmentVjournalItemBinding = FragmentVjournalItemBinding.inflate(inflater, container, false)

        val itemApplication = requireNotNull(this.activity).application

        val dataSource = VJournalDatabase.getInstance(itemApplication).vJournalDatabaseDao

        val arguments = VJournalItemFragmentArgs.fromBundle((arguments!!))



        val viewModelFactory = VJournalItemViewModelFactory(arguments.vJournalItemId, dataSource, itemApplication)
        val vJournalItemViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalItemViewModel::class.java)

        binding.vJournalItemViewModel = vJournalItemViewModel
        binding.lifecycleOwner = this


        vJournalItemViewModel.editingClicked.observe(viewLifecycleOwner, Observer {
            if (it) {
                vJournalItemViewModel.editingClicked.value = false
                this.findNavController().navigate(
                        VJournalItemFragmentDirections.actionVjournalItemFragmentToVJournalItemEditFragment().setVJournalItemEditId(vJournalItemViewModel.vJournalItem.value!!.id))
            }
        })


        /*
        val chip = inflater.inflate(R.layout.fragment_vjournal_item_categories_chip, binding.categoriesChipgroup, false) as Chip
        chip.text = ("Chip #1")
        //binding.categoriesChipgroup.addView(chip)
*/


        return binding.root
    }




}

