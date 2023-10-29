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
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.skeletal.model.elements.SkeletalRotation

object SkeletalRotationDeserializer : StdDeserializer<SkeletalRotation>(SkeletalRotation::class.java) {

    override fun deserialize(parser: JsonParser, context: DeserializationContext?): SkeletalRotation {
        return when (parser.currentToken) {
            JsonToken.START_OBJECT -> parser.readValueAs(SkeletalRotation::class.java)
            JsonToken.START_ARRAY -> {
                val rotation = parser.readValueAs(FloatArray::class.java)
                if (rotation.size != 3) throw IllegalArgumentException("Invalid count of components: ${rotation.size}")
                SkeletalRotation(Vec3(0, rotation))
            }

            else -> throw IllegalArgumentException("Can not skeletal rotation: $parser")
        }
    }
}
