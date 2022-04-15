/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import android.accounts.Account
import android.content.ContentProviderClient
import android.content.ContentValues
import android.content.Context
import at.bitfire.ical4android.JtxCollection
import at.bitfire.ical4android.JtxCollectionFactory
import at.bitfire.ical4android.JtxICalObject
import at.bitfire.ical4android.JtxICalObjectFactory
import at.bitfire.ical4android.MiscUtils.CursorHelper.toValues
import at.techbee.jtx.contract.JtxContract
import at.techbee.jtx.contract.JtxContract.asSyncAdapter
import java.io.OutputStream
import java.io.Reader

object Ical4androidUtil {


    /**
     * @param [account] to look up
     * @param [context] to get the content provider client
     * @param [collectionId] to look up
     * @param [iCalObjectId] to look up
     * @return the ICalObject as ICS-format as a string or null if something went wrong.
     */
    fun getICSFormatFromProvider(account: Account, context: Context?, collectionId: Long, iCalObjectId: Long): String? {

        val collection = getCollection(account, context, collectionId) ?: return null

        collection.client.query(JtxContract.JtxICalObject.CONTENT_URI.asSyncAdapter(account),null, "${JtxContract.JtxICalObject.ID} = ?", arrayOf(iCalObjectId.toString()),null)?.use {
            if(!it.moveToFirst())
                return null
            val iCalObject = JtxICalObject(collection)
            iCalObject.populateFromContentValues(it.toValues())
            return iCalObject.getICalendarFormat().toString()
        }
        return null
    }


    /**
     * @param [account] to look up
     * @param [context] to get the content provider client
     * @param [collectionId] to look up
     * @return a string with all the JtxICalObjects as iCalendar.
     */
    fun getICSFormatForCollectionFromProvider(account: Account, context: Context?, collectionId: Long): String? {
        val collection = getCollection(account, context, collectionId) ?: return null
        return collection.getICSForCollection()
    }



    /**
     * @param [account] to look up
     * @param [context] to get the content provider client
     * @param [collectionId] to look up
     * @param [iCalObjectId] to look up
     * @param [os] the output stream where the ics should be written to
     * @return true if the ics was written successfully to the os, false otherwise
     */
    fun writeICSFormatFromProviderToOS(
        account: Account,
        context: Context?,
        collectionId: Long,
        iCalObjectId: Long,
        os: OutputStream
    ): Boolean {

        val collection = getCollection(account, context, collectionId) ?: return false

        val uri = JtxContract.JtxICalObject.CONTENT_URI
            .asSyncAdapter(account)
            .buildUpon()
            .appendPath(iCalObjectId.toString())
            .build()

        collection.client.query(uri,null, null, null, null)?.use {
            if(!it.moveToFirst())
                return false
            val iCalObject = JtxICalObject(collection)
            iCalObject.populateFromContentValues(it.toValues())
            iCalObject.write(os)
            return true
        }
        return false
    }

    /**
     * @param [account] to look up
     * @param [context]
     * @param [collectionId] to look up
     * @return A JtxCollection object or null (if not found or if the query returned more than 1 result)
     */
    private fun getCollection(account: Account,
                              context: Context?,
                              collectionId: Long): JtxCollection<JtxICalObject>? {

        val client =
            context?.contentResolver?.acquireContentProviderClient(JtxContract.AUTHORITY)
                ?: return null
        val collections = JtxCollection.find(account, client, context, LocalJtxCollection.Factory, "${JtxContract.JtxCollection.ID} = ?", arrayOf(collectionId.toString()))
        return if (collections.size != 1)
            null
        else
            collections.first()
    }


    /**
     * @param [account] to look up
     * @param [context]
     * @param [collectionId] where the parsed items should be inserted
     * @return A pair with <number of added entries, number of skipped entries>
     */
    fun insertFromReader(account: Account,
                         context: Context?,
                         collectionId: Long,
                         reader: Reader
    ): Pair<Int, Int> {

        val client = context?.contentResolver?.acquireContentProviderClient(JtxContract.AUTHORITY) ?: return Pair(0,0)
        val collections = JtxCollection.find(account, client, context, LocalJtxCollection.Factory, "${JtxContract.JtxCollection.ID} = ?", arrayOf(collectionId.toString()))
        if (collections.size != 1)
            return Pair(0,0)
        val collection = collections.first()

        var numAdded = 0
        var numSkipped = 0

        val jtxICalObjects = JtxICalObject.fromReader(reader, collection)
        jtxICalObjects.forEach {

            //Check if UID already exists. If yes, check sequence and delete (to insert) or skip entry
            val foundCV = collection.queryByUID(it.uid)
            if(foundCV != null) {
                val found = JtxICalObject(collection)
                found.populateFromContentValues(foundCV)
                if(it.sequence > found.sequence)
                    found.delete()
                else {
                    numSkipped += 1
                    return@forEach
                }
            }
            it.dirty = true
            it.add()
            numAdded += 1
        }

        collection.updateRelatedTo()
        return Pair(numAdded, numSkipped)
    }
}



class LocalJtxICalObject(collection: JtxCollection<*>) :
    JtxICalObject(collection) {

    object Factory : JtxICalObjectFactory<LocalJtxICalObject> {

        override fun fromProvider(
            collection: JtxCollection<JtxICalObject>,
            values: ContentValues
        ): LocalJtxICalObject {
            return LocalJtxICalObject(collection).apply {
                populateFromContentValues(values)
            }
        }
    }
}


class LocalJtxCollection(account: Account, client: ContentProviderClient, id: Long):
    JtxCollection<JtxICalObject>(account, client, LocalJtxICalObject.Factory, id){

    object Factory: JtxCollectionFactory<LocalJtxCollection> {
        override fun newInstance(account: Account, client: ContentProviderClient, id: Long) = LocalJtxCollection(account, client, id)
    }

}