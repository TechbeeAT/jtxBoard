package com.example.android.vjournalcalendar.ui

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.vjournalcalendar.R
import com.example.android.vjournalcalendar.convertCategoriesCSVtoList
import com.example.android.vjournalcalendar.convertLongToDateString
import com.example.android.vjournalcalendar.convertLongToTimeString
import com.example.android.vjournalcalendar.database.VJournalDatabase
import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.databinding.FragmentVjournalItemBinding
import com.google.android.material.chip.Chip
import java.util.*


class VJournalItemFragment : Fragment() {

    lateinit var binding: FragmentVjournalItemBinding
    lateinit var application: Application
    lateinit var dataSource: VJournalDatabaseDao
    lateinit var viewModelFactory: VJournalItemViewModelFactory
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

        // add menu
        setHasOptionsMenu(true)


        // set up view model
        viewModelFactory = VJournalItemViewModelFactory(arguments.vJournalItemId, dataSource, application)
        vJournalItemViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalItemViewModel::class.java)

        binding.vJournalItemViewModel = vJournalItemViewModel
        binding.lifecycleOwner = this


        // set up observers
        vJournalItemViewModel.editingClicked.observe(viewLifecycleOwner, Observer {
            if (it) {
                vJournalItemViewModel.editingClicked.value = false
                this.findNavController().navigate(
                        VJournalItemFragmentDirections.actionVjournalItemFragmentToVJournalItemEditFragment().setVJournalItemEditId(vJournalItemViewModel.vJournalItem.value!!.id))
            }
        })

        vJournalItemViewModel.vJournalItem.observe(viewLifecycleOwner, {

            if (vJournalItemViewModel.vJournalItem.value != null) {
                addChips(convertCategoriesCSVtoList(vJournalItemViewModel.vJournalItem.value!!.categories))

                val statusArray = resources.getStringArray(R.array.vjournal_status)
                binding.statusChip.text = statusArray[vJournalItemViewModel.vJournalItem.value!!.status]

                val classificationArray = resources.getStringArray(R.array.vjournal_classification)
                binding.classificationChip.text = classificationArray[vJournalItemViewModel.vJournalItem.value!!.classification]

            }


        })

        return binding.root
    }

    // adds Chips to the categoriesChipgroup based on the categories List
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


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vjournal_item, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.vjournal_item_share) {

            var shareText: String = "${convertLongToDateString(vJournalItemViewModel.vJournalItem.value!!.dtstart)} ${convertLongToTimeString(vJournalItemViewModel.vJournalItem.value!!.dtstart)}\n"
            shareText +=  "${vJournalItemViewModel.vJournalItem.value!!.summary}\n\n"
            shareText += "${vJournalItemViewModel.vJournalItem.value!!.description}\n\n"
            shareText += "Categories/Labels: ${vJournalItemViewModel.vJournalItem.value!!.categories}"

            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type="text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, vJournalItemViewModel.vJournalItem.value!!.summary)
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent(shareIntent))


        }
        return super.onOptionsItemSelected(item)
    }

}

