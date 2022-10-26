/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.relations

import android.content.Context
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalObject.Companion.TZ_ALLDAY
import at.techbee.jtx.database.ICalObject.Companion.getLatLongString
import at.techbee.jtx.database.ICalObject.Companion.getMapLink
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.database.properties.Organizer
import at.techbee.jtx.util.DateTimeUtils
import kotlinx.parcelize.Parcelize


@Parcelize
data class ICalEntity(
    @Embedded
    var property: ICalObject = ICalObject(),


    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Comment::class)
    var comments: List<Comment>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Category::class)
    var categories: List<Category>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Attendee::class)
    var attendees: List<Attendee>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Organizer::class)
    var organizer: Organizer? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Relatedto::class)
    var relatedto: List<Relatedto>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Resource::class)
    var resources: List<Resource>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Attachment::class)
    var attachments: List<Attachment>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Alarm::class)
    var alarms: List<Alarm>? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Unknown::class)
    var unknown: List<Unknown>? = null,


    @Relation(
        parentColumn = COLUMN_ICALOBJECT_COLLECTIONID,
        entityColumn = COLUMN_COLLECTION_ID,
        entity = at.techbee.jtx.database.ICalCollection::class
    )
    var ICalCollection: ICalCollection? = null

    /*
    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Contact::class)
    var contact: Contact? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Resource::class)
    var resource: List<Resource>? = null

     */

) : Parcelable {


    /**
     * This function creates a copy of the ICalEntity in the selected module.
     * This also applies some transformations, e.g. when a task is copied to a note
     * @param [newModule] the new module of the copied entity
     * @return The [ICalEntity] in transformed to the new module (or as journal, if the moduleString was faulty)
     */
    fun getIcalEntityCopy(newModule: Module): ICalEntity {

        val newEntity = ICalEntity()
        newEntity.property = property.copy()
        newEntity.attendees = attendees?.toList()     // using toList() to create a copy of the list
        newEntity.resources = resources?.toList()
        newEntity.categories = categories?.toList()
        newEntity.alarms = alarms?.toList()
        newEntity.attachments = attachments?.toList()
        newEntity.relatedto = relatedto?.toList()
        newEntity.ICalCollection = ICalCollection?.copy()
        newEntity.comments = comments?.toList()
        newEntity.organizer = organizer?.copy()
        newEntity.unknown = unknown?.toList()

        return newEntity.apply {

            property.id = 0L
            property.module = newModule.name
            property.dtstamp = System.currentTimeMillis()
            property.created = System.currentTimeMillis()
            property.lastModified = System.currentTimeMillis()
            property.dtend = null
            property.dtendTimezone = null
            property.recurOriginalIcalObjectId = null
            property.isRecurLinkedInstance = false
            property.exdate = null
            property.rdate = null
            property.uid = ICalObject.generateNewUID()
            property.dirty = true

            property.flags = null
            property.scheduleTag = null
            property.eTag = null
            property.fileName = null


            if (newModule == Module.JOURNAL || newModule == Module.NOTE) {
                property.component = Component.VJOURNAL.name

                if (newModule == Module.JOURNAL && property.dtstart == null) {
                    property.dtstart = DateTimeUtils.getTodayAsLong()
                    property.dtstartTimezone = TZ_ALLDAY
                }
                if(newModule == Module.NOTE) {
                    property.dtstart = null
                    property.dtstartTimezone = null
                    property.rrule = null
                }
                property.due = null
                property.dueTimezone = null
                property.completed = null
                property.completedTimezone = null
                property.duration = null
                property.priority = null
                property.percent = null

                if(property.status != StatusJournal.FINAL.name || property.status != StatusJournal.DRAFT.name || property.status != StatusJournal.CANCELLED.name)
                    property.status = StatusJournal.FINAL.name

            } else if (newModule == Module.TODO) {
                property.component = Component.VTODO.name
                if(property.status != StatusTodo.COMPLETED.name || property.status != StatusTodo.`IN-PROCESS`.name || property.status != StatusTodo.`NEEDS-ACTION`.name || property.status != StatusTodo.CANCELLED.name)
                    property.status = StatusTodo.`NEEDS-ACTION`.name
            }

            // reset the ids of all list properties to make sure that they get inserted as new ones
            attachments?.forEach { it.attachmentId = 0L }
            attendees?.forEach { it.attendeeId = 0L }
            categories?.forEach { it.categoryId = 0L }
            comments?.forEach { it.commentId = 0L }
            organizer?.organizerId = 0L
            relatedto?.forEach { it.relatedtoId = 0L }
            resources?.forEach { it.resourceId = 0L }
            alarms?.forEach { it.alarmId = 0L }
            unknown?.forEach { it.unknownId = 0L }
        }
    }

    /**
     * @return a text that can be put in an email containing all information
     */
    fun getShareText(context: Context): String {

        var shareText = ""
        property.dtstart?.let {
            if(property.component == Component.VJOURNAL.name)
                shareText += DateTimeUtils.convertLongToFullDateTimeString(it, property.dtstartTimezone) + System.lineSeparator() + System.lineSeparator()
            else
                shareText += context.getString(R.string.view_started) + ": " + DateTimeUtils.convertLongToFullDateTimeString(it, property.dtstartTimezone) + System.lineSeparator() + System.lineSeparator()
        }
        property.due?.let { shareText += context.getString(R.string.view_due) + ": " + DateTimeUtils.convertLongToFullDateTimeString(it, property.dueTimezone) + System.lineSeparator() }
        property.completed?.let { shareText += context.getString(R.string.view_completed) + ": " + DateTimeUtils.convertLongToFullDateTimeString(it, property.completedTimezone) + System.lineSeparator() }
        property.getRecurInfo(context)?.let { shareText += it }
        property.summary?.let { shareText += it + System.lineSeparator() }
        property.description?.let { shareText += it + System.lineSeparator() + System.lineSeparator() }

        val categories: MutableList<String> = mutableListOf()
        this.categories?.forEach { categories.add(it.text) }
        if(categories.isNotEmpty())
            shareText += context.getString(R.string.categories) + ": " + categories.joinToString(separator=", ") + System.lineSeparator()

        if(property.contact?.isNotEmpty() == true)
            shareText += context.getString(R.string.contact) + ": " + property.contact + System.lineSeparator()

        if (property.location?.isNotEmpty() == true)
            shareText += context.getString(R.string.location) + ": " + (property.location?:"") + System.lineSeparator()

        if(property.geoLat != null && property.geoLong != null) {
            shareText += context.getString(R.string.map) + ": ${getLatLongString(property.geoLat, property.geoLong)} - "
            getMapLink(property.geoLat, property.geoLong, BuildConfig.FLAVOR)?.let { shareText += it }
            shareText += System.lineSeparator()
        }

        if(property.url?.isNotEmpty() == true)
            shareText += context.getString(R.string.url) + ": " + property.url + System.lineSeparator()

        val resources: MutableList<String> = mutableListOf()
        this.resources?.forEach { resource -> resource.text?.let { resources.add(it) } }
        if(resources.isNotEmpty())
            shareText += context.getString(R.string.resources) + ": " + resources.joinToString(separator=", ") + System.lineSeparator()

        val attachments: MutableList<String> = mutableListOf()
        this.attachments?.forEach { attachment ->
            if(attachment.uri?.startsWith("http") == true)
                attachments.add(attachment.uri!!)
        }
        if(attachments.isNotEmpty())
            shareText += context.getString(R.string.attachments) + ": " + System.lineSeparator() + attachments.joinToString(separator=System.lineSeparator()) + System.lineSeparator()

        return shareText.trim()
    }
}