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
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.provider.BaseColumns
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.work.*
import at.techbee.jtx.AUTHORITY_FILEPROVIDER
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

        /**
         * Create a new [Attachment] from the specified [ContentValues].
         *
         * @param values that at least contain [COLUMN_ATTACHMENT_ICALOBJECT_ID]
         * @return A newly created [Attachment] instance.
         */
        fun fromContentValues(values: ContentValues?): Attachment? {

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
            uri = null,
            null,
            "application/pdf",
            null,
            "myfile.pdf",
            "pdf",
            123000
        )

        /**
         * Takes an uri, creates a new file, stores the content of the uri in the file and returns a new Attachment
         * @param [uri] of the file to be stored as Attachment
         * @return [Attachment] with the new link to the Attachment (without ICalObjectId)
         */
        fun getNewAttachmentFromUri(uri: Uri, context: Context): Attachment? {
            try {
                val extension =
                    if(uri.toString().startsWith("content://"))
                        MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri))
                    else
                        MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                val filename = "${System.currentTimeMillis()}.$extension"
                val newFile = File(getAttachmentDirectory(context), filename)
                newFile.createNewFile()

                val attachmentDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                val attachmentBytes = ParcelFileDescriptor.AutoCloseInputStream(attachmentDescriptor).readBytes()
                newFile.writeBytes(attachmentBytes)

                return Attachment(
                    uri = FileProvider.getUriForFile(
                        context,
                        AUTHORITY_FILEPROVIDER,
                        newFile
                    ).toString(),
                    filename = getFileNameFromUri(context, uri) ?: filename,
                    extension = extension,
                    filesize = newFile.length(),
                    fmttype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                )
            } catch (e: IOException) {
                Log.w("IOException", "Failed to process file\n$e")
                return null
            }
        }

        private fun getFileNameFromUri(context: Context, uri: Uri): String? {
            var fileName: String? = null
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val indexDisplayName = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(cursor.count > 0 && indexDisplayName >= 0) {
                    cursor.moveToFirst()
                    fileName = cursor.getString(indexDisplayName)
                }
                cursor.close()
            }
            return fileName
        }

        fun getNewAttachmentUriForPhoto(context: Context): Uri? {
            if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                try {
                    val storageDir = getAttachmentDirectory(context)
                    val file = File.createTempFile("jtx_", ".jpg", storageDir)
                    //Log.d("externalFilesPath", file.absolutePath)
                    return FileProvider.getUriForFile(context, AUTHORITY_FILEPROVIDER, file)
                } catch (e: ActivityNotFoundException) {
                    Log.e("takePictureIntent", "Failed to open camera\n$e")
                    Toast.makeText(context, "Failed to open camera", Toast.LENGTH_LONG).show()
                } catch (e: IOException) {
                    Log.e("takePictureIntent", "Failed to access storage\n$e")
                    Toast.makeText(context, "Failed to access storage", Toast.LENGTH_LONG).show()
                }
            }
            return null
        }
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

        // TODO: make sure that the additional fields are filled out (filesize)

        return this
    }

    fun openFile(context: Context) {

        val uri = Uri.parse(this.uri) ?: return
        val noAppFoundToast = Toast.makeText(
            context,
            context.getText(R.string.attachment_error_no_app_found_to_open_file_or_uri),
            Toast.LENGTH_LONG
        )

        try {
            val intent = Intent(Intent.ACTION_VIEW)

            if (uri.toString().startsWith("content://", true)) {
                intent.setDataAndType(Uri.parse(this.uri), this.fmttype)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else if (uri.scheme?.startsWith("http") == true) {
                intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK
                intent.data = uri
                context.startActivity(intent)
            } else if(uri.scheme == null) {
                val uriWithHttps = Uri.parse("https://" + uri.toString())
                intent.flags += Intent.FLAG_ACTIVITY_NEW_TASK
                intent.data = uriWithHttps
                context.startActivity(intent)
            }
        } catch (e: IOException) {
            Log.i("fileprovider", "Failed to retrieve file\n$e")
            noAppFoundToast.show()
        } catch (e: ActivityNotFoundException) {
            Log.i("ActivityNotFound", "No activity found to open file\n$e")
            noAppFoundToast.show()
        } catch (e: Exception) {     // catches actually FileUriExposedException, but this is only available from SDK lvl 23
            Log.i("FileUriExposed", "File Uri cannot be accessed\n$e")
            Toast.makeText(context, "This file Uri cannot be accessed.", Toast.LENGTH_LONG).show()
        }
    }

    fun getFilesize(context: Context): Long? {
        return try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(Uri.parse(this.uri), "r")
            val filesize = fileDescriptor?.statSize
            fileDescriptor?.close()
            filesize
        } catch (e: Exception) {
            null
        }
    }

    fun getFilenameOrLink(): String? {

        return when {
            filename?.isNotEmpty() == true -> filename
            uri?.isNotEmpty() == true -> uri
            fmttype?.isNotEmpty() == true -> fmttype
            else -> null
        }
    }

/*
    fun getPreview(context: Context): Bitmap? {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && uri != null) {
            try {
                val thumbSize = Size(50, 50)
                val thumbUri = Uri.parse(uri)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return context.contentResolver?.loadThumbnail(thumbUri, thumbSize, null)

                }
            } catch (e: java.lang.NullPointerException) {
                Log.i("UriEmpty", "Uri was empty or could not be parsed.")
            } catch (e: FileNotFoundException) {
                Log.d("FileNotFound", "File with uri $uri not found.\n$e")
            } catch (e: ImageDecoder.DecodeException) {
                Log.i("ImageThumbnail", "Could not retrieve image thumbnail from file $uri")
            }
        }
        return null
    }
 */
}


