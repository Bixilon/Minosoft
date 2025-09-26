/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.test.IT
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class RotatingTest {
    private val session by lazy {
        val session = createSession(5)
        session.world.fill(BlockPosition(-20, 0, -20), BlockPosition(20, 0, 20), IT.BLOCK_1)

        return@lazy session
    }

    fun landing() {
        val player = createPlayer(session)
        player.forceTeleport(Vec3d(6.0, 5.0, 6.0))
        player.input = PlayerMovementInput(forward = true)
        for (x in 0..100) {
            player.tick()
            player.forceRotate(EntityRotation(kotlin.math.sin(x / PIf / 100.0f) * 400.0f, 0.0f))
        }
        player.assertPosition(-9.15550880091356, 1.0, 13.327908590122458)
        player.assertVelocity(-0.09932769708439651, -0.0784000015258789, -0.06331093297127419)
    }
}
