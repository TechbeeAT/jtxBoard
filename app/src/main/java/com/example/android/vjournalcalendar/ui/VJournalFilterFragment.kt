package com.example.android.vjournalcalendar.ui



import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.vjournalcalendar.*
import com.example.android.vjournalcalendar.database.VJournalDatabase
import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.databinding.FragmentVjournalFilterBinding
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_vjournal_item_categories_chip.view.*
import java.util.*


class VJournalFilterFragment : Fragment()  {

    lateinit var binding: FragmentVjournalFilterBinding
    lateinit var application: Application
    lateinit var dataSource: VJournalDatabaseDao
    lateinit var viewModelFactory:  VJournalFilterViewModelFactory
    lateinit var vJournalFilterViewModel: VJournalFilterViewModel
    lateinit var inflater: LayoutInflater

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.

        this.inflater = inflater
        this.binding = FragmentVjournalFilterBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao

        //val arguments = VJournalItemEditFragmentArgs.fromBundle((arguments!!))

        // add menu
        setHasOptionsMenu(true)


        this.viewModelFactory = VJournalFilterViewModelFactory(dataSource, application)
        vJournalFilterViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalFilterViewModel::class.java)

        binding.model = vJournalFilterViewModel
        binding.lifecycleOwner = this




        vJournalFilterViewModel.allCategories.observe(viewLifecycleOwner, {

            // Add the chips for categories
            if (vJournalFilterViewModel.allCategories.value != null)
                addCategoryChips(vJournalFilterViewModel.allCategories.value!!)

        })

        vJournalFilterViewModel.allOrganizers.observe(viewLifecycleOwner, {

            // Add the chips for organizers
            if (vJournalFilterViewModel.allOrganizers.value != null)
                addOrganizerChips(vJournalFilterViewModel.allOrganizers.value!!)
        })







        return binding.root
    }



    fun addCategoryChips(categories: List<String>) {

        categories.forEach() { category ->

            if (category == "")
                return@forEach

            val categoryChip = inflater.inflate(R.layout.fragment_vjournal_filter_chip, binding.categoryFilterChipgroup, false) as Chip
            categoryChip.text = category
            binding.categoryFilterChipgroup.addView(categoryChip)


        }
    }

    fun addOrganizerChips(organizers: List<String>) {

        organizers.forEach() { category ->

            if (category == "")
                return@forEach

            val categoryChip = inflater.inflate(R.layout.fragment_vjournal_filter_chip, binding.organizerFilterChipgroup, false) as Chip
            categoryChip.text = category
            binding.organizerFilterChipgroup.addView(categoryChip)


        }
    }

/*
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vjournal_item_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.vjournal_item_delete) {
            vJournalItemEditViewModel.deleteClicked()
        }
        return super.onOptionsItemSelected(item)
    }

 */
}

