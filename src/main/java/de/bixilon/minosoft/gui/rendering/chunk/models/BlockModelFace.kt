/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.models

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import glm_.vec2.Vec2

class BlockModelFace(data: JsonObject) {
    val textureName: String = data.get("texture").asString.removePrefix("#")
    val cullFace: Directions?

    val positions: Array<Vec2>

    init {
        var textureStart = Vec2(0, 0)
        var textureEnd = Vec2(16, 16)
        data["uv"]?.asJsonArray?.let {
            textureStart = Vec2(it[0].asFloat, it[1].asFloat)
            textureEnd = Vec2(it[2].asFloat, it[3].asFloat)
        }
        positions = arrayOf(
            uvToFloat(Vec2(textureStart.x, textureStart.y)),
            uvToFloat(Vec2(textureStart.x, textureEnd.y)),
            uvToFloat(Vec2(textureEnd.x, textureEnd.y)),
            uvToFloat(Vec2(textureEnd.x, textureStart.y)),
        )

        cullFace = data["cullface"]?.asString?.let {
            return@let if (it == "bottom") {
                Directions.DOWN
            } else {
                Directions.valueOf(it.toUpperCase())
            }
        }
    }


    companion object {
        private fun uvToFloat(uv: Float): Float {
            return (uv) / 16f
        }

        fun uvToFloat(vec2: Vec2): Vec2 {
            return Vec2(uvToFloat(vec2.x), uvToFloat(vec2.y))
        }
    }
}
