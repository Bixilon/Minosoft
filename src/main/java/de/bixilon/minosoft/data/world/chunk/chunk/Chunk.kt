/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.concurrent.lock.LockUtil.acquired
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantRWLock
import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.light.section.ChunkLight
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.chunk.update.block.ChunkLocalBlockUpdate
import de.bixilon.minosoft.data.world.chunk.update.block.ProposedBlockChange
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkLightUpdate.Causes
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight

/**
 * Collection of chunk sections (height aligned)
 */
class Chunk(
    val world: World,
    val position: ChunkPosition,
) : Tickable {
    val lock = ReentrantRWLock()
    val light = ChunkLight(this)
    val sections = ChunkSectionManagement(this)
    var biomeSource: BiomeSource? = null

    val neighbours = ChunkNeighbours(this)

    operator fun get(sectionHeight: SectionHeight): ChunkSection? = sections[sectionHeight]

    operator fun get(position: InChunkPosition): BlockState? {
        return this[position.sectionHeight]?.blocks?.get(position.inSectionPosition)
    }

    operator fun set(position: InChunkPosition, state: BlockState?) {
        sections.create(position.sectionHeight)?.set(position.inSectionPosition, state)
    }

    fun getBlockEntity(position: InChunkPosition): BlockEntity? {
        return this[position.sectionHeight]?.entities?.get(position.inSectionPosition)
    }

    fun updateBlockEntity(position: InChunkPosition): BlockEntity? {
        return this[position.sectionHeight]?.entities?.update(position.inSectionPosition)
    }

    fun apply(update: ProposedBlockChange) {
        this[update.position] = update.state
    }

    private fun unsafeApply(vararg updates: ProposedBlockChange): Set<ChunkLocalBlockUpdate.Change> {
        val executed: MutableSet<ChunkLocalBlockUpdate.Change> = HashSet(updates.size)

        for (update in updates) {
            val (position, state) = update
            val sectionHeight = position.sectionHeight

            var section = this[sectionHeight]
            if (state == null && section == null) continue

            section = this.sections.create(sectionHeight) ?: continue

            val previous = section.blocks.set(position.inSectionPosition, state)

            if (previous == update.state) continue
            if (previous?.block != state?.block) {
                section.entities[position.inSectionPosition] = null
            }

            section.entities.update(position.inSectionPosition)

            executed += ChunkLocalBlockUpdate.Change(update.position, previous, update.state)
        }

        return executed
    }

    fun apply(vararg updates: ProposedBlockChange) {
        if (updates.isEmpty()) return
        if (updates.size == 1) return apply(updates.first())


        var executed: Set<ChunkLocalBlockUpdate.Change> = unsafeNull()
        lock.locked {
            executed = unsafeApply(*updates)

            if (executed.isEmpty()) return

            light.heightmap.recalculate() // TODO: Only changed ones
            light.recalculate(fireEvent = false, cause = Causes.INITIAL)
        }

        if (neighbours.complete) {
            val sections: HashSet<ChunkSection> = hashSetOf()

            for (change in executed) {
                sections += this[change.position.sectionHeight] ?: continue
            }

            for (section in sections) {
                light.fireLightChange(section, Causes.BLOCK_CHANGE)
            }
            light.fireLightChange(Causes.PROPAGATION)
        }

        ChunkLocalBlockUpdate(this, executed).fire(world.session)
    }

    @Deprecated("sections.create")
    fun getOrPut(height: Int, light: Boolean = true) = sections.create(height, light)

    override fun tick() {
        if (!neighbours.complete) return
        lock.acquired { sections.forEach { it.tick() } }
    }

    override fun toString() = "Chunk($position)"
}


