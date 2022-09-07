/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks.cube

import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions

object CubeDirections {
    const val CUBE_DIRECTION_COMBINATIONS = 15 // 5+4+3+2+1

    val PAIRS = arrayOf(
        DirectionPair(Directions.DOWN, Directions.UP),
        DirectionPair(Directions.DOWN, Directions.NORTH),
        DirectionPair(Directions.DOWN, Directions.SOUTH),
        DirectionPair(Directions.DOWN, Directions.WEST),
        DirectionPair(Directions.DOWN, Directions.EAST),

        DirectionPair(Directions.UP, Directions.NORTH),
        DirectionPair(Directions.UP, Directions.SOUTH),
        DirectionPair(Directions.UP, Directions.WEST),
        DirectionPair(Directions.UP, Directions.EAST),

        DirectionPair(Directions.NORTH, Directions.SOUTH),
        DirectionPair(Directions.NORTH, Directions.WEST),
        DirectionPair(Directions.NORTH, Directions.EAST),

        DirectionPair(Directions.SOUTH, Directions.WEST),
        DirectionPair(Directions.SOUTH, Directions.EAST),

        DirectionPair(Directions.WEST, Directions.EAST),
    )


    fun getIndex(`in`: Directions, out: Directions): Int {
        // ToDo: Calculate this far better
        val preferIn = `in`.ordinal < out.ordinal

        val first: Directions
        val second: Directions

        if (preferIn) {
            first = `in`
            second = out
        } else {
            first = out
            second = `in`
        }

        when (first) {
            Directions.DOWN -> return when (second) {
                Directions.UP -> 0
                Directions.NORTH -> 1
                Directions.SOUTH -> 2
                Directions.WEST -> 3
                Directions.EAST -> 4
                else -> Broken()
            }

            Directions.UP -> return when (second) {
                Directions.NORTH -> 5
                Directions.SOUTH -> 6
                Directions.WEST -> 7
                Directions.EAST -> 8
                else -> Broken()
            }

            Directions.NORTH -> return when (second) {
                Directions.SOUTH -> 9
                Directions.WEST -> 10
                Directions.EAST -> 11
                else -> Broken()
            }

            Directions.SOUTH -> return when (second) {
                Directions.WEST -> 12
                Directions.EAST -> 13
                else -> Broken()
            }

            else -> return 14 // WEST->EAST
        }
    }
}
