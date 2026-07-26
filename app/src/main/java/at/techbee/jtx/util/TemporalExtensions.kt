/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import at.techbee.jtx.contract.JtxContract.JtxICalObject.TZ_ALLDAY
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.Temporal

/**
 * Extensions to convert ical4j [Temporal] date values to/from the representation used by the
 * jtx Board content provider (a UNIX timestamp in milliseconds plus a separate timezone string).
 *
 * These replace the equivalent helpers that were previously provided by the synctools library.
 */

/** Converts this [Temporal] to an [Instant]. All-day dates are anchored to UTC midnight,
 *  floating date-times to the system default time zone. */
fun Temporal.toInstant(): Instant = when (this) {
    is Instant -> this
    is ZonedDateTime -> toInstant()
    is OffsetDateTime -> toInstant()
    is LocalDateTime -> atZone(ZoneId.systemDefault()).toInstant()
    is LocalDate -> atStartOfDay(ZoneOffset.UTC).toInstant()
    else -> error("Unsupported Temporal type: ${this::class.qualifiedName}")
}

/** UNIX timestamp in milliseconds (as stored by the jtx Board provider). */
fun Temporal.toTimestamp(): Long = toInstant().toEpochMilli()

/** The [LocalDate] part of this [Temporal]. */
fun Temporal.toLocalDate(): LocalDate = when (this) {
    is LocalDate -> this
    is LocalDateTime -> toLocalDate()
    is OffsetDateTime -> toLocalDate()
    is ZonedDateTime -> toLocalDate()
    is Instant -> LocalDate.ofInstant(this, ZoneOffset.UTC)
    else -> error("Unsupported Temporal type: ${this::class.qualifiedName}")
}

/**
 * The timezone identifier to store for this [Temporal] in the jtx Board provider:
 * - [TZ_ALLDAY] for a plain date (all-day),
 * - `null` for a floating date-time,
 * - `"UTC"` for a UTC date-time,
 * - the zone id for a zoned date-time.
 */
fun Temporal.getTimeZoneId(): String? = when (this) {
    is ZonedDateTime -> zone.id
    is OffsetDateTime -> ZoneOffset.UTC.id
    is Instant -> ZoneOffset.UTC.id
    is LocalDateTime -> null
    is LocalDate -> TZ_ALLDAY
    else -> null
}
