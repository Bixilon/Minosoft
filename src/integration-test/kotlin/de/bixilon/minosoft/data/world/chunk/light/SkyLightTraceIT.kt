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

package de.bixilon.minosoft.data.world.chunk.light

import de.bixilon.minosoft.data.world.chunk.light.LightTestUtil.assertLight
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.test.IT
import org.testng.annotations.Test


@Test(groups = ["light"], dependsOnGroups = ["block"], threadPoolSize = 8, priority = 1000)
class SkyLightTraceIT {

    fun `check level below block`() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = IT.BLOCK_1
        world.assertLight(8, 9, 8, 0xE0)
    }

    fun `heightmap optimization west, upper block set`() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = IT.BLOCK_1
        world[BlockPosition(7, 12, 8)] = IT.BLOCK_1
        world.assertLight(7, 11, 8, 0xE0)
    }

    fun `heightmap optimization west, upper block set 2`() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 255, 8)] = IT.BLOCK_1
        world[BlockPosition(7, 1, 8)] = IT.BLOCK_1
        for (y in 0..254) {
            world.assertLight(8, y, 8, 0xE0)
        }
    }

    fun `heightmap optimization east, upper block set`() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = IT.BLOCK_1
        world[BlockPosition(9, 12, 8)] = IT.BLOCK_1
        world.assertLight(9, 11, 8, 0xE0)
    }

    fun `heightmap optimization north, upper block set`() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = IT.BLOCK_1
        world[BlockPosition(8, 12, 7)] = IT.BLOCK_1
        world.assertLight(8, 11, 7, 0xE0)
    }

    fun `heightmap optimization south, upper block set`() {
        val world = createSession(3, light = true).world
        world[BlockPosition(8, 10, 8)] = IT.BLOCK_1
        world[BlockPosition(8, 12, 9)] = IT.BLOCK_1
        world.assertLight(8, 11, 9, 0xE0)
    }
}
