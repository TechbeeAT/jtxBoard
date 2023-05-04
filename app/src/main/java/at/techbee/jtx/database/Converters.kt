/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

import androidx.room.TypeConverter
import at.techbee.jtx.database.locals.StoredListSettingData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter fun listSettingsParcelToString(value: StoredListSettingData) = Json.encodeToString(value)
    @TypeConverter fun stringToListSettingsParcel(value: String) = Json.decodeFromString<StoredListSettingData>(value)

    @TypeConverter fun stringToStatus(value: String) = Status.values().find { it.name == value } ?: Status.NO_STATUS
    @TypeConverter fun statusToString(value: Status?) = value?.name
}
