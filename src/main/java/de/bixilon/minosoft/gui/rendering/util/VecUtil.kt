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

package de.bixilon.minosoft.gui.rendering.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.blocks.Block
import de.bixilon.minosoft.data.mappings.blocks.RandomOffsetTypes
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelElement
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.func.common.clamp
import glm_.func.cos
import glm_.func.sin
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

object VecUtil {
    val EMPTY_VEC3 = Vec3(0, 0, 0)
    val EMPTY_VEC3I = Vec3i(0, 0, 0)
    val BLOCK_SIZE_VEC3 = Vec3(BlockModelElement.BLOCK_RESOLUTION)
    val ONES_VEC3 = Vec3(1)

    fun JsonElement.toVec3(): Vec3 {
        return when (this) {
            is JsonArray -> Vec3(this[0].asFloat, this[1].asFloat, this[2].asFloat)
            is JsonObject -> Vec3(this["x"]?.asFloat ?: 0, this["y"]?.asFloat ?: 0, this["z"]?.asFloat ?: 0)
            else -> throw IllegalArgumentException("Not a Vec3!")
        }
    }

    fun getRotatedValues(x: Float, y: Float, sin: Float, cos: Float, rescale: Boolean): Vec2 {
        val result = Vec2(x * cos - y * sin, x * sin + y * cos)
        if (rescale) {
            return result / cos
        }
        return result
    }

    fun Vec3.rotate(angle: Float, axis: Axes): Vec3 {
        return this.rotate(angle, axis, false)
    }

    fun Vec3.rotate(angle: Float, axis: Axes, rescale: Boolean): Vec3 {
        if (angle == 0.0f) {
            return this
        }
        return when (axis) {
            Axes.X -> {
                val rotatedValues = getRotatedValues(this.y, this.z, angle.sin, angle.cos, rescale)
                Vec3(this.x, rotatedValues)
            }
            Axes.Y -> {
                val rotatedValues = getRotatedValues(this.x, this.z, angle.sin, angle.cos, rescale)
                Vec3(rotatedValues.x, this.y, rotatedValues.y)
            }
            Axes.Z -> {
                val rotatedValues = getRotatedValues(this.x, this.y, angle.sin, angle.cos, rescale)
                Vec3(rotatedValues.x, rotatedValues.y, this.z)
            }
        }
    }

    fun Vec3.rotate(axis: Vec3, sin: Float, cos: Float): Vec3 {
        return this * cos + (axis cross this) * sin + axis * (axis dot this) * (1 - cos)
    }

    fun JsonArray.readUV(): Pair<Vec2, Vec2> {
        return Pair(Vec2(this[0].asFloat, BlockModelElement.BLOCK_RESOLUTION - this[1].asFloat), Vec2(this[2].asFloat, BlockModelElement.BLOCK_RESOLUTION - this[3].asFloat))
    }

    val Vec3i.chunkPosition: Vec2i
        get() {
            val chunkX = if (this.x >= 0) {
                this.x / ProtocolDefinition.SECTION_WIDTH_X
            } else {
                ((this.x + 1) / ProtocolDefinition.SECTION_WIDTH_X) - 1
            }
            val chunkY = if (this.z >= 0) {
                this.z / ProtocolDefinition.SECTION_WIDTH_Z
            } else {
                ((this.z + 1) / ProtocolDefinition.SECTION_WIDTH_Z) - 1
            }
            return Vec2i(chunkX, chunkY)
        }

    val Vec3i.inChunkPosition: Vec3i
        get() {
            var x: Int = this.x % ProtocolDefinition.SECTION_WIDTH_X
            if (x < 0) {
                x += ProtocolDefinition.SECTION_WIDTH_X
            }
            var z: Int = this.z % ProtocolDefinition.SECTION_WIDTH_Z
            if (z < 0) {
                z += ProtocolDefinition.SECTION_WIDTH_Z
            }
            return Vec3i(x, y, z)
        }

    val Vec3i.inChunkSectionPosition: Vec3i
        get() {
            val inVec2i = inChunkPosition
            val y = if (y < 0) {
                ((ProtocolDefinition.SECTION_HEIGHT_Y + (y % ProtocolDefinition.SECTION_HEIGHT_Y))) % ProtocolDefinition.SECTION_HEIGHT_Y
            } else {
                y % ProtocolDefinition.SECTION_HEIGHT_Y
            }
            return Vec3i(inVec2i.x, y, inVec2i.z)
        }

    val Vec3i.sectionHeight: Int
        get() {
            return if (y < 0) {
                (y + 1) / ProtocolDefinition.SECTION_HEIGHT_Y - 1
            } else {
                y / ProtocolDefinition.SECTION_HEIGHT_Y
            }
        }

    val Vec3i.entityPosition: Vec3
        get() {
            return Vec3(x + 0.5f, y, z + 0.5f) // ToDo
        }

    val Vec3.blockPosition: Vec3i
        get() {
            return Vec3i((x - 0.5f).toInt(), y.toInt(), (z - 0.5f).toInt()) // ToDo
        }

    fun Vec3i.Companion.of(chunkPosition: Vec2i, sectionHeight: Int, inChunkSectionPosition: Vec3i): Vec3i {
        return Vec3i(chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X + inChunkSectionPosition.x, sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y + inChunkSectionPosition.y, chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z + inChunkSectionPosition.z) // ToDo
    }

    infix operator fun Vec3i.plus(vec3: Vec3i?): Vec3i {
        if (vec3 == null) {
            return this
        }
        return Vec3i((x + vec3.x), (y + vec3.y), (z + vec3.z))
    }

    infix operator fun Vec3i.plus(vec2: Vec2i?): Vec3i {
        if (vec2 == null) {
            return this
        }
        return Vec3i((x + vec2.x), y, (z + vec2.y))
    }

    infix operator fun Vec3i.plus(direction: Directions?): Vec3i {
        return this + direction?.directionVector
    }

    infix operator fun Vec3i.plus(input: Vec3): Vec3 {
        return Vec3(input.x + x, input.y + y, input.z + z)
    }

    infix operator fun Vec2i.plus(vec3: Vec3i): Vec2i {
        return Vec2i(x + vec3.x, y + vec3.z)
    }

    infix operator fun Vec2i.plus(direction: Directions): Vec2i {
        return this + direction.directionVector
    }

    fun Vec3i.getWorldOffset(block: Block): Vec3 {
        if (block.randomOffsetType == null || !Minosoft.config.config.game.other.flowerRandomOffset) {
            return EMPTY_VEC3

        }
        val positionHash = generatePositionHash(x, 0, z)
        val maxModelOffset = 0.25f // use block.model.max_model_offset

        fun horizontal(axisHash: Long): Float {
            return (((axisHash and 0xF) / 15.0f) - 0.5f) / 2.0f
        }

        return Vec3(
            x = horizontal(positionHash),
            y = if (block.randomOffsetType === RandomOffsetTypes.XYZ) {
                (((positionHash shr 4 and 0xF) / 15.0f) - 1.0f) / 5.0f
            } else {
                0.0f
            },
            z = horizontal(positionHash shr 8)).clamp(-maxModelOffset, maxModelOffset)
    }

    private fun Vec3.clamp(min: Float, max: Float): Vec3 {
        return Vec3(
            x = x.clamp(min, max),
            y = y.clamp(min, max),
            z = z.clamp(min, max),
        )
    }

    private fun generatePositionHash(x: Int, y: Int, z: Int): Long {
        var hash = (x * 3129871L) xor z.toLong() * 116129781L xor y.toLong()
        hash = hash * hash * 42317861L + hash * 11L
        return hash shr 16
    }
}
