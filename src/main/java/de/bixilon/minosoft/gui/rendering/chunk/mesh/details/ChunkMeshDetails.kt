/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.mesh.details

import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.positions.SectionPosition
import kotlin.math.abs

enum class ChunkMeshDetails {
    ENTITIES,
    TEXT,
    AMBIENT_OCCLUSION,

    ANTI_MOIRE_PATTERN,

    RANDOM_OFFSET,

    FLOWING_FLUID,
    FLUID_HEIGHTS,

    CULL_FULL_OPAQUE,
    MINOR_VISUAL_IMPACT,
    NON_FULL_BLOCKS, // TODO: That is looking bad, check if the block has at least one side that is full
    AGGRESSIVE_CULLING,

    DARK_CAVE_SURFACE,

    SIDE_DOWN,
    SIDE_UP,

    SIDE_NORTH,
    SIDE_SOUTH,

    SIDE_WEST,
    SIDE_EAST,


    // TODO: border blocks, texture animations, biome blending, ... See https://gitlab.bixilon.de/bixilon/minosoft/-/issues/128
    ;


    companion object : ValuesEnum<ChunkMeshDetails> {
        override val VALUES = values()
        override val NAME_MAP = names()

        private const val SIDE_MIN = 2
        private const val SIDE_NORMAL = 3
        private const val SIDE_MAX = 5


        val ALL = VALUES.foldRight(IntInlineEnumSet<ChunkMeshDetails>()) { detail, accumulator -> accumulator + detail } - AGGRESSIVE_CULLING - CULL_FULL_OPAQUE


        fun of(position: SectionPosition, camera: SectionPosition): IntInlineEnumSet<ChunkMeshDetails> {
            var details = ALL

            val delta = position - camera
            val distanceXZ = delta.x * delta.x + delta.z * delta.z
            val distance = distanceXZ + (delta.y * delta.y / 4)

            if (distance >= 10 * 10) details -= ENTITIES
            if (distance >= 5 * 5) details -= TEXT

            if (distance >= 24 * 24) details -= AMBIENT_OCCLUSION

            if (distance >= 15 * 15) details -= ANTI_MOIRE_PATTERN
            if (distance >= 12 * 12) details -= RANDOM_OFFSET

            if (distance >= 6 * 6) details -= FLOWING_FLUID
            if (distance >= 8 * 8) details -= FLUID_HEIGHTS

            if (distance >= 2 * 2) details += CULL_FULL_OPAQUE
            if (distance >= 36 * 36) details -= NON_FULL_BLOCKS

            if (abs(delta.y) < 3) {
                if (distance >= 14 * 14) details -= MINOR_VISUAL_IMPACT
            } else {
                if (distance >= 32 * 32) details -= MINOR_VISUAL_IMPACT
            }

            if (distance >= 12 * 12) details += AGGRESSIVE_CULLING

            if (distanceXZ >= 8 * 8) details -= DARK_CAVE_SURFACE
            if ((distanceXZ >= 5 * 5 && abs(delta.y) >= 5)) details -= DARK_CAVE_SURFACE
            if (distanceXZ >= 2 * 2 && abs(delta.y) >= 8) details -= DARK_CAVE_SURFACE

            if (delta.y < -SIDE_NORMAL) details -= SIDE_DOWN
            if (delta.y > +SIDE_NORMAL) details -= SIDE_UP

            if (delta.z < -SIDE_NORMAL) details -= SIDE_NORTH
            if (delta.z > +SIDE_NORMAL) details -= SIDE_SOUTH

            if (delta.x < -SIDE_NORMAL) details -= SIDE_WEST
            if (delta.x > +SIDE_NORMAL) details -= SIDE_EAST

            return details
        }

        fun update(previous: IntInlineEnumSet<ChunkMeshDetails>, position: SectionPosition, camera: SectionPosition): IntInlineEnumSet<ChunkMeshDetails> {

            if (position == SectionPosition(-54, 0, 70)) {
                println()
            }

            var details = previous
            val delta = position - camera

            val distanceXZ = delta.x * delta.x + delta.z * delta.z
            val distance = distanceXZ + (delta.y * delta.y / 4)


            if (distance < 9 * 9) details += ENTITIES
            if (distance >= 12 * 12) details -= ENTITIES

            if (distance < 4 * 4) details += TEXT
            if (distance >= 8 * 8) details -= TEXT

            if (distance < 20 * 20) details += AMBIENT_OCCLUSION
            if (distance >= 32 * 32) details -= AMBIENT_OCCLUSION

            if (distance < 13 * 13) details += ANTI_MOIRE_PATTERN
            if (distance < 10 * 10) details += RANDOM_OFFSET

            if (distance < 5 * 5) details += FLOWING_FLUID
            if (distance < 7 * 7) details += FLUID_HEIGHTS

            if (distance < 1 * 1) details -= CULL_FULL_OPAQUE

            if (distance < 18 * 18) details += NON_FULL_BLOCKS
            if (distance >= 24 * 24) details -= NON_FULL_BLOCKS

            if (distance < 10 * 10) details -= AGGRESSIVE_CULLING
            if (distance >= 16 * 16) details += AGGRESSIVE_CULLING

            if (abs(delta.y) < 3) {
                if (distance >= 18 * 18) details -= MINOR_VISUAL_IMPACT
                if (distance <= 12 * 12) details += MINOR_VISUAL_IMPACT
            } else {
                if (distance >= 36 * 36) details -= MINOR_VISUAL_IMPACT
                if (distance <= 28 * 28) details += MINOR_VISUAL_IMPACT
            }

            if (distanceXZ <= 7 * 7) details += DARK_CAVE_SURFACE
            if (distanceXZ <= 3 * 3 && abs(delta.y) < 4) details += DARK_CAVE_SURFACE
            if (distanceXZ <= 5 * 5 && abs(delta.y) < 7) details += DARK_CAVE_SURFACE

            if (distanceXZ >= 10 * 10) details -= DARK_CAVE_SURFACE
            if ((distanceXZ >= 7 * 7 && abs(delta.y) >= 6)) details -= DARK_CAVE_SURFACE
            if (distanceXZ >= 3 * 3 && abs(delta.y) >= 10) details -= DARK_CAVE_SURFACE



            if (delta.y >= -SIDE_MIN) details += SIDE_DOWN
            if (delta.y <= +SIDE_MIN) details += SIDE_UP

            if (delta.z >= -SIDE_MIN) details += SIDE_NORTH
            if (delta.z <= +SIDE_MIN) details += SIDE_SOUTH

            if (delta.x >= -SIDE_MIN) details += SIDE_WEST
            if (delta.x <= +SIDE_MIN) details += SIDE_EAST


            if (delta.y < -SIDE_MAX) details -= SIDE_DOWN
            if (delta.y > +SIDE_MAX) details -= SIDE_UP

            if (delta.z < -SIDE_MAX) details -= SIDE_NORTH
            if (delta.z > +SIDE_MAX) details -= SIDE_SOUTH

            if (delta.x < -SIDE_MAX) details -= SIDE_WEST
            if (delta.x > +SIDE_MAX) details -= SIDE_EAST

            return details
        }


        fun Directions.toMeshDetail() = when (this) {
            Directions.DOWN -> SIDE_DOWN
            Directions.UP -> SIDE_UP
            Directions.NORTH -> SIDE_NORTH
            Directions.SOUTH -> SIDE_SOUTH
            Directions.WEST -> SIDE_WEST
            Directions.EAST -> SIDE_EAST
        }
    }
}
