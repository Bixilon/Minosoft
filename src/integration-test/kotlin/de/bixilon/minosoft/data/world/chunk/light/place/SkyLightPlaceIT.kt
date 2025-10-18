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

package de.bixilon.minosoft.data.world.chunk.light.place

import de.bixilon.minosoft.data.registries.blocks.GlassTest0
import de.bixilon.minosoft.data.registries.blocks.SlimeTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.data.world.chunk.light.LightTestUtil.assertLight
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.test.IT
import org.testng.annotations.Test


@Test(groups = ["light"], dependsOnGroups = ["block"], threadPoolSize = 8, priority = 1000)
class SkyLightPlaceIT {

    fun aboveBlock() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = IT.BLOCK_1
        world.assertLight(8, 11, 8, 0xF0)
    }

    fun inBlock() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = IT.BLOCK_1
        world.assertLight(8, 10, 8, 0x00)
    }

    fun belowBlock() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = IT.BLOCK_1
        world.assertLight(8, 9, 8, 0xE0)
    }

    fun `below block 1`() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 16, 8)] = IT.BLOCK_1
        world.assertLight(8, 15, 8, 0xE0)
    }

    fun `below block 2`() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 0, 8)] = IT.BLOCK_1
        world.assertLight(8, -1, 8, 0xE0)
    }

    fun `below block 3`() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 15, 8)] = IT.BLOCK_1
        world.assertLight(8, 14, 8, 0xE0)
    }

    fun `more blocks below block`() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 37, 8)] = IT.BLOCK_1
        for (y in 0..36) {
            world.assertLight(8, y, 8, 0xE0)
        }
    }

    fun belowBlock3() {
        val world = createSession(3, light = true).world
        world.fill(7, 10, 7, 9, 10, 9, IT.BLOCK_1, false)
        // world.chunks[0,0]!!.light.reset()
        // world.chunks[0,0]!!.light.sky.calculate()
        world.assertLight(8, 9, 8, 0xD0)
    }

    fun filteredInBlock() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = SlimeTest0.state
        world.assertLight(8, 10, 8, 0xE0)
    }

    fun filteredBelowBlock5() {
        val world = createSession(3, light = true).world
        world.fill(6, 10, 6, 10, 10, 10, SlimeTest0.state, false)
        world.chunks[0, 0]!!.light.clear()
        world.chunks[0, 0]!!.light.calculate()
        world.assertLight(8, 11, 8, 0xF0)
        world.assertLight(8, 10, 8, 0xE0)
        world.assertLight(8, 9, 8, 0xD0)
    }

    fun filtered2BelowBlock3() {
        val world = createSession(3, light = true).world
        world.fill(7, 10, 7, 9, 10, 9, SlimeTest0.state, false)
        world.assertLight(8, 8, 8, 0xD0)
    }

    fun transparentInBlock() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = GlassTest0.state
        world.assertLight(8, 10, 8, 0xF0)
    }

    fun transparentBelowBlock() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = GlassTest0.state
        world.assertLight(8, 9, 8, 0xF0)
    }

    fun belowSection() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 16, 8)] = IT.BLOCK_1
        world.assertLight(8, 15, 8, 0xE0)
    }

    // TODO: section borders, border light, directed light, distance, max propagation
}
