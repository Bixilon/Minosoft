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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.entities.block.TestBlockEntities
import de.bixilon.minosoft.data.registries.blocks.state.TestBlockStates
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.WorldBiomes
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.update.AbstractWorldUpdate
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateTestUtil.collectUpdates
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkDataUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.modding.event.master.EventMaster
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.test.ITUtil.allocate
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["chunk"])
class ChunkDataTest {

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

    fun `merge two data`() {
        val existing = ChunkData()
        val next = ChunkData(blocks = Array(1) { arrayOfNulls(ChunkSize.BLOCKS_PER_SECTION) })

        // TODO: entities, ...
        existing.update(next)

        assertEquals(existing.blocks!!.size, 1)
    }


    fun `update blocks without replace`() {
        val chunk = create()
        chunk[InChunkPosition(4, 3, 5)] = TestBlockStates.TEST1
        chunk[InChunkPosition(4, 36, 5)] = TestBlockStates.TEST1


        ChunkData(blocks = Array(16) { if (it == 0) arrayOfNulls(ChunkSize.BLOCKS_PER_SECTION) else null }).update(chunk, false)

        assertEquals(chunk[InChunkPosition(4, 3, 5)], null)
        assertEquals(chunk[InChunkPosition(4, 36, 5)], TestBlockStates.TEST1)
    }

    fun `update blocks with replace`() {
        val chunk = create()
        chunk[InChunkPosition(4, 3, 5)] = TestBlockStates.TEST1
        chunk[InChunkPosition(4, 36, 5)] = TestBlockStates.TEST1


        ChunkData(blocks = Array(16) { if (it == 0) arrayOfNulls(ChunkSize.BLOCKS_PER_SECTION) else null }).update(chunk, true)

        assertEquals(chunk[InChunkPosition(4, 3, 5)], null)
        assertEquals(chunk[InChunkPosition(4, 36, 5)], null)
    }

    fun `update biome source`() {
        val chunk = create()

        chunk.biomeSource = DummyBiomeSource(null)
        val next = DummyBiomeSource(null)

        ChunkData(biomeSource = next).update(chunk, false)

        assertSame(chunk.biomeSource, next)
    }

    fun `clear existing block entities`() {
        val chunk = create()
        chunk[InChunkPosition(0, 0, 2)] = TestBlockStates.ENTITY1

        ChunkData(blocks = Array(16) { if (it == 0) Array(ChunkSize.BLOCKS_PER_SECTION) { TestBlockStates.TEST1 } else null }).update(chunk, false)

        assertNull(chunk.getBlockEntity(InChunkPosition(0, 0, 2)))
    }

    fun `create block entities`() {
        val chunk = create()

        ChunkData(blocks = Array(16) { if (it == 0) Array(ChunkSize.BLOCKS_PER_SECTION) { if (it == 1) TestBlockStates.ENTITY1 else TestBlockStates.TEST1 } else null }).update(chunk, false)

        assertTrue(chunk.getBlockEntity(InChunkPosition(0, 0, 1)) is TestBlockEntities.TestBlockEntity)
        assertNull(chunk.getBlockEntity(InChunkPosition(0, 0, 2)))
    }

    fun `set block entity data`() {
        val chunk = create()

        val nbt = Int2ObjectOpenHashMap<JsonObject>()
        nbt[InChunkPosition(0, 0, 1).raw] = mapOf("hello" to "test")
        ChunkData(blocks = Array(16) { if (it == 0) Array(ChunkSize.BLOCKS_PER_SECTION) { if (it == 1) TestBlockStates.ENTITY1 else TestBlockStates.TEST1 } else null }, entities = nbt).update(chunk, false)

        val entity = chunk.getBlockEntity(InChunkPosition(0, 0, 1)).unsafeCast<TestBlockEntities.TestBlockEntity>()
        assertEquals(entity.data, mapOf("hello" to "test"))
    }

    fun `no events if data is empty`() {
        val chunk = create()
        chunk[InChunkPosition(4, 36, 5)] = TestBlockStates.TEST1
        chunk[InChunkPosition(4, 3, 5)] = TestBlockStates.TEST1

        val events = chunk.world.collectUpdates()

        ChunkData(blocks = arrayOfNulls(16)).update(chunk, false)

        assertEquals(events, listOf<AbstractWorldUpdate>())
    }

    fun `event on block update without replace`() {
        val chunk = create()
        chunk[InChunkPosition(4, 3, 5)] = TestBlockStates.TEST1
        chunk[InChunkPosition(4, 36, 5)] = TestBlockStates.TEST1

        val events = chunk.world.collectUpdates()

        ChunkData(blocks = Array(16) { if (it == 0) arrayOfNulls(ChunkSize.BLOCKS_PER_SECTION) else null }).update(chunk, false)

        assertEquals(events, listOf(ChunkDataUpdate(chunk, setOf(chunk.sections[0]!!))))
    }

    fun `event on block update with replace`() {
        val chunk = create()
        chunk[InChunkPosition(4, 3, 5)] = TestBlockStates.TEST1
        chunk[InChunkPosition(4, 36, 5)] = TestBlockStates.TEST1

        val events = chunk.world.collectUpdates()

        ChunkData(blocks = Array(16) { if (it == 0) arrayOfNulls(ChunkSize.BLOCKS_PER_SECTION) else null }).update(chunk, false)

        assertEquals(events, listOf(ChunkDataUpdate(chunk, setOf(chunk.sections[0]!!, chunk.sections[2]!!))))
    }

    fun `event on biome source change`() {
        val chunk = create()
        chunk[InChunkPosition(4, 3, 5)] = TestBlockStates.TEST1 // create block, so sections can be affected
        chunk.biomeSource = DummyBiomeSource(null)

        val events = chunk.world.collectUpdates()

        val next = DummyBiomeSource(null)

        ChunkData(biomeSource = next).update(chunk, false)

        assertEquals(events, listOf(ChunkDataUpdate(chunk, setOf(chunk.sections[0]!!))))
    }
}
