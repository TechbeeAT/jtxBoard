/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.properties.Attachment
import java.io.File

class FileCleanupJob (private val appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

        override suspend fun doWork(): Result {

            val foundFileContentUris = mutableListOf<Uri>()
            val database = ICalDatabase.getInstance(appContext.applicationContext).iCalDatabaseDao

            Log.d("FileCleanupJob", "File CleanupJob started")

            Attachment.getAttachmentDirectory(appContext)?.listFiles()?.forEach {
                Log.d("FileInFolder", it.path.toString())
                val fileContentUri = FileProvider.getUriForFile(appContext, AUTHORITY_FILEPROVIDER, it)
                foundFileContentUris.add(fileContentUri)
                Log.d("FileInFolderCUri", fileContentUri.toString())
            }

            val allAttachmentUris = database.getAllAttachmentUris()
            allAttachmentUris.forEach { attachment2keep ->
                foundFileContentUris.remove(Uri.parse(attachment2keep))
            }

            if(foundFileContentUris.size == 0)
                Log.d("FileCleanupJob", "No files to delete")
            else {
                foundFileContentUris.forEach {
                    appContext.contentResolver.delete(it, null, null)
                    Log.d("FileCleanupJob", "$it deleted")
                }
            }

            // Indicate whether the work finished successfully with the Result
            return Result.success()
        }
    }
