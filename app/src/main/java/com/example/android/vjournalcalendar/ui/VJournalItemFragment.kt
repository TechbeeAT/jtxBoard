package com.example.android.vjournalcalendar.ui

import android.app.Application
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
import com.example.android.vjournalcalendar.R
import com.example.android.vjournalcalendar.convertCategoriesCSVtoList
import com.example.android.vjournalcalendar.database.VJournalDatabase
import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.databinding.FragmentVjournalItemBinding
import com.example.android.vjournalcalendar.databinding.FragmentVjournalItemEditBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.*


class VJournalItemFragment : Fragment() {

    lateinit var binding: FragmentVjournalItemBinding
    lateinit var application: Application
    lateinit var dataSource: VJournalDatabaseDao
    lateinit var viewModelFactory:  VJournalItemViewModelFactory
    lateinit var vJournalItemViewModel: VJournalItemViewModel
    lateinit var inflater: LayoutInflater


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentVjournalItemBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao

        val arguments = VJournalItemFragmentArgs.fromBundle((arguments!!))



        viewModelFactory = VJournalItemViewModelFactory(arguments.vJournalItemId, dataSource, application)
        vJournalItemViewModel =
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

        vJournalItemViewModel.vJournalItem.observe(viewLifecycleOwner, {

            if (vJournalItemViewModel.vJournalItem.value != null)
                addChips(convertCategoriesCSVtoList(vJournalItemViewModel.vJournalItem.value!!.categories))

        })




        return binding.root
    }



    fun addChips(categories: List<String>) {

        categories.forEach() { category ->

            if (category == "")
                return@forEach


            val categoryChip = inflater.inflate(R.layout.fragment_vjournal_item_categories_chip, binding.categoriesChipgroup, false) as Chip
            categoryChip.text = category
            binding.categoriesChipgroup.addView(categoryChip)

            categoryChip.setOnClickListener {

                // Responds to chip click
                this.findNavController().navigate(
                        VJournalItemFragmentDirections.actionVjournalItemFragmentToVjournalListFragmentList().setCategoryFilterString(category)
                )
            }

        }
    }

}

