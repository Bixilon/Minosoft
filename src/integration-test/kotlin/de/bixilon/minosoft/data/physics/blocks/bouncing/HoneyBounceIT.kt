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

package de.bixilon.minosoft.data.physics.blocks.bouncing

import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.test.IT
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class HoneyBounceIT : BounceIT() {

    @Test(priority = -1)
    fun getHoney() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.HONEY_BLOCK]?.states?.default ?: throw SkipException("Can not find honey!")
    }

    fun honeyLanding1() {
        val player = super.landing()

        player.assertPosition(5.0, 10.9375, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun honeyLongFall() {
        val player = super.longFall()

        player.assertPosition(5.0, 10.9375, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun honeyStillJump() {
        val player = super.stillJump()

        player.assertPosition(5.0, 10.9375, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun honeyFallJump() {
        val player = super.fallJump()

        player.assertPosition(5.0, 11.288474942991385, 5.0)
        player.assertVelocity(0.0, -0.11061950482553849, 0.0)
    }

    fun honeySlabJump() {
        val player = super.slabJump()

        player.assertPosition(5.0, 11.5, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun honeySliding1() {
        val player = createPlayer(createSession(2))
        player.forceTeleport(Vec3d(5.0, 11.0, 5.0))
        player.session.world.fill(BlockPosition(5, 1, 6), BlockPosition(5, 10, 6), block)
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(30)

        player.assertPosition(5.0, 5.630038399680596, 5.762499988079071)
        player.assertVelocity(0.0, -0.12740000247955321, 0.0)
    }
}
