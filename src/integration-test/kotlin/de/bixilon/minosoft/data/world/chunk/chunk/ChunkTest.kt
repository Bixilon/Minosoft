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

import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.entities.block.TestBlockEntities
import de.bixilon.minosoft.data.registries.blocks.state.TestBlockStates
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.WorldBiomes
import de.bixilon.minosoft.data.world.chunk.update.AbstractWorldUpdate
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateTestUtil.collectUpdates
import de.bixilon.minosoft.data.world.chunk.update.block.ChunkLocalBlockUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.modding.event.master.EventMaster
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["chunk"])
class ChunkTest {

    private fun create(): Chunk {
        val world = World::class.java.allocate()
        world::dimension.forceSet(DataObserver(DimensionProperties()))
        world::biomes.forceSet(WorldBiomes(world))

        val session = PlaySession::class.java.allocate()
        session::events.forceSet(EventMaster())
        world::session.forceSet(session)

        val chunk = Chunk(world, ChunkPosition(0, 0))
        return chunk
    }


    fun `apply single update`() {
        val chunk = create()
        chunk.apply(ChunkLocalBlockUpdate.Change(InChunkPosition(1, 2, 3), TestBlockStates.TEST1))

        assertEquals(chunk[InChunkPosition(1, 2, 3)], TestBlockStates.TEST1)
    }

    fun `apply single update with block entity`() {
        val chunk = create()
        chunk.apply(ChunkLocalBlockUpdate.Change(InChunkPosition(1, 2, 3), TestBlockStates.ENTITY1))

        assertTrue(chunk.getBlockEntity(InChunkPosition(1, 2, 3)) is TestBlockEntities.TestBlockEntity)
    }

    fun `apply multiple updates`() {
        val chunk = create()
        chunk.apply(
            ChunkLocalBlockUpdate.Change(InChunkPosition(1, 2, 3), TestBlockStates.TEST1),
            ChunkLocalBlockUpdate.Change(InChunkPosition(1, 30, 3), TestBlockStates.TEST2),
        )

        assertEquals(chunk[InChunkPosition(1, 2, 3)], TestBlockStates.TEST1)
        assertEquals(chunk[InChunkPosition(1, 30, 3)], TestBlockStates.TEST2)
    }

    fun `apply multiple updates with entities`() {
        val chunk = create()
        chunk.apply(
            ChunkLocalBlockUpdate.Change(InChunkPosition(1, 2, 3), TestBlockStates.ENTITY1),
            ChunkLocalBlockUpdate.Change(InChunkPosition(1, 30, 3), TestBlockStates.ENTITY2),
        )

        assertTrue(chunk.getBlockEntity(InChunkPosition(1, 2, 3)) is TestBlockEntities.TestBlockEntity)
        assertTrue(chunk.getBlockEntity(InChunkPosition(1, 30, 3)) is TestBlockEntities.TestBlockEntity)
    }

    fun `don't create section if just clearing blocks`() {
        val chunk = create()
        chunk.apply(
            ChunkLocalBlockUpdate.Change(InChunkPosition(1, 2, 3), null),
            ChunkLocalBlockUpdate.Change(InChunkPosition(1, 5, 3), null),
        )

        assertNull(chunk[0])
    }

    fun `trigger update event`() {
        val chunk = create()
        val updates = setOf(
            ChunkLocalBlockUpdate.Change(InChunkPosition(1, 2, 3), TestBlockStates.TEST1),
            ChunkLocalBlockUpdate.Change(InChunkPosition(1, 30, 3), TestBlockStates.TEST2),
        )

        val events = chunk.world.collectUpdates()

        chunk.apply(*updates.toTypedArray())


        assertEquals(events, listOf(
            ChunkLocalBlockUpdate(chunk, updates),
        ))
    }

    fun `trigger update event only changes`() {
        val chunk = create()

        val update1 = ChunkLocalBlockUpdate.Change(InChunkPosition(1, 2, 3), TestBlockStates.TEST1)
        val update2 = ChunkLocalBlockUpdate.Change(InChunkPosition(1, 30, 3), TestBlockStates.TEST2)

        chunk.apply(update2)


        val updates = chunk.world.collectUpdates()

        chunk.apply(update1, update2)


        assertEquals(updates, listOf(
            ChunkLocalBlockUpdate(chunk, setOf(update1)),
        ))
    }

    fun `trigger update event without changes`() {
        val chunk = create()

        val update1 = ChunkLocalBlockUpdate.Change(InChunkPosition(1, 2, 3), TestBlockStates.TEST1)
        val update2 = ChunkLocalBlockUpdate.Change(InChunkPosition(1, 30, 3), TestBlockStates.TEST2)

        chunk.apply(update1); chunk.apply(update2)

        val updates = chunk.world.collectUpdates()

        chunk.apply(update1, update2)


        assertEquals(updates, listOf<AbstractWorldUpdate>())
    }
}
