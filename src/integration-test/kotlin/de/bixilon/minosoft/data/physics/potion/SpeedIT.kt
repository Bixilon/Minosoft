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
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.applySlowness
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.applySpeed
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class SpeedIT {
    private val connection by lazy {
        val connection = createConnection(5)
        connection.world.fill(Vec3i(-20, 0, -20), Vec3i(20, 0, 20), StoneTest0.state)

        return@lazy connection
    }

    fun speedWalk1() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.applySpeed(1)
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)
        player.assertPosition(-10.0, 1.0, -8.811469675062929)
        player.assertVelocity(0.0, -0.0784000015258789, 0.16396849920155476)
        player.assertGround(true)
    }

    fun speedWalk2() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.applySpeed(2)
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)
        player.assertPosition(-10.0, 1.0, -8.517605441930291)
        player.assertVelocity(0.0, -0.0784000015258789, 0.18735412633674173)
        player.assertGround(true)
    }

    fun speedWalk3() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.applySpeed(3)
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)
        player.assertPosition(-10.0, 1.0, -8.223740989851695)
        player.assertVelocity(0.0, -0.0784000015258789, 0.21073977089558227)
        player.assertGround(true)
    }

    fun speedWalk4() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.applySpeed(1)
        player.input = PlayerMovementInput(forward = true, left = true)
        player.runTicks(50)
        player.assertPosition(0.29948633645105016, 1.0, -0.7005136635489497)
        player.assertVelocity(0.11905563086483577, -0.0784000015258789, 0.11905563086483577)
        player.assertGround(true)
    }

    fun slownessWalk1() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.applySlowness(1)
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)
        player.assertPosition(-10.0, 1.0, -9.839994709973121)
        player.assertVelocity(0.0, -0.0784000015258789, 0.08211878680474685)
        player.assertGround(true)
    }

    fun slownessWalk2() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.applySlowness(2)
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)
        player.assertPosition(-10.0, 1.0, -10.06039293955909)
        player.assertVelocity(0.0, -0.0784000015258789, 0.06457956209744323)
        player.assertGround(true)
    }

    fun slownessWalk3() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.applySlowness(3)
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)
        player.assertPosition(-10.0, 1.0, -10.28079116914506)
        player.assertVelocity(0.0, -0.0784000015258789, 0.04704033739013962)
        player.assertGround(true)
    }

    fun slownessWalk4() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.applySlowness(1)
        player.input = PlayerMovementInput(forward = true, left = true)
        player.runTicks(50)
        player.assertPosition(-4.802609044696876, 1.0, -5.802609044696876)
        player.assertVelocity(0.05952781543242086, -0.0784000015258789, 0.05952781543242086)
        player.assertGround(true)
    }

    fun speedRotated1() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.applySpeed(1)
        player.input = PlayerMovementInput(forward = true)
        player.forceRotate(EntityRotation(79.0f, 4.0f))
        player.runTicks(50)
        player.assertPosition(-22.866750477070855, -4.68838879282739, -8.498302669811709)
        player.assertVelocity(-0.182994765116179, -0.9054323524772837, 0.03557988601282002)
        player.assertGround(false)
    }

    fun speedJump1() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.applySpeed(1)
        player.input = PlayerMovementInput(forward = true, jump = true)
        player.forceRotate(EntityRotation(12.0f, 4.0f))
        player.runTicks(50)
        player.assertPosition(-11.96629594610332, 1.0, -1.7469784584323824)
        player.assertVelocity(-0.03977211612750839, -0.0784000015258789, 0.1871601515585018)
        player.assertGround(true)
    }

    fun speedJump3() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.applySpeed(3)
        player.input = PlayerMovementInput(forward = true, jump = true)
        player.forceRotate(EntityRotation(54.0f, 4.0f))
        player.runTicks(37)
        player.assertPosition(-15.918122160371535, 1.1212968405391892, -6.699885747361271)
        player.assertVelocity(-0.16172720012853317, -0.4448259643949201, 0.11751116645899087)
        player.assertGround(false)
    }

    fun speedSprintJump1() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.applySpeed(3)
        player.input = PlayerMovementInput(forward = true, jump = true, sprint = true)
        player.forceRotate(EntityRotation(54.0f, 4.0f))
        player.runTicks(33)
        player.assertPosition(-19.201682667319968, 2.176759275064237, -4.314046925256089)
        player.assertVelocity(-0.2727865709148412, -0.15233518685055708, 0.19820703197128983)
        player.assertGround(false)
    }
}
