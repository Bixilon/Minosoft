/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

@file:OptIn(ExperimentalTime::class)

package de.bixilon.minosoft.util.json.time

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object InstantSerializer : SimpleModule() {

    init {
        addDeserializer(Instant::class.java, Deserializer)
        addSerializer(Instant::class.java, Serializer)
    }

    object Deserializer : StdDeserializer<Instant>(Instant::class.java) {

        override fun deserialize(parser: JsonParser, context: DeserializationContext?) = when (parser.currentToken) {
            JsonToken.VALUE_NUMBER_INT -> Instant.fromEpochSeconds(parser.valueAsLong, 0L)
            else -> TODO("Can not parse instant!")
        }
    }

    object Serializer : StdSerializer<Instant>(Instant::class.java) {

        override fun serialize(value: Instant?, generator: JsonGenerator, provider: SerializerProvider?) {
            generator.writeNumber(value?.epochSeconds ?: -1)
        }
    }
}
