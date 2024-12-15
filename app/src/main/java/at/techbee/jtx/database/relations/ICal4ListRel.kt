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
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
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
import at.techbee.jtx.ui.list.SortOrder
import at.techbee.jtx.util.DateTimeUtils
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale


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

        fun getGroupedList(initialList: List<ICal4ListRel>, groupBy: GroupBy?, sortOrder: SortOrder, module: Module, context: Context): Map<String, List<ICal4ListRel>> {
            // first apply a proper sort order, then group
            var sortedList = when (groupBy) {
                GroupBy.STATUS -> initialList.sortedBy {
                    if (module == Module.TODO && it.iCal4List.percent != 100)
                        try {
                            Status.valueOf(it.iCal4List.status ?: Status.NO_STATUS.name).ordinal
                        } catch (e: java.lang.IllegalArgumentException) {
                            -1
                        }
                    else
                        try {
                            Status.valueOf(it.iCal4List.status ?: Status.FINAL.name).ordinal
                        } catch (e: java.lang.IllegalArgumentException) {
                            -1
                        }
                }.let { if (sortOrder == SortOrder.DESC) it.asReversed() else it }
                GroupBy.CLASSIFICATION -> initialList.sortedBy {
                    try {
                        Classification.valueOf(it.iCal4List.classification ?: Classification.PUBLIC.name).ordinal
                    } catch (e: java.lang.IllegalArgumentException) {
                        -1
                    }
                }.let { if (sortOrder == SortOrder.DESC) it.asReversed() else it }
                else -> initialList
            }

            if ((groupBy == GroupBy.STATUS || groupBy == GroupBy.CLASSIFICATION) && sortOrder == SortOrder.DESC)
                sortedList = sortedList.asReversed()

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
                }.toSortedMap(
                    if(sortOrder == SortOrder.DESC)
                        compareByDescending { it.uppercase() }
                    else
                        compareBy { it.uppercase() }
                )
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
                }.toSortedMap(
                    if(sortOrder == SortOrder.DESC)
                        compareByDescending { it.uppercase() }
                    else
                        compareBy { it.uppercase() }
                )
                //GroupBy.CATEGORY -> sortedList.groupBy { if(it.categories.isEmpty()) context.getString(R.string.filter_no_category) else it.categories.joinToString(separator = ", ") { category -> category.text } }.toSortedMap()
                //GroupBy.RESOURCE -> sortedList.groupBy { if(it.resources.isEmpty()) context.getString(R.string.filter_no_resource) else it.resources.joinToString(separator = ", ") { resource -> resource.text?:"" } }.toSortedMap()
                GroupBy.STATUS -> sortedList.groupBy {
                    Status.entries.find { status -> status.status == it.iCal4List.status }?.stringResource?.let { stringRes -> context.getString(stringRes) } ?: it.iCal4List.status ?: ""
                }

                GroupBy.CLASSIFICATION -> sortedList.groupBy {
                    Classification.entries.find { classif -> classif.classification == it.iCal4List.classification }?.stringResource?.let { stringRes -> context.getString(stringRes) }
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

                GroupBy.DATE, GroupBy.START -> sortedList.groupBy {
                    ICalObject.getDtstartTextInfo(
                        module = module,
                        dtstart = it.iCal4List.dtstart,
                        dtstartTimezone = it.iCal4List.dtstartTimezone,
                        daysOnly = true,
                        context = context
                    )
                }
                GroupBy.DATE_WEEK, GroupBy.START_WEEK -> sortedList.groupBy {ical4ListRel ->
                    if(ical4ListRel.iCal4List.dtstart == null && module == Module.TODO)
                        context.getString(R.string.list_start_without)
                    else if(ical4ListRel.iCal4List.dtstart == null)
                        context.getString(R.string.list_date_without)
                    else
                        ical4ListRel.iCal4List.dtstart!!.let {
                            val date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), DateTimeUtils.requireTzId(ical4ListRel.iCal4List.dtstartTimezone)).toLocalDate()
                            context.getString(
                                R.string.week_number_year,
                                date[WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()],
                                date.year
                            )
                        }
                }
                GroupBy.DATE_MONTH, GroupBy.START_MONTH -> sortedList.groupBy {ical4ListRel ->
                    if(ical4ListRel.iCal4List.dtstart == null && module == Module.TODO)
                        context.getString(R.string.list_start_without)
                    else if(ical4ListRel.iCal4List.dtstart == null)
                        context.getString(R.string.list_date_without)
                    else
                        ical4ListRel.iCal4List.dtstart!!.let {
                            val date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), DateTimeUtils.requireTzId(ical4ListRel.iCal4List.dtstartTimezone)).toLocalDate()
                            "${date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${date.year}"
                        }
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
                GroupBy.DUE_WEEK -> sortedList.groupBy {ical4ListRel ->
                    if(ical4ListRel.iCal4List.due == null)
                        context.getString(R.string.list_due_without)
                    else
                        ical4ListRel.iCal4List.due!!.let {
                            val date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), DateTimeUtils.requireTzId(ical4ListRel.iCal4List.dueTimezone)).toLocalDate()
                            context.getString(
                                R.string.week_number_year,
                                date[WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()],
                                date.year
                            )
                        }
                }
                GroupBy.DUE_MONTH -> sortedList.groupBy {ical4ListRel ->
                    if(ical4ListRel.iCal4List.due == null)
                        context.getString(R.string.list_due_without)
                    else
                        ical4ListRel.iCal4List.due!!.let {
                            val date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), DateTimeUtils.requireTzId(ical4ListRel.iCal4List.dueTimezone)).toLocalDate()
                            "${date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${date.year}"
                        }
                }

                null -> sortedList.groupBy { it.iCal4List.module }
            }
        }

        /**
         * Generates a string of headers for a CSV
         * Use getCSVRow(...) to get the row that correponds to this header
         * @return a String with the headers delimited by |
         */
        fun getCSVHeader(module: Module, context: Context): String {
            return mutableListOf<String>().apply {
                if (module == Module.JOURNAL)
                    add(context.getString(R.string.date))
                if (module == Module.TODO) {
                    add(context.getString(R.string.started))
                    add(context.getString(R.string.due))
                    add(context.getString(R.string.completed))
                }
                add(context.getString(R.string.summary))
                add(context.getString(R.string.description))

                if (module == Module.TODO)
                    add(context.getString(R.string.progress))

                add(context.getString(R.string.categories))
                if (module == Module.TODO)
                    add(context.getString(R.string.resources))

                add(context.getString(R.string.status))
                add(context.getString(R.string.classification))
                if (module == Module.TODO)
                    add(context.getString(R.string.priority))

                add(context.getString(R.string.contact))
                add(context.getString(R.string.url))
                add(context.getString(R.string.location))
                add(context.getString(R.string.latitude) + "/" + context.getString(R.string.longitude))

                add(context.getString(R.string.subtasks))
                add(context.getString(R.string.view_feedback_linked_notes))
                add(context.getString(R.string.attachments))
                add(context.getString(R.string.attendees))
                add(context.getString(R.string.comments))
                add(context.getString(R.string.alarms))

                add(context.getString(R.string.account))
                add(context.getString(R.string.collection))

                add(context.getString(R.string.filter_created))
                add(context.getString(R.string.filter_last_modified))
            }.joinToString(separator = "|")
        }
    }


    /**
     * @return string of values o the current entry delimited by | that corresponds to the headers in getCSVHeader(...)
     */
    fun getCSVRow(context: Context): String {
        return mutableListOf<String>().apply {
            if(iCal4List.module == Module.JOURNAL.name)
                add(DateTimeUtils.convertLongToExcelDateTimeString(iCal4List.dtstart, if(iCal4List.dtstartTimezone == TZ_ALLDAY) "UTC" else iCal4List.dtstartTimezone))
            if(iCal4List.module == Module.TODO.name) {
                add(DateTimeUtils.convertLongToExcelDateTimeString(iCal4List.dtstart, if(iCal4List.dtstartTimezone == TZ_ALLDAY) "UTC" else iCal4List.dtstartTimezone))
                add(DateTimeUtils.convertLongToExcelDateTimeString(iCal4List.due, if(iCal4List.dueTimezone == TZ_ALLDAY) "UTC" else iCal4List.dueTimezone))
                add(DateTimeUtils.convertLongToExcelDateTimeString(iCal4List.completed, if(iCal4List.completedTimezone == TZ_ALLDAY) "UTC" else iCal4List.completedTimezone))
            }

            add(iCal4List.summary?:"")
            add(iCal4List.description?:"")

            if(iCal4List.module == Module.TODO.name)
                add(iCal4List.percent?.toString()?:"")

            add(iCal4List.getSplitCategories().joinToString(separator = ", "))
            if(iCal4List.module == Module.TODO.name)
                add(iCal4List.resources?:"")

            add(Status.getStatusFromString(iCal4List.status)?.let  {
                context.getString(it.stringResource)
            }?:iCal4List.status ?:"")
            add(Classification.getClassificationFromString(iCal4List.classification)?.let  {
                context.getString(it.stringResource)
            }?:iCal4List.classification ?:"")
            if(iCal4List.module == Module.TODO.name)
                add(if (iCal4List.priority in 1..9) context.resources.getStringArray(R.array.priority)[iCal4List.priority!!] else "")

            add(iCal4List.contact?:"")
            add(iCal4List.url?:"")
            add(iCal4List.location?:"")
            add(if(iCal4List.geoLat != null && iCal4List.geoLong != null)
                "(" + iCal4List.geoLat!!.toString() + ", " + iCal4List.geoLong!!.toString() + ")" else ""
            )

            add(iCal4List.numSubtasks.toString())
            add(iCal4List.numSubnotes.toString())
            add(iCal4List.numAttachments.toString())
            add(iCal4List.numAttendees.toString())
            add(iCal4List.numComments.toString())
            add(iCal4List.numAlarms.toString())

            add(iCal4List.accountName?:"")
            add(iCal4List.collectionDisplayName?:"")

            add(DateTimeUtils.convertLongToExcelDateTimeString(iCal4List.created, null))
            add(DateTimeUtils.convertLongToExcelDateTimeString(iCal4List.lastModified, null))
        }.joinToString(separator = "|")
    }
}