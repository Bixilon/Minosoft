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
package de.bixilon.minosoft.data.world.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.biome.accessor.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.SectionLight
import de.bixilon.minosoft.data.world.container.SectionDataProvider
import de.bixilon.minosoft.data.world.container.block.BlockSectionDataProvider
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.util.*

/**
 * Collection of 16x16x16 blocks
 */
class ChunkSection(
    val sectionHeight: Int,
    var blocks: BlockSectionDataProvider = BlockSectionDataProvider(),
    var biomes: SectionDataProvider<Biome> = SectionDataProvider(checkSize = false),
    var blockEntities: SectionDataProvider<BlockEntity?> = SectionDataProvider(checkSize = false),
    chunk: Chunk? = null
) {
    val chunk: Chunk = chunk ?: unsafeNull()
    var light = SectionLight(this)
    var neighbours: Array<ChunkSection?>? = null

    fun tick(connection: PlayConnection, chunkPosition: Vec2i, sectionHeight: Int, random: Random) {
        if (blockEntities.isEmpty) {
            return
        }
        acquire()
        val min = blockEntities.minPosition
        val max = blockEntities.maxPosition
        for (y in min.y..max.y) {
            for (z in min.z..max.z) {
                for (x in min.x..max.x) {
                    val index = getIndex(x, y, z)
                    val entity = blockEntities[index] ?: continue
                    val state = blocks[index] ?: continue
                    val position = Vec3i.of(chunkPosition, sectionHeight, index.indexPosition)
                    entity.tick(connection, state, position, random)
                }
            }
        }
        release()
    }

    fun acquire() {
        blocks.acquire()
        biomes.acquire()
        blockEntities.acquire()
    }

    fun release() {
        blocks.release()
        biomes.release()
        blockEntities.release()
    }

    fun lock() {
        blocks.lock()
        biomes.lock()
        blockEntities.lock()
    }

    fun unlock() {
        blocks.unlock()
        biomes.unlock()
        blockEntities.unlock()
    }

    fun buildBiomeCache(neighbours: Array<Chunk>, biomeAccessor: NoiseBiomeAccessor) {
        val chunkPositionX = chunk.chunkPosition.x
        val chunkPositionZ = chunk.chunkPosition.y
        val blockOffset = Vec3i.of(chunk.chunkPosition, sectionHeight)
        val x = blockOffset.x
        val y = blockOffset.y
        val z = blockOffset.z
        val biomes: Array<Biome?> = arrayOfNulls(ProtocolDefinition.BLOCKS_PER_SECTION)
        for (index in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
            biomes[index] = biomeAccessor.getBiome(x + (index and 0x0F), y + ((index shr 8) and 0x0F), z + ((index shr 4) and 0x0F), chunkPositionX, chunkPositionZ, chunk, neighbours)
        }
        this.biomes.setData(biomes.cast())
    }


    fun clear() {
        blocks.clear()
        biomes.clear()
        blockEntities.clear()
    }

    companion object {

        inline val Vec3i.index: Int
            get() = getIndex(x, y, z)

        inline val Int.indexPosition: Vec3i
            get() = Vec3i(this and 0x0F, (this shr 8) and 0x0F, (this shr 4) and 0x0F)

        inline fun getIndex(x: Int, y: Int, z: Int): Int {
            return y shl 8 or (z shl 4) or x
        }
    }
}
