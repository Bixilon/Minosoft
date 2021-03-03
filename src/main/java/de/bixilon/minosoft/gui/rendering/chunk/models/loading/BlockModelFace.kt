/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.models.loading

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import glm_.Java.Companion.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import java.util.*

class BlockModelFace {
    val textureName: String
    val cullFace: Directions?
    val tint: Boolean
    private val positions: MutableList<Vec2>

    constructor(data: JsonObject, from: Vec3, to: Vec3, direction: Directions) {
        tint = data.has("tintindex")
        textureName = data.get("texture").asString.removePrefix("#")
        var textureTopLeft = Vec2(0, 16)
        var textureBottomRight = Vec2(16, 0)
        when (direction) {
            Directions.EAST, Directions.WEST -> run {
                textureTopLeft = Vec2(from.z.toInt(), to.y.toInt())
                textureBottomRight = Vec2(to.z.toInt(), from.y.toInt())
            }
            Directions.UP, Directions.DOWN -> {
                textureTopLeft = Vec2(from.x.toInt(), to.z.toInt())
                textureBottomRight = Vec2(to.x.toInt(), from.z.toInt())
            }
            Directions.NORTH, Directions.SOUTH -> {
                textureTopLeft = Vec2(from.x.toInt(), to.y.toInt())
                textureBottomRight = Vec2(to.x.toInt(), from.y.toInt())
            }
        }
        data["uv"]?.asJsonArray?.let {
            textureTopLeft = Vec2(it[0].asFloat, it[3].asFloat)
            textureBottomRight = Vec2(it[2].asFloat, it[1].asFloat)
        }
        cullFace = data["cullface"]?.asString?.let {
            return@let if (it == "bottom") {
                Directions.DOWN
            } else {
                Directions.valueOf(it.toUpperCase())
            }
        }
        positions = mutableListOf(
            uvToFloat(Vec2(textureTopLeft.x, textureTopLeft.y)),
            uvToFloat(Vec2(textureTopLeft.x, textureBottomRight.y)),
            uvToFloat(Vec2(textureBottomRight.x, textureBottomRight.y)),
            uvToFloat(Vec2(textureBottomRight.x, textureTopLeft.y)),
        )
        val rotation = data["rotation"]?.asInt?.div(90) ?: 0
        Collections.rotate(positions, rotation)
    }

    constructor(parent: BlockModelFace) {
        textureName = parent.textureName
        cullFace = parent.cullFace
        tint = parent.tint
        positions = mutableListOf()
        for (position in parent.positions) {
            positions.add(Vec2(position))
        }
    }

    fun getTexturePositionArray(direction: Directions): Array<Vec2?> {
        val template = textureTemplate[direction.ordinal]
        val result = arrayOfNulls<Vec2>(template.size)
        for (i in template.indices) {
            result[i] = positions[template[i]]
        }
        return result
    }

    fun rotate(angle: Float) {
        if (angle == 0f) {
            return
        }
        val sin = glm.sin(angle)
        val cos = glm.cos(angle)
        for ((i, position) in positions.withIndex()) {
            val offset = position - TEXTURE_MIDDLE
            positions[i] = VecUtil.getRotatedValues(offset.x, offset.y, sin, cos) + TEXTURE_MIDDLE
        }
    }

    companion object {
        private fun uvToFloat(uv: Float): Float {
            return (uv) / BlockModelElement.BLOCK_RESOLUTION
        }

        fun uvToFloat(vec2: Vec2): Vec2 {
            return Vec2(uvToFloat(vec2.x), uvToFloat(BlockModelElement.BLOCK_RESOLUTION - vec2.y))
        }

        val textureTemplate = arrayOf(
            arrayOf(0, 1, 2, 3),
            arrayOf(0, 1, 2, 3),
            arrayOf(3, 2, 1, 0),
            arrayOf(0, 1, 2, 3),
            arrayOf(2, 3, 0, 1),
            arrayOf(1, 0, 3, 2),
        )

        val TEXTURE_MIDDLE = Vec2(0.5, 0.5)
    }
}
