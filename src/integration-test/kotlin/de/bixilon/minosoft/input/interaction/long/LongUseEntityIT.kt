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

package de.bixilon.minosoft.input.interaction.long

import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.item.items.weapon.defend.ShieldItem
import de.bixilon.minosoft.input.interaction.InteractionTestUtil
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.assertUseItem
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.lookAtPig
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.tick
import de.bixilon.minosoft.input.interaction.InteractionTestUtil.unsafePress
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityEmptyInteractC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.interact.EntityInteractPositionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.PositionRotationC2SP
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item", "block"])
class LongUseEntityIT {


    fun shieldOnPig() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())
        connection.lookAtPig()
        val use = connection.camera.interactions.use


        use.unsafePress()
        use.tick(10)
        connection.assertPacket(EntityInteractPositionC2SP::class.java).let { assertEquals(it.hand, Hands.MAIN) }
        connection.assertPacket(EntityEmptyInteractC2SP::class.java).let { assertEquals(it.hand, Hands.MAIN) }

        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.MAIN)

        connection.assertNoPacket()
    }

    fun shieldOffhandOnPig() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.OFF_HAND] = ItemStack(ShieldItem())
        connection.lookAtPig()
        val use = connection.camera.interactions.use


        use.unsafePress()
        use.tick(10)

        connection.assertPacket(EntityInteractPositionC2SP::class.java).let { assertEquals(it.hand, Hands.MAIN) }
        connection.assertPacket(EntityEmptyInteractC2SP::class.java).let { assertEquals(it.hand, Hands.MAIN) }
        connection.assertPacket(EntityInteractPositionC2SP::class.java).let { assertEquals(it.hand, Hands.OFF) }
        connection.assertPacket(EntityEmptyInteractC2SP::class.java).let { assertEquals(it.hand, Hands.OFF) }

        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertUseItem(Hands.OFF)

        connection.assertNoPacket()
    }

    fun carrotsOnPigWithHunger() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())
        connection.lookAtPig()
        val use = connection.camera.interactions.use


        use.unsafePress()
        use.tick(10)
        // TODO: use entity at (h=0)
        // TODO: use entity (h=0)

        // TODO: arm animation (after animation status packet)

        // TODO: continuing of use entity packets
    }

    fun goldenAppleOnPig() {
        val connection = InteractionTestUtil.createConnection()
        connection.player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())
        connection.lookAtPig()
        val use = connection.camera.interactions.use


        use.unsafePress()
        use.tick(10)
        // TODO: use entity at (h=0)
        // TODO: use entity (h=0)
        // position look
        // use item
        // wait
        // use entity at
        // use entity
        // position look
        // use item
        // block dig (stopping)
    }
}
