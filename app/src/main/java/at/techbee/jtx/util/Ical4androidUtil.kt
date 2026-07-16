/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import android.accounts.Account
import android.content.Context
import android.content.Entity
import android.util.Log
import at.bitfire.synctools.icalendar.CalendarUidSplitter
import at.bitfire.synctools.icalendar.ICalendarParser
import at.bitfire.synctools.mapping.jtx.JtxObjectBuilder
import at.bitfire.synctools.mapping.jtx.JtxObjectHandler
import at.bitfire.synctools.mapping.jtx.handler.AndroidAttachmentFetcher
import at.bitfire.synctools.storage.jtx.JtxCollection
import at.bitfire.synctools.storage.jtx.JtxCollectionProvider
import at.bitfire.synctools.storage.jtx.JtxObjectAndExceptions
import at.bitfire.synctools.storage.jtx.JtxRecurringCollection
import at.techbee.jtx.contract.JtxContract
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.ComponentList
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.PropertyList
import net.fortuna.ical4j.model.component.CalendarComponent
import net.fortuna.ical4j.model.property.ProdId
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion
import java.io.OutputStream
import java.io.Reader
import java.io.StringWriter
import java.io.Writer

object Ical4androidUtil {

    private const val TAG = "Ical4AndroidUtil"

    private val prodId = ProdId("+//IDN techbee.at//jtx Board")

    /**
     * @param [account] to look up
     * @param [context] to get the content provider client
     * @param [collectionId] to look up
     * @return a string with all the jtx objects of the collection as iCalendar (or null on error).
     */
    fun getICSFormatForCollectionFromProvider(account: Account, context: Context?, collectionId: Long): String? {
        val client = context?.contentResolver?.acquireContentProviderClient(JtxContract.AUTHORITY) ?: return null
        try {
            val collection = JtxCollectionProvider(account, client).getCollection(collectionId) ?: return null
            val handler = jtxObjectHandler(collection)

            // gather all main jtx objects of the collection (exceptions are exported with their main object)
            val mainIds = mutableListOf<Long>()
            collection.iterateJtxObjectRows(
                arrayOf(JtxContract.JtxICalObject.ID),
                "${JtxContract.JtxICalObject.RECURID} IS NULL",
                null
            ) { row -> row.getAsLong(JtxContract.JtxICalObject.ID)?.let { mainIds += it } }

            val components = mutableListOf<CalendarComponent>()
            mainIds.forEach { mainId -> components += componentsForObject(collection, handler, mainId) }

            val writer = StringWriter()
            writeComponents(components, writer)
            return writer.toString()
        } catch (e: Exception) {
            Log.w(TAG, e.stackTraceToString())
            return null
        } finally {
            client.close()
        }
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
        val client = context?.contentResolver?.acquireContentProviderClient(JtxContract.AUTHORITY) ?: return false
        try {
            val collection = JtxCollectionProvider(account, client).getCollection(collectionId) ?: return false
            val handler = jtxObjectHandler(collection)

            val components = mutableListOf<CalendarComponent>()
            iCalObjectIds.forEach { iCalObjectId ->
                components += componentsForObject(collection, handler, iCalObjectId)
            }

            writeComponents(components, os)
            return true
        } catch (e: Exception) {
            Log.w(TAG, e.stackTraceToString())
            return false
        } finally {
            client.close()
        }
    }

    /**
     * @param [account] to look up
     * @param [context]
     * @param [collectionId] where the parsed items should be inserted
     * @return A pair with <number of added entries, number of skipped entries>
     */
    fun insertFromReader(
        account: Account,
        context: Context?,
        collectionId: Long,
        reader: Reader
    ): Pair<Int, Int> {
        val client = context?.contentResolver?.acquireContentProviderClient(JtxContract.AUTHORITY) ?: return Pair(0, 0)
        try {
            val collection = JtxCollectionProvider(account, client).getCollection(collectionId) ?: return Pair(0, 0)
            val recurringCollection = JtxRecurringCollection(collection)

            val calendar = ICalendarParser().parse(reader)
            val splitter = CalendarUidSplitter<CalendarComponent>()
            val associatedComponents =
                splitter.associateByUid(calendar, Component.VJOURNAL).values +
                        splitter.associateByUid(calendar, Component.VTODO).values

            var numAdded = 0
            var numSkipped = 0

            associatedComponents.forEach { component ->
                // exceptions can't be imported without their main object
                if (component.main == null)
                    return@forEach

                val builtObject = JtxObjectBuilder(
                    collectionId = collection.id,
                    fileName = null,
                    eTag = null,
                    scheduleTag = null,
                    flags = 0
                ).build(component)

                val mainValues = builtObject.main.entity.entityValues
                val uid = mainValues.getAsString(JtxContract.JtxICalObject.UID)
                val newSequence = mainValues.getAsInteger(JtxContract.JtxICalObject.SEQUENCE) ?: 0

                // Check if UID already exists. If yes, check sequence and delete (to insert) or skip entry.
                if (uid != null) {
                    val foundRow = collection.findJtxObjectRow(
                        arrayOf(JtxContract.JtxICalObject.ID, JtxContract.JtxICalObject.SEQUENCE),
                        "${JtxContract.JtxICalObject.UID} = ? AND ${JtxContract.JtxICalObject.RECURID} IS NULL",
                        arrayOf(uid)
                    )
                    if (foundRow != null) {
                        val foundSequence = foundRow.getAsInteger(JtxContract.JtxICalObject.SEQUENCE) ?: 0
                        if (newSequence > foundSequence) {
                            foundRow.getAsLong(JtxContract.JtxICalObject.ID)?.let { foundId ->
                                recurringCollection.deleteJtxObjectAndExceptions(foundId)
                            }
                        } else {
                            numSkipped += 1
                            return@forEach
                        }
                    }
                }

                // mark as dirty so that the imported entry gets synchronized
                mainValues.put(JtxContract.JtxICalObject.DIRTY, true)

                recurringCollection.addJtxEntityAndExceptions(builtObject)
                numAdded += 1
            }

            return Pair(numAdded, numSkipped)
        } catch (e: Exception) {
            Log.w(TAG, e.stackTraceToString())
            return Pair(0, 0)
        } finally {
            client.close()
        }
    }


    /**
     * @return a [JtxObjectHandler] that maps jtx objects of the given [collection] to iCalendar components.
     */
    private fun jtxObjectHandler(collection: JtxCollection) = JtxObjectHandler(
        prodId = prodId,
        attachmentFetcher = AndroidAttachmentFetcher(collection.client, collection.account)
    )

    /**
     * Maps a main jtx object (and its recurrence exceptions) to iCalendar components.
     *
     * @param [mainId] ID of a jtx object; recurrence exception rows (RECURID set) are skipped as they
     * are exported together with their main object.
     * @return the mapped iCalendar components (main component followed by its exceptions), or an empty
     * list if the object can't be found or mapped.
     */
    private fun componentsForObject(
        collection: JtxCollection,
        handler: JtxObjectHandler,
        mainId: Long
    ): List<CalendarComponent> {
        val main = collection.getJtxObject(mainId) ?: return emptyList()

        // skip recurrence exceptions – they are exported with their main object
        if (main.entityValues.getAsString(JtxContract.JtxICalObject.RECURID) != null)
            return emptyList()

        val uid = main.entityValues.getAsString(JtxContract.JtxICalObject.UID)
        val exceptions = mutableListOf<Entity>()
        if (uid != null) {
            val exceptionIds = mutableListOf<Long>()
            collection.iterateJtxObjectRows(
                arrayOf(JtxContract.JtxICalObject.ID),
                "${JtxContract.JtxICalObject.UID} = ? AND ${JtxContract.JtxICalObject.RECURID} IS NOT NULL AND ${JtxContract.JtxICalObject.SEQUENCE} > 0",
                arrayOf(uid)
            ) { row -> row.getAsLong(JtxContract.JtxICalObject.ID)?.let { exceptionIds += it } }
            exceptionIds.forEach { exceptionId ->
                collection.getJtxObject(exceptionId)?.let { exceptions += it }
            }
        }

        return try {
            val result = handler.mapToCalendarComponents(JtxObjectAndExceptions(main, exceptions))
            listOfNotNull(result.associatedComponents.main) + result.associatedComponents.exceptions
        } catch (e: Exception) {
            Log.w(TAG, e.stackTraceToString())
            emptyList()
        }
    }

    private fun buildCalendar(components: List<CalendarComponent>): Calendar =
        Calendar(
            PropertyList(listOf<Property>(ImmutableVersion.VERSION_2_0, prodId)),
            ComponentList<CalendarComponent>(components)
        )

    private fun writeComponents(components: List<CalendarComponent>, os: OutputStream) =
        CalendarOutputter(false).output(buildCalendar(components), os)

    private fun writeComponents(components: List<CalendarComponent>, writer: Writer) =
        CalendarOutputter(false).output(buildCalendar(components), writer)
}
