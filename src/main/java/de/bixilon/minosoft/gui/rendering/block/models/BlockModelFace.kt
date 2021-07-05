/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.block.models

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.readUV
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.KUtil.unsafeCast
import glm_.func.cos
import glm_.func.sin
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import java.util.*

class BlockModelFace {
    val textureName: String?
    val cullFace: Directions?
    val tint: Boolean
    val positions: List<Vec2>


    constructor(textureName: String?, cullFace: Directions?, tint: Boolean, positions: List<Vec2>) {
        this.textureName = textureName
        this.cullFace = cullFace
        this.tint = tint
        this.positions = positions
    }

    constructor(data: Map<String, Any>, from: Vec3, to: Vec3, direction: Directions) {
        tint = data.containsKey("tintindex")
        textureName = data["texture"]!!.unsafeCast<String>().removePrefix("#")
        cullFace = data["cullface"]?.nullCast<String>()?.let {
            if (it == "bottom") {
                Directions.DOWN
            } else {
                Directions[it]
            }
        }
        val positions = calculateTexturePositions(data, from, to, direction)
        val rotation = data["rotation"]?.toInt()?.div(90) ?: 0
        Collections.rotate(positions, rotation)
        this.positions = positions.toList()
    }

    private fun calculateTexturePositions(data: Map<String, Any>?, from: Vec3, to: Vec3, direction: Directions): MutableList<Vec2> {
        val (textureTopLeft: Vec2, textureBottomRight: Vec2) = data?.get("uv")?.unsafeCast<List<Float>>()?.readUV() ?: getTexturePositionsFromRegion(AABB(from, to), direction)
        return mutableListOf(
            uvToFloat(Vec2(textureTopLeft.x, textureTopLeft.y)),
            uvToFloat(Vec2(textureTopLeft.x, textureBottomRight.y)),
            uvToFloat(Vec2(textureBottomRight.x, textureBottomRight.y)),
            uvToFloat(Vec2(textureBottomRight.x, textureTopLeft.y)),
        )
    }

    private fun getTexturePositionsFromRegion(aabb: AABB, direction: Directions): Pair<Vec2, Vec2> {
        // ToDo: Remove the duplicated code in Directions
        return when (direction) {
            Directions.UP, Directions.DOWN -> Pair(Vec2(aabb.min.x.toInt(), aabb.max.z.toInt()), Vec2(aabb.max.x.toInt(), aabb.min.z.toInt()))
            Directions.NORTH, Directions.SOUTH -> Pair(Vec2(aabb.min.x.toInt(), aabb.max.y.toInt()), Vec2(aabb.max.x.toInt(), aabb.min.y.toInt()))
            Directions.EAST, Directions.WEST -> Pair(Vec2(aabb.min.z.toInt(), aabb.max.y.toInt()), Vec2(aabb.max.z.toInt(), aabb.min.y.toInt()))
        }
    }

    constructor(other: BlockModelFace) {
        textureName = other.textureName
        cullFace = other.cullFace
        tint = other.tint
        this.positions = other.positions
    }

    constructor(vertexPositions: List<Vec3>, direction: Directions) {
        textureName = null
        cullFace = null
        tint = false
        val template = BlockModelElement.FACE_POSITION_MAP_TEMPLATE[direction.ordinal]
        val positions: MutableList<Vec2> = mutableListOf()
        for (templatePosition in template) {
            positions += calculateTexturePosition(vertexPositions[templatePosition], direction)
        }
        this.positions = positions.toList()
    }

    private fun calculateTexturePosition(position: Vec3, direction: Directions): Vec2 {
        return when (direction) {
            Directions.UP, Directions.DOWN -> Vec2(position.x, BlockModelElement.BLOCK_RESOLUTION - position.z)
            Directions.NORTH, Directions.SOUTH -> Vec2(position.x, position.y)
            Directions.EAST, Directions.WEST -> Vec2(position.z, BlockModelElement.BLOCK_RESOLUTION - position.y)
        }
    }

    fun getTexturePositionArray(direction: Directions): Array<Vec2?> {
        val template = textureTemplate[direction.ordinal]
        val ret: MutableList<Vec2> = mutableListOf()
        for (i in template.indices) {
            ret += positions[template[i]]
        }
        return ret.toTypedArray()
    }

    fun rotate(angle: Float): BlockModelFace {
        if (angle == 0.0f) {
            return this
        }
        val sin = angle.sin
        val cos = angle.cos
        val positions = this.positions.toMutableList()
        for ((i, position) in positions.withIndex()) {
            val offset = position - TEXTURE_MIDDLE
            positions[i] = VecUtil.getRotatedValues(offset.x, offset.y, sin, cos, false) + TEXTURE_MIDDLE
        }
        return BlockModelFace(textureName, cullFace, tint, positions.toList())
    }

    fun scale(scaleFactor: Double): BlockModelFace {
        val positions = positions.toMutableList()
        for ((i, position) in positions.withIndex()) {
            positions[i] = position * scaleFactor
        }
        return BlockModelFace(textureName, cullFace, tint, positions.toList())
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
