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
import de.bixilon.minosoft.data.entities.StatusEffectInstance
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.registries.effects.vision.VisionEffect
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.Assert
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class BlindnessIT {

    private fun LocalPlayerEntity.applyBlindness() {
        effects += StatusEffectInstance(VisionEffect.Blindness, 1, 1000000)
    }

    fun blindness5() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.applyBlindness()
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(5)
        player.assertPosition(17.0, 9.0, 8.550090466546338)
        player.assertVelocity(0.0, -0.0784000015258789, 0.10422007506984735)
        Assert.assertFalse(player.isSprinting)
        player.assertGround()
    }

    fun blindness30() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.applyBlindness()
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(30)
        player.assertPosition(17.0, -6.909458649259166, 13.193021665078014)
        player.assertVelocity(0.0, -1.406611164032995, 0.18589753686745414)
        Assert.assertFalse(player.isSprinting)
    }
}
