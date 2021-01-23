package at.bitfire.notesx5.ui



import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.ICalDatabase
import at.bitfire.notesx5.database.ICalDatabaseDao
import at.bitfire.notesx5.databinding.FragmentIcalFilterBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*


class IcalFilterFragment : Fragment()  {

    lateinit var binding: FragmentIcalFilterBinding
    lateinit var application: Application
    lateinit var dataSource: ICalDatabaseDao
    lateinit var viewModelFactory:  IcalFilterViewModelFactory
    lateinit var icalFilterViewModel: IcalFilterViewModel
    lateinit var inflater: LayoutInflater

    private var displayedCategoryChips: MutableList<String> = mutableListOf()
    private var displayedStatusChips: MutableList<String> = mutableListOf()
    private var displayedClassificationChips: MutableList<String> = mutableListOf()
    private var displayedCollectionChips: MutableList<String> = mutableListOf()


    private val categoriesSelected: MutableList<String> = mutableListOf()
    private val statusSelected: MutableList<Int> = mutableListOf()
    private val classificationSelected: MutableList<Int> = mutableListOf()
    private val collectionSelected: MutableList<String> = mutableListOf()

    private var categoriesPreselected: MutableList<String> = mutableListOf()
    private var statusPreselected: MutableList<String> = mutableListOf()
    private var classificationPreselected: MutableList<String> = mutableListOf()
    private var collectionPreselected: MutableList<String> = mutableListOf()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.

        this.inflater = inflater
        this.binding = FragmentIcalFilterBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        //val arguments = VJournalItemEditFragmentArgs.fromBundle((arguments!!))

        // add menu
        setHasOptionsMenu(true)



        this.viewModelFactory = IcalFilterViewModelFactory(dataSource, application)
        icalFilterViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(IcalFilterViewModel::class.java)

        binding.model = icalFilterViewModel
        binding.lifecycleOwner = this



        // get previously selected items from arguments
        val arguments = IcalFilterFragmentArgs.fromBundle((arguments!!))

        if (arguments.category2preselect?.isNotEmpty() == true)
            categoriesPreselected = arguments.category2preselect!!.toMutableList()

        if (arguments.classification2preselect?.isNotEmpty() == true) {
            arguments.classification2preselect!!.forEach {
                classificationPreselected.add(resources.getStringArray(R.array.ical_classification)[it])
            }
        }

        if (arguments.status2preselect?.isNotEmpty() == true) {
            arguments.status2preselect!!.forEach {
                statusPreselected.add(resources.getStringArray(R.array.vjournal_status)[it])
            }
        }

        if (arguments.collection2preselect?.isNotEmpty() == true)
            collectionPreselected = arguments.collection2preselect!!.toMutableList()



        // Set Chips for Status and Classification
        val statusItems = resources.getStringArray(R.array.vjournal_status).toList()
        val classificationItems = resources.getStringArray(R.array.ical_classification).toList()

        addChipsInt(binding.statusFilterChipgroup, statusItems, displayedStatusChips, statusSelected, statusPreselected)
        addChipsInt(binding.classificationFilterChipgroup, classificationItems, displayedClassificationChips, classificationSelected, classificationPreselected)


        // observe and set chips for categories
        icalFilterViewModel.allCategories.observe(viewLifecycleOwner, {

            // Add the chips for categories
            if (icalFilterViewModel.allCategories.value != null)
                addChips(binding.categoryFilterChipgroup, icalFilterViewModel.allCategories.value!!, displayedCategoryChips, categoriesSelected, categoriesPreselected)

        })

        //observe and set list for organizers
        icalFilterViewModel.allCollections.observe(viewLifecycleOwner, {

            // Add the chips for collections
            if (icalFilterViewModel.allCollections.value != null)
                addChips(binding.collectionFilterChipgroup, icalFilterViewModel.allCollections.value!!, displayedCollectionChips, collectionSelected, collectionPreselected)
        })



        return binding.root
    }


    /**
     * Method to add Chips to a [chipGroup] based on the String value for Classification and Status.
     * [displayed] is a MutableList that saves the already created Chips in order to not display the same category twice (can be an issue especially with loading of data from the DB.
     * [selected] takes a MutableList to save the selected items
     * [preselected] items can be passed through arguments
     */
    private fun addChips(chipGroup: ChipGroup, list: List<String>, displayed: MutableList<String>, selected: MutableList<String>, preselected: MutableList<String>)  {

        list.forEach() { listItem ->

            if (listItem == "" || displayed.contains(listItem))   // don't show empty items and only show items that are not there yet
                return@forEach

            val chip = inflater.inflate(R.layout.fragment_ical_filter_chip, chipGroup, false) as Chip
            chip.text = listItem
            chipGroup.addView(chip)
            if(preselected.contains(listItem)) {   // if the current item is in the list of preselected items, then check it
                chip.isChecked = true
                selected.add(listItem)
            }
            displayed.add(listItem)

            chip.setOnCheckedChangeListener { _, isChecked ->
                // Responds to chip checked/unchecked
                if(isChecked)
                        selected.add(listItem)
                // If returnIndex == true, remove the Int Index to the List, otherwise remove the name
                if(!isChecked)
                        selected.remove(listItem)
            }
        }
    }



    /**
     * Method to add Chips to a [chipGroup] based on the Int value for Classification and Status.
     * [displayed] is a MutableList that saves the already created Chips in order to not display the same category twice (can be an issue especially with loading of data from the DB.
     * [selected] takes a MutableList to save the selected items
     * [preselected] items can be passed through arguments
     */
    private fun addChipsInt(chipGroup: ChipGroup, list: List<String>, displayed: MutableList<String>, selected: MutableList<Int>, preselected: MutableList<String>)  {

        list.forEach() { listItem ->

            if (listItem == "" || displayed.contains(listItem))   // don't show empty items and only show items that are not there yet
                return@forEach

            val chip = inflater.inflate(R.layout.fragment_ical_filter_chip, chipGroup, false) as Chip
            chip.text = listItem
            chipGroup.addView(chip)
            if(preselected.contains(listItem)) {   // if the current item is in the list of preselected items, then check it
                chip.isChecked = true
                selected.add(list.indexOf(listItem))
            }
            displayed.add(listItem)

            chip.setOnCheckedChangeListener { _, isChecked ->
                // Responds to chip checked/unchecked
                if(isChecked)
                    selected.add(list.indexOf(listItem))      // add the index of the selected value
                if(!isChecked)
                        selected.remove(list.indexOf(listItem))  // remove the index of the selected value
            }
        }
    }




    /*
    private fun addChips(chipGroup: ChipGroup, chipList: List<String>) {

        chipList.forEach() { chipText ->

            if (chipText == "")
                return@forEach

            val chip = inflater.inflate(R.layout.fragment_vjournal_filter_chip, chipGroup, false) as Chip
            chip.text = chipText
            chipGroup.addView(chip)

        }
    }


    fun addCategoryChips(chipGroup: ChipGroup, categories: List<String>) {

        categories.forEach() { category ->

            if (category == "" || displayedCategoryChips.contains(category))   // don't show empty categories and only show categories that are not there yet
                return@forEach

            val chip = inflater.inflate(R.layout.fragment_vjournal_filter_chip, chipGroup, false) as Chip
            chip.text = category
            chipGroup.addView(chip)
            displayedCategoryChips.add(category)

            chip.setOnCheckedChangeListener { chip, isChecked ->
                // Responds to chip checked/unchecked
                if(isChecked)
                    categories2filter.add(category)
                if(!isChecked)
                    categories2filter.remove(category)
            }

        }
    }

    fun addOrganizerChips(chipGroup: ChipGroup, organizers: List<String>) {

        organizers.forEach() { organizer ->

            if (organizer == "" || displayedOrganizerChips.contains(organizer))   // don't show empty categories and only show categories that are not there yet
                return@forEach

            val chip = inflater.inflate(R.layout.fragment_vjournal_filter_chip, chipGroup, false) as Chip
            chip.text = organizer
            chipGroup.addView(chip)
            displayedOrganizerChips.add(organizer)

            chip.setOnCheckedChangeListener { chip, isChecked ->
                // Responds to chip checked/unchecked
                if(isChecked)
                    organizers2filter.add(organizer)
                if(!isChecked)
                    organizers2filter.remove(organizer)
            }

        }
    }


 */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_ical_filter, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.vjournal_filter_reset -> {
                binding.collectionFilterChipgroup.clearCheck()
                binding.statusFilterChipgroup.clearCheck()
                binding.classificationFilterChipgroup.clearCheck()
                binding.categoryFilterChipgroup.clearCheck()
            }
            R.id.vjournal_filter_apply -> {
                //val selectedCategoryArray = arrayOf(category)     // convert to array
                // Responds to chip click


                val direction = IcalFilterFragmentDirections.actionIcalFilterFragmentToIcalListFragment().apply {
                    this.status2filter = statusSelected.toIntArray()
                    this.classification2filter = classificationSelected.toIntArray()
                    this.collection2filter = collectionSelected.toTypedArray()
                    this.category2filter = categoriesSelected.toTypedArray()
                }


                this.findNavController().navigate(direction)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
