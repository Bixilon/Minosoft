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

package de.bixilon.minosoft.data.registries.blocks.light

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.VoxelShape
import de.bixilon.minosoft.data.registries.blocks.cube.CubeDirections

class DirectedProperty(private val directions: BooleanArray) : LightProperties {

    override fun propagatesSkylight(from: Directions, to: Directions): Boolean {
        return directions[CubeDirections.getIndex(from, to)]
    }

    override fun propagatesBlockLight(from: Directions, to: Directions): Boolean {
        return directions[CubeDirections.getIndex(from, to)]
    }

    companion object {

        private val BooleanArray.isSimple: Boolean?
            get() {
                var value: Boolean? = null
                for (entry in this) {
                    if (value == null) {
                        value = entry
                        continue
                    }
                    if (entry != value) {
                        return null
                    }
                }
                return value
            }

        fun of(shape: VoxelShape): LightProperties {
            val directions = BooleanArray(CubeDirections.CUBE_DIRECTION_COMBINATIONS)

            for ((index, pair) in CubeDirections.PAIRS.withIndex()) {
                directions[index] = shape.canPropagate(pair.`in`, pair.out)
            }

            val simple = directions.isSimple ?: return DirectedProperty(directions)

            return if (simple) TransparentProperty else SolidProperty
        }

        fun VoxelShape.canPropagate(`in`: Directions, out: Directions): Boolean {
            TODO("Not yet implemented")
        }
    }
}
