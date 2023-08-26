/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.relations

import android.content.Context
import androidx.room.Embedded
import androidx.room.Relation
import at.techbee.jtx.R
import at.techbee.jtx.database.COLUMN_ID
import at.techbee.jtx.database.Classification
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.Status
import at.techbee.jtx.database.properties.COLUMN_CATEGORY_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_RELATEDTO_ICALOBJECT_ID
import at.techbee.jtx.database.properties.COLUMN_RESOURCE_ICALOBJECT_ID
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.GroupBy


data class ICal4ListRel(
    @Embedded
    var iCal4List: ICal4List,

    @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_RELATEDTO_ICALOBJECT_ID, entity = Relatedto::class)
    var relatedto: List<Relatedto>,

    @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_CATEGORY_ICALOBJECT_ID, entity = Category::class)
    var categories: List<Category>,

    @Relation(parentColumn = COLUMN_ID, entityColumn = COLUMN_RESOURCE_ICALOBJECT_ID, entity = Resource::class)
    var resources: List<Resource>
) {
    companion object {

        fun getGroupedList(sortedList: List<ICal4ListRel>, groupBy: GroupBy?, context: Context): Map<String, List<ICal4ListRel>> {
            return when (groupBy) {
                GroupBy.CATEGORY -> mutableMapOf<String, MutableList<ICal4ListRel>>().apply {

                    sortedList.forEach { sortedEntry ->
                        if (sortedEntry.categories.isNotEmpty()) {
                            sortedEntry.categories.forEach { category ->
                                if (this.containsKey(category.text))
                                    this[category.text]?.add(sortedEntry)
                                else
                                    this[category.text] = mutableListOf(sortedEntry)
                            }
                        } else {
                            if (this.containsKey(context.getString(R.string.filter_no_category)))
                                this[context.getString(R.string.filter_no_category)]?.add(sortedEntry)
                            else
                                this[context.getString(R.string.filter_no_category)] = mutableListOf(sortedEntry)
                        }
                    }
                }
                GroupBy.RESOURCE -> mutableMapOf<String, MutableList<ICal4ListRel>>().apply {
                    sortedList.forEach { sortedEntry ->
                        if (sortedEntry.resources.isNotEmpty()) {
                            sortedEntry.resources.forEach { resource ->
                                if (this.containsKey(resource.text))
                                    this[resource.text]?.add(sortedEntry)
                                else
                                    this[resource.text ?: context.getString(R.string.filter_no_resource)] = mutableListOf(sortedEntry)
                            }
                        } else {
                            if (this.containsKey(context.getString(R.string.filter_no_resource)))
                                this[context.getString(R.string.filter_no_resource)]?.add(sortedEntry)
                            else
                                this[context.getString(R.string.filter_no_resource)] = mutableListOf(sortedEntry)
                        }
                    }
                }
                //GroupBy.CATEGORY -> sortedList.groupBy { if(it.categories.isEmpty()) context.getString(R.string.filter_no_category) else it.categories.joinToString(separator = ", ") { category -> category.text } }.toSortedMap()
                //GroupBy.RESOURCE -> sortedList.groupBy { if(it.resources.isEmpty()) context.getString(R.string.filter_no_resource) else it.resources.joinToString(separator = ", ") { resource -> resource.text?:"" } }.toSortedMap()
                GroupBy.STATUS -> sortedList.groupBy {
                    Status.values().find { status -> status.status == it.iCal4List.status }?.stringResource?.let { stringRes -> context.getString(stringRes) } ?: it.iCal4List.status ?: ""
                }

                GroupBy.CLASSIFICATION -> sortedList.groupBy {
                    Classification.values().find { classif -> classif.classification == it.iCal4List.classification }?.stringResource?.let { stringRes -> context.getString(stringRes) }
                        ?: it.iCal4List.classification ?: ""
                }

                GroupBy.ACCOUNT -> sortedList.groupBy { it.iCal4List.accountName ?: "" }
                GroupBy.COLLECTION -> sortedList.groupBy { it.iCal4List.collectionDisplayName ?: "" }
                GroupBy.PRIORITY -> sortedList.groupBy {
                    when (it.iCal4List.priority) {
                        null -> context.resources.getStringArray(R.array.priority)[0]
                        in 0..9 -> context.resources.getStringArray(R.array.priority)[it.iCal4List.priority!!]
                        else -> it.iCal4List.priority.toString()
                    }
                }

                GroupBy.DATE -> sortedList.groupBy {
                    ICalObject.getDtstartTextInfo(
                        module = Module.JOURNAL,
                        dtstart = it.iCal4List.dtstart,
                        dtstartTimezone = it.iCal4List.dtstartTimezone,
                        daysOnly = true,
                        context = context
                    )
                }

                GroupBy.START -> sortedList.groupBy {
                    ICalObject.getDtstartTextInfo(
                        module = Module.TODO,
                        dtstart = it.iCal4List.dtstart,
                        dtstartTimezone = it.iCal4List.dtstartTimezone,
                        daysOnly = true,
                        context = context
                    )
                }

                GroupBy.DUE -> sortedList.groupBy {
                    ICalObject.getDueTextInfo(
                        status = it.iCal4List.status,
                        due = it.iCal4List.due,
                        dueTimezone = it.iCal4List.dueTimezone,
                        percent = it.iCal4List.percent,
                        daysOnly = true,
                        context = context
                    )
                }
                null -> sortedList.groupBy { it.iCal4List.module }
            }
        }
    }
}