/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.views

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import at.techbee.jtx.database.*

const val VIEW_NAME_COLLECTIONS_VIEW = "collectionsView"

/**
 * This data class defines a view that is used by the CollectionsViewModel.
 * It provides only necessary columns that are needed to display collections with the number of entries
 */
@DatabaseView(viewName = VIEW_NAME_COLLECTIONS_VIEW,
        value = "SELECT " +
                "$COLUMN_COLLECTION_ID, " +
                "$COLUMN_COLLECTION_URL, " +
                "$COLUMN_COLLECTION_DISPLAYNAME, " +
                "$COLUMN_COLLECTION_DESCRIPTION, " +
                "$COLUMN_COLLECTION_OWNER, " +
                "$COLUMN_COLLECTION_OWNER_DISPLAYNAME, " +
                "$COLUMN_COLLECTION_COLOR, " +
                "$COLUMN_COLLECTION_SUPPORTSVEVENT, " +
                "$COLUMN_COLLECTION_SUPPORTSVTODO, " +
                "$COLUMN_COLLECTION_SUPPORTSVJOURNAL, " +
                "$COLUMN_COLLECTION_ACCOUNT_NAME, " +
                "$COLUMN_COLLECTION_ACCOUNT_TYPE, " +
                "$COLUMN_COLLECTION_SYNC_VERSION, " +
                "$COLUMN_COLLECTION_READONLY, " +
                "$COLUMN_COLLECTION_ISVISIBLE, " +
                "(SELECT count(*) FROM $TABLE_NAME_ICALOBJECT WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ICALOBJECT_COLLECTIONID = $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID AND $COLUMN_MODULE = 'JOURNAL' AND $COLUMN_DELETED = 0) as numJournals, " +
                "(SELECT count(*) FROM $TABLE_NAME_ICALOBJECT WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ICALOBJECT_COLLECTIONID = $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID AND $COLUMN_MODULE = 'NOTE' AND $COLUMN_DELETED = 0) as numNotes, " +
                "(SELECT count(*) FROM $TABLE_NAME_ICALOBJECT WHERE $TABLE_NAME_ICALOBJECT.$COLUMN_ICALOBJECT_COLLECTIONID = $TABLE_NAME_COLLECTION.$COLUMN_COLLECTION_ID AND $COLUMN_MODULE = 'TODO' AND $COLUMN_DELETED = 0) as numTodos " +
                "FROM $TABLE_NAME_COLLECTION"
)

data class CollectionsView (
    @ColumnInfo(index = true, name = COLUMN_COLLECTION_ID)  var collectionId: Long = 0L,
    @ColumnInfo(name = COLUMN_COLLECTION_URL)               var url: String = ICalCollection.LOCAL_COLLECTION_URL,
    @ColumnInfo(name = COLUMN_COLLECTION_DISPLAYNAME)       var displayName: String? = null,
    @ColumnInfo(name = COLUMN_COLLECTION_DESCRIPTION)       var description: String? = null,
    @ColumnInfo(name = COLUMN_COLLECTION_OWNER)             var owner: String? = null,
    @ColumnInfo(name = COLUMN_COLLECTION_OWNER_DISPLAYNAME) var ownerDisplayName: String? = null,
    @ColumnInfo(name = COLUMN_COLLECTION_COLOR)             var color: Int? = null,
    @ColumnInfo(name = COLUMN_COLLECTION_SUPPORTSVEVENT)    var supportsVEVENT: Boolean = false,
    @ColumnInfo(name = COLUMN_COLLECTION_SUPPORTSVTODO)     var supportsVTODO: Boolean = false,
    @ColumnInfo(name = COLUMN_COLLECTION_SUPPORTSVJOURNAL)  var supportsVJOURNAL: Boolean = false,
    @ColumnInfo(name = COLUMN_COLLECTION_ACCOUNT_NAME)      var accountName: String? = null,
    @ColumnInfo(name = COLUMN_COLLECTION_ACCOUNT_TYPE)      var accountType: String? = ICalCollection.LOCAL_ACCOUNT_TYPE,
    @ColumnInfo(name = COLUMN_COLLECTION_SYNC_VERSION)      var syncversion: String? = null,
    @ColumnInfo(name = COLUMN_COLLECTION_READONLY)          var readonly: Boolean = false,
    @ColumnInfo(name = COLUMN_COLLECTION_ISVISIBLE)         var isVisible: Boolean = true,
                                                            var numJournals: Int? = null,
                                                            var numNotes: Int? = null,
                                                            var numTodos: Int? = null
) {

    /**
     * Maps the view object into the original ICalCollection object
     * @return [ICalCollection] with the data mapped from the view object
     */
    fun toICalCollection(): ICalCollection {
        val icc = ICalCollection()
        icc.collectionId = this.collectionId
        icc.accountName = this.accountName
        icc.accountType = this.accountType
        icc.displayName = this.displayName
        icc.color = this.color
        icc.description = this.description
        icc.owner = this.owner
        icc.ownerDisplayName = this.ownerDisplayName
        icc.readonly = this.readonly
        icc.isVisible = this.isVisible
        icc.supportsVEVENT = this.supportsVEVENT
        icc.supportsVJOURNAL = this.supportsVJOURNAL
        icc.supportsVTODO = this.supportsVTODO
        icc.syncversion = this.syncversion
        icc.url = this.url
        return icc
    }
}