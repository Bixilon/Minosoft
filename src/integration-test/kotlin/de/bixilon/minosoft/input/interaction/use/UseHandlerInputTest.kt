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

package de.bixilon.minosoft.input.interaction.use

import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.physics.PhysicsTestUtil
import de.bixilon.minosoft.data.registries.item.items.weapon.defend.ShieldItem
import de.bixilon.minosoft.data.registries.items.EggTest0
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.PlayerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.SwingArmC2SP
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item"])
class UseHandlerInputTest {

    fun singlePress() {
        val connection = ConnectionTestUtil.createConnection(1)
        val player = PhysicsTestUtil.createPlayer(connection)
        val handler = UseHandler(connection.camera.interactions)
        player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(EggTest0.item, 16)

        handler.press()
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertPacket(UseItemC2SP::class.java)
        connection.assertPacket(SwingArmC2SP::class.java)
        handler.release()
        connection.assertNoPacket()
    }

    fun continuesPress() {
        val connection = ConnectionTestUtil.createConnection(1)
        val player = PhysicsTestUtil.createPlayer(connection)
        val handler = UseHandler(connection.camera.interactions)
        player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(EggTest0.item, 16)

        handler.press()
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertPacket(UseItemC2SP::class.java)
        connection.assertPacket(SwingArmC2SP::class.java)
        Thread.sleep(4 * 50 + 10)
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertPacket(UseItemC2SP::class.java)
        connection.assertPacket(SwingArmC2SP::class.java)
        handler.release()
        connection.assertNoPacket()
    }

    fun longUse() {
        val connection = ConnectionTestUtil.createConnection(1)
        val player = PhysicsTestUtil.createPlayer(connection)
        val handler = UseHandler(connection.camera.interactions)
        player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())

        handler.press()
        connection.assertPacket(PositionRotationC2SP::class.java)
        connection.assertPacket(UseItemC2SP::class.java)
        Thread.sleep(55)
        connection.assertNoPacket()
        handler.release()
        connection.assertPacket(PlayerActionC2SP::class.java)
    }
}
