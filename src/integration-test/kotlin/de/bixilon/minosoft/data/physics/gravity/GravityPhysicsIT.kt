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
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test

@Test(groups = ["physics"])
class GravityPhysicsIT {
    private val connection by lazy { createConnection(5) }

    fun blankFalling1() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.00, 178.0, 13.0))
        player.runTicks(1)
        player.assertPosition(45.0, 178.0, 13.0, 1)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0, 1)
    }

    fun blankFalling2() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.runTicks(2)
        player.assertPosition(45.0, 177.9215999984741, 13.0, 2)
        player.assertVelocity(0.0, -0.1552320045166016, 0.0, 2)
    }

    fun blankFalling3() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.runTicks(3)
        player.assertPosition(45.0, 177.76636799395752, 13.0, 3)
        player.assertVelocity(0.0, -0.230527368912964, 0.0, 3)
    }

    fun blankFalling90() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.runTicks(90)
        player.assertPosition(45.0, -10.612955100288408, 13.0, 90)
        player.assertVelocity(0.0, -3.2837446328299578, 0.0, 90)
    }

    fun forwardFalling1() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(1)
        player.assertPosition(45.0, 178.0, 13.019599999943376, 1)
        player.assertVelocity(0.0, -0.0784000015258789, 0.017836000462502232, 1)
    }

    fun forwardFalling2() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(2)
        player.assertPosition(45.0, 177.9215999984741, 13.057036000349253, 2)
        player.assertVelocity(0.0, -0.1552320045166016, 0.034066761351146994, 2)
    }

    fun forwardFalling3() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(3)
        player.assertPosition(45.0, 177.76636799395752, 13.110702761643775, 3)
        player.assertVelocity(0.0, -0.230527368912964, 0.04883675418548237, 3)
    }

    fun forwardFalling90() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(90)
        player.assertPosition(45.0, -10.612955100288408, 30.39848246593973, 90)
        player.assertVelocity(0.0, -3.2837446328299578, 0.19813702926258814, 90)
    }

    fun backwardsFalling1() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.input = PlayerMovementInput(backward = true)
        player.runTicks(1)
        player.assertPosition(45.0, 178.0, 12.980400000056624, 1)
        player.assertVelocity(0.0, -0.0784000015258789, -0.017836000462502232, 1)
    }

    fun backwardsFalling2() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.input = PlayerMovementInput(backward = true)
        player.runTicks(2)
        player.assertPosition(45.0, 177.9215999984741, 12.942963999650747, 2)
        player.assertVelocity(0.0, -0.1552320045166016, -0.034066761351146994, 2)
    }

    fun backwardsFalling3() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.input = PlayerMovementInput(backward = true)
        player.runTicks(3)
        player.assertPosition(45.0, 177.76636799395752, 12.889297238356225, 3)
        player.assertVelocity(0.0, -0.230527368912964, -0.04883675418548237, 3)
    }

    fun backwardsFalling90() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.input = PlayerMovementInput(backward = true)
        player.runTicks(90)
        player.assertPosition(45.0, -10.612955100288408, -4.398482465939721, 90)
        player.assertVelocity(0.0, -3.2837446328299578, -0.19813702926258814, 90)
    }

    fun positiveFalling37() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.input = PlayerMovementInput(forward = true, right = true)
        player.runTicks(37)
        player.assertPosition(40.72633353510353, 136.14441316887843, 17.273666464896486, 37)
        player.assertVelocity(-0.13862913662297455, -2.063689118167052, 0.13862913662297455, 37)
    }

    fun negativeFalling124() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.input = PlayerMovementInput(backward = true, left = true)
        player.runTicks(124)
        player.assertPosition(62.89592991916189, -128.08640972995516, -4.895929919161902, 124)
        player.assertVelocity(0.14299155476093758, -3.599877832744839, -0.14299155476093758, 124)
    }

    fun rotatedFalling5() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.forceRotate(EntityRotation(23.0f, 86.0f))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(5)
        player.assertPosition(44.89801306123169, 177.23152379758702, 13.240267603596612, 5)
        player.assertVelocity(-0.029112635668382618, -0.37663049823865513, 0.06858548056152435, 5)
    }

    fun rotatedFalling12() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(45.0, 178.0, 13.0))
        player.forceRotate(EntityRotation(123.0f, 23.0f))
        player.input = PlayerMovementInput(backward = true, left = true)
        player.runTicks(12)
        player.assertPosition(45.237969193687825, 173.15552175294314, 14.119310744403338, 12)
        player.assertVelocity(0.02849208777799813, -0.8439105457704985, 0.13401524578106938, 12)
    }
}
