/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

import android.accounts.Account
import android.content.ContentValues
import android.content.Context
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import at.techbee.jtx.R

import kotlinx.parcelize.Parcelize


/** The name of the the table for Collections.
 * ICalObjects MUST be linked to a collection! */
const val TABLE_NAME_COLLECTION = "collection"

/** The name of the ID column for collections.
 * This is the unique identifier of a Collection
 * Type: [Long]*/
const val COLUMN_COLLECTION_ID = BaseColumns._ID

/* The names of all the other columns  */
/**
 * Purpose:  This column/property defines the url of the collection.
 * Type: [String]
 */
const val COLUMN_COLLECTION_URL = "url"
/**
 * Purpose:  This column/property defines the display name of the collection.
 * Type: [String]
 */
const val COLUMN_COLLECTION_DISPLAYNAME = "displayname"
/**
 * Purpose:  This column/property defines a description of the collection.
 * Type: [String]
 */
const val COLUMN_COLLECTION_DESCRIPTION = "description"
/**
 * Purpose:  This column/property defines the URL of the owner of the collection.
 * Type: [String]
 */
const val COLUMN_COLLECTION_OWNER = "owner"
/**
 * Purpose:  This column/property defines the diplayname of the owner of the collection.
 * Type: [String]
 */
const val COLUMN_COLLECTION_OWNER_DISPLAYNAME = "ownerdisplayname"
/**
 * Purpose:  This column/property defines the color of the collection items.
 * This color can also be overwritten by the color in an ICalObject.
 * Type: [Int]
 */
const val COLUMN_COLLECTION_COLOR = "color"
/**
 * Purpose:  This column/property defines the if the collection supports VEVENTs.
 * Type: [Boolean]
 */
const val COLUMN_COLLECTION_SUPPORTSVEVENT = "supportsVEVENT"
/**
 * Purpose:  This column/property defines the if the collection supports VTODOs.
 * Type: [Boolean]
 */
const val COLUMN_COLLECTION_SUPPORTSVTODO = "supportsVTODO"
/**
 * Purpose:  This column/property defines the if the collection supports VJOURNALs.
 * Type: [Boolean]
 */
const val COLUMN_COLLECTION_SUPPORTSVJOURNAL = "supportsVJOURNAL"
/**
 * Purpose:  This column/property defines the if the account name under which the collection resides.
 * Type: [String]
 */
const val COLUMN_COLLECTION_ACCOUNT_NAME = "accountname"
/**
 * Purpose:  This column/property defines the if the account type under which the collection resides.
 * Type: [String]
 */
const val COLUMN_COLLECTION_ACCOUNT_TYPE = "accounttype"
/**
 * Purpose:  This column/property defines a field for the Sync Version for the Sync Adapter
 * Type: [String]
 */
const val COLUMN_COLLECTION_SYNC_VERSION = "syncversion"
/**
 * Purpose:  This column/property defines if a collection is marked as read-only by the Sync Adapter
 * Type: [Boolean]
 */
const val COLUMN_COLLECTION_READONLY = "readonly"
/**
 * Purpose:  This column/property defines the date/time of the last sync
 * Type: [Long]
 */
const val COLUMN_COLLECTION_LAST_SYNC = "lastsync"


@Parcelize
@Entity(tableName = TABLE_NAME_COLLECTION)
data class ICalCollection(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(index = true, name = COLUMN_COLLECTION_ID)   var collectionId: Long = 0L,

        @ColumnInfo(name = COLUMN_COLLECTION_URL)               var url: String = LOCAL_COLLECTION_URL,

        @ColumnInfo(name = COLUMN_COLLECTION_DISPLAYNAME)       var displayName: String? = null,
        @ColumnInfo(name = COLUMN_COLLECTION_DESCRIPTION)       var description: String? = null,
        @ColumnInfo(name = COLUMN_COLLECTION_OWNER)             var owner: String? = null,
        @ColumnInfo(name = COLUMN_COLLECTION_OWNER_DISPLAYNAME) var ownerDisplayName: String? = null,
        @ColumnInfo(name = COLUMN_COLLECTION_COLOR)
        @ColorInt
        var color: Int? = null,

        /** whether the collection supports VEVENT; in case of calendars: null means true */
        @ColumnInfo(name = COLUMN_COLLECTION_SUPPORTSVEVENT)          var supportsVEVENT: Boolean = false,

        /** whether the collection supports VTODO; in case of calendars: null means true */
        @ColumnInfo(name = COLUMN_COLLECTION_SUPPORTSVTODO)     var supportsVTODO: Boolean = false,

        /** whether the collection supports VJOURNAL; in case of calendars: null means true */
        @ColumnInfo(name = COLUMN_COLLECTION_SUPPORTSVJOURNAL)    var supportsVJOURNAL: Boolean = false,

        /** Account name */
        @ColumnInfo(name = COLUMN_COLLECTION_ACCOUNT_NAME)            var accountName: String? = null,

        /** Webcal subscription source URL */
        @ColumnInfo(name = COLUMN_COLLECTION_ACCOUNT_TYPE)            var accountType: String? = LOCAL_ACCOUNT_TYPE,

        /** Webcal subscription source URL */
        @ColumnInfo(name = COLUMN_COLLECTION_SYNC_VERSION)            var syncversion: String? = null,

        /** Webcal subscription source URL */
        @ColumnInfo(name = COLUMN_COLLECTION_READONLY)            var readonly: Boolean = false,

        /** Stores the date/time of the last sync for this collection */
        @ColumnInfo(name = COLUMN_COLLECTION_LAST_SYNC)         var lastSync: Long? = null

): Parcelable {
        companion object Factory {

                const val LOCAL_COLLECTION_URL = "https://localhost/"
                const val LOCAL_ACCOUNT_TYPE = "LOCAL"

                /**
                 * Create a new [ICalCollection] from the specified [ContentValues].
                 * @param values A [ICalCollection] that at least contain [.COLUMN_NAME].
                 * @return A newly created [ICalCollection] instance.
                 */
                fun fromContentValues(values: ContentValues?): ICalCollection? {

                        if (values == null)
                                return null

                        if(values.getAsString(COLUMN_COLLECTION_ACCOUNT_TYPE) == LOCAL_ACCOUNT_TYPE)
                                throw IllegalArgumentException("Forbidden account type: $LOCAL_ACCOUNT_TYPE")

                        return ICalCollection().applyContentValues(values)
                }

                /**
                 * Creates a new local collection with all Components activated
                 * @param [context] to resolve the default local account name from string resources
                 */
                fun createLocalCollection(context: Context) = ICalCollection().apply {
                        this.accountType = LOCAL_ACCOUNT_TYPE
                        this.accountName = context.getString(R.string.default_local_account_name)
                        this.supportsVEVENT = true
                        this.supportsVJOURNAL = true
                        this.supportsVTODO = true
                        this.readonly = false
                }
        }

        fun applyContentValues(values: ContentValues):ICalCollection {

                values.getAsString(COLUMN_COLLECTION_URL)?.let { url -> this.url = url }
                values.getAsString(COLUMN_COLLECTION_DISPLAYNAME)?.let { displayName -> this.displayName = displayName }
                values.getAsString(COLUMN_COLLECTION_DESCRIPTION)?.let { description -> this.description = description }
                values.getAsString(COLUMN_COLLECTION_OWNER)?.let { owner -> this.owner = owner }
                values.getAsString(COLUMN_COLLECTION_OWNER_DISPLAYNAME)?.let { ownerDisplayName -> this.ownerDisplayName = ownerDisplayName }
                values.getAsInteger(COLUMN_COLLECTION_COLOR)?.let { color -> this.color = color }
                values.getAsString(COLUMN_COLLECTION_SUPPORTSVEVENT)?.let { supportsVEVENT -> this.supportsVEVENT = supportsVEVENT == "1" || supportsVEVENT == "true"}
                values.getAsString(COLUMN_COLLECTION_SUPPORTSVTODO)?.let { supportsVTODO -> this.supportsVTODO = supportsVTODO == "1"  || supportsVTODO == "true"}
                values.getAsString(COLUMN_COLLECTION_SUPPORTSVJOURNAL)?.let { supportsVJOURNAL -> this.supportsVJOURNAL = supportsVJOURNAL == "1"  || supportsVJOURNAL == "true"}
                values.getAsString(COLUMN_COLLECTION_ACCOUNT_NAME)?.let { accountName -> this.accountName = accountName }
                values.getAsString(COLUMN_COLLECTION_ACCOUNT_TYPE)?.let { accountType -> this.accountType = accountType }
                values.getAsString(COLUMN_COLLECTION_SYNC_VERSION)?.let { syncversion -> this.syncversion = syncversion }
                values.getAsString(COLUMN_COLLECTION_READONLY)?.let { readonly -> this.readonly = readonly == "1"  || readonly == "true"}
                values.getAsLong(COLUMN_COLLECTION_LAST_SYNC)?.let { lastSync -> this.lastSync = lastSync }
                return this
        }

        /**
         * @return The account of the given collection as [Account]
         */
        fun getAccount(): Account = Account(accountName?:"", accountType?: LOCAL_ACCOUNT_TYPE)
}
