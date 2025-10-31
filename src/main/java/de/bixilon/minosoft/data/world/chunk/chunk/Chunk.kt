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

import de.bixilon.kutil.concurrent.lock.LockUtil.acquired
import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantRWLock
import de.bixilon.kutil.math.simple.IntMath.clamp
import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.light.section.ChunkLight
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

/**
 * Collection of chunks sections (from the lowest section to the highest section in y axis)
 */
class Chunk(
    val session: PlaySession,
    val position: ChunkPosition,
) : Tickable {
    val lock = ReentrantRWLock()
    val world = session.world
    val light = ChunkLight(this)
    val sections = ChunkSectionManagement(this)
    var biomeSource: BiomeSource? = null

    @Deprecated("move to biome source?")
    val cacheBiomes = world.biomes.noise != null

    val neighbours = ChunkNeighbours(this)


    init {
        light.heightmap.recalculate()
    }

    operator fun get(sectionHeight: SectionHeight): ChunkSection? = sections[sectionHeight]

    operator fun get(position: InChunkPosition): BlockState? {
        return this[position.y.sectionHeight]?.blocks?.get(position.inSectionPosition)
    }

    operator fun set(position: InChunkPosition, state: BlockState?) {
        val section = getOrPut(position.y.sectionHeight) ?: return
        val previous = section.blocks.set(position.inSectionPosition, state)
        if (previous == state) return

        if (previous?.block != state?.block) {
            this[position.y.sectionHeight]?.entities?.set(position.inSectionPosition, null) // TODO: unload
        }
        val entity = updateBlockEntity(position)

        if (world.dimension.light) {
            light.onBlockChange(position, section, previous, state)
        }

        SingleBlockUpdate(this.position.blockPosition(position), this, state, entity).fire(session)
    }

    fun getBlockEntity(position: InChunkPosition): BlockEntity? {
        return this[position.y.sectionHeight]?.entities?.get(position.inSectionPosition)
    }

    fun updateBlockEntity(position: InChunkPosition): BlockEntity? {
        return this[position.y.sectionHeight]?.entities?.update(position.inSectionPosition)
    }

    fun apply(update: ChunkLocalBlockUpdate.LocalUpdate) {
        TODO()
        this[update.position] = update.state
    }

    fun apply(updates: Collection<ChunkLocalBlockUpdate.LocalUpdate>) {
        TODO()
        if (updates.isEmpty()) return
        if (updates.size == 1) return apply(updates.first())

        val executed: MutableSet<ChunkLocalBlockUpdate.LocalUpdate> = hashSetOf()
        val sections: MutableSet<ChunkSection> = hashSetOf()

        lock.lock()
        for (update in updates) {
            val sectionHeight = update.position.y.sectionHeight
            var section = this[sectionHeight]
            if (update.state == null && section == null) continue

            section = getOrPut(sectionHeight) ?: continue
            val previous = section.blocks.noOcclusionSet(InSectionPosition(update.position.x, update.position.y.inSectionHeight, update.position.z), update.state)
            if (previous == update.state) continue
            if (previous?.block != update.state?.block) {
                this[update.position.y.sectionHeight]?.entities?.set(update.position.x, update.position.y and 0x0F, update.position.z, null)
            }
            updateBlockEntity(update.position)
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
        light.fireLightChange(true)
    }

    @Deprecated("sections.create")
    fun getOrPut(height: Int, light: Boolean = true) = sections.create(height, light)

    override fun tick() {
        if (!neighbours.complete) return
        lock.acquired {
            sections.forEach { it.tick() }
        }
    }

    fun getBiome(position: InChunkPosition): Biome? {
        val position = position.with(y = position.y.clamp(world.dimension.minY, world.dimension.maxY))
        if (!cacheBiomes) {
            return biomeSource?.get(position)
        }
        return session.world.biomes[position, this]
    }
}


