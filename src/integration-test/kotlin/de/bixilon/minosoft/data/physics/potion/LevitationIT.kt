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
import de.bixilon.minosoft.data.registries.blocks.DirtTest0
import de.bixilon.minosoft.data.registries.blocks.SlabTest0
import de.bixilon.minosoft.data.registries.effects.movement.MovementEffect
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class LevitationIT {

    fun levitationFalling1() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applyLevitation(1)
        player.runTicks(10)
        player.assertPosition(12.0, 9.524167497370202, 4.0)
        player.assertVelocity(0.0, 0.08277983238089458, 0.0)
    }

    fun levitationFalling2() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applyLevitation(2)
        player.runTicks(20)
        player.assertPosition(12.0, 11.09692855198106, 4.0)
        player.assertVelocity(0.0, 0.13506347621277298, 0.0)
    }

    fun levitationFalling3() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applyLevitation(3)
        player.runTicks(20)
        player.assertPosition(12.0, 11.79590473597475, 4.0)
        player.assertVelocity(0.0, 0.180084634950364, 0.0)
    }

    fun levitationFalling12() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applyLevitation(12)
        player.runTicks(18)
        player.assertPosition(12.0, 16.920237149439117, 4.0)
        player.assertVelocity(0.0, 0.5824289412063375, 0.0)
    }

    fun levitationFalling90() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applyLevitation(90)
        player.runTicks(27)
        player.assertPosition(12.0, 101.38742311545764, 4.0)
        player.assertVelocity(0.0, 4.122918485416909, 0.0)
    }

    fun levitationFalling412() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.applyLevitation(3)
        player.runTicks(412)
        player.assertPosition(12.0, 82.93018492862176, 4.0)
        player.assertVelocity(0.0, 0.1814814978339229, 0.0)
    }

    fun levitationCollision1() {
        val connection = createConnection(3)
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        connection.world[Vec3i(12, 11, 4)] = DirtTest0.state
        player.applyLevitation(2)
        player.runTicks(16)
        player.assertPosition(12.0, 9.200000047683716, 4.0)
        player.assertVelocity(0.0, 0.029400000572204595, 0.0)
    }

    fun levitationCollision2() {
        val connection = createConnection(3)
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        connection.world[Vec3i(12, 12, 4)] = DirtTest0.state
        player.applyLevitation(34)
        player.runTicks(47)
        player.assertPosition(12.0, 10.200000047683716, 4.0)
        player.assertVelocity(0.0, 0.3430000066757202, 0.0)
    }

    fun levitationCollision3() {
        val connection = createConnection(3)
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        connection.world[Vec3i(12, 12, 4)] = SlabTest0.top
        player.applyLevitation(1)
        player.runTicks(27)
        player.assertPosition(12.0, 10.700000047683716, 4.0)
        player.assertVelocity(0.0, 0.01960000038146973, 0.0)
    }

    fun levitationCollision4() {
        val connection = createConnection(3)
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        connection.world[Vec3i(12, 12, 4)] = SlabTest0.top
        player.applyLevitation(3)
        player.runTicks(27)
        player.assertPosition(12.0, 10.700000047683716, 4.0)
        player.assertVelocity(0.0, 0.03920000076293946, 0.0)
    }

    fun levitationMovement1() {
        val connection = createConnection(3)
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.input = PlayerMovementInput(forward = true)
        player.applyLevitation(3)
        player.runTicks(22)
        player.assertPosition(12.0, 12.15637572823701, 6.865653977122466)
        player.assertVelocity(0.0, 0.18062290764794509, 0.17329121596800717)
    }

    fun levitationMovement2() {
        val connection = createConnection(3)
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.input = PlayerMovementInput(forward = true, left = true)
        player.applyLevitation(3)
        player.runTicks(22)
        player.assertPosition(14.067676857469072, 12.15637572823701, 6.067676857469071)
        player.assertVelocity(0.12503611382261814, 0.18062290764794509, 0.12503611382261814)
    }

    fun levitationMovement3() {
        val connection = createConnection(3)
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.forceRotate(EntityRotation(140.0f, 29.0f))
        player.input = PlayerMovementInput(forward = true, left = true)
        player.applyLevitation(3)
        player.runTicks(21)
        player.assertPosition(9.280562372698041, 11.975989370925113, 3.963403501976169)
        player.assertVelocity(-0.1736532159418235, 0.18038635731189828, -0.0015858482934032185)
    }

    fun levitationMovement4() {
        val connection = createConnection(3)
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.forceRotate(EntityRotation(76.0f, 29.0f))
        player.input = PlayerMovementInput(backward = true, left = true)
        player.applyLevitation(3)
        player.runTicks(21)
        player.assertPosition(14.339963735448979, 11.975989370925113, 5.405876713227169)
        player.assertVelocity(0.14942141851993956, 0.18038635731189828, 0.08977408050054557)
    }

    fun levitationMovement5() {
        val connection = createConnection(3)
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(7.0, 9.0, 4.0))
        player.forceRotate(EntityRotation(1.0f, 38.0f))
        player.input = PlayerMovementInput(backward = true, right = true)
        player.applyLevitation(3)
        player.runTicks(19)
        player.assertPosition(5.367725707227726, 11.616204950967832, 2.3097449032621284)
        player.assertVelocity(-0.11706669618255752, 0.17969978500691763, -0.12122507887124932)
    }

    fun levitationCollisionMovement1() {
        val connection = createConnection(3)
        val player = createPlayer(connection)
        connection.world[Vec3i(12, 12, 4)] = SlabTest0.top
        player.forceTeleport(Vec3d(12.0, 9.0, 4.0))
        player.input = PlayerMovementInput(forward = true)
        player.applyLevitation(3)
        player.runTicks(37)
        player.assertPosition(12.0, 14.217807046683689, 9.922999649479184)
        player.assertVelocity(0.0, 0.1809537602040281, 0.1921301847886176)
    }

    companion object {

        fun LocalPlayerEntity.applyLevitation(level: Int) {
            effects += StatusEffectInstance(MovementEffect.Levitation, level, 1000000)
        }
    }
}
