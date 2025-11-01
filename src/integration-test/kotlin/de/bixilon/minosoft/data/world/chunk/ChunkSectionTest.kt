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

import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.TestBlockEntities
import de.bixilon.minosoft.data.registries.blocks.TestBlocks
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.blocks.state.TestBlockStates
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.WorldBiomes
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.update.AbstractWorldUpdate
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateTestUtil.collectUpdates
import de.bixilon.minosoft.data.world.chunk.update.block.SingleBlockUpdate
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.modding.event.master.EventMaster
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["chunk"])
class ChunkSectionTest {

    private fun create(): ChunkSection {
        val world = World::class.java.allocate()
        world::dimension.forceSet(DataObserver(DimensionProperties()))
        world::biomes.forceSet(WorldBiomes(world))


        val session = PlaySession::class.java.allocate()
        session::events.forceSet(EventMaster())
        world::session.forceSet(session)

        val chunk = Chunk(world, ChunkPosition(0, 0))
        return chunk.sections.create(2)!!
    }

    fun `trace same chunk no block`() {
        val section = create()

        assertEquals(section.traceBlock(InSectionPosition(1, 1, 1), BlockPosition(1, 2, 3)), null)
    }

    fun `trace no offset`() {
        val section = create()
        section.blocks[1, 1, 1] = TestBlockStates.TEST1

        assertEquals(section.traceBlock(InSectionPosition(1, 1, 1), BlockPosition(0, 0, 0)), TestBlockStates.TEST1)
    }

    fun `trace same chunk offset`() {
        val section = create()
        section.blocks[2, 3, 4] = TestBlockStates.TEST1

        assertEquals(section.traceBlock(InSectionPosition(1, 1, 1), BlockPosition(1, 2, 3)), TestBlockStates.TEST1)
    }

    fun `trace direction`() {
        val section = create()
        section.blocks[1, 2, 1] = TestBlockStates.TEST1

        assertEquals(section.traceBlock(InSectionPosition(1, 1, 1), Directions.UP), TestBlockStates.TEST1)
    }

    fun `trace neighbour`() {
        val section = create()
        section.chunk.getOrPut(3)!!.blocks[2, 3, 4] = TestBlockStates.TEST1

        assertEquals(section.traceBlock(InSectionPosition(1, 1, 1), BlockPosition(1, 18, 3)), TestBlockStates.TEST1)
    }


    fun `create block entity when block is with entity`() {
        val section = create()
        section[InSectionPosition(2, 3, 4)] = TestBlockStates.ENTITY1

        assertTrue(section.entities[2, 3, 4] is TestBlockEntities.TestBlockEntity)
    }


    fun `block entity correct data`() {
        val section = create()
        section[InSectionPosition(2, 3, 4)] = TestBlockStates.ENTITY1

        val entity = section.entities[2, 3, 4]!!

        assertEquals(entity.position, BlockPosition(2, 35, 4))
        assertSame(entity.state, TestBlockStates.ENTITY1)
    }

    fun `remove block entity when block is removed`() {
        val section = create()
        section[InSectionPosition(2, 3, 4)] = TestBlockStates.ENTITY1
        section[InSectionPosition(2, 3, 4)] = null

        assertNull(section.entities[2, 3, 4])
    }

    fun `remove block entity when block is replaced`() {
        val section = create()
        section[InSectionPosition(2, 3, 4)] = TestBlockStates.ENTITY1
        section[InSectionPosition(2, 3, 4)] = TestBlockStates.TEST1

        assertNull(section.entities[2, 3, 4])
    }

    fun `replace block entity if block is different`() {
        val section = create()
        section[InSectionPosition(2, 3, 4)] = TestBlockStates.ENTITY1

        val entity = section.entities[2, 3, 4]

        section[InSectionPosition(2, 3, 4)] = TestBlockStates.ENTITY2

        assertNotSame(entity, section.entities[2, 3, 4])
    }

    fun `keep block entity if only state is different`() {
        val section = create()
        section[InSectionPosition(2, 3, 4)] = TestBlockStates.ENTITY1

        val entity = section.entities[2, 3, 4]!!

        val state = PropertyBlockState(TestBlocks.ENTITY1, mapOf(BlockProperties.AXIS to Axes.X), 4)
        section[InSectionPosition(2, 3, 4)] = state

        assertSame(entity, section.entities[2, 3, 4])
        assertSame(entity.state, state)
    }

    fun `fire block change when block is set`() {
        val section = create()

        val updates = section.chunk.world.collectUpdates()

        section[InSectionPosition(2, 3, 4)] = TestBlockStates.TEST1

        assertEquals(updates, listOf(
            SingleBlockUpdate(section.chunk, BlockPosition(2, 35, 4), TestBlockStates.TEST1),
        ))
    }

    fun `fire block change when block is removed`() {
        val section = create()
        section[InSectionPosition(2, 3, 4)] = TestBlockStates.TEST1

        val updates = section.chunk.world.collectUpdates()

        section[InSectionPosition(2, 3, 4)] = null

        assertEquals(updates, listOf(
            SingleBlockUpdate(section.chunk, BlockPosition(2, 35, 4), null),
        ))
    }

    fun `fire block change when block is replaced`() {
        val section = create()
        section[InSectionPosition(2, 3, 4)] = TestBlockStates.TEST1

        val updates = section.chunk.world.collectUpdates()

        section[InSectionPosition(2, 3, 4)] = TestBlockStates.TEST2

        assertEquals(updates, listOf(
            SingleBlockUpdate(section.chunk, BlockPosition(2, 35, 4), TestBlockStates.TEST2),
        ))
    }

    fun `no event if change is already present`() {
        val section = create()
        section[InSectionPosition(2, 3, 4)] = TestBlockStates.TEST1

        val updates = section.chunk.world.collectUpdates()

        section[InSectionPosition(2, 3, 4)] = TestBlockStates.TEST1

        assertEquals(updates, listOf<AbstractWorldUpdate>())
    }
}
