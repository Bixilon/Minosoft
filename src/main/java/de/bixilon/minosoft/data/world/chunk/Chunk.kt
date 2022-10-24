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
package de.bixilon.minosoft.data.world.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.lock.thread.ThreadLock
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.chunk.light.ChunkLight
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.container.BlockSectionDataProvider
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkPosition
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.blocks.chunk.ChunkDataChangeEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.chunk.ChunkUtil

/**
 * Collection of chunks sections (from the lowest section to the highest section in y axis)
 */
class Chunk(
    val connection: PlayConnection,
    val chunkPosition: ChunkPosition,
    var sections: Array<ChunkSection?>? = null,
    var biomeSource: BiomeSource? = null,
) : Iterable<ChunkSection?>, BiomeAccessor {
    val lock = ThreadLock()
    val world = connection.world
    val light = ChunkLight(this)
    val lowestSection = world.dimension!!.minSection
    val highestSection = world.dimension!!.maxSection
    val cacheBiomes = world.cacheBiomeAccessor != null

    var blocksInitialized = false // All block data was received
    var biomesInitialized = false // All biome data is initialized (aka. cache built, or similar)

    val neighbours = ChunkNeighbours(this)

    val isLoaded: Boolean
        get() = blocksInitialized && biomesInitialized

    val isFullyLoaded: Boolean
        get() = isLoaded && neighbours.complete

    operator fun get(sectionHeight: SectionHeight): ChunkSection? = sections?.getOrNull(sectionHeight - lowestSection)

    fun unsafeGet(x: Int, y: Int, z: Int): BlockState? {
        return this[y.sectionHeight]?.blocks?.unsafeGet(x, y.inSectionHeight, z)
    }

    operator fun get(x: Int, y: Int, z: Int): BlockState? {
        return this[y.sectionHeight]?.blocks?.get(x, y.inSectionHeight, z)
    }

    operator fun get(position: InChunkPosition): BlockState? = get(position.x, position.y, position.z)

    fun unsafeGet(position: Vec3i): BlockState? = unsafeGet(position.x, position.y, position.z)

    fun set(x: Int, y: Int, z: Int, blockState: BlockState?, blockEntity: BlockEntity? = null) {
        val section = getOrPut(y.sectionHeight) ?: return
        val inSectionHeight = y.inSectionHeight
        section[x, inSectionHeight, z] = blockState

        section.blockEntities[x, inSectionHeight, z] = blockEntity
        if (blockEntity == null) {
            getOrPutBlockEntity(x, y, z)
        }
        // ToDo: Remove section if isEmpty

        light.onBlockChange(x, y, z, section, blockState)
    }

    operator fun set(position: Vec3i, blockState: BlockState?) = set(position.x, position.y, position.z, blockState)

    fun setBlocks(blocks: Map<Vec3i, BlockState?>) {
        for ((position, blockState) in blocks) {
            set(position, blockState)
        }
    }

    fun getBlockEntity(x: Int, y: Int, z: Int): BlockEntity? {
        return this[y.sectionHeight]?.blockEntities?.get(x, y.inSectionHeight, z)
    }

    fun getOrPutBlockEntity(x: Int, y: Int, z: Int): BlockEntity? {
        val sectionHeight = y.sectionHeight
        val inSectionHeight = y.inSectionHeight
        var blockEntity = this[sectionHeight]?.blockEntities?.get(x, inSectionHeight, z)
        if (blockEntity != null) {
            return blockEntity
        }
        val block = this[sectionHeight]?.blocks?.get(x, inSectionHeight, z) ?: return null
        if (block.block !is BlockWithEntity<*>) {
            return null
        }
        blockEntity = block.block.factory?.build(connection) ?: return null
        (this.getOrPut(sectionHeight) ?: return null).blockEntities[x, inSectionHeight, z] = blockEntity

        return blockEntity
    }

    fun getBlockEntity(position: Vec3i): BlockEntity? = getBlockEntity(position.x, position.y, position.z)
    fun getOrPutBlockEntity(position: Vec3i): BlockEntity? = getOrPutBlockEntity(position.x, position.y, position.z)

    fun setBlockEntity(x: Int, y: Int, z: Int, blockEntity: BlockEntity?) {
        (getOrPut(y.sectionHeight) ?: return).blockEntities[x, y.inSectionHeight, z] = blockEntity
    }

    fun setBlockEntity(position: Vec3i, blockEntity: BlockEntity?) = setBlockEntity(position.x, position.y, position.z, blockEntity)

    private fun initialize() {
        lock.lock()
        this.sections = arrayOfNulls(world.dimension!!.sections)
        lock.unlock()
    }


    fun setData(data: ChunkData) {
        lock.lock()
        if (sections == null) {
            initialize()
        }
        data.blocks?.let {
            for ((index, blocks) in it.withIndex()) {
                blocks ?: continue
                val section = getOrPut(index + lowestSection) ?: return@let
                section.blocks = blocks
            }
            light.recalculateHeightmap()
            blocksInitialized = true
        }
        data.blockEntities?.let {
            for ((position, blockEntity) in it) {
                setBlockEntity(position, blockEntity)
            }
        }
        if (!StaticConfiguration.IGNORE_SERVER_LIGHT) {
            data.light?.let {
                for ((index, light) in it.withIndex()) {
                    light ?: continue
                    val section = getOrPut(index + lowestSection) ?: return@let
                    section.light.light = light
                }
            }
            data.bottomLight?.let { light.bottom.update(it) }
            data.topLight?.let { light.top.update(it) }
        }
        data.biomeSource?.let {
            this.biomeSource = it
            if (!cacheBiomes) {
                biomesInitialized = true
            }
        }
        lock.unlock()
        world.onChunkUpdate(chunkPosition, this)
        connection.fireEvent(ChunkDataChangeEvent(connection, EventInitiators.UNKNOWN, chunkPosition, this))
    }

    fun getOrPut(sectionHeight: Int, calculateLight: Boolean = true): ChunkSection? {
        lock.lock()
        val sections = sections
        if (sections == null) {
            lock.unlock()
            return null
        }
        val sectionIndex = sectionHeight - lowestSection
        if (sectionIndex < 0 || sectionIndex >= sections.size) {
            lock.unlock()
            return null
        }

        var section = sections[sectionIndex]
        if (section == null) {
            section = ChunkSection(sectionHeight, BlockSectionDataProvider(occlusionUpdateCallback = world.occlusionUpdateCallback), chunk = this)
            val cacheBiomeAccessor = world.cacheBiomeAccessor
            val neighbours = this.neighbours.get()
            if (neighbours != null) {
                if (cacheBiomeAccessor != null && biomesInitialized) {
                    section.buildBiomeCache(chunkPosition, sectionHeight, this, neighbours, cacheBiomeAccessor)
                }
                section.neighbours = ChunkUtil.getDirectNeighbours(neighbours, this, sectionHeight)
                for (neighbour in neighbours) {
                    val neighbourNeighbours = neighbour.neighbours.get() ?: continue
                    neighbour.updateNeighbours(neighbourNeighbours, sectionHeight)
                }
            }

            sections[sectionIndex] = section

            if (sectionIndex > 0) {
                sections[sectionIndex - 1]?.neighbours?.set(Directions.O_UP, section)
            }
            val highestIndex = highestSection - 1
            if (sectionIndex < highestIndex) {
                sections[sectionIndex + 1]?.neighbours?.set(Directions.O_DOWN, section)
            }

            // check light of neighbours to check if their light needs to be traced into our own chunk
            if (calculateLight) {
                section.light.propagateFromNeighbours()
            }
        }
        lock.unlock()
        return section
    }

    fun tick(connection: PlayConnection, chunkPosition: Vec2i) {
        if (!isFullyLoaded) {
            return
        }
        val sections = sections!!
        for ((index, section) in sections.withIndex()) {
            section ?: continue
            section.tick(connection, chunkPosition, index + lowestSection)
        }
    }

    fun buildBiomeCache() {
        val cacheBiomeAccessor = connection.world.cacheBiomeAccessor ?: return
        check(!biomesInitialized) { "Biome cache already initialized!" }
        check(cacheBiomes) { "Cache is disabled!" }
        check(neighbours.complete) { "Neighbours not set!" }

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

    private fun updateNeighbours(neighbours: Array<Chunk>, sectionHeight: Int) {
        for (nextSectionHeight in sectionHeight - 1..sectionHeight + 1) {
            if (nextSectionHeight < lowestSection || nextSectionHeight > highestSection) {
                continue
            }

            val section = this[sectionHeight] ?: return
            val sectionNeighbours = ChunkUtil.getDirectNeighbours(neighbours, this, nextSectionHeight)
            section.neighbours = sectionNeighbours
        }
    }

    fun traceBlock(offset: Vec3i, origin: Vec3i, blockPosition: Vec3i = origin + offset): BlockState? {
        val originChunkPosition = origin.chunkPosition
        val targetChunkPosition = blockPosition.chunkPosition

        val deltaChunkPosition = targetChunkPosition - originChunkPosition

        return traceBlock(blockPosition.inChunkPosition, deltaChunkPosition)
    }

    fun traceChunk(offset: Vec2i): Chunk? {
        if (offset.x == 0 && offset.y == 0) {
            return this
        }

        if (offset.x > 0) {
            offset.x--
            return neighbours[6]?.traceChunk(offset)
        }
        if (offset.x < 0) {
            offset.x++
            return neighbours[1]?.traceChunk(offset)
        }
        if (offset.y > 0) {
            offset.y--
            return neighbours[4]?.traceChunk(offset)
        }
        if (offset.y < 0) {
            offset.y++
            return neighbours[3]?.traceChunk(offset)
        }

        Broken("Can not get chunk from offset: $offset")
    }

    private fun traceBlock(inChunkPosition: Vec3i, chunkOffset: Vec2i): BlockState? {
        return traceChunk(chunkOffset)?.get(inChunkPosition)
    }
}


