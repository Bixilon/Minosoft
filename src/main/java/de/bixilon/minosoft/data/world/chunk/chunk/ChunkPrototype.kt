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

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.light.types.LightArray
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class ChunkPrototype(
    var blocks: Array<Array<BlockState?>?>? = null,
    var blockEntities: Map<InChunkPosition, JsonObject>? = null,
    var biomeSource: BiomeSource? = null,
    var light: Array<LightArray?>? = null,
    var bottomLight: LightArray? = null,
    var topLight: LightArray? = null,
) {

    @Synchronized
    fun update(data: ChunkPrototype) {
        data.blocks?.let { this.blocks = it }
        data.blockEntities?.let { this.blockEntities = it }
        data.biomeSource?.let { this.biomeSource = it }
        data.light?.let { this.light = it }
        data.bottomLight?.let { this.bottomLight = it }
        data.topLight?.let { this.topLight = it }
    }


    fun createChunk(session: PlaySession, position: ChunkPosition): Chunk? {
        val blocks = this.blocks ?: return null
        val biomeSource = this.biomeSource ?: return null

        val dimension = session.world.dimension


        val light = this.light
        val chunk = Chunk(session, position, biomeSource)

        for ((index, blockData) in blocks.withIndex()) {
            if (blockData == null) continue
            val section = ChunkSection(index + dimension.minSection, chunk)
            section.blocks.setData(blockData)

            if (!StaticConfiguration.IGNORE_SERVER_LIGHT) {
                light?.get(index)?.let { section.light.update(it) }
            }

            chunk.sections[index] = section
        }
        this.blockEntities.update(dimension.minSection, chunk, null, session)

        chunk.light.heightmap.recalculate()

        if (!StaticConfiguration.IGNORE_SERVER_LIGHT) {
            this.topLight?.let { chunk.light.top.update(it) }
            this.bottomLight?.let { chunk.light.bottom.update(it) }
        }


        return chunk
    }

    private fun Array<Array<BlockState?>?>.update(chunk: Chunk, replace: Boolean, affected: IntOpenHashSet) {
        for ((index, provider) in this.withIndex()) {
            var section = chunk.sections[index]
            val sectionHeight = index - chunk.minSection
            if (provider == null) {
                if (replace && section != null && (!section.blocks.isEmpty || !section.blockEntities.isEmpty)) {
                    section.blocks.clear()
                    section.blockEntities.clear()
                    affected += sectionHeight
                }
                continue
            }
            if (section == null) {
                section = chunk.getOrPut(sectionHeight) ?: continue
            } else {
                section.blockEntities.clear()
            }

            section.blocks.setData(provider)
            affected += sectionHeight
        }
        chunk.light.heightmap.recalculate()
    }

    private fun Map<InChunkPosition, JsonObject>?.update(minSection: Int, chunk: Chunk, affected: IntOpenHashSet?, session: PlaySession) {
        val empty = isNullOrEmpty()
        for ((index, section) in chunk.sections.withIndex()) {
            if (section == null || section.blocks.isEmpty) continue
            val blocks = section.blocks
            val sectionHeight = (index + minSection)
            val yOffset = sectionHeight * ChunkSize.SECTION_HEIGHT_Y

            for (y in blocks.minPosition.y..blocks.maxPosition.y) {
                for (z in blocks.minPosition.z..blocks.maxPosition.z) {
                    for (x in blocks.minPosition.x..blocks.maxPosition.x) {
                        val inSection = InSectionPosition(x, y, z)
                        val block = blocks[inSection]?.block ?: continue
                        if (block !is BlockWithEntity<*>) continue

                        var entity = section.blockEntities[inSection]
                        if (entity == null) {
                            entity = block.createBlockEntity(session) ?: continue
                            section.blockEntities[inSection] = entity
                        }
                        if (!empty) {
                            this!![InChunkPosition(x, y + yOffset, z)]?.let { entity.updateNBT(it) }
                        }
                        affected?.add(sectionHeight)
                    }
                }
            }
        }
    }

    fun updateChunk(chunk: Chunk, replace: Boolean): IntOpenHashSet? {
        val affected = IntOpenHashSet()
        this.blocks?.update(chunk, replace, affected)
        this.blockEntities?.update(chunk.minSection, chunk, affected, chunk.session)

        this.biomeSource?.let { chunk.biomeSource = it } // TODO: invalidate cache

        if (!StaticConfiguration.IGNORE_SERVER_LIGHT) {
            this.topLight?.let { chunk.light.top.update(it) }
            this.light?.forEachIndexed { index, data -> data?.let { chunk.sections[index]?.light?.update(it) } }
            this.bottomLight?.let { chunk.light.bottom.update(it) }
        }

        if (affected.isEmpty()) return null
        return affected
    }
}
