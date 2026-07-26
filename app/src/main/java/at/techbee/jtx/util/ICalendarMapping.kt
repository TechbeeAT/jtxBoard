/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * The VJOURNAL/VTODO <-> jtx object mapping is adapted from the ical4j-based mapping that was
 * previously provided by the (now discontinued) bitfireAT/synctools library, ported here so that
 * jtx Board no longer depends on that library for local iCalendar import/export.
 */

package at.techbee.jtx.util

import android.util.Base64
import android.util.Log
import at.techbee.jtx.contract.JtxContract
import at.techbee.jtx.contract.JtxContract.JtxICalObject.TZ_ALLDAY
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmAction
import at.techbee.jtx.database.properties.AlarmRelativeTo
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Attendee
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.database.properties.Organizer
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.properties.Resource
import at.techbee.jtx.database.properties.Unknown
import net.fortuna.ical4j.model.ComponentList
import net.fortuna.ical4j.model.DateList
import net.fortuna.ical4j.model.Parameter
import net.fortuna.ical4j.model.ParameterList
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.PropertyList
import net.fortuna.ical4j.model.TextList
import net.fortuna.ical4j.model.component.CalendarComponent
import net.fortuna.ical4j.model.component.VAlarm
import net.fortuna.ical4j.model.component.VJournal
import net.fortuna.ical4j.model.component.VToDo
import net.fortuna.ical4j.model.parameter.AltRep
import net.fortuna.ical4j.model.parameter.Cn
import net.fortuna.ical4j.model.parameter.CuType
import net.fortuna.ical4j.model.parameter.DelegatedFrom
import net.fortuna.ical4j.model.parameter.DelegatedTo
import net.fortuna.ical4j.model.parameter.Dir
import net.fortuna.ical4j.model.parameter.FmtType
import net.fortuna.ical4j.model.parameter.Language
import net.fortuna.ical4j.model.parameter.Member
import net.fortuna.ical4j.model.parameter.PartStat
import net.fortuna.ical4j.model.parameter.RelType
import net.fortuna.ical4j.model.parameter.Related
import net.fortuna.ical4j.model.parameter.Role
import net.fortuna.ical4j.model.parameter.Rsvp
import net.fortuna.ical4j.model.parameter.SentBy
import net.fortuna.ical4j.model.parameter.TzId
import net.fortuna.ical4j.model.parameter.XParameter
import net.fortuna.ical4j.model.property.Action
import net.fortuna.ical4j.model.property.Attach
import net.fortuna.ical4j.model.property.Categories
import net.fortuna.ical4j.model.property.Clazz
import net.fortuna.ical4j.model.property.Color
import net.fortuna.ical4j.model.property.Completed
import net.fortuna.ical4j.model.property.Contact
import net.fortuna.ical4j.model.property.Created
import net.fortuna.ical4j.model.property.Description
import net.fortuna.ical4j.model.property.DtEnd
import net.fortuna.ical4j.model.property.DtStamp
import net.fortuna.ical4j.model.property.DtStart
import net.fortuna.ical4j.model.property.Due
import net.fortuna.ical4j.model.property.Duration
import net.fortuna.ical4j.model.property.ExDate
import net.fortuna.ical4j.model.property.Geo
import net.fortuna.ical4j.model.property.LastModified
import net.fortuna.ical4j.model.property.Location
import net.fortuna.ical4j.model.property.PercentComplete
import net.fortuna.ical4j.model.property.Priority
import net.fortuna.ical4j.model.property.ProdId
import net.fortuna.ical4j.model.property.RDate
import net.fortuna.ical4j.model.property.RRule
import net.fortuna.ical4j.model.property.RecurrenceId
import net.fortuna.ical4j.model.property.Repeat
import net.fortuna.ical4j.model.property.Resources
import net.fortuna.ical4j.model.property.Sequence
import net.fortuna.ical4j.model.property.Status
import net.fortuna.ical4j.model.property.Summary
import net.fortuna.ical4j.model.property.Trigger
import net.fortuna.ical4j.model.property.Uid
import net.fortuna.ical4j.model.property.Url
import net.fortuna.ical4j.model.property.XProperty
import net.fortuna.ical4j.model.property.immutable.ImmutableAction
import net.fortuna.ical4j.model.property.immutable.ImmutablePriority
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.Temporal
import kotlin.jvm.optionals.getOrNull

private const val TAG = "ICalendarMapping"

// Extended (X-)properties/parameters, using the same names as DAVx5/synctools for interoperability.
private const val X_PROP_COMPLETEDTIMEZONE = "X-COMPLETEDTIMEZONE"
private const val X_PARAM_ATTACH_LABEL = "X-LABEL"      // used for filename in KOrganizer
private const val X_PARAM_FILENAME = "FILENAME"         // used for filename in GNOME Evolution
private const val X_PROP_XSTATUS = "X-STATUS"           // extended status (additionally to standard status)
private const val X_PROP_GEOFENCE_RADIUS = "X-GEOFENCE-RADIUS"

private val VTODO = JtxContract.JtxICalObject.Component.VTODO.name
private val VJOURNAL = JtxContract.JtxICalObject.Component.VJOURNAL.name

/** Adds a [Parameter] to this [Property] in place (ical4j's [Property.add] mutates the property). */
private operator fun Property.plusAssign(parameter: Parameter) {
    add<Property>(parameter)
}

/** Bundles a parsed [ICalObject] with its associated sub-entities (with `icalObjectId` not yet set). */
data class ParsedICalObject(
    val iCalObject: ICalObject,
    val categories: List<Category> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val resources: List<Resource> = emptyList(),
    val attendees: List<Attendee> = emptyList(),
    val organizer: Organizer? = null,
    val relatedto: List<Relatedto> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val alarms: List<Alarm> = emptyList(),
    val unknowns: List<Unknown> = emptyList()
)


// -------------------------------------------------------------------------------------------------
// Export: jtx object (+ sub-entities) -> VJOURNAL / VTODO
// -------------------------------------------------------------------------------------------------

/**
 * Builds a [VJournal] or [VToDo] (including [VAlarm] sub-components for tasks) from the given jtx
 * object and its sub-entities. Returns `null` for unsupported component types.
 *
 * @param readAttachmentBytes resolves the binary content of a `content://` attachment uri, or `null`
 */
fun ICalObject.toICalComponent(
    categories: List<Category> = emptyList(),
    comments: List<Comment> = emptyList(),
    resources: List<Resource> = emptyList(),
    attendees: List<Attendee> = emptyList(),
    organizer: Organizer? = null,
    relatedto: List<Relatedto> = emptyList(),
    attachments: List<Attachment> = emptyList(),
    alarms: List<Alarm> = emptyList(),
    unknowns: List<Unknown> = emptyList(),
    readAttachmentBytes: (String) -> ByteArray? = { null }
): CalendarComponent? {
    val isTodo = when (component) {
        VTODO -> true
        VJOURNAL -> false
        else -> return null
    }

    val props = mutableListOf<Property>()

    props += Uid(uid)
    props += Sequence((sequence ?: 0L).toInt())
    props += DtStamp(Instant.ofEpochMilli(dtstamp))
    created?.let { props += Created(Instant.ofEpochMilli(it)) }
    lastModified?.let { props += LastModified(Instant.ofEpochMilli(it)) }
    summary?.let { props += Summary(it) }
    description?.let { props += Description(it) }

    location?.let { loc ->
        val locationProp = Location(loc)
        locationAltrep?.let { locationProp += AltRep(it) }
        props += locationProp
    }
    if (geoLat != null && geoLong != null)
        props += Geo(geoLat!!.toBigDecimal(), geoLong!!.toBigDecimal())
    geofenceRadius?.let { props += XProperty(X_PROP_GEOFENCE_RADIUS, it.toString()) }

    color?.let { props += Color(null, Css3Color.nearestMatch(it).name) }
    url?.let {
        try {
            props += Url(URI(it))
        } catch (e: Exception) {
            Log.w(TAG, "Ignoring invalid URL: $it")
        }
    }
    contact?.let { props += Contact(it) }
    classification?.let { props += Clazz(it) }
    status?.let { props += Status(it) }
    xstatus?.let { props += XProperty(X_PROP_XSTATUS, it) }

    categories.mapNotNull { it.text.ifBlank { null } }.let {
        if (it.isNotEmpty()) props += Categories(TextList(it))
    }
    resources.mapNotNull { it.text?.ifBlank { null } }.let {
        if (it.isNotEmpty()) props += Resources(it)
    }

    comments.forEach { comment ->
        props += net.fortuna.ical4j.model.property.Comment(comment.text).apply {
            comment.altrep?.let { this += AltRep(it) }
            comment.language?.let { this += Language(it) }
            comment.other?.let { JtxContract.getXParametersFromJson(it).forEach { p -> this += p } }
        }
    }

    attendees.forEach { attendee ->
        if (attendee.caladdress.isBlank()) return@forEach
        val calAddr = try {
            URI(attendee.caladdress)
        } catch (e: Exception) {
            Log.w(TAG, "Ignoring invalid attendee URI: ${attendee.caladdress}")
            return@forEach
        }
        props += net.fortuna.ical4j.model.property.Attendee().apply {
            calAddress = calAddr
            attendee.cn?.let { this += Cn(it) }
            attendee.cutype?.let {
                this += when {
                    it.equals(CuType.INDIVIDUAL.value, true) -> CuType.INDIVIDUAL
                    it.equals(CuType.GROUP.value, true) -> CuType.GROUP
                    it.equals(CuType.ROOM.value, true) -> CuType.ROOM
                    it.equals(CuType.RESOURCE.value, true) -> CuType.RESOURCE
                    else -> CuType.UNKNOWN
                }
            }
            attendee.delegatedfrom?.let { this += DelegatedFrom(it) }
            attendee.delegatedto?.let { this += DelegatedTo(it) }
            attendee.dir?.let { this += Dir(it) }
            attendee.language?.let { this += Language(it) }
            attendee.member?.let { this += Member(it) }
            attendee.partstat?.let { this += PartStat(it) }
            attendee.role?.let { this += Role(it) }
            attendee.rsvp?.let { this += Rsvp(it) }
            attendee.sentby?.let { this += SentBy(it) }
            attendee.other?.let { JtxContract.getXParametersFromJson(it).forEach { p -> this += p } }
        }
    }

    organizer?.let { org ->
        props += net.fortuna.ical4j.model.property.Organizer().apply {
            if (org.caladdress.isNotBlank())
                try {
                    calAddress = URI(org.caladdress)
                } catch (e: Exception) {
                    Log.w(TAG, "Ignoring invalid organizer URI: ${org.caladdress}")
                }
            org.cn?.let { this += Cn(it) }
            org.dir?.let { this += Dir(it) }
            org.language?.let { this += Language(it) }
            org.sentby?.let { this += SentBy(it) }
            org.other?.let { JtxContract.getXParametersFromJson(it).forEach { p -> this += p } }
        }
    }

    attachments.forEach { attachment ->
        try {
            val bytes = when {
                attachment.binary?.isNotEmpty() == true -> Base64.decode(attachment.binary, Base64.DEFAULT)
                attachment.uri?.startsWith("content://") == true -> readAttachmentBytes(attachment.uri!!)
                else -> null
            }
            val att = when {
                bytes != null -> Attach(bytes)
                attachment.uri?.isNotEmpty() == true -> Attach(URI(attachment.uri))
                else -> return@forEach
            }
            attachment.fmttype?.let { att += FmtType(it) }
            attachment.filename?.let {
                att += XParameter(X_PARAM_ATTACH_LABEL, it)
                att += XParameter(X_PARAM_FILENAME, it)
            }
            props += att
        } catch (e: Exception) {
            Log.w(TAG, "Ignoring attachment ${attachment.uri}: ${e.message}")
        }
    }

    unknowns.forEach { unknown ->
        unknown.value?.let {
            try {
                props += UnknownProperty.fromJsonString(it)
            } catch (e: Exception) {
                Log.w(TAG, "Ignoring unparseable unknown property")
            }
        }
    }

    relatedto.forEach { rel ->
        val param: Parameter = when (rel.reltype) {
            RelType.CHILD.value -> RelType.CHILD
            RelType.SIBLING.value -> RelType.SIBLING
            RelType.PARENT.value -> RelType.PARENT
            else -> return@forEach
        }
        rel.text?.let { props += net.fortuna.ical4j.model.property.RelatedTo(ParameterList().add(param), it) }
    }

    dtstart?.let { props += DtStart(temporalFor(it, dtstartTimezone)) }
    rrule?.let { props += RRule<Temporal>(it) }
    recurid?.let {
        props += if (recuridTimezone == TZ_ALLDAY || recuridTimezone.isNullOrEmpty())
            RecurrenceId<Temporal>(it)
        else
            RecurrenceId<Temporal>(ParameterList(listOf(TzId(recuridTimezone))), it)
    }
    rdate?.let { props += RDate(dateListFor(JtxContract.getLongListFromString(it), dtstartTimezone)) }
    exdate?.let { props += ExDate(dateListFor(JtxContract.getLongListFromString(it), dtstartTimezone)) }
    duration?.let { props += Duration().apply { value = it } }

    if (isTodo) {
        completed?.let {
            props += Completed(Instant.ofEpochMilli(it))
            completedTimezone?.let { tz -> props += XProperty(X_PROP_COMPLETEDTIMEZONE, tz) }
        }
        percent?.let { props += PercentComplete(it) }
        if (priority != null && priority != ImmutablePriority.UNDEFINED.level)
            priority?.let { props += Priority(it) }
        due?.let { props += Due(temporalFor(it, dueTimezone ?: dtstartTimezone)) }
    }

    val propertyList = PropertyList(props)
    return if (isTodo) {
        // VALARMs are only valid inside VTODO (not VJOURNAL per RFC 5545)
        VToDo(propertyList, ComponentList(alarms.map { it.toVAlarm() }))
    } else {
        VJournal(propertyList)
    }
}

private fun Alarm.toVAlarm(): VAlarm {
    val alarmProps = mutableListOf<Property>()
    action?.let {
        when (it) {
            AlarmAction.DISPLAY.name -> alarmProps += ImmutableAction.DISPLAY
            AlarmAction.AUDIO.name -> alarmProps += ImmutableAction.AUDIO
            AlarmAction.EMAIL.name -> alarmProps += ImmutableAction.EMAIL
            else -> {}
        }
    }
    when {
        triggerRelativeDuration != null -> alarmProps += Trigger().apply {
            try {
                duration = java.time.Duration.parse(triggerRelativeDuration)
                if (triggerRelativeTo == AlarmRelativeTo.END.name) this += Related.END
                else this += Related.START
            } catch (e: Exception) {
                Log.w(TAG, "Could not parse alarm trigger duration: $triggerRelativeDuration")
            }
        }
        triggerTime != null -> alarmProps += Trigger().apply {
            date = if (triggerTimezone == ZoneOffset.UTC.id || triggerTimezone.isNullOrEmpty())
                Instant.ofEpochMilli(triggerTime!!)
            else
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(triggerTime!!), ZoneId.of(triggerTimezone)).toInstant()
        }
    }
    summary?.let { alarmProps += Summary(it) }
    repeat?.let { alarmProps += Repeat().apply { value = it } }
    duration?.let { dur ->
        alarmProps += Duration().apply {
            try {
                duration = java.time.Duration.parse(dur)
            } catch (e: Exception) {
                Log.w(TAG, "Could not parse alarm duration: $dur")
            }
        }
    }
    description?.let { alarmProps += Description(it) }
    attach?.let { alarmProps += Attach().apply { value = it } }
    other?.let { alarmProps.addAll(JtxContract.getXPropertyListFromJson(it).all) }

    return VAlarm().apply { propertyList = PropertyList(alarmProps) }
}

/** Builds an ical4j date value from a jtx timestamp + timezone string. */
private fun temporalFor(timestamp: Long, timezone: String?): Temporal {
    val instant = Instant.ofEpochMilli(timestamp)
    return when {
        timezone == TZ_ALLDAY -> instant.toLocalDate()
        timezone == ZoneOffset.UTC.id -> instant.atZone(ZoneOffset.UTC)
        timezone.isNullOrEmpty() -> instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        else -> instant.atZone(ZoneId.of(timezone))
    }
}

private fun dateListFor(timestamps: List<Long>, timezone: String?): DateList<Temporal> {
    val temporals: List<Temporal> = timestamps.map { ts ->
        val instant = Instant.ofEpochMilli(ts)
        when {
            timezone == TZ_ALLDAY -> LocalDate.ofInstant(instant, ZoneOffset.UTC)
            timezone == ZoneOffset.UTC.id -> ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
            timezone.isNullOrEmpty() -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            else -> ZonedDateTime.ofInstant(instant, ZoneId.of(timezone))
        }
    }
    return DateList(temporals)
}


// -------------------------------------------------------------------------------------------------
// Import: VJOURNAL / VTODO -> jtx object (+ sub-entities)
// -------------------------------------------------------------------------------------------------

/**
 * Parses a [VJournal] or [VToDo] (including its [VAlarm] sub-components) into an [ICalObject] and
 * its sub-entities. Returns `null` for unsupported component types.
 */
fun parseICalComponent(component: CalendarComponent): ParsedICalObject? {
    if (component !is VToDo && component !is VJournal)
        return null
    // Start from a clean object (avoid the UI-oriented defaults of ICalObject.createTask/createJournal).
    val iCalObject = ICalObject(
        component = if (component is VToDo) VTODO else VJOURNAL,
        module = if (component is VToDo) Module.TODO.name else Module.NOTE.name
    )
    iCalObject.sequence = 0

    val categories = mutableListOf<Category>()
    val comments = mutableListOf<Comment>()
    val resources = mutableListOf<Resource>()
    val attendees = mutableListOf<Attendee>()
    var organizer: Organizer? = null
    val relatedto = mutableListOf<Relatedto>()
    val attachments = mutableListOf<Attachment>()
    val unknowns = mutableListOf<Unknown>()

    for (prop in component.propertyList.all) {
        when (prop) {
            is Uid -> iCalObject.uid = prop.value
            is Sequence -> iCalObject.sequence = prop.sequenceNo.toLong()
            is Created -> iCalObject.created = prop.date.toTimestamp()
            is LastModified -> iCalObject.lastModified = prop.date.toTimestamp()
            is Summary -> iCalObject.summary = prop.value
            is Description -> iCalObject.description = prop.value
            is Location -> {
                iCalObject.location = prop.value
                iCalObject.locationAltrep = prop.getParameter<AltRep>(Parameter.ALTREP).getOrNull()?.value
            }
            is Geo -> {
                iCalObject.geoLat = prop.latitude.toDouble()
                iCalObject.geoLong = prop.longitude.toDouble()
            }
            is Color -> iCalObject.color = Css3Color.fromString(prop.value)?.argb
            is Url -> iCalObject.url = prop.value
            is Contact -> iCalObject.contact = prop.value
            is Priority -> iCalObject.priority = prop.level
            is Clazz -> iCalObject.classification = prop.value
            is Status -> iCalObject.status = prop.value
            is DtStart<*> -> {
                iCalObject.dtstart = prop.date.toTimestamp()
                iCalObject.dtstartTimezone = prop.date.getTimeZoneId()
            }
            is DtEnd<*> -> Log.w(TAG, "DTEND is not supported for VTODO/VJOURNAL, ignoring")
            is Completed -> if (iCalObject.component == VTODO) iCalObject.completed = prop.date.toTimestamp()
            is Due<*> -> if (iCalObject.component == VTODO) {
                iCalObject.due = prop.date.toTimestamp()
                iCalObject.dueTimezone = prop.date.getTimeZoneId()
            }
            is Duration -> iCalObject.duration = prop.value
            is PercentComplete -> if (iCalObject.component == VTODO) iCalObject.percent = prop.percentage
            is RRule<*> -> iCalObject.rrule = prop.value
            is RDate<*> -> iCalObject.rdate = mergeTimestamps(iCalObject.rdate, prop.dates.dates)
            is ExDate<*> -> iCalObject.exdate = mergeTimestamps(iCalObject.exdate, prop.dates.dates)
            is RecurrenceId<*> -> {
                iCalObject.recurid = prop.value
                iCalObject.recuridTimezone = prop.date.getTimeZoneId()
            }
            is Categories -> prop.categories.texts.forEach { categories += Category(text = it) }
            is Resources -> prop.resources.texts.forEach { resources += Resource(text = it) }
            is net.fortuna.ical4j.model.property.Comment -> comments += Comment().apply {
                text = prop.value
                language = prop.getParameter<Language>(Parameter.LANGUAGE).getOrNull()?.value
                altrep = prop.getParameter<AltRep>(Parameter.ALTREP).getOrNull()?.value
                prop.removeAll<Property>(Parameter.LANGUAGE, Parameter.ALTREP)
                other = JtxContract.getJsonStringFromXParameters(prop.parameterList)
            }
            is Attach -> {
                val attachment = Attachment()
                prop.uri?.let { attachment.uri = it.toString() }
                prop.binary?.let { attachment.binary = Base64.encodeToString(it, Base64.DEFAULT) }
                prop.getParameter<FmtType>(Parameter.FMTTYPE).getOrNull()?.let { attachment.fmttype = it.value }
                (prop.getParameter<XParameter>(X_PARAM_ATTACH_LABEL).getOrNull()
                    ?: prop.getParameter<XParameter>(X_PARAM_FILENAME).getOrNull())?.let { attachment.filename = it.value }
                prop.removeAll<Property>(Parameter.FMTTYPE, X_PARAM_ATTACH_LABEL, X_PARAM_FILENAME)
                attachment.other = JtxContract.getJsonStringFromXParameters(prop.parameterList)
                if (attachment.uri?.isNotEmpty() == true || attachment.binary?.isNotEmpty() == true)
                    attachments += attachment
            }
            is net.fortuna.ical4j.model.property.RelatedTo -> relatedto += Relatedto().apply {
                text = prop.value
                reltype = prop.getParameter<RelType>(Parameter.RELTYPE).getOrNull()?.value ?: Reltype.PARENT.name
                prop.removeAll<Property>(Parameter.RELTYPE)
                other = JtxContract.getJsonStringFromXParameters(prop.parameterList)
            }
            is net.fortuna.ical4j.model.property.Attendee -> attendees += Attendee().apply {
                caladdress = prop.calAddress?.toString() ?: ""
                cn = prop.getParameter<Cn>(Parameter.CN).getOrNull()?.value
                delegatedto = prop.getParameter<DelegatedTo>(Parameter.DELEGATED_TO).getOrNull()?.value
                delegatedfrom = prop.getParameter<DelegatedFrom>(Parameter.DELEGATED_FROM).getOrNull()?.value
                cutype = prop.getParameter<CuType>(Parameter.CUTYPE).getOrNull()?.value
                dir = prop.getParameter<Dir>(Parameter.DIR).getOrNull()?.value
                language = prop.getParameter<Language>(Parameter.LANGUAGE).getOrNull()?.value
                member = prop.getParameter<Member>(Parameter.MEMBER).getOrNull()?.value
                partstat = prop.getParameter<PartStat>(Parameter.PARTSTAT).getOrNull()?.value
                role = prop.getParameter<Role>(Parameter.ROLE).getOrNull()?.value
                rsvp = prop.getParameter<Rsvp>(Parameter.RSVP).getOrNull()?.value?.toBoolean()
                sentby = prop.getParameter<SentBy>(Parameter.SENT_BY).getOrNull()?.value
                prop.removeAll<Property>(
                    Parameter.CN, Parameter.DELEGATED_TO, Parameter.DELEGATED_FROM, Parameter.CUTYPE,
                    Parameter.DIR, Parameter.LANGUAGE, Parameter.MEMBER, Parameter.PARTSTAT,
                    Parameter.ROLE, Parameter.RSVP, Parameter.SENT_BY
                )
                other = JtxContract.getJsonStringFromXParameters(prop.parameterList)
            }
            is net.fortuna.ical4j.model.property.Organizer -> organizer = Organizer().apply {
                caladdress = prop.calAddress?.toString() ?: ""
                cn = prop.getParameter<Cn>(Parameter.CN).getOrNull()?.value
                dir = prop.getParameter<Dir>(Parameter.DIR).getOrNull()?.value
                language = prop.getParameter<Language>(Parameter.LANGUAGE).getOrNull()?.value
                sentby = prop.getParameter<SentBy>(Parameter.SENT_BY).getOrNull()?.value
                prop.removeAll<Property>(Parameter.CN, Parameter.DIR, Parameter.LANGUAGE, Parameter.SENT_BY)
                other = JtxContract.getJsonStringFromXParameters(prop.parameterList)
            }
            is ProdId, is DtStamp -> { /* not stored */ }
            else -> when (prop.name) {
                X_PROP_COMPLETEDTIMEZONE -> iCalObject.completedTimezone = prop.value
                X_PROP_XSTATUS -> iCalObject.xstatus = prop.value
                X_PROP_GEOFENCE_RADIUS -> iCalObject.geofenceRadius = prop.value.toIntOrNull()
                else -> unknowns += Unknown(value = UnknownProperty.toJsonString(prop))
            }
        }
    }

    // A VJOURNAL with a start date is a journal entry, without one it is a note.
    if (component is VJournal)
        iCalObject.module = if (iCalObject.dtstart != null) Module.JOURNAL.name else Module.NOTE.name

    // VALARMs are only valid inside VTODO (per RFC 5545)
    val alarms = when (component) {
        is VToDo -> component.componentList.all.filterIsInstance<VAlarm>().map { it.toAlarm() }
        else -> emptyList()
    }

    return ParsedICalObject(
        iCalObject = iCalObject,
        categories = categories,
        comments = comments,
        resources = resources,
        attendees = attendees,
        organizer = organizer,
        relatedto = relatedto,
        attachments = attachments,
        alarms = alarms,
        unknowns = unknowns
    )
}

private fun VAlarm.toAlarm(): Alarm = Alarm().apply {
    getProperty<Action>(Property.ACTION).getOrNull()?.let {
        action = when (it.value?.uppercase()) {
            AlarmAction.DISPLAY.name -> AlarmAction.DISPLAY.name
            AlarmAction.AUDIO.name -> AlarmAction.AUDIO.name
            AlarmAction.EMAIL.name -> AlarmAction.EMAIL.name
            else -> null
        }
    }
    getProperty<Trigger>(Property.TRIGGER).getOrNull()?.let { trigger ->
        val relativeDuration = trigger.duration
        if (relativeDuration != null) {
            triggerRelativeDuration = relativeDuration.toString()
            triggerRelativeTo = when (trigger.getParameter<Related>(Parameter.RELATED).getOrNull()) {
                Related.END -> AlarmRelativeTo.END.name
                else -> AlarmRelativeTo.START.name
            }
        } else {
            trigger.date?.let {
                triggerTime = it.toTimestamp()
                triggerTimezone = it.getTimeZoneId()
            }
        }
    }
    getProperty<Summary>(Property.SUMMARY).getOrNull()?.let { summary = it.value }
    getProperty<Description>(Property.DESCRIPTION).getOrNull()?.let { description = it.value }
    getProperty<Duration>(Property.DURATION).getOrNull()?.let { duration = it.value }
    getProperty<Repeat>(Property.REPEAT).getOrNull()?.let { repeat = it.value }
    getProperty<Attach>(Property.ATTACH).getOrNull()?.let { attach = it.value }
}

/** Appends the timestamps of an ical4j date list to an existing comma-separated jtx timestamp string. */
private fun mergeTimestamps(existing: String?, dates: List<Temporal>): String =
    buildList {
        if (!existing.isNullOrEmpty()) addAll(JtxContract.getLongListFromString(existing))
        dates.forEach { add(it.toTimestamp()) }
    }.joinToString(separator = ",")
