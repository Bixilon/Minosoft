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
package de.bixilon.minosoft.data.direction

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kotlinglm.vec3.swizzle.xy
import de.bixilon.kotlinglm.vec3.swizzle.xz
import de.bixilon.kotlinglm.vec3.swizzle.yz
import de.bixilon.kotlinglm.vec3.swizzle.zy
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.registries.blocks.properties.serializer.BlockPropertiesSerializer
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.get
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import kotlin.reflect.jvm.javaField

enum class Directions(
    @Deprecated("remove") val campfireId: Int,
    val vector: Vec3i,
    val index: Vec3i,
) {
    DOWN(-1, Vec3i(0, -1, 0), Vec3i(1, -1, 1)),
    UP(-1, Vec3i(0, 1, 0), Vec3i(3, -1, 3)),
    NORTH(2, Vec3i(0, 0, -1), Vec3i(0, 0, -1)),
    SOUTH(0, Vec3i(0, 0, 1), Vec3i(2, 2, -1)),
    WEST(1, Vec3i(-1, 0, 0), Vec3i(-1, 3, 2)),
    EAST(3, Vec3i(1, 0, 0), Vec3i(-1, 1, 0)),
    ;

    val negative = ordinal % 2 == 0

    val vectorf = Vec3(vector)
    val vectord = Vec3d(vector)

    val axis: Axes = unsafeNull()
    val rotatedMatrix: Mat4 = unsafeNull()
    val inverted: Directions = unsafeNull()

    private fun invert(): Directions {
        val ordinal = ordinal
        return if (ordinal % 2 == 0) {
            Directions[ordinal + 1]
        } else {
            Directions[ordinal - 1]
        }
    }

    operator fun get(axis: Axes): Int {
        return vector[axis]
    }


    @Deprecated("outsource")
    fun getPositions(from: Vec3, to: Vec3): Array<Vec3> {
        return when (this) {
            DOWN -> arrayOf(Vec3(from.x, from.y, to.z), Vec3(to.x, from.y, to.z), Vec3(to.x, from.y, from.z), from)
            UP -> arrayOf(Vec3(from.x, to.y, from.z), Vec3(to.x, to.y, from.z), to, Vec3(from.x, to.y, to.z))
            NORTH -> arrayOf(Vec3(to.x, to.y, from.z), Vec3(from.x, to.y, from.z), from, Vec3(to.x, from.y, from.z))
            SOUTH -> arrayOf(Vec3(from.x, to.y, to.z), to, Vec3(to.x, from.y, to.z), Vec3(from.x, from.y, to.z))
            WEST -> arrayOf(Vec3(from.x, to.y, from.z), Vec3(from.x, to.y, to.z), Vec3(from.x, from.y, to.z), from)
            EAST -> arrayOf(to, Vec3(to.x, to.y, from.z), Vec3(to.x, from.y, from.z), Vec3(to.x, from.y, to.z))
        }
    }

    @Deprecated("outsource")
    fun getBlock(x: Int, y: Int, z: Int, section: ChunkSection, neighbours: Array<ChunkSection?>): BlockState? {
        return when (this) {
            DOWN -> {
                if (y == 0) {
                    neighbours[Directions.O_DOWN]?.blocks?.let { it[x, ProtocolDefinition.SECTION_MAX_Y, z] }
                } else {
                    section.blocks[x, y - 1, z]
                }
            }

            UP -> {
                if (y == ProtocolDefinition.SECTION_MAX_Y) {
                    neighbours[Directions.O_UP]?.blocks?.let { it[x, 0, z] }
                } else {
                    section.blocks[x, y + 1, z]
                }
            }

            NORTH -> {
                if (z == 0) {
                    neighbours[Directions.O_NORTH]?.blocks?.let { it[x, y, ProtocolDefinition.SECTION_MAX_Z] }
                } else {
                    section.blocks[x, y, z - 1]
                }
            }

            SOUTH -> {
                if (z == ProtocolDefinition.SECTION_MAX_Z) {
                    neighbours[Directions.O_SOUTH]?.blocks?.let { it[x, y, 0] }
                } else {
                    section.blocks[x, y, z + 1]
                }
            }

            WEST -> {
                if (x == 0) {
                    neighbours[Directions.O_WEST]?.blocks?.let { it[ProtocolDefinition.SECTION_MAX_X, y, z] }
                } else {
                    section.blocks[x - 1, y, z]
                }
            }

            EAST -> {
                if (x == ProtocolDefinition.SECTION_MAX_X) {
                    neighbours[Directions.O_EAST]?.blocks?.let { it[0, y, z] }
                } else {
                    section.blocks[x + 1, y, z]
                }
            }
        }
    }


    companion object : BlockPropertiesSerializer, ValuesEnum<Directions> {
        const val O_DOWN = 0 // Directions.DOWN.ordinal
        const val O_UP = 1 // Directions.UP.ordinal
        const val O_NORTH = 2 // Directions.NORTH.ordinal
        const val O_SOUTH = 3 // Directions.SOUTH.ordinal
        const val O_WEST = 4 // Directions.WEST.ordinal
        const val O_EAST = 5 // Directions.EAST.ordinal

        const val SIZE = 6
        const val SIZE_SIDES = 4
        const val SIDE_OFFSET = 2
        override val VALUES = values()
        override val NAME_MAP: Map<String, Directions> = EnumUtil.getEnumValues(VALUES)
        val SIDES = arrayOf(NORTH, SOUTH, WEST, EAST)

        val INDEXED = arrayOf(
            arrayOf(NORTH, DOWN, SOUTH, UP), // X
            arrayOf(NORTH, EAST, SOUTH, WEST), // y
            arrayOf(EAST, DOWN, WEST, UP), // z
        )

        val XYZ = arrayOf(WEST, EAST, DOWN, UP, NORTH, SOUTH)

        override fun deserialize(value: Any): Directions {
            return NAME_MAP[value] ?: throw IllegalArgumentException("No such property: $value")
        }

        private const val MIN_ERROR = 0.0001f

        @Deprecated("outsource")
        fun byDirection(direction: Vec3): Directions {
            var minDirection = VALUES[0]
            var minError = 2.0f
            for (testDirection in VALUES) {
                val error = (testDirection.vectorf - direction).length()
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
            val rotationMatrix = Directions::rotatedMatrix.javaField!!
            val inverted = Directions::inverted.javaField!!
            val axis = Directions::axis.javaField!!
            for (direction in VALUES) {
                inverted.forceSet(direction, direction.invert())
                rotationMatrix.forceSet(direction, DirectionUtil.rotateMatrix(direction))
                axis.forceSet(direction, Axes[direction])
            }
            NAME_MAP.unsafeCast<MutableMap<String, Directions>>()["bottom"] = DOWN
        }
    }
}
