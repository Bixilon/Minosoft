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

package de.bixilon.minosoft.data.world.chunk.chunk

import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.SectionHeight

class ChunkSectionManagement(val chunk: Chunk) {
    private val world = chunk.world
    val sections: Array<ChunkSection?> = arrayOfNulls(world.dimension.sections)

    private val minSection = world.dimension.minSection
    private val maxSection = world.dimension.maxSection


    operator fun get(height: SectionHeight) = sections.getOrNull(height - minSection)

    fun unsafeCreate(height: SectionHeight, light: Boolean = true): ChunkSection {
        val index = height - minSection
        sections[index]?.let { return it }

        val section = ChunkSection(height, chunk = chunk)
        sections[index] = section

        chunk.neighbours.updateNeighbours(section)
        chunk.neighbours.updateNeighbourNeighbours(section.height, section)

        // check light of neighbours to check if their light needs to be traced into our own chunk
        if (light) {
            section.light.propagateFromNeighbours()
        }

        return section
    }

    fun create(height: SectionHeight, light: Boolean = true): ChunkSection? {
        this[height]?.let { return it }
        if (height !in minSection..maxSection) return null

        return chunk.lock.locked { unsafeCreate(height, light) }
    }

    inline fun forEach(consumer: (section: ChunkSection) -> Unit) {
        for ((index, section) in sections.withIndex()) {
            if (section == null) continue
            if (section.blocks.isEmpty) continue

            consumer.invoke(section)
        }
    }
}
