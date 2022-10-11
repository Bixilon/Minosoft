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
import de.bixilon.minosoft.data.registries.shapes.VoxelShape
import de.bixilon.minosoft.data.registries.shapes.side.VoxelSide
import de.bixilon.minosoft.data.registries.shapes.side.VoxelSideSet

class DirectedProperty(private val directions: BooleanArray) : LightProperties {
    override val propagatesLight: Boolean = true
    override val propagatesSkylight: Boolean = propagatesLight(Directions.UP) && propagatesLight(Directions.DOWN)

    override fun propagatesLight(direction: Directions): Boolean {
        return directions[direction.ordinal]
    }

    companion object {
        private val FULL_SIDE = VoxelSide(0.0f, 0.0f, 1.0f, 1.0f)

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
            val directions = BooleanArray(Directions.SIZE)

            for ((index, direction) in Directions.VALUES.withIndex()) {
                directions[index] = !shape.isSideCovered(direction)
            }

            val simple = directions.isSimple ?: return DirectedProperty(directions)

            return if (simple) TransparentProperty else SolidProperty
        }


        private fun VoxelShape.getSide(side: Directions): VoxelSideSet {
            // ToDo: This whole calculation is technically wrong, it could be that 2 different sides of 2 blocks are "free". That means that light can still not pass the blocks, but
            // this algorithm does not cover it. Let's see it as performance hack

            val sides: MutableSet<VoxelSide> = mutableSetOf()

            for (aabb in this) {
                when (side.axis) {
                    Axes.Y -> {
                        if ((side == Directions.DOWN && aabb.min.y != 0.0) || (side == Directions.UP && aabb.max.y != 1.0)) {
                            continue
                        }
                        sides += VoxelSide(aabb.min.x, aabb.min.z, aabb.max.x, aabb.max.z)
                    }

                    Axes.X -> {
                        if ((side == Directions.WEST && aabb.min.x != 0.0) || (side == Directions.EAST && aabb.max.x != 1.0)) {
                            continue
                        }
                        sides += VoxelSide(aabb.min.y, aabb.min.z, aabb.max.y, aabb.max.z)
                    }

                    Axes.Z -> {
                        if ((side == Directions.NORTH && aabb.min.z != 0.0) || (side == Directions.SOUTH && aabb.max.z != 1.0)) {
                            continue
                        }
                        sides += VoxelSide(aabb.min.x, aabb.min.y, aabb.max.x, aabb.max.y)
                    }
                }
            }

            return VoxelSideSet(sides)
        }

        fun VoxelShape.isSideCovered(direction: Directions): Boolean {
            val side = getSide(direction)
            if (side.isEmpty()) {
                return false
            }

            val rest = FULL_SIDE - side
            var compacted = rest.compact()
            if (rest != compacted) {
                compacted = FULL_SIDE - compacted
            }

            return !FULL_SIDE.touches(compacted)
        }
    }
}
