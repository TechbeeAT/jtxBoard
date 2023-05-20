/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import android.content.Context
import android.content.Intent

interface GeofenceClientDefinition {

    fun addGeofence(lat: Double, long: Double, radius: Int, iCalObjectId: Long)

    fun removeGeofence(iCalObjectIds: List<Long>)

    fun processOnReceive(context: Context, intent: Intent)
}