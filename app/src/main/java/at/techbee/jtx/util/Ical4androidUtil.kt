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
import android.util.Log
import androidx.core.net.toUri
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.ComponentList
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.PropertyList
import net.fortuna.ical4j.model.component.CalendarComponent
import net.fortuna.ical4j.model.component.VJournal
import net.fortuna.ical4j.model.component.VToDo
import net.fortuna.ical4j.model.property.ProdId
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion
import java.io.OutputStream
import java.io.Reader
import java.io.StringWriter
import java.io.Writer

/**
 * Local iCalendar (.ics) import/export for jtx Board.
 *
 * The VJOURNAL/VTODO mapping lives in [ICalendarMapping]; this object only handles reading/writing
 * the jtx Board database (via [ICalDatabaseDao]) and assembling/parsing the iCalendar. It no longer
 * depends on any external sync library.
 *
 * The [account] parameters are kept for source compatibility with existing callers, but are unused:
 * a collection is uniquely identified by its [collectionId].
 */
object Ical4androidUtil {

    private const val TAG = "Ical4AndroidUtil"

    private val prodId = ProdId("+//IDN techbee.at//jtx Board")

    /**
     * @return a string with all jtx objects of the collection as iCalendar (or `null` on error).
     */
    fun getICSFormatForCollectionFromProvider(account: Account, context: Context?, collectionId: Long): String? {
        context ?: return null
        val dao = ICalDatabase.getInstance(context).iCalDatabaseDao()
        return try {
            val components = dao.getICalObjectIdsByCollectionSync(collectionId)
                .mapNotNull { loadComponent(dao, context, it) }
            StringWriter().also { writeComponents(components, it) }.toString()
        } catch (e: Exception) {
            Log.w(TAG, e.stackTraceToString())
            null
        }
    }

    /**
     * Writes the given jtx objects as a single iCalendar to [os].
     *
     * @return true if the ics was written successfully, false otherwise
     */
    fun writeICSFormatFromProviderToOS(
        account: Account,
        context: Context?,
        collectionId: Long,
        iCalObjectIds: List<Long>,
        os: OutputStream
    ): Boolean {
        context ?: return false
        val dao = ICalDatabase.getInstance(context).iCalDatabaseDao()
        return try {
            val components = iCalObjectIds.mapNotNull { loadComponent(dao, context, it) }
            writeComponents(components, os)
            true
        } catch (e: Exception) {
            Log.w(TAG, e.stackTraceToString())
            false
        }
    }

    /**
     * Parses the iCalendar from [reader] and inserts the contained jtx objects into the collection.
     *
     * @return A pair with <number of added entries, number of skipped entries>
     */
    fun insertFromReader(
        account: Account,
        context: Context?,
        collectionId: Long,
        reader: Reader
    ): Pair<Int, Int> {
        context ?: return Pair(0, 0)
        val dao = ICalDatabase.getInstance(context).iCalDatabaseDao()

        var numAdded = 0
        var numSkipped = 0
        try {
            val calendar = CalendarBuilder().build(reader)
            val components = calendar.getComponents<CalendarComponent>()
                .filter { it is VToDo || it is VJournal }

            components.forEach { component ->
                val parsed = parseICalComponent(component) ?: return@forEach
                val iCalObject = parsed.iCalObject.apply {
                    this.collectionId = collectionId
                    dirty = true          // imported entries need to be synchronized
                    deleted = false
                }

                // Check if UID already exists. If yes, check sequence and delete (to re-insert) or skip.
                val existing = dao.getICalObjectByUidSync(iCalObject.uid)
                if (existing != null) {
                    if ((iCalObject.sequence ?: 0L) > (existing.sequence ?: 0L)) {
                        dao.deleteICalObjectsbyId(existing.id)
                    } else {
                        numSkipped += 1
                        return@forEach
                    }
                }

                val newId = dao.insertICalObjectSync(iCalObject)
                parsed.categories.forEach { it.icalObjectId = newId; dao.insertCategorySync(it) }
                parsed.comments.forEach { it.icalObjectId = newId; dao.insertCommentSync(it) }
                parsed.resources.forEach { it.icalObjectId = newId; dao.insertResourceSync(it) }
                parsed.attendees.forEach { it.icalObjectId = newId; dao.insertAttendeeSync(it) }
                parsed.organizer?.let { it.icalObjectId = newId; dao.insertOrganizerSync(it) }
                parsed.relatedto.forEach {
                    it.icalObjectId = newId
                    it.linkedICalObjectId = it.text?.let { uid -> dao.getICalObjectByUidSync(uid)?.id }
                    dao.insertRelatedtoSync(it)
                }
                parsed.attachments.forEach { it.icalObjectId = newId; dao.insertAttachmentSync(it) }
                parsed.alarms.forEach { it.icalObjectId = newId; dao.insertAlarmSync(it) }
                parsed.unknowns.forEach { it.icalObjectId = newId; dao.insertUnknownSync(it) }
                numAdded += 1
            }
        } catch (e: Exception) {
            Log.w(TAG, e.stackTraceToString())
        }
        return Pair(numAdded, numSkipped)
    }


    /** Loads a jtx object with all its sub-entities and maps it to a [VJournal]/[VToDo]. */
    private fun loadComponent(dao: ICalDatabaseDao, context: Context, id: Long): CalendarComponent? {
        val iCalObject = dao.getICalObjectByIdSync(id) ?: return null
        return iCalObject.toICalComponent(
            categories = dao.getCategoriesSync(id),
            comments = dao.getCommentsSync(id),
            resources = dao.getResourcesSync(id),
            attendees = dao.getAttendeesSync(id),
            organizer = dao.getOrganizerSync(id),
            relatedto = dao.getRelatedtoSync(id),
            attachments = dao.getAttachmentsSync(id),
            alarms = dao.getAlarmsSync(id),
            unknowns = dao.getUnknownSync(id),
            readAttachmentBytes = { uri ->
                try {
                    context.contentResolver.openInputStream(uri.toUri())?.use { it.readBytes() }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not read attachment $uri: ${e.message}")
                    null
                }
            }
        )
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
