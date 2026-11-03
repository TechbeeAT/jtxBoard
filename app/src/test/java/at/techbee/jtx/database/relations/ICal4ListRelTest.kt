/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.relations

import android.content.Context
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.ui.list.GroupBy
import at.techbee.jtx.ui.list.OrderBy
import at.techbee.jtx.ui.list.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class ICal4ListRelTest {

    private val context = mock(Context::class.java)

    private fun entry(id: Long, summary: String, categories: List<String> = emptyList(), resources: List<String> = emptyList()) = ICal4ListRel(
        iCal4List = ICal4List.getSample(Module.TODO).copy(id = id, summary = summary),
        relatedto = emptyList(),
        categories = categories.mapIndexed { index, text -> Category(categoryId = id * 10 + index, icalObjectId = id, text = text) },
        resources = resources.mapIndexed { index, text -> Resource(resourceId = id * 10 + index, icalObjectId = id, text = text) }
    )

    @Test
    fun getGroupedList_groupByCategory_sortsBySecondOrderWithinGroup() {
        val list = listOf(
            entry(1L, "Alpha", listOf("Work")),
            entry(2L, "Charlie", listOf("Work", "Urgent")),
            entry(3L, "Bravo", listOf("Work", "Home")),
        )

        val sortedList = ICal4ListRel.getSortedList(
            initialList = list,
            orderBy = OrderBy.CATEGORIES,
            sortOrder = SortOrder.ASC,
            orderBy2 = OrderBy.SUMMARY,
            sortOrder2 = SortOrder.ASC,
            module = Module.TODO,
            groupBy = GroupBy.CATEGORY
        )
        val groupedList = ICal4ListRel.getGroupedList(sortedList, GroupBy.CATEGORY, SortOrder.ASC, Module.TODO, context)

        assertEquals(listOf("Home", "Urgent", "Work"), groupedList.keys.toList())
        assertEquals(listOf("Alpha", "Bravo", "Charlie"), groupedList["Work"]?.map { it.iCal4List.summary })
    }

    @Test
    fun getGroupedList_groupByResource_sortsBySecondOrderWithinGroup() {
        val list = listOf(
            entry(1L, "Alpha", resources = listOf("Room", "Beamer")),
            entry(2L, "Charlie", resources = listOf("Room")),
            entry(3L, "Bravo", resources = listOf("Room", "Screen")),
        )

        val sortedList = ICal4ListRel.getSortedList(
            initialList = list,
            orderBy = OrderBy.RESOURCES,
            sortOrder = SortOrder.ASC,
            orderBy2 = OrderBy.SUMMARY,
            sortOrder2 = SortOrder.DESC,
            module = Module.TODO,
            groupBy = GroupBy.RESOURCE
        )
        val groupedList = ICal4ListRel.getGroupedList(sortedList, GroupBy.RESOURCE, SortOrder.ASC, Module.TODO, context)

        assertEquals(listOf("Charlie", "Bravo", "Alpha"), groupedList["Room"]?.map { it.iCal4List.summary })
    }

    @Test
    fun getSortedList_orderByCategoriesWithoutGrouping_sortsByAllCategories() {
        val list = listOf(
            entry(1L, "Alpha", listOf("Work")),
            entry(2L, "Charlie", listOf("Work", "Urgent")),
            entry(3L, "Bravo", listOf("Work", "Home")),
        )

        val sortedList = ICal4ListRel.getSortedList(
            initialList = list,
            orderBy = OrderBy.CATEGORIES,
            sortOrder = SortOrder.ASC,
            orderBy2 = OrderBy.SUMMARY,
            sortOrder2 = SortOrder.ASC,
            module = Module.TODO
        )

        assertEquals(listOf("Bravo", "Charlie", "Alpha"), sortedList.map { it.iCal4List.summary })
    }

    @Test
    fun getSortedList_orderByCategories_sameCategories_sortsBySecondOrder() {
        val list = listOf(
            entry(1L, "Charlie", listOf("Work")),
            entry(2L, "Alpha", listOf("Work")),
            entry(3L, "Bravo", listOf("Work")),
        )

        val sortedList = ICal4ListRel.getSortedList(
            initialList = list,
            orderBy = OrderBy.CATEGORIES,
            sortOrder = SortOrder.ASC,
            orderBy2 = OrderBy.SUMMARY,
            sortOrder2 = SortOrder.ASC,
            module = Module.TODO
        )

        assertEquals(listOf("Alpha", "Bravo", "Charlie"), sortedList.map { it.iCal4List.summary })
    }
}
