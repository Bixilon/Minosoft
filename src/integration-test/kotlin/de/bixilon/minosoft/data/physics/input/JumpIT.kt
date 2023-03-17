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

package de.bixilon.minosoft.data.physics.input

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class JumpIT {

    fun jumpStill1() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(1)
        player.assertPosition(17.0, 9.0, 8.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun jumpStill3() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(3)
        player.assertPosition(17.0, 9.419999986886978, 8.0)
        player.assertVelocity(0.0, 0.33319999363422365, 0.0)
        player.assertGround(false)
    }

    fun jumpStill10() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(10)
        player.assertPosition(17.0, 10.024424088213681, 8.0)
        player.assertVelocity(0.0, -0.22768848754498797, 0.0)
        player.assertGround(false)
    }

    fun jumpStill13() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(13)
        player.assertPosition(17.0, 9.121296840539191, 8.0)
        player.assertVelocity(0.0, -0.4448259643949201, 0.0)
        player.assertGround(false)
    }

    fun jumpStill14() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(14)
        player.assertPosition(17.0, 9.0, 8.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(true)
    }

    fun jumpStill15() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(15)
        player.assertPosition(17.0, 9.419999986886978, 8.0)
        player.assertVelocity(0.0, 0.33319999363422365, 0.0)
        player.assertGround(false)
    }

    fun jumpStill20() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(20)
        player.assertPosition(17.0, 10.252203340253725, 8.0)
        player.assertVelocity(0.0, -0.07544406518948656, 0.0)
        player.assertGround(false)
    }

    fun jumpStill200() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(200)
        player.assertPosition(17.0, 10.252203340253725, 8.0)
        player.assertVelocity(0.0, -0.07544406518948656, 0.0)
        player.assertGround(false)
    }

    fun jumpStill278() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(278)
        player.assertPosition(17.0, 9.0, 8.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(true)
    }

    fun jumpStill400() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(400)
        player.assertPosition(17.0, 9.753199980521202, 8.0)
        player.assertVelocity(0.0, 0.24813599859094576, 0.0)
        player.assertGround(false)
    }
}
