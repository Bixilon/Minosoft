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

package de.bixilon.minosoft.util.json.time

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.bixilon.kutil.unit.UnitFormatter.format
import de.bixilon.minosoft.util.KUtil.toDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object DurationSerializer : SimpleModule() {

    init {
        addDeserializer(Duration::class.java, Deserializer)
        addSerializer(Duration::class.java, Serializer)
    }

    object Deserializer : StdDeserializer<Duration>(Duration::class.java) {

        override fun deserialize(parser: JsonParser, context: DeserializationContext?) = when (parser.currentToken) {
            JsonToken.VALUE_NUMBER_FLOAT -> parser.valueAsDouble.seconds
            JsonToken.VALUE_STRING -> parser.valueAsString.toDuration()

            else -> TODO("Can not parse duration!")
        }
    }

    object Serializer : StdSerializer<Duration>(Duration::class.java) {

        override fun serialize(value: Duration?, generator: JsonGenerator, provider: SerializerProvider?) {
            if (value == null) {
                generator.writeNumber(0)
            } else {
                generator.writeString(value.format())
            }
        }
    }
}
