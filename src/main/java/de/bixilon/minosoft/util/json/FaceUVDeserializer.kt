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
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import de.bixilon.minosoft.gui.rendering.models.block.element.face.FaceUV

object FaceUVDeserializer : SimpleModule() {

    init {
        addDeserializer(FaceUV::class.java, Deserializer)
    }

    object Deserializer : StdDeserializer<FaceUV>(FaceUV::class.java) {

        override fun deserialize(parser: JsonParser, context: DeserializationContext?): FaceUV {
            val array = parser.readValueAs(FloatArray::class.java)
            if (array.size != 4) throw IllegalArgumentException("Must have 4 components!")
            return FaceUV(array)
        }
    }
}
