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

package de.bixilon.minosoft.data.physics.precision

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test

@Test(groups = ["physics"])
class VelocityFlatteningIT {

    fun flattenVelocity1() {
        val player = createPlayer(createConnection(1))
        player.forceTeleport(Vec3d(0.0, 0.0, 0.0))
        player.physics.velocity = Vec3d(0.002, 0.002, 0.002)
        player.runTicks(1)
        player.assertPosition(0.0, 0.0, 0.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun flattenVelocity1n() {
        val player = createPlayer(createConnection(1))
        player.forceTeleport(Vec3d(0.0, 0.0, 0.0))
        player.physics.velocity = Vec3d(-0.002, -0.002, -0.002)
        player.runTicks(1)
        player.assertPosition(0.0, 0.0, 0.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun flattenVelocity2() {
        val player = createPlayer(createConnection(1))
        player.forceTeleport(Vec3d(0.0, 0.0, 0.0))
        player.physics.velocity = Vec3d(0.003, 0.003, 0.003)
        player.runTicks(1)
        player.assertPosition(0.003, 0.003, 0.003)
        player.assertVelocity(0.002730000078678131, -0.07546000146865844, 0.002730000078678131)
    }

    fun flattenVelocity2n() {
        val player = createPlayer(createConnection(1))
        player.forceTeleport(Vec3d(0.0, 0.0, 0.0))
        player.physics.velocity = Vec3d(-0.003, -0.003, -0.003)
        player.runTicks(1)
        player.assertPosition(-0.003, -0.003, -0.003)
        player.assertVelocity(-0.002730000078678131, -0.08134000158309937, -0.002730000078678131)
    }

    fun flattenVelocity3() {
        val player = createPlayer(createConnection(1))
        player.forceTeleport(Vec3d(0.0, 0.0, 0.0))
        player.physics.velocity = Vec3d(0.004, 0.004, 0.004)
        player.runTicks(1)
        player.assertPosition(0.004, 0.004, 0.004)
        player.assertVelocity(0.0036400001049041748, -0.07448000144958496, 0.0036400001049041748)
    }

    fun flattenVelocity3n() {
        val player = createPlayer(createConnection(1))
        player.forceTeleport(Vec3d(0.0, 0.0, 0.0))
        player.physics.velocity = Vec3d(-0.004, -0.004, -0.004)
        player.runTicks(1)
        player.assertPosition(-0.004, -0.004, -0.004)
        player.assertVelocity(-0.0036400001049041748, -0.08232000160217286, -0.0036400001049041748)
    }

    fun flattenVelocity4() {
        val player = createPlayer(createConnection(1))
        player.forceTeleport(Vec3d(0.0, 0.0, 0.0))
        player.physics.velocity = Vec3d(0.005, 0.005, 0.005)
        player.runTicks(1)
        player.assertPosition(0.005, 0.005, 0.005)
        player.assertVelocity(0.004550000131130219, -0.07350000143051147, 0.004550000131130219)
    }

    fun flattenVelocity4n() {
        val player = createPlayer(createConnection(1))
        player.forceTeleport(Vec3d(0.0, 0.0, 0.0))
        player.physics.velocity = Vec3d(-0.005, -0.005, -0.005)
        player.runTicks(1)
        player.assertPosition(-0.005, -0.005, -0.005)
        player.assertVelocity(-0.004550000131130219, -0.08330000162124634, -0.004550000131130219)
    }
}
