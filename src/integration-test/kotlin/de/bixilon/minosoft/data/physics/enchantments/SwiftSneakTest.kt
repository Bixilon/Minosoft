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

package de.bixilon.minosoft.data.physics.enchantments

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.registries.enchantment.armor.MovementEnchantment
import de.bixilon.minosoft.data.registries.item.items.armor.materials.IronArmor
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.test.IT
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class SwiftSneakTest {
    private val connection by lazy {
        val connection = createConnection(5)
        connection.world.fill(Vec3i(-20, 0, -20), Vec3i(20, 0, 20), StoneTest0.state)

        return@lazy connection
    }

    private fun LocalPlayerEntity.applySwiftSneak(level: Int, slot: EquipmentSlots = EquipmentSlots.LEGS) {
        val boots = ItemStack(IT.REGISTRIES.item[IronArmor.IronLeggings]!!)
        boots.enchanting.enchantments[MovementEnchantment.SwiftSneak] = level

        this.equipment[slot] = boots
    }

    fun wrongItem() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(6.0, 1.0, 6.0))
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.applySwiftSneak(1, EquipmentSlots.HEAD)

        player.runTicks(20)

        player.assertPosition(6.0, 1.0, 7.127381519292467)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03535725486625218)
        player.assertGround(true)
    }

    fun swiftSneak1() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(6.0, 1.0, 6.0))
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.applySwiftSneak(1)

        player.runTicks(20)

        player.assertPosition(6.0, 1.0, 7.691072164657802)
        player.assertVelocity(0.0, -0.0784000015258789, 0.053035878715268735)
        player.assertGround(true)
    }

    fun swiftSneak2() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(6.0, 1.0, 6.0))
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.applySwiftSneak(2)

        player.runTicks(20)

        player.assertPosition(6.0, 1.0, 8.254763038584935)
        player.assertVelocity(0.0, -0.0784000015258789, 0.07071450973250436)
        player.assertGround(true)
    }

    fun swiftSneak3() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(6.0, 1.0, 6.0))
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.applySwiftSneak(3)

        player.runTicks(15)

        player.assertPosition(6.0, 1.0, 8.009032542296579)
        player.assertVelocity(0.0, -0.0784000015258789, 0.08837021246297563)
        player.assertGround(true)
    }

    fun swiftSneak10() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(6.0, 1.0, 6.0))
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.applySwiftSneak(10)

        player.runTicks(15)

        player.assertPosition(6.0, 1.0, 8.678710056395438)
        player.assertVelocity(0.0, -0.0784000015258789, 0.11782694995063417)
        player.assertGround(true)
    }

    fun swiftSneak100() {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(6.0, 1.0, 6.0))
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.applySwiftSneak(10)

        player.runTicks(15)

        player.assertPosition(6.0, 1.0, 8.678710056395438)
        player.assertVelocity(0.0, -0.0784000015258789, 0.11782694995063417)
        player.assertGround(true)
    }
}
