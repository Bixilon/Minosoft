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

package de.bixilon.minosoft.data.world.chunk

import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantRWLock
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.jvmField
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.WorldBiomes
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkSectionManagement
import de.bixilon.minosoft.data.world.chunk.light.section.ChunkLight
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.modding.event.master.EventMaster
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.test.ITUtil.allocate
import org.objenesis.ObjenesisStd

const val SECTIONS = 16

object LightTestingUtil {
    private val world = createWorld()

    fun createSession(): PlaySession {
        val session = ObjenesisStd().newInstance(PlaySession::class.java)

        Session::events.jvmField.forceSet(session, EventMaster())
        return session
    }

    fun createWorld(): World {
        val objenesis = ObjenesisStd()
        val world = World::class.java.allocate()
        world::dimension.forceSet(DataObserver(DimensionProperties(skyLight = true)))
        world::session.forceSet(createSession())
        world::biomes.forceSet(WorldBiomes(world))

        return world
    }

    fun createEmptyChunk(position: ChunkPosition): Chunk {
        val chunk = Chunk::class.java.allocate()
        chunk::lock.forceSet(ReentrantRWLock())
        chunk::position.forceSet(position.raw)
        chunk::world.forceSet(world)
        chunk::light.forceSet(ChunkLight(chunk))
        chunk::neighbours.forceSet(ChunkNeighbours(chunk))
        chunk::sections.forceSet(ChunkSectionManagement(chunk))

        return chunk
    }

    fun createChunkWithNeighbours(): Chunk {
        val chunk = createEmptyChunk(ChunkPosition.EMPTY)
        for (x in -1..1) {
            for (z in -1..1) {
                val position = ChunkPosition(x, z)
                if (position == ChunkPosition.EMPTY) {
                    continue
                }
                chunk.neighbours[position] = createEmptyChunk(ChunkPosition(x, z))
            }
        }

        return chunk
    }

    fun ChunkSection.fill(state: BlockState) {
        for (index in 0 until 4096) {
            blocks.unsafeSet(InSectionPosition(index), state)
        }
    }
}
