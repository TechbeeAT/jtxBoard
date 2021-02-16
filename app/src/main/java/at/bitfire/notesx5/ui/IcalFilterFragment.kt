package at.bitfire.notesx5.ui



import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.databinding.FragmentIcalFilterBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.fragment_ical_filter.*
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


    private var categoriesPreselected: MutableList<String> = mutableListOf()
    private var statusTodoPreselected: MutableList<String> = mutableListOf()
    private var statusJournalPreselected: MutableList<String> = mutableListOf()
    private var classificationPreselected: MutableList<String> = mutableListOf()
    private var collectionPreselected: MutableList<String> = mutableListOf()
    private var componentPreselected: String = "JOURNAL"     // default should be overwritten


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

        componentPreselected = arguments.component2preselect

        if (arguments.category2preselect?.isNotEmpty() == true)
            categoriesPreselected = arguments.category2preselect!!.toMutableList()

        if (arguments.collection2preselect?.isNotEmpty() == true)
            collectionPreselected = arguments.collection2preselect!!.toMutableList()

        if (arguments.classification2preselect?.isNotEmpty() == true) {
            arguments.classification2preselect!!.forEach {
                if (Classification.values().contains(it))
                    classificationPreselected.add(getString(it.stringResource))
            }
        }

        if (arguments.statusJournal2preselect?.isNotEmpty() == true) {
            arguments.statusJournal2preselect!!.forEach {
                if (StatusJournal.values().contains(it))
                    statusJournalPreselected.add(getString(it.stringResource))
            }
        }

        if (arguments.statusTodo2preselect?.isNotEmpty() == true) {
            arguments.statusTodo2preselect!!.forEach {
                if (StatusTodo.values().contains(it))
                    statusTodoPreselected.add(getString(it.stringResource))
            }
        }

//        val priorityItems = resources.getStringArray(R.array.priority)

        // Retrieve the String values for the ENUMs Classification, StatusTodo and StatusJournal
        val classificationItems: MutableList<String> = mutableListOf()
        Classification.values().forEach { classificationItems.add(getString(it.stringResource))       }
        val statusTodoItems: MutableList<String> = mutableListOf()
        StatusTodo.values().forEach { statusTodoItems.add(getString(it.stringResource))       }
        val statusJournalItems: MutableList<String> = mutableListOf()
        StatusJournal.values().forEach { statusJournalItems.add(getString(it.stringResource))       }

        if (arguments.component2preselect == Component.TODO.name)
            addChips(binding.statusTodoFilterChipgroup, statusTodoItems, displayedStatusChips, statusTodoPreselected)
        if (arguments.component2preselect == Component.JOURNAL.name || arguments.component2preselect == Component.NOTE.name)
            addChips(binding.statusJournalFilterChipgroup, statusJournalItems, displayedStatusChips, statusJournalPreselected)

        addChips(binding.classificationFilterChipgroup, classificationItems, displayedClassificationChips, classificationPreselected)


        // observe and set chips for categories
        icalFilterViewModel.allCategories.observe(viewLifecycleOwner, {

            // Add the chips for categories
            if (icalFilterViewModel.allCategories.value != null)
                addChips(binding.categoryFilterChipgroup, icalFilterViewModel.allCategories.value!!, displayedCategoryChips, categoriesPreselected)

        })

        //observe and set list for organizers
        icalFilterViewModel.allCollections.observe(viewLifecycleOwner, {

            val collectionDisplayNames = mutableListOf<String>()
            icalFilterViewModel.allCollections.value?.forEach {
                collectionDisplayNames.add(it.displayName?: it.url)
            }

            // Add the chips for collections
            if (icalFilterViewModel.allCollections.value != null)
                addChips(binding.collectionFilterChipgroup, collectionDisplayNames, displayedCollectionChips, collectionPreselected)

        })



        return binding.root
    }


    /**
     * Method to add Chips to a [chipGroup] based on the String value for Classification and Status.
     * [displayed] is a MutableList that saves the already created Chips in order to not display the same category twice (can be an issue especially with loading of data from the DB.
     * [preselected] items can be passed through arguments
     */
    private fun addChips(chipGroup: ChipGroup, list: List<String>, displayed: MutableList<String>, preselected: MutableList<String>)  {

        list.forEach() { listItem ->

            if (listItem == "" || displayed.contains(listItem))   // don't show empty items and only show items that are not there yet
                return@forEach

            val chip = inflater.inflate(R.layout.fragment_ical_filter_chip, chipGroup, false) as Chip
            chip.text = listItem
            chipGroup.addView(chip)
            if(preselected.contains(listItem)) {   // if the current item is in the list of preselected items, then check it
                chip.isChecked = true
            }
            displayed.add(listItem)

        }
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_ical_filter, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_filter_reset -> {
                binding.collectionFilterChipgroup.clearCheck()
                binding.statusTodoFilterChipgroup.clearCheck()
                binding.statusJournalFilterChipgroup.clearCheck()
                binding.classificationFilterChipgroup.clearCheck()
                binding.categoryFilterChipgroup.clearCheck()
            }
            R.id.menu_filter_apply -> {

                val categoriesSelected: MutableList<String> = mutableListOf()
                val statusTodoSelected: MutableList<StatusTodo> = mutableListOf()
                val statusJournalSelected: MutableList<StatusJournal> = mutableListOf()
                val classificationSelected: MutableList<Classification> = mutableListOf()
                val collectionSelected: MutableList<String> = mutableListOf()


                binding.classificationFilterChipgroup.checkedChipIds.forEach { checkedChipId ->
                    val chip: Chip = binding.classificationFilterChipgroup.findViewById(checkedChipId)
                    val index = binding.classificationFilterChipgroup.indexOfChild(chip)
                    Classification.values().find { it.id == index }?.let { classificationSelected.add(it) }
                }

                binding.statusTodoFilterChipgroup.checkedChipIds.forEach { checkedChipId ->
                    val chip: Chip = binding.statusTodoFilterChipgroup.findViewById(checkedChipId)
                    val index = binding.statusTodoFilterChipgroup.indexOfChild(chip)
                    StatusTodo.values().find { it.id == index }?.let { statusTodoSelected.add(it) }
                }

                binding.statusJournalFilterChipgroup.checkedChipIds.forEach { checkedChipId ->
                    val chip: Chip = binding.statusJournalFilterChipgroup.findViewById(checkedChipId)
                    val index = binding.statusJournalFilterChipgroup.indexOfChild(chip)
                    StatusJournal.values().find { it.id == index }?.let { statusJournalSelected.add(it) }
                }

                binding.categoryFilterChipgroup.checkedChipIds.forEach { checkedChipId ->
                    val chip: Chip = binding.categoryFilterChipgroup.findViewById(checkedChipId)
                    categoriesSelected.add(chip.text.toString())
                }

                binding.collectionFilterChipgroup.checkedChipIds.forEach { checkedChipId ->
                    val chip: Chip = binding.collectionFilterChipgroup.findViewById(checkedChipId)
                    collectionSelected.add(chip.text.toString())
                }


                val direction = IcalFilterFragmentDirections.actionIcalFilterFragmentToIcalListFragment().apply {
                    this.statusJournal2filter = statusJournalSelected.toTypedArray()
                    this.statusTodo2filter = statusTodoSelected.toTypedArray()
                    this.classification2filter = classificationSelected.toTypedArray()
                    this.collection2filter = collectionSelected.toTypedArray()
                    this.category2filter = categoriesSelected.toTypedArray()
                    this.component2show = componentPreselected
                }


                this.findNavController().navigate(direction)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
