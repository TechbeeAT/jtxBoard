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
import android.util.Log
import at.bitfire.ical4android.*
import at.bitfire.ical4android.util.MiscUtils.toValues
import at.techbee.jtx.contract.JtxContract
import at.techbee.jtx.contract.JtxContract.asSyncAdapter
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VJournal
import net.fortuna.ical4j.model.component.VToDo
import net.fortuna.ical4j.model.property.Version
import java.io.OutputStream
import java.io.Reader

object Ical4androidUtil {


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
     * @param [iCalObjectIds] to look up
     * @param [os] the output stream where the ics should be written to
     * @return true if the ics was written successfully to the os, false otherwise
     */
    fun writeICSFormatFromProviderToOS(
        account: Account,
        context: Context?,
        collectionId: Long,
        iCalObjectIds: List<Long>,
        os: OutputStream
    ): Boolean {

        val collection = getCollection(account, context, collectionId) ?: return false

        val ical = Calendar()
        ical.properties += Version.VERSION_2_0
        ical.properties += ICalendar.prodId

        iCalObjectIds.forEach { iCalObjectId ->
            val uri = JtxContract.JtxICalObject.CONTENT_URI
                .asSyncAdapter(account)
                .buildUpon()
                .appendPath(iCalObjectId.toString())
                .build()

            collection.client.query(uri,null, null, null, null)?.use { cursor ->
                //Ical4Android.log.fine("writeICSFormatFromProviderToOS: found ${cursor.count} records in ${account.name}")

                while (cursor.moveToNext()) {
                    val jtxIcalObject = JtxICalObject(collection)
                    jtxIcalObject.populateFromContentValues(cursor.toValues())
                    val singleICS = jtxIcalObject.getICalendarFormat()
                    singleICS?.components?.forEach { component ->
                        if(component is VToDo || component is VJournal)
                            ical.components += component
                    }
                }
            }
        }

        Ical4Android.checkThreadContextClassLoader()
        try {
            CalendarOutputter(false).output(ical, os)
        } catch (e: Exception) {
            Log.w("Ical4AndroidUtil", e.stackTraceToString())
            return false
        }
        return true
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