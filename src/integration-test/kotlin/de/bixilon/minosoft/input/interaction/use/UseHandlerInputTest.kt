/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
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
import de.bixilon.minosoft.input.interaction.KeyHandlerUtil.awaitTicks
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.PlayerActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.SwingArmC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import org.testng.annotations.Test

@Test(groups = ["interaction"], dependsOnGroups = ["item"])
class UseHandlerInputTest {

    fun singlePress() {
        val session = SessionTestUtil.createSession(1)
        val player = PhysicsTestUtil.createPlayer(session)
        val handler = UseHandler(session.camera.interactions)
        player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(EggTest0.item, 16)

        handler.press()
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertPacket(UseItemC2SP::class.java)
        session.assertPacket(SwingArmC2SP::class.java)
        handler.release()
        session.assertNoPacket()
    }

    fun continuesPress() {
        val session = SessionTestUtil.createSession(1)
        val player = PhysicsTestUtil.createPlayer(session)
        val handler = UseHandler(session.camera.interactions)
        player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(EggTest0.item, 16)

        handler.press()
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertPacket(UseItemC2SP::class.java)
        session.assertPacket(SwingArmC2SP::class.java)
        session.assertNoPacket()
        handler.awaitTicks(4)
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertPacket(UseItemC2SP::class.java)
        session.assertPacket(SwingArmC2SP::class.java)
        handler.release()
        session.assertNoPacket()
    }

    fun longUse() {
        val session = SessionTestUtil.createSession(1)
        val player = PhysicsTestUtil.createPlayer(session)
        val handler = UseHandler(session.camera.interactions)
        player.items.inventory[EquipmentSlots.MAIN_HAND] = ItemStack(ShieldItem())

        handler.press()
        session.assertPacket(PositionRotationC2SP::class.java)
        session.assertPacket(UseItemC2SP::class.java)
        handler.awaitTicks(1)
        session.assertNoPacket()
        handler.release()
        session.assertPacket(PlayerActionC2SP::class.java)
    }
}
