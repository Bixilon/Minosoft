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
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.light.types.LightArray
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkDataUpdate
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class ChunkData(
    var blocks: Array<Array<BlockState?>?>? = null,
    var entities: Int2ObjectOpenHashMap<JsonObject>? = null,
    var biomeSource: BiomeSource? = null,
    var light: Array<LightArray?>? = null,
    var bottomLight: LightArray? = null,
    var topLight: LightArray? = null,
) {

    @Deprecated("useless")
    fun update(data: ChunkData) {
        data.blocks?.let { this.blocks = it }
        data.entities?.let { this.entities = it }
        data.biomeSource?.let { this.biomeSource = it }
        data.light?.let { this.light = it }
        data.bottomLight?.let { this.bottomLight = it }
        data.topLight?.let { this.topLight = it }
    }


    private fun Chunk.update(blocks: Array<Array<BlockState?>?>, replace: Boolean, affected: MutableSet<ChunkSection>?) {
        val minSection = world.dimension.minSection
        for ((index, data) in blocks.withIndex()) {
            val height = index + minSection
            var section = this.sections[height]

            if (data == null) {
                if (replace && section != null) {
                    section.clear()
                    affected?.add(section)
                }
                continue
            }

            if (section == null) {
                section = this.sections.create(height, false) ?: continue
            }
            section.blocks.clear()
            section.entities.clear()

            section.blocks.setData(data)

            affected?.add(section)
        }

        this.light.heightmap.recalculate()
    }

    private fun Chunk.update(data: Int2ObjectMap<JsonObject>?, affected: MutableSet<ChunkSection>?) {
        sections.forEach { section ->
            if (data == null && affected != null && section !in affected) return@forEach
            val blocks = section.blocks
            val yOffset = section.height * ChunkSize.SECTION_HEIGHT_Y

            blocks.forEach { position, state ->
                if (BlockStateFlags.ENTITY !in state.flags) return@forEach

                val entity = section.entities.update(position) ?: return@forEach
                val inChunk = InChunkPosition(position.x, position.y + yOffset, position.z)
                data?.get(inChunk.raw)?.let { entity.updateNBT(it) }

                affected?.add(section)
            }
        }
    }

    private fun Chunk.update(replace: Boolean, affected: MutableSet<ChunkSection>?) {
        val blocks = this@ChunkData.blocks
        val entities = this@ChunkData.entities

        blocks?.let { update(it, replace, affected) }
        if (blocks != null || entities != null) { // this operation is expensive, only do it if data has changed
            update(entities, affected)
        }

        this@ChunkData.biomeSource?.let {
            biomeSource = it
            sections.forEach { section ->
                if (section.biomes.isEmpty) return@forEach
                section.biomes.clear()
                affected?.add(section)
            }
        }

        if (!StaticConfiguration.IGNORE_SERVER_LIGHT) {
            this@ChunkData.topLight?.let { light.top.update(it) }
            // TODO: section light, update affected
            this@ChunkData.bottomLight?.let { light.bottom.update(it) }
        }
    }


    fun update(chunk: Chunk, replace: Boolean) {
        val affected: MutableSet<ChunkSection> = HashSet(5)

        chunk.update(replace, affected)

        if (affected.isEmpty()) return

        ChunkDataUpdate(chunk, affected).fire(chunk.world.session)
    }
}
