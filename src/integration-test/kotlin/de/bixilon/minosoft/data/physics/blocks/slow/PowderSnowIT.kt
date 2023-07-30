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

package de.bixilon.minosoft.data.physics.blocks.slow

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.registries.item.items.armor.materials.LeatherArmor
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.test.IT
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class PowderSnowIT : SlowMovementIT() {

    @Test(priority = -1)
    fun getPowderSnow() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.POWDER_SNOW]?.states?.default ?: throw SkipException("Can not find powder snow!")
    }

    private fun LocalPlayerEntity.setFrozenTicks(ticks: Int) {
        this.data[Entity.TICKS_FROZEN_DATA] = ticks
    }

    private fun LocalPlayerEntity.addLeatherBoots() {
        equipment[EquipmentSlots.FEET] = ItemStack(IT.REGISTRIES.item[LeatherArmor.LeatherBoots]!!)
    }

    fun powderSnowLanding() {
        val player = landing()

        player.assertPosition(5.0, 9.870046447769717, 5.0)
        player.assertVelocity(0.0, -0.1552320045166016, 0.0)
        player.assertGround(false)
    }

    fun powderSnowLandingLeather() {
        val player = createPlayer(createConnection(3))
        player.addLeatherBoots()
        landing(player)

        player.assertPosition(5.0, 11.0, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun powderSnowDescendingLeather() {
        val player = createPlayer(createConnection(3))
        player.addLeatherBoots()
        landing(player)
        player.input = PlayerMovementInput(sneak = true)
        player.runTicks(5)

        player.assertPosition(5.0, 10.335951984832766, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun powderSnowFalling() {
        val player = falling()

        player.assertPosition(5.0, 9.904751976440435, 5.0)
        player.assertVelocity(0.0, -0.1552320045166016, 0.0)
        player.assertGround(false)
    }

    fun powderSnowFallingLeather() {
        val player = createPlayer(createConnection(3))
        player.addLeatherBoots()
        falling(player)

        player.assertPosition(5.0, 11.0, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun powderSnowLongFalling() {
        val player = createPlayer(createConnection(2))
        player.forceTeleport(Vec3d(5.0, 15.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.runTicks(20)

        player.assertPosition(5.0, 7.0703936080893826, 5.0)
        player.assertVelocity(0.0, -0.6517088341626173, 0.0)
        player.assertGround(false)
    }

    fun powderSnowFallingForwardsMovement() {
        val player = fallingForwardsMovement()

        player.assertPosition(5.0, 9.904751976440435, 5.2307760810686075)
        player.assertVelocity(0.0, -0.1552320045166016, 0.017836000462502232)
    }

    fun powderSnowFallingForwardsMovement2() {
        val player = createPlayer(createConnection(3))
        player.setFrozenTicks(20)
        super.fallingForwardsMovement(player)

        player.assertPosition(5.0, 9.904751976440435, 5.2307760810686075)
        player.assertVelocity(0.0, -0.1552320045166016, 0.017836000462502232)
    }

    fun powderSnowForwardsMovement1() {
        val player = forwardsMovement()

        player.assertPosition(5.0, 10.0, 5.758892404971276)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun powderSnowForwardsMovement2() {
        val player = createPlayer(createConnection(3))
        player.setFrozenTicks(20)
        forwardsMovement(player)

        player.assertPosition(5.0, 10.0, 5.708492408329568)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun powderSnowForwardsMovement3() {
        val player = createPlayer(createConnection(3))
        player.setFrozenTicks(50)
        forwardsMovement(player)

        player.assertPosition(5.0, 10.0, 5.632892413367016)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun powderSnowForwardsMovement4() {
        val player = createPlayer(createConnection(3))
        player.setFrozenTicks(100)
        forwardsMovement(player)

        player.assertPosition(5.0, 10.0, 5.506892369191454)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun powderSnowForwardsMovement5() {
        val player = createPlayer(createConnection(3))
        player.setFrozenTicks(1000)
        forwardsMovement(player)

        player.assertPosition(5.0, 10.0, 5.406092402193699)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun powderSnowForwardsMovement6() {
        val player = createPlayer(createConnection(3))
        player.setFrozenTicks(1000000)
        forwardsMovement(player)

        player.assertPosition(5.0, 10.0, 5.406092402193699)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun powderSnowSidewaysMovement1() {
        val player = super.sidewaysMovement1()

        player.assertPosition(5.2307760810686075, 9.904751976440435, 5.0)
        player.assertVelocity(0.017836000462502232, -0.1552320045166016, 0.0)
    }

    fun powderSnowSidewaysMovement2() {
        val player = super.sidewaysMovement2()

        player.assertPosition(4.166712796975605, 7.653729617588597, 5.0)
        player.assertVelocity(-0.1210041730153868, -0.7170746714356033, 0.0)
    }

    fun powderSnowCombinedMovement() {
        val player = super.combinedMovement()

        player.assertPosition(4.398752019932013, 7.653729617588597, 5.601247980067987)
        player.assertVelocity(-0.08730905063853293, -0.7170746714356033, 0.08730905063853293)
    }

    fun powderSnowStanding() {
        val player = super.standing()

        player.assertPosition(5.0, 10.0, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun powderSnowStandingJump1() {
        val player = super.standingJump1()

        player.assertPosition(5.0, 10.629999980330467, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun powderSnowStandingJump2() {
        val player = super.standingJump2()

        player.assertPosition(5.0, 10.394799975752832, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun powderSnowUnfreeze() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(5.0, 10.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.connection.world[Vec3i(5, 9, 5)] = StoneTest0.state
        player.connection.world[Vec3i(5, 10, 6)] = block
        player.connection.world[Vec3i(5, 9, 6)] = StoneTest0.state

        player.runTicks(5)
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(5)
        player.setFrozenTicks(90)
        player.runTicks(5)
        player.setFrozenTicks(0)
        player.runTicks(5)

        player.assertPosition(5.0, 10.0, 6.181250019861122)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }
}
