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

package de.bixilon.minosoft.data.world.chunk.manager

import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkPrototype
import de.bixilon.minosoft.data.world.chunk.manager.size.WorldSizeManager
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbourArray
import de.bixilon.minosoft.data.world.chunk.update.AbstractWorldUpdate
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkCreateUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkUnloadUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.NeighbourChangeUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.prototype.PrototypeChange
import de.bixilon.minosoft.data.world.chunk.update.chunk.prototype.PrototypeChangeUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPosition

class ChunkManager(val world: World, chunkCapacity: Int = 0, prototypeCapacity: Int = 0) {
    val chunks: LockMap<ChunkPosition, Chunk> = LockMap(HashMap(chunkCapacity), world.lock)
    val prototypes: LockMap<ChunkPosition, ChunkPrototype> = LockMap(HashMap(prototypeCapacity), world.lock)
    val size = WorldSizeManager(world)
    val ticker = ChunkTicker(this)
    var revision by observed(0)


    operator fun get(position: ChunkPosition): Chunk? {
        return chunks[position]
    }

    operator fun get(x: Int, z: Int): Chunk? {
        return this[ChunkPosition(x, z)]
    }

    fun unload(position: ChunkPosition) {
        world.lock.lock()
        this.prototypes.unsafe -= position
        val chunk = chunks.unsafe.remove(position)
        if (chunk == null) {
            world.lock.unlock()
            return
        }
        val updates = hashSetOf<AbstractWorldUpdate>(ChunkUnloadUpdate(chunk))

        for ((index, neighbour) in chunk.neighbours.neighbours.array.withIndex()) {
            if (neighbour == null) continue
            val offset = ChunkPosition(ChunkNeighbourArray.OFFSETS[index])
            val neighbourPosition = position + offset
            neighbour.neighbours.remove(-offset)
            updates += NeighbourChangeUpdate(neighbour)
        }
        size.onUnload(position)
        world.occlusion++
        world.lock.unlock()

        for (update in updates) {
            world.session.events.fire(WorldUpdateEvent(world.session, update))
        }
        revision++
    }

    operator fun minusAssign(position: ChunkPosition) = unload(position)

    fun clear() {
        chunks.unsafe.clear()
        prototypes.unsafe.clear()
        size.clear()
        world.view.updateServerDistance()
        revision++
    }

    private fun updateExisting(position: ChunkPosition, prototype: ChunkPrototype, replace: Boolean): PrototypeChange? {
        val chunk = this.chunks.unsafe[position] ?: return null
        val affected = prototype.updateChunk(chunk, replace) ?: return null
        return PrototypeChange(chunk, affected)
    }


    operator fun set(position: ChunkPosition, prototype: ChunkPrototype) {
        set(position, prototype, true)
    }

    fun set(position: ChunkPosition, prototype: ChunkPrototype, replace: Boolean) {
        world.lock.lock()
        updateExisting(position, prototype, replace)?.let {
            world.lock.unlock()
            PrototypeChangeUpdate(it.chunk, it.affected).fire(world.session)
            // TODO: it.chunk.light.recalculate(fireEvent = true, fireSameChunkEvent = false)
            revision++
            return
        }
        val existingPrototype = this.prototypes.unsafe[position]
        existingPrototype?.update(prototype)

        val chunk = (existingPrototype ?: prototype).createChunk(world.session, position)
        if (chunk == null) {
            // chunk not complete
            if (existingPrototype == null) {
                this.prototypes.unsafe.getOrPut(position) { ChunkPrototype() }
            }
            world.lock.unlock()
            return
        }

        if (existingPrototype != null) {
            this.prototypes.unsafe -= position
        }

        this.chunks.unsafe[position] = chunk
        val updates = onChunkCreate(chunk)
        world.lock.unlock()
        for (update in updates) update.fire(world.session)
        revision++
    }

    private fun onChunkCreate(chunk: Chunk): Set<AbstractWorldUpdate> {
        // TODO: update chunk neighbours, update section neighbours, build biome cache, update light (propagate from neighbours), fire update
        size.onCreate(chunk.position)
        world.view.updateServerDistance()

        val updates = HashSet<AbstractWorldUpdate>(9, 1.0f)
        updates += ChunkCreateUpdate(chunk)

        for (index in 0 until ChunkNeighbourArray.COUNT) {
            val offset = ChunkPosition(ChunkNeighbourArray.OFFSETS[index])
            val neighbour = this.chunks.unsafe[chunk.position + offset] ?: continue
            chunk.neighbours[offset] = neighbour
            neighbour.neighbours[-offset] = chunk

        }
        // TODO: Update section neighbours
        // TODO: fire event


        for (neighbour in chunk.neighbours.neighbours.array) {
            if (neighbour == null) continue
            updates += NeighbourChangeUpdate(neighbour)
        }

        return updates
    }


    fun create(position: ChunkPosition, biome: BiomeSource = DummyBiomeSource(null)): Chunk {
        chunks.lock.lock()
        val chunk = chunks.unsafe.getOrPut(position) { Chunk(world.session, position, biome) }
        val updates = onChunkCreate(chunk)
        chunks.lock.unlock()

        for (update in updates) update.fire(world.session)
        revision++


        return chunk
    }

    fun tick(simulationDistance: Int, cameraPosition: ChunkPosition) {
        ticker.tick(simulationDistance, cameraPosition)
    }
}
