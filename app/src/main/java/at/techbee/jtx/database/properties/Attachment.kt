/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.properties

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.BaseColumns
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.room.*
import androidx.work.*
import at.techbee.jtx.FileCleanupJob
import at.techbee.jtx.R
import at.techbee.jtx.database.COLUMN_ID
import at.techbee.jtx.database.ICalObject
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.IOException


/** The name of the the table for Attachments that are linked to an ICalObject.
 * [https://tools.ietf.org/html/rfc5545#section-3.8.1.4]*/
const val TABLE_NAME_ATTACHMENT = "attachment"

/** The name of the ID column for attachments.
 * This is the unique identifier of an Attachment
 * Type: [Long]*/
const val COLUMN_ATTACHMENT_ID = BaseColumns._ID

/** The name of the Foreign Key Column for IcalObjects.
 * Type: [Long] */
const val COLUMN_ATTACHMENT_ICALOBJECT_ID = "icalObjectId"


/* The names of all the other columns  */
/**
 * Purpose:  This property specifies the uri of an attachment.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.1]
 * Type: [String]
 */
const val COLUMN_ATTACHMENT_URI = "uri"

/**
 * Purpose:  To specify the value of the attachment (binary).
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.1]
 * Type: [String]
 */
const val COLUMN_ATTACHMENT_BINARY = "binary"

/**
 * Purpose:  To specify the fmttype of the attachment.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.1]
 * Type: [String]
 */
const val COLUMN_ATTACHMENT_FMTTYPE = "fmttype"

/**
 * Purpose:  To specify other properties for the attachment.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.1]
 * Type: [String]
 */
const val COLUMN_ATTACHMENT_OTHER = "other"

/**
 * Purpose:  To specify the filename for the attachment.
 * not in RFC-5545
 * Type: [String]
 */
const val COLUMN_ATTACHMENT_FILENAME = "filename"

/**
 * Purpose:  To specify the extension for the attachment including "." (eg. ".pdf").
 * not in RFC-5545
 * Type: [String]
 */
const val COLUMN_ATTACHMENT_EXTENSION = "extension"

/**
 * Purpose:  To specify the filesize for the attachment in Bytes.
 * not in RFC-5545
 * Type: [Long]
 */
const val COLUMN_ATTACHMENT_FILESIZE = "filesize"


@Parcelize
@Entity(tableName = TABLE_NAME_ATTACHMENT,
        foreignKeys = [ForeignKey(entity = ICalObject::class,
                parentColumns = arrayOf(COLUMN_ID),
                childColumns = arrayOf(COLUMN_ATTACHMENT_ICALOBJECT_ID),
                onDelete = ForeignKey.CASCADE)])
data class Attachment (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ATTACHMENT_ID)
    var attachmentId: Long = 0L,

    @ColumnInfo(index = true, name = COLUMN_ATTACHMENT_ICALOBJECT_ID) var icalObjectId: Long = 0L,
    @ColumnInfo(name = COLUMN_ATTACHMENT_URI)                        var uri: String? = null,
    @ColumnInfo(name = COLUMN_ATTACHMENT_BINARY)               var binary: String? = null,
    @ColumnInfo(name = COLUMN_ATTACHMENT_FMTTYPE)               var fmttype: String? = null,
    @ColumnInfo(name = COLUMN_ATTACHMENT_OTHER)                      var other: String? = null,
    @ColumnInfo(name = COLUMN_ATTACHMENT_FILENAME)                      var filename: String? = null,
    @ColumnInfo(name = COLUMN_ATTACHMENT_EXTENSION)                      var extension: String? = null,
    @ColumnInfo(name = COLUMN_ATTACHMENT_FILESIZE)                      var filesize: Long? = null

): Parcelable


{
    companion object Factory {

        const val FMTTYPE_AUDIO_3GPP = "audio/3gpp"
        const val FMTTYPE_AUDIO_MP4_AAC = "audio/aac"
        const val FMTTYPE_AUDIO_OGG = "audio/ogg"

        //const val ATTACHMENT_DIR = "attachments"


        /**
         * Create a new [Attachment] from the specified [ContentValues].
         *
         * @param values that at least contain [COLUMN_ATTACHMENT_ICALOBJECT_ID]
         * @return A newly created [Attachment] instance.
         */
        fun fromContentValues(@Nullable values: ContentValues?): Attachment? {

            if (values == null)
                return null

            if (values.getAsLong(COLUMN_ATTACHMENT_ICALOBJECT_ID) == null)     // at least a icalObjectId and text must be given!
                return null

            return Attachment().applyContentValues(values)
        }


        /**
         * Returns the directory with the attachments.
         * If the folder doesn't exist, it will be created.
         * This function is necessary, as the user might
         * choose to put the application to the external storage,
         * the files must still be stored and retrieved from the right place
         */
        fun getAttachmentDirectory(context: Context?): File? {

            if(context == null)
                return null

            val filesPath = File(context.filesDir, "attachments/")
            if (!filesPath.exists()) {
                if (!filesPath.mkdirs()) {
                    Log.e("Attachment", "Failed creating attachment directory")
                    return null
                }
            }
            return filesPath
        }


        fun scheduleCleanupJob(context: Context) {

            //TODO: This constraint is currently not used as it didn't work in the test, this should be further investigated!
            // set constraints for the scheduler
            val constraints = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Constraints.Builder()
                    .setRequiresDeviceIdle(true)
                    .setRequiresBatteryNotLow(true)
                    .build()
            } else {
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            }

            //create the cleanup job to make sure that the files are getting deleted as well when the device is idle
            val fileCleanupWorkRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<FileCleanupJob>()
                    // Additional configuration
                    .setConstraints(constraints)
                    .build()

            // enqueue the fileCleanupWorkRequest
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork("fileCleanupWorkRequest", ExistingWorkPolicy.KEEP, fileCleanupWorkRequest)

            Log.d("IcalEditFragment", "enqueued fileCleanupWorkRequest")
        }

        fun getSample() = Attachment(
            attachmentId = 1L,
            icalObjectId = 1L,
            uri = "content://whatever",
            null,
            "application/pdf",
            null,
            "myfile",
            "pdf",
            123000
        )


    }

    fun applyContentValues(values: ContentValues): Attachment {

        values.getAsLong(COLUMN_ATTACHMENT_ICALOBJECT_ID)?.let { icalObjectId -> this.icalObjectId = icalObjectId }
        values.getAsString(COLUMN_ATTACHMENT_URI)?.let { uri -> this.uri = uri }
        values.getAsString(COLUMN_ATTACHMENT_BINARY)?.let { value -> this.binary = value }
        values.getAsString(COLUMN_ATTACHMENT_FMTTYPE)?.let { fmttype -> this.fmttype = fmttype }
        values.getAsString(COLUMN_ATTACHMENT_OTHER)?.let { other -> this.other = other }
        values.getAsString(COLUMN_ATTACHMENT_FILENAME)?.let { filename -> this.filename = filename }
        values.getAsString(COLUMN_ATTACHMENT_EXTENSION)?.let { extension -> this.extension = extension }
        values.getAsLong(COLUMN_ATTACHMENT_FILESIZE)?.let { filesize -> this.filesize = filesize }

        try {
            val uri = Uri.parse(uri)
            if (filename.isNullOrBlank())
                filename = uri.lastPathSegment
        } catch (e: NullPointerException) {
            if(binary.isNullOrEmpty())
                Log.i("Attachment", "Binary is empty and Uri could not be parsed: $uri. \n $e")
        }

        // TODO: make sure that the additional fields are filled out (filename, filesize and extension)

        return this
    }

    fun openFile(context: Context) {

        val uri = Uri.parse(this.uri) ?: return

        if(uri.toString().startsWith("content://", true)) {

            try {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(Uri.parse(this.uri), this.fmttype)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

            } catch (e: IOException) {
                Log.i("fileprovider", "Failed to retrieve file\n$e")
                Toast.makeText(
                    context,
                    context.getText(R.string.attachment_error_on_retrieving_file),
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: ActivityNotFoundException) {
                Log.i("ActivityNotFound", "No activity found to open file\n$e")
                Toast.makeText(
                    context,
                    context.getText(R.string.attachment_error_no_app_found_to_open_file_or_uri),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            try {
                //TODO: Improve the handling here, the uri might be without https:// then the call would fail
                val intent = Intent(Intent.ACTION_VIEW)
                intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK
                intent.data = uri
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.i("ActivityNotFound", "No activity found to open file\n$e")
                Toast.makeText(
                    context,
                    context.getText(R.string.attachment_error_no_app_found_to_open_file_or_uri),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun getFilesize(context: Context): Long {
        return try {
            context.contentResolver.openFileDescriptor(Uri.parse(this.uri), "r")?.statSize ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun getFilenameOrLink(): String? {

        return when {
            uri?.startsWith("http") == true -> uri
            filename?.isNotEmpty() == true -> filename
            fmttype?.isNotEmpty() == true -> fmttype
            else -> null
        }
    }
}


