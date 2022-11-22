/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.chunk.light.place.block

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.minosoft.data.registries.blocks.StoneTestO
import de.bixilon.minosoft.data.registries.blocks.TorchTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.modding.event.events.blocks.chunk.LightChangeEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil
import org.testng.Assert.assertEquals
import org.testng.annotations.Test


@Test(groups = ["light"], dependsOnGroups = ["block"])
class BlockLightPlaceIT {

    fun inBlock() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(8, 10, 8)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(8, 10, 8)), 0xFD)
    }

    fun nextToBlock1() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(8, 10, 8)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(8, 9, 8)), 0xFC)
        assertEquals(world.getLight(Vec3i(8, 0, 8)), 0xF4)
    }

    fun nextToBlock2() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(8, 10, 8)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(8, 11, 8)), 0xFC)
    }

    fun nextToBlock3() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(8, 10, 8)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(7, 10, 8)), 0xFC)
    }

    fun nextToBlock4() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(8, 10, 8)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(9, 10, 8)), 0xFC)
    }

    fun nextToBlock5() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(8, 10, 8)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(8, 10, 7)), 0xFC)
    }

    fun nextToBlock6() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(8, 10, 8)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(8, 10, 9)), 0xFC)
    }

    fun nextNeighbour1() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(16, 16, 16)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(16, 17, 16)), 0xFC)
    }

    fun nextNeighbour2() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(16, 16, 16)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(17, 16, 16)), 0xFC)
    }

    fun nextNeighbour3() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(16, 16, 16)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(16, 16, 17)), 0xFC)
    }

    fun nextNeighbour4() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(17, 17, 17)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(17, 16, 17)), 0xFC)
    }

    fun nextNeighbour5() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(17, 17, 17)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(16, 17, 17)), 0xFC)
    }

    fun nextNeighbour6() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(17, 17, 17)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(17, 17, 16)), 0xFC)
    }

    fun totalPropagation1() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(12, 20, 12)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(12, 7, 12)), 0xF1)
    }

    fun totalPropagation2() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(12, 20, 12)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(12, 33, 12)), 0xF1)
    }

    fun totalPropagation3() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(12, 20, 12)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(-1, 33, 12)), 0xF1)
    }

    fun totalPropagation4() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(12, 20, 12)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(25, 33, 12)), 0xF1)
    }

    fun totalPropagation5() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(12, 20, 12)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(12, 33, 25)), 0xF1)
    }

    fun totalPropagation6() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(12, 20, 12)] = TorchTest0.state
        assertEquals(world.getLight(Vec3i(12, 33, -1)), 0xF1)
    }

    fun lightUpdate() {
        val world = ConnectionTestUtil.createConnection(3).world
        val events: MutableSet<Vec3i> = synchronizedSetOf()
        world.connection.events.listen<LightChangeEvent> {
            events += Vec3i(it.chunkPosition.x, it.sectionHeight, it.chunkPosition.y)
        }
        world[Vec3i(8, 24, 8)] = TorchTest0.state

        assertEquals(
            events, setOf(
                Vec3i(+0, 1, +0),
                Vec3i(+0, 0, +0),
                Vec3i(+0, 2, +0),
                Vec3i(+0, 1, -1),
                Vec3i(+0, 1, +1),
                Vec3i(-1, 1, +0),
                Vec3i(+1, 1, +0),
            )
        )
    }


    fun bottomLight() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(8, 0, 8)] = TorchTest0.state
        val chunk = world[Vec2i(0, 0)]!!
        assertEquals(chunk.light[Vec3i(8, -1, 8)], 0xFC)
        assertEquals(chunk.light[Vec3i(9, -1, 8)], 0xFB)
    }


    fun topLight() {
        val world = ConnectionTestUtil.createConnection(3).world
        world[Vec3i(8, 255, 8)] = TorchTest0.state
        val chunk = world[Vec2i(0, 0)]!!
        assertEquals(chunk.light[Vec3i(8, 256, 8)], 0xFC)
        assertEquals(chunk.light[Vec3i(9, 256, 8)], 0xFB)
    }

    fun bottomPropagation() {
        val world = ConnectionTestUtil.createConnection(5).world
        world.fill(Vec3i(-10, 0, -10), Vec3i(30, 1, 30), StoneTestO.state)
        world[Vec3i(8, 0, 8)] = TorchTest0.state

        val chunk = world[Vec2i(0, 0)]!!
        assertEquals(chunk.light[Vec3i(8, -1, 8)], 0x0C)
        assertEquals(chunk.light[Vec3i(9, -1, 8)], 0x0B)

        assertEquals(chunk.light[Vec3i(20, -1, 8)], 0x01)
        assertEquals(chunk.light[Vec3i(8, -1, 8)], 0x01)
        assertEquals(chunk.light[Vec3i(-4, -1, 20)], 0x01)
        assertEquals(chunk.light[Vec3i(8, -1, -4)], 0x01)
    }

    fun topPropagation() {
        val world = ConnectionTestUtil.createConnection(4).world
        world.fill(Vec3i(-10, 254, -10), Vec3i(30, 255, 30), StoneTestO.state)
        world[Vec3i(8, 255, 8)] = TorchTest0.state

        val chunk = world[Vec2i(0, 0)]!!
        assertEquals(chunk.light[Vec3i(8, 256, 8)], 0xFC)
        assertEquals(chunk.light[Vec3i(9, 256, 8)], 0xFB)

        assertEquals(chunk.light[Vec3i(20, 256, 8)], 0xF1)
        assertEquals(chunk.light[Vec3i(8, 256, 8)], 0xF1)
        assertEquals(chunk.light[Vec3i(-4, 256, 20)], 0xF1)
        assertEquals(chunk.light[Vec3i(8, 256, -4)], 0xF1)
    }
}
