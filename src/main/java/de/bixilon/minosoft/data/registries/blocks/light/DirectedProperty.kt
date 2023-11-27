/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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
import de.bixilon.minosoft.data.registries.shapes.side.SideQuad
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape

class DirectedProperty(
    private val directions: BooleanArray,
    override val skylightEnters: Boolean,
    override val filtersSkylight: Boolean,
) : LightProperties {
    override val propagatesLight: Boolean = true

    override fun propagatesLight(direction: Directions): Boolean {
        return directions[direction.ordinal]
    }

    companion object {
        private val TRUE = BooleanArray(Directions.SIZE) { true }
        private val FALSE = BooleanArray(Directions.SIZE) { false }
        private val FULL_SIDE = SideQuad(0.0f, 0.0f, 1.0f, 1.0f)
        private val REQUIRED_SURFACE_AREA = FULL_SIDE.surfaceArea() - 0.0001f // add some padding for floating point

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

        fun of(shape: AbstractVoxelShape, skylightEnters: Boolean = true, filtersLight: Boolean = false): LightProperties {
            val directions = BooleanArray(Directions.SIZE)

            for ((index, direction) in Directions.VALUES.withIndex()) {
                directions[index] = !shape.isSideCovered(direction)
            }


            val simple = directions.isSimple ?: return DirectedProperty(directions, skylightEnters, filtersLight)

            if (!filtersLight) {
                return DirectedProperty(if (simple) TRUE else FALSE, simple, !simple)
            }

            return if (simple) TransparentProperty else OpaqueProperty
        }

        private fun AbstractVoxelShape.getSideArea(direction: Directions, target: SideQuad): Float {
            // overlapping is broken, see https://stackoverflow.com/questions/7342935/algorithm-to-compute-total-area-covered-by-a-set-of-overlapping-segments
            // ToDo: This whole calculation is technically wrong, it could be that 2 different sides of 2 blocks are "free". That means that light can still not pass the blocks, but
            // this algorithm does not cover it. Let's see it as performance hack

            if (this.aabbs == 0) return 0.0f

            var area = 0.0f


            for (aabb in this) {
                val a: Float
                val b: Float
                val c: Float
                val d: Float

                when (direction.axis) {
                    Axes.Y -> {
                        if ((direction == Directions.DOWN && aabb.min.y != 0.0) || (direction == Directions.UP && aabb.max.y != 1.0)) {
                            continue
                        }
                        a = aabb.min.x.toFloat(); b = aabb.min.z.toFloat(); c = aabb.max.x.toFloat(); d = aabb.max.z.toFloat()
                    }

                    Axes.X -> {
                        if ((direction == Directions.WEST && aabb.min.x != 0.0) || (direction == Directions.EAST && aabb.max.x != 1.0)) {
                            continue
                        }
                        a = aabb.min.y.toFloat(); b = aabb.min.z.toFloat(); c = aabb.max.y.toFloat(); d = aabb.max.z.toFloat()
                    }

                    Axes.Z -> {
                        if ((direction == Directions.NORTH && aabb.min.z != 0.0) || (direction == Directions.SOUTH && aabb.max.z != 1.0)) {
                            continue
                        }
                        a = aabb.min.x.toFloat(); b = aabb.min.y.toFloat(); c = aabb.max.x.toFloat(); d = aabb.max.y.toFloat()
                    }
                }
                val width = minOf(target.max.x, c) - maxOf(a, target.min.x)
                val height = minOf(target.max.y, d) - maxOf(b, target.min.y)

                area += width * height
            }

            return area
        }

        fun AbstractVoxelShape.isSideCovered(direction: Directions): Boolean {
            // this should be improved: https://stackoverflow.com/questions/76373725/check-if-a-quad-is-fully-covered-by-a-set-of-others
            val surface = getSideArea(direction, FULL_SIDE)

            return surface >= REQUIRED_SURFACE_AREA
        }
    }
}
