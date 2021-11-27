/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui


import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.techbee.jtx.*
import at.techbee.jtx.database.*
import at.techbee.jtx.databinding.FragmentIcalFilterBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.lang.ClassCastException
import java.util.*


class IcalFilterFragment : Fragment() {

    lateinit var binding: FragmentIcalFilterBinding
    lateinit var application: Application
    private lateinit var dataSource: ICalDatabaseDao
    private lateinit var viewModelFactory: IcalFilterViewModelFactory
    private lateinit var icalFilterViewModel: IcalFilterViewModel
    private lateinit var inflater: LayoutInflater

    private var displayedCategoryChips: MutableList<String> = mutableListOf()
    private var displayedStatusChips: MutableList<String> = mutableListOf()
    private var displayedClassificationChips: MutableList<String> = mutableListOf()
    private var displayedCollectionChips: MutableList<String> = mutableListOf()
    private var displayedAccountChips: MutableList<String> = mutableListOf()


    private var categoriesPreselected: MutableList<String> = mutableListOf()
    private var statusTodoPreselected: MutableList<String> = mutableListOf()
    private var statusJournalPreselected: MutableList<String> = mutableListOf()
    private var classificationPreselected: MutableList<String> = mutableListOf()
    private var collectionPreselected: MutableList<String> = mutableListOf()
    private var accountPreselected: MutableList<String> = mutableListOf()
    private var modulePreselected: String = Module.JOURNAL.name     // default should be overwritten


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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
                this, viewModelFactory
            )[IcalFilterViewModel::class.java]

        binding.model = icalFilterViewModel
        binding.lifecycleOwner = viewLifecycleOwner


        // get previously selected items from arguments
        val arguments = IcalFilterFragmentArgs.fromBundle((requireArguments()))

        modulePreselected = arguments.module2preselect

        categoriesPreselected = arguments.category2preselect?.toMutableList() ?: mutableListOf()
        collectionPreselected = arguments.collection2preselect?.toMutableList() ?: mutableListOf()
        accountPreselected = arguments.account2preselect?.toMutableList() ?: mutableListOf()

        arguments.classification2preselect?.forEach {
            if (Classification.values().contains(it))
                classificationPreselected.add(getString(it.stringResource))
        }

        arguments.statusJournal2preselect?.forEach {
            if (StatusJournal.values().contains(it))
                statusJournalPreselected.add(getString(it.stringResource))
        }

        arguments.statusTodo2preselect?.forEach {
            if (StatusTodo.values().contains(it))
                statusTodoPreselected.add(getString(it.stringResource))
        }

        // Retrieve the String values for the ENUMs Classification, StatusTodo and StatusJournal
        val classificationItems: MutableList<String> = mutableListOf()
        Classification.values().forEach { classificationItems.add(getString(it.stringResource)) }
        val statusTodoItems: MutableList<String> = mutableListOf()
        StatusTodo.values().forEach { statusTodoItems.add(getString(it.stringResource)) }
        val statusJournalItems: MutableList<String> = mutableListOf()
        StatusJournal.values().forEach { statusJournalItems.add(getString(it.stringResource)) }

        if (arguments.module2preselect == Module.TODO.name)
            addChips(
                binding.statusTodoFilterChipgroup,
                statusTodoItems,
                displayedStatusChips,
                statusTodoPreselected
            )
        if (arguments.module2preselect == Module.JOURNAL.name || arguments.module2preselect == Module.NOTE.name)
            addChips(
                binding.statusJournalFilterChipgroup,
                statusJournalItems,
                displayedStatusChips,
                statusJournalPreselected
            )

        addChips(
            binding.classificationFilterChipgroup,
            classificationItems,
            displayedClassificationChips,
            classificationPreselected
        )

        binding.filterFabApplyfilter.setOnClickListener { applyFilter() }


        // observe and set chips for categories
        icalFilterViewModel.allCategories.observe(viewLifecycleOwner, { categories ->
            // Add the chips for categories
            if (icalFilterViewModel.allCategories.value != null)
                addChips(
                    binding.categoryFilterChipgroup,
                    categories,
                    displayedCategoryChips,
                    categoriesPreselected
                )

        })

        //observe and set list for organizers
        icalFilterViewModel.allCollections.observe(viewLifecycleOwner, { collections ->

            val collectionDisplayNames = mutableListOf<String>()
            collections.forEach { collection ->
                collectionDisplayNames.add(collection.displayName ?: collection.url)
            }
            addChips(
                binding.collectionFilterChipgroup,
                collectionDisplayNames,
                displayedCollectionChips,
                collectionPreselected
            )

            val accountNames = mutableListOf<String>()
            collections.forEach { collection ->
                collection.accountName?.let { accountNames.add(it) }
            }
            addChips(
                binding.accountFilterChipgroup,
                accountNames.distinct(),
                displayedAccountChips,
                accountPreselected
            )

        })

        return binding.root
    }

    override fun onResume() {

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarTitle(getString(R.string.toolbar_text_filter), null)
        } catch (e: ClassCastException) {
            Log.d("setToolbarText", "Class cast to MainActivity failed (this is common for tests but doesn't really matter)\n$e")
        }

        super.onResume()
    }



    /**
     * Method to add Chips to a [chipGroup] based on the String value for Classification and Status.
     * [displayed] is a MutableList that saves the already created Chips in order to not display the same category twice (can be an issue especially with loading of data from the DB.
     * [preselected] items can be passed through arguments
     */
    private fun addChips(
        chipGroup: ChipGroup,
        list: List<String>,
        displayed: MutableList<String>,
        preselected: MutableList<String>
    ) {

        list.forEach { listItem ->

            if (listItem == "" || displayed.contains(listItem))   // don't show empty items and only show items that are not there yet
                return@forEach

            val chip =
                inflater.inflate(R.layout.fragment_ical_filter_chip, chipGroup, false) as Chip
            chip.text = listItem
            chipGroup.addView(chip)
            if (preselected.contains(listItem)) {   // if the current item is in the list of preselected items, then check it
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
            R.id.menu_filter_reset -> resetFilter()
            R.id.menu_filter_apply -> applyFilter()
        }

        return super.onOptionsItemSelected(item)
    }


    private fun resetFilter() {

        binding.collectionFilterChipgroup.clearCheck()
        binding.accountFilterChipgroup.clearCheck()
        binding.statusTodoFilterChipgroup.clearCheck()
        binding.statusJournalFilterChipgroup.clearCheck()
        binding.classificationFilterChipgroup.clearCheck()
        binding.categoryFilterChipgroup.clearCheck()
    }


    private fun applyFilter() {

        val categoriesSelected: MutableList<String> = mutableListOf()
        val statusTodoSelected: MutableList<StatusTodo> = mutableListOf()
        val statusJournalSelected: MutableList<StatusJournal> = mutableListOf()
        val classificationSelected: MutableList<Classification> = mutableListOf()
        val collectionSelected: MutableList<String> = mutableListOf()
        val accountSelected: MutableList<String> = mutableListOf()


        binding.classificationFilterChipgroup.checkedChipIds.forEach { checkedChipId ->
            val chip: Chip = binding.classificationFilterChipgroup.findViewById(checkedChipId)
            val index = binding.classificationFilterChipgroup.indexOfChild(chip)
            Classification.values()[index].let { classificationSelected.add(it) }
        }

        binding.statusTodoFilterChipgroup.checkedChipIds.forEach { checkedChipId ->
            val chip: Chip = binding.statusTodoFilterChipgroup.findViewById(checkedChipId)
            val index = binding.statusTodoFilterChipgroup.indexOfChild(chip)
            StatusTodo.values()[index].let { statusTodoSelected.add(it) }
        }

        binding.statusJournalFilterChipgroup.checkedChipIds.forEach { checkedChipId ->
            val chip: Chip = binding.statusJournalFilterChipgroup.findViewById(checkedChipId)
            val index = binding.statusJournalFilterChipgroup.indexOfChild(chip)
            StatusJournal.values()[index].let { statusJournalSelected.add(it) }
        }

        binding.categoryFilterChipgroup.checkedChipIds.forEach { checkedChipId ->
            val chip: Chip = binding.categoryFilterChipgroup.findViewById(checkedChipId)
            categoriesSelected.add(chip.text.toString())
        }

        binding.collectionFilterChipgroup.checkedChipIds.forEach { checkedChipId ->
            val chip: Chip = binding.collectionFilterChipgroup.findViewById(checkedChipId)
            collectionSelected.add(chip.text.toString())
        }

        binding.accountFilterChipgroup.checkedChipIds.forEach { checkedChipId ->
            val chip: Chip = binding.accountFilterChipgroup.findViewById(checkedChipId)
            accountSelected.add(chip.text.toString())
        }


        val direction =
            IcalFilterFragmentDirections.actionIcalFilterFragmentToIcalListFragment().apply {
                this.statusJournal2filter = statusJournalSelected.toTypedArray()
                this.statusTodo2filter = statusTodoSelected.toTypedArray()
                this.classification2filter = classificationSelected.toTypedArray()
                this.collection2filter = collectionSelected.toTypedArray()
                this.account2filter = accountSelected.toTypedArray()
                this.category2filter = categoriesSelected.toTypedArray()
                this.module2show = modulePreselected
            }
        this.findNavController().navigate(direction)
    }
}


