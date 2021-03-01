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
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec3.Vec3

object VecUtil {

    fun jsonToVec3(json: JsonElement): Vec3 {
        when (json) {
            is JsonArray -> return Vec3(json[0].asFloat, json[1].asFloat, json[2].asFloat)
            is JsonObject -> TODO()
            else -> throw IllegalArgumentException("Not a Vec3!")
        }
    }

    fun getRotatedValues(x: Float, y: Float, sin: Float, cos: Float): Vec2 {
        return Vec2(x * cos - y * sin, x * sin + y * cos)
    }

    fun rotateVector(original: Vec3, angle: Float, axis: Axes): Vec3 {
        if (angle == 0f) {
            return original
        }
        return when (axis) {
            Axes.X -> {
                val rotatedValues = getRotatedValues(original.y, original.z, glm.sin(angle), glm.cos(angle))
                Vec3(original.x, rotatedValues)
            }
            Axes.Y -> {
                val rotatedValues = getRotatedValues(original.x, original.z, glm.sin(angle), glm.cos(angle))
                Vec3(rotatedValues.x, original.y, rotatedValues.y)
            }
            Axes.Z -> {
                val rotatedValues = getRotatedValues(original.x, original.y, glm.sin(angle), glm.cos(angle))
                Vec3(rotatedValues.x, rotatedValues.y, original.z)
            }
        }
    }
}
