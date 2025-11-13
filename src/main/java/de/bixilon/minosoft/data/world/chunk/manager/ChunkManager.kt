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
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkData
import de.bixilon.minosoft.data.world.chunk.manager.size.WorldSizeManager
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbourUtil
import de.bixilon.minosoft.data.world.chunk.update.AbstractWorldUpdate
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkDataUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkLightUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkUnloadUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.NeighbourSetUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPosition

class ChunkManager(
    val world: World,
    chunkCapacity: Int = 0,
) {
    val chunks: LockMap<ChunkPosition, Chunk> = LockMap(HashMap(chunkCapacity), world.lock)
    val size = WorldSizeManager(world)
    val ticker = ChunkTicker(this)
    var revision by observed(0)


    operator fun get(position: ChunkPosition) = chunks[position]
    operator fun get(x: Int, z: Int) = this[ChunkPosition(x, z)]

    fun unload(position: ChunkPosition) {
        val chunk = world.lock.locked {
            val chunk = chunks.unsafe.remove(position) ?: return
            size.onUnload(position)
            return@locked chunk
        }
        val updates = hashSetOf<AbstractWorldUpdate>(ChunkUnloadUpdate(chunk))

        val neighbours = chunk.neighbours
        for ((index, neighbour) in neighbours.array.withIndex()) {
            if (neighbour == null) continue
            val offset = ChunkPosition(ChunkNeighbourUtil.OFFSETS[index])
            neighbour.neighbours.remove(-offset)
            neighbours.array[index] = null
        }
        world.occlusion++

        for (update in updates) {
            world.session.events.fire(WorldUpdateEvent(world.session, update))
        }
        revision++
    }

    operator fun minusAssign(position: ChunkPosition) = unload(position)

    fun clear() {
        chunks.unsafe.clear()
        size.clear()
        world.view.updateServerDistance()
        revision++
    }

    private fun create(position: ChunkPosition): Chunk {
        val chunk = Chunk(world, position)

        for (index in 0 until ChunkNeighbourUtil.COUNT) {
            if (chunk.neighbours.array[index] != null) continue
            val offset = ChunkPosition(ChunkNeighbourUtil.OFFSETS[index])
            val neighbour = this.chunks.unsafe[chunk.position + offset] ?: continue
            chunk.neighbours[offset] = neighbour
            neighbour.neighbours[-offset] = chunk
        }

        this.chunks.unsafe[position] = chunk

        return chunk
    }

    fun update(position: ChunkPosition, data: ChunkData, replace: Boolean): Chunk {
        var created = false
        val chunk = world.lock.locked {
            chunks.unsafe[position]?.let { return@locked it }
            created = true

            return@locked create(position)
        }


        val affected = data.update(chunk, replace && !created)

        size.onCreate(chunk.position)
        world.view.updateServerDistance()

        if (created || affected.isNotEmpty()) {
            if (!created) chunk.light.reset()
            chunk.light.calculate(false, ChunkLightUpdate.Causes.INITIAL)
            chunk.light.propagateFromNeighbours(fireEvent = false, ChunkLightUpdate.Causes.INITIAL)

            for (neighbour in chunk.neighbours.array) {
                if (neighbour == null || !neighbour.neighbours.complete) continue
                if (created && neighbour.light.initial) continue
                neighbour.light.initial = false


                neighbour.light.reset()
                neighbour.light.calculate(false, ChunkLightUpdate.Causes.INITIAL)
                neighbour.light.propagateFromNeighbours(true, ChunkLightUpdate.Causes.INITIAL)
            }
        }

        revision++

        if (affected.isNotEmpty()) {
            ChunkDataUpdate(chunk, affected).fire(world.session)
        }

        if (created) {
            for (neighbour in chunk.neighbours.array) {
                if (neighbour == null) continue
                NeighbourSetUpdate(neighbour).fire(world.session)
            }
        }


        return chunk
    }

    operator fun set(position: ChunkPosition, data: ChunkData) = update(position, data, true)


    fun tick(simulationDistance: Int, cameraPosition: ChunkPosition) {
        ticker.tick(simulationDistance, cameraPosition)
    }

    inline fun forEach(consumer: (Chunk) -> Unit) {
        for ((_, chunk) in this.chunks.unsafe) {
            consumer.invoke(chunk)
        }
    }
}
