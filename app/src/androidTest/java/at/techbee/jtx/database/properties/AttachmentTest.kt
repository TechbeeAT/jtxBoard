/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import android.content.ContentValues
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.junit.Assert.*

import org.junit.Test

class AttachmentTest {


    @Test
    fun applyContentValuesTest() {

        val attachment = Attachment().apply {
            this.icalObjectId = 1L
            this.binary = "ZW1wdHk="
            this.extension = ".txt"
            this.filename = "test"
            this.filesize = 1000L
            this.fmttype = "text/html"
            this.other = "other"
            this.uri = "http://www.google.com"
        }

        val cv = ContentValues(8).apply {
            put(COLUMN_ATTACHMENT_ICALOBJECT_ID, attachment.icalObjectId)
            put(COLUMN_ATTACHMENT_URI, attachment.uri)
            put(COLUMN_ATTACHMENT_BINARY, attachment.binary)
            put(COLUMN_ATTACHMENT_FMTTYPE, attachment.fmttype)
            put(COLUMN_ATTACHMENT_OTHER, attachment.other)
            put(COLUMN_ATTACHMENT_FILENAME, attachment.filename)
            put(COLUMN_ATTACHMENT_EXTENSION, attachment.extension)
            put(COLUMN_ATTACHMENT_FILESIZE, attachment.filesize)
        }

        val attachmentFromCV = Attachment.fromContentValues(cv)

        assertEquals(attachment, attachmentFromCV)

    }

    @Test
    fun getAttachmentsDirectoryTest() {

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = Attachment.getAttachmentDirectory(context)

        assertEquals("/data/user/0/${context.packageName}/files", dir)
    }

    @Test
    fun scheduleCleanupJobTest() {

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        Attachment.scheduleCleanupJob(context)

        val workManager = WorkManager.getInstance(context)
        val workInfo = workManager.getWorkInfosForUniqueWork("fileCleanupWorkRequest").get()

        //check if the Work was enqueued
        assertEquals(workInfo.firstOrNull()?.state, WorkInfo.State.ENQUEUED)
    }
}