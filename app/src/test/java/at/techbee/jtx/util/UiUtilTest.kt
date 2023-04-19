/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import androidx.compose.ui.graphics.Color
import at.techbee.jtx.util.UiUtil.getAttachmentSizeString
import at.techbee.jtx.util.UiUtil.isDarkColor
import at.techbee.jtx.util.UiUtil.isValidEmail
import at.techbee.jtx.util.UiUtil.isValidURL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UiUtilTest {

    @Test fun isValidURL0() = assertTrue(isValidURL("https://www.orf.at/97/kjh/lll"))
    @Test fun isValidURL1() = assertTrue(isValidURL("https://www.orf.at"))
    @Test fun isValidURL2() = assertTrue(isValidURL("www.orf.at"))
    @Test fun isValidURL3() = assertTrue(isValidURL("orf.at"))


    @Test
    fun extractLinks() {
        val extracted = UiUtil.extractLinks("This is my text with www.orf.at and a second link https://www.techbee.at/")
        assertEquals(2, extracted.size)
        assertEquals("www.orf.at", extracted[0])
        assertEquals("https://www.techbee.at/", extracted[1])
    }


    @Test fun isValidEmail_testTrue() = assertTrue(isValidEmail("valid@email.com"))
    @Test fun isValidEmail_testFalse1() = assertFalse(isValidEmail("invalid.com"))
    @Test fun isValidEmail_testFalse2() = assertFalse(isValidEmail("invalid@com"))

    @Test fun isValidURL_testTrue1() = assertTrue(isValidURL("example.com"))
    @Test fun isValidURL_testTrue2() = assertTrue(isValidURL("www.example.com"))
    @Test fun isValidURL_testTrue3() = assertTrue(isValidURL("http://example.com"))
    @Test fun isValidURL_testTrue4() = assertTrue(isValidURL("https://www.example.com/asdf"))
    @Test fun isValidURL_testFalse1() = assertFalse(isValidURL("AABB"))
    @Test fun isValidURL_testFalse2() = assertFalse(isValidURL("asdf://AABB.com"))

    @Test fun getAttachmentSizeString_bytes() = assertEquals("100 Bytes", getAttachmentSizeString(100))
    @Test fun getAttachmentSizeString_kilobytes() = assertEquals("1 KB", getAttachmentSizeString(1024))
    @Test fun getAttachmentSizeString_megabytes() = assertEquals("1 MB", getAttachmentSizeString(1048576))

    @Test fun isDarkColor_black() = assertTrue(isDarkColor(Color.Black))
    @Test fun isDarkColor_blue() = assertTrue(isDarkColor(Color.Blue))
    @Test fun isDarkColor_white() = assertFalse(isDarkColor(Color.White))
    @Test fun isDarkColor_yellow() = assertFalse(isDarkColor(Color.Yellow))
}