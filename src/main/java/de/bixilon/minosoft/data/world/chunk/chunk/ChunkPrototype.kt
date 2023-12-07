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

package de.bixilon.minosoft.data.world.chunk.chunk

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class ChunkPrototype(
    var blocks: Array<Array<BlockState?>?>? = null,
    var blockEntities: Map<Vec3i, JsonObject>? = null,
    var biomeSource: BiomeSource? = null,
    var light: Array<ByteArray?>? = null,
    var bottomLight: ByteArray? = null,
    var topLight: ByteArray? = null,
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


    fun createChunk(connection: PlayConnection, position: ChunkPosition): Chunk? {
        val blocks = this.blocks ?: return null
        val biomeSource = this.biomeSource ?: return null

        val dimension = connection.world.dimension


        val light = this.light
        val chunk = Chunk(connection, position, biomeSource)

        for ((index, blockData) in blocks.withIndex()) {
            if (blockData == null) continue
            val section = ChunkSection(index + dimension.minSection, chunk)
            section.blocks.setData(blockData)

            if (!StaticConfiguration.IGNORE_SERVER_LIGHT) {
                light?.get(index)?.let { section.light.light = it }
            }

            chunk.sections[index] = section
        }
        this.blockEntities.update(dimension.minSection, chunk, null, connection)

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
    }

    private fun Map<Vec3i, JsonObject>?.update(minSection: Int, chunk: Chunk, affected: IntOpenHashSet?, connection: PlayConnection) {
        val position = Vec3i()
        for ((index, section) in chunk.sections.withIndex()) {
            if (section == null || section.blocks.isEmpty) continue
            val blocks = section.blocks
            val sectionHeight = (index + minSection)
            val yOffset = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y

            for (y in blocks.minPosition.y..blocks.maxPosition.y) {
                for (z in blocks.minPosition.z..blocks.maxPosition.z) {
                    for (x in blocks.minPosition.x..blocks.maxPosition.x) {
                        val index = ChunkSection.getIndex(x, y, z)
                        val block = blocks[index]?.block ?: continue
                        if (block !is BlockWithEntity<*>) continue

                        if (this != null) {
                            position.x = x; position.y = yOffset + y; position.z = z
                        }
                        var entity = section.blockEntities[index]
                        if (entity == null) {
                            entity = block.createBlockEntity(connection) ?: continue
                            section.blockEntities[index] = entity
                        }
                        this?.get(position)?.let { entity.updateNBT(it) }
                        affected?.add(sectionHeight)
                    }
                }
            }
        }
    }

    fun updateChunk(chunk: Chunk, replace: Boolean): IntOpenHashSet? {
        val affected = IntOpenHashSet()
        this.blocks?.update(chunk, replace, affected)
        this.blockEntities?.update(chunk.minSection, chunk, affected, chunk.connection)

        this.biomeSource?.let { chunk.biomeSource = it } // TODO: invalidate cache

        if (!StaticConfiguration.IGNORE_SERVER_LIGHT) {
            this.topLight?.let { chunk.light.top.update(it) }
            // TODO: section light, update affected
            this.bottomLight?.let { chunk.light.bottom.update(it) }
        }

        if (affected.isEmpty()) return null
        return affected
    }
}
