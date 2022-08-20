/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import org.junit.Assert.*

import org.junit.Test

class UiUtilTest {

    @Test
    fun isValidURL0() = assertTrue(UiUtil.isValidURL("https://www.orf.at/97/kjh/lll"))
    fun isValidURL1() = assertTrue(UiUtil.isValidURL("https://www.orf.at"))
    fun isValidURL2() = assertTrue(UiUtil.isValidURL("www.orf.at"))
    fun isValidURL3() = assertFalse(UiUtil.isValidURL("orf.at"))


    @Test
    fun extractLinks() {
        val extracted = UiUtil.extractLinks("This is my text with www.orf.at and a second link https://www.techbee.at/")
        assertEquals(2, extracted.size)
        assertEquals("www.orf.at", extracted[0])
        assertEquals("https://www.techbee.at/", extracted[1])
    }
}