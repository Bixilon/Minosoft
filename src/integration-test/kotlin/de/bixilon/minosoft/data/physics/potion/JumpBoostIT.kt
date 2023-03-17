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

package de.bixilon.minosoft.data.physics.potion

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.StatusEffectInstance
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.registries.effects.movement.MovementEffect
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class JumpBoostIT {

    private fun LocalPlayerEntity.applyJumpBoost(level: Int) {
        effects += StatusEffectInstance(MovementEffect.JumpBoost, level, 1000000)
    }

    fun jumpBoost1() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.applyJumpBoost(1)
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(10)
        player.assertPosition(17.0, 11.516793983214157, 8.0)
        player.assertVelocity(0.0, -0.0575358540000684, 0.0)
    }

    fun jumpBoost2() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.applyJumpBoost(2)
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(10)
        player.assertPosition(17.0, 12.262978986309507, 8.0)
        player.assertVelocity(0.0, 0.02754046911107082, 0.0)
    }

    fun jumpBoost10() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.applyJumpBoost(10)
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(10)
        player.assertPosition(17.0, 18.232458566311415, 8.0)
        player.assertVelocity(0.0, 0.7081510032907486, 0.0)
    }

    fun jumpBoost100() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.applyJumpBoost(100)
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(10)
        player.assertPosition(17.0, 85.38910550918618, 8.0)
        player.assertVelocity(0.0, 8.365019702972509, 0.0)
    }
}
