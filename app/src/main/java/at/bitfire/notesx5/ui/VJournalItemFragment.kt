package at.bitfire.notesx5.ui

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.bitfire.notesx5.R
import at.bitfire.notesx5.convertLongToDateString
import at.bitfire.notesx5.convertLongToTimeString
import at.bitfire.notesx5.database.VCategory
import at.bitfire.notesx5.database.VJournalDatabase
import at.bitfire.notesx5.database.VJournalDatabaseDao
import at.bitfire.notesx5.databinding.FragmentVjournalItemBinding
import com.google.android.material.chip.Chip
import java.util.*


class VJournalItemFragment : Fragment() {

    lateinit var binding: FragmentVjournalItemBinding
    lateinit var application: Application
    lateinit var dataSource: VJournalDatabaseDao
    lateinit var viewModelFactory: VJournalItemViewModelFactory
    lateinit var vJournalItemViewModel: VJournalItemViewModel
    lateinit var inflater: LayoutInflater

    var displayedCategoryChips = mutableListOf<VCategory>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentVjournalItemBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao

        val arguments = VJournalItemFragmentArgs.fromBundle((arguments!!))

        // add menu
        setHasOptionsMenu(true)


        // set up view model
        viewModelFactory = VJournalItemViewModelFactory(arguments.item2show, dataSource, application)
        vJournalItemViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalItemViewModel::class.java)

        binding.model = vJournalItemViewModel
        binding.lifecycleOwner = this


        // set up observers
        vJournalItemViewModel.editingClicked.observe(viewLifecycleOwner, Observer {
            if (it) {
                vJournalItemViewModel.editingClicked.value = false
                this.findNavController().navigate(
                        VJournalItemFragmentDirections.actionVjournalItemFragmentToVJournalItemEditFragment().setItem2edit(vJournalItemViewModel.vJournal.value!!.vJournalItem.id))
            }
        })

        vJournalItemViewModel.vJournal.observe(viewLifecycleOwner, {

            if (it?.vJournalItem != null) {

                val statusArray = resources.getStringArray(R.array.vjournal_status)
                binding.statusChip.text = statusArray[vJournalItemViewModel.vJournal.value!!.vJournalItem.status]

                val classificationArray = resources.getStringArray(R.array.vjournal_classification)
                binding.classificationChip.text = classificationArray[vJournalItemViewModel.vJournal.value!!.vJournalItem.classification]
            }
        })

        vJournalItemViewModel.vCategory.observe(viewLifecycleOwner, {
            if (it != null)
                addChips(vJournalItemViewModel.vJournal.value!!.vCategory!!)
        })

        return binding.root
    }

    // adds Chips to the categoriesChipgroup based on the categories List
    private fun addChips(categories: List<VCategory>) {

        categories.forEach() { category ->

            if (category.categories == "")     // don't add empty categories
                return@forEach

            if(displayedCategoryChips.indexOf(category) != -1)    // only show categories that are not there yet
                return@forEach

            val categoryChip = inflater.inflate(R.layout.fragment_vjournal_item_categories_chip, binding.categoriesChipgroup, false) as Chip
            categoryChip.text = category.categories
            binding.categoriesChipgroup.addView(categoryChip)
            displayedCategoryChips.add(category)

            categoryChip.setOnClickListener {

                val selectedCategoryArray = arrayOf(category.categories)     // convert to array
                // Responds to chip click
                this.findNavController().navigate(
                        VJournalItemFragmentDirections.actionVjournalItemFragmentToVjournalListFragmentList().setCategory2filter(selectedCategoryArray)
                )
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vjournal_item, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.vjournal_item_share) {

            var shareText: String = "${convertLongToDateString(vJournalItemViewModel.vJournal.value!!.vJournalItem.dtstart)} ${convertLongToTimeString(vJournalItemViewModel.vJournal.value!!.vJournalItem.dtstart)}\n"
            shareText += "${vJournalItemViewModel.vJournal.value!!.vJournalItem.summary}\n\n"
            shareText += "${vJournalItemViewModel.vJournal.value!!.vJournalItem.description}\n\n"
            //todo add category again
            //shareText += "Categories/Labels: ${vJournalItemViewModel.vJournal.value!!.vCategory}"

            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, vJournalItemViewModel.vJournal.value!!.vJournalItem.summary)
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent(shareIntent))


        }
        return super.onOptionsItemSelected(item)
    }

}

