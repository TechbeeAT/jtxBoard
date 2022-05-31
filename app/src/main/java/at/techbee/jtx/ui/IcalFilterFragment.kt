/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.forEach
import androidx.fragment.app.activityViewModels
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.databinding.FragmentIcalFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class IcalFilterFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentIcalFilterBinding? = null
    private val binding get() = _binding!!

    private val icalListViewModel: IcalListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentIcalFilterBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        Classification.values().forEach { classification ->
            val chip = inflater.inflate(R.layout.fragment_ical_filter_chip, binding.classificationFilterChipgroup, false) as Chip
            chip.text = getString(classification.stringResource)
            binding.classificationFilterChipgroup.addView(chip)

            if(icalListViewModel.searchClassification.contains(classification))
                chip.isChecked = true

            chip.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked)
                    icalListViewModel.searchClassification.add(classification)
                else
                    icalListViewModel.searchClassification.remove(classification)
                icalListViewModel.updateSearch()
            }
        }

        StatusJournal.values().forEach { statusJournal ->
            val chip = inflater.inflate(R.layout.fragment_ical_filter_chip, binding.statusJournalFilterChipgroup, false) as Chip
            chip.text = getString(statusJournal.stringResource)
            binding.statusJournalFilterChipgroup.addView(chip)

            if(icalListViewModel.searchStatusJournal.contains(statusJournal))
                chip.isChecked = true

            chip.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked)
                    icalListViewModel.searchStatusJournal.add(statusJournal)
                else
                    icalListViewModel.searchStatusJournal.remove(statusJournal)
                icalListViewModel.updateSearch()
            }
        }

        StatusTodo.values().forEach { statusTodo ->
            val chip = inflater.inflate(R.layout.fragment_ical_filter_chip, binding.statusTodoFilterChipgroup, false) as Chip
            chip.text = getString(statusTodo.stringResource)
            binding.statusTodoFilterChipgroup.addView(chip)

            if(icalListViewModel.searchStatusTodo.contains(statusTodo))
                chip.isChecked = true

            chip.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked)
                    icalListViewModel.searchStatusTodo.add(statusTodo)
                else
                    icalListViewModel.searchStatusTodo.remove(statusTodo)
                icalListViewModel.updateSearch()
            }
        }

        OrderBy.values().forEach {

            if(!it.compatibleModules.contains(Module.valueOf(icalListViewModel.searchModule)))
                return@forEach
            val chip = inflater.inflate(R.layout.fragment_ical_filter_chip, binding.filterOrderbyChipgroup, false) as Chip
            chip.text = getString(it.stringResource)
            binding.filterOrderbyChipgroup.addView(chip)

            if(icalListViewModel.orderBy == it)
                chip.isChecked = true

            chip.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    icalListViewModel.orderBy = it
                    icalListViewModel.updateSearch()
                }
            }
        }

        SortOrder.values().forEach {

            val chip = inflater.inflate(R.layout.fragment_ical_filter_chip, binding.filterSortorderChipgroup, false) as Chip
            chip.text = getString(it.stringResource)
            binding.filterSortorderChipgroup.addView(chip)

            if(icalListViewModel.sortOrder == it)
                chip.isChecked = true

            chip.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    icalListViewModel.sortOrder = it
                    icalListViewModel.updateSearch()
                }
            }
        }

        // observe and set chips for categories
        icalListViewModel.allCategories.observe(viewLifecycleOwner) { categories ->

            categories.forEach { category ->
                val chip = inflater.inflate(R.layout.fragment_ical_filter_chip, binding.categoryFilterChipgroup, false) as Chip
                chip.text = category
                if(!isChiptextPresentInChipgroup(binding.categoryFilterChipgroup, category)) {
                    binding.categoryFilterChipgroup.addView(chip)

                    if(icalListViewModel.searchCategories.contains(category))
                        chip.isChecked = true

                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if(isChecked)
                            icalListViewModel.searchCategories.add(category)
                        else
                            icalListViewModel.searchCategories.remove(category)
                        icalListViewModel.updateSearch()
                    }
                }
            }

        }

        //observe and set list for organizers
        icalListViewModel.allCollections.observe(viewLifecycleOwner) { collections ->

            collections.forEach { collection ->
                val chip = inflater.inflate(R.layout.fragment_ical_filter_chip, binding.collectionFilterChipgroup, false) as Chip
                val collectionName = collection.displayName ?: collection.url
                chip.text = collectionName
                if(!isChiptextPresentInChipgroup(binding.collectionFilterChipgroup, collectionName)) {
                    binding.collectionFilterChipgroup.addView(chip)

                    if(icalListViewModel.searchCollection.contains(collectionName))
                        chip.isChecked = true

                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if(isChecked)
                            icalListViewModel.searchCollection.add(collectionName)
                        else
                            icalListViewModel.searchCollection.remove(collectionName)
                        icalListViewModel.updateSearch()
                    }
                }

                // we already cover the accounts here
                val chipAccount = inflater.inflate(R.layout.fragment_ical_filter_chip, binding.accountFilterChipgroup, false) as Chip
                val accountName = collection.accountName ?: return@forEach
                chipAccount.text = accountName
                if(!isChiptextPresentInChipgroup(binding.accountFilterChipgroup, accountName)) {
                    binding.accountFilterChipgroup.addView(chipAccount)

                    if(icalListViewModel.searchAccount.contains(accountName))
                        chipAccount.isChecked = true

                    chipAccount.setOnCheckedChangeListener { _, isChecked ->
                        if(isChecked)
                            icalListViewModel.searchAccount.add(accountName)
                        else
                            icalListViewModel.searchAccount.remove(accountName)
                        icalListViewModel.updateSearch()
                    }
                }
            }
        }

        if(icalListViewModel.searchModule == Module.JOURNAL.name || icalListViewModel.searchModule == Module.NOTE.name)
            binding.statusTodoFilterChipgroup.visibility = View.GONE
        if(icalListViewModel.searchModule == Module.TODO.name)
            binding.statusJournalFilterChipgroup.visibility = View.GONE


        binding.filterFabResetfilter.setOnClickListener {
            resetFilter()
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun resetFilter() {

        binding.collectionFilterChipgroup.clearCheck()
        binding.accountFilterChipgroup.clearCheck()
        binding.statusTodoFilterChipgroup.clearCheck()
        binding.statusJournalFilterChipgroup.clearCheck()
        binding.classificationFilterChipgroup.clearCheck()
        binding.categoryFilterChipgroup.clearCheck()

        icalListViewModel.clearFilter()
    }

    private fun isChiptextPresentInChipgroup(group: ChipGroup, chiptext: String): Boolean {
        group.forEach {
            val chip = it as Chip
            if(chip.text == chiptext)
                return true
        }
        return false
    }
}


