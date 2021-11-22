/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.relations

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.*
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.VJournal
import net.fortuna.ical4j.util.MapTimeZoneCache
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.StringReader

class ICalEntityAndroidTest {

    private lateinit var context: Context

    @Before
    fun setup() {

        // fix for crash when Timezones are needed for ical4j, see https://github.com/ical4j/ical4j/issues/195
        System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache::class.java.name)
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun getIcalFormat_Test() {

        val entity = ICalEntity(
            property = ICalObject(
                id = 20,
                module = Module.JOURNAL.name,
                component = Component.VJOURNAL.name,
                summary = "Almost all fields",
                description = "This is the description",
                dtstart = 1631404800000,
                dtstartTimezone = null,
                dtend = null,
                dtendTimezone = null,
                status = StatusJournal.FINAL.name,
                classification = Classification.PUBLIC.name,
                url = "www.techbee.at",
                contact = "John Doe",
                geoLat = null,
                geoLong = null,
                location = "At home",
                percent = null,
                priority = null,
                due = null,
                dueTimezone = null,
                completed = null,
                completedTimezone = null,
                duration = null,
                uid = "1631559989679-931e89bb-4f67-4f17-a36f-7a2ec23ccf63@at.techbee.jtx",
                created = 1631560099793,
                dtstamp = 1631564420619,
                lastModified = 1631564420619,
                sequence = 9,
                color = null,
                collectionId = 2,
                dirty = false,
                deleted = false,
                fileName = "1631559989679-931e89bb-4f67-4f17-a36f-7a2ec23ccf63@at.techbee.jtx.ics",
                eTag = "e3d572269fe04ca026ae0e1a1d95b485",
                scheduleTag = null,
                flags = 1
            ),
            comments = listOf(Comment(
                commentId = 3,
                icalObjectId = 20,
                text = "Comment number one",
                altrep = null,
                language = null,
                other = null
            )),
            categories = listOf(Category(
                categoryId = 2,
                icalObjectId = 20,
                text = "Cat1",
                language = null,
                other = null
            ), Category(
                categoryId = 3,
                icalObjectId = 20,
                text = "Dog2",
                language = null,
                other = null
            )),
            attendees = listOf(Attendee(
                attendeeId = 5,
                icalObjectId = 20,
                caladdress = "john@doe.com",
                cutype = Cutype.INDIVIDUAL.name,
                member = null,
                role = Role.`REQ-PARTICIPANT`.name,
                partstat = null,
                rsvp = null,
                delegatedto = null,
                delegatedfrom = null,
                sentby = null,
                cn = null,
                dir = null,
                language = null,
                other = null
            )),
            organizer = null,
            relatedto = listOf(Relatedto(
                relatedtoId = 8,
                icalObjectId = 20,
                linkedICalObjectId = 21,
                text = "1631560872973-d2d29ddb-76d5-4c21-9afd-2928a0e703b8@at.techbee.jtx",
                reltype = Reltype.CHILD.name,
                other = null
            )),
            resources = listOf(),
            attachments = listOf())
            //ICalCollection = ICalCollection(collectionId = 2,  url = https://baikal.techbee.at/html/dav.php/calendars/patrick/jtx-board/, displayName=JTX Board, description=null, owner=https://baikal.techbee.at/html/dav.php/principals/patrick/, color=null, supportsVEVENT=true, supportsVTODO=true, supportsVJOURNAL=true, accountName=baikal techbee, accountType=bitfire.at.davdroid, syncversion=null, readonly=false))


        // opens the file in \src\androidTest\assets\
        val inputStream = context.assets.open("journal-most-fields.ics")
        val icalStringReader = StringReader(inputStream.reader().readText())

        // for the Test the DTSTAMP property is removed from both files, the function would set DTSTAMP to the system time and the comparison would fail.
        val comparisonCal = CalendarBuilder().build(icalStringReader)
        comparisonCal.getComponent<VJournal>(net.fortuna.ical4j.model.Component.VJOURNAL).properties.apply {
            remove(this.getProperty(Property.DTSTAMP))
        }

        val entityCal = entity.getIcalFormat(context)
        entityCal.getComponent<VJournal>(net.fortuna.ical4j.model.Component.VJOURNAL).properties.apply {
            remove(this.getProperty(Property.DTSTAMP))
        }

        // compare properties one by one as the order might not be the same
        for(i in 0 until comparisonCal.components.size)  {
            comparisonCal.components[i].properties.forEach { comparisonProp ->
                if(comparisonProp.name == "DTSTAMP")
                    return@forEach
                val entityProp = comparisonCal.components[i].properties.getProperty<Property>(comparisonProp.name)
                assertEquals(comparisonProp, entityProp)
            }
        }
    }

    @Test
    fun writeIcalOutputStream() {

        val entity = ICalEntity(
            property = ICalObject(
                id = 20,
                module = Module.JOURNAL.name,
                component = Component.VJOURNAL.name,
                summary = "Almost all fields",
                description = "This is the description",
                dtstart = 1631404800000,
                dtstartTimezone = "Africa/Asmara",
                dtend = null,
                dtendTimezone = null,
                status = StatusJournal.FINAL.name,
                classification = Classification.PUBLIC.name,
                url = "www.techbee.at",
                contact = "John Doe",
                geoLat = null,
                geoLong = null,
                location = "At home",
                percent = null,
                priority = null,
                due = null,
                dueTimezone = null,
                completed = null,
                completedTimezone = null,
                duration = null,
                uid = "1631559989679-931e89bb-4f67-4f17-a36f-7a2ec23ccf63@at.techbee.jtx",
                created = 1631560099793,
                dtstamp = 1631564420619,
                lastModified = 1631564420619,
                sequence = 9,
                color = null,
                collectionId = 2,
                dirty = false,
                deleted = false,
                fileName = "1631559989679-931e89bb-4f67-4f17-a36f-7a2ec23ccf63@at.techbee.jtx.ics",
                eTag = "e3d572269fe04ca026ae0e1a1d95b485",
                scheduleTag = null,
                flags = 1
            ),
            comments = listOf(),
            categories = listOf(),
            attendees = listOf(),
            organizer = null,
            relatedto = listOf(),
            resources = listOf(),
            attachments = listOf())
        //ICalCollection = ICalCollection(collectionId = 2,  url = https://baikal.techbee.at/html/dav.php/calendars/patrick/jtx-board/, displayName=JTX Board, description=null, owner=https://baikal.techbee.at/html/dav.php/principals/patrick/, color=null, supportsVEVENT=true, supportsVTODO=true, supportsVJOURNAL=true, accountName=baikal techbee, accountType=bitfire.at.davdroid, syncversion=null, readonly=false))

        val os = ByteArrayOutputStream()
        entity.writeIcalOutputStream(context, os)

        val entityCal = entity.getIcalFormat(context)

        assertEquals(entityCal.toString(), os.toString())

    }
}