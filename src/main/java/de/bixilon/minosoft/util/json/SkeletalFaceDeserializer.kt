/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.type.MapType
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalFace
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalFace.Companion.DEFAULT
import java.util.*


object SkeletalFaceDeserializer : StdDeserializer<Map<Directions, SkeletalFace>>(Map::class.java) {
    private val VALUES = mapOf(
        "all" to mapOf(Directions.DOWN to DEFAULT, Directions.UP to DEFAULT, Directions.NORTH to DEFAULT, Directions.SOUTH to DEFAULT, Directions.WEST to DEFAULT, Directions.EAST to DEFAULT),
        "x" to mapOf(Directions.WEST to DEFAULT, Directions.EAST to DEFAULT),
        "y" to mapOf(Directions.DOWN to DEFAULT, Directions.UP to DEFAULT),
        "z" to mapOf(Directions.NORTH to DEFAULT, Directions.SOUTH to DEFAULT),
        "side" to mapOf(Directions.NORTH to DEFAULT, Directions.SOUTH to DEFAULT, Directions.WEST to DEFAULT, Directions.EAST to DEFAULT),
        "-x" to mapOf(Directions.DOWN to DEFAULT, Directions.UP to DEFAULT, Directions.NORTH to DEFAULT, Directions.SOUTH to DEFAULT),
        "-y" to mapOf(Directions.NORTH to DEFAULT, Directions.SOUTH to DEFAULT, Directions.WEST to DEFAULT, Directions.EAST to DEFAULT),
        "-z" to mapOf(Directions.DOWN to DEFAULT, Directions.UP to DEFAULT, Directions.WEST to DEFAULT, Directions.EAST to DEFAULT),

        "-down" to mapOf(Directions.UP to DEFAULT, Directions.NORTH to DEFAULT, Directions.SOUTH to DEFAULT, Directions.WEST to DEFAULT, Directions.EAST to DEFAULT),
        "-up" to mapOf(Directions.DOWN to DEFAULT, Directions.NORTH to DEFAULT, Directions.SOUTH to DEFAULT, Directions.WEST to DEFAULT, Directions.EAST to DEFAULT),
        "-north" to mapOf(Directions.DOWN to DEFAULT, Directions.UP to DEFAULT, Directions.SOUTH to DEFAULT, Directions.WEST to DEFAULT, Directions.EAST to DEFAULT),
        "-south" to mapOf(Directions.DOWN to DEFAULT, Directions.UP to DEFAULT, Directions.NORTH to DEFAULT, Directions.WEST to DEFAULT, Directions.EAST to DEFAULT),
        "-west" to mapOf(Directions.DOWN to DEFAULT, Directions.UP to DEFAULT, Directions.NORTH to DEFAULT, Directions.SOUTH to DEFAULT, Directions.EAST to DEFAULT),
        "-east" to mapOf(Directions.DOWN to DEFAULT, Directions.UP to DEFAULT, Directions.NORTH to DEFAULT, Directions.SOUTH to DEFAULT, Directions.WEST to DEFAULT),
    )

    private val MAP: MapType = Jackson.MAPPER.typeFactory.constructMapType(EnumMap::class.java, Directions::class.java, SkeletalFace::class.java)


    override fun deserialize(parser: JsonParser, context: DeserializationContext?): Map<Directions, SkeletalFace> {
        val codec = parser.codec
        val next = parser.currentToken
        when (next) {
            JsonToken.START_OBJECT -> return codec.readValue(parser, MAP)
            JsonToken.START_ARRAY -> {
                val directions: Array<Directions> = codec.readValue(parser, Array<Directions>::class.java)
                val map: MutableMap<Directions, SkeletalFace> = mutableMapOf()
                for (direction in directions) {
                    map[direction] = DEFAULT
                }
                return map
            }

            JsonToken.VALUE_STRING -> {
                val raw = codec.readValue(parser, String::class.java)
                return VALUES[raw] ?: throw IllegalArgumentException("Invalid face: $raw")
            }

            else -> throw IllegalArgumentException("Can not read face: $parser")
        }
    }
}
