/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.StatusJournal
import at.techbee.jtx.database.StatusTodo
import at.techbee.jtx.databinding.FragmentIcalListRecyclerBinding


class IcalListFragmentJournals : Fragment() {

    private lateinit var binding: FragmentIcalListRecyclerBinding
    private val icalListViewModel: IcalListViewModel by activityViewModels()

    companion object {
        const val PREFS_LIST_JOURNALS = "prefsListJournals"

        const val PREFS_COLLECTION = "prefsCollection"
        const val PREFS_ACCOUNT = "prefsAccount"
        const val PREFS_CATEGORIES = "prefsCategories"
        const val PREFS_CLASSIFICATION = "prefsClassification"
        const val PREFS_STATUS_JOURNAL = "prefsStatusJournal"
        const val PREFS_STATUS_TODO = "prefsStatusTodo"
        const val PREFS_EXCLUDE_DONE = "prefsExcludeDone"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentIcalListRecyclerBinding.inflate(inflater, container, false)
        binding.listRecycler.layoutManager = LinearLayoutManager(context)
        binding.listRecycler.setHasFixedSize(false)
        binding.listRecycler.adapter = IcalListAdapterJournal(requireContext(), icalListViewModel)
        binding.listRecycler.scheduleLayoutAnimation()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        icalListViewModel.searchModule = Module.JOURNAL.name

        val prefs = requireActivity().getSharedPreferences(PREFS_LIST_JOURNALS, Context.MODE_PRIVATE)

        icalListViewModel.searchCategories = prefs.getStringSet(PREFS_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
        icalListViewModel.searchCollection = prefs.getStringSet(PREFS_COLLECTION, null)?.toMutableList() ?: mutableListOf()
        icalListViewModel.searchAccount = prefs.getStringSet(PREFS_ACCOUNT, null)?.toMutableList() ?: mutableListOf()
        icalListViewModel.searchStatusJournal = StatusJournal.getListFromStringList(prefs.getStringSet(
            PREFS_STATUS_JOURNAL, null))
        icalListViewModel.searchStatusTodo = StatusTodo.getListFromStringList(prefs.getStringSet(
            PREFS_STATUS_TODO, null))
        icalListViewModel.searchClassification = Classification.getListFromStringList(prefs.getStringSet(
            PREFS_CLASSIFICATION, null))
        icalListViewModel.isExcludeDone = prefs.getBoolean(PREFS_EXCLUDE_DONE, false)
        icalListViewModel.isFilterOverdue = false
        icalListViewModel.isFilterDueToday = false
        icalListViewModel.isFilterDueTomorrow = false
        icalListViewModel.isFilterDueFuture = false

        icalListViewModel.updateSearch()

        icalListViewModel.iCal4ListJournals.observe(viewLifecycleOwner) {
            binding.listRecycler.adapter?.notifyDataSetChanged()
            icalListViewModel.scrollOnceId.postValue(icalListViewModel.scrollOnceId.value)    // we post the value again as the observer might have missed the change
        }

        icalListViewModel.scrollOnceId.observe(viewLifecycleOwner) {
            if (it == null)
                return@observe

            val scrollToItem = icalListViewModel.iCal4ListJournals.value?.find { listItem -> listItem.property.id == it }
            val scrollToItemPos = icalListViewModel.iCal4ListJournals.value?.indexOf(scrollToItem)
            scrollToItemPos?.let { pos ->
                binding.listRecycler.layoutManager?.scrollToPosition(pos)
                icalListViewModel.scrollOnceId.postValue(null)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        val prefs = requireActivity().getSharedPreferences(PREFS_LIST_JOURNALS, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(PREFS_COLLECTION, icalListViewModel.searchCollection.toSet()).apply()
        prefs.edit().putStringSet(PREFS_ACCOUNT, icalListViewModel.searchAccount.toSet()).apply()
        prefs.edit().putStringSet(PREFS_STATUS_JOURNAL, StatusJournal.getStringSetFromList(icalListViewModel.searchStatusJournal)).apply()
        prefs.edit().putStringSet(PREFS_STATUS_TODO, StatusTodo.getStringSetFromList(icalListViewModel.searchStatusTodo)).apply()
        prefs.edit().putStringSet(PREFS_CLASSIFICATION, Classification.getStringSetFromList(icalListViewModel.searchClassification)).apply()
        prefs.edit().putStringSet(PREFS_CATEGORIES, icalListViewModel.searchCategories.toSet()).apply()
        prefs.edit().putBoolean(PREFS_EXCLUDE_DONE, icalListViewModel.isExcludeDone).apply()
    }
}
