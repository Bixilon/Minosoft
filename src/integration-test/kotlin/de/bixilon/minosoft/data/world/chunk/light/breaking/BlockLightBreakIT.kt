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

package de.bixilon.minosoft.data.world.chunk.light.breaking

import de.bixilon.kmath.vec.vec3.i.SVec3i
import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.minosoft.data.registries.blocks.TorchTest0
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.data.world.chunk.light.LightTestUtil.assertLight
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkLightUpdate
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.annotations.Test


@Test(groups = ["light"], dependsOnGroups = ["block"], threadPoolSize = 8, priority = 1000)
class BlockLightBreakIT {

    fun inBlock() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = TorchTest0.state
        world[BlockPosition(8, 10, 8)] = null
        world.assertLight(8, 10, 8, 0xF0)
    }

    fun nextToBlock1() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = TorchTest0.state
        world[BlockPosition(8, 10, 8)] = null
        world.assertLight(8, 9, 8, 0xF0)
        world.assertLight(8, 0, 8, 0xF0)
    }

    fun nextToBlock2() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = TorchTest0.state
        world[BlockPosition(8, 10, 8)] = null
        world.assertLight(8, 11, 8, 0xF0)
    }

    fun nextToBlock3() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = TorchTest0.state
        world[BlockPosition(8, 10, 8)] = null
        world.assertLight(7, 10, 8, 0xF0)
    }

    fun nextToBlock4() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = TorchTest0.state
        world[BlockPosition(8, 10, 8)] = null
        world.assertLight(9, 10, 8, 0xF0)
    }

    fun nextToBlock5() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = TorchTest0.state
        world[BlockPosition(8, 10, 8)] = null
        world.assertLight(8, 10, 7, 0xF0)
    }

    fun nextToBlock6() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = TorchTest0.state
        world[BlockPosition(8, 10, 8)] = null
        world.assertLight(8, 10, 9, 0xF0)
    }

    fun nextNeighbour1() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(16, 16, 16)] = TorchTest0.state
        world[BlockPosition(16, 16, 16)] = null
        world.assertLight(16, 17, 16, 0xF0)
    }

    fun nextNeighbour2() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(16, 16, 16)] = TorchTest0.state
        world[BlockPosition(16, 16, 16)] = null
        world.assertLight(17, 16, 16, 0xF0)
    }

    fun nextNeighbour3() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(16, 16, 16)] = TorchTest0.state
        world[BlockPosition(16, 16, 16)] = null
        world.assertLight(16, 16, 17, 0xF0)
    }

    fun nextNeighbour4() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(17, 17, 17)] = TorchTest0.state
        world[BlockPosition(17, 17, 17)] = null
        world.assertLight(17, 16, 17, 0xF0)
    }

    fun nextNeighbour5() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(17, 17, 17)] = TorchTest0.state
        world[BlockPosition(17, 17, 17)] = null
        world.assertLight(16, 17, 17, 0xF0)
    }

    fun nextNeighbour6() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(17, 17, 17)] = TorchTest0.state
        world[BlockPosition(17, 17, 17)] = null
        world.assertLight(17, 17, 16, 0xF0)
    }

    fun lowerSection() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(8, 16, 8)] = TorchTest0.state
        world[BlockPosition(8, 16, 8)] = null
        world.assertLight(8, 15, 8, 0xF0)
    }

    fun upperSection() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(8, 15, 8)] = TorchTest0.state
        world[BlockPosition(8, 15, 8)] = null
        world.assertLight(8, 16, 8, 0xF0)
    }

    fun totalPropagation1() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(12, 20, 12)] = TorchTest0.state
        world[BlockPosition(12, 20, 12)] = null
        world.assertLight(12, 7, 12, 0xF0)
    }

    fun totalPropagation2() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(12, 20, 12)] = TorchTest0.state
        world[BlockPosition(12, 20, 12)] = null
        world.assertLight(12, 33, 12, 0xF0)
    }

    fun totalPropagation3() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(12, 20, 12)] = TorchTest0.state
        world[BlockPosition(12, 20, 12)] = null
        world.assertLight(-1, 20, 12, 0xF0)
    }

    fun totalPropagation4() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(12, 20, 12)] = TorchTest0.state
        world[BlockPosition(12, 20, 12)] = null
        world.assertLight(25, 20, 12, 0xF0)
    }

    fun totalPropagation5() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(12, 20, 12)] = TorchTest0.state
        world[BlockPosition(12, 20, 12)] = null
        world.assertLight(12, 20, 25, 0xF0)
    }

    fun totalPropagation6() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(12, 20, 12)] = TorchTest0.state
        world[BlockPosition(12, 20, 12)] = null
        world.assertLight(12, 20, -1, 0xF0)
    }

    fun lightUpdate() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(8, 24, 8)] = TorchTest0.state
        val events: MutableList<SVec3i> = synchronizedListOf()
        world.session.events.listen<WorldUpdateEvent> {
            if (it.update !is ChunkLightUpdate) return@listen
            events += SVec3i(it.update.chunk.position.x, (it.update as ChunkLightUpdate).sectionHeight, it.update.chunk.position.z)
        }
        world[BlockPosition(8, 24, 8)] = null

        assertEquals(events.toSet(), setOf(
            SVec3i(+0, 1, +0),
            SVec3i(+0, 0, +0),
            SVec3i(+0, 2, +0),
            SVec3i(+0, 1, -1),
            SVec3i(+0, 1, +1),
            SVec3i(-1, 1, +0),
            SVec3i(+1, 1, +0),
        ))
        assertEquals(events.size, 7)
    }


    fun bottomLight() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(8, 0, 8)] = TorchTest0.state
        world[BlockPosition(8, 0, 8)] = null
        val chunk = world.chunks[ChunkPosition(0, 0)]!!
        chunk.assertLight(8, -1, 8, 0xF0)
        chunk.assertLight(9, -1, 8, 0xF0)
    }


    fun topLight() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world[BlockPosition(8, 255, 8)] = TorchTest0.state
        world[BlockPosition(8, 255, 8)] = null
        val chunk = world.chunks[ChunkPosition(0, 0)]!!
        chunk.assertLight(8, 256, 8, 0xF0)
        chunk.assertLight(9, 256, 8, 0xF0)
    }

    fun bottomPropagation() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world.fill(BlockPosition(-20, 0, -20), BlockPosition(40, 1, 40), IT.BLOCK_1)
        world[BlockPosition(8, 0, 8)] = TorchTest0.state
        world[BlockPosition(8, 0, 8)] = null

        world.assertLight(8, -1, 8, 0x00)
        world.assertLight(9, -1, 8, 0x00)

        world.assertLight(+20, -1, +8, 0x00)
        world.assertLight(+8, -1, +8, 0x00)
        world.assertLight(-4, -1, +8, 0x00)
        world.assertLight(+8, -1, -4, 0x00)
    }

    fun topPropagation() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world.fill(BlockPosition(-20, 254, -20), BlockPosition(40, 255, 40), IT.BLOCK_1)
        world[BlockPosition(8, 255, 8)] = TorchTest0.state
        world[BlockPosition(8, 255, 8)] = null

        world.assertLight(8, 256, 8, 0xF0)
        world.assertLight(9, 256, 8, 0xF0)

        world.assertLight(+20, 256, +8, 0xF0)
        world.assertLight(+8, 256, +20, 0xF0)
        world.assertLight(-4, 256, +8, 0xF0)
        world.assertLight(+8, 256, -4, 0xF0)
    }

    fun `increase on break down 1`() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world.fill(BlockPosition(-20, 254, -20), BlockPosition(40, 255, 40), IT.BLOCK_1)
        world[BlockPosition(8, 7, 8)] = StoneTest0.state
        world[BlockPosition(8, 8, 8)] = TorchTest0.state
        world.assertLight(8, 6, 8, 0x0A)
        world[BlockPosition(8, 7, 8)] = null
        world.assertLight(8, 6, 8, 0x0C)
    }

    fun `increase on break down 2`() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world.fill(BlockPosition(-20, 254, -20), BlockPosition(40, 255, 40), IT.BLOCK_1)
        world[BlockPosition(8, 8, 8)] = TorchTest0.state
        world[BlockPosition(8, 7, 8)] = StoneTest0.state
        world.assertLight(8, 6, 8, 0x0A)
        world[BlockPosition(8, 7, 8)] = null
        world.assertLight(8, 6, 8, 0x0C)
    }

    fun `increase on break up`() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world.fill(BlockPosition(-20, 254, -20), BlockPosition(40, 255, 40), IT.BLOCK_1)
        world[BlockPosition(8, 8, 8)] = TorchTest0.state
        world[BlockPosition(8, 9, 8)] = StoneTest0.state
        world.assertLight(8, 10, 8, 0x0A)
        world[BlockPosition(8, 9, 8)] = null
        world.assertLight(8, 10, 8, 0x0C)
    }

    fun `increase on break west`() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world.fill(BlockPosition(-20, 254, -20), BlockPosition(40, 255, 40), IT.BLOCK_1)
        world[BlockPosition(8, 8, 8)] = TorchTest0.state
        world[BlockPosition(7, 8, 8)] = StoneTest0.state
        world.assertLight(6, 8, 8, 0x0A)
        world[BlockPosition(7, 8, 8)] = null
        world.assertLight(6, 8, 8, 0x0C)
    }

    fun `increase on break east`() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world.fill(BlockPosition(-20, 254, -20), BlockPosition(40, 255, 40), IT.BLOCK_1)
        world[BlockPosition(8, 8, 8)] = TorchTest0.state
        world[BlockPosition(9, 8, 8)] = StoneTest0.state
        world.assertLight(10, 8, 8, 0x0A)
        world[BlockPosition(9, 8, 8)] = null
        world.assertLight(10, 8, 8, 0x0C)
    }

    fun `increase on break north`() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world.fill(BlockPosition(-20, 254, -20), BlockPosition(40, 255, 40), IT.BLOCK_1)
        world[BlockPosition(8, 8, 8)] = TorchTest0.state
        world[BlockPosition(8, 8, 7)] = StoneTest0.state
        world.assertLight(8, 8, 6, 0x0A)
        world[BlockPosition(8, 8, 7)] = null
        world.assertLight(8, 8, 6, 0x0C)
    }

    fun `increase on break south`() {
        val world = SessionTestUtil.createSession(3, light = true).world
        world.fill(BlockPosition(-20, 254, -20), BlockPosition(40, 255, 40), IT.BLOCK_1)
        world[BlockPosition(8, 8, 8)] = TorchTest0.state
        world[BlockPosition(8, 8, 9)] = StoneTest0.state
        world.assertLight(8, 8, 10, 0x0A)
        world[BlockPosition(8, 8, 9)] = null
        world.assertLight(8, 8, 10, 0x0C)
    }
}
