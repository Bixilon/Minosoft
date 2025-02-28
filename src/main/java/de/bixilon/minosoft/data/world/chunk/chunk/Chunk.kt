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

import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.kutil.math.simple.IntMath.clamp
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.light.ChunkLight
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.chunk.update.block.ChunkLocalBlockUpdate
import de.bixilon.minosoft.data.world.chunk.update.block.SingleBlockUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import java.util.*

/**
 * Collection of chunks sections (from the lowest section to the highest section in y axis)
 */
class Chunk(
    val session: PlaySession,
    val position: ChunkPosition,
    var biomeSource: BiomeSource,
) : Iterable<ChunkSection?> {
    val lock = RWLock.rwlock()
    val world = session.world
    val light = ChunkLight(this)
    val minSection = world.dimension.minSection
    val maxSection = world.dimension.maxSection

    @Deprecated("move to biome source?")
    val cacheBiomes = world.biomes.noise != null
    var sections: Array<ChunkSection?> = arrayOfNulls(world.dimension.sections)

    val neighbours = ChunkNeighbours(this)


    init {
        light.heightmap.recalculate()
    }

    operator fun get(sectionHeight: SectionHeight): ChunkSection? = sections.getOrNull(sectionHeight - minSection)

    operator fun get(position: InChunkPosition): BlockState? {
        return this[position.y.sectionHeight]?.blocks?.get(position.inSectionPosition)
    }

    operator fun set(position: InChunkPosition, state: BlockState?) {
        val section = getOrPut(position.y.sectionHeight) ?: return
        val previous = section.blocks.set(position.inSectionPosition, state)
        if (previous == state) return
        if (previous?.block != state?.block) {
            this[position.y.sectionHeight]?.blockEntities?.set(position.inSectionPosition, null)
        }
        val entity = getOrPutBlockEntity(position)

        if (world.dimension.light) {
            light.onBlockChange(position, section, previous, state)
        }

        SingleBlockUpdate(this.position.blockPosition(position), this, state, entity).fire(session)
    }

    fun getBlockEntity(position: InChunkPosition): BlockEntity? {
        return this[position.y.sectionHeight]?.blockEntities?.get(position.inSectionPosition)
    }

    fun getOrPutBlockEntity(position: InChunkPosition): BlockEntity? {
        val sectionHeight = position.y.sectionHeight
        val inSection = position.inSectionPosition
        var blockEntity = this[sectionHeight]?.blockEntities?.get(inSection)
        val state = this[sectionHeight]?.blocks?.get(inSection) ?: return null
        if (blockEntity != null && state.block !is BlockWithEntity<*>) {
            this[sectionHeight]?.blockEntities?.set(inSection, null)
            return null
        }
        if (blockEntity != null) {
            return blockEntity
        }
        if (state.block !is BlockWithEntity<*>) {
            return null
        }
        blockEntity = state.block.createBlockEntity(session) ?: return null
        val section = this.getOrPut(sectionHeight) ?: return null
        section.blockEntities[inSection] = blockEntity

        return blockEntity
    }

    fun set(position: InChunkPosition, blockEntity: BlockEntity?) {
        val section = getOrPut(position.y.sectionHeight) ?: return
        section.blockEntities[position.inSectionPosition] = blockEntity
    }

    fun apply(update: ChunkLocalBlockUpdate.LocalUpdate) {
        this[update.position] = update.state
    }

    fun apply(updates: Collection<ChunkLocalBlockUpdate.LocalUpdate>) {
        if (updates.isEmpty()) return
        if (updates.size == 1) return apply(updates.first())

        val executed: MutableSet<ChunkLocalBlockUpdate.LocalUpdate> = hashSetOf()
        val sections: MutableSet<ChunkSection> = hashSetOf()

        lock.lock()
        for (update in updates) {
            val sectionHeight = update.position.y.sectionHeight
            var section = this[sectionHeight]
            if (update.state == null && section == null) continue

            section = getOrPut(sectionHeight, lock = false) ?: continue
            val previous = section.blocks.noOcclusionSet(InSectionPosition(update.position.x, update.position.y.inSectionHeight, update.position.z), update.state)
            if (previous == update.state) continue
            if (previous?.block != update.state?.block) {
                this[update.position.y.sectionHeight]?.blockEntities?.set(update.position.x, update.position.y and 0x0F, update.position.z, null)
            }
            getOrPutBlockEntity(update.position)
            executed += update
            sections += section
        }

        if (executed.isEmpty()) {
            return lock.unlock()
        }
        light.heightmap.recalculate()
        light.recalculate(fireEvent = false)

        for (section in sections) {
            section.blocks.occlusion.recalculate(true)
        }

        lock.unlock()

        ChunkLocalBlockUpdate(position, this, executed).fire(session)
        light.fireLightChange(this.sections, true)
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
            val neighbours = this.neighbours.get()
            if (neighbours != null) {
                this.neighbours.completeSection(neighbours, section, sectionHeight, world.biomes.noise)
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
                    neighbour.neighbours.update(neighbourNeighbours, sectionHeight)
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

    fun tick(session: PlaySession, random: Random) {
        if (!neighbours.complete) return
        lock.acquire()
        for ((index, section) in sections.withIndex()) {
            section?.tick(session, random)
        }
        lock.release()
    }

    override fun iterator(): Iterator<ChunkSection?> {
        return sections.iterator()
    }

    fun getBiome(position: InChunkPosition): Biome? {
        val position = position.with(y = position.y.clamp(world.dimension.minY, world.dimension.maxY))
        if (!cacheBiomes) {
            return biomeSource.get(position)
        }
        return session.world.biomes.get(position, this)
    }
}


