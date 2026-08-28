/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Serialization format adapted from bitfireAT/synctools (GPL-3.0-or-later).
 */

package at.techbee.jtx.util

import net.fortuna.ical4j.data.DefaultParameterFactorySupplier
import net.fortuna.ical4j.data.DefaultPropertyFactorySupplier
import net.fortuna.ical4j.model.Parameter
import net.fortuna.ical4j.model.ParameterBuilder
import net.fortuna.ical4j.model.ParameterFactory
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.PropertyBuilder
import net.fortuna.ical4j.model.PropertyFactory
import org.json.JSONArray
import org.json.JSONObject

/**
 * Helpers to (de)serialize an unknown iCalendar [Property] as a JSON string so that it can be
 * stored in the jtx Board content provider (see [at.techbee.jtx.database.properties.Unknown]).
 *
 * Format: `[propertyName, propertyValue, { param1Name: param1Value, ... }]`, with the third
 * array element (parameters) being optional.
 */
object UnknownProperty {

    private val propertyFactorySupplier: List<PropertyFactory<out Property>> = DefaultPropertyFactorySupplier().get()
    private val parameterFactorySupplier: List<ParameterFactory<out Parameter>> = DefaultParameterFactorySupplier().get()

    /**
     * Deserializes a JSON string to an ical4j [Property].
     *
     * @throws org.json.JSONException when the input value can't be parsed
     */
    fun fromJsonString(jsonString: String): Property {
        val json = JSONArray(jsonString)
        val name = json.getString(0)
        val value = json.getString(1)

        val builder = PropertyBuilder(propertyFactorySupplier)
            .name(name)
            .value(value)

        json.optJSONObject(2)?.let { jsonParams ->
            for (paramName in jsonParams.keys())
                builder.parameter(
                    ParameterBuilder(parameterFactorySupplier)
                        .name(paramName)
                        .value(jsonParams.getString(paramName))
                        .build()
                )
        }

        return builder.build()
    }

    /**
     * Serializes an ical4j [Property] to a JSON string.
     */
    fun toJsonString(prop: Property): String {
        val json = JSONArray()
        json.put(prop.name)
        json.put(prop.value)

        if (prop.parameterList.all.isNotEmpty()) {
            val jsonParams = JSONObject()
            for (param in prop.parameterList.all)
                jsonParams.put(param.name, param.value)
            json.put(jsonParams)
        }

        return json.toString()
    }
}
