/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.StatusJournal
import at.techbee.jtx.database.StatusTodo
import at.techbee.jtx.ui.compose.screens.ListScreenCompact
import at.techbee.jtx.ui.compose.screens.ListScreenList
import at.techbee.jtx.ui.compose.screens.ListScreenGrid
import at.techbee.jtx.ui.compose.screens.ListScreenKanban
import at.techbee.jtx.ui.theme.JtxBoardTheme


open class IcalListFragmentModule(val module: Module) : Fragment() {

    companion object {
        const val PREFS_LIST_JOURNALS = "prefsListJournals"
        const val PREFS_LIST_NOTES = "prefsListNotes"
        const val PREFS_LIST_TODOS = "prefsListTodos"

        private const val PREFS_COLLECTION = "prefsCollection"
        private const val PREFS_ACCOUNT = "prefsAccount"
        private const val PREFS_CATEGORIES = "prefsCategories"
        private const val PREFS_CLASSIFICATION = "prefsClassification"
        private const val PREFS_STATUS_JOURNAL = "prefsStatusJournal"
        private const val PREFS_STATUS_TODO = "prefsStatusTodo"
        private const val PREFS_EXCLUDE_DONE = "prefsExcludeDone"
        private const val PREFS_ORDERBY = "prefsOrderBy"
        private const val PREFS_SORTORDER = "prefsSortOrder"
        private const val PREFS_FILTER_OVERDUE = "prefsFilterOverdue"
        private const val PREFS_FILTER_DUE_TODAY = "prefsFilterToday"
        private const val PREFS_FILTER_DUE_TOMORROW = "prefsFilterTomorrow"
        private const val PREFS_FILTER_DUE_FUTURE = "prefsFilterFuture"
        private const val PREFS_FILTER_NO_DATES_SET = "prefsFilterNoDatesSet"

        private const val PREFS_VIEWMODE = "prefsViewmode"
    }

    private val icalListViewModel: IcalListViewModel by activityViewModels()
    private lateinit var prefs: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        prefs = when (module) {
            Module.JOURNAL -> requireActivity().getSharedPreferences(
                PREFS_LIST_JOURNALS,
                Context.MODE_PRIVATE
            )
            Module.NOTE -> requireActivity().getSharedPreferences(
                PREFS_LIST_NOTES,
                Context.MODE_PRIVATE
            )
            Module.TODO -> requireActivity().getSharedPreferences(
                PREFS_LIST_TODOS,
                Context.MODE_PRIVATE
            )
        }

        val navController = this.findNavController()

        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                JtxBoardTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {

                        val viewMode by icalListViewModel.viewMode.observeAsState()

                        when (viewMode) {
                            IcalListFragment.PREFS_VIEWMODE_LIST -> {
                                ListScreenList(
                                    listLive = when (module) {
                                        Module.JOURNAL -> icalListViewModel.iCal4ListJournals
                                        Module.NOTE -> icalListViewModel.iCal4ListNotes
                                        Module.TODO -> icalListViewModel.iCal4ListTodos
                                    },
                                    subtasksLive = icalListViewModel.allSubtasksMap,
                                    subnotesLive = icalListViewModel.allSubnotesMap,
                                    attachmentsLive = icalListViewModel.allAttachmentsMap,
                                    scrollOnceId = icalListViewModel.scrollOnceId,
                                    isExcludeDone = icalListViewModel.isExcludeDone,
                                    goToView = { itemId ->
                                        navController.navigate(
                                            IcalListFragmentDirections
                                                .actionIcalListFragmentToIcalViewFragment()
                                                .setItem2show(itemId)
                                        )
                                    },
                                    goToEdit = { itemId ->
                                        icalListViewModel.postDirectEditEntity(
                                            itemId
                                        )
                                    },
                                    onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance -> icalListViewModel.updateProgress(itemId, newPercent, isLinkedRecurringInstance)  },
                                    onExpandedChanged = { itemId: Long, isSubtasksExpanded: Boolean, isSubnotesExpanded: Boolean, isAttachmentsExpanded: Boolean -> icalListViewModel.updateExpanded(itemId, isSubtasksExpanded, isSubnotesExpanded, isAttachmentsExpanded)},
                                )
                            }
                            IcalListFragment.PREFS_VIEWMODE_GRID -> {
                                ListScreenGrid(
                                    listLive = when (module) {
                                        Module.JOURNAL -> icalListViewModel.iCal4ListJournals
                                        Module.NOTE -> icalListViewModel.iCal4ListNotes
                                        Module.TODO -> icalListViewModel.iCal4ListTodos
                                    },
                                    scrollOnceId = icalListViewModel.scrollOnceId,
                                    onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance ->
                                        icalListViewModel.updateProgress(
                                            itemId,
                                            newPercent,
                                            isLinkedRecurringInstance
                                        )
                                    },
                                    goToView = { itemId ->
                                        navController.navigate(
                                            IcalListFragmentDirections
                                                .actionIcalListFragmentToIcalViewFragment()
                                                .setItem2show(itemId)
                                        )
                                    },
                                    goToEdit = { itemId ->
                                        icalListViewModel.postDirectEditEntity(
                                            itemId
                                        )
                                    }
                                )
                            }
                            IcalListFragment.PREFS_VIEWMODE_COMPACT -> {
                                ListScreenCompact(
                                    listLive = when (module) {
                                        Module.JOURNAL -> icalListViewModel.iCal4ListJournals
                                        Module.NOTE -> icalListViewModel.iCal4ListNotes
                                        Module.TODO -> icalListViewModel.iCal4ListTodos
                                    },
                                    subtasksLive = icalListViewModel.allSubtasksMap,
                                    scrollOnceId = icalListViewModel.scrollOnceId,
                                    isExcludeDone = icalListViewModel.isExcludeDone,
                                    onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance ->
                                        icalListViewModel.updateProgress(
                                            itemId,
                                            newPercent,
                                            isLinkedRecurringInstance
                                        )
                                    },
                                    goToView = { itemId ->
                                        navController.navigate(
                                            IcalListFragmentDirections
                                                .actionIcalListFragmentToIcalViewFragment()
                                                .setItem2show(itemId)
                                        )
                                    },
                                    goToEdit = { itemId ->
                                        icalListViewModel.postDirectEditEntity(
                                            itemId
                                        )
                                    }
                                )
                            }
                            IcalListFragment.PREFS_VIEWMODE_KANBAN -> {
                                ListScreenKanban(
                                    module = module,
                                    listLive = when (module) {
                                        Module.JOURNAL -> icalListViewModel.iCal4ListJournals
                                        Module.NOTE -> icalListViewModel.iCal4ListNotes
                                        Module.TODO -> icalListViewModel.iCal4ListTodos
                                    },
                                    scrollOnceId = icalListViewModel.scrollOnceId,
                                    onProgressChanged = { itemId, newPercent, isLinkedRecurringInstance, scrollOnce ->
                                        icalListViewModel.updateProgress(
                                            itemId,
                                            newPercent,
                                            isLinkedRecurringInstance,
                                            scrollOnce
                                        )
                                    },
                                    onStatusChanged = { itemId, newStatus, isLinkedRecurringInstance, scrollOnce ->
                                        icalListViewModel.updateStatusJournal(itemId, newStatus, isLinkedRecurringInstance, scrollOnce)
                                    },
                                    goToView = { itemId ->
                                        navController.navigate(
                                            IcalListFragmentDirections
                                                .actionIcalListFragmentToIcalViewFragment()
                                                .setItem2show(itemId)
                                        )
                                    },
                                    goToEdit = { itemId ->
                                        icalListViewModel.postDirectEditEntity(
                                            itemId
                                        )
                                    }
                                )
                            }
                        }

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        icalListViewModel.searchModule = module.name
        loadPrefs()
        icalListViewModel.updateSearch()

        try {
            val activity = requireActivity() as MainActivity
            val toolbarText = getString(R.string.toolbar_text_jtx_board)
            val toolbarSubtitle = when (module) {
                Module.JOURNAL -> getString(R.string.toolbar_text_jtx_board_journals_overview)
                Module.NOTE -> getString(R.string.toolbar_text_jtx_board_notes_overview)
                Module.TODO -> getString(R.string.toolbar_text_jtx_board_tasks_overview)
            }
            activity.setToolbarTitle(toolbarText, toolbarSubtitle)
        } catch (e: ClassCastException) {
            Log.d(
                "setToolbarText",
                "Class cast to MainActivity failed (this is common for tests but doesn't really matter)\n$e"
            )
        }
    }

    override fun onPause() {
        super.onPause()
        savePrefs()
    }


    private fun savePrefs() {

        prefs.edit().putStringSet(PREFS_COLLECTION, icalListViewModel.searchCollection.toSet())
            .apply()
        prefs.edit().putStringSet(PREFS_ACCOUNT, icalListViewModel.searchAccount.toSet()).apply()
        prefs.edit().putStringSet(
            PREFS_STATUS_JOURNAL,
            StatusJournal.getStringSetFromList(icalListViewModel.searchStatusJournal)
        ).apply()
        prefs.edit().putStringSet(
            PREFS_STATUS_TODO,
            StatusTodo.getStringSetFromList(icalListViewModel.searchStatusTodo)
        ).apply()
        prefs.edit().putStringSet(
            PREFS_CLASSIFICATION,
            Classification.getStringSetFromList(icalListViewModel.searchClassification)
        ).apply()
        prefs.edit().putStringSet(PREFS_CATEGORIES, icalListViewModel.searchCategories.toSet())
            .apply()
        prefs.edit().putBoolean(PREFS_EXCLUDE_DONE, icalListViewModel.isExcludeDone.value ?: false)
            .apply()

        prefs.edit().putBoolean(PREFS_FILTER_OVERDUE, icalListViewModel.isFilterOverdue).apply()
        prefs.edit().putBoolean(PREFS_FILTER_DUE_TODAY, icalListViewModel.isFilterDueToday).apply()
        prefs.edit().putBoolean(PREFS_FILTER_DUE_TOMORROW, icalListViewModel.isFilterDueTomorrow)
            .apply()
        prefs.edit().putBoolean(PREFS_FILTER_DUE_FUTURE, icalListViewModel.isFilterDueFuture)
            .apply()
        prefs.edit().putBoolean(PREFS_FILTER_NO_DATES_SET, icalListViewModel.isFilterNoDatesSet)
            .apply()

        prefs.edit().putString(PREFS_SORTORDER, icalListViewModel.sortOrder.name).apply()
        prefs.edit().putString(PREFS_ORDERBY, icalListViewModel.orderBy.name).apply()

        prefs.edit().putString(PREFS_VIEWMODE, icalListViewModel.viewMode.value).apply()
    }

    private fun loadPrefs() {
        icalListViewModel.searchCategories =
            prefs.getStringSet(PREFS_CATEGORIES, null)?.toMutableList() ?: mutableListOf()
        icalListViewModel.searchCollection =
            prefs.getStringSet(PREFS_COLLECTION, null)?.toMutableList() ?: mutableListOf()
        icalListViewModel.searchAccount =
            prefs.getStringSet(PREFS_ACCOUNT, null)?.toMutableList() ?: mutableListOf()
        icalListViewModel.searchStatusJournal = StatusJournal.getListFromStringList(
            prefs.getStringSet(
                PREFS_STATUS_JOURNAL, null
            )
        )
        icalListViewModel.searchStatusTodo = StatusTodo.getListFromStringList(
            prefs.getStringSet(
                PREFS_STATUS_TODO, null
            )
        )
        icalListViewModel.searchClassification = Classification.getListFromStringList(
            prefs.getStringSet(
                PREFS_CLASSIFICATION, null
            )
        )
        icalListViewModel.isExcludeDone.value = prefs.getBoolean(PREFS_EXCLUDE_DONE, false)
        icalListViewModel.isFilterOverdue = prefs.getBoolean(PREFS_FILTER_OVERDUE, false)
        icalListViewModel.isFilterDueToday = prefs.getBoolean(PREFS_FILTER_DUE_TODAY, false)
        icalListViewModel.isFilterDueTomorrow = prefs.getBoolean(PREFS_FILTER_DUE_TOMORROW, false)
        icalListViewModel.isFilterDueFuture = prefs.getBoolean(PREFS_FILTER_DUE_FUTURE, false)
        icalListViewModel.isFilterNoDatesSet = prefs.getBoolean(PREFS_FILTER_NO_DATES_SET, false)
        icalListViewModel.orderBy =
            prefs.getString(PREFS_ORDERBY, null)?.let { OrderBy.valueOf(it) } ?: OrderBy.DUE
        icalListViewModel.sortOrder =
            prefs.getString(PREFS_SORTORDER, null)?.let { SortOrder.valueOf(it) } ?: SortOrder.ASC

        icalListViewModel.viewMode.value =
            prefs.getString(PREFS_VIEWMODE, IcalListFragment.PREFS_VIEWMODE_LIST)
    }
}


class IcalListFragmentJournal : IcalListFragmentModule(module = Module.JOURNAL)
class IcalListFragmentNote : IcalListFragmentModule(module = Module.NOTE)
class IcalListFragmentTodo : IcalListFragmentModule(module = Module.TODO)

