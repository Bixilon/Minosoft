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
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.world.ChunkSection.Companion.index
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.ChunkDataChangeEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.unsafeCast
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

/**
 * Collection of chunks sections (from the lowest section to the highest section in y axis)
 */
class Chunk(
    private val connection: PlayConnection,
    private val chunkPosition: Vec2i,
    private var sections: Array<ChunkSection?>? = null,
    var biomeSource: BiomeSource? = null,
) : Iterable<ChunkSection?>, BiomeAccessor {
    private val world = connection.world
    var bottomLight: ByteArray? = null
    var topLight: ByteArray? = null
    val lowestSection = world.dimension!!.lowestSection
    val highestSection = world.dimension!!.highestSection
    val cacheBiomes = world.cacheBiomeAccessor != null

    var blocksInitialized = false // All block data was received
    var biomesInitialized = false // All biome data is initialized (aka. cache built, or similar)
    var lightInitialized = false
    var neighboursLoaded = false

    val isLoaded: Boolean
        get() = blocksInitialized && biomesInitialized && lightInitialized

    val isFullyLoaded: Boolean
        get() = isLoaded && neighboursLoaded

    operator fun get(sectionHeight: Int): ChunkSection? = sections?.getOrNull(sectionHeight - lowestSection)

    fun get(x: Int, y: Int, z: Int): BlockState? {
        return this[y.sectionHeight]?.blocks?.get(x, y.inSectionHeight, z)
    }

    operator fun get(position: Vec3i): BlockState? = get(position.x, position.y, position.z)

    fun set(x: Int, y: Int, z: Int, blockState: BlockState?, blockEntity: BlockEntity? = null) {
        val section = getOrPut(y.sectionHeight)
        section.blocks[x, y.inSectionHeight, z] = blockState
        section.blockEntities[x, y.inSectionHeight, z] = blockEntity // ToDo
    }

    operator fun set(position: Vec3i, blockState: BlockState?) = set(position.x, position.y, position.z, blockState)

    fun setBlocks(blocks: Map<Vec3i, BlockState?>) {
        for ((location, blockState) in blocks) {
            set(location, blockState)
        }
    }

    fun getBlockEntity(x: Int, y: Int, z: Int): BlockEntity? {
        return this[y.sectionHeight]?.blockEntities?.get(x, y.inSectionHeight, z)
    }

    fun getBlockEntity(position: Vec3i): BlockEntity? = getBlockEntity(position.x, position.y, position.z)

    fun setBlockEntity(x: Int, y: Int, z: Int, blockEntity: BlockEntity?) {
        getOrPut(y.sectionHeight).blockEntities[x, y.inSectionHeight, z] = blockEntity
    }

    fun setBlockEntity(position: Vec3i, blockEntity: BlockEntity?) = setBlockEntity(position.x, position.y, position.z, blockEntity)

    @Synchronized
    private fun initialize(): Array<ChunkSection?> {
        val sections: Array<ChunkSection?> = arrayOfNulls(world.dimension!!.sections)
        this.sections = sections
        return sections
    }


    @Synchronized
    fun setData(data: ChunkData, merge: Boolean = true) {
        if (sections == null || !merge) {
            initialize()
        }
        data.blocks?.let {
            for ((index, blocks) in it.withIndex()) {
                blocks ?: continue
                val section = getOrPut(index - lowestSection)
                section.blocks = blocks
            }
            blocksInitialized = true
        }
        data.blockEntities?.let {
            for ((position, blockEntity) in it) {
                setBlockEntity(position, blockEntity)
            }
        }
        data.light?.let {
            for ((index, light) in it.withIndex()) {
                light ?: continue
                val section = getOrPut(index - lowestSection)
                section.light = light
            }
            lightInitialized = true
        }
        data.bottomLight?.let {
            bottomLight = it
            lightInitialized = true
        }
        data.topLight?.let {
            topLight = it
            lightInitialized = true
        }
        data.biomeSource?.let {
            this.biomeSource = it
            if (!cacheBiomes) {
                biomesInitialized = true
            }
        }
        world.onChunkUpdate(chunkPosition, this)
        connection.fireEvent(ChunkDataChangeEvent(connection, EventInitiators.UNKNOWN, chunkPosition, this))
    }

    @Synchronized
    private fun getOrPut(sectionHeight: Int): ChunkSection {
        val sections = sections ?: throw NullPointerException("Sections not initialized yet!")
        val sectionIndex = sectionHeight - lowestSection

        var section = sections[sectionIndex]
        if (section == null) {
            section = ChunkSection(connection.registries)
            val neighbours: Array<Chunk> = world.getChunkNeighbours(chunkPosition).unsafeCast()
            val cacheBiomeAccessor = world.cacheBiomeAccessor
            if (cacheBiomeAccessor != null && biomesInitialized && neighboursLoaded) {
                section.buildBiomeCache(chunkPosition, sectionHeight, this, neighbours, cacheBiomeAccessor)
            }
            sections[sectionIndex] = section
        }
        return section
    }

    fun tick(connection: PlayConnection, chunkPosition: Vec2i) {
        if (!isFullyLoaded) {
            return
        }
        val sections = sections!!
        for ((index, section) in sections.withIndex()) {
            section ?: continue
            section.tick(connection, chunkPosition, index - lowestSection)
        }
    }

    fun getLight(position: Vec3i): Int {
        val sectionHeight = position.sectionHeight
        val index = position.inChunkSectionPosition.index
        if (sectionHeight == lowestSection - 1) {
            return bottomLight?.get(index)?.toInt() ?: 0xFF
        }
        if (sectionHeight == highestSection + 1) {
            return topLight?.get(index)?.toInt() ?: 0xFF
        }
        return get(position.sectionHeight)?.light?.get(index)?.toInt() ?: 0xFF
    }

    fun buildBiomeCache() {
        val cacheBiomeAccessor = connection.world.cacheBiomeAccessor ?: return
        check(!biomesInitialized) { "Biome cache already initialized!" }
        check(cacheBiomes) { "Cache is disabled!" }
        check(neighboursLoaded)

        // ToDo: Return if isEmpty

        val neighbours: Array<Chunk> = connection.world.getChunkNeighbours(chunkPosition).unsafeCast()
        for ((sectionIndex, section) in sections!!.withIndex()) {
            section ?: continue
            val sectionHeight = sectionIndex + lowestSection
            section.buildBiomeCache(chunkPosition, sectionHeight, this, neighbours, cacheBiomeAccessor)
        }
        biomesInitialized = true
    }

    override fun iterator(): Iterator<ChunkSection?> {
        return sections!!.iterator()
    }

    override fun getBiome(x: Int, y: Int, z: Int): Biome? {
        if (cacheBiomes) {
            val section = this[y.sectionHeight] ?: return connection.world.cacheBiomeAccessor?.getBiome((chunkPosition.x shl 4) or x, y, (chunkPosition.y shl 4) or z, chunkPosition.x, chunkPosition.y, this, null)
            return section.biomes[x, y.inSectionHeight, z]
        }
        return biomeSource?.getBiome(x and 0x0F, y, z and 0x0F)
    }
}
