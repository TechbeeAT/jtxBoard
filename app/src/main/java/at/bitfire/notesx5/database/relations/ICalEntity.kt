/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.database.relations

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.room.Embedded
import androidx.room.Relation
import at.bitfire.notesx5.BuildConfig
import at.bitfire.notesx5.NotesX5Contract
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.Component
import at.bitfire.notesx5.database.properties.*
import at.bitfire.notesx5.database.properties.Attendee
import at.bitfire.notesx5.database.properties.Comment
import at.bitfire.notesx5.database.properties.Organizer
import at.bitfire.notesx5.util.Css3Color
import kotlinx.parcelize.Parcelize
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.*
import net.fortuna.ical4j.model.component.VJournal
import net.fortuna.ical4j.model.component.VToDo
import net.fortuna.ical4j.model.parameter.FmtType
import net.fortuna.ical4j.model.parameter.RelType
import net.fortuna.ical4j.model.property.*
import net.fortuna.ical4j.validate.ValidationException
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URISyntaxException

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


    @Relation(
        parentColumn = COLUMN_ICALOBJECT_COLLECTIONID,
        entityColumn = COLUMN_COLLECTION_ID,
        entity = at.bitfire.notesx5.database.ICalCollection::class
    )
    var ICalCollection: ICalCollection? = null

    /*
    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Contact::class)
    var contact: Contact? = null,

    @Relation(parentColumn = COLUMN_ID, entityColumn = "icalObjectId", entity = Resource::class)
    var resource: List<Resource>? = null

     */

) : Parcelable {


    fun getIcalFormat(context: Context): Calendar {

        val ical = Calendar()
        ical.properties += Version.VERSION_2_0
        ical.properties += ProdId("+//IDN bitfire.at//ical4android")     // TODO to be adapted!

        if (this.property.component == NotesX5Contract.X5ICalObject.Component.VTODO.name) {
            val vTodo = VToDo(true /* generates DTSTAMP */)
            ical.components += vTodo
            val props = vTodo.properties
            addProperties(props, context)

        } else if (this.property.component == NotesX5Contract.X5ICalObject.Component.VJOURNAL.name) {
            val vJournal = VJournal(true /* generates DTSTAMP */)
            ical.components += vJournal
            val props = vJournal.properties
            addProperties(props, context)
        }

        return ical
    }


    private fun addProperties(props: PropertyList<Property>, context: Context) {

        this.property.apply {

            uid.let { props += Uid(it) }
            sequence.let { props += Sequence(it.toInt()) }

            created.let { props += Created(DateTime(it)) }
            lastModified.let { props += LastModified(DateTime(it)) }

            summary?.let { props += Summary(it) }
            description?.let { props += Description(it) }

            location?.let { props += Location(it) }
            if (geoLat != null && geoLong != null)
                props += Geo(geoLat!!.toBigDecimal(), geoLong!!.toBigDecimal())
            color?.let { props += Color(null, Css3Color.nearestMatch(it).name) }
            url?.let {
                try {
                    props += Url(URI(it))
                } catch (e: URISyntaxException) {
                    Log.w("ical4j processing", "Ignoring invalid task URL: $url", e)
                }
            }
            //organizer?.let { props += it }


            classification.let { props += Clazz(it) }
            status.let { props += Status(it) }


            val categoryTextList = TextList()
            categories?.forEach {
                categoryTextList.add(it.text)
            }
            if (!categoryTextList.isEmpty)
                props += Categories(categoryTextList)


            comments?.forEach {
                props += net.fortuna.ical4j.model.property.Comment(it.text)
            }

            attendees?.forEach {
                props += net.fortuna.ical4j.model.property.Attendee(it.caladdress)
                //todo: take care of other attributes for attendees
            }

            attachments?.forEach {
                if (it.uri?.isNotEmpty() == true)
                    context.contentResolver.openInputStream(Uri.parse(URI(it.uri).toString()))
                        .use { file ->
                            val att = Attach(IOUtils.toByteArray(file))
                            att.parameters.add(FmtType(it.fmttype))
                            props += att
                        }
            }

            relatedto?.forEach {
                val param: Parameter =
                    when (it.reltype) {
                        RelType.CHILD.value -> RelType.CHILD
                        RelType.SIBLING.value -> RelType.SIBLING
                        RelType.PARENT.value -> RelType.PARENT
                        else -> return@forEach
                    }
                val parameterList = ParameterList()
                parameterList.add(param)
                props += RelatedTo(parameterList, it.text)
            }


            /*
        props.addAll(unknownProperties)

        // remember used time zones
        val usedTimeZones = HashSet<TimeZone>()
        duration?.let(props::add)
        */

            /*
        rRule?.let { props += it }
        rDates.forEach { props += it }
        exDates.forEach { props += it }
*/


            dtstart?.let {
                when {
                    dtstartTimezone == "ALLDAY" -> props += DtStart(Date(it))
                    dtstartTimezone.isNullOrEmpty() -> props += DtStart(DateTime(it))
                    else -> {
                        val timezone = TimeZoneRegistryFactory.getInstance().createRegistry()
                            .getTimeZone(dtstartTimezone)
                        val withTimezone = DtStart(DateTime(it))
                        withTimezone.timeZone = timezone
                        props += withTimezone
                    }
                }
            }

            // Attributes only for VTODOs
            if (component == Component.VTODO.name) {
                dtend?.let {
                    when {
                        dtendTimezone == "ALLDAY" -> props += DtEnd(Date(it))
                        dtendTimezone.isNullOrEmpty() -> props += DtEnd(DateTime(it))
                        else -> {
                            val timezone = TimeZoneRegistryFactory.getInstance().createRegistry()
                                .getTimeZone(dtendTimezone)
                            val withTimezone = DtEnd(DateTime(it))
                            withTimezone.timeZone = timezone
                            props += withTimezone
                        }
                    }
                }
                completed?.let {
                    //Completed is defines as always DateTime! And is always UTC!?
                    props += Completed(DateTime(it))
                }
                percent?.let { props += PercentComplete(it) }


                if (priority != Priority.UNDEFINED.level)
                    priority?.let { props += Priority(priority!!) }

                due?.let {
                    when {
                        dueTimezone == "ALLDAY" -> props += Due(Date(it))
                        dueTimezone.isNullOrEmpty() -> props += Due(DateTime(it))
                        else -> {
                            val timezone = TimeZoneRegistryFactory.getInstance().createRegistry()
                                .getTimeZone(dueTimezone)
                            val withTimezone = Due(DateTime(it))
                            withTimezone.timeZone = timezone
                            props += withTimezone
                        }
                    }
                }
            }

            /*
        if (alarms.isNotEmpty())
            vTodo.alarms.addAll(alarms)

        // determine earliest referenced date
        val earliest = arrayOf(
            dtStart?.date,
            due?.date,
            completedAt?.date
        ).filterNotNull().min()
        // add VTIMEZONE components
        for (tz in usedTimeZones)
            ical.components += ICalendar.minifyVTimeZone(tz.vTimeZone, earliest)
 */
        }

    }


    fun writeIcalOutputStream(ical: Calendar, os: ByteArrayOutputStream) {


        // Corresponds to   ICalendar.softValidate(ical)   in ical4android
        try {
            ical.validate(true)
        } catch (e: ValidationException) {
            if (BuildConfig.DEBUG)
            // debug build, re-throw ValidationException
                throw e
            else
                Log.w("ical4j processing", "iCalendar validation failed - This is only a warning!", e)
        }

        CalendarOutputter(false).output(ical, os)

    }

}