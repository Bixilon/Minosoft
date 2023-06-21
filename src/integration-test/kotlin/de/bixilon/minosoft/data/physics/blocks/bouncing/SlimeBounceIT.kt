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

package de.bixilon.minosoft.data.physics.blocks.bouncing

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.test.IT
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class SlimeBounceIT : BounceIT() {

    @Test(priority = -1)
    fun getSlime() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.SLIME_BLOCK]?.states?.default ?: throw SkipException("Can not find slime!")
    }

    fun slimeLanding() {
        val player = super.landing()

        player.assertPosition(5.0, 11.664211276933782, 5.0)
        player.assertVelocity(0.0, -0.036186325041706796, 0.0)
        player.assertGround(false)
    }

    fun slimeLongFall() {
        val player = super.longFall()

        player.assertPosition(5.0, 23.946450471654025, 5.0)
        player.assertVelocity(0.0, 0.9170712603890487, 0.0)
    }

    fun slimeStillJump() {
        val player = super.stillJump()

        player.assertPosition(5.0, 11.796735600668693, 5.0)
        player.assertVelocity(0.0, -0.30153472366278034, 0.0)
    }

    fun slimeFallJump1() {
        val player = super.fallJump()

        player.assertPosition(5.0, 11.0, 5.0)
        player.assertVelocity(0.0, 0.3575294520655247, 0.0)
        player.assertGround(true)
    }

    fun slimeSlabJump() {
        val player = super.slabJump()

        player.assertPosition(5.0, 11.995200877005914, 5.0)
        player.assertVelocity(0.0, -0.3739040364667221, 0.0)
    }

    fun slimeFallJump2() {
        val player = createPlayer(createConnection(2))
        player.forceTeleport(Vec3d(5.0, 12.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(33)

        player.assertPosition(5.0, 12.001335979112149, 5.0)
        player.assertVelocity(0.0, 0.16477328182606651, 0.0)
    }
}
