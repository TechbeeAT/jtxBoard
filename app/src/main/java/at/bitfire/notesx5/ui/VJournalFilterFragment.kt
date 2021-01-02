package at.bitfire.notesx5.ui



import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.VCategory
import at.bitfire.notesx5.database.VJournalDatabase
import at.bitfire.notesx5.database.VJournalDatabaseDao
import at.bitfire.notesx5.databinding.FragmentVjournalFilterBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.fragment_vjournal_filter.*
import kotlinx.android.synthetic.main.fragment_vjournal_item.*
import kotlinx.android.synthetic.main.fragment_vjournal_item_categories_chip.view.*
import java.util.*


class VJournalFilterFragment : Fragment()  {

    lateinit var binding: FragmentVjournalFilterBinding
    lateinit var application: Application
    lateinit var dataSource: VJournalDatabaseDao
    lateinit var viewModelFactory:  VJournalFilterViewModelFactory
    lateinit var vJournalFilterViewModel: VJournalFilterViewModel
    lateinit var inflater: LayoutInflater

    private var displayedCategoryChips: MutableList<String> = mutableListOf()
    private var displayedOrganizerChips: MutableList<String> = mutableListOf()
    private var displayedStatusChips: MutableList<String> = mutableListOf()
    private var displayedClassificationChips: MutableList<String> = mutableListOf()
    private var displayedCollectionChips: MutableList<String> = mutableListOf()


    val categories2filter: MutableList<String> = mutableListOf()
    val organizers2filter: MutableList<String> = mutableListOf()
    val status2filter: MutableList<String> = mutableListOf()
    val classification2filter: MutableList<String> = mutableListOf()
    val collection2filter: MutableList<String> = mutableListOf()


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


        // Set Chips for Status and Classification
        val statusItems = resources.getStringArray(R.array.vjournal_status).toList()
        val classificationItems = resources.getStringArray(R.array.vjournal_classification).toList()

        addChips(binding.statusFilterChipgroup, statusItems, displayedStatusChips, status2filter, true)
        addChips(binding.classificationFilterChipgroup, classificationItems, displayedClassificationChips, classification2filter, true)


        // observe and set chips for categories
        vJournalFilterViewModel.allCategories.observe(viewLifecycleOwner, {

            // Add the chips for categories
            if (vJournalFilterViewModel.allCategories.value != null)
                addChips(binding.categoryFilterChipgroup, vJournalFilterViewModel.allCategories.value!!, displayedCategoryChips, categories2filter, false)

        })

        //observe and set list for organizers
        vJournalFilterViewModel.allOrganizers.observe(viewLifecycleOwner, {

            // Add the chips for collections
            if (vJournalFilterViewModel.allOrganizers.value != null)
                addChips(binding.organizerFilterChipgroup, vJournalFilterViewModel.allOrganizers.value!!, displayedOrganizerChips, organizers2filter, false)
        })



        return binding.root
    }


    /**
     * Generic method to add a Chip to a Chip to the passed [chipGroup].
     * [displayed] is a MutableList that saves the already created Chips in order to not display the same category twice (can be an issue especially with loading of data from the DB.
     * [selected] takes a MutableList to save the selected items
     * [returnIndex] == true when the index of the selected item should be stored in [selected] (relevant for Status and Classification), otherwise the displayed String is stored in [selected]
     */
    fun addChips(chipGroup: ChipGroup, list: List<String>, displayed: MutableList<String>, selected: MutableList<String>, returnIndex: Boolean)  {

        list.forEach() { listItem ->

            if (listItem == "" || displayed.contains(listItem))   // don't show empty items and only show items that are not there yet
                return@forEach

            val chip = inflater.inflate(R.layout.fragment_vjournal_filter_chip, chipGroup, false) as Chip
            chip.text = listItem
            chipGroup.addView(chip)
            displayed.add(listItem)

            chip.setOnCheckedChangeListener { _, isChecked ->
                // Responds to chip checked/unchecked
                if(isChecked)
                    // If returnIndex == true, add the Int Index to the List, otherwise add the name
                    if(returnIndex)
                        selected.add(list.indexOf(listItem).toString())
                    else
                        selected.add(listItem)
                // If returnIndex == true, remove the Int Index to the List, otherwise remove the name
                if(!isChecked)
                    if(returnIndex)
                        selected.remove(list.indexOf(listItem).toString())
                    else
                        selected.remove(listItem)
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
        inflater.inflate(R.menu.menu_vjournal_filter, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.vjournal_filter_reset -> {
                binding.organizerFilterChipgroup.clearCheck()
                binding.statusFilterChipgroup.clearCheck()
                binding.classificationFilterChipgroup.clearCheck()
                binding.categoryFilterChipgroup.clearCheck()
            }
            R.id.vjournal_filter_apply -> {
                //val selectedCategoryArray = arrayOf(category)     // convert to array
                // Responds to chip click


                val direction = VJournalFilterFragmentDirections.actionVJournalFilterFragmentToVjournalListFragmentList()
                direction.status2filter = status2filter.toTypedArray()
                direction.classification2filter = classification2filter.toTypedArray()
                direction.organizer2filter = organizers2filter.toTypedArray()
                direction.category2filter = categories2filter.toTypedArray()

                this.findNavController().navigate(direction)
            }
        }


        return super.onOptionsItemSelected(item)
    }


}
