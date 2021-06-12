package at.bitfire.notesx5

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.bitfire.notesx5.database.ICalDatabase
import java.io.File

class FileCleanupJob (val appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

        override suspend fun doWork(): Result {


            val foundFileContentUris = mutableListOf<Uri>()

            val dataSource = ICalDatabase.getInstance(appContext.applicationContext).iCalDatabaseDao

            Log.d("FileCleanupJob", "File CleanupJob started")

            val filesPath = File(appContext.filesDir, ".")
            filesPath.listFiles()?.forEach {
                Log.d("FileInFolder", it.path.toString())
                val fileContentUri = FileProvider.getUriForFile(appContext, AUTHORITY_FILEPROVIDER, it)
                foundFileContentUris.add(fileContentUri)
                Log.d("FileInFolderCUri", fileContentUri.toString())

            }

            val extFilesPath = File(appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())
            extFilesPath.listFiles()?.forEach {
                val fileContentUri = FileProvider.getUriForFile(appContext, AUTHORITY_FILEPROVIDER, it)
                foundFileContentUris.add(fileContentUri)
            }

            val allAttachmentUris = dataSource.getAllAttachmentUris()
            allAttachmentUris.forEach { attachment2keep ->
                foundFileContentUris.remove(Uri.parse(attachment2keep))
            }

            foundFileContentUris.forEach {
                appContext.contentResolver.delete(it, null, null)
                Log.d("FileCleanupJob", "$it deleted")
            }

            // Indicate whether the work finished successfully with the Result
            return Result.success()
        }
    }
