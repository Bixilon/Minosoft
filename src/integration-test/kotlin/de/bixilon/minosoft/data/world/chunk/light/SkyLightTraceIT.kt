/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.chunk.light.LightTestUtil.assertLight
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test


@Test(groups = ["light"], dependsOnGroups = ["block"], threadPoolSize = 8, priority = 1000)
class SkyLightTraceIT {

    fun `check level below block`() {
        val world = createConnection(3, light = true).world
        world[Vec3i(8, 10, 8)] = StoneTest0.state
        world.assertLight(8, 9, 8, 0xE0)
    }

    fun `heightmap optimization west, upper block set`() {
        val world = createConnection(3, light = true).world
        world[Vec3i(8, 10, 8)] = StoneTest0.state
        world[Vec3i(7, 12, 8)] = StoneTest0.state
        world.assertLight(7, 11, 8, 0xE0)
    }

    fun `heightmap optimization west, upper block set 2`() {
        val world = createConnection(3, light = true).world
        world[Vec3i(8, 255, 8)] = StoneTest0.state
        world[Vec3i(7, 1, 8)] = StoneTest0.state
        for (y in 0..254) {
            world.assertLight(8, y, 8, 0xE0)
        }
    }

    fun `heightmap optimization east, upper block set`() {
        val world = createConnection(3, light = true).world
        world[Vec3i(8, 10, 8)] = StoneTest0.state
        world[Vec3i(9, 12, 8)] = StoneTest0.state
        world.assertLight(9, 11, 8, 0xE0)
    }

    fun `heightmap optimization north, upper block set`() {
        val world = createConnection(3, light = true).world
        world[Vec3i(8, 10, 8)] = StoneTest0.state
        world[Vec3i(8, 12, 7)] = StoneTest0.state
        world.assertLight(8, 11, 7, 0xE0)
    }

    fun `heightmap optimization south, upper block set`() {
        val world = createConnection(3, light = true).world
        world[Vec3i(8, 10, 8)] = StoneTest0.state
        world[Vec3i(8, 12, 9)] = StoneTest0.state
        world.assertLight(8, 11, 9, 0xE0)
    }
}
