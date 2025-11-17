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

    TRANSPARENCY,

    FULL_OPAQUE_CULLED,


    SIDE_DOWN,
    SIDE_UP,

    SIDE_NORTH,
    SIDE_SOUTH,

    SIDE_WEST,
    SIDE_EAST,

    // TODO: border blocks, texture animations, leaves, biome blending, ... See https://gitlab.bixilon.de/bixilon/minosoft/-/issues/128
    ;


    companion object : ValuesEnum<ChunkMeshDetails> {
        override val VALUES = values()
        override val NAME_MAP = names()

        private const val SIDE_MIN = 1
        private const val SIDE_MAX = 3


        val ALL = VALUES.foldRight(IntInlineEnumSet<ChunkMeshDetails>()) { detail, accumulator -> accumulator + detail }


        fun of(position: SectionPosition, camera: SectionPosition): IntInlineEnumSet<ChunkMeshDetails> {
            var details = ALL

            val delta = position - camera
            val max = maxOf(abs(delta.x), abs(delta.y), abs(delta.z))

            if (max >= 8) details -= ENTITIES
            if (max >= 5) details -= TEXT

            if (max >= 20) details -= AMBIENT_OCCLUSION

            if (max >= 15) details -= ANTI_MOIRE_PATTERN
            if (max >= 8) details -= RANDOM_OFFSET

            if (max >= 5) details -= FLOWING_FLUID
            if (max >= 7) details -= FLUID_HEIGHTS

            if (max >= 15) details -= TRANSPARENCY

            if (max >= 2) details -= FULL_OPAQUE_CULLED

            details = removeSides(details, delta)

            return details
        }


        private fun removeSides(details: IntInlineEnumSet<ChunkMeshDetails>, delta: SectionPosition): IntInlineEnumSet<ChunkMeshDetails> {
            var details = details

            if (delta.y < -SIDE_MAX) details -= SIDE_DOWN
            if (delta.y > +SIDE_MAX) details -= SIDE_UP

            if (delta.z < -SIDE_MAX) details -= SIDE_NORTH
            if (delta.z > +SIDE_MAX) details -= SIDE_SOUTH

            if (delta.x < -SIDE_MAX) details -= SIDE_WEST
            if (delta.x > +SIDE_MAX) details -= SIDE_EAST

            return details
        }

        private fun addSides(details: IntInlineEnumSet<ChunkMeshDetails>, delta: SectionPosition): IntInlineEnumSet<ChunkMeshDetails> {
            var details = details

            if (delta.y >= -SIDE_MIN) details += SIDE_DOWN
            if (delta.y <= +SIDE_MIN) details += SIDE_UP

            if (delta.z >= -SIDE_MIN) details += SIDE_NORTH
            if (delta.z <= +SIDE_MIN) details += SIDE_SOUTH

            if (delta.x >= -SIDE_MIN) details += SIDE_WEST
            if (delta.x <= +SIDE_MIN) details += SIDE_EAST

            return details
        }

        fun update(previous: IntInlineEnumSet<ChunkMeshDetails>, position: SectionPosition, camera: SectionPosition): IntInlineEnumSet<ChunkMeshDetails> {
            var details = previous
            val delta = position - camera
            val max = maxOf(abs(delta.x), abs(delta.y), abs(delta.z))


            if (max < 8) details += ENTITIES
            if (max >= 9) details -= ENTITIES

            if (max < 4) details += TEXT
            if (max >= 5) details -= TEXT

            if (max < 18) details += AMBIENT_OCCLUSION
            if (max >= 20) details -= AMBIENT_OCCLUSION

            if (max < 13) details += ANTI_MOIRE_PATTERN
            if (max < 7) details += RANDOM_OFFSET

            if (max < 5) details += FLOWING_FLUID
            if (max < 7) details += FLUID_HEIGHTS

            if (max < 13) details += TRANSPARENCY

            if (max < 1) details += FULL_OPAQUE_CULLED

            details = removeSides(details, delta)
            details = addSides(details, delta)

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
