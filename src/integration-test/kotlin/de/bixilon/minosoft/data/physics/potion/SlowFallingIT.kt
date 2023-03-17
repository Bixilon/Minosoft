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
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.StatusEffectInstance
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.SlabTest0
import de.bixilon.minosoft.data.registries.effects.movement.MovementEffect
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class SlowFallingIT {

    fun slowFallingFalling10() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applySlowFalling()
        player.runTicks(10)
        player.assertPosition(12.0, 8.581716202198574, 4.0)
        player.assertVelocity(0.0, -0.08963433392945042, 0.0)
    }

    fun slowFallingFalling20() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applySlowFalling()
        player.runTicks(20)
        player.assertPosition(12.0, 7.34360447964139, 4.0)
        player.assertVelocity(0.0, -0.16287212500076237, 0.0)
    }

    fun slowFallingFalling30() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applySlowFalling()
        player.runTicks(30)
        player.assertPosition(12.0, 5.435633523066263, 4.0)
        player.assertVelocity(0.0, -0.2227127441682665, 0.0)
    }

    fun slowFallingFalling819() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 1731.0, 4.0))
        player.applySlowFalling()
        player.runTicks(819)
        player.assertPosition(12.0, 1354.1896550798454, 4.0)
        player.assertVelocity(0.0, -0.49000044489580974, 0.0)
    }

    fun slowFallingMovement1() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applySlowFalling()
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(22)
        player.assertPosition(12.0, 7.011317668842605, 6.865653977122466)
        player.assertVelocity(0.0, -0.17582639550412438, 0.17329121596800717)
    }

    fun slowFallingMovement2() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applySlowFalling()
        player.input = PlayerMovementInput(forward = true, left = true)
        player.runTicks(22)
        player.assertPosition(14.067676857469072, 7.011317668842605, 6.067676857469071)
        player.assertVelocity(0.12503611382261814, -0.17582639550412438, 0.12503611382261814)
    }

    fun slowFallingMovement3() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applySlowFalling()
        player.input = PlayerMovementInput(forward = true, left = true)
        player.forceRotate(EntityRotation(112.0f, 2.0f))
        player.runTicks(21)
        player.assertPosition(9.487287028821642, 7.180732354640627, 5.066861381758541)
        player.assertVelocity(-0.16045254496855785, -0.16941468579802124, 0.06812581691396043)
    }

    fun slowFallingMovement4() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applySlowFalling()
        player.input = PlayerMovementInput(backward = true, left = true)
        player.forceRotate(EntityRotation(87.0f, 29.0f))
        player.runTicks(21)
        player.assertPosition(14.028803648871559, 7.180732354640627, 5.826437788086396)
        player.assertVelocity(0.12955188771532442, -0.16941468579802124, 0.11662955327037396)
    }

    fun slowFallingMovement5() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(7.0, 9.0, 4.0))
        player.applySlowFalling()
        player.input = PlayerMovementInput(backward = true, right = true)
        player.forceRotate(EntityRotation(1.0f, 1.0f))
        player.runTicks(19)
        player.assertPosition(5.367725707227726, 7.499800522325905, 2.3097449032621284)
        player.assertVelocity(-0.11706669618255752, -0.1561960426845147, -0.12122507887124932)
    }

    fun slowFallingCollisionMovement1() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.connection.world[Vec3i(12, 7, 4)] = SlabTest0.top
        player.applySlowFalling()
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(67)
        player.assertPosition(12.0, -5.658685464394173, 16.39310681036411)
        player.assertVelocity(0.0, -0.36342658308358955, 0.19782070829555476)
    }

    companion object {

        fun LocalPlayerEntity.applySlowFalling() {
            effects += StatusEffectInstance(MovementEffect.SlowFalling, 1, 1000000)
        }
    }
}
