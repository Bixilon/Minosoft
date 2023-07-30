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
import de.bixilon.minosoft.data.entities.entities.player.local.HealthCondition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertEquals
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.WaterTest0
import de.bixilon.minosoft.data.registries.blocks.types.fluid.LavaFluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class SprintIT {

    fun noSprint() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput()
        player.runTicks(5)
        player.assertPosition(17.0, 9.0, 8.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        assertFalse(player.isSprinting)
    }

    fun noMovement() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(sprint = true)
        player.runTicks(5)
        player.assertPosition(17.0, 9.0, 8.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        assertFalse(player.isSprinting)
    }

    fun stopSprinting() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(3)
        player.input = PlayerMovementInput()
        player.runTicks(3)
        player.assertPosition(17.0, 9.0, 8.397700111502969)
        player.assertVelocity(0.0, -0.0784000015258789, 0.01482561015377492)
        assertFalse(player.isSprinting)
        assertEquals(player.physics().movementSpeed, 0.1f)
    }

    fun forwardsSprinting() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(5)
        player.assertPosition(17.0, 9.0, 8.694907422181794)
        player.assertVelocity(0.0, -0.0784000015258789, 0.13469353477365476)
        assertTrue(player.isSprinting)
    }

    fun hungerSprinting() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.healthCondition = HealthCondition(hunger = 1)
        player.runTicks(5)
        player.assertPosition(17.0, 9.0, 8.550090466546338)
        player.assertVelocity(0.0, -0.0784000015258789, 0.10422007506984735)
        assertFalse(player.isSprinting)
    }

    fun backwardsSprinting() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(backward = true, sprint = true)
        player.runTicks(5)
        player.assertPosition(17.0, 8.921599998474122, 7.449909533453662)
        player.assertVelocity(0.0, -0.1552320045166016, -0.10422007506984735)
        assertFalse(player.isSprinting)
    }

    fun sidewaysSprint() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(left = true, sprint = true)
        player.runTicks(5)
        player.assertPosition(17.55009046654634, 9.0, 8.0)
        player.assertVelocity(0.10422007506984735, -0.0784000015258789, 0.0)
        assertFalse(player.isSprinting)
    }

    fun collisionSprint() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.connection.world[Vec3i(17, 9, 9)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(20)
        player.assertPosition(17.0, 9.0, 8.699999988079071)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        assertFalse(player.isSprinting)
    }

    fun waterStartSprint() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world.fill(10, 8, 5, 20, 8, 15, StoneTest0.state)
        player.connection.world.fill(10, 9, 5, 20, 9, 15, WaterTest0.state)
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(20)
        player.assertPosition(17.0, 9.0, 9.572519513961863)
        player.assertVelocity(0.0, -0.005, 0.07749611482103191)
        player.assertGround()
        assertFalse(player.isSprinting)
    }

    fun waterStopSprint() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world.fill(10, 8, 5, 20, 8, 15, StoneTest0.state)
        player.connection.world.fill(10, 9, 9, 20, 9, 15, WaterTest0.state)
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(20)
        player.assertPosition(17.0, 9.0, 10.638288142020382)
        player.assertVelocity(0.0, -0.005, 0.08124567810261885)
        player.assertGround()
        assertFalse(player.isSprinting)
    }

    fun lavaStopSprint() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world.fill(10, 8, 5, 20, 8, 15, StoneTest0.state)
        player.connection.world.fill(10, 9, 9, 20, 9, 15, player.connection.registries.block[LavaFluidBlock]!!.states.default)
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(20)
        player.assertPosition(17.0, 9.0, 9.7527920785995)
        player.assertVelocity(0.0, -0.02, 0.01960753797398108)
        player.assertGround()
        assertTrue(player.isSprinting)
    }

    fun airSprint() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(5)
        player.assertPosition(17.0, 8.23152379758701, 8.314758828510087)
        player.assertVelocity(0.0, -0.37663049823865513, 0.09319171771884109)
        assertTrue(player.isSprinting)
    }
}
