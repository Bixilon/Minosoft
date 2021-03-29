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

enum class Axes {
    X,
    Y,
    Z;

    companion object {
        fun byDirection(direction: Directions): Axes {
            return when (direction) {
                Directions.EAST, Directions.WEST -> X
                Directions.UP, Directions.DOWN -> Y
                Directions.NORTH, Directions.SOUTH -> Z
            }
        }

        fun choose(axis: Axes, vec3: Vec3): Float {
            return choose(axis, vec3.x, vec3.y, vec3.z);
        }

        private fun choose(axis: Axes, x: Float, y: Float, z: Float): Float {
            return when (axis) {
                X -> x
                Y -> y
                Z -> z
            }
        }

        val AXES = values()
    }
}
