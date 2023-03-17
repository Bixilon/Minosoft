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

package de.bixilon.minosoft.data.physics

import de.bixilon.kotlinglm.GLM.PIf
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertEquals
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.physics.potion.LevitationIT.Companion.applyLevitation
import de.bixilon.minosoft.data.physics.potion.SlowFallingIT.Companion.applySlowFalling
import de.bixilon.minosoft.data.registries.fluid.fluids.LavaFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.item.items.armor.extra.ElytraItem
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.input.camera.MovementInputActions
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class ElytraFlyIT {

    private fun LocalPlayerEntity.equip() {
        val stack = ItemStack(connection.registries.item[ElytraItem] ?: throw SkipException("Can not get elytra!"))
        equipment[EquipmentSlots.CHEST] = stack
    }

    private fun LocalPlayerEntity.startElytra() {
        input = PlayerMovementInput(jump = true)
        inputActions = MovementInputActions(startElytraFly = true)
    }

    fun startFly() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.5, 8.0))
        player.equip()

        player.runTicks(3)
        player.startElytra()
        player.runTicks(1)

        assertTrue(player.isFlyingWithElytra)

        player.assertPosition(17.0, 9.045402850275698, 8.022321988785174)
        player.assertVelocity(0.0, -0.22096514368182157, 0.02232198878517446)
    }

    fun notFly() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.5, 8.0))
        player.equip()

        player.runTicks(3)

        assertFalse(player.isFlyingWithElytra)
    }

    fun flyStraight() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(0.0, 30.5, 0.0))
        player.forceRotate(EntityRotation(0.0f, 10.0f))
        player.equip()

        player.runTicks(3)
        player.startElytra()
        player.runTicks(15)

        assertTrue(player.isFlyingWithElytra)

        player.assertPosition(0.0, 27.344325090018774, 2.25861571106837)
        player.assertVelocity(0.0, -0.1777975972596197, 0.2653968799703123)
    }

    fun flyUp() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(0.0, 30.5, 0.0))
        player.equip()

        player.runTicks(3)
        player.startElytra()
        player.runTicks(1)

        player.physics.velocity = Vec3d(0.0, 0.0, 3.0)
        player.forceRotate(EntityRotation(0.0f, -10.0f))
        player.runTicks(5)

        player.assertPosition(0.0, 30.67722863096575, 14.318547745315495)
        player.assertVelocity(0.0, 0.20304412254090723, 2.7666904258499585)
    }

    fun flyRotated() {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(0.0, 30.5, 0.0))
        player.equip()

        player.runTicks(3)
        player.startElytra()
        player.runTicks(1)

        for (x in 0..100) {
            player.tick()
            player.forceRotate(EntityRotation(kotlin.math.sin(x.toFloat() / PIf / 100.0f) * 400.0f, 0.0f))
        }
        assertEquals(player.physics.rotation.yaw, 125.184715f)

        player.assertPosition(-47.475471315052964, 14.412523037084584, 18.44373760001819)
        player.assertVelocity(-0.8389563402612562, -0.1494917721422921, -0.3833823856891355)
    }

    fun startFlyingInWater() {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(0.0, 31.8, 0.0))
        player.connection.world.fill(Vec3i(-3, 30, -3), Vec3i(3, 33, 3), player.connection.registries.block[WaterFluid]!!.defaultState)

        player.equip()

        player.runTicks(3)
        player.startElytra()
        player.runTicks(5)

        assertFalse(player.isFlyingWithElytra)

        player.assertPosition(0.0, 32.166172474654296, 0.0)
        player.assertVelocity(0.0, 0.0867655049639091, 0.0)
    }

    fun startFlyingInLava() {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(0.0, 31.8, 0.0))
        player.connection.world.fill(Vec3i(-3, 30, -3), Vec3i(3, 33, 3), player.connection.registries.block[LavaFluid]!!.defaultState)
        player.equip()

        player.runTicks(3)
        player.startElytra()
        player.runTicks(5)

        assertTrue(player.isFlyingWithElytra)

        player.assertPosition(0.0, 31.884374993629752, 0.0)
        player.assertVelocity(0.0, -4.470348362317633E-10, 0.0)
    }

    fun levitation() {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(0.0, 30.8, 0.0))
        player.applyLevitation(1)
        player.equip()

        player.runTicks(3)
        player.startElytra()
        player.runTicks(10)

        assertFalse(player.isFlyingWithElytra)

        player.assertPosition(0.0, 31.577294243594405, 0.0)
        player.assertVelocity(0.0, 0.0869044602032835, 0.0)
    }

    fun slowFalling() {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(0.0, 30.8, 0.0))
        player.applySlowFalling()
        player.equip()

        player.runTicks(3)
        player.startElytra()
        player.runTicks(10)

        assertTrue(player.isFlyingWithElytra)

        player.assertPosition(0.0, 30.529788468834735, 0.024346678853114284)
        player.assertVelocity(0.0, -0.021572288577037305, 0.002179241375646837)
    }
}
