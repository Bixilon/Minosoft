/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data

import de.bixilon.minosoft.data.mappings.blocks.properties.serializer.BlockPropertiesSerializer
import de.bixilon.minosoft.gui.rendering.chunk.models.FaceSize
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelElement
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

enum class Directions(val directionVector: Vec3i) {
    DOWN(Vec3i(0, -1, 0)),
    UP(Vec3i(0, 1, 0)),
    NORTH(Vec3i(0, 0, -1)),
    SOUTH(Vec3i(0, 0, 1)),
    WEST(Vec3i(-1, 0, 0)),
    EAST(Vec3i(1, 0, 0));

    val floatDirectionVector = Vec3(directionVector)

    lateinit var inverted: Directions
        private set

    private fun inverse(): Directions {
        val ordinal = ordinal
        return if (ordinal % 2 == 0) {
            byId(ordinal + 1)
        } else {
            byId(ordinal - 1)
        }
    }

    fun sidesNextTo(direction: Directions): Set<Directions> {
        return when (direction) {
            NORTH, SOUTH -> setOf(EAST, WEST)
            EAST, WEST -> setOf(NORTH, SOUTH)
            else -> emptySet()
        }
    }

    /**
     * @return the size of the face in this direction. null if the face is not touching the border (determinated by the block resolution)
     */
    fun getFaceBorderSizes(start: Vec3, end: Vec3): FaceSize? {
        // check if face is touching the border of a block

        if (!isBlockResolutionBorder(start, end)) {
            return null
        }
        return getFaceSize(start, end)
    }

    fun getFaceSize(start: Vec3, end: Vec3): FaceSize {
        return when (this) {
            DOWN, UP -> FaceSize(Vec2i(start.x, start.z), Vec2i(end.x, end.z))
            NORTH, SOUTH -> FaceSize(Vec2i(start.x, start.y), Vec2i(end.x, end.y))
            EAST, WEST -> FaceSize(Vec2i(start.y, start.z), Vec2i(end.y, end.z))
        }
    }

    private fun isBlockResolutionBorder(start: Vec3, end: Vec3): Boolean {
        return isCoordinateBorder(directionVector.x, start.x, end.x) || isCoordinateBorder(directionVector.y, start.y, end.y) || isCoordinateBorder(directionVector.z, start.z, end.z)
    }

    private fun isCoordinateBorder(directionValue: Int, start: Float, end: Float): Boolean {
        if (directionValue == 1) {
            return start == BlockModelElement.BLOCK_RESOLUTION_FLOAT || end == BlockModelElement.BLOCK_RESOLUTION_FLOAT
        }
        if (directionValue == -1) {
            return start == 0.0f || end == 0.0f
        }
        return false
    }


    companion object : BlockPropertiesSerializer, ValuesEnum<Directions> {
        override val VALUES = values()
        override val NAME_MAP: Map<String, Directions> = KUtil.getEnumValues(VALUES)
        val SIDES = arrayOf(NORTH, SOUTH, WEST, EAST)
        const val SIDES_OFFSET = 2


        override fun deserialize(value: Any): Directions {
            return NAME_MAP[value] ?: throw IllegalArgumentException("No such property: $value")
        }

        @JvmStatic
        fun byId(id: Int): Directions {
            return VALUES[id]
        }

        private const val MIN_ERROR = 0.0001f

        fun byDirection(direction: Vec3): Directions {
            var minDirection = VALUES[0]
            var minError = 2.0f
            for (testDirection in VALUES) {
                val error = (testDirection.floatDirectionVector - direction).length()
                if (error < MIN_ERROR) {
                    return testDirection
                } else if (error < minError) {
                    minError = error
                    minDirection = testDirection
                }
            }
            return minDirection
        }


        init {
            for (direction in VALUES) {
                direction.inverted = direction.inverse()
            }
        }
    }
}
