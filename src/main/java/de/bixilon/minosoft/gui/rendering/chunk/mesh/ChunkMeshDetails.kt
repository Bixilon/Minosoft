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

package de.bixilon.minosoft.gui.rendering.chunk.mesh

import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
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


        val ALL = VALUES.foldRight(IntInlineEnumSet<ChunkMeshDetails>()) { detail, accumulator -> accumulator + detail }


        fun of(position: SectionPosition, camera: SectionPosition): IntInlineEnumSet<ChunkMeshDetails> {
            var details = ALL

            val delta = position - camera
            val max = maxOf(abs(delta.x), abs(delta.y), abs(delta.z))

            if (max >= 5) details -= ENTITIES
            if (max >= 4) details -= TEXT

            if (max >= 8) details -= AMBIENT_OCCLUSION

            if (max >= 10) details -= ANTI_MOIRE_PATTERN

            if (max >= 5) details -= RANDOM_OFFSET

            if (max >= 5) details -= FLOWING_FLUID
            if (max >= 8) details -= FLUID_HEIGHTS

            if (max >= 8) details -= TRANSPARENCY

            if (max >= 2) details -= FULL_OPAQUE_CULLED

            if (delta.y < -3) details -= SIDE_DOWN
            if (delta.y > 3) details -= SIDE_UP

            if (delta.z < -3) details -= SIDE_NORTH
            if (delta.z > 3) details -= SIDE_SOUTH

            if (delta.x < -3) details -= SIDE_WEST
            if (delta.x > 3) details -= SIDE_EAST


            return details
        }

        fun update(previous: IntInlineEnumSet<ChunkMeshDetails>, position: SectionPosition, camera: SectionPosition): IntInlineEnumSet<ChunkMeshDetails> {

            // TODO
            return previous
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
