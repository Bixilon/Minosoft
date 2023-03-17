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

package de.bixilon.minosoft.data.physics.gravity

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.GlassTest0
import de.bixilon.minosoft.data.registries.blocks.SlabTest0
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class CollisionIT {

    fun collisionGlass1() {
        val player = createPlayer(createConnection(2))
        player.connection.world[Vec3i(0, 4, 0)] = GlassTest0.state
        player.forceTeleport(Vec3d(0.0, 5.0, 0.0))
        player.runTicks(10)
        player.assertPosition(0.0, 5.0, 0.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun collisionGlass2() {
        val player = createPlayer(createConnection(2))
        player.connection.world[Vec3i(0, 2, 0)] = GlassTest0.state
        player.forceTeleport(Vec3d(0.0, 5.0, 0.0))
        player.runTicks(10)
        player.assertPosition(0.0, 3.0, 0.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun collisionGlass3() {
        val player = createPlayer(createConnection(2))
        player.connection.world[Vec3i(0, 0, 0)] = GlassTest0.state
        player.forceTeleport(Vec3d(0.0, 300.0, 0.0))
        player.runTicks(1000)
        player.assertPosition(0.0, 1.0, 0.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun collisionSlab() {
        val player = createPlayer(createConnection(2))
        player.connection.world[Vec3i(0, 0, 0)] = SlabTest0.state
        player.forceTeleport(Vec3d(0.0, 2.0, 0.0))
        player.runTicks(10)
        player.assertPosition(0.0, 0.5, 0.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun collisionWithHighVelocity() {
        val player = createPlayer(createConnection(2))
        player.connection.world[Vec3i(0, 1, 0)] = SlabTest0.state
        player.forceTeleport(Vec3d(0.0, 3.0, 0.0))
        player.physics.velocity = Vec3d(0.0, -15.0, 0.0)
        player.runTicks(10)
        player.assertPosition(0.0, 1.5, 0.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun collisionWithReallyHighVelocity() {
        val player = createPlayer(createConnection(2))
        player.connection.world[Vec3i(0, 1, 0)] = SlabTest0.state
        player.forceTeleport(Vec3d(0.0, 3.0, 0.0))
        player.physics.velocity = Vec3d(0.0, -150.0, 0.0)
        player.runTicks(10)
        player.assertPosition(0.0, 1.5, 0.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }
}
