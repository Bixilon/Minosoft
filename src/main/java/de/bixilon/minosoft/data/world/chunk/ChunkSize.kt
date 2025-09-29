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

package de.bixilon.minosoft.data.world.chunk

object ChunkSize {
    const val SECTION_LENGTH = 16
    const val SECTION_WIDTH_X = SECTION_LENGTH
    const val SECTION_MAX_X = SECTION_WIDTH_X - 1
    const val SECTION_WIDTH_Z = SECTION_LENGTH
    const val SECTION_MAX_Z = SECTION_WIDTH_Z - 1
    const val SECTION_HEIGHT_Y = SECTION_LENGTH
    const val SECTION_MAX_Y = SECTION_HEIGHT_Y - 1
    const val BLOCKS_PER_SECTION = SECTION_WIDTH_X * SECTION_HEIGHT_Y * SECTION_WIDTH_X

    const val CHUNK_MIN_Y = -2048
    const val CHUNK_MIN_SECTION = CHUNK_MIN_Y / SECTION_HEIGHT_Y
    const val CHUNK_MAX_Y = 2048
    const val CHUNK_MAX_SECTION = CHUNK_MAX_Y / SECTION_HEIGHT_Y
    const val CHUNK_MAX_HEIGHT = CHUNK_MAX_Y - CHUNK_MIN_Y
    const val CHUNK_MAX_SECTIONS = CHUNK_MAX_HEIGHT / SECTION_HEIGHT_Y


    @Deprecated("cherry pick rewrite-light-engine")
    const val MAX_LIGHT_LEVEL_I = 15
}
