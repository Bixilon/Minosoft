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

package de.bixilon.minosoft.data.physics.server

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class ServerVelocityIT {
    private val connection by lazy {
        val connection = createConnection(5)
        connection.world.fill(Vec3i(-20, 0, -20), Vec3i(20, 0, 20), StoneTest0.state)

        return@lazy connection
    }

    fun serverVelocity1() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 1.0, -11.0))
        player.physics.velocity = Vec3d(1.4, 2.4, 1.3)
        player.runTicks(10)
        player.assertPosition(-0.5020274821311955, 19.60499464246711, -2.180454090550392)
        player.assertVelocity(0.5451827224860496, 1.2439004467539785, 0.506241099451332)
    }

    fun serverVelocity2() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-10.0, 10.0, -11.0))
        player.physics.velocity = Vec3d(-0.5, -2.1, -3.1)
        player.runTicks(10)
        player.assertPosition(-13.392133042096003, -12.553627279180112, -32.03122486099521)
        player.assertVelocity(-0.19470811517358924, -2.432927899851489, -1.207190314076253)
    }
}
