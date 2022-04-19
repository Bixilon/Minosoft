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

package de.bixilon.minosoft.util.json.vec

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import de.bixilon.kotlinglm.vec3.Vec3

object Vec3Serializer : SimpleModule() {

    init {
        addDeserializer(Vec3::class.java, Deserializer)
        addSerializer(Vec3::class.java, Serializer)
    }

    object Deserializer : StdDeserializer<Vec3>(Vec3::class.java) {

        override fun deserialize(parser: JsonParser, context: DeserializationContext?): Vec3 {
            when (val tree = parser.readValueAsTree<JsonNode>()) {
                is ArrayNode -> return Vec3(tree[0].asDouble(), tree[1].asDouble(), tree[2].asDouble())
                else -> TODO("Can not convert $tree to Vec3!")
            }
        }
    }

    object Serializer : StdSerializer<Vec3>(Vec3::class.java) {

        override fun serialize(value: Vec3?, generator: JsonGenerator, provider: SerializerProvider?) {
            if (value == null) {
                generator.writeNull()
                return
            }
            generator.writeArray(doubleArrayOf(value.x.toDouble(), value.y.toDouble(), value.z.toDouble()), 0, 3)
        }
    }
}
