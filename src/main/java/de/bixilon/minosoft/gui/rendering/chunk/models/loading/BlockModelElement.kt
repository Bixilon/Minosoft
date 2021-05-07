/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.models.loading

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.gui.rendering.util.VecUtil.rotate
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import glm_.func.rad
import glm_.vec3.Vec3
import java.util.*

open class BlockModelElement(data: JsonObject) {
    val faces: MutableMap<Directions, BlockModelFace> = mutableMapOf()
    var transformedPositions: Array<Vec3>
    val from: Vec3 = data["from"]?.asJsonArray?.toVec3() ?: Vec3()
    val to: Vec3 = data["to"]?.asJsonArray?.toVec3() ?: Vec3(BLOCK_RESOLUTION)

    init {
        transformedPositions = arrayOf(
            Vec3(from),
            Vec3(to.x, from.y, from.z),
            Vec3(from.x, from.y, to.z),
            Vec3(to.x, from.y, to.z),
            Vec3(from.x, to.y, from.z),
            Vec3(to.x, to.y, from.z),
            Vec3(from.x, to.y, to.z),
            Vec3(to),
        )

        data["rotation"]?.asJsonObject?.let {
            val axis = Axes.valueOf(it["axis"].asString.uppercase(Locale.getDefault()))
            val angle = it["angle"].asFloat.rad
            val rescale = it["rescale"]?.asBoolean ?: false
            rotatePositions(transformedPositions, axis, angle, it["origin"].asJsonArray.toVec3(), rescale)
        }

        data["faces"]?.asJsonObject?.let {
            for ((directionName, json) in it.entrySet()) {
                val direction = Directions.valueOf(directionName.uppercase(Locale.getDefault()))
                faces[direction] = BlockModelFace(json.asJsonObject, from, to, direction)
            }
        }

        // transformed positions
        for ((index, position) in transformedPositions.withIndex()) {
            transformedPositions[index] = transformPosition(position)
        }
    }

    companion object {
        const val BLOCK_RESOLUTION = 16
        const val BLOCK_RESOLUTION_FLOAT = 16f

        val FACE_POSITION_MAP_TEMPLATE = arrayOf(
            intArrayOf(0, 2, 3, 1),
            intArrayOf(6, 4, 5, 7),
            intArrayOf(1, 5, 4, 0),
            intArrayOf(2, 6, 7, 3),
            intArrayOf(6, 2, 0, 4),
            intArrayOf(5, 1, 3, 7)
        )

        fun rotatePositions(positions: Array<Vec3>, axis: Axes, angle: Float, origin: Vec3, rescale: Boolean) {
            // TODO: optimize for 90deg, 180deg, 270deg rotations
            if (angle == 0.0f) {
                return
            }
            for ((i, position) in positions.withIndex()) {
                var transformedPosition = position - origin
                transformedPosition = transformedPosition.rotate(angle, axis, rescale)
                positions[i] = transformedPosition + origin
            }
        }

        fun transformPosition(position: Vec3): Vec3 {
            fun positionToFloat(uv: Float): Float {
                return (uv - (BLOCK_RESOLUTION / 2)) / BLOCK_RESOLUTION
            }
            return Vec3(positionToFloat(position.x), positionToFloat(position.y), positionToFloat(position.z))
        }
    }
}
