/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.accounts.Account
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.sqlite.db.SimpleSQLiteQuery
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.relations.ICal4ListWithRelatedto
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.database.views.VIEW_NAME_ICAL4LIST
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class IcalListViewModel(
        val database: ICalDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    var searchModule: String = Module.JOURNAL.name
    var searchText: String = ""
    var searchCategories: MutableList<String> = mutableListOf()
    var searchOrganizer: MutableList<String> = mutableListOf()
    var searchStatusJournal: MutableList<StatusJournal> = mutableListOf()
    var searchStatusTodo: MutableList<StatusTodo> = mutableListOf()
    var searchClassification: MutableList<Classification> = mutableListOf()
    var searchCollection: MutableList<String> = mutableListOf()

    var searchSettingShowSubtasksOfVJOURNALs: Boolean = false


    //var listQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>().apply { postValue(constructQuery()) }
    var listQuery: MutableLiveData<SimpleSQLiteQuery> = MutableLiveData<SimpleSQLiteQuery>()
    var iCal4List: LiveData<List<ICal4ListWithRelatedto>> = Transformations.switchMap(listQuery) {
        database.getIcalObjectWithRelatedto(it)
    }

    // TODO maybe retrieve all subtasks only when subtasks are needed!
    val allSubtasks: LiveData<List<ICal4List?>> = database.getAllSubtasks()

    var focusItemId: MutableLiveData<Long?> = MutableLiveData(null)

    val allRemoteCollections = database.getAllRemoteCollections()


    fun getFocusItemPosition(): Int {
        val focusItem = iCal4List.value?.find { focusItemId.value == it.property.id }
        return iCal4List.value?.indexOf(focusItem) ?: -1
    }

    fun resetFocusItem() {

        if(searchModule == Module.NOTE.name) {               // Notes have no default focus item (list scrolls to top by default)
            focusItemId.value = 0L
            return
        } else {
            val today = Calendar.getInstance()
            iCal4List.value?.forEach {
                val itemDate = Calendar.getInstance().apply {
                    timeInMillis = it.property.dtstart?:0L
                }
                if(itemDate.get(Calendar.YEAR) >= today.get(Calendar.YEAR) && itemDate.get(Calendar.DAY_OF_YEAR) >= today.get(Calendar.DAY_OF_YEAR)) {
                    focusItemId.value = it.property.id
                    return                                    // return when the first item is found that is in the future (focus item stays set on this one)
                }
            }
        }
        focusItemId.value = 0L                  // if the list is empty, the focus item would be set to 0L

    }

    private fun constructQuery(): SimpleSQLiteQuery {

        val args = arrayListOf<String>()

// Beginning of query string
        var queryString = "SELECT DISTINCT $VIEW_NAME_ICAL4LIST.* FROM $VIEW_NAME_ICAL4LIST "
        if(searchCategories.size > 0)
            queryString += "LEFT JOIN $TABLE_NAME_CATEGORY ON $VIEW_NAME_ICAL4LIST.$COLUMN_ID = $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_ICALOBJECT_ID "
        if(searchOrganizer.size > 0)
            queryString += "LEFT JOIN $TABLE_NAME_ORGANIZER ON $VIEW_NAME_ICAL4LIST.$COLUMN_ID = $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_ICALOBJECT_ID "
        if(searchCollection.size > 0)
            queryString += "LEFT JOIN $TABLE_NAME_COLLECTION ON $VIEW_NAME_ICAL4LIST.$COLUMN_ICALOBJECT_COLLECTIONID = $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID "  // +
        //     "LEFT JOIN vattendees ON icalobject._id = vattendees.icalObjectId " +
        //     "LEFT JOIN vorganizer ON icalobject._id = vorganizer.icalObjectId " +
        //     "LEFT JOIN vRelatedto ON icalobject._id = vRelatedto.icalObjectId "

        // First query parameter Component must always be present!
        queryString += "WHERE $COLUMN_MODULE = ? "
        args.add(searchModule)

        // Query for the given text search from the action bar
        if (searchText.isNotEmpty() && searchText.length >= 2) {
            queryString += "AND ($VIEW_NAME_ICAL4LIST.$COLUMN_SUMMARY LIKE ? OR $VIEW_NAME_ICAL4LIST.$COLUMN_DESCRIPTION LIKE ?) "
            args.add(searchText)
            args.add(searchText)
        }

        // Query for the passed filter criteria from VJournalFilterFragment
        if (searchCategories.size > 0) {
            queryString += "AND $TABLE_NAME_CATEGORY.$COLUMN_CATEGORY_TEXT IN ("
            searchCategories.forEach {
                queryString += "?,"
                args.add(it)
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from VJournalFilterFragment
        if (searchOrganizer.size > 0) {
            queryString += "AND $TABLE_NAME_ORGANIZER.$COLUMN_ORGANIZER_CALADDRESS IN ("
            searchOrganizer.forEach {
                queryString += "?,"
                args.add(it)
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from FilterFragment
        if (searchStatusJournal.size > 0 && (searchModule == Module.JOURNAL.name || searchModule == Module.NOTE.name)) {
            queryString += "AND $COLUMN_STATUS IN ("
            searchStatusJournal.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from FilterFragment
        if (searchStatusTodo.size > 0 && searchModule == Module.TODO.name) {
            queryString += "AND $COLUMN_STATUS IN ("
            searchStatusTodo.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Query for the passed filter criteria from FilterFragment
        if (searchClassification.size > 0) {
            queryString += "AND $COLUMN_CLASSIFICATION IN ("
            searchClassification.forEach {
                queryString += "?,"
                args.add(it.toString())
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }


        // Query for the passed filter criteria from FilterFragment
        if (searchCollection.size > 0) {
            queryString += "AND $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_DISPLAYNAME IN ("
            searchCollection.forEach {
                queryString += "?,"
                args.add(it)
            }
            queryString = queryString.removeSuffix(",")      // remove the last comma
            queryString += ") "
        }

        // Exclude items that are Child items by checking if they appear in the linkedICalObjectId of relatedto!
        //queryString += "AND $VIEW_NAME_ICAL4LIST.$COLUMN_ID NOT IN (SELECT $COLUMN_RELATEDTO_LINKEDICALOBJECT_ID FROM $TABLE_NAME_RELATEDTO) "
        when {
            (searchModule == Module.JOURNAL.name || searchModule == Module.NOTE.name) ->
                queryString += "AND $VIEW_NAME_ICAL4LIST.isChildOfVJOURNAL = 0 AND $VIEW_NAME_ICAL4LIST.isChildOfVTODO = 0 "
            (searchModule == Module.TODO.name && !searchSettingShowSubtasksOfVJOURNALs) ->
                queryString += "AND $VIEW_NAME_ICAL4LIST.isChildOfVJOURNAL = 0 AND $VIEW_NAME_ICAL4LIST.isChildOfVTODO = 0 "
            // to make it clearer, if all Subtasks should be shown, then we don't need an additional condition
            (searchModule == Module.TODO.name && searchSettingShowSubtasksOfVJOURNALs) ->
                queryString += "AND $VIEW_NAME_ICAL4LIST.isChildOfVTODO = 0 "
        }

        when (searchModule) {
            Module.JOURNAL.name -> queryString += "ORDER BY $COLUMN_DTSTART ASC, $COLUMN_CREATED ASC "
            Module.NOTE.name -> queryString += "ORDER BY $COLUMN_LAST_MODIFIED DESC, $COLUMN_CREATED ASC "
            Module.TODO.name -> queryString += "ORDER BY $COLUMN_DUE ASC, $COLUMN_CREATED ASC "
        }



        Log.println(Log.INFO, "queryString", queryString)
        Log.println(Log.INFO, "queryStringArgs", args.joinToString(separator = ", "))


        return SimpleSQLiteQuery(queryString, args.toArray())
    }

    fun updateSearch() {
        val newQuery = constructQuery()
        if(listQuery.value != newQuery)
            listQuery.postValue(newQuery)         // only update if the query was actually changed!
    }


    /**
     * Clears all search criteria
     * @param updateSearch if true, the search is updated immediately, if false, only the search criteria is cleared
     */
    fun clearFilter() {
        searchCategories.clear()
        searchOrganizer.clear()
        searchStatusJournal.clear()
        searchStatusTodo.clear()
        searchClassification.clear()
        searchCollection.clear()
        updateSearch()
    }


    fun updateProgress(itemId: Long, newPercent: Int, isLinkedRecurringInstance: Boolean) {

        viewModelScope.launch(Dispatchers.IO) {
            val currentItem = database.getICalObjectById(itemId)
            ICalObject.makeRecurringException(currentItem!!, database)
            val item = database.getSync(itemId)!!.property
            database.update(item.setUpdatedProgress(newPercent))

        }
        if(isLinkedRecurringInstance)
            Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_LONG).show()

    }

    fun removeDeletedAccounts(allDavx5Accounts: Array<Account>) {

        Log.d("checkForDeletedAccounts", "Found accounts: $allDavx5Accounts")
        allRemoteCollections.value?.forEach { collection ->

            val found = allDavx5Accounts.find { account ->
                collection.accountName == account.name && collection.accountType == account.type
            }

            // if the collection cannot be found in the list of accounts, then it was deleted, delete it also in jtx
            if (found == null) {
                Log.d("checkForDeletedAccounts", "Account ${collection.accountName} / ${collection.accountType} not found, deleting...")
                viewModelScope.launch(Dispatchers.IO) {
                    database.deleteICalCollectionbyId(collection.collectionId)
                }
            }
        }
    }
}
