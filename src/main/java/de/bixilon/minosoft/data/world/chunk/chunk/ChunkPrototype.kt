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
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.light.types.LightArray
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class ChunkPrototype(
    var blocks: Array<Array<BlockState?>?>? = null,
    var entities: Int2ObjectOpenHashMap<JsonObject>? = null,
    var biomeSource: BiomeSource? = null,
    var light: Array<LightArray?>? = null,
    var bottomLight: LightArray? = null,
    var topLight: LightArray? = null,
) {

    @Synchronized
    fun update(data: ChunkPrototype) {
        data.blocks?.let { this.blocks = it }
        data.entities?.let { this.entities = it }
        data.biomeSource?.let { this.biomeSource = it }
        data.light?.let { this.light = it }
        data.bottomLight?.let { this.bottomLight = it }
        data.topLight?.let { this.topLight = it }
    }


    fun create(session: PlaySession, position: ChunkPosition): Chunk? {
        if (this.blocks == null) return null

        val chunk = Chunk(session, position)
        chunk.update(true, null)


        return chunk
    }

    fun Chunk.update(blocks: Array<Array<BlockState?>?>, replace: Boolean, affected: IntOpenHashSet?) {
        val minSection = world.dimension.minSection
        for ((index, data) in blocks.withIndex()) {
            val height = index + minSection
            var section = this.sections[height]

            if (data == null) {
                if (replace) {
                    section?.clear()
                    affected?.add(height)
                }
                continue
            }

            if (section == null) {
                section = this.sections.create(height, false) ?: continue
            }
            section.blocks.clear()
            section.entities.clear()

            section.blocks.setData(data)

            affected?.add(height)
        }

        this.light.heightmap.recalculate()
    }

    private fun Chunk.update(data: Int2ObjectMap<JsonObject>?, affected: IntOpenHashSet?) {
        sections.forEach { section ->
            if (data == null && affected != null && section.height !in affected) return@forEach
            val blocks = section.blocks
            val yOffset = section.height * ChunkSize.SECTION_HEIGHT_Y

            blocks.forEach { position, state ->
                if (BlockStateFlags.ENTITY !in state.flags) return@forEach

                val entity = section.entities.update(position) ?: return@forEach
                val inChunk = InChunkPosition(position.x, position.y + yOffset, position.z)
                data?.get(inChunk.raw)?.let { entity.updateNBT(it) }

                affected?.add(section.height)
            }
        }
    }

    private fun Chunk.update(replace: Boolean, affected: IntOpenHashSet?) {
        val blocks = this@ChunkPrototype.blocks
        val entities = this@ChunkPrototype.entities

        blocks?.let { update(it, replace, affected) }
        if (blocks != null || entities != null) { // this operation is expensive, only do it if data has changed
            update(entities, affected)
        }

        this@ChunkPrototype.biomeSource?.let { biomeSource = it } // TODO: invalidate cache

        if (!StaticConfiguration.IGNORE_SERVER_LIGHT) {
            this@ChunkPrototype.topLight?.let { light.top.update(it) }
            // TODO: section light, update affected
            this@ChunkPrototype.bottomLight?.let { light.bottom.update(it) }
        }
    }


    fun update(chunk: Chunk, replace: Boolean): IntOpenHashSet? {
        val affected = IntOpenHashSet(5)

        chunk.update(replace, affected)

        if (affected.isEmpty()) return null

        return affected
    }
}
