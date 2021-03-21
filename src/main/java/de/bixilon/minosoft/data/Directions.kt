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
package de.bixilon.minosoft.data

import glm_.vec3.Vec3

enum class Directions(direction: Vec3) {
    DOWN(Vec3(0, -1, 0)),
    UP(Vec3(0, 1, 0)),
    NORTH(Vec3(0, 0, -1)),
    SOUTH(Vec3(0, 0, 1)),
    WEST(Vec3(-1, 0, 0)),
    EAST(Vec3(1, 0, 0));

    val inverse: Directions = inverse()

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

    val directionVector: Vec3 = direction

    companion object {
        val DIRECTIONS = values()
        val SIDES = arrayOf(NORTH, SOUTH, WEST, EAST)
        const val SIDES_OFFSET = 2

        @JvmStatic
        fun byId(id: Int): Directions {
            return DIRECTIONS[id]
        }

        private const val MIN_ERROR = 0.0001f

        fun byDirection(direction: Vec3): Directions {
            var minDirection = DIRECTIONS[0]
            var minError = 2f
            for (testDirection in DIRECTIONS) {
                val error = (testDirection.directionVector - direction).length()
                if (error < MIN_ERROR) {
                    return testDirection
                } else if (error < minError) {
                    minError = error
                    minDirection = testDirection
                }
            }
            return minDirection
        }
    }
}
