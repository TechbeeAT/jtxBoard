/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import org.junit.Assert.*

import org.junit.Test

class AttachmentTest {

    @Test
    fun getFilenameOrLink_filename() {

        val attachment = Attachment()
        attachment.filename = "test.pdf"
        attachment.fmttype = "application/pdf"
        attachment.uri = "content://at.techbee.jtx.fileprovider/attachments/1642417199207.PDF"
        assertEquals(attachment.filename, attachment.getFilenameOrLink())
    }

    @Test
    fun getFilenameOrLink_link() {

        val attachment = Attachment()
        attachment.filename = null
        attachment.fmttype = "application/pdf"
        attachment.uri = "https://jtx.techbee.at/test.txt"
        assertEquals(attachment.uri, attachment.getFilenameOrLink())
    }

    @Test
    fun getFilenameOrLink_fmttype() {

        val attachment = Attachment()
        attachment.filename = null
        attachment.fmttype = "application/pdf"
        attachment.uri = "content://at.techbee.jtx.fileprovider/attachments/1642417199207.PDF"
        assertEquals(attachment.fmttype, attachment.getFilenameOrLink())
    }

    @Test
    fun getFilenameOrLink_null() {

        val attachment = Attachment()
        attachment.filename = null
        attachment.fmttype = null
        attachment.uri = null
        assertNull(attachment.getFilenameOrLink())
    }
}