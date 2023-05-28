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
package de.bixilon.minosoft.data.world.chunk.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.concurrent.lock.thread.ThreadLock
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.math.simple.IntMath.clamp
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.BlockWithEntity
import de.bixilon.minosoft.data.world.biome.accessor.BiomeAccessor
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.light.ChunkLight
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.chunk.update.block.ChunkLocalBlockUpdate
import de.bixilon.minosoft.data.world.chunk.update.block.SingleBlockUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.chunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkPosition
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.chunk.ChunkUtil
import java.util.*

/**
 * Collection of chunks sections (from the lowest section to the highest section in y axis)
 */
class Chunk(
    val connection: PlayConnection,
    val chunkPosition: ChunkPosition,
    var sections: Array<ChunkSection?>,
    var biomeSource: BiomeSource,
) : Iterable<ChunkSection?>, BiomeAccessor {
    val lock = ThreadLock()
    val world = connection.world
    val light = ChunkLight(this)
    val minSection = world.dimension.minSection
    val maxSection = world.dimension.maxSection
    val cacheBiomes = world.cacheBiomeAccessor != null

    val neighbours = ChunkNeighbours(this)


    init {
        light.recalculateHeightmap()
    }

    @Deprecated("neighbours.complete", ReplaceWith("neighbours.complete"))
    val isFullyLoaded: Boolean
        get() = neighbours.complete

    operator fun get(sectionHeight: SectionHeight): ChunkSection? = sections.getOrNull(sectionHeight - minSection)

    operator fun get(x: Int, y: Int, z: Int): BlockState? {
        return this[y.sectionHeight]?.blocks?.get(x, y.inSectionHeight, z)
    }

    operator fun get(position: InChunkPosition): BlockState? = get(position.x, position.y, position.z)

    operator fun set(x: Int, y: Int, z: Int, state: BlockState?) {
        val section = getOrPut(y.sectionHeight) ?: return
        val previous = section.set(x, y and 0x0F, z, state)
        if (previous == state) return
        val entity = getOrPutBlockEntity(x, y, z)
        light.onBlockChange(x, y, z, section, state) // TODO: heightmap gets updated after the block set? -> optimize/maybe even invalid

        SingleBlockUpdate(Vec3i(chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X + x, y, chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z + z), this, state, entity).fire(connection)
    }

    operator fun set(x: Int, y: Int, z: Int, state: BlockState?, entity: BlockEntity?) {
        val section = getOrPut(y.sectionHeight) ?: return
        section[x, y and 0x0F, z] = state
        section.blockEntities[x, y and 0x0F, z] = entity
    }

    operator fun set(position: Vec3i, blockState: BlockState?) = set(position.x, position.y, position.z, blockState)

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

    operator fun set(x: Int, y: Int, z: Int, blockEntity: BlockEntity) {
        this.set(x, y, z, blockEntity as BlockEntity?)
    }

    @JvmName("set2")
    fun set(x: Int, y: Int, z: Int, blockEntity: BlockEntity?) {
        (getOrPut(y.sectionHeight) ?: return).blockEntities[x, y.inSectionHeight, z] = blockEntity
    }

    operator fun set(position: Vec3i, blockEntity: BlockEntity) = set(position.x, position.y, position.z, blockEntity)

    @JvmName("set2")
    fun set(position: Vec3i, blockEntity: BlockEntity?) = set(position.x, position.y, position.z, blockEntity)

    fun apply(updates: Collection<ChunkLocalBlockUpdate.LocalUpdate>) {
        lock.lock()
        val executed: MutableSet<ChunkLocalBlockUpdate.LocalUpdate> = hashSetOf()
        for (update in updates) {
            val section = getOrPut(update.position.y.sectionHeight, lock = false) ?: continue
            val previous = section.set(update.position.x, update.position.y.inSectionHeight, update.position.z, update.state)
            if (previous == update.state) continue
            getOrPutBlockEntity(update.position)
            executed += update
        }
        if (executed.isNotEmpty()) {
            light.recalculateHeightmap()
            light.recalculate()
        }

        lock.unlock()
        if (executed.isEmpty()) return

        ChunkLocalBlockUpdate(chunkPosition, this, executed).fire(connection)
    }

    fun getOrPut(sectionHeight: Int, calculateLight: Boolean = true, lock: Boolean = true): ChunkSection? {
        val index = sectionHeight - minSection
        if (index < 0 || index >= sections.size) {
            return null
        }
        sections[index]?.let { return it }
        if (lock) this.lock.lock()

        var section = sections[index] // get another time, it might have changed already
        if (section == null) {
            section = ChunkSection(sectionHeight, chunk = this)
            section.blocks::section.forceSet(section)
            val neighbours = this.neighbours.get()
            if (neighbours != null) {
                this.neighbours.completeSection(neighbours, section, sectionHeight, world.cacheBiomeAccessor)
            }

            sections[index] = section

            if (index > 0) {
                sections[index - 1]?.neighbours?.set(Directions.O_UP, section)
            }
            if (index < maxSection - 1) {
                sections[index + 1]?.neighbours?.set(Directions.O_DOWN, section)
            }

            if (neighbours != null) {
                for (neighbour in neighbours) {
                    val neighbourNeighbours = neighbour.neighbours.get() ?: continue
                    neighbour.updateNeighbours(neighbourNeighbours, sectionHeight)
                }
            }

            // check light of neighbours to check if their light needs to be traced into our own chunk
            if (calculateLight) {
                section.light.propagateFromNeighbours()
            }
        }
        if (lock) this.lock.unlock()
        return section
    }

    fun tick(connection: PlayConnection, chunkPosition: Vec2i, random: Random) {
        if (!neighbours.complete) return
        for ((index, section) in sections.withIndex()) {
            section?.tick(connection, chunkPosition, index + minSection, random)
        }
    }

    override fun iterator(): Iterator<ChunkSection?> {
        return sections.iterator()
    }

    override fun getBiome(x: Int, y: Int, z: Int): Biome? {
        val y = y.clamp(world.dimension.minY, world.dimension.maxY)
        if (cacheBiomes) {
            val section = this[y.sectionHeight] ?: return connection.world.cacheBiomeAccessor?.getBiome((chunkPosition.x shl 4) or x, y, (chunkPosition.y shl 4) or z, chunkPosition.x, chunkPosition.y, this, this.neighbours.get())
            return section.biomes[x, y.inSectionHeight, z]
        }
        return biomeSource.getBiome(x and 0x0F, y, z and 0x0F)
    }

    @Deprecated("")
    private fun updateNeighbours(neighbours: Array<Chunk>, sectionHeight: Int) {
        for (nextSectionHeight in sectionHeight - 1..sectionHeight + 1) {
            if (nextSectionHeight < minSection || nextSectionHeight > maxSection) {
                continue
            }

            val section = this[nextSectionHeight] ?: continue
            val sectionNeighbours = ChunkUtil.getDirectNeighbours(neighbours, this, nextSectionHeight)
            section.neighbours = sectionNeighbours
        }
    }

    @Deprecated("neighbours")
    fun traceBlock(offset: Vec3i, origin: Vec3i, blockPosition: Vec3i = origin + offset): BlockState? {
        val chunkDelta = (origin - blockPosition).chunkPosition

        return traceBlock(blockPosition.x and 0x0F, blockPosition.y, blockPosition.z and 0x0F, chunkDelta)
    }

    @Deprecated("neighbours")
    fun traceBlock(offset: Vec3i): BlockState? {
        return traceBlock(offset.inChunkPosition, offset.chunkPosition)
    }

    @Deprecated("neighbours")
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

    @Deprecated("neighbours")
    private fun traceBlock(inChunkPosition: Vec3i, chunkOffset: Vec2i): BlockState? {
        return traceChunk(chunkOffset)?.get(inChunkPosition)
    }

    @Deprecated("neighbours")
    fun traceBlock(x: Int, y: Int, z: Int, chunkOffset: Vec2i): BlockState? {
        return traceChunk(chunkOffset)?.get(x, y, z)
    }
}


