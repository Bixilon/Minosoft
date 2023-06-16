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
import de.bixilon.minosoft.data.registries.shapes.side.VoxelSide
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

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

        fun of(shape: AbstractVoxelShape, skylightEnters: Boolean, filtersLight: Boolean): LightProperties {
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


        private fun AbstractVoxelShape.getSide(side: Directions): VoxelSide? {
            // ToDo: This whole calculation is technically wrong, it could be that 2 different sides of 2 blocks are "free". That means that light can still not pass the blocks, but
            // this algorithm does not cover it. Let's see it as performance hack

            if (this.aabbs == 0) return null

            val sides: MutableSet<SideQuad> = ObjectOpenHashSet()

            for (aabb in this) {
                when (side.axis) {
                    Axes.Y -> {
                        if ((side == Directions.DOWN && aabb.min.y != 0.0) || (side == Directions.UP && aabb.max.y != 1.0)) {
                            continue
                        }
                        val side = SideQuad(aabb.min.x.toFloat(), aabb.min.z.toFloat(), aabb.max.x.toFloat(), aabb.max.z.toFloat())
                        if (side.surfaceArea() > 0.0f) {
                            sides += side
                        }
                    }

                    Axes.X -> {
                        if ((side == Directions.WEST && aabb.min.x != 0.0) || (side == Directions.EAST && aabb.max.x != 1.0)) {
                            continue
                        }
                        val side = SideQuad(aabb.min.y.toFloat(), aabb.min.z.toFloat(), aabb.max.y.toFloat(), aabb.max.z.toFloat())
                        if (side.surfaceArea() > 0.0f) {
                            sides += side
                        }
                    }

                    Axes.Z -> {
                        if ((side == Directions.NORTH && aabb.min.z != 0.0) || (side == Directions.SOUTH && aabb.max.z != 1.0)) {
                            continue
                        }
                        val side = SideQuad(aabb.min.x.toFloat(), aabb.min.y.toFloat(), aabb.max.x.toFloat(), aabb.max.y.toFloat())
                        if (side.surfaceArea() > 0.0f) {
                            sides += side
                        }
                    }
                }
            }

            if (sides.isEmpty()) return null

            return VoxelSide(sides)
        }

        private fun VoxelSide.getSideArea(target: SideQuad): Float {
            // overlapping is broken, see https://stackoverflow.com/questions/7342935/algorithm-to-compute-total-area-covered-by-a-set-of-overlapping-segments
            var area = 0.0f

            for (quad in this) {
                val width = minOf(target.max.x, quad.max.x) - maxOf(quad.min.x, target.min.x)
                val height = minOf(target.max.y, quad.max.y) - maxOf(quad.min.y, target.min.y)

                area += width * height
            }

            return area
        }

        fun AbstractVoxelShape.isSideCovered(direction: Directions): Boolean {
            // this should be improved: https://stackoverflow.com/questions/76373725/check-if-a-quad-is-fully-covered-by-a-set-of-others
            val side = getSide(direction) ?: return false

            val surface = side.getSideArea(FULL_SIDE)

            return surface >= REQUIRED_SURFACE_AREA
        }
    }
}
