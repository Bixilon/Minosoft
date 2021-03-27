/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelElement
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3

object VecUtil {
    val EMPTY_VECTOR = Vec3()
    val BLOCK_SIZE_VECTOR = Vec3(BlockModelElement.BLOCK_RESOLUTION)

    fun JsonElement.toVec3(): Vec3 {
        when (this) {
            is JsonArray -> return Vec3(this[0].asFloat, this[1].asFloat, this[2].asFloat)
            is JsonObject -> TODO()
            else -> throw IllegalArgumentException("Not a Vec3!")
        }
    }

    fun getRotatedValues(x: Float, y: Float, sin: Float, cos: Float): Vec2 {
        return Vec2(x * cos - y * sin, x * sin + y * cos)
    }

    fun Vec3.rotate(angle: Float, axis: Axes): Vec3 {
        if (angle == 0.0f) {
            return this
        }
        return when (axis) {
            Axes.X -> {
                val rotatedValues = getRotatedValues(this.y, this.z, glm.sin(angle), glm.cos(angle))
                Vec3(this.x, rotatedValues)
            }
            Axes.Y -> {
                val rotatedValues = getRotatedValues(this.x, this.z, glm.sin(angle), glm.cos(angle))
                Vec3(rotatedValues.x, this.y, rotatedValues.y)
            }
            Axes.Z -> {
                val rotatedValues = getRotatedValues(this.x, this.y, glm.sin(angle), glm.cos(angle))
                Vec3(rotatedValues.x, rotatedValues.y, this.z)
            }
        }
    }

    fun Vec3.rotate(axis: Vec3, sin: Float, cos: Float): Vec3 {
        return this * cos + (axis cross this) * sin + axis * (axis dot this) * (1 - cos)
    }

    fun Vec3.oneContainsIgnoreZero(vec3: Vec3): Boolean {
        if (x == vec3.x) {
            return true
        }
        if (y == vec3.y) {
            return true
        }
        if (z == vec3.z) {
            return true
        }
        return false
    }

    fun JsonArray.readUV(): Pair<Vec2, Vec2> {
        return Pair(Vec2(this[0].asFloat, this[3].asFloat), Vec2(this[2].asFloat, this[1].asFloat))
    }
}
