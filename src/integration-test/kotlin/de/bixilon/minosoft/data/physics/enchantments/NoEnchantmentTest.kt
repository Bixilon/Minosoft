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

package de.bixilon.minosoft.data.physics.enchantments

import glm_.vec3.Vec3d
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.physics.enchantments.EnchantmentTestUtil.applySoulSpeed
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.test.IT
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class NoEnchantmentTest {
    private val session by lazy {
        val session = createSession(5)
        session.world.fill(BlockPosition(-20, 0, -20), BlockPosition(20, 0, 20), IT.BLOCK_1)

        return@lazy session
    }

    fun stoneSoulSpeed() {
        val player = createPlayer(session)
        player.forceTeleport(Vec3d(6.0, 1.0, 6.0))
        player.input = PlayerMovementInput(forward = true)
        player.applySoulSpeed(1)
        player.runTicks(20)
        player.assertPosition(6.0, 1.0, 9.757938092892497)
        player.assertVelocity(0.0, -0.0784000015258789, 0.11785750666321516)
        player.assertGround(true)
    }
}
