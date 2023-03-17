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

package de.bixilon.minosoft.data.physics

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.local.Abilities
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class SpectatorTest {

    fun spectatorTest1() {
        val player = createPlayer(createConnection(2))
        player.forceTeleport(Vec3d(0.0, 5.0, 0.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(1)
        player.assertPosition(0.0, 5.0, 0.01959999994337558, 1)
        player.assertVelocity(0.0, -0.0784000015258789, 0.017836000462502232, 1)
    }

    fun spectatorTest2() {
        val player = createPlayer(createConnection(2))
        player.forceTeleport(Vec3d(12.0, 5.0, 12.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)
        player.assertPosition(12.0, 1.6537296175885952, 12.833287203024394, 10)
        player.assertVelocity(0.0, -0.7170746714356033, 0.1210041730153868, 10)
    }

    fun spectatorTest3() {
        val player = createPlayer(createConnection(2))
        player.forceTeleport(Vec3d(12.0, 5.0, 12.0))
        player.input = PlayerMovementInput(forward = true, left = true)
        player.forceRotate(EntityRotation(12.0f, 60.0f))
        player.runTicks(10)
        player.assertPosition(12.463139249994395, 1.6537296175885952, 12.713092076628122, 10)
        player.assertVelocity(0.06725386125352226, -0.7170746714356033, 0.10355027258673084, 10)
    }

    fun spectatorTest4() {
        val player = createPlayer(createConnection(2))
        player.forceTeleport(Vec3d(12.0, 5.0, -19.0))
        player.input = PlayerMovementInput(forward = true, right = true)
        player.forceRotate(EntityRotation(12.0f, 60.0f))
        player.runTicks(100)
        player.assertPosition(-4.752302242873709, -216.99353744489684, -8.11970985131264, 100)
        player.assertVelocity(-0.16957864530475542, -3.40013363788066, 0.11013798803217949, 100)
    }

    fun spectatorTest5() {
        val player = createPlayer(createConnection(2))
        player.forceTeleport(Vec3d(12.0, 5.0, -19.0))
        player.input = PlayerMovementInput(forward = true, right = true)
        player.abilities = Abilities(flying = true)
        player.forceRotate(EntityRotation(12.0f, 60.0f))
        player.runTicks(100)
        player.assertPosition(-29.880757167364045, 5.0, 8.200726385024396, 100)
        player.assertVelocity(-0.42394662905513114, 0.0, 0.2753449803378482, 100)
    }
}
