/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class ListWidgetContentTest {

    @Test
    fun calculateWidgetDescriptionMaxLines_singleEntryUsesWholeHeight() {
        assertEquals(20, calculateWidgetDescriptionMaxLines(heightForDescriptions = 320.dp, numEntriesWithDescription = 1, descriptionLineHeight = 16.dp))
    }

    @Test
    fun calculateWidgetDescriptionMaxLines_heightIsSharedBetweenEntries() {
        assertEquals(6, calculateWidgetDescriptionMaxLines(heightForDescriptions = 320.dp, numEntriesWithDescription = 3, descriptionLineHeight = 16.dp))
    }

    @Test
    fun calculateWidgetDescriptionMaxLines_notEnoughSpace() {
        assertEquals(MIN_WIDGET_DESCRIPTION_LINES, calculateWidgetDescriptionMaxLines(heightForDescriptions = 40.dp, numEntriesWithDescription = 10, descriptionLineHeight = 16.dp))
        assertEquals(MIN_WIDGET_DESCRIPTION_LINES, calculateWidgetDescriptionMaxLines(heightForDescriptions = (-100).dp, numEntriesWithDescription = 10, descriptionLineHeight = 16.dp))
    }

    @Test
    fun estimateWidgetTextLines_emptyText() {
        assertEquals(0, estimateWidgetTextLines(null, fontSize = 14.dp, width = 200.dp))
        assertEquals(0, estimateWidgetTextLines("", fontSize = 14.dp, width = 200.dp))
    }

    @Test
    fun estimateWidgetTextLines_shortText() {
        assertEquals(1, estimateWidgetTextLines("Short", fontSize = 14.dp, width = 200.dp))
    }

    @Test
    fun estimateWidgetTextLines_wrappedText() {
        // 10 dp font size and 55 dp width -> 10 characters per line
        assertEquals(3, estimateWidgetTextLines("a".repeat(25), fontSize = 10.dp, width = 55.dp))
    }

    @Test
    fun estimateWidgetTextLines_lineBreaks() {
        assertEquals(4, estimateWidgetTextLines("first\n\n" + "a".repeat(15), fontSize = 10.dp, width = 55.dp))
    }

    @Test
    fun calculateWidgetDescriptionMaxLines_noEntriesWithDescription() {
        assertEquals(MIN_WIDGET_DESCRIPTION_LINES, calculateWidgetDescriptionMaxLines(heightForDescriptions = 320.dp, numEntriesWithDescription = 0, descriptionLineHeight = 16.dp))
    }
}
