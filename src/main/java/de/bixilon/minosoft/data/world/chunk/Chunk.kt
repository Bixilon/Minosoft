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
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.chunk.light.BorderSectionLight
import de.bixilon.minosoft.data.world.container.BlockSectionDataProvider
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkPosition
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.blocks.chunk.ChunkDataChangeEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.LightChangeEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
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
    var bottomLight = BorderSectionLight(false, this)
    var topLight = BorderSectionLight(true, this)
    val lowestSection = world.dimension!!.minSection
    val highestSection = world.dimension!!.maxSection
    val cacheBiomes = world.cacheBiomeAccessor != null

    var blocksInitialized = false // All block data was received
    var biomesInitialized = false // All biome data is initialized (aka. cache built, or similar)

    val heightmap = IntArray(ProtocolDefinition.SECTION_WIDTH_X * ProtocolDefinition.SECTION_WIDTH_Z)

    var neighbours: Array<Chunk>? = null
        @Synchronized set(value) {
            if (field.contentEquals(value)) {
                return
            }
            field = value
            if (value != null) {
                updateSectionNeighbours(value)
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

    operator fun get(x: Int, y: Int, z: Int): BlockState? {
        return this[y.sectionHeight]?.blocks?.get(x, y.inSectionHeight, z)
    }

    operator fun get(position: Vec3i): BlockState? = get(position.x, position.y, position.z)

    fun unsafeGet(position: Vec3i): BlockState? = unsafeGet(position.x, position.y, position.z)

    fun set(x: Int, y: Int, z: Int, blockState: BlockState?, blockEntity: BlockEntity? = null) {
        val section = getOrPut(y.sectionHeight) ?: return
        val inSectionHeight = y.inSectionHeight
        section[x, inSectionHeight, z] = blockState
        updateHeightmap(x, y, z, blockState != null)

        section.blockEntities[x, inSectionHeight, z] = blockEntity
        if (blockEntity == null) {
            getOrPutBlockEntity(x, y, z)
        }
        // ToDo: Remove section if isEmpty

        val neighbours = this.neighbours ?: return

        if (!section.light.update) {
            return
        }
        section.light.update = false
        val sectionHeight = y.sectionHeight

        connection.fireEvent(LightChangeEvent(connection, EventInitiators.CLIENT, chunkPosition, this, sectionHeight, true))

        val down = section.neighbours?.get(Directions.O_DOWN)?.light
        if (down != null && down.update) {
            down.update = false
            connection.fireEvent(LightChangeEvent(connection, EventInitiators.CLIENT, chunkPosition, this, sectionHeight - 1, false))
        }
        val up = section.neighbours?.get(Directions.O_UP)?.light
        if (up?.update == true) {
            up.update = false
            connection.fireEvent(LightChangeEvent(connection, EventInitiators.CLIENT, chunkPosition, this, sectionHeight + 1, false))
        }


        var neighbourIndex = 0
        for (chunkX in -1..1) {
            for (chunkZ in -1..1) {
                if (chunkX == 0 && chunkZ == 0) {
                    continue
                }
                val chunkPosition = chunkPosition + Vec2i(chunkX, chunkZ)
                val chunk = neighbours[neighbourIndex++]
                for (chunkY in -1..1) {
                    val neighbourSection = chunk[sectionHeight + chunkY] ?: continue
                    if (!neighbourSection.light.update) {
                        continue
                    }
                    neighbourSection.light.update = false
                    connection.fireEvent(LightChangeEvent(connection, EventInitiators.CLIENT, chunkPosition, chunk, sectionHeight + chunkY, false))
                }
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
            updateHeightmap()
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
            data.bottomLight?.let { bottomLight.update(it) }
            data.topLight?.let { topLight.update(it) }
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
    fun getOrPut(sectionHeight: Int, calculateLight: Boolean = true): ChunkSection? {
        val sections = sections ?: return null
        val sectionIndex = sectionHeight - lowestSection
        if (sectionIndex < 0 || sectionIndex >= sections.size) {
            return null
        }

        var section = sections[sectionIndex]
        if (section == null) {
            section = ChunkSection(sectionHeight, BlockSectionDataProvider(occlusionUpdateCallback = world.occlusionUpdateCallback), chunk = this)
            val cacheBiomeAccessor = world.cacheBiomeAccessor
            val neighbours = this.neighbours
            if (neighbours != null) {
                if (cacheBiomeAccessor != null && biomesInitialized) {
                    section.buildBiomeCache(chunkPosition, sectionHeight, this, neighbours, cacheBiomeAccessor)
                }
                section.neighbours = ChunkUtil.getDirectNeighbours(neighbours, this, sectionHeight)
                for (neighbour in neighbours) {
                    val neighbourNeighbours = neighbour.neighbours ?: continue
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
        return getLight(position.x, position.y, position.z)
    }

    fun getLight(x: Int, y: Int, z: Int): Int {
        val sectionHeight = y.sectionHeight
        val inSectionHeight = y.inSectionHeight
        val heightmapIndex = (z shl 4) or x
        val index = inSectionHeight shl 8 or heightmapIndex
        if (sectionHeight == lowestSection - 1) {
            return bottomLight[index].toInt()
        }
        if (sectionHeight == highestSection + 1) {
            return topLight[index].toInt() or 0xF0 // top has always sky=15
        }
        var light = this[sectionHeight]?.light?.get(index)?.toInt() ?: 0x00
        if (y >= heightmap[heightmapIndex]) {
            // set sky=15
            light = light or 0xF0
        }
        return light
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
        recalculateSkylight()
    }

    fun calculateLight() {
        val sections = sections ?: Broken("Sections is null")
        for (section in sections) {
            if (section == null) {
                continue
            }
            section.light.calculate()
        }
        recalculateSkylight()
    }

    fun resetLight() {
        val sections = sections ?: Broken("Sections is null")
        for (section in sections) {
            if (section == null) {
                continue
            }
            section.light.resetLight()
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

    fun traceBlock(offset: Vec3i, origin: Vec3i, blockPosition: Vec3i = origin + offset): BlockState? {
        val originChunkPosition = origin.chunkPosition
        val targetChunkPosition = blockPosition.chunkPosition

        val deltaChunkPosition = targetChunkPosition - originChunkPosition

        return traceBlock(blockPosition.inChunkPosition, deltaChunkPosition)
    }

    private fun traceBlock(inChunkSectionPosition: Vec3i, chunkOffset: Vec2i): BlockState? {
        if (chunkOffset.x == 0 && chunkOffset.y == 0) {
            return this[inChunkSectionPosition]
        }
        val neighbours = this.neighbours ?: return null

        if (chunkOffset.x > 0) {
            chunkOffset.x--
            return neighbours[6].traceBlock(inChunkSectionPosition, chunkOffset)
        }
        if (chunkOffset.x < 0) {
            chunkOffset.x++
            return neighbours[1].traceBlock(inChunkSectionPosition, chunkOffset)
        }
        if (chunkOffset.y > 0) {
            chunkOffset.y--
            return neighbours[4].traceBlock(inChunkSectionPosition, chunkOffset)
        }
        if (chunkOffset.y < 0) {
            chunkOffset.y++
            return neighbours[3].traceBlock(inChunkSectionPosition, chunkOffset)
        }

        Broken("Can not get chunk from offset: $chunkOffset")
    }


    @Synchronized
    private fun updateHeightmap() {
        val maxY = highestSection * ProtocolDefinition.SECTION_HEIGHT_Y

        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            z@ for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                checkHeightmapY(x, maxY, z)
            }
        }
        recalculateSkylight()
    }

    private fun checkHeightmapY(x: Int, startY: Int, z: Int) {
        val minY = lowestSection * ProtocolDefinition.SECTION_HEIGHT_Y
        var y = startY

        var sectionHeight = y.sectionHeight
        var section: ChunkSection? = this[sectionHeight]
        while (y > minY) {
            val nextSectionHeight = y.sectionHeight
            if (sectionHeight != nextSectionHeight) {
                sectionHeight = nextSectionHeight
                section = this[sectionHeight]
            }
            if (section == null) {
                y -= ProtocolDefinition.SECTION_HEIGHT_Y
                continue
            }
            val block = section.blocks[x, y.inSectionHeight, z]
            if (block == null || !block.isSolid) {
                y--
                continue
            }

            heightmap[(z shl 4) or x] = y
            return
        }
        heightmap[(z shl 4) or x] = minY
    }

    @Synchronized
    private fun updateHeightmap(x: Int, y: Int, z: Int, place: Boolean) {
        val index = (z shl 4) or x

        val current = heightmap[index]

        if (current > y) {
            // our block is/was not the highest, ignore everything
            return
        }
        if (current < y) {
            if (place) {
                // we are the highest block now
                heightmap[index] = y
            }
            return
        }

        if (place) {
            return
        }

        // we used to be the highest block, find out the block below us
        checkHeightmapY(x, y - 1, z)
    }

    private fun recalculateSkylight() {
        if (world.dimension?.hasSkyLight != true) {
            // no need to calculate it
            return
        }
        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                calculateSkylight(x, z)
            }
        }
    }

    private fun calculateSkylight(x: Int, z: Int) {
        val heightmapIndex = (z shl 4) or x
        val maxHeight = heightmap[heightmapIndex]

        // ToDo: only update changed ones
        for (sectionHeight in highestSection - 1 downTo maxHeight.sectionHeight + 1) {
            val section = sections?.get(sectionHeight - lowestSection) ?: continue
            section.light.update = true
        }
        val maxSection = sections?.get(maxHeight.sectionHeight - lowestSection)
        if (maxSection != null) {
            for (y in ProtocolDefinition.SECTION_MAX_Y downTo maxHeight.inSectionHeight) {
                val index = (y shl 8) or heightmapIndex
                maxSection.light.light[index] = (maxSection.light.light[index].toInt() and 0x0F or 0xF0).toByte()
            }
            maxSection.light.update = true
        }
    }
}


