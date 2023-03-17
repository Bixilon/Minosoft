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

package de.bixilon.minosoft.data.physics.health

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.damage
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.kill
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class DamageMovementIT {

    fun deathMovement() {
        val player = createPlayer(createConnection(2))
        player.connection.world[Vec3i(0, 4, 0)] = StoneTest0.state
        player.forceTeleport(Vec3d(0.0, 5.0, 0.0))
        player.kill()
        player.input = PlayerMovementInput(forward = true)

        player.runTicks(10)

        player.assertPosition(0.0, 5.0, 0.0)
        // player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        // player.assertGround()
    }

    fun damageMovement1() {
        val player = createPlayer(createConnection(2))
        player.forceTeleport(Vec3d(0.0, 5.0, 0.0))
        player.connection.world[Vec3i(0, 4, 0)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true)

        player.runTicks(2)
        player.damage()
        player.runTicks(5)

        player.assertPosition(0.0, 5.0, 0.9607227240030559)
        player.assertVelocity(0.0, -0.0784000015258789, 0.11379306296185036)
        player.assertGround()
    }

    fun damageMovement2() {
        val player = createPlayer(createConnection(2))
        player.forceTeleport(Vec3d(0.0, 5.0, 0.0))
        player.connection.world[Vec3i(0, 4, 0)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true)

        for (i in 0 until 10) {
            player.damage()
            player.runTicks(1)
        }

        player.assertPosition(0.0, 4.921599998474121, 1.6008017491988142)
        player.assertVelocity(0.0, -0.1552320045166016, 0.11719723621935406)
        player.assertGround(false)
    }

    fun damageJump1() {
        val player = createPlayer(createConnection(2))
        player.forceTeleport(Vec3d(0.0, 5.0, 0.0))
        player.connection.world[Vec3i(0, 4, 0)] = StoneTest0.state
        player.input = PlayerMovementInput(jump = true)

        player.damage()
        player.runTicks(1)
        player.damage()
        player.runTicks(10)

        player.assertPosition(0.0, 5.796735600668692, 0.0)
        player.assertVelocity(0.0, -0.30153472366278034, 0.0)
        player.assertGround(false)
    }

    fun damageJump2() {
        val player = createPlayer(createConnection(2))
        player.forceTeleport(Vec3d(0.0, 5.0, 0.0))
        player.connection.world[Vec3i(0, 4, 0)] = StoneTest0.state
        player.input = PlayerMovementInput(jump = true)

        for (i in 0 until 10) {
            player.damage()
            player.runTicks(1)
        }

        player.assertPosition(0.0, 6.02442408821368, 0.0)
        player.assertVelocity(0.0, -0.22768848754498797, 0.0)
        player.assertGround(false)
    }
}
