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
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.player.local.Abilities
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.input.camera.MovementInputActions
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class FlyIT {

    fun flyLanding() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.5, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.abilities = Abilities(flying = true, allowFly = true)
        player.input = PlayerMovementInput(flyDown = true)
        player.runTicks(20)
        player.assertPosition(17.0, 9.0, 8.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(true)
        assertFalse(player.abilities.flying)
    }

    fun stopFlying() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.5, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.abilities = Abilities(flying = true, allowFly = true)

        player.input = PlayerMovementInput()
        player.runTicks(20)
        player.inputActions = MovementInputActions(toggleFly = true)
        player.runTicks(10)

        player.assertPosition(17.0, 9.0, 8.0)
        player.assertGround(true)
        assertFalse(player.abilities.flying)
    }

    fun startFlying() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.5, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.abilities = Abilities(flying = false, allowFly = true)

        player.inputActions = MovementInputActions(toggleFly = true)
        player.runTicks(1)

        player.assertGround(false)
        assertTrue(player.abilities.flying)
    }

    fun toggleMultipleTimes() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.5, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.abilities = Abilities(flying = false, allowFly = true)

        player.inputActions = MovementInputActions(toggleFly = true)
        player.runTicks(1)
        assertTrue(player.abilities.flying)
        player.inputActions = MovementInputActions(toggleFly = true)
        player.runTicks(1)
        assertFalse(player.abilities.flying)
        player.inputActions = MovementInputActions(toggleFly = true)
        player.runTicks(1)
        assertTrue(player.abilities.flying)
    }

    fun spectatorStopFly() {
        val player = createPlayer(createConnection(3))
        player.additional.gamemode = Gamemodes.SPECTATOR
        player.abilities = Abilities(allowFly = true, flying = true)
        player.forceTeleport(Vec3d(17.0, 9.5, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state

        player.inputActions = MovementInputActions(toggleFly = true)
        player.runTicks(1)
        player.assertGround(false)
        assertTrue(player.abilities.flying)

        player.inputActions = MovementInputActions(toggleFly = true)
        player.runTicks(1)
        player.assertGround(false)
        assertTrue(player.abilities.flying)
    }
}
