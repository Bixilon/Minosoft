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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import glm_.glm
import glm_.vec3.Vec3

open class BlockModelElement(data: JsonObject) {
    val faces: MutableMap<Directions, BlockModelFace> = mutableMapOf()
    var positions: Array<Vec3>

    init {
        var from = Vec3(0, 0, 0)
        var to = Vec3(BLOCK_RESOLUTION, BLOCK_RESOLUTION, BLOCK_RESOLUTION)
        data["from"]?.let {
            val array = it.asJsonArray
            from = Vec3(array[0].asFloat, array[1].asFloat, array[2].asFloat)
        }
        data["to"]?.let {
            val array = it.asJsonArray
            to = Vec3(array[0].asFloat, array[1].asFloat, array[2].asFloat)
        }
        positions = arrayOf(
            Vec3(from),
            Vec3(to.x, from.y, from.z),
            Vec3(from.x, from.y, to.z),
            Vec3(to.x, from.y, to.z),
            Vec3(from.x, to.y, from.z),
            Vec3(to.x, to.y, from.z),
            Vec3(from.x, to.y, to.z),
            Vec3(to),
        )
        data["rotation"]?.let {
            val rotation = it.asJsonObject
            val axis = Axes.valueOf(rotation["axis"].asString.toUpperCase())
            val angle = glm.radians(rotation["angle"].asDouble)
            rotatePositions(positions, axis, angle, jsonArrayToVec3(rotation["origin"].asJsonArray))
        }
        data["faces"]?.let {
            for ((directionName, json) in it.asJsonObject.entrySet()) {
                val direction = Directions.valueOf(directionName.toUpperCase())
                faces[direction] = BlockModelFace(json.asJsonObject, from, to, direction)
            }
        }
        for ((i, position) in positions.withIndex()) {
            positions[i] = transformPosition(position)
        }
    }

    companion object {
        fun jsonArrayToVec3(array: JsonArray): Vec3 {
            return Vec3(array[0].asFloat, array[1].asFloat, array[2].asFloat)
        }
        const val BLOCK_RESOLUTION = 16f

        val FACE_POSITION_MAP_TEMPLATE = arrayOf(
            intArrayOf(0, 2, 3, 1),
            intArrayOf(6, 4, 5, 7),
            intArrayOf(1, 5, 4, 0),
            intArrayOf(2, 6, 7, 3),
            intArrayOf(6, 2, 0, 4),
            intArrayOf(5, 1, 3, 7)
        )

        private val POSITION_1 = Vec3(-0.5f, -0.5f, -0.5f)           // Vec3(0, 0, 0)
        private val POSITION_2 = Vec3(+0.5f, -0.5f, -0.5f)           // Vec3(BLOCK_RESOLUTION, 0, 0)
        private val POSITION_3 = Vec3(-0.5f, -0.5f, +0.5f)           // Vec3(0, 0, BLOCK_RESOLUTION)
        private val POSITION_4 = Vec3(+0.5f, -0.5f, +0.5f)           // Vec3(BLOCK_RESOLUTION, 0, BLOCK_RESOLUTION)
        private val POSITION_5 = Vec3(-0.5f, +0.5f, -0.5f)           // Vec3(0, BLOCK_RESOLUTION, 0)
        private val POSITION_6 = Vec3(+0.5f, +0.5f, +0.5f)           // Vec3(BLOCK_RESOLUTION, BLOCK_RESOLUTION, 0)
        private val POSITION_7 = Vec3(-0.5f, +0.5f, +0.5f)           // Vec3(0, BLOCK_RESOLUTION, BLOCK_RESOLUTION)
        private val POSITION_8 = Vec3(+0.5f, +0.5f, +0.5f)           // Vec3(BLOCK_RESOLUTION, BLOCK_RESOLUTION, BLOCK_RESOLUTION)

        val fullTestPositions = mapOf(
            Directions.EAST to setOf(POSITION_1, POSITION_3, POSITION_5, POSITION_7),
            Directions.WEST to setOf(POSITION_2, POSITION_4, POSITION_6, POSITION_8),
            Directions.DOWN to setOf(POSITION_1, POSITION_2, POSITION_3, POSITION_4),
            Directions.UP to setOf(POSITION_5, POSITION_6, POSITION_7, POSITION_8),
            Directions.SOUTH to setOf(POSITION_1, POSITION_2, POSITION_5, POSITION_6),
            Directions.NORTH to setOf(POSITION_3, POSITION_4, POSITION_7, POSITION_8),
        )

        fun rotateVector(original: Vec3, angle: Double, axis: Axes): Vec3 {
            fun getRotatedValues(x: Float, y: Float, sin: Double, cos: Double): Pair<Float, Float> {
                return Pair((x * cos - y * sin).toFloat(), (x * sin + y * cos).toFloat())
            }
            return when (axis) {
                Axes.X -> {
                    val rotatedValues = getRotatedValues(original.y, original.z, glm.sin(angle), glm.cos(angle))
                    Vec3(original.x, rotatedValues.first, rotatedValues.second)
                }
                Axes.Y -> {
                    val rotatedValues = getRotatedValues(original.x, original.z, glm.sin(angle), glm.cos(angle))
                    Vec3(rotatedValues.first, original.y, rotatedValues.second)
                }
                Axes.Z -> {
                    val rotatedValues = getRotatedValues(original.x, original.y, glm.sin(angle), glm.cos(angle))
                    Vec3(rotatedValues.first, rotatedValues.second, original.z)
                }
            }
        }

        fun rotatePositions(positions: Array<Vec3>, axis: Axes, angle: Double, origin: Vec3) {
            // TODO: optimize for 90deg, 180deg, 270deg rotations
            if (angle == 0.0) {
                return
            }
            for ((i, position) in positions.withIndex()) {
                var transformedPosition = position - origin
                transformedPosition = rotateVector(transformedPosition, angle, axis)
                positions[i] = transformedPosition + origin
            }
        }

        fun transformPosition(position: Vec3): Vec3 {
            fun positionToFloat(uv: Float): Float {
                return (uv - 8f) / BLOCK_RESOLUTION
            }
            return Vec3(positionToFloat(position.x), positionToFloat(position.y), positionToFloat(position.z))
        }
    }
}
