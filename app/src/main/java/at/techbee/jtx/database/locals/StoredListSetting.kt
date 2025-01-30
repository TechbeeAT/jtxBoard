/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.locals

import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.ui.list.AnyAllNone
import at.techbee.jtx.ui.list.GroupBy
import at.techbee.jtx.ui.list.ListSettings
import at.techbee.jtx.ui.list.OrderBy
import at.techbee.jtx.ui.list.SortOrder
import at.techbee.jtx.ui.list.ViewMode
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


/** The name of the the table for filters that are stored only locally. */
const val TABLE_NAME_STORED_LIST_SETTINGS = "stored_list_settings"

/** The name of the ID column for resources.
 * This is the unique identifier of a Resource
 * Type: [Long]*/
const val COLUMN_STORED_LIST_SETTING_ID = BaseColumns._ID

const val COLUMN_STORED_LIST_SETTING_MODULE = "module"
const val COLUMN_STORED_LIST_SETTING_NAME = "name"
const val COLUMN_STORED_LIST_SETTING_SETTINGS = "list_settings"


@Serializable
@Parcelize
@Entity(tableName = TABLE_NAME_STORED_LIST_SETTINGS)
data class StoredListSetting (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_STORED_LIST_SETTING_ID)
    var id: Long = 0L,

    @ColumnInfo(name = COLUMN_STORED_LIST_SETTING_MODULE) var module: Module,
    @ColumnInfo(name = COLUMN_STORED_LIST_SETTING_NAME) var name: String,
    @ColumnInfo(name = COLUMN_STORED_LIST_SETTING_SETTINGS) var storedListSettingData: StoredListSettingData
): Parcelable



@Parcelize
@Serializable
data class StoredListSettingData(
    var searchCategories: List<String> = emptyList(),
    var searchCategoriesAnyAllNone: AnyAllNone = AnyAllNone.ANY,
    var searchResources: List<String> = emptyList(),
    var searchResourcesAnyAllNone: AnyAllNone = AnyAllNone.ANY,
    var searchStatus: List<Status> = emptyList(),
    var searchClassification: List<Classification> = emptyList(),
    var searchPriority: List<Int?> = emptyList(),
    var searchCollection: List<String> = emptyList(),
    var searchAccount: List<String> = emptyList(),
    var orderBy: OrderBy = OrderBy.CREATED,
    var sortOrder: SortOrder = SortOrder.ASC,
    var orderBy2: OrderBy = OrderBy.SUMMARY,
    var sortOrder2: SortOrder = SortOrder.ASC,
    var groupBy: GroupBy? = null,
    var subtasksOrderBy: OrderBy = OrderBy.CREATED,
    var subtasksSortOrder: SortOrder = SortOrder.ASC,
    var subnotesOrderBy: OrderBy = OrderBy.CREATED,
    var subnotesSortOrder: SortOrder = SortOrder.ASC,
    var isExcludeDone: Boolean = false,
    var isFilterOverdue: Boolean = false,
    var isFilterDueToday: Boolean = false,
    var isFilterDueTomorrow: Boolean = false,
    var isFilterDueWithin7Days: Boolean = false,
    var isFilterDueFuture: Boolean = false,
    var isFilterStartInPast: Boolean = false,
    var isFilterStartToday: Boolean = false,
    var isFilterStartTomorrow: Boolean = false,
    var isFilterStartWithin7Days: Boolean = false,
    var isFilterStartFuture: Boolean = false,
    var isFilterNoDatesSet: Boolean = false,
    var isFilterNoStartDateSet: Boolean = false,
    var isFilterNoDueDateSet: Boolean = false,
    var isFilterNoCompletedDateSet: Boolean = false,

    var filterStartRangeStart: Long? = null,
    var filterStartRangeEnd: Long? = null,
    var filterDueRangeStart: Long? = null,
    var filterDueRangeEnd: Long? = null,
    var filterCompletedRangeStart: Long? = null,
    var filterCompletedRangeEnd: Long? = null,

    var isFilterNoCategorySet: Boolean = false,
    var isFilterNoResourceSet: Boolean = false,
    var searchText: String? = null,        // search text is not saved!
    var viewMode: ViewMode = ViewMode.LIST,
    var flatView: Boolean = false,
    var showOneRecurEntryInFuture: Boolean = false,
) : Parcelable {

    companion object {
        fun fromListSettings(listSettings: ListSettings): StoredListSettingData {
            return StoredListSettingData(
                searchCategories = listSettings.searchCategories,
                searchCategoriesAnyAllNone = listSettings.searchCategoriesAnyAllNone.value,
                searchResources = listSettings.searchResources,
                searchResourcesAnyAllNone = listSettings.searchResourcesAnyAllNone.value,
                searchStatus = listSettings.searchStatus,
                searchClassification = listSettings.searchClassification,
                searchPriority = listSettings.searchPriority,
                searchCollection = listSettings.searchCollection,
                searchAccount = listSettings.searchAccount,
                orderBy = listSettings.orderBy.value,
                sortOrder = listSettings.sortOrder.value,
                orderBy2 = listSettings.orderBy2.value,
                sortOrder2 = listSettings.sortOrder2.value,
                groupBy = listSettings.groupBy.value,
                subtasksOrderBy = listSettings.subtasksOrderBy.value,
                subtasksSortOrder = listSettings.subtasksSortOrder.value,
                subnotesOrderBy = listSettings.subnotesOrderBy.value,
                subnotesSortOrder = listSettings.subnotesSortOrder.value,
                isExcludeDone = listSettings.isExcludeDone.value,
                isFilterOverdue = listSettings.isFilterOverdue.value,
                isFilterDueToday = listSettings.isFilterDueToday.value,
                isFilterDueTomorrow = listSettings.isFilterDueTomorrow.value,
                isFilterDueWithin7Days = listSettings.isFilterDueWithin7Days.value,
                isFilterDueFuture = listSettings.isFilterDueFuture.value,
                isFilterStartInPast = listSettings.isFilterStartInPast.value,
                isFilterStartToday = listSettings.isFilterStartToday.value,
                isFilterStartTomorrow = listSettings.isFilterStartTomorrow.value,
                isFilterStartWithin7Days = listSettings.isFilterStartWithin7Days.value,
                isFilterStartFuture = listSettings.isFilterStartFuture.value,
                isFilterNoDatesSet = listSettings.isFilterNoDatesSet.value,
                isFilterNoStartDateSet = listSettings.isFilterNoStartDateSet.value,
                isFilterNoDueDateSet = listSettings.isFilterNoDueDateSet.value,
                isFilterNoCompletedDateSet = listSettings.isFilterNoCompletedDateSet.value,
                filterStartRangeStart = listSettings.filterStartRangeStart.value,
                filterStartRangeEnd = listSettings.filterStartRangeEnd.value,
                filterDueRangeStart = listSettings.filterDueRangeStart.value,
                filterDueRangeEnd = listSettings.filterDueRangeEnd.value,
                filterCompletedRangeStart = listSettings.filterCompletedRangeStart.value,
                filterCompletedRangeEnd = listSettings.filterCompletedRangeEnd.value,
                isFilterNoCategorySet = listSettings.isFilterNoCategorySet.value,
                isFilterNoResourceSet = listSettings.isFilterNoResourceSet.value
            )
        }
    }

    fun applyToListSettings(listSettings: ListSettings) {
        listSettings.reset()
        listSettings.searchCategories.addAll(searchCategories)
        listSettings.searchCategoriesAnyAllNone.value = searchCategoriesAnyAllNone
        listSettings.searchResources.addAll(searchResources)
        listSettings.searchResourcesAnyAllNone.value = searchResourcesAnyAllNone
        listSettings.searchStatus.addAll(searchStatus)
        listSettings.searchClassification.addAll(searchClassification)
        listSettings.searchPriority.addAll(searchPriority)
        listSettings.searchCollection.addAll(searchCollection)
        listSettings.searchAccount.addAll(searchAccount)
        listSettings.orderBy.value = orderBy
        listSettings.sortOrder.value = sortOrder
        listSettings.orderBy2.value = orderBy2
        listSettings.sortOrder2.value = sortOrder2
        listSettings.groupBy.value = groupBy
        listSettings.subtasksOrderBy.value = subtasksOrderBy
        listSettings.subtasksSortOrder.value = subtasksSortOrder
        listSettings.subnotesOrderBy.value = subnotesOrderBy
        listSettings.subnotesSortOrder.value = subnotesSortOrder
        listSettings.isExcludeDone.value = isExcludeDone
        listSettings.isFilterOverdue.value = isFilterOverdue
        listSettings.isFilterDueToday.value = isFilterDueToday
        listSettings.isFilterDueTomorrow.value = isFilterDueTomorrow
        listSettings.isFilterDueWithin7Days.value = isFilterDueWithin7Days
        listSettings.isFilterDueFuture.value = isFilterDueFuture
        listSettings.isFilterStartInPast.value = isFilterStartInPast
        listSettings.isFilterStartToday.value = isFilterStartToday
        listSettings.isFilterStartTomorrow.value = isFilterStartTomorrow
        listSettings.isFilterStartWithin7Days.value = isFilterStartWithin7Days
        listSettings.isFilterStartFuture.value = isFilterStartFuture
        listSettings.isFilterNoDatesSet.value = isFilterNoDatesSet
        listSettings.isFilterNoStartDateSet.value = isFilterNoStartDateSet
        listSettings.isFilterNoDueDateSet.value = isFilterNoDueDateSet
        listSettings.isFilterNoCompletedDateSet.value = isFilterNoCompletedDateSet

        listSettings.filterStartRangeStart.value = filterStartRangeStart
        listSettings.filterStartRangeEnd.value = filterStartRangeEnd
        listSettings.filterDueRangeStart.value = filterDueRangeStart
        listSettings.filterDueRangeEnd.value = filterDueRangeEnd
        listSettings.filterCompletedRangeStart.value = filterCompletedRangeStart
        listSettings.filterCompletedRangeEnd.value = filterCompletedRangeEnd

        listSettings.isFilterNoCategorySet.value = isFilterNoCategorySet
        listSettings.isFilterNoResourceSet.value = isFilterNoResourceSet
    }
}