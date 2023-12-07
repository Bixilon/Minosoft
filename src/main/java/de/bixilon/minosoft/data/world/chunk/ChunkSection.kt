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
import de.bixilon.kutil.reflection.ReflectionUtil.jvmField
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.biome.accessor.noise.NoiseBiomeAccessor
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
    chunk: Chunk? = null,
    var blocks: BlockSectionDataProvider = BlockSectionDataProvider(chunk?.lock),
    var biomes: SectionDataProvider<Biome> = SectionDataProvider(chunk?.lock, checkSize = false),
    var blockEntities: SectionDataProvider<BlockEntity?> = SectionDataProvider(chunk?.lock, checkSize = true),
) {
    val chunk: Chunk = chunk ?: unsafeNull()
    var light = SectionLight(this)
    var neighbours: Array<ChunkSection?>? = null

    fun tick(connection: PlayConnection, chunkPosition: Vec2i, sectionHeight: Int, random: Random) {
        if (blockEntities.isEmpty) return

        val offset = Vec3i.of(chunkPosition, sectionHeight)
        val position = Vec3i()

        val min = blockEntities.minPosition
        val max = blockEntities.maxPosition
        for (y in min.y..max.y) {
            position.y = offset.y + y
            for (z in min.z..max.z) {
                position.z = offset.z + z
                for (x in min.x..max.x) {
                    val index = getIndex(x, y, z)
                    val entity = blockEntities[index] ?: continue
                    val state = blocks[index] ?: continue
                    position.x = offset.x + x
                    entity.tick(connection, state, position, random)
                }
            }
        }
    }

    fun buildBiomeCache(noise: NoiseBiomeAccessor) {
        val biomes: Array<Biome?> = arrayOfNulls(ProtocolDefinition.BLOCKS_PER_SECTION)
        for (index in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
            biomes[index] = noise.get(index and 0x0F, (index shr 8) and 0x0F, (index shr 4) and 0x0F, chunk)
        }
        this.biomes.setData(biomes.cast())
    }


    fun clear() {
        blocks.clear()
        biomes.clear()
        blockEntities.clear()
    }

    fun updateChunk(chunk: Chunk) {
        CHUNK[this] = chunk
        blocks.lock = chunk.lock
        // biomes?
        blockEntities.lock = chunk.lock
    }

    companion object {
        private val CHUNK = ChunkSection::chunk.jvmField

        inline val Vec3i.index: Int
            get() = getIndex(x, y, z)

        inline val Int.indexPosition: Vec3i
            get() = Vec3i(this and 0x0F, (this shr 8) and 0x0F, (this shr 4) and 0x0F)

        inline fun getIndex(x: Int, y: Int, z: Int): Int {
            return y shl 8 or (z shl 4) or x
        }
    }
}
