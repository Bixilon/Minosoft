/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.world

import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.biome.accessor.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.container.BlockSectionDataProvider
import de.bixilon.minosoft.data.world.container.SectionDataProvider
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.unsafeCast
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

/**
 * Collection of 16x16x16 blocks
 */
class ChunkSection(
    var blocks: BlockSectionDataProvider = BlockSectionDataProvider(),
    var biomes: SectionDataProvider<Biome> = SectionDataProvider(checkSize = false),
    var blockEntities: SectionDataProvider<BlockEntity?> = SectionDataProvider(checkSize = false),
    var light: ByteArray = ByteArray(ProtocolDefinition.BLOCKS_PER_SECTION), // packed (skyLight: 0xF0, blockLight: 0x0F)
) {

    fun tick(connection: PlayConnection, chunkPosition: Vec2i, sectionHeight: Int) {
        if (blockEntities.isEmpty) {
            return
        }
        acquire()
        var blockEntity: BlockEntity?
        for (index in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
            blockEntity = blockEntities.unsafeGet(index) ?: continue
            val position = Vec3i.of(chunkPosition, sectionHeight, index.indexPosition)
            val blockState = blocks.unsafeGet(index) ?: continue
            blockEntity.tick(connection, blockState, position)
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

    companion object {
        val Vec3i.index: Int
            get() = getIndex(x, y, z)

        val Int.indexPosition: Vec3i
            get() = Vec3i(this and 0x0F, (this shr 8) and 0x0F, (this shr 4) and 0x0F)

        fun getIndex(x: Int, y: Int, z: Int): Int {
            return y shl 8 or (z shl 4) or x
        }
    }

    fun buildBiomeCache(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, neighbours: Array<Chunk>, biomeAccessor: NoiseBiomeAccessor) {
        val chunkPositionX = chunkPosition.x
        val chunkPositionZ = chunkPosition.y
        val blockOffset = Vec3i.of(chunkPosition, sectionHeight)
        val x = blockOffset.x
        val y = blockOffset.y
        val z = blockOffset.z
        val biomes: Array<Biome?> = arrayOfNulls(ProtocolDefinition.BLOCKS_PER_SECTION)
        for (index in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
            biomes[index] = biomeAccessor.getBiome(x + (index and 0x0F), y + ((index shr 8) and 0x0F), z + ((index shr 4) and 0x0F), chunkPositionX, chunkPositionZ, chunk, neighbours) //!!
        }
        this.biomes.setData(biomes.unsafeCast())
    }
}
