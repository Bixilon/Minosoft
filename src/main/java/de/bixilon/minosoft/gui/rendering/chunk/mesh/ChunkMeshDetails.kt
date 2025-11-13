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

enum class ChunkMeshDetails {
    ENTITIES,
    TEXT,
    AMBIENT_OCCLUSION,

    FAST_BEDROCK,

    ANTI_MOIRE_PATTERN,

    RANDOM_OFFSET,


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


        val SIDES_ALL = IntInlineEnumSet<ChunkMeshDetails>() + SIDE_DOWN + SIDE_UP + SIDE_NORTH + SIDE_SOUTH + SIDE_WEST + SIDE_EAST // TODO: kutil 1.30.1


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
