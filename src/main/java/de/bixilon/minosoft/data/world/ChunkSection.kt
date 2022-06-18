/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.biome.accessor.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.container.BlockSectionDataProvider
import de.bixilon.minosoft.data.world.container.SectionDataProvider
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

/**
 * Collection of 16x16x16 blocks
 */
class ChunkSection(
    blocks: BlockSectionDataProvider,
    var biomes: SectionDataProvider<Biome> = SectionDataProvider(checkSize = false),
    var blockEntities: SectionDataProvider<BlockEntity?> = SectionDataProvider(checkSize = false),
    var light: ByteArray = ByteArray(ProtocolDefinition.BLOCKS_PER_SECTION) { 0x00.toByte() }, // packed (skyLight: 0xF0, blockLight: 0x0F)
) {
    var blocks = blocks
        set(value) {
            field = value
            recalculateLight()
        }

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

    fun onLightDecrease(x: Int, y: Int, z: Int, luminance: Byte) {
        // ToDo: make faster, set light to 0 and trace to next night increase. then backtrace
        recalculateLight()
    }

    fun onLightIncrease(x: Int, y: Int, z: Int, luminance: Byte) {
        traceLightIncrease(x, y, z, luminance)
    }


    private fun traceLightIncrease(x: Int, y: Int, z: Int, nextLuminance: Byte) {
        val index = getIndex(x, y, z)
        val block = blocks.unsafeGet(index)
        if (block != null && block.luminance == 0.toByte() && block.isSolid) {
            // light can not pass through the block
            return
        }
        // get block or next luminance level
        var luminance = nextLuminance
        val currentLuminance = block?.luminance ?: nextLuminance
        if (currentLuminance > luminance) {
            luminance = currentLuminance
        }
        val currentLight = light[index].toInt() and 0x0F // we just care about block light
        if (currentLight >= luminance) {
            // light is already higher, no need to trace
            return
        }
        light[index] = luminance

        val neighbourLuminance = (luminance - 1).toByte()
        if (neighbourLuminance == 0.toByte()) {
            // luminance is 1, we can not further increase the light
            return
        }

        // ToDo: check neighbours and trace there
        if (x > 0) traceLightIncrease(x - 1, y, z, neighbourLuminance)
        if (x < ProtocolDefinition.SECTION_MAX_X) traceLightIncrease(x + 1, y, z, neighbourLuminance)
        if (y > 0) traceLightIncrease(x, y - 1, z, neighbourLuminance)
        if (y < ProtocolDefinition.SECTION_MAX_Y) traceLightIncrease(x, y + 1, z, neighbourLuminance)
        if (z > 0) traceLightIncrease(x, y, z - 1, neighbourLuminance)
        if (z < ProtocolDefinition.SECTION_MAX_Y) traceLightIncrease(x, y, z + 1, neighbourLuminance)
    }


    fun recalculateLight() {
        // clear light
        for (index in light.indices) {
            light[index] = 0x00.toByte()
        }

        blocks.acquire()
        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                    val index = getIndex(x, y, z)
                    val luminance = blocks.unsafeGet(index)?.luminance ?: continue
                    if (luminance == 0.toByte()) {
                        // block is not emitting light, ignore it
                        continue
                    }
                    traceLightIncrease(x, y, z, luminance)
                }
            }
        }
        blocks.release()
    }
}
