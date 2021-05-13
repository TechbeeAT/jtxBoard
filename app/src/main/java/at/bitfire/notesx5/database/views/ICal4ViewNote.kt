/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.database.views

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*

const val VIEW_NAME_ICAL4VIEWNOTE = "ical4viewNote"

/**
 * This data class defines a view that is used by the IcalViewViewModel.
 * It provides only necessary columns that are needed to display notes (comments)
 * with their possible audio attachment.
 */
@DatabaseView(viewName = VIEW_NAME_ICAL4VIEWNOTE,
        value = "SELECT " +
                "$TABLE_NAME_ICALOBJECT.$COLUMN_ID, " +
                "$TABLE_NAME_ICALOBJECT.$COLUMN_MODULE, " +
                "$TABLE_NAME_ICALOBJECT.$COLUMN_COMPONENT, " +
                "$TABLE_NAME_ICALOBJECT.$COLUMN_SUMMARY, " +
                "$TABLE_NAME_ICALOBJECT.$COLUMN_DESCRIPTION, " +
                "$TABLE_NAME_ICALOBJECT.$COLUMN_CREATED, " +
                "$TABLE_NAME_ICALOBJECT.$COLUMN_LAST_MODIFIED, " +
                "$TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_ICALOBJECT_ID, " +     // = parentId
                "$TABLE_NAME_ATTACHMENT.$COLUMN_ATTACHMENT_VALUE, " +
                "$TABLE_NAME_ATTACHMENT.$COLUMN_ATTACHMENT_ENCODING, " +
                "$TABLE_NAME_ATTACHMENT.$COLUMN_ATTACHMENT_FMTTYPE, " +
                "$TABLE_NAME_ATTACHMENT.$COLUMN_ATTACHMENT_URI " +
                "FROM $TABLE_NAME_ICALOBJECT " +
                "INNER JOIN $TABLE_NAME_RELATEDTO ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_RELATEDTO.$COLUMN_RELATEDTO_LINKEDICALOBJECT_ID " +  // this join filters standalone notes already
                "LEFT JOIN $TABLE_NAME_ATTACHMENT ON $TABLE_NAME_ICALOBJECT.$COLUMN_ID = $TABLE_NAME_ATTACHMENT.$COLUMN_ATTACHMENT_ICALOBJECT_ID " +
                "WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_DELETED = 0 " +
                "AND $TABLE_NAME_ICALOBJECT.$COLUMN_MODULE = 'NOTE'")           // locally deleted entries are already excluded in the view!

//TODO: Filter only audio attachments here!

data class ICal4ViewNote (

        @ColumnInfo(index = true, name = COLUMN_ID)  var id: Long,
        @ColumnInfo(name = COLUMN_MODULE) var module: String,
        @ColumnInfo(name = COLUMN_COMPONENT) var component: String,
        @ColumnInfo(name = COLUMN_SUMMARY) var summary: String?,
        @ColumnInfo(name = COLUMN_DESCRIPTION) var description: String?,
        @ColumnInfo(name = COLUMN_CREATED) var created: Long,
        @ColumnInfo(name = COLUMN_LAST_MODIFIED) var lastModified: Long,
        @ColumnInfo(name = COLUMN_RELATEDTO_ICALOBJECT_ID) var parentId: Long,
        @ColumnInfo(name = COLUMN_ATTACHMENT_VALUE) var attachmentValue: String?,
        @ColumnInfo(name = COLUMN_ATTACHMENT_ENCODING) var attachmentEncoding: String?,
        @ColumnInfo(name = COLUMN_ATTACHMENT_FMTTYPE) var attachmentFmttype: String?,
        @ColumnInfo(name = COLUMN_ATTACHMENT_URI) var attachmentUri: String?
)

