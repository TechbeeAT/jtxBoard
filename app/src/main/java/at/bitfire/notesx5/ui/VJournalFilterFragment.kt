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

    val categories2filter: MutableList<String> = mutableListOf()
    val organizers2filter: MutableList<String> = mutableListOf()


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

        addChips(binding.statusFilterChipgroup, statusItems)
        addChips(binding.classificationFilterChipgroup, classificationItems)


        // observe and set chips for categories
        vJournalFilterViewModel.allCategories.observe(viewLifecycleOwner, {

            // Add the chips for categories
            if (vJournalFilterViewModel.allCategories.value != null)
                addCategoryChips(binding.categoryFilterChipgroup, vJournalFilterViewModel.allCategories.value!!)

        })

        //observe and set list for organizers
        vJournalFilterViewModel.allOrganizers.observe(viewLifecycleOwner, {

            // Add the chips for organizers
            if (vJournalFilterViewModel.allOrganizers.value != null)
                addOrganizerChips(binding.organizerFilterChipgroup, vJournalFilterViewModel.allOrganizers.value!!)
        })



        return binding.root
    }



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

                val status2filter = binding.statusFilterChipgroup.checkedChipIds.toIntArray()
                        . map { it. toString() }. toTypedArray()
                val classification2filter = binding.classificationFilterChipgroup.checkedChipIds.toIntArray()
                        . map { it. toString() }. toTypedArray()


                /*
                binding.categoryFilterChipgroup.checkedChipIds.forEach {
                    categories2filter.add(vJournalFilterViewModel.allCategories.value?.get(it)!!)
                }

                val organizers2filter: MutableList<String> = mutableListOf()
                binding.organizerFilterChipgroup.checkedChipIds.forEach {
                    organizers2filter.add(vJournalFilterViewModel.allOrganizers.value?.get(it)!!)
                }

                 */


                val direction = VJournalFilterFragmentDirections.actionVJournalFilterFragmentToVjournalListFragmentList()
                direction.status2filter = status2filter
                direction.classification2filter = classification2filter
                direction.organizer2filter = organizers2filter.toTypedArray()
                direction.category2filter = categories2filter.toTypedArray()

                this.findNavController().navigate(direction)
            }
        }



        return super.onOptionsItemSelected(item)
    }


}
