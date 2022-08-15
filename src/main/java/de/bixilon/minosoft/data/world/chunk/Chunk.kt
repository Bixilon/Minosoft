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
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSection.Companion.index
import de.bixilon.minosoft.data.world.chunk.light.BorderSectionLight
import de.bixilon.minosoft.data.world.container.BlockSectionDataProvider
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.sectionHeight
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.blocks.chunk.ChunkDataChangeEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.LightChangeEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.chunk.ChunkUtil

/**
 * Collection of chunks sections (from the lowest section to the highest section in y axis)
 */
class Chunk(
    private val connection: PlayConnection,
    val chunkPosition: Vec2i,
    var sections: Array<ChunkSection?>? = null,
    var biomeSource: BiomeSource? = null,
) : Iterable<ChunkSection?>, BiomeAccessor {
    private val world = connection.world
    var bottomLight = BorderSectionLight(false)
    var topLight = BorderSectionLight(true)
    val lowestSection = world.dimension!!.minSection
    val highestSection = world.dimension!!.maxSection
    val cacheBiomes = world.cacheBiomeAccessor != null

    var blocksInitialized = false // All block data was received
    var biomesInitialized = false // All biome data is initialized (aka. cache built, or similar)

    var neighbours: Array<Chunk>? = null
        set(value) {
            if (field.contentEquals(value)) {
                return
            }
            field = value
            if (value != null) {
                updateSectionNeighbours(value)
                recalculateLight()
            }
        }

    val isLoaded: Boolean
        get() = blocksInitialized && biomesInitialized

    val isFullyLoaded: Boolean
        get() = isLoaded && neighbours != null

    init {
        // connection.world.view.updateServerViewDistance(chunkPosition, true)
    }

    operator fun get(sectionHeight: Int): ChunkSection? = sections?.getOrNull(sectionHeight - lowestSection)

    fun unsafeGet(x: Int, y: Int, z: Int): BlockState? {
        return this[y.sectionHeight]?.blocks?.unsafeGet(x, y.inSectionHeight, z)
    }

    fun get(x: Int, y: Int, z: Int): BlockState? {
        return this[y.sectionHeight]?.blocks?.get(x, y.inSectionHeight, z)
    }

    operator fun get(position: Vec3i): BlockState? = get(position.x, position.y, position.z)

    fun unsafeGet(position: Vec3i): BlockState? = unsafeGet(position.x, position.y, position.z)

    fun set(x: Int, y: Int, z: Int, blockState: BlockState?, blockEntity: BlockEntity? = null) {
        val section = getOrPut(y.sectionHeight) ?: return
        val inSectionHeight = y.inSectionHeight
        section[x, inSectionHeight, z] = blockState
        section.blockEntities[x, inSectionHeight, z] = blockEntity
        if (blockEntity == null) {
            getOrPutBlockEntity(x, y, z)
        }

        val neighbours = this.neighbours ?: return

        if (!section.light.update) {
            return
        }
        section.light.update = false
        val sectionHeight = y.sectionHeight

        connection.fireEvent(LightChangeEvent(connection, EventInitiators.CLIENT, chunkPosition, this, sectionHeight, true))


        var neighbourIndex = 0
        for (chunkX in -1..1) {
            for (chunkZ in -1..1) {
                if (chunkX == 0 && chunkZ == 0) {
                    continue
                }
                val chunkPosition = chunkPosition + Vec2i(chunkX, chunkZ)
                for (chunkY in -1..1) {
                    val chunk = neighbours[neighbourIndex]
                    val neighbourSection = chunk[sectionHeight + chunkY] ?: continue
                    if (!neighbourSection.light.update) {
                        continue
                    }
                    neighbourSection.light.update = false
                    connection.fireEvent(LightChangeEvent(connection, EventInitiators.CLIENT, chunkPosition, chunk, sectionHeight + chunkY, false))
                }
                neighbourIndex++
            }
        }
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

    @Synchronized
    private fun initialize(): Array<ChunkSection?> {
        val sections: Array<ChunkSection?> = arrayOfNulls(world.dimension!!.sections)
        this.sections = sections
        return sections
    }


    @Synchronized
    fun setData(data: ChunkData) {
        if (sections == null) {
            initialize()
        }
        data.blocks?.let {
            for ((index, blocks) in it.withIndex()) {
                blocks ?: continue
                val section = getOrPut(index + lowestSection) ?: return@let
                section.blocks = blocks
            }
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
            // ToDo: top and bottom light
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
    private fun getOrPut(sectionHeight: Int): ChunkSection? {
        val sections = sections ?: return null
        val sectionIndex = sectionHeight - lowestSection
        if (sectionIndex < 0 || sectionIndex >= sections.size) {
            return null
        }

        var section = sections[sectionIndex]
        if (section == null) {
            section = ChunkSection(sectionHeight, BlockSectionDataProvider(occlusionUpdateCallback = world.occlusionUpdateCallback))
            val cacheBiomeAccessor = world.cacheBiomeAccessor
            val neighbours = this.neighbours
            if (neighbours != null) {
                if (cacheBiomeAccessor != null && biomesInitialized) {
                    section.buildBiomeCache(chunkPosition, sectionHeight, this, neighbours, cacheBiomeAccessor)
                }
                for (neighbour in neighbours) {
                    val neighbourNeighbours = neighbour.neighbours ?: continue
                    neighbour.updateNeighbours(neighbourNeighbours, sectionHeight)
                }
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
            section.tick(connection, chunkPosition, index + lowestSection)
        }
    }

    fun getLight(position: Vec3i): Int {
        val sectionHeight = position.sectionHeight
        val index = position.inChunkSectionPosition.index
        if (sectionHeight == lowestSection - 1) {
            return bottomLight[index].toInt()
        }
        if (sectionHeight == highestSection + 1) {
            return topLight[index].toInt()
        }
        return this[position.sectionHeight]?.light?.get(index)?.toInt() ?: 0x00
    }

    fun getLight(x: Int, y: Int, z: Int): Int {
        val sectionHeight = y.sectionHeight
        val inSectionHeight = y.inSectionHeight
        val index = inSectionHeight shl 8 or (z shl 4) or x
        if (sectionHeight == lowestSection - 1) {
            return bottomLight[index].toInt()
        }
        if (sectionHeight == highestSection + 1) {
            return topLight[index].toInt()
        }
        return this[sectionHeight]?.light?.get(index)?.toInt() ?: 0x00
    }

    fun buildBiomeCache() {
        val cacheBiomeAccessor = connection.world.cacheBiomeAccessor ?: return
        check(!biomesInitialized) { "Biome cache already initialized!" }
        check(cacheBiomes) { "Cache is disabled!" }
        check(neighbours != null) { "Neighbours not set!" }

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

    fun recalculateLight() {
        val sections = sections ?: Broken("Sections is null")
        for (section in sections) {
            if (section == null) {
                continue
            }
            section.light.recalculate()
        }
    }

    private fun updateSectionNeighbours(neighbours: Array<Chunk>) {
        for ((index, section) in sections!!.withIndex()) {
            if (section == null) {
                continue
            }
            val sectionNeighbours = ChunkUtil.getDirectNeighbours(neighbours, this, index + lowestSection)
            section.neighbours = sectionNeighbours
        }
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

    fun getWorld(offset: Vec3i, origin: Vec3i, blockPosition: Vec3i = origin + offset): BlockState? {
        val originChunkPosition = origin.chunkPosition
        val targetChunkPosition = blockPosition.chunkPosition

        val deltaChunkPosition = targetChunkPosition - originChunkPosition

        return getWorld(blockPosition.inChunkPosition, deltaChunkPosition)
    }

    private fun getWorld(inChunkSectionPosition: Vec3i, chunkOffset: Vec2i): BlockState? {
        if (chunkOffset.x == 0 && chunkOffset.y == 0) {
            return this[inChunkSectionPosition]
        }
        val neighbours = this.neighbours ?: return null

        if (chunkOffset.x > 0) {
            chunkOffset.x--
            return neighbours[6].getWorld(inChunkSectionPosition, chunkOffset)
        }
        if (chunkOffset.x < 0) {
            chunkOffset.x++
            return neighbours[1].getWorld(inChunkSectionPosition, chunkOffset)
        }
        if (chunkOffset.y > 0) {
            chunkOffset.y--
            return neighbours[4].getWorld(inChunkSectionPosition, chunkOffset)
        }
        if (chunkOffset.y < 0) {
            chunkOffset.y++
            return neighbours[3].getWorld(inChunkSectionPosition, chunkOffset)
        }

        Broken("Can not get chunk from offset: $chunkOffset")
    }
}

