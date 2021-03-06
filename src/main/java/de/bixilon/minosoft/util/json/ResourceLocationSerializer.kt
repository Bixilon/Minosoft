/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object ResourceLocationSerializer : SimpleModule() {

    init {
        addDeserializer(ResourceLocation::class.java, Deserializer)
        addSerializer(ResourceLocation::class.java, Serializer)
    }

    object Deserializer : StdDeserializer<ResourceLocation>(ResourceLocation::class.java) {

        override fun deserialize(parser: JsonParser, context: DeserializationContext?): ResourceLocation {
            return parser.valueAsString.toResourceLocation()
        }
    }

    object Serializer : StdSerializer<ResourceLocation>(ResourceLocation::class.java) {

        override fun serialize(value: ResourceLocation?, generator: JsonGenerator, provider: SerializerProvider?) {
            generator.writeString(value?.full)
        }
    }
}
