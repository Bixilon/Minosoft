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

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.VoxelShape
import de.bixilon.minosoft.data.registries.blocks.cube.CubeDirections

class DirectedProperty(private val directions: BooleanArray) : LightProperties {
    override val propagatesSkylight: Boolean = propagatesLight(Directions.UP, Directions.DOWN)

    override fun propagatesLight(from: Directions, to: Directions): Boolean {
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
            return isSideCovered(`in`) && isSideCovered(out) // ToDo: That could go wrong
        }

        @Deprecated("Absolutely trash")
        fun VoxelShape.isSideCovered(side: Directions): Boolean {
            // ToDo: This whole calculation is technically wrong, it could be that 2 different sides of 2 blocks are "free". That means that light can still not pass the blocks, but
            // this algorithm does not cover it. Let's see it as performance hack

            var min1 = 0.0
            var min2 = 0.0
            var max1 = 0.0
            var max2 = 0.0

            for (aabb in this) {
                when (side.axis) {
                    Axes.Y -> {
                        if ((side == Directions.DOWN && aabb.min.y != 0.0) || (side == Directions.UP && aabb.max.y != 1.0)) {
                            continue
                        }
                        min1 = minOf(min1, aabb.min.x)
                        min2 = minOf(min2, aabb.min.z)
                        max1 = maxOf(max1, aabb.max.x)
                        max2 = maxOf(max2, aabb.max.z)
                    }

                    Axes.X -> {
                        if ((side == Directions.WEST && aabb.min.x != 0.0) || (side == Directions.EAST && aabb.max.x != 1.0)) {
                            continue
                        }
                        min1 = minOf(min1, aabb.min.y)
                        min2 = minOf(min2, aabb.min.z)
                        max1 = maxOf(max1, aabb.max.y)
                        max2 = maxOf(max2, aabb.max.z)
                    }

                    Axes.Z -> {
                        if ((side == Directions.NORTH && aabb.min.z != 0.0) || (side == Directions.SOUTH && aabb.max.z != 1.0)) {
                            continue
                        }
                        min1 = minOf(min1, aabb.min.x)
                        min2 = minOf(min2, aabb.min.y)
                        max1 = maxOf(max1, aabb.max.x)
                        max2 = maxOf(max2, aabb.max.y)
                    }
                }
            }
            return min1 == 0.0 && min2 == 0.0 && max1 == 1.0 && max2 == 1.0
        }
    }
}
