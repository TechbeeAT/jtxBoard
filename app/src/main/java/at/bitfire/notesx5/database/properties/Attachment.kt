/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.database.properties

import android.content.ContentValues
import android.content.Context
import android.os.Parcelable
import android.provider.BaseColumns
import android.util.Log
import androidx.annotation.Nullable
import androidx.room.*
import at.bitfire.notesx5.database.COLUMN_ID
import at.bitfire.notesx5.database.ICalObject
import kotlinx.parcelize.Parcelize
import java.io.File


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
 * Purpose:  To specify the encoding of the attachment.
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.1]
 * Type: [String]
 */
const val COLUMN_ATTACHMENT_ENCODING = "encoding"

/**
 * Purpose:  To specify the value of the attachment (binary).
 * see [https://tools.ietf.org/html/rfc5545#section-3.8.1.1]
 * Type: [String]
 */
const val COLUMN_ATTACHMENT_VALUE = "value"

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
        @ColumnInfo(name = COLUMN_ATTACHMENT_ENCODING)                 var encoding: String? = ENCODING_BASE64,
        @ColumnInfo(name = COLUMN_ATTACHMENT_VALUE)               var value: String? = null,
        @ColumnInfo(name = COLUMN_ATTACHMENT_FMTTYPE)               var fmttype: String? = null,
        @ColumnInfo(name = COLUMN_ATTACHMENT_OTHER)                      var other: String? = null,
        @ColumnInfo(name = COLUMN_ATTACHMENT_FILENAME)                      var filename: String? = null,
        @ColumnInfo(name = COLUMN_ATTACHMENT_EXTENSION)                      var extension: String? = null,
        @ColumnInfo(name = COLUMN_ATTACHMENT_FILESIZE)                      var filesize: Long? = null

): Parcelable


{
    companion object Factory {

        const val ENCODING_BASE64 = "BASE64"
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
        fun getAttachmentDirectory(context: Context): String? {

            val filePath = "${context.filesDir}"
            val file = File(filePath)

            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.e("Attachment", "Failed creating attachment directory")
                    return null
                }
            }

            return filePath
        }
    }

    fun applyContentValues(values: ContentValues): Attachment {

        values.getAsLong(COLUMN_ATTACHMENT_ICALOBJECT_ID)?.let { icalObjectId -> this.icalObjectId = icalObjectId }
        values.getAsString(COLUMN_ATTACHMENT_URI)?.let { uri -> this.uri = uri }
        values.getAsString(COLUMN_ATTACHMENT_ENCODING)?.let { encoding -> this.encoding = encoding }
        values.getAsString(COLUMN_ATTACHMENT_VALUE)?.let { value -> this.value = value }
        values.getAsString(COLUMN_ATTACHMENT_FMTTYPE)?.let { fmttype -> this.fmttype = fmttype }
        values.getAsString(COLUMN_ATTACHMENT_OTHER)?.let { other -> this.other = other }
        values.getAsString(COLUMN_ATTACHMENT_FILENAME)?.let { filename -> this.filename = filename }
        values.getAsString(COLUMN_ATTACHMENT_EXTENSION)?.let { extension -> this.extension = extension }
        values.getAsLong(COLUMN_ATTACHMENT_FILESIZE)?.let { filesize -> this.filesize = filesize }


        // TODO: make sure that the additional fields are filled out (filename, filesize and extension)

        return this
    }


    fun getICalString(): String {

        var content = "ATTACH"
        if (fmttype?.isNotEmpty() == true)
            content += ";FMTTYPE=\"$fmttype\""
        if (encoding?.isNotEmpty() == true)
            content += ";ENCODING=$encoding"
        if (value?.isNotEmpty() == true)
            content += ";VALUE=BINARY:$value"
        if (uri?.isNotEmpty() == true)
            uri += ":$uri"
        content += "\r\n"

        return content
    }

}


